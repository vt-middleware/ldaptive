/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive.schema.io;

import java.text.ParseException;
import org.ldaptive.schema.ObjectClass;

/**
 * Decodes and encodes an object class for use in an ldap attribute value.
 *
 * @author  Middleware Services
 * @version  $Revision: 2940 $ $Date: 2014-03-31 11:10:46 -0400 (Mon, 31 Mar 2014) $
 */
public class ObjectClassValueTranscoder
  extends AbstractSchemaElementValueTranscoder<ObjectClass>
{


  /** {@inheritDoc} */
  @Override
  public ObjectClass decodeStringValue(final String value)
  {
    try {
      return ObjectClass.parse(value);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Could not transcode object class", e);
    }
  }


  /** {@inheritDoc} */
  @Override
  public Class<ObjectClass> getType()
  {
    return ObjectClass.class;
  }
}
