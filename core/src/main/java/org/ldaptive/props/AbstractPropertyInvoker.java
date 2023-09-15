/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides methods common to property invokers.
 *
 * @author  Middleware Services
 */
public abstract class AbstractPropertyInvoker implements PropertyInvoker
{

  /** Cache of properties. */
  private static final Map<String, Map<String, Method[]>> PROPERTIES_CACHE = new HashMap<>();

  /** Class to invoke methods on. */
  private Class<?> clazz;

  /** Map of all properties to their getter and setter methods. */
  private Map<String, Method[]> properties;


  /**
   * Initializes the properties cache with the supplied class. The cache contains a map of properties to an array of the
   * setter and getter methods. If a method named 'initialize' is found, it is also cached.
   *
   * @param  c  to read methods from
   */
  protected synchronized void initialize(final Class<?> c)
  {
    final String cacheKey = c.getName();
    if (PROPERTIES_CACHE.containsKey(cacheKey)) {
      properties = PROPERTIES_CACHE.get(cacheKey);
    } else {
      properties = new HashMap<>();
      // look for get, is, and initialize
      for (Method method : c.getMethods()) {
        if (!method.isBridge()) {
          if (method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
            final String mName = method.getName().substring(3);
            final String pName = Character.toLowerCase(mName.charAt(0)) + mName.substring(1);
            if (properties.containsKey(pName)) {
              final Method[] m = properties.get(pName);
              m[0] = method;
            } else {
              properties.put(pName, new Method[] {method, null});
            }
          } else if (method.getName().startsWith("is") && method.getParameterTypes().length == 0) {
            final String mName = method.getName().substring(2);
            final String pName = Character.toLowerCase(mName.charAt(0)) + mName.substring(1);
            if (properties.containsKey(pName)) {
              final Method[] m = properties.get(pName);
              // prefer the first method we find
              if (m[0] == null) {
                m[0] = method;
              }
            } else {
              properties.put(pName, new Method[] {method, null});
            }
          } else if ("initialize".equals(method.getName()) && method.getParameterTypes().length == 0) {
            final String pName = method.getName();
            properties.put(pName, new Method[] {method, method});
          }
        }
      }
      // look for setters
      for (Method method : c.getMethods()) {
        if (!method.isBridge()) {
          if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
            final String mName = method.getName().substring(3);
            final String pName = Character.toLowerCase(mName.charAt(0)) + mName.substring(1);
            if (properties.containsKey(pName)) {
              final Method[] m = properties.get(pName);
              if (m[0] != null && method.getParameterTypes()[0].equals(m[0].getReturnType())) {
                m[1] = method;
              }
            }
          }
        }
      }

      // remove any properties that don't have both getters and setters
      properties.values().removeIf(method -> method[0] == null || method[1] == null);

      PROPERTIES_CACHE.put(cacheKey, Collections.unmodifiableMap(properties));
    }
    clazz = c;
  }


  /**
   * This invokes the setter method for the supplied property name with the supplied value.
   *
   * @param  object  to invoke method on
   * @param  name  of the property
   * @param  value  of the property
   *
   * @throws  IllegalArgumentException  if an invocation exception occurs
   */
  @Override
  public void setProperty(final Object object, final String name, final String value)
  {
    if (!clazz.isInstance(object)) {
      throw new IllegalArgumentException(
        "Illegal attempt to set property for class " + clazz.getName() + " on object of type " +
        object.getClass().getName());
    }

    final Method getter = properties.get(name) != null ? properties.get(name)[0] : null;
    if (getter == null) {
      throw new IllegalArgumentException("No getter method found for " + name + " on object " + clazz.getName());
    }

    final Method setter = properties.get(name) != null ? properties.get(name)[1] : null;
    if (setter == null) {
      throw new IllegalArgumentException("No setter method found for " + name + " on object " + clazz.getName());
    }

    invokeMethod(setter, object, convertValue(getter.getReturnType(), value));
  }


  /**
   * Converts the supplied string value into an Object of the supplied type. If value cannot be converted it is returned
   * as is.
   *
   * @param  type  of object to convert value into
   * @param  value  to parse
   *
   * @return  object of the supplied type
   */
  protected abstract Object convertValue(Class<?> type, String value);


  /**
   * Returns whether the supplied property exists for this invoker.
   *
   * @param  name  to check
   *
   * @return  whether the supplied property exists
   */
  @Override
  public boolean hasProperty(final String name)
  {
    return properties.containsKey(name);
  }


  /**
   * Returns the property keys for this invoker.
   *
   * @return  set of property names
   */
  @Override
  public Set<String> getProperties()
  {
    return Collections.unmodifiableSet(properties.keySet());
  }


  /**
   * Creates an instance of the supplied type.
   *
   * @param  <T>  type of class returned
   * @param  type  of class to create
   * @param  className  to create
   *
   * @return  class of type T
   *
   * @throws  IllegalArgumentException  if the supplied class name cannot create a new instance of T
   */
  public static <T> T instantiateType(final T type, final String className)
  {
    try {
      try {
        final Class<?> clazz = createClass(className);
        final Constructor<?> con = clazz.getDeclaredConstructor((Class[]) null);
        @SuppressWarnings("unchecked")
        final T t = (T) con.newInstance();
        return t;
      } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
        throw new IllegalArgumentException(e);
      }
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("Error instantiating type " + type + " using  " + className, e);
    }
  }


  /**
   * Creates the class with the supplied name.
   *
   * @param  className  to create
   *
   * @return  class
   *
   * @throws  IllegalArgumentException  if the supplied class name cannot be created
   */
  public static Class<?> createClass(final String className)
  {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(String.format("Could not find class '%s'", className), e);
    }
  }


  /**
   * Converts simple types that are common to all property invokers. If value cannot be converted it is returned as is.
   *
   * @param  type  of object to convert value into
   * @param  value  to parse
   *
   * @return  object of the supplied type
   */
  protected Object convertSimpleType(final Class<?> type, final String value)
  {
    Object newValue = value;
    if (Class.class.isAssignableFrom(type)) {
      newValue = createTypeFromPropertyValue(Class.class, value);
    } else if (Class[].class.isAssignableFrom(type)) {
      newValue = createArrayTypeFromPropertyValue(Class.class, value);
    } else if (type.isEnum()) {
      newValue = getEnum(type, value);
    } else if (type.isArray() && type.getComponentType().isEnum()) {
      newValue = createArrayEnumFromPropertyValue(type.getComponentType(), value);
    } else if (String[].class == type) {
      newValue = value.split(",");
    } else if (Object[].class == type) {
      newValue = value.split(",");
    } else if (Map.class == type) {
      newValue = Stream.of(value.split(",")).map(s -> s.split("=")).collect(Collectors.toMap(v -> v[0], v -> v[1]));
    } else if (float.class == type || Float.class == type) {
      newValue = Float.parseFloat(value);
    } else if (int.class == type || Integer.class == type) {
      newValue = Integer.parseInt(value);
    } else if (long.class == type || Long.class == type) {
      newValue = Long.parseLong(value);
    } else if (short.class == type || Short.class == type) {
      newValue = Short.parseShort(value);
    } else if (double.class == type || Double.class == type) {
      newValue = Double.parseDouble(value);
    } else if (boolean.class == type || Boolean.class == type) {
      newValue = Boolean.valueOf(value);
    } else if (Duration.class == type) {
      newValue = Duration.parse(value);
    }
    return newValue;
  }


  /**
   * Returns the enum for the supplied type and value.
   *
   * @param  clazz  of the enum
   * @param  value  of the enum
   *
   * @return  enum that matches the supplied value
   */
  protected static Enum<?> getEnum(final Class<?> clazz, final String value)
  {
    for (Object o : clazz.getEnumConstants()) {
      final Enum<?> e = (Enum<?>) o;
      if (e.name().equals(value)) {
        return e;
      }
    }
    throw new IllegalArgumentException(String.format("Unknown enum value %s for %s", value, clazz));
  }


  /**
   * Returns the object which represents the supplied class given the supplied string representation.
   *
   * @param  c  type to instantiate
   * @param  s  property value to parse
   *
   * @return  the supplied type or null
   */
  protected Object createTypeFromPropertyValue(final Class<?> c, final String s)
  {
    final Object newObject;
    if ("null".equals(s)) {
      newObject = null;
    } else {
      if (PropertyValueParser.isConfig(s)) {
        final PropertyValueParser configParser = new PropertyValueParser(s);
        newObject = configParser.initializeType();
      } else {
        if (Class.class == c) {
          newObject = createClass(s);
        } else {
          newObject = instantiateType(c, s);
        }
      }
    }
    return newObject;
  }


  /**
   * Returns the object which represents an array of the supplied class given the supplied string representation.
   *
   * @param  c  type to instantiate
   * @param  s  property value to parse
   *
   * @return  an array or null
   */
  protected Object createArrayTypeFromPropertyValue(final Class<?> c, final String s)
  {
    final Object newObject;
    if ("null".equals(s)) {
      newObject = null;
    } else {
      if (s.contains("},")) {
        final String[] classes = s.split("\\},");
        newObject = Array.newInstance(c, classes.length);
        for (int i = 0; i < classes.length; i++) {
          classes[i] = classes[i] + "}";
          if (PropertyValueParser.isConfig(classes[i])) {
            final PropertyValueParser configParser = new PropertyValueParser(classes[i]);
            Array.set(newObject, i, configParser.initializeType());
          } else {
            throw new IllegalArgumentException(String.format("Could not parse property string: %s", classes[i]));
          }
        }
      } else {
        final String[] classes = s.split(",");
        newObject = Array.newInstance(c, classes.length);
        for (int i = 0; i < classes.length; i++) {
          if (PropertyValueParser.isConfig(classes[i])) {
            final PropertyValueParser configParser = new PropertyValueParser(classes[i]);
            Array.set(newObject, i, configParser.initializeType());
          } else {
            if (Class.class == c) {
              Array.set(newObject, i, createClass(classes[i]));
            } else {
              Array.set(newObject, i, instantiateType(c, classes[i]));
            }
          }
        }
      }
    }
    return newObject;
  }


  /**
   * Returns the enum array which represents the supplied class given the supplied string representation.
   *
   * @param  c  type to instantiate
   * @param  s  property value to parse
   *
   * @return  Enum[] of the supplied type or null
   */
  protected Object createArrayEnumFromPropertyValue(final Class<?> c, final String s)
  {
    final Object newObject;
    if ("null".equals(s)) {
      newObject = null;
    } else {
      final String[] values = s.split(",");
      newObject = Array.newInstance(c, values.length);
      for (int i = 0; i < values.length; i++) {
        Array.set(newObject, i, getEnum(c, values[i]));
      }
    }
    return newObject;
  }


  /**
   * Invokes the supplied method on the supplied object with the supplied argument.
   *
   * @param  method  to invoke
   * @param  object  to invoke method on
   * @param  arg  to invoke method with
   *
   * @return  object produced by the invocation
   *
   * @throws  IllegalArgumentException  if an error occurs invoking the method
   */
  public static Object invokeMethod(final Method method, final Object object, final Object arg)
  {
    try {
      try {
        Object[] params = new Object[] {arg};
        if (arg == null && method.getParameterTypes().length == 0) {
          params = null;
        }
        return method.invoke(object, params);
      } catch (InvocationTargetException | IllegalAccessException e) {
        throw new IllegalArgumentException(e);
      }
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("Error invoking " + method + " on " + object + " with param " + arg, e);
    }
  }
}
