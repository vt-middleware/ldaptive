/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema.io;

import java.text.ParseException;
import org.ldaptive.schema.MatchingRuleUse;

/**
 * Decodes and encodes a matching rule use for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class MatchingRuleUseValueTranscoder
  extends AbstractSchemaElementValueTranscoder<MatchingRuleUse>
{


  /** {@inheritDoc} */
  @Override
  public MatchingRuleUse decodeStringValue(final String value)
  {
    try {
      return MatchingRuleUse.parse(value);
    } catch (ParseException e) {
      throw new IllegalArgumentException(
        "Could not transcode matching rule use",
        e);
    }
  }


  /** {@inheritDoc} */
  @Override
  public Class<MatchingRuleUse> getType()
  {
    return MatchingRuleUse.class;
  }
}
