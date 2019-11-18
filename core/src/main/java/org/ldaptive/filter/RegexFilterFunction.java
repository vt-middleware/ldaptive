/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses an LDAP search filter string using regular expressions.
 *
 * @author  Middleware Services
 */
public class RegexFilterFunction extends AbstractFilterFunction
{

  /** Regular expression that matches an attribute description. */
  private static final String ATTRIBUTE_DESC = "[\\p{Alnum};\\-\\.]+";

  /** Regular expression that matches an assertion value. */
  private static final String ASSERTION_VALUE = "([^\\)]*+)";

  /** Regular expression that matches characters that should have been escaped. */
  private static final Pattern ESCAPE_CHARS_PATTERN = Pattern.compile("[\0\\(\\)]+");

  /** Regex pattern to match a presence filter. */
  private static final Pattern PRESENCE_FILTER_PATTERN = Pattern.compile("\\((" + ATTRIBUTE_DESC + ")=\\*\\)");

  /** Regex pattern to match an equality filter. */
  private static final Pattern EQUALITY_FILTER_PATTERN = Pattern.compile("\\((" + ATTRIBUTE_DESC + ")=([^\\*]*)\\)");

  /** Regex pattern to match a substring filter. */
  private static final Pattern SUBSTRING_FILTER_PATTERN = Pattern.compile(
    "\\((" + ATTRIBUTE_DESC + ")=((?:[^\\*]*\\*[^\\*]*)+)\\)");

  /** Regex pattern to match an extensible filter. */
  private static final Pattern EXTENSIBLE_FILTER_PATTERN = Pattern.compile(
    "\\((" + ATTRIBUTE_DESC + ")?(:[Dd][Nn])?(?::(.+))?:=(" + ASSERTION_VALUE + ")\\)");

  /** Regex pattern to match a greater or equal filter. */
  private static final Pattern GREATER_OR_EQUAL_FILTER_PATTERN = Pattern.compile(
    "\\((" + ATTRIBUTE_DESC + ")>=(" + ASSERTION_VALUE + ")\\)");

  /** Regex pattern to match a less or equal filter. */
  private static final Pattern LESS_OR_EQUAL_FILTER_PATTERN = Pattern.compile(
    "\\((" + ATTRIBUTE_DESC + ")<=(" + ASSERTION_VALUE + ")\\)");

  /** Regex pattern to match an approximate filter. */
  private static final Pattern APPROXIMATE_FILTER_PATTERN = Pattern.compile(
    "\\((" + ATTRIBUTE_DESC + ")~=(" + ASSERTION_VALUE + ")\\)");


  @Override
  protected Filter parseFilterComp(final String filter)
  {
    // note that presence *must* be checked before substring
    Filter searchFilter = parsePresenceFilter(filter);
    if (searchFilter == null) {
      searchFilter = parseEqualityFilter(filter);
    }
    if (searchFilter == null) {
      searchFilter = parseSubstringFilter(filter);
    }
    if (searchFilter == null) {
      searchFilter = parseExtensibleFilter(filter);
    }
    if (searchFilter == null) {
      searchFilter = parseGreaterOrEqualFilter(filter);
    }
    if (searchFilter == null) {
      searchFilter = parseLessOrEqualFilter(filter);
    }
    if (searchFilter == null) {
      searchFilter = parseApproximateFilter(filter);
    }
    return searchFilter;
  }


  /**
   * Creates a new presence filter by parsing the supplied filter string.
   *
   * @param  component  to parse
   *
   * @return  presence filter or null if component doesn't match this filter type
   */
  static PresenceFilter parsePresenceFilter(final String component)
  {
    final Matcher m = PRESENCE_FILTER_PATTERN.matcher(component);
    if (m.matches()) {
      return new PresenceFilter(m.group(1));
    }
    return null;
  }


  /**
   * Creates a new equality filter by parsing the supplied filter string.
   *
   * @param  component  to parse
   *
   * @return  equality filter or null if component doesn't match this filter type
   */
  static EqualityFilter parseEqualityFilter(final String component)
  {
    final Matcher m = EQUALITY_FILTER_PATTERN.matcher(component);
    if (m.matches()) {
      final String attr = m.group(1);
      final String value = m.group(2);
      throwOnEscapeChars(value);
      return new EqualityFilter(attr, FilterUtils.parseAssertionValue(value));
    }
    return null;
  }


