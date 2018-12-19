/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

/**
 * Marker interface for ldap request controls.
 *
 * @author  Middleware Services
 */
public interface RequestControl extends Control
{


  /**
   * Provides the BER encoding of this control.
   *
   * @return  BER encoded request control
   */
  byte[] encode();


  /**
   * Returns whether the control has a value associated with it.
   *
   * @return  whether the control has a value
   */
  boolean hasValue();
}
