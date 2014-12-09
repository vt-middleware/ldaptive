/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema.io;

import org.ldaptive.io.AbstractStringValueTranscoder;
import org.ldaptive.schema.SchemaElement;

/**
 * Base class for schema element value transcoders.
 *
 * @param  <T>  type of schema element
 *
 * @author  Middleware Services
 */
public abstract class
AbstractSchemaElementValueTranscoder<T extends SchemaElement>
  extends AbstractStringValueTranscoder<T>
{


  @Override
  public String encodeStringValue(final T value)
  {
    return value.format();
  }
}
