/*
  $Id: TestCallbackHandler.java 2198 2012-01-04 21:02:09Z dfisher $

  Copyright (C) 2003-2010 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2198 $
  Updated: $Date: 2012-01-04 16:02:09 -0500 (Wed, 04 Jan 2012) $
*/
package org.ldaptive.jaas;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Class that implements a callback handler to help with jaas testing.
 *
 * @author  Middleware Services
 * @version  $Revision: 2198 $
 */
public class TestCallbackHandler implements CallbackHandler
{

  /** test name. */
  private String name;

  /** test password. */
  private String password;


  /** @param  s  to set name with */
  public void setName(final String s)
  {
    name = s;
  }


  /** @param  s  to set password with */
  public void setPassword(final String s)
  {
    password = s;
  }


  /**
   * @param  callbacks  to handle
   *
   * @throws  IOException  if an input or output error occurs
   * @throws  UnsupportedCallbackException  if a supplied callback cannot be
   * handled
   */
  public void handle(final Callback[] callbacks)
    throws IOException, UnsupportedCallbackException
  {
    for (Callback callback : callbacks) {
      if (callback instanceof NameCallback) {
        final NameCallback nc = (NameCallback) callback;
        nc.setName(name);
      } else if (callback instanceof PasswordCallback) {
        final PasswordCallback pc = (PasswordCallback) callback;
        if (password != null) {
          pc.setPassword(password.toCharArray());
        }
      } else {
        throw new UnsupportedCallbackException(callback, "Unsupported");
      }
    }
  }
}
