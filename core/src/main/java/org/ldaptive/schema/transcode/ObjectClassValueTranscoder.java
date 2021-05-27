/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema.transcode;

import org.ldaptive.schema.ObjectClass;
import org.ldaptive.schema.SchemaParseException;

/**
 * Decodes and encodes an object class for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class ObjectClassValueTranscoder extends AbstractSchemaElementValueTranscoder<ObjectClass>
{


  @Override
  public ObjectClass decodeStringValue(final String value)
  {
    try {
      return ObjectClass.parse(value);
    } catch (SchemaParseException e) {
      throw new IllegalArgumentException("Could not transcode object class", e);
    }
  }


  @Override
  public Class<ObjectClass> getType()
  {
    return ObjectClass.class;
  }
}
