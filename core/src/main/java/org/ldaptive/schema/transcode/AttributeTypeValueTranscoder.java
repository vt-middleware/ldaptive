/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema.transcode;

import org.ldaptive.schema.AttributeType;
import org.ldaptive.schema.SchemaParseException;

/**
 * Decodes and encodes an attribute type for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class AttributeTypeValueTranscoder extends AbstractSchemaElementValueTranscoder<AttributeType>
{


  @Override
  public AttributeType decodeStringValue(final String value)
  {
    try {
      return AttributeType.parse(value);
    } catch (SchemaParseException e) {
      throw new IllegalArgumentException("Could not transcode attribute type", e);
    }
  }


  @Override
  public Class<AttributeType> getType()
  {
    return AttributeType.class;
  }
}
