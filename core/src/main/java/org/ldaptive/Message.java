/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.control.Control;

/**
 * Interface for ldap messages.
 *
 * @param  <T>  type of control
 *
 * @author  Middleware Services
 */
public interface Message<T extends Control>
{


  /**
   * Returns the controls for this message.
   *
   * @return  controls
   */
  T[] getControls();
}
