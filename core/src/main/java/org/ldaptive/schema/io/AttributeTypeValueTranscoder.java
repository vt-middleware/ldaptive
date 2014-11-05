/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema.io;

import java.text.ParseException;
import org.ldaptive.schema.AttributeType;

/**
 * Decodes and encodes an attribute type for use in an ldap attribute value.
 *
 * @author  Middleware Services
 * @version  $Revision: 2940 $ $Date: 2014-03-31 11:10:46 -0400 (Mon, 31 Mar 2014) $
 */
public class AttributeTypeValueTranscoder
  extends AbstractSchemaElementValueTranscoder<AttributeType>
{


  /** {@inheritDoc} */
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


  /** {@inheritDoc} */
  @Override
  public Class<AttributeType> getType()
  {
    return AttributeType.class;
  }
}
