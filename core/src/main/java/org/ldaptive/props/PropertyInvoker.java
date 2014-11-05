/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.util.Set;

/**
 * Interface for property driven object method invocation.
 *
 * @author  Middleware Services
 */
public interface PropertyInvoker
{


  /**
   * Invokes the setter method on the supplied object for the supplied property
   * name and value.
   *
   * @param  object  to invoke property setter on
   * @param  name  of the property to invoke
   * @param  value  of the property to set
   */
  void setProperty(Object object, String name, String value);


  /**
   * Returns whether a property with the supplied name exists on this invoker.
   *
   * @param  name  of the property to check
   *
   * @return  whether a property with the supplied name exists on this invoker
   */
  boolean hasProperty(String name);


  /**
   * Returns the property names for this invoker.
   *
   * @return  set of property names
   */
  Set<String> getProperties();
}
