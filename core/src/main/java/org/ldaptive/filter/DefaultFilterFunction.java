/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ldaptive.ResultCode;

/**
 * Parses an LDAP search filter string.
 *
 * @author  Middleware Services
 */
public class DefaultFilterFunction extends AbstractFilterFunction
{

  /** Lower and upper case ASCII alphabetical, digits, semi-colon, dot, dash. */
  protected static final String DEFAULT_ATTRIBUTE_DESCRIPTION_CHARS =
    "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + ";.-";

  /** Allowed attribute description characters. */
  private final String attributeDescriptionChars;


  /** Default constructor. */
  public DefaultFilterFunction()
  {
    this(DEFAULT_ATTRIBUTE_DESCRIPTION_CHARS);
  }


  /**
   * Creates a new default filter function.
   *
   * @param  validChars  characters that are valid for an attribute description
   */
  public DefaultFilterFunction(final String validChars)
  {
    attributeDescriptionChars = validChars;
  }


  @Override
  protected Filter parseFilterComp(final String filter)
    throws FilterParseException
  {
    if (filter == null || filter.isEmpty()) {
      throw new FilterParseException(ResultCode.FILTER_ERROR, "Filter cannot be null or empty");
    }
    CharBuffer filterBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(filter.getBytes()));
    if (filterBuffer.get() != '(') {
      throw new FilterParseException(ResultCode.FILTER_ERROR, "Filter '" + filter + "' must start with '('");
    }
    if (filterBuffer.get(filterBuffer.limit() - 1) != ')') {
      throw new FilterParseException(ResultCode.FILTER_ERROR, "Filter '" + filter + "' must end with ')'");
    }
    if (!filterBuffer.hasRemaining()) {
      throw new FilterParseException(ResultCode.FILTER_ERROR, "Filter '" + filter + "' does not contain an expression");
    }
    final Filter searchFilter;
    filterBuffer = filterBuffer.limit(filterBuffer.limit() - 1).slice();
    if (!filterBuffer.hasRemaining()) {
      throw new FilterParseException(ResultCode.FILTER_ERROR, "Filter '" + filter + "' does not contain an expression");
    }
    if (filterBuffer.get() == ':') {
      // extensible filter with no attribute description
      searchFilter = parseExtensible(null, filterBuffer);
    } else {
      // read an attribute
      filterBuffer.position(filterBuffer.position() - 1);
      final CharBuffer attribute = readAttribute(filterBuffer);
      if (attribute.length() == 0) {
        throw new FilterParseException(
          ResultCode.FILTER_ERROR,
          "Invalid attribute description for filter '" + filter + "'");
      }
      switch (filterBuffer.get()) {
      case '=':
        if (!filterBuffer.hasRemaining()) {
          // empty equality
          searchFilter = new EqualityFilter(attribute.toString(), new byte[0]);
        } else {
          if (filterBuffer.get() == '*' && !filterBuffer.hasRemaining()) {
            // presence
            searchFilter = new PresenceFilter(attribute.toString());
          } else {
            // substring or equality
            searchFilter = parseSubstringOrEquality(
              attribute.toString(),
              filterBuffer.position(filterBuffer.position() - 1).slice());
          }
        }
        break;
      case ':':
        if (filterBuffer.get() != '=') {
          searchFilter = parseExtensible(
            attribute.toString(),
            filterBuffer.position(filterBuffer.position() - 1).slice());
        } else {
          try {
            searchFilter = new ExtensibleFilter(
              null,
              attribute.toString(),
              FilterUtils.parseAssertionValue(filterBuffer.slice().toString()));
          } catch (IllegalArgumentException e) {
            throw new FilterParseException(ResultCode.FILTER_ERROR, e);
          }
        }
        break;
      case '>':
        if (filterBuffer.get() != '=') {
          throw new FilterParseException(
            ResultCode.FILTER_ERROR,
            "Invalid greaterOrEqual expression for filter '" + filter + "'");
        }
        searchFilter = new GreaterOrEqualFilter(
          attribute.toString(),
          FilterUtils.parseAssertionValue(filterBuffer.slice().toString()));
        break;
      case '<':
        if (filterBuffer.get() != '=') {
          throw new FilterParseException(
            ResultCode.FILTER_ERROR,
            "Invalid lessOrEqual expression for filter '" + filter + "'");
        }
        searchFilter = new LessOrEqualFilter(
          attribute.toString(),
          FilterUtils.parseAssertionValue(filterBuffer.slice().toString()));
        break;
      case '~':
        if (filterBuffer.get() != '=') {
          throw new FilterParseException(
            ResultCode.FILTER_ERROR,
            "Invalid approximate expression for filter '" + filter + "'");
        }
        searchFilter = new ApproximateFilter(
          attribute.toString(),
          FilterUtils.parseAssertionValue(filterBuffer.slice().toString()));
        break;
      default:
        throw new FilterParseException(
          ResultCode.FILTER_ERROR,
          "Invalid filter expression for filter '" + filter + "'");
      }
    }
    return searchFilter;
  }


  /**
   * Returns a new buffer containing an attribute description. The supplied buffer will have it's position set to the
   * next position after the attribute.
   *
   * @param  cb  to read from
   *
   * @return  new char buffer
   *
   * @throws  FilterParseException  if the char buffer is empty
   */
  private CharBuffer readAttribute(final CharBuffer cb)
    throws FilterParseException
  {
    if (cb.length() == 0) {
      throw new FilterParseException(ResultCode.LOCAL_ERROR, "Attribute buffer size must be greater than zero");
    }
    final int limit = cb.limit();
    while (cb.hasRemaining()) {
      final char c = cb.get();
      if (attributeDescriptionChars.indexOf(c) == -1) {
        break;
      }
    }
    final int pos = cb.position() - 1;
    cb.position(pos);
    final CharBuffer slice = cb.flip().slice();
    cb.limit(limit).position(pos);
    return slice;
  }


  /**
   * Parses the supplied buffer and returns either a substring or equality filter.
   *
   * @param  attribute  attribute description
   * @param  cb  containing the assertion
   *
   * @return  either EqualityFilter or SubstringFilter
   *
   * @throws  FilterParseException  if neither substring or equality syntax can be parsed
   */
  private Filter parseSubstringOrEquality(final String attribute, final CharBuffer cb)
    throws FilterParseException
  {
    final Filter filter;
    final Map<String, List<CharBuffer>> substrings = readSubstrings(cb);
    if (substrings.size() == 0) {
      throw new FilterParseException(ResultCode.FILTER_ERROR, "Could not parse equality or substring assertion");
    }
    if (substrings.containsKey("EQUALITY")) {
      filter = new EqualityFilter(
        attribute,
        FilterUtils.parseAssertionValue(substrings.get("EQUALITY").get(0).toString()));
    } else {
      try {
        filter = new SubstringFilter(
          attribute,
          substrings.get("INITIAL") == null ? null :
            FilterUtils.parseAssertionValue(substrings.get("INITIAL").get(0).toString()),
          substrings.get("FINAL") == null ? null :
            FilterUtils.parseAssertionValue(substrings.get("FINAL").get(0).toString()),
          substrings.get("ANY") == null ? null :
            FilterUtils.parseAssertionValue(
              substrings.get("ANY").stream().map(CharBuffer::toString).toArray(String[]::new)));
      } catch (IllegalArgumentException e) {
        throw new FilterParseException(ResultCode.FILTER_ERROR, e);
      }
    }
    return filter;
  }


  /**
   * Reads the supplied buffer and builds a map of the substring data it contains. The following keys are made available
   * in the map:
   * <ul>
   *   <li>INITIAL: singleton list containing the initial substring or null</li>
   *   <li>ANY: list of any substring components or null</li>
   *   <li>FINAL: singleton list containing the final substring or null</li>
   *   <li>EQUALITY: singleton list containing the equality expression or null</li>
   * </ul>
   * If the return map contains 'EQUALITY', all other entries will be null and the buffer should be considered an
   * equality assertion.
   *
   * @param  cb  to read
   *
   * @return  map of character buffers
   */
  private Map<String, List<CharBuffer>> readSubstrings(final CharBuffer cb)
  {
    final Map<String, List<CharBuffer>> buffers = new HashMap<>();
    final int limit = cb.limit();
    cb.mark();
    while (cb.hasRemaining()) {
      final char c = cb.get();
      if (c == '*') {
        if (cb.position() == 1) {
          buffers.put("INITIAL", null);
          cb.mark();
        } else {
          if (cb.position() == cb.limit()) {
            buffers.put("FINAL", null);
          }
          final int pos = cb.position();
          if (buffers.containsKey("INITIAL")) {
            if (!buffers.containsKey("ANY")) {
              buffers.put("ANY", new ArrayList<>());
            }
            buffers.get("ANY").add(cb.limit(pos - 1).reset().slice());
          } else {
            buffers.put("INITIAL", Collections.singletonList(cb.limit(pos - 1).reset().slice()));
          }
          cb.limit(limit).position(pos);
          cb.mark();
        }
      }
    }
    cb.reset();
    if (cb.hasRemaining()) {
      if (buffers.size() > 0) {
        buffers.put("FINAL", Collections.singletonList(cb.slice()));
      } else {
        buffers.put("EQUALITY", Collections.singletonList(cb.slice()));
      }
    }
    if (!buffers.containsKey("INITIAL")) {
      buffers.put("INITIAL", null);
    }
    if (!buffers.containsKey("ANY")) {
      buffers.put("ANY", null);
    }
    if (!buffers.containsKey("FINAL")) {
      buffers.put("FINAL", null);
    }
    return buffers;
  }


  /**
   * Parses the supplied buffer and creates an extensible filter.
   *
   * @param  attribute  attribute description or null
   * @param  cb  to parse
   *
   * @return  extensible filter
   *
   * @throws  FilterParseException  if the buffer does not contain an extensible expression
   */
  private ExtensibleFilter parseExtensible(final String attribute, final CharBuffer cb)
    throws FilterParseException
  {
    boolean dnAttrs = false;
    CharBuffer remainingFilter = cb.slice();
    CharBuffer matchingRule = sliceAtMatch(remainingFilter, ':');
    if (matchingRule == null) {
      throw new FilterParseException(ResultCode.FILTER_ERROR, "Invalid extensible expression, no data after ':'");
    }
    if ("dn".equalsIgnoreCase(matchingRule.toString())) {
      dnAttrs = true;
      matchingRule = null;
      if (remainingFilter.hasRemaining()) {
        if (remainingFilter.get() != '=') {
          remainingFilter = remainingFilter.position(remainingFilter.position() - 1).slice();
          matchingRule = sliceAtMatch(remainingFilter, ':');
        } else {
          remainingFilter.position(remainingFilter.position() - 1);
        }
      }
    }
    if (remainingFilter.hasRemaining() && remainingFilter.get() != '=') {
      throw new FilterParseException(ResultCode.FILTER_ERROR, "Invalid extensible expression");
    }
    try {
      return new ExtensibleFilter(
        matchingRule == null ? null : matchingRule.toString(),
        attribute,
        FilterUtils.parseAssertionValue(remainingFilter.slice().toString()),
        dnAttrs);
    } catch (IllegalArgumentException e) {
      throw new FilterParseException(ResultCode.FILTER_ERROR, e);
    }
  }


  /**
   * Returns a new char buffer whose position is 0 and whose limit is before the match character. The supplied buffer
   * has it's position incremented one position past the match character.
   *
   * @param  cb  to search
   * @param  match  to search for
   *
   * @return  new char buffer or null if there is no match
   */
  private CharBuffer sliceAtMatch(final CharBuffer cb, final char match)
  {
    final int limit = cb.limit();
    while (cb.hasRemaining()) {
      final char c = cb.get();
      if (c == match) {
        final int pos = cb.position();
        cb.position(pos - 1);
        final CharBuffer slice = cb.flip().slice();
        cb.limit(limit).position(pos);
        return slice;
      }
    }
    return null;
  }
}
