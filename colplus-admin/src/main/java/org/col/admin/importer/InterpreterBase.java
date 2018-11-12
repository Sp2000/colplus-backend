package org.col.admin.importer;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.ibm.icu.text.Transliterator;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.col.admin.importer.reference.ReferenceFactory;
import org.col.api.exception.InvalidNameException;
import org.col.api.model.*;
import org.col.api.vocab.Issue;
import org.col.api.vocab.Origin;
import org.col.common.date.FuzzyDate;
import org.col.parser.*;
import org.gbif.dwc.terms.Term;
import org.gbif.nameparser.api.NameType;
import org.gbif.nameparser.api.ParsedName;
import org.gbif.nameparser.api.Rank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.col.parser.SafeParser.parse;

/**
 * Base interpreter providing common methods for both ACEF and DWC
 */
public class InterpreterBase {
  
  private static final Logger LOG = LoggerFactory.getLogger(InterpreterBase.class);
  protected static final Splitter MULTIVAL = Splitter.on(CharMatcher.anyOf(";|,")).trimResults();
  private static final Transliterator transLatin = Transliterator.getInstance("Any-Latin");
  private static final Transliterator transAscii = Transliterator.getInstance("Latin-ASCII");
  
  protected final Dataset dataset;
  protected final ReferenceFactory refFactory;
  
  public InterpreterBase(Dataset dataset, ReferenceFactory refFactory) {
    this.dataset = dataset;
    this.refFactory = refFactory;
  }
  
  protected String latinName(String name) {
    return transLatin.transform(name);
  }
  
  protected String asciiName(String name) {
    return transAscii.transform(latinName(name));
  }
  
  protected List<VernacularName> interpretVernacular(VerbatimRecord rec, BiConsumer<VernacularName, VerbatimRecord> addReferences, Term name, Term translit, Term lang, Term... countryTerms) {
    VernacularName vn = new VernacularName();
    vn.setVerbatimKey(rec.getKey());
    vn.setName(rec.get(name));
    if (StringUtils.isBlank(vn.getName())) {
      rec.addIssue(Issue.VERNACULAR_NAME_INVALID);
      return Collections.emptyList();
    }
    
    if (translit != null) {
      vn.setLatin(rec.get(translit));
    }
    if (lang != null) {
      vn.setLanguage(SafeParser.parse(LanguageParser.PARSER, rec.get(lang)).orNull());
    }
    vn.setCountry(SafeParser.parse(CountryParser.PARSER, rec.getFirst(countryTerms)).orNull());
    
    addReferences.accept(vn, rec);
    
    if (StringUtils.isBlank(vn.getLatin())) {
      vn.setLatin(latinName(vn.getName()));
      rec.addIssue(Issue.VERNACULAR_NAME_TRANSLITERATED);
    }
    return Lists.newArrayList(vn);
  }
  
  protected LocalDate date(VerbatimRecord v, VerbatimEntity ent, Issue invalidIssue, Term term) {
    Optional<FuzzyDate> date;
    try {
      date = DateParser.PARSER.parse(v.get(term));
    } catch (UnparsableException e) {
      v.addIssue(invalidIssue);
      return null;
    }
    if (date.isPresent()) {
      if (date.get().isFuzzyDate()) {
        v.addIssue(Issue.PARTIAL_DATE);
      }
      return date.get().toLocalDate();
    }
    return null;
  }
  
  protected URI uri(VerbatimRecord v, VerbatimEntity ent, Issue invalidIssue, Term... term) {
    return parse(UriParser.PARSER, v.getFirstRaw(term)).orNull(invalidIssue, v);
  }
  
  protected Boolean bool(VerbatimRecord v, VerbatimEntity ent, Issue invalidIssue, Term... term) {
    return parse(BooleanParser.PARSER, v.getFirst(term)).orNull(invalidIssue, v);
  }
  
