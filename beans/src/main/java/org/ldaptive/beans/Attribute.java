/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.ldaptive.SortBehavior;

/**
 * Annotation to describe LDAP attribute data on a bean.
 *
 * @author  Middleware Services
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Attribute
{

  /** Attribute name. */
  String name() default "";

  /** Attribute values. Mutually exclusive with {@link #property()}. */
  String[] values() default {};

  /** Name of the method or field that maps to this attribute. Mutually
   * exclusive with {@link #values()}. */
  String property() default "";

  /** Whether this attribute is binary. */
  boolean binary() default false;

  /** Sort behavior for this attribute. */
  SortBehavior sortBehavior() default SortBehavior.UNORDERED;

  /** Transcoder for this attribute. */
  String transcoder() default "";
}
