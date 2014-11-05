/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

/**
 * Marker interface for ldap request controls.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface RequestControl extends Control
{


  /**
   * Provides the BER encoding of this control.
   *
   * @return  BER encoded request control
   */
  byte[] encode();
}
