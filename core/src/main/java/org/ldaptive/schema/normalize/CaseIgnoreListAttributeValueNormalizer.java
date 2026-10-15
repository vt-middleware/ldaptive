/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema.normalize;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.ldaptive.LdapUtils;

/**
 * Provides normalization for attributes that use the caseIgnoreListMatch matching rule.
 *
 * @author  Middleware Services
 */
public class CaseIgnoreListAttributeValueNormalizer extends AbstractAttributeValueNormalizer
{

  /** Default normalizer implementation. */
  private static final Function<String, String> DEFAULT_NORMALIZER = value -> {
    LdapUtils.assertNotNullArg(value, "Value cannot be null");
    if (value.isEmpty()) {
      return "";
    }
    if (value.chars().allMatch(c -> c == ' ')) {
      return " ";
    }
    final List<String> list = new ArrayList<>();
    final int len = value.length();
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < len; i++) {
      final char c = value.charAt(i);
      if (c == '$') {
        list.add(sb.toString());
        sb.setLength(0);
      } else {
        sb.append(c);
      }
    }
    if (sb.length() > 0) {
      list.add(sb.toString());
    }
    sb.setLength(0);
    list.forEach(section -> {
      if (section.isEmpty()) {
        sb.append('$');
      } else if (section.chars().allMatch(c -> c == ' ')) {
        sb.append(" $");
      } else {
        sb.append(LdapUtils.toLowerCase(LdapUtils.compressSpace(section, true))).append('$');
      }
    });
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  };


  /**
   * Creates a new case ignore list attribute value normalizer.
   */
  public CaseIgnoreListAttributeValueNormalizer()
  {
    super(DEFAULT_NORMALIZER);
  }
}
