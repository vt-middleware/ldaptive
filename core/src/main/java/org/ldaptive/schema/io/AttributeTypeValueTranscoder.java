/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema.io;

import java.text.ParseException;
import org.ldaptive.schema.AttributeType;

/**
 * Decodes and encodes an attribute type for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class AttributeTypeValueTranscoder
  extends AbstractSchemaElementValueTranscoder<AttributeType>
{


  @Override
  public AttributeType decodeStringValue(final String value)
  {
    try {
      return AttributeType.parse(value);
    } catch (ParseException e) {
      throw new IllegalArgumentException(
        "Could not transcode attribute type",
        e);
    }
  }


  @Override
  public Class<AttributeType> getType()
  {
    return AttributeType.class;
  }
}
