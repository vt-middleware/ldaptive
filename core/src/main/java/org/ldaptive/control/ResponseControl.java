/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.asn1.DERBuffer;

/**
 * Marker interface for ldap response controls.
 *
 * @author  Middleware Services
 */
public interface ResponseControl extends Control
{


  /**
   * Initializes this response control with the supplied BER encoded data.
   *
   * @param  encoded  BER encoded response control
   */
  void decode(DERBuffer encoded);
}
