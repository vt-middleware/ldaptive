/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to describe LDAP attribute data on a bean.
 *
 * @author  Middleware Services
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Attribute
{

  /**
   * Attribute name.
   *
   * @return  name of this attribute
   */
  String name() default "";

  /**
   * Attribute values. Mutually exclusive with {@link #property()}.
   *
   * @return  concrete values for this attribute
   */
  String[] values() default {};

  /**
   * Name of the method or field that maps to this attribute. Mutually exclusive with {@link #values()}.
   *
   * @return  property that contains attribute values
   */
  String property() default "";

  /**
   * Whether this attribute is binary.
   *
   * @return  whether this attribute is binary
   */
  boolean binary() default false;

  /**
   * Transcoder for this attribute.
   *
   * @return  transcoder class name
   */
  String transcoder() default "";
}
