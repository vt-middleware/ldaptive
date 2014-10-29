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

/**
 * Describes the attribute value mutators and DN value mutators for a specific
 * type.
 *
 * @author  Middleware Services
 * @version  $Revision: 2887 $ $Date: 2014-02-26 12:23:53 -0500 (Wed, 26 Feb 2014) $
 */
public interface ClassDescriptor
{


  /**
   * Prepare this class descriptor for use.
   *
   * @param  type  of object to describe
   */
  void initialize(Class<?> type);


  /**
   * Returns the DN value mutator for this type.
   *
   * @return  dn value mutator
   */
  DnValueMutator getDnValueMutator();


  /**
   * Returns the attribute value mutators for this type.
   *
   * @return  value mutators
   */
  Collection<AttributeValueMutator> getAttributeValueMutators();


  /**
   * Returns the attribute value mutator for the attribute with the supplied
   * name.
   *
   * @param  name  of the attribute
   *
   * @return  value mutator
   */
  AttributeValueMutator getAttributeValueMutator(String name);
}
