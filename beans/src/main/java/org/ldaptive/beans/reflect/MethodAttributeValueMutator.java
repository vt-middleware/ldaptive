/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.reflect;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Attribute mutator associated with the {@link Method} of an object.
 *
 * @author  Middleware Services
 */
public class MethodAttributeValueMutator extends AbstractAttributeValueMutator
{

  /** Method to get data from. */
  private final Method getterMethod;

  /** Method to set data on. */
  private final Method setterMethod;


  /**
   * Creates a new method attribute value mutator.
   *
   * @param  transcoder  for mutating the methods
   * @param  getter  method to read data
   * @param  setter  method to write data
   */
  public MethodAttributeValueMutator(final ReflectionTranscoder transcoder, final Method getter, final Method setter)
  {
    super(null, false, transcoder);
    getterMethod = getter;
    if (getterMethod != null) {
      getterMethod.setAccessible(true);
    }
    setterMethod = setter;
    if (setterMethod != null) {
      setterMethod.setAccessible(true);
    }
  }


  /**
   * Creates a new method attribute value mutator.
   *
   * @param  name  of the attribute
   * @param  binary  whether the attribute is binary
   * @param  transcoder  to mutate the methods
   * @param  getter  method to read data
   * @param  setter  method to write data
   */
  public MethodAttributeValueMutator(
    final String name,
    final boolean binary,
    final ReflectionTranscoder transcoder,
    final Method getter,
    final Method setter)
  {
    super(name, binary, transcoder);
    getterMethod = getter;
    if (getterMethod != null) {
      getterMethod.setAccessible(true);
    }
    setterMethod = setter;
    if (setterMethod != null) {
      setterMethod.setAccessible(true);
    }
  }


  @Override
  public Collection<String> getStringValues(final Object object)
  {
    if (getterMethod == null) {
      return null;
    }
    return getReflectionTranscoder().encodeStringValues(ReflectionUtils.invokeGetterMethod(getterMethod, object));
  }


  @Override
  public Collection<byte[]> getBinaryValues(final Object object)
  {
    if (getterMethod == null) {
      return null;
    }
    return getReflectionTranscoder().encodeBinaryValues(ReflectionUtils.invokeGetterMethod(getterMethod, object));
  }


  @Override
  public void setStringValues(final Object object, final Collection<String> values)
  {
    if (setterMethod != null) {
      ReflectionUtils.invokeSetterMethod(setterMethod, object, getReflectionTranscoder().decodeStringValues(values));
    }
  }


  @Override
  public void setBinaryValues(final Object object, final Collection<byte[]> values)
  {
    if (setterMethod != null) {
      ReflectionUtils.invokeSetterMethod(setterMethod, object, getReflectionTranscoder().decodeBinaryValues(values));
    }
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "name=" + getName() + ", " +
      "binary=" + isBinary() + ", " +
      "reflectionTranscoder=" + getReflectionTranscoder() + ", " +
      "getterMethod=" + getterMethod + ", " +
      "setterMethod=" + setterMethod + "]";
  }
}
