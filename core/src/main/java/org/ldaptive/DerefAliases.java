/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Enum to define how aliases are dereferenced.
 *
 * @author  Middleware Services
 */
public enum DerefAliases {

  /** never dereference aliases. */
  NEVER,

  /**
   * dereference when searching the entries beneath the starting point but not
   * when searching for the starting entry.
   */
  SEARCHING,

  /**
   * dereference when searching for the starting entry but not when searching
   * the entries beneath the starting point.
   */
  FINDING,

  /**
   * dereference when searching for the starting entry and when searching the
   * entries beneath the starting point.
   */
  ALWAYS
}
