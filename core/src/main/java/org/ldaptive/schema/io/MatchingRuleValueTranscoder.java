/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema.io;

import java.text.ParseException;
import org.ldaptive.schema.MatchingRule;

/**
 * Decodes and encodes a matching rule for use in an ldap attribute value.
 *
 * @author  Middleware Services
 * @version  $Revision: 2940 $ $Date: 2014-03-31 11:10:46 -0400 (Mon, 31 Mar 2014) $
 */
public class MatchingRuleValueTranscoder
  extends AbstractSchemaElementValueTranscoder<MatchingRule>
{


  /** {@inheritDoc} */
  @Override
  public MatchingRule decodeStringValue(final String value)
  {
    try {
      return MatchingRule.parse(value);
    } catch (ParseException e) {
      throw new IllegalArgumentException(
        "Could not transcode matching rule",
        e);
    }
  }


  /** {@inheritDoc} */
  @Override
  public Class<MatchingRule> getType()
  {
    return MatchingRule.class;
  }
}
