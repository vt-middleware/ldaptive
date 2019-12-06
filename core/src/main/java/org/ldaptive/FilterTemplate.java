/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.ldaptive.filter.FilterUtils;

/**
 * Class for producing an LDAP search filter from a filter template. Templates can use either index based parameters or
 * name based parameters for substitutions. Parameters are encoded according to RFC 4515.
 *
 * @author  Middleware Services
 */
public class FilterTemplate
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 311;

  /** filter. */
  private String searchFilter;

  /** filter parameters. */
  private final Map<String, Object> parameters = new HashMap<>();


  /** Default constructor. */
  public FilterTemplate() {}


  /**
   * Creates a new search filter with the supplied filter.
   *
   * @param  filter  to set
   */
  public FilterTemplate(final String filter)
  {
    setFilter(filter);
  }


  /**
   * Creates a new search filter with the supplied filter and parameters.
   *
   * @param  filter  to set
   * @param  params  to set
   */
  public FilterTemplate(final String filter, final Object[] params)
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
   * Returns this filter with it's parameters encoded and replaced. See {@link #encode(Object)}.
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
   * Encodes the supplied attribute value for use in a search filter. See {@link FilterUtils#escape(String)}.
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

    return FilterUtils.escape(value);
  }


  /**
   * Hex encodes the supplied object if it is of type byte[], otherwise the string format of the object is escaped. See
   * {@link FilterUtils#escape(String)}.
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

    final String str;
    if (obj instanceof String) {
      str = encodeValue((String) obj);
    } else if (obj instanceof byte[]) {
      str = encodeValue((byte[]) obj);
    } else {
      str = encodeValue(obj.toString());
    }
    return str;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof FilterTemplate) {
      final FilterTemplate v = (FilterTemplate) o;
      return LdapUtils.areEqual(searchFilter, v.searchFilter) &&
        LdapUtils.areEqual(parameters, v.parameters);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, searchFilter, parameters);
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("filter=").append(searchFilter).append(", ")
      .append("parameters=").append(parameters).append("]").toString();
  }


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static Builder builder()
  {
    return new Builder();
  }


  // CheckStyle:OFF
  public static class Builder
  {


    private final FilterTemplate object = new FilterTemplate();


    protected Builder() {}


    public Builder filter(final String filter)
    {
      object.setFilter(filter);
      return this;
    }


    public Builder parameter(final String name, final String value)
    {
      object.setParameter(name, value);
      return this;
    }


    public Builder parameter(final String name, final Object value)
    {
      object.setParameter(name, value);
      return this;
    }


    public Builder parameter(final int pos, final String value)
    {
      object.setParameter(pos, value);
      return this;
    }


    public Builder parameter(final int pos, final Object value)
    {
      object.setParameter(pos, value);
      return this;
    }


    public Builder parameters(final Object... values)
    {
      object.setParameters(values);
      return this;
    }


    public FilterTemplate build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
