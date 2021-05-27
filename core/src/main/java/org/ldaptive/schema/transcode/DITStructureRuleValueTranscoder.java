/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema.transcode;

import org.ldaptive.schema.DITStructureRule;
import org.ldaptive.schema.SchemaParseException;

/**
 * Decodes and encodes a DIT structure rule for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class DITStructureRuleValueTranscoder extends AbstractSchemaElementValueTranscoder<DITStructureRule>
{


  @Override
  public DITStructureRule decodeStringValue(final String value)
  {
    try {
      return DITStructureRule.parse(value);
    } catch (SchemaParseException e) {
      throw new IllegalArgumentException("Could not transcode DIT structure rule", e);
    }
  }


  @Override
  public Class<DITStructureRule> getType()
  {
    return DITStructureRule.class;
  }
}
