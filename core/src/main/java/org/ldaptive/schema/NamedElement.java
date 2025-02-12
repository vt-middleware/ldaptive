/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

/**
 * Interface for schema elements that have name identifiers.
 *
 * @author  Middleware Services
 */
public interface NamedElement
{


  /**
   * Returns the first name defined or null if no names are defined.
   *
   * @return  first name in the list
   */
  String getName();


  /**
   * Returns the names.
   *
   * @return  names
   */
  String[] getNames();
}
