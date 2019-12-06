package life.catalogue.parser;

import java.util.List;

import com.google.common.collect.Lists;
import life.catalogue.api.vocab.Country;
import life.catalogue.parser.CountryParser;
import org.junit.Test;

/**
 *
 */
public class CountryParserTest extends ParserTestBase<Country> {

  public CountryParserTest() {
    super(CountryParser.PARSER);
  }

  @Test
  public void parse() throws Exception {
    assertParse(Country.GERMANY, "deu");
    assertParse(Country.GERMANY, "deutschland");
    assertParse(Country.GERMANY, "GER");
    assertParse(Country.GERMANY, "276");
    assertParse(Country.KOSOVO, "Kosovo");

    // user defined codes
    assertParse(Country.INTERNATIONAL_WATERS, "xz");

    // other user defined codes
    assertUnparsable("Oceania");
    assertUnparsable("QO");
    assertUnparsable("AA");
    assertUnparsable("XAZ");
    assertUnparsable("unknown");
  }

  @Override
  List<String> additionalUnparsableValues() {
    return Lists.newArrayList("term", "deuter", "unknown");
  }
}