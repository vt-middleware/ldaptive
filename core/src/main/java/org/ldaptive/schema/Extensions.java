/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ldaptive.LdapUtils;

/**
 * Bean for an extension found in a schema element.
 *
 * @author  Middleware Services
 */
public class Extensions
{

  /** Pattern to match attribute type definitions. */
  protected static final Pattern DEFINITION_PATTERN = Pattern.compile(
    "(?:(X-[^ ]+)[ ]*(?:'([^']+)'|\\(([^\\)]+)\\))?)+");

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1171;

  /** Extensions. */
  private final Map<String, List<String>> extensions =
    new LinkedHashMap<>();


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
   * Returns a single string value for the extension with the supplied name. See
   * {@link #getValues(String)}.
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
    extensions.put(name, new ArrayList<String>(0));
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
   * Parses the supplied definition string and creates an initialized extension.
   *
   * @param  definition  to parse
   *
   * @return  extension
   */
  public static Extensions parse(final String definition)
  {
    final Matcher m = DEFINITION_PATTERN.matcher(definition);
    final Extensions exts = new Extensions();
    while (m.find()) {
      final String name = m.group(1).trim();
      final List<String> values = new ArrayList<>(1);

      // CheckStyle:MagicNumber OFF
      if (m.group(2) != null) {
        values.add(m.group(2).trim());
      } else if (m.group(3) != null) {
        values.addAll(
          Arrays.asList(SchemaUtils.parseDescriptors(m.group(3).trim())));
      }
      // CheckStyle:MagicNumber ON
      exts.addExtension(name, values);
    }

    return exts;
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
        sb.append(
          SchemaUtils.formatDescriptors(
            entry.getValue().toArray(new String[entry.getValue().size()])));
      }
    }
    return sb.toString();
  }


  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, extensions);
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::extensions=%s]",
        getClass().getName(),
        hashCode(),
        extensions);
  }
}
