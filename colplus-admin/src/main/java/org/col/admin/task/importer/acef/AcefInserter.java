package org.col.admin.task.importer.acef;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.col.admin.task.importer.NeoInserter;
import org.col.admin.task.importer.NormalizationFailedException;
import org.col.admin.task.importer.neo.NeoDb;
import org.col.admin.task.importer.neo.model.NeoTaxon;
import org.col.admin.task.importer.neo.model.UnescapedVerbatimRecord;
import org.col.admin.task.importer.reference.ReferenceFactory;
import org.col.api.model.Dataset;
import org.col.api.model.Reference;
import org.col.api.model.TermRecord;
import org.col.api.vocab.DataFormat;
import org.col.api.vocab.Issue;
import org.gbif.dwc.terms.AcefTerm;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Splitter;

import static com.google.common.base.Strings.emptyToNull;

/**
 *
 */
public class AcefInserter extends NeoInserter {

  private static final Logger LOG = LoggerFactory.getLogger(AcefInserter.class);
  private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

  private AcefReader reader;
  private AcefInterpreter inter;

  public AcefInserter(NeoDb store, Path folder, ReferenceFactory refFactory) {
    super(folder, store, refFactory);
  }

  private void initReader() {
    if (reader == null) {
      try {
        reader = AcefReader.from(folder);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Inserts ACEF data from a source folder into the normalizer store. Before inserting it does a
   * quick check to see if all required files are existing.
   */
  @Override
  public void batchInsert() throws NormalizationFailedException {
    try {
      initReader();
      inter = new AcefInterpreter(store.getDataset(), meta, store, refFactory);

      insertReferences();
      insertTaxaAndNames();
      insertReferenceLinks();

    } catch (RuntimeException e) {
      throw new NormalizationFailedException("Failed to read ACEF files", e);
    }
  }

  @Override
  public void insert() throws NormalizationFailedException {
    try (Transaction tx = store.getNeo().beginTx()) {
      reader.stream(AcefTerm.Distribution).forEach(this::addVerbatimRecord);
      reader.stream(AcefTerm.CommonNames).forEach(this::addVerbatimRecord);

    } catch (RuntimeException e) {
      throw new NormalizationFailedException("Failed to read ACEF files", e);
    }
  }

  @Override
  protected NeoDb.NodeBatchProcessor relationProcessor() {
    return new AcefRelationInserter(store, inter);
  }

  private void addVerbatimRecord(TermRecord rec) {
    super.addVerbatimRecord(AcefTerm.AcceptedTaxonID, rec);
  }

  private void insertTaxaAndNames() {
    // species
    reader.stream(AcefTerm.AcceptedSpecies).forEach(rec -> {
      UnescapedVerbatimRecord v = build(rec.get(AcefTerm.AcceptedTaxonID), rec);
      NeoTaxon t = inter.interpretTaxon(v, false);
      store.put(t);
      meta.incRecords(t.name.getRank());
    });
    // infraspecies
    reader.stream(AcefTerm.AcceptedInfraSpecificTaxa).forEach(rec -> {
      UnescapedVerbatimRecord v = build(rec.get(AcefTerm.AcceptedTaxonID), rec);
      // accepted infraspecific names in ACEF have no genus or species but a link to their parent
      // species ID.
      // so we cannot update the scientific name yet - we do this in the relation inserter instead!
      NeoTaxon t = inter.interpretTaxon(v, false);
      store.put(t);
      meta.incRecords(t.name.getRank());
    });
    // synonyms
    reader.stream(AcefTerm.Synonyms).forEach(rec -> {
      UnescapedVerbatimRecord v = build(rec.get(AcefTerm.ID), rec);
      NeoTaxon t = inter.interpretTaxon(v, true);
      store.put(t);
      meta.incRecords(t.name.getRank());
    });
  }

  /**
   * This inserts the plain references from the Reference file with no links to names, taxa or distributions.
   * Links are added afterwards in other methods when a ACEF:ReferenceID field is processed by lookup to the neo store.
   */
  private void insertReferences() {
    reader.stream(AcefTerm.Reference).forEach(rec -> {
      store.put(refFactory.fromACEF(
          emptyToNull(rec.get(AcefTerm.ReferenceID)),
          emptyToNull(rec.get(AcefTerm.Author)),
          emptyToNull(rec.get(AcefTerm.Title)),
          emptyToNull(rec.get(AcefTerm.Year)),
          emptyToNull(rec.get(AcefTerm.Source)),
          emptyToNull(rec.get(AcefTerm.ReferenceType)),
          emptyToNull(rec.get(AcefTerm.Details))
      ));
    });
  }

  /**
   * Inserts the NameReferecesLinks table from ACEF by looking up both the taxonID and the ReferenceID
   * ComNameRef references are linked from the individual common name already, we only process name and taxon references here
   *
   * A name should only have one reference - the publishedIn one.
   * A taxon can have multiple and are treated as the bibliography extension in dwc.
   */
  private void insertReferenceLinks() {
    reader.stream(AcefTerm.NameReferencesLinks).forEach(rec -> {
      String taxonID = emptyToNull(rec.get(AcefTerm.ID));
      String referenceID = emptyToNull(rec.get(AcefTerm.ReferenceID));
      String refType = emptyToNull(rec.get(AcefTerm.ReferenceType)); // NomRef, TaxAccRef, ComNameRef

      if (refType != null) {
        // lookup NeoTaxon
        NeoTaxon t = store.getByID(taxonID);
        if (t == null) {
          LOG.debug("taxonID {} from NameReferencesLinks line {} not existing", taxonID, rec.getLine());

        } else {
          Reference ref = store.refById(referenceID);
          if (ref == null) {
            LOG.debug("referenceID {} from NameReferencesLinks line {} not existing", referenceID, rec.getLine());
            t.addIssue(Issue.REFERENCE_ID_INVALID);
            store.update(t);

          } else {
            //TODO: better parsing needed? Use enum???
            if (refType.equalsIgnoreCase("NomRef")) {
              t.name.setPublishedInKey(ref.getKey());
              //TODO: what to do with page?
              // extract page from CSL, store in name and then remove from CSL?
              // Deduplicate refs afterwards if page is gone ???

            } else if (refType.equalsIgnoreCase("TaxAccRef")) {
              t.bibliography.add(ref);

            } else if (refType.equalsIgnoreCase("ComNameRef")) {
              // ignore here, we should see this again when parsing common names

            } else {
              // unkown type
              LOG.debug("Unknown reference type {} used in NameReferencesLinks line {}", refType, rec.getLine());
            }
          }
        }
      }
    });
  }


  /**
   * Reads the dataset metadata and puts it into the store
   */
  @Override
  protected Optional<Dataset> readMetadata() {
    Dataset d = null;
    initReader();
    Optional<TermRecord> metadata = reader.readFirstRow(AcefTerm.SourceDatabase);
    if (metadata.isPresent()) {
      TermRecord dr = metadata.get();
      d = new Dataset();
      d.setTitle(dr.get(AcefTerm.DatabaseFullName));
      d.setVersion(dr.get(AcefTerm.DatabaseVersion));
      d.setDescription(dr.get(AcefTerm.Abstract));
      d.setAuthorsAndEditors(dr.get(AcefTerm.AuthorsEditors, COMMA_SPLITTER));
      d.setDescription(dr.get(AcefTerm.Abstract));
      d.setHomepage(dr.getURI(AcefTerm.HomeURL));
      d.setDataFormat(DataFormat.ACEF);
    }
    return Optional.ofNullable(d);
  }

}
