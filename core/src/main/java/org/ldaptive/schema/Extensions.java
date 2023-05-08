/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ldaptive.LdapUtils;

/**
 * Bean for an extension found in a schema element.
 *
 * @author  Middleware Services
 */
public class Extensions
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1171;

  /** Extensions. */
  private final Map<String, List<String>> extensions = new LinkedHashMap<>();


  /** Creates a new extensions. */
  public Extensions() {}


  /**
   * Creates a new extensions.
   *
   * @param  name  of a single extension
   * @param  values  for that extension
   */
  public Extensions(final String name, final List<String> values)
  {
    addExtension(name, values);
  }


  /**
   * Returns the name.
   *
   * @return  name
   */
  public Set<String> getNames()
  {
    return extensions.keySet();
  }


  /**
   * Returns the values for the extension with the supplied name.
   *
   * @param  name  of the extension
   *
   * @return  values
   */
  public List<String> getValues(final String name)
  {
    return extensions.get(name);
  }


  /**
   * Returns a single string value for the extension with the supplied name. See {@link #getValues(String)}.
   *
   * @param  name  of the extension
   *
   * @return  single string extension value
   */
  public String getValue(final String name)
  {
    final List<String> values = getValues(name);
    if (values == null || values.isEmpty()) {
      return null;
    }
    return values.iterator().next();
  }


  /**
   * Returns all the values in this extensions.
   *
   * @return  map of name to values for every extension
   */
  public Map<String, List<String>> getAllValues()
  {
    return Collections.unmodifiableMap(extensions);
  }


  /**
   * Adds an extension.
   *
   * @param  name  of the extension
   */
  public void addExtension(final String name)
  {
    extensions.put(name, new ArrayList<>(0));
  }


  /**
   * Adds an extension.
   *
   * @param  name  of the extension
   * @param  values  in the extension
   */
  public void addExtension(final String name, final List<String> values)
  {
    extensions.put(name, values);
  }


  /**
   * Returns the number of extensions in the underlying map.
   *
   * @return  number of extensions
   */
  public int size()
  {
    return extensions.size();
  }


  /**
   * Returns whether the number of extensions is zero.
   *
   * @return  whether the number of extensions is zero
   */
  public boolean isEmpty()
  {
    return extensions.isEmpty();
  }


  /**
   * Returns this extension as formatted string per RFC 4512.
   *
   * @return  formatted string
   */
  public String format()
  {
    final StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, List<String>> entry : extensions.entrySet()) {
      sb.append(entry.getKey()).append(" ");
      if (entry.getValue() != null && !entry.getValue().isEmpty()) {
        sb.append(SchemaUtils.formatDescriptors(entry.getValue().toArray(new String[0])));
      }
    }
    return sb.toString();
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof Extensions) {
      final Extensions v = (Extensions) o;
      return LdapUtils.areEqual(extensions, v.extensions);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, extensions);
  }


  @Override
  public String toString()
  {
    return "[" + getClass().getName() + "@" + hashCode() + "::" + "extensions=" + extensions + "]";
  }
}
