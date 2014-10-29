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
package org.ldaptive.servlets;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ldaptive.LdapException;

/**
 * Interface to encapsulate servlet search operations.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface ServletSearchExecutor
{


  /**
   * Prepares this servlet search executor for use.
   *
   * @param  config  to initialize this object with
   */
  void initialize(ServletConfig config);


  /**
   * Reads parameters from the request, performs an ldap search, and writes the
   * result to the response.
   *
   * @param  request  servlet request to read search parameters from
   * @param  response  servlet response to write ldap search result to
   *
   * @throws  LdapException  if an error occurs performing the search
   * @throws  IOException  if an error occurs writing to the response
   */
  void search(HttpServletRequest request, HttpServletResponse response)
    throws LdapException, IOException;


  /** Closes any resources associated with this servlet search executor. */
  void close();
}
