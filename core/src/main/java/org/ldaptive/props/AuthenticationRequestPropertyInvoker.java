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
package org.ldaptive.props;

import org.ldaptive.Credential;

/**
 * Handles properties for {@link org.ldaptive.auth.AuthenticationRequest}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class AuthenticationRequestPropertyInvoker
  extends AbstractPropertyInvoker
{


  /**
   * Creates a new authentication request property invoker for the supplied
   * class.
   *
   * @param  c  class that has setter methods
   */
  public AuthenticationRequestPropertyInvoker(final Class<?> c)
  {
    initialize(c);
  }


  /** {@inheritDoc} */
  @Override
  protected Object convertValue(final Class<?> type, final String value)
  {
    Object newValue = value;
    if (type != String.class) {
      if (Credential.class.isAssignableFrom(type)) {
        newValue = new Credential(value);
      } else {
        newValue = convertSimpleType(type, value);
      }
    }
    return newValue;
  }
}
