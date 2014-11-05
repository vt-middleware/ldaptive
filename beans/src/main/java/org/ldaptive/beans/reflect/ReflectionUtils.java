/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Provides utility methods for common reflection operations.
 *
 * @author  Middleware Services
 */
public final class ReflectionUtils
{


  /** Default constructor. */
  private ReflectionUtils() {}


  /**
   * Casts the supplied type to a class.
   *
   * @param  t  to cast
   *
   * @return  class cast from t
   *
   * @throws  IllegalArgumentException  if t is not an instance of Class
   */
  public static Class<?> classFromType(final Type t)
  {
    if (!(t instanceof Class)) {
      throw new IllegalArgumentException("Unsupported type: " + t);
    }
    return (Class<?>) t;
  }


  /**
   * Returns the value of the supplied field on the supplied object.
   *
   * @param  field  containing the value to return
   * @param  object  that has the field
   *
   * @return  value of the field on the object
   *
   * @throws  IllegalArgumentException  if the field cannot be retrieved
   */
  public static Object getField(final Field field, final Object object)
  {
    try {
      return field.get(object);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException(e);
    }
  }


  /**
   * Sets the supplied value of the supplied field on the supplied object.
   *
   * @param  field  of the object to set
   * @param  object  that has the field
   * @param  value  to set
   *
   * @throws  IllegalArgumentException  if the field cannot be set
   */
  public static void setField(
    final Field field,
    final Object object,
    final Object value)
  {
    try {
      field.set(object, value);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException(e);
    }
  }


  /**
   * Invokes the supplied method on the supplied object.
   *
   * @param  method  to invoke
   * @param  object  that has the method
   *
   * @return  value of the invoked method
   *
   * @throws  IllegalArgumentException  if the method cannot be invoked
   */
  public static Object invokeGetterMethod(
    final Method method,
    final Object object)
  {
    try {
      return method.invoke(object);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new IllegalArgumentException(e);
    }
  }


  /**
   * Invokes the supplied method on the supplied object with the supplied value
   * as a parameter.
   *
   * @param  method  to invoke
   * @param  object  that has the method
   * @param  value  to set
   *
   * @throws  IllegalArgumentException  if the method cannot be invoked
   */
  public static void invokeSetterMethod(
    final Method method,
    final Object object,
    final Object value)
  {
    try {
      method.invoke(object, value);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
