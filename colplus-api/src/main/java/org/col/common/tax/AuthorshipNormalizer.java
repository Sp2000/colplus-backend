package org.col.common.tax;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.col.api.model.Name;
import org.col.common.io.Resources;
import org.gbif.nameparser.api.Authorship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.col.common.text.StringUtils.foldToAscii;

/**
 * Utility to compare scientific name authorships, i.e. the recombination and basionym author and publishing year.
 * Author strings are normalized to ASCII and then compared. As authors are often abbreviated in all kind of ways a shared common substring is accepted
 * as a positive equality.
 * If any of the names given has an empty author & year the results will always be Equality.UNKNOWN.
 * <p>
 * The class exposes two kind of compare methods. A strict one always requiring both year and author to match
 * and a more lax default comparison that only looks at years when the authors differ (as it is quite hard to compare authors)
 */
public class AuthorshipNormalizer {
  private static final Logger LOG = LoggerFactory.getLogger(AuthorshipNormalizer.class);
  
  private static final Pattern FIL = Pattern.compile("([A-Z][a-z]*)[\\. ]\\s*f(:?il)?\\.?\\b");
  private static final Pattern TRANSLITERATIONS = Pattern.compile("([auo])e", Pattern.CASE_INSENSITIVE);
  private static final Pattern AUTHOR = Pattern.compile("^((?:[a-z]\\s)*).*?([a-z]+)( filius)?$");
  private static final String AUTHOR_MAP_FILENAME = "authorship/authormap.txt";
  private static final Pattern PUNCTUATION = Pattern.compile("[\\p{Punct}&&[^,]]+");
  private final Map<String, String> authorMap;
  
  
  public static AuthorshipNormalizer createWithoutAuthormap() {
    return new AuthorshipNormalizer(Maps.<String, String>newHashMap());
  }
  
  public static AuthorshipNormalizer createWithAuthormap() {
    Map<String, String> map = new HashMap<>();
    Resources.tabRows(AUTHOR_MAP_FILENAME).forEach(row -> {
      map.put(row[0], row[2]);
      map.put(row[1], row[2]);
    });
    return new AuthorshipNormalizer(map);
  }
  
  
  public AuthorshipNormalizer(Map<String, String> authors) {
    Map<String, String> map = Maps.newHashMap();
    for (Map.Entry<String, String> entry : authors.entrySet()) {
      String key = normalize(entry.getKey());
      String val = normalize(entry.getValue());
      if (key != null && val != null) {
        map.put(key, val);
      }
    }
    authorMap = ImmutableMap.copyOf(map);
    LOG.info("Created author normalizer with {} abbreviation entries", map.size());
  }
  
  
  /**
   * @return queue of normalized authors, never null.
   * ascii only, lower cased string without punctuation. Empty string instead of null.
   * Umlaut transliterations reduced to single letter
   */
  public static List<String> normalize(Authorship authorship) {
    return authorship == null ? Collections.EMPTY_LIST : normalize(authorship.getAuthors());
  }
  
  private static List<String> normalize(List<String> authors) {
    if (authors == null || authors.isEmpty()) {
      return Collections.EMPTY_LIST;
    }
    List<String> normed = new ArrayList<>(authors.size());
    for (String x : authors) {
      x = normalize(x);
      // ignore et al authors
      if (x != null && !x.equals("al")) {
        normed.add(x);
      }
    }
    return normed;
  }
  
  /**
   * Shortcut doing author normalization for all combination, basionym authors and their ex-authors, doing a lookup of known authors
   * and finally a alphabetical sorting of unique names only, merging ex and regular authors into a single list of unique names.
   *
   * For names without a parsed authorship normalize the full authorship string but keep its order, not trying to parse it again.
   */
  public List<String> normalizeAllAndLookup(Name n) {
    if (n.hasAuthorship()) {
      Stream<String> comb = combinedAuthorStream(n.getCombinationAuthorship());
      Stream<String> bas  = combinedAuthorStream(n.getBasionymAuthorship());
      return Stream.concat(comb, bas)
          .distinct()
          .sorted()
          .collect(Collectors.toList());

    } else if (n.getAuthorship() != null){
      return Lists.newArrayList(normalize(n.getAuthorship()));
    }
    
    return Collections.EMPTY_LIST;
  }
  
