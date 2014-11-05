/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans;

import java.util.Collection;
import org.ldaptive.SortBehavior;

/**
 * Interface for mutating an attribute value on an arbitrary object.
 *
 * @author  Middleware Services
 */
public interface AttributeValueMutator
{


  /**
   * Returns the name of the attribute.
   *
   * @return  attribute name
   */
  String getName();


  /**
   * Returns whether the attribute is binary.
   *
   * @return  whether the attribute is binary
   */
  boolean isBinary();


  /**
   * Returns the sort behavior of the attribute.
   *
   * @return  sort behavior of the attribute
   */
  SortBehavior getSortBehavior();


  /**
   * Returns the string values of the attribute.
   *
   * @param  object  containing attribute values
   *
   * @return  attribute values
   */
  Collection<String> getStringValues(Object object);


  /**
   * Returns the binary values of the attribute.
   *
   * @param  object  containing attribute values
   *
   * @return  attribute values
   */
  Collection<byte[]> getBinaryValues(Object object);


  /**
   * Sets the string values of the attribute.
   *
   * @param  object  to set values on
   * @param  values  to set
   */
  void setStringValues(Object object, Collection<String> values);


  /**
   * Sets the binary values of the attribute.
   *
   * @param  object  to set values on
   * @param  values  to set
   */
  void setBinaryValues(Object object, Collection<byte[]> values);
}
