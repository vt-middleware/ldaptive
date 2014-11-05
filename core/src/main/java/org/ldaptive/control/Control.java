/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

/**
 * Marker interface for ldap controls.
 *
 * @author  Middleware Services
 */
public interface Control
{


  /**
   * Returns the OID for this control.
   *
   * @return  oid
   */
  String getOID();


  /**
   * Returns whether the control is critical.
   *
   * @return  whether the control is critical
   */
  boolean getCriticality();
}
