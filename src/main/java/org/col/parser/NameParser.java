package org.col.parser;

import com.google.common.base.Splitter;
import org.col.api.Name;
import org.col.api.vocab.NamePart;
import org.col.api.vocab.NameType;
import org.col.api.vocab.Rank;
import org.col.api.vocab.VocabularyUtils;
import org.gbif.api.model.checklistbank.ParsedName;
import org.gbif.nameparser.GBIFNameParser;

import java.util.List;
import java.util.Optional;

/**
 * Wrapper around the GBIF Name parser to deal with col Name and API.
 */
public class NameParser {
  private static final GBIFNameParser PARSER = new GBIFNameParser();
  private static final Splitter AUTHORTEAM_SPLITTER = Splitter.on(",").trimResults();


  /**
   * Fully parses a name using #parse(String, Rank) but converts names that throw a UnparsableException
   * into ParsedName objects with the scientific name, rank and name type given.
   */
  public Name parse(String name, Optional<Rank> rank) {
    ParsedName pn = PARSER.parseQuietly(name, VocabularyUtils.convertToGbif(rank));
    Name n = new Name();
    n.setRank(rank.orElse(Rank.UNRANKED));
    n.setType(VocabularyUtils.convertEnum(NameType.class, pn.getType()));
    if (pn.isParsed()) {
      n.setScientificName(pn.canonicalName());

      n.setAuthorship(pn.authorshipComplete());
      n.setCombinationAuthors(splitAuthors(pn.getAuthorship()));
      n.setCombinationYear(pn.getYear());
      n.setOriginalAuthors(splitAuthors(pn.getBracketAuthorship()));
      n.setOriginalYear(pn.getBracketYear());

      if (pn.isBinomial()) {
        n.setGenus(pn.getGenusOrAbove());
        n.setSpecificEpithet(pn.getSpecificEpithet());
      }
      n.setInfragenericEpithet(pn.getInfraGeneric());
      n.setInfraspecificEpithet(pn.getInfraSpecificEpithet());
      n.setNotho(VocabularyUtils.convertEnum(NamePart.class, pn.getNotho()));

    } else {
      n.setScientificName(name);
    }
    return n;
  }

  private List<String> splitAuthors(String authors) {
    return AUTHORTEAM_SPLITTER.splitToList(authors);
  }

  /**
   * parses the name without authorship and returns the ParsedName.canonicalName() string
   *
   * @param rank the rank of the name if it is known externally. Helps identifying infrageneric names vs bracket authors
   */
  public String parseToCanonical(String scientificName, Optional<Rank> rank) {
    return PARSER.parseToCanonical(scientificName, VocabularyUtils.convertToGbif(rank));
  }

}
