/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for regex definition functions.
 *
 * @param  <T>  type of schema element
 *
 * @author  Middleware Services
 */
public abstract class AbstractRegexDefinitionFunction<T extends SchemaElement<?>> implements DefinitionFunction<T>
{

  /** Regex to match zero or more spaces. */
  protected static final String WSP_REGEX = "[ ]*";

  /** Regex to match one or more spaces. */
  protected static final String ONE_WSP_REGEX = "[ ]+";

  /** Regex to match one or more non spaces. */
  protected static final String NO_WSP_REGEX = "[^ ]+";

  /** Pattern to match extensions. */
  private static final Pattern EXTENSIONS_PATTERN = Pattern.compile(
    "(?:(X-[^ ]+)[ ]*(?:'([^']+)'|\\(([^\\)]+)\\))?)+");


  /**
   * Parses extensions from the supplied definition.
   *
   * @param  definition  that was parsed
   *
   * @return  extensions
   */
  protected Extensions parseExtensions(final String definition)
  {
    final Matcher m = EXTENSIONS_PATTERN.matcher(definition);
    final Extensions exts = new Extensions();
    while (m.find()) {
      final String name = m.group(1).trim();
      final List<String> values = new ArrayList<>(1);

      // CheckStyle:MagicNumber OFF
      if (m.group(2) != null) {
        values.add(m.group(2).trim());
      } else if (m.group(3) != null) {
        values.addAll(Arrays.asList(SchemaUtils.parseDescriptors(m.group(3).trim())));
      }
      // CheckStyle:MagicNumber ON
      exts.addExtension(name, values);
    }

    return exts;
  }
}
