/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to describe an LDAP entry on a bean.
 *
 * @author  Middleware Services
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
