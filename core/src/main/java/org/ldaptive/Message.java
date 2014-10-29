/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive;

import org.ldaptive.control.Control;

/**
 * Interface for ldap messages.
 *
 * @param  <T>  type of control
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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
