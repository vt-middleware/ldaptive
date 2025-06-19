/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema.transcode;

import org.ldaptive.schema.SchemaElement;
import org.ldaptive.transcode.AbstractStringValueTranscoder;

/**
 * Base class for schema element value transcoders.
 *
 * @param  <T>  type of schema element
 *
 * @author  Middleware Services
 */
public abstract class AbstractSchemaElementValueTranscoder<T extends SchemaElement<?>>
  extends AbstractStringValueTranscoder<T>
{


  @Override
  public String encodeStringValue(final T value)
  {
    return value.format();
  }
}
