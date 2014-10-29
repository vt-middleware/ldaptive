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
package org.ldaptive.sasl;

/**
 * Enum to define SASL quality of protection.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public enum QualityOfProtection {

  /** Authentication only. */
  AUTH,

  /** Authentication with integrity protection. */
  AUTH_INT,

  /** Authentication with integrity and privacy protection. */
  AUTH_CONF
}
