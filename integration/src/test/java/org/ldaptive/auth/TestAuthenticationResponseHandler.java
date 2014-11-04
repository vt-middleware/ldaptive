/*
  $Id: TestAuthenticationResponseHandler.java 2619 2013-02-12 21:52:33Z dfisher $

  Copyright (C) 2003-2010 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2619 $
  Updated: $Date: 2013-02-12 16:52:33 -0500 (Tue, 12 Feb 2013) $
*/
package org.ldaptive.auth;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for testing that authentication response handlers are firing.
 *
 * @author  Middleware Services
 * @version  $Revision: 2619 $ $Date: 2013-02-12 16:52:33 -0500 (Tue, 12 Feb 2013) $
 */
public class TestAuthenticationResponseHandler
  implements AuthenticationResponseHandler
{

  /** results. */
  private final Map<String, Boolean> results = new HashMap<>();


  /** {@inheritDoc} */
  @Override
  public void handle(final AuthenticationResponse response)
  {
    results.put(response.getLdapEntry().getDn(), response.getResult());
  }


  /**
   * Returns the authentication results.
   *
   * @return  authentication results
   */
  public Map<String, Boolean> getResults()
  {
    return results;
  }
}
