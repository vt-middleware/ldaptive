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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to describe an LDAP entry on a bean.
 *
 * @author  Middleware Services
 * @version  $Revision: 2888 $ $Date: 2014-03-07 10:15:59 -0500 (Fri, 07 Mar 2014) $
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entry
{

  /** Entry DN. */
  String dn();

  /** Entry attributes. */
  Attribute[] attributes() default {};
}