  /**
   * Creates a new substring filter by parsing the supplied filter string.
   *
   * @param  component  to parse
   *
   * @return  substring filter or null if component doesn't match this filter type
   */
  static SubstringFilter parseSubstringFilter(final String component)
  {
    final Matcher m = SUBSTRING_FILTER_PATTERN.matcher(component);
    if (m.matches()) {
      // don't allow presence match or multiple asterisks
      if (!m.group(2).equals("*") && !m.group(2).contains("**")) {
        final String attr = m.group(1);
        final String assertions = m.group(2);

        String startsWith = null;
        final int firstAsterisk = assertions.indexOf('*');
        if (firstAsterisk > 0) {
          startsWith = assertions.substring(0, firstAsterisk);
          throwOnEscapeChars(startsWith);
        }
        String endsWith = null;
        final int lastAsterisk = assertions.lastIndexOf('*');
        if (lastAsterisk < assertions.length() - 1) {
          endsWith = assertions.substring(lastAsterisk + 1);
          throwOnEscapeChars(endsWith);
        }
        String[] contains = null;
        if (lastAsterisk > firstAsterisk) {
          contains = assertions.substring(firstAsterisk + 1, lastAsterisk).split("\\*");
          throwOnEscapeChars(contains);
        }
        return new SubstringFilter(
          attr,
          startsWith != null ? FilterUtils.parseAssertionValue(startsWith) : null,
          endsWith != null ? FilterUtils.parseAssertionValue(endsWith) : null,
          contains != null ? FilterUtils.parseAssertionValue(contains) : null);
      }
    }
    return null;
  }


  /**
   * Creates a new extensible filter by parsing the supplied filter string.
   *
   * @param  component  to parse
   *
   * @return  extensible filter or null if component doesn't match this filter type
   */
  static ExtensibleFilter parseExtensibleFilter(final String component)
  {
    final Matcher m = EXTENSIBLE_FILTER_PATTERN.matcher(component);
    if (m.matches()) {
      // CheckStyle:MagicNumber OFF
      final String rule = m.group(3);
      final String attr = m.group(1);
      final String value = m.group(4);
      final boolean dn = m.group(2) != null;
      return new ExtensibleFilter(rule, attr, FilterUtils.parseAssertionValue(value), dn);
      // CheckStyle:MagicNumber ON
    }
    return null;
  }


  /**
   * Creates a new greater or equal filter by parsing the supplied filter string.
   *
   * @param  component  to parse
   *
   * @return  greater or equal filter or null if component doesn't match this filter type
   */
  static GreaterOrEqualFilter parseGreaterOrEqualFilter(final String component)
  {
    final Matcher m = GREATER_OR_EQUAL_FILTER_PATTERN.matcher(component);
    if (m.matches()) {
      final String attr = m.group(1);
      final String value = m.group(2);
      return new GreaterOrEqualFilter(attr, FilterUtils.parseAssertionValue(value));
    }
    return null;
  }


  /**
   * Creates a new less or equal filter by parsing the supplied filter string.
   *
   * @param  component  to parse
   *
   * @return  less or equal filter or null if component doesn't match this filter type
   */
  static LessOrEqualFilter parseLessOrEqualFilter(final String component)
  {
    final Matcher m = LESS_OR_EQUAL_FILTER_PATTERN.matcher(component);
    if (m.matches()) {
      final String attr = m.group(1);
      final String value = m.group(2);
      return new LessOrEqualFilter(attr, FilterUtils.parseAssertionValue(value));
    }
    return null;
  }


  /**
   * Creates a new approximate filter by parsing the supplied filter string.
   *
   * @param  component  to parse
   *
   * @return  approximate filter or null if component doesn't match this filter type
   */
  static ApproximateFilter parseApproximateFilter(final String component)
  {
    final Matcher m = APPROXIMATE_FILTER_PATTERN.matcher(component);
    if (m.matches()) {
      final String attr = m.group(1);
      final String value = m.group(2);
      return new ApproximateFilter(attr, FilterUtils.parseAssertionValue(value));
    }
    return null;
  }


  /**
   * Throws an exception if the supplied value matches {@link #ESCAPE_CHARS_PATTERN}.
   *
   * @param  values  to check
   *
   * @throws  IllegalArgumentException  if a value contains characters that should be escaped
   */
  private static void throwOnEscapeChars(final String... values)
  {
    for (String s : values) {
      final Matcher m = ESCAPE_CHARS_PATTERN.matcher(s);
      if  (m.find()) {
        throw new IllegalArgumentException("Invalid filter syntax, contains unescaped characters");
      }
    }
  }
}
