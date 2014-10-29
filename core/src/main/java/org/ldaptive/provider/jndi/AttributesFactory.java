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
package org.ldaptive.provider.jndi;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

/**
 * Provides convenience methods for creating jndi attributes and attribute
 * objects.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public final class AttributesFactory
{

  /** Default ignore case value, value of this constant is {@value}. */
  public static final boolean DEFAULT_IGNORE_CASE = true;


  /** Default constructor. */
  private AttributesFactory() {}


  /**
   * Creates a new attributes. Attributes will be case-insensitive.
   *
   * @param  name  of the attribute
   *
   * @return  attributes
   */
  public static Attributes createAttributes(final String name)
  {
    return createAttributes(name, DEFAULT_IGNORE_CASE);
  }


  /**
   * Creates a new attributes.
   *
   * @param  name  of the attribute
   * @param  ignoreCase  whether to ignore the case of attribute values
   *
   * @return  attributes
   */
  public static Attributes createAttributes(
    final String name,
    final boolean ignoreCase)
  {
    return createAttributes(name, null, ignoreCase);
  }


  /**
   * Creates a new attributes. Attributes will be case-insensitive.
   *
   * @param  name  of the attribute
   * @param  value  of the attribute
   *
   * @return  attributes
   */
  public static Attributes createAttributes(
    final String name,
    final Object value)
  {
    return createAttributes(name, value, DEFAULT_IGNORE_CASE);
  }


  /**
   * Creates a new attributes.
   *
   * @param  name  of the attribute
   * @param  value  of the attribute
   * @param  ignoreCase  whether to ignore the case of attribute values
   *
   * @return  attributes
   */
  public static Attributes createAttributes(
    final String name,
    final Object value,
    final boolean ignoreCase)
  {
    if (value == null) {
      return createAttributes(name, null, ignoreCase);
    } else {
      return createAttributes(name, new Object[] {value}, ignoreCase);
    }
  }


  /**
   * Creates a new attributes. Attributes will be case-insensitive.
   *
   * @param  name  of the attribute
   * @param  values  of the attribute
   *
   * @return  attributes
   */
  public static Attributes createAttributes(
    final String name,
    final Object[] values)
  {
    return createAttributes(name, values, DEFAULT_IGNORE_CASE);
  }


  /**
   * Creates a new attributes.
   *
   * @param  name  of the attribute
   * @param  values  of the attribute
   * @param  ignoreCase  whether to ignore the case of attribute values
   *
   * @return  attributes
   */
  public static Attributes createAttributes(
    final String name,
    final Object[] values,
    final boolean ignoreCase)
  {
    final Attributes attrs = new BasicAttributes(ignoreCase);
    attrs.put(createAttribute(name, values));
    return attrs;
  }


  /**
   * Creates a new attribute.
   *
   * @param  name  of the attribute
   *
   * @return  attribute
   */
  public static Attribute createAttribute(final String name)
  {
    return createAttribute(name, null);
  }


  /**
   * Creates a new attribute.
   *
   * @param  name  of the attribute
   * @param  value  of the attribute
   *
   * @return  attribute
   */
  public static Attribute createAttribute(final String name, final Object value)
  {
    if (value == null) {
      return createAttribute(name, null);
    } else {
      return createAttribute(name, new Object[] {value});
    }
  }


  /**
   * Creates a new attribute.
   *
   * @param  name  of the attribute
   * @param  values  of the attribute
   *
   * @return  attribute
   */
  public static Attribute createAttribute(
    final String name,
    final Object[] values)
  {
    final Attribute attr = new BasicAttribute(name);
    if (values != null) {
      for (Object o : values) {
        attr.add(o);
      }
    }
    return attr;
  }
}
