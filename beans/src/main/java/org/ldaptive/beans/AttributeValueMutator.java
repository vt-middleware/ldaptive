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
package org.ldaptive.beans;

import java.util.Collection;
import org.ldaptive.SortBehavior;

/**
 * Interface for mutating an attribute value on an arbitrary object.
 *
 * @author  Middleware Services
 * @version  $Revision: 2887 $ $Date: 2014-02-26 12:23:53 -0500 (Wed, 26 Feb 2014) $
 */
public interface AttributeValueMutator
{


  /**
   * Returns the name of the attribute.
   *
   * @return  attribute name
   */
  String getName();


  /**
   * Returns whether the attribute is binary.
   *
   * @return  whether the attribute is binary
   */
  boolean isBinary();


  /**
   * Returns the sort behavior of the attribute.
   *
   * @return  sort behavior of the attribute
   */
  SortBehavior getSortBehavior();


  /**
   * Returns the string values of the attribute.
   *
   * @param  object  containing attribute values
   *
   * @return  attribute values
   */
  Collection<String> getStringValues(Object object);


  /**
   * Returns the binary values of the attribute.
   *
   * @param  object  containing attribute values
   *
   * @return  attribute values
   */
  Collection<byte[]> getBinaryValues(Object object);


  /**
   * Sets the string values of the attribute.
   *
   * @param  object  to set values on
   * @param  values  to set
   */
  void setStringValues(Object object, Collection<String> values);


  /**
   * Sets the binary values of the attribute.
   *
   * @param  object  to set values on
   * @param  values  to set
   */
  void setBinaryValues(Object object, Collection<byte[]> values);
}
