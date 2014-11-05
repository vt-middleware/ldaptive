/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple bean for an ldap search filter and it's parameters.
 *
 * @author  Middleware Services
 */
public class SearchFilter
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 311;

  /** filter. */
  private String searchFilter;

  /** filter parameters. */
  private final Map<String, Object> parameters = new HashMap<>();


  /** Default constructor. */
  public SearchFilter() {}


  /**
   * Creates a new search filter with the supplied filter.
   *
   * @param  filter  to set
   */
  public SearchFilter(final String filter)
  {
    setFilter(filter);
  }


  /**
   * Creates a new search filter with the supplied filter and parameters.
   *
   * @param  filter  to set
   * @param  params  to set
   */
  public SearchFilter(final String filter, final Object[] params)
  {
    setFilter(filter);
    setParameters(params);
  }


  /**
   * Gets the filter.
   *
   * @return  filter
   */
  public String getFilter()
  {
    return searchFilter;
  }


  /**
   * Sets the filter.
   *
   * @param  filter  to set
   */
  public void setFilter(final String filter)
  {
    searchFilter = filter;
  }


  /**
   * Gets the filter parameters.
   *
   * @return  unmodifiable map of filter parameters
   */
  public Map<String, Object> getParameters()
  {
    return Collections.unmodifiableMap(parameters);
  }


  /**
   * Sets a positional filter parameter.
   *
   * @param  position  of the parameter in the filter
   * @param  value  to set
   */
  public void setParameter(final int position, final Object value)
  {
    parameters.put(Integer.toString(position), value);
  }


  /**
   * Sets a named filter parameter.
   *
   * @param  name  of the parameter in the filter
   * @param  value  to set
   */
  public void setParameter(final String name, final Object value)
  {
    parameters.put(name, value);
  }


  /**
   * Sets positional filter parameters.
   *
   * @param  values  to set
   */
  public void setParameters(final Object[] values)
  {
    int i = 0;
    for (Object o : values) {
      parameters.put(Integer.toString(i++), o);
    }
  }


  /**
   * Returns this filter with it's parameters encoded and replaced. See {@link
   * #encode(Object)}.
   *
   * @return  formatted and encoded filter
   */
  public String format()
  {
    String s = searchFilter;
    if (!parameters.isEmpty()) {
      for (Map.Entry<String, Object> e : parameters.entrySet()) {
        final String encoded = encode(e.getValue());
        if (encoded != null) {
          s = s.replace("{" + e.getKey() + "}", encoded);
        }
      }
    }
    return s;
  }


  /**
   * Hex encodes the supplied byte array for use in a search filter.
   *
   * @param  value  to encode
   *
   * @return  encoded value or null if supplied value is null
   */
  public static String encodeValue(final byte[] value)
  {
    if (value == null) {
      return null;
    }

    final char[] c = LdapUtils.hexEncode(value);
    final StringBuilder sb = new StringBuilder(c.length + c.length / 2);
    for (int i = 0; i < c.length; i += 2) {
      sb.append('\\').append(c[i]).append(c[i + 1]);
    }
    return sb.toString();
  }


  /**
   * Encodes the supplied attribute value for use in a search filter. See {@link
   * #escape(String)}.
   *
   * @param  value  to encode
   *
   * @return  encoded value or null if supplied value is null
   */
  public static String encodeValue(final String value)
  {
    if (value == null) {
      return null;
    }

    return escape(value);
  }


  /**
   * Hex encodes the supplied object if it is of type byte[], otherwise the
   * string format of the object is escaped. See {@link #escape(String)}.
   *
   * @param  obj  to encode
   *
   * @return  encoded object
   */
  protected static String encode(final Object obj)
  {
    if (obj == null) {
      return null;
    }

    String str;
    if (obj instanceof String) {
      str = encodeValue((String) obj);
    } else if (obj instanceof byte[]) {
      str = encodeValue((byte[]) obj);
    } else {
      str = encodeValue(obj.toString());
    }
    return str;
  }


  /**
   * Escapes the supplied string per RFC 2254.
   *
   * @param  s  to escape
   *
   * @return  escaped string
   */
  private static String escape(final String s)
  {
    final int len = s.length();
    final StringBuilder sb = new StringBuilder(len);
    char ch;
    for (int i = 0; i < len; i++) {
      ch = s.charAt(i);
      switch (ch) {

      case '*':
        sb.append("\\2a");
        break;

      case '(':
        sb.append("\\28");
        break;

      case ')':
        sb.append("\\29");
        break;

      case '\\':
        sb.append("\\5c");
        break;

      case 0:
        sb.append("\\00");
        break;

      default:
        sb.append(ch);
      }
    }
    return sb.toString();
  }


  /** {@inheritDoc} */
  @Override
  public boolean equals(final Object o)
  {
    return LdapUtils.areEqual(this, o);
  }


  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, searchFilter, parameters);
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::filter=%s, parameters=%s]",
        getClass().getName(),
        hashCode(),
        searchFilter,
        parameters);
  }
}