  private Stream<String> combinedAuthorStream(Authorship a) {
    if (a != null) {
      Stream<String> sau = lookup(normalize(a.getAuthors())).stream();
      Stream<String> sex = lookup(normalize(a.getExAuthors())).stream();
      return Stream.concat(sau, sex);
    }
    return Stream.empty();
  }
  
  public static String normalize(String x) {
    if (StringUtils.isBlank(x)) {
      return null;
    }
    // normalize filius
    x = FIL.matcher(x).replaceAll("$1 filius");
    // simplify umlauts transliterated properly with additional e
    x = TRANSLITERATIONS.matcher(x).replaceAll("$1");
    // fold to ascii
    x = foldToAscii(x);
    // replace all punctuation but commas
    x = PUNCTUATION.matcher(x).replaceAll(" ");
    // norm space
    x = StringUtils.normalizeSpace(x);
    
    if (StringUtils.isBlank(x)) {
      return null;
    }
    return x.toLowerCase();
  }
  
  /**
   * Looks up individual authors from an authorship string
   *
   * @return entire authorship string with expanded authors if found
   */
  public String lookup(String normalizedAuthor) {
    if (normalizedAuthor != null && authorMap.containsKey(normalizedAuthor)) {
      return authorMap.get(normalizedAuthor);
    } else {
      return normalizedAuthor;
    }
  }
  
  public List<String> lookup(List<String> authorTeam) {
    List<String> authors = Lists.newArrayList();
    for (String author : authorTeam) {
      authors.add(lookup(author));
    }
    return authors;
  }
  
  public List<String> lookup(List<String> normalizedAuthorTeam, int minAuthorLengthWithoutLookup) {
    List<String> authors = Lists.newArrayList();
    for (String author : normalizedAuthorTeam) {
      if (minAuthorLengthWithoutLookup > 0 && author.length() < minAuthorLengthWithoutLookup) {
        authors.add(lookup(author));
      } else {
        authors.add(author);
      }
    }
    return authors;
  }
  
  
  public static class Author {
    public final String fullname;
    public final String initials;
    public final String surname;
    public final String suffix;
  
    public Author(String a) {
      fullname = a;
      Matcher m = AUTHOR.matcher(a);
      if (m.find()) {
        initials = trim(m.group(1));
        surname = trim(m.group(2));
        suffix = trim(m.group(3));
      } else {
        LOG.warn("Cannot parse author: {}", a);
        initials = "";
        surname = trim(a);
        suffix = "";
      }
    }
    
    private String trim(String x) {
      return x == null ? null : StringUtils.trimToNull(x);
    }
  
    public boolean hasInitials() {
      return initials != null && !initials.isEmpty();
    }
    
    /**
     * Gracefully compare initials of the first author only
     *
     * @return true if they differ
     */
    public boolean firstInitialsDiffer(Author other) {
      if (hasInitials() && other.hasInitials()) {
        if (initials.equals(other.initials)) {
          return false;
          
        } else {
          // if one set of chars is a subset of the other we consider this a match
          List<Character> smaller = Lists.charactersOf(StringUtils.deleteWhitespace(initials));
          List<Character> larger = Lists.charactersOf(StringUtils.deleteWhitespace(other.initials));
          if (smaller.size() > larger.size()) {
            // swap, the Sets difference method needs the right inputs
            List<Character> tmp = smaller;
            smaller = larger;
            larger = tmp;
          }
          // remove all of the chars from the larger queue and see if any remain
          if (org.apache.commons.collections4.CollectionUtils.isSubCollection(smaller, larger)) {
            // one is a subset of the other
            return false;
          }
        }
        // they seem to differ
        return true;
        
      } else {
        // no initials in at least one of them
        return false;
      }
    }
    
  }
  
}