/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema.io;

import java.text.ParseException;
import org.ldaptive.schema.DITContentRule;

/**
 * Decodes and encodes a DIT content rule for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class DITContentRuleValueTranscoder
  extends AbstractSchemaElementValueTranscoder<DITContentRule>
{


  @Override
  public DITContentRule decodeStringValue(final String value)
  {
    try {
      return DITContentRule.parse(value);
    } catch (ParseException e) {
      throw new IllegalArgumentException(
        "Could not transcode DIT content rule",
        e);
    }
  }


  @Override
  public Class<DITContentRule> getType()
  {
    return DITContentRule.class;
  }
}