  public Optional<NameAccordingTo> interpretName(final String id, final String vrank, final String sciname, final String authorship,
                                                 final String genus, final String infraGenus, final String species, final String infraspecies,
                                                 String nomCode, String nomStatus, String link, String remarks, VerbatimRecord v) {
    final boolean isAtomized = ObjectUtils.anyNotNull(genus, infraGenus, species, infraspecies);
    
    NameAccordingTo nat;
    
    Name atom = new Name();
    atom.setType(NameType.SCIENTIFIC);
    atom.setGenus(genus);
    atom.setInfragenericEpithet(infraGenus);
    atom.setSpecificEpithet(species);
    atom.setInfraspecificEpithet(infraspecies);
    
    // parse rank
    Rank rank = SafeParser.parse(RankParser.PARSER, vrank).orElse(Rank.UNRANKED, Issue.RANK_INVALID, v);
    atom.setRank(rank);
    
    // we can get the scientific name in various ways.
    // we parse all names from the scientificName + optional authorship
    // or use the atomized parts which we also use to validate the parsing result.
    if (sciname != null) {
      nat = NameParser.PARSER.parse(sciname, rank, v).get();
      
    } else if (!isAtomized) {
      LOG.info("No name given for {}", id);
      return Optional.empty();
      
    } else {
      // parse the reconstructed name without authorship
      // cant use the atomized name just like that cause we would miss name type detection (virus,
      // hybrid, placeholder, garbage)
      Optional<NameAccordingTo> natFromAtom = NameParser.PARSER.parse(atom.canonicalNameComplete(), rank, v);
      if (!natFromAtom.isPresent()) {
        LOG.warn("Failed to parse {} {} ({}) from given atoms. Use name atoms directly: {}/{}/{}/{}", rank, atom.canonicalNameComplete(), id, genus, infraGenus, species, infraspecies);
        v.addIssue(Issue.PARSED_NAME_DIFFERS);
        nat = new NameAccordingTo();
        nat.setName(atom);
      } else {
        nat = natFromAtom.get();
        // if parsed compare with original atoms
        if (nat.getName().isParsed()) {
          if (!Objects.equals(genus, nat.getName().getGenus()) ||
              !Objects.equals(infraGenus, nat.getName().getInfragenericEpithet()) ||
              !Objects.equals(species, nat.getName().getSpecificEpithet()) ||
              !Objects.equals(infraspecies, nat.getName().getInfraspecificEpithet())
              ) {
            LOG.warn("Parsed and given name atoms differ: [{}] vs [{}]", nat.getName().canonicalNameComplete(), atom.canonicalNameComplete());
            v.addIssue(Issue.PARSED_NAME_DIFFERS);
          }
        } else if (!Strings.isNullOrEmpty(authorship)) {
          // append authorship to unparsed scientificName
          String fullname = nat.getName().getScientificName().trim() + " " + authorship.trim();
          nat.getName().setScientificName(fullname);
        }
      }
    }
    
    // try to add an authorship if not yet there
    if (nat.getName().isParsed() && !Strings.isNullOrEmpty(authorship)) {
      ParsedName pnAuthorship = NameParser.PARSER.parseAuthorship(authorship).orElseGet(() -> {
        LOG.warn("Unparsable authorship {}", authorship);
        v.addIssue(Issue.UNPARSABLE_AUTHORSHIP);
        // add the full, unparsed authorship in this case to not lose it
        ParsedName pn = new ParsedName();
        pn.getCombinationAuthorship().getAuthors().add(authorship);
        return pn;
      });
      
      // we might have already parsed an authorship from the scientificName string which does not match up?
      if (nat.getName().hasAuthorship() &&
          !nat.getName().authorshipComplete().equalsIgnoreCase(pnAuthorship.authorshipComplete())) {
        v.addIssue(Issue.INCONSISTENT_AUTHORSHIP);
        LOG.info("Different authorship [{}] found in dwc:scientificName=[{}] and dwc:scientificNameAuthorship=[{}]",
            nat.getName().authorshipComplete(), sciname, pnAuthorship.authorshipComplete());
      }
      nat.getName().setCombinationAuthorship(pnAuthorship.getCombinationAuthorship());
      nat.getName().setSanctioningAuthor(pnAuthorship.getSanctioningAuthor());
      nat.getName().setBasionymAuthorship(pnAuthorship.getBasionymAuthorship());
    }
    
    // common basics
    nat.getName().setId(id);
    nat.getName().setVerbatimKey(v.getKey());
    nat.getName().setOrigin(Origin.SOURCE);
    nat.getName().setSourceUrl(SafeParser.parse(UriParser.PARSER, link).orNull());
    nat.getName().setNomStatus(SafeParser.parse(NomStatusParser.PARSER, nomStatus).orElse(null, Issue.NOMENCLATURAL_STATUS_INVALID, v));
    // applies default dataset code if we cannot find or parse any
    // Always make sure this happens BEFORE we update the canonical scientific name
    nat.getName().setCode(SafeParser.parse(NomCodeParser.PARSER, nomCode).orElse(dataset.getCode(), Issue.NOMENCLATURAL_CODE_INVALID, v));
    nat.getName().setRemarks(remarks);
    
    // assign best rank
    if (rank.notOtherOrUnranked() || nat.getName().getRank() == null) {
      // TODO: check ACEF ranks...
      nat.getName().setRank(rank);
    }
    
    // finally update the scientificName with the canonical form if we can
    try {
      nat.getName().updateScientificName();
    } catch (InvalidNameException e) {
      LOG.info("Invalid atomised name found: {}", nat.getName());
      v.addIssue(Issue.INCONSISTENT_NAME);
      if (isAtomized) {
        nat.getName().setScientificName(org.col.common.text.StringUtils.concat(genus, infraGenus, species, infraspecies));
        v.addIssue(Issue.DOUBTFUL_NAME);
      } else {
        return Optional.empty();
      }
    }
    
    return Optional.of(nat);
  }
  
  protected void setRefKey(Referenced obj, Reference r) {
    if (r != null) {
      obj.addReferenceId(r.getId());
    }
  }
  
}
