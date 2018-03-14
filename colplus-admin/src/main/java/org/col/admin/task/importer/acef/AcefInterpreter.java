package org.col.admin.task.importer.acef;

import static org.col.parser.SafeParser.parse;
import java.time.LocalDate;
import java.util.Set;
import org.col.admin.task.importer.InsertMetadata;
import org.col.admin.task.importer.InterpreterBase;
import org.col.admin.task.importer.neo.ReferenceStore;
import org.col.admin.task.importer.neo.model.NeoTaxon;
import org.col.admin.task.importer.neo.model.UnescapedVerbatimRecord;
import org.col.api.model.Classification;
import org.col.api.model.Dataset;
import org.col.api.model.Distribution;
import org.col.api.model.Name;
import org.col.api.model.Reference;
import org.col.api.model.Referenced;
import org.col.api.model.Taxon;
import org.col.api.model.TermRecord;
import org.col.api.model.VerbatimRecord;
import org.col.api.model.VernacularName;
import org.col.api.vocab.DistributionStatus;
import org.col.api.vocab.Gazetteer;
import org.col.api.vocab.Issue;
import org.col.api.vocab.Lifezone;
import org.col.api.vocab.Origin;
import org.col.api.vocab.TaxonomicStatus;
import org.col.parser.AreaParser;
import org.col.parser.CountryParser;
import org.col.parser.DistributionStatusParser;
import org.col.parser.GazetteerParser;
import org.col.parser.LanguageParser;
import org.col.parser.LifezoneParser;
import org.col.parser.RankParser;
import org.col.parser.SafeParser;
import org.col.parser.TaxonomicStatusParser;
import org.gbif.dwc.terms.AcefTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.nameparser.api.Rank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;

/**
 * Interprets a verbatim ACEF record and transforms it into a name, taxon and unique references.
 */
public class AcefInterpreter extends InterpreterBase {
  private static final Logger LOG = LoggerFactory.getLogger(AcefInterpreter.class);

  public AcefInterpreter(Dataset dataset, InsertMetadata metadata, ReferenceStore refStore) {
    super(dataset, refStore);
    // turn on normalization of flat classification
    metadata.setDenormedClassificationMapped(true);
  }

  public NeoTaxon interpretTaxon(UnescapedVerbatimRecord v, boolean synonym, boolean skipName) {
    NeoTaxon t = new NeoTaxon();
    // verbatim
    t.verbatim = v;
    // name
    t.name = interpretName(v);
    // taxon
    t.taxon = new Taxon();
    t.taxon.setId(v.getId());
    t.taxon.setOrigin(Origin.SOURCE);
    t.taxon.setStatus(parse(TaxonomicStatusParser.PARSER, v.getTerm(AcefTerm.Sp2000NameStatus))
        .orElse(TaxonomicStatus.ACCEPTED));
    t.taxon.setAccordingTo(v.getTerm(AcefTerm.LTSSpecialist));
    t.taxon.setAccordingToDate(date(t, Issue.ACCORDING_TO_DATE_INVALID, AcefTerm.LTSDate));
    t.taxon.setOrigin(Origin.SOURCE);
    t.taxon.setDatasetUrl(uri(t, Issue.URL_INVALID, AcefTerm.InfraSpeciesURL, AcefTerm.SpeciesURL));
    t.taxon.setFossil(bool(t, Issue.IS_FOSSIL_INVALID, AcefTerm.IsFossil, AcefTerm.HasPreHolocene));
    t.taxon.setRecent(bool(t, Issue.IS_RECENT_INVALID, AcefTerm.IsRecent, AcefTerm.HasModern));
    t.taxon.setRemarks(v.getTerm(AcefTerm.AdditionalData));

    // lifezones
    String raw = t.verbatim.getTerm(AcefTerm.LifeZone);
    if (raw != null) {
      for (String lzv : MULTIVAL.split(raw)) {
        Lifezone lz = parse(LifezoneParser.PARSER, lzv).orNull(Issue.LIFEZONE_INVALID, t.issues);
        if (lz != null) {
          t.taxon.getLifezones().add(lz);
        }
      }
    }

    t.taxon.setSpeciesEstimate(null);
    t.taxon.setSpeciesEstimateReferenceKey(null);

    // synonym
    if (synonym) {
      t.synonym = new NeoTaxon.Synonym();
    }

    // acts
    // TODO: https://github.com/Sp2000/colplus-backend/issues/18
    t.acts = Lists.newArrayList();
    // flat classification
    t.classification = interpretClassification(v, synonym);

    return t;
  }

  protected LocalDate date(NeoTaxon t, Issue invalidIssue, Term term) {
    return parse(AcefDateParser.PARSER, t.verbatim.getTerm(term)).orNull(invalidIssue, t.issues);
  }

