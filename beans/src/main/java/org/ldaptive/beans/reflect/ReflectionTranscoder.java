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
package org.ldaptive.beans.reflect;

import java.util.Collection;

/**
 * Interface for reflection based transcoders.
 *
 * @author  Middleware Services
 * @version  $Revision: 2887 $ $Date: 2014-02-26 12:23:53 -0500 (Wed, 26 Feb 2014) $
 */
public interface ReflectionTranscoder
{


  /**
   * Decodes the supplied values into an object.
   *
   * @param  values  to decode
   *
   * @return  object initialized with the values
   */
  Object decodeStringValues(Collection<String> values);


  /**
   * Decodes the supplied values into an object.
   *
   * @param  values  to decode
   *
   * @return  object initialized with the values
   */
  Object decodeBinaryValues(Collection<byte[]> values);


  /**
   * Encodes the supplied value into one or more strings for use in an
   * attribute.
   *
   * @param  value  containing data to encode as strings
   *
   * @return  string attribute values
   */
  Collection<String> encodeStringValues(Object value);


  /**
   * Encodes the supplied value into one or more byte arrays for use in an
   * attribute.
   *
   * @param  value  containing data to encode as byte arrays
   *
   * @return  binary attribute values
   */
  Collection<byte[]> encodeBinaryValues(Object value);


  /**
   * Returns the type produced by this transcoder.
   *
   * @return  type produced by this transcoder
   */
  Class<?> getType();


  /**
   * Returns whether this transcoder can convert object of the supplied type.
   *
   * @param  type  of object
   *
   * @return  whether the supplied type can be converted
   */
  boolean supports(Class<?> type);
}
