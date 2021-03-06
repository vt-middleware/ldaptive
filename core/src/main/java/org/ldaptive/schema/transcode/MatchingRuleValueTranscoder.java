/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema.transcode;

import org.ldaptive.schema.MatchingRule;
import org.ldaptive.schema.SchemaParseException;

/**
 * Decodes and encodes a matching rule for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class MatchingRuleValueTranscoder extends AbstractSchemaElementValueTranscoder<MatchingRule>
{


  @Override
  public MatchingRule decodeStringValue(final String value)
  {
    try {
      return MatchingRule.parse(value);
    } catch (SchemaParseException e) {
      throw new IllegalArgumentException("Could not transcode matching rule", e);
    }
  }


  @Override
  public Class<MatchingRule> getType()
  {
    return MatchingRule.class;
  }
}
