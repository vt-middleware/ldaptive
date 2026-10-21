/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema.normalize;

import java.util.function.Function;
import org.ldaptive.LdapUtils;

/**
 * Provides normalization for attributes that use the caseExactMatch and caseExactIA5Match matching rules.
 *
 * @author  Middleware Services
 */
public class CaseExactAttributeValueNormalizer extends AbstractAttributeValueNormalizer
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
    return LdapUtils.compressSpace(value, true);
  };


  /**
   * Creates a new case exact attribute value normalizer.
   */
  public CaseExactAttributeValueNormalizer()
  {
    super(DEFAULT_NORMALIZER);
  }
}