  void interpretVernaculars(NeoTaxon t) {
    for (TermRecord rec : t.verbatim.getExtensionRecords(AcefTerm.CommonNames)) {
      VernacularName vn = new VernacularName();
      vn.setName(rec.get(AcefTerm.CommonName));
      vn.setLanguage(SafeParser.parse(LanguageParser.PARSER, rec.get(AcefTerm.Language)).orNull());
      vn.setCountry(SafeParser.parse(CountryParser.PARSER, rec.get(AcefTerm.Country)).orNull());
      vn.setLatin(rec.get(AcefTerm.TransliteratedName));
      addReferences(vn, rec, t.issues);
      addAndTransliterate(t, vn);
    }
  }

  void interpretDistributions(NeoTaxon t) {
    for (TermRecord rec : t.verbatim.getExtensionRecords(AcefTerm.Distribution)) {
      // require location
      if (rec.hasTerm(AcefTerm.DistributionElement)) {
        Distribution d = new Distribution();

        // which standard?
        d.setGazetteer(parse(GazetteerParser.PARSER, rec.get(AcefTerm.StandardInUse))
            .orElse(Gazetteer.TEXT, Issue.DISTRIBUTION_GAZETEER_INVALID, t.issues));

        // TODO: try to split location into several distributions...
        String loc = rec.get(AcefTerm.DistributionElement);
        if (d.getGazetteer() == Gazetteer.TEXT) {
          d.setArea(loc);
        } else {
          // only parse area if other than text
          AreaParser.Area textArea = new AreaParser.Area(loc, Gazetteer.TEXT);
          if (loc.indexOf(':') < 0) {
            loc = d.getGazetteer().locationID(loc);
          }
          AreaParser.Area area = SafeParser.parse(AreaParser.PARSER, loc).orElse(textArea,
              Issue.DISTRIBUTION_AREA_INVALID, t.issues);
          d.setArea(area.area);
          // check if we have contradicting extracted a gazetteer
          if (area.standard != Gazetteer.TEXT && area.standard != d.getGazetteer()) {
            LOG.info(
                "Area standard {} found in area {} different from explicitly given standard {} for taxon {}",
                area.standard, area.area, d.getGazetteer(), t.getTaxonID());
          }
        }

        // status
        d.setStatus(parse(DistributionStatusParser.PARSER, rec.get(AcefTerm.DistributionStatus))
            .orElse(DistributionStatus.NATIVE, Issue.DISTRIBUTION_STATUS_INVALID, t.issues));
        addReferences(d, rec, t.issues);
        t.distributions.add(d);

      } else {
        t.addIssue(Issue.DISTRIBUTION_INVALID);
      }
    }
  }

  private void addReferences(Referenced obj, TermRecord v, Set<Issue> issueCollector) {
    if (v.hasTerm(AcefTerm.ReferenceID)) {
      Reference r = refStore.refById(v.get(AcefTerm.ReferenceID));
      if (r != null) {
        obj.addReferenceKey(r.getKey());
      } else {
        LOG.info("ReferenceID {} not existing but referred from {} for taxon {}",
            v.get(AcefTerm.ReferenceID), obj.getClass().getSimpleName(),
            v.get(AcefTerm.AcceptedTaxonID));
        issueCollector.add(Issue.REFERENCE_ID_INVALID);
      }
    }
  }

  private Classification interpretClassification(VerbatimRecord v, boolean isSynonym) {
    Classification cl = new Classification();
    cl.setKingdom(v.getTerm(AcefTerm.Kingdom));
    cl.setPhylum(v.getTerm(AcefTerm.Phylum));
    cl.setClass_(v.getTerm(AcefTerm.Class));
    cl.setOrder(v.getTerm(AcefTerm.Order));
    cl.setSuperfamily(v.getTerm(AcefTerm.Superfamily));
    cl.setFamily(v.getTerm(AcefTerm.Family));
    if (!isSynonym) {
      cl.setGenus(v.getTerm(AcefTerm.Genus));
      cl.setSubgenus(v.getTerm(AcefTerm.SubGenusName));
    }
    return cl;
  }

  private Name interpretName(VerbatimRecord v) {
    String authorship;
    String rank;
    if (v.hasTerm(AcefTerm.InfraSpeciesEpithet)) {
      rank = v.getTerm(AcefTerm.InfraSpeciesMarker);
      authorship = v.getTerm(AcefTerm.InfraSpeciesAuthorString);
    } else {
      rank = "species";
      authorship = v.getTerm(AcefTerm.AuthorString);
    }

    if (v.getTerms().getType() == AcefTerm.AcceptedInfraSpecificTaxa) {
      // preliminary name with just id and rank
      Name n = new Name();
      n.setId(v.getId());
      n.setRank(SafeParser.parse(RankParser.PARSER, rank).orElse(Rank.INFRASPECIFIC_NAME));
      return n;
    }
    return interpretName(v.getId(), rank, null, authorship, v.getTerm(AcefTerm.Genus),
        v.getTerm(AcefTerm.SubGenusName), v.getTerm(AcefTerm.SpeciesEpithet),
        v.getTerm(AcefTerm.InfraSpeciesEpithet), null, v.getTerm(AcefTerm.GSDNameStatus), null,
        null);
  }
}
