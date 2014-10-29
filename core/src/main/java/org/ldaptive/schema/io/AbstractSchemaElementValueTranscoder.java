/*
  $Id: AbstractSchemaElementValueTranscoder.java 2994 2014-06-03 19:00:45Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2994 $
  Updated: $Date: 2014-06-03 15:00:45 -0400 (Tue, 03 Jun 2014) $
*/
package org.ldaptive.schema.io;

import org.ldaptive.io.AbstractStringValueTranscoder;
import org.ldaptive.schema.SchemaElement;

/**
 * Base class for schema element value transcoders.
 *
 * @param  <T>  type of schema element
 *
 * @author  Middleware Services
 * @version  $Revision: 2994 $ $Date: 2014-06-03 15:00:45 -0400 (Tue, 03 Jun 2014) $
 */
public abstract class
AbstractSchemaElementValueTranscoder<T extends SchemaElement>
  extends AbstractStringValueTranscoder<T>
{


  /** {@inheritDoc} */
  @Override
  public String encodeStringValue(final T value)
  {
    return value.format();
  }
}
