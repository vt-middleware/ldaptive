/* See LICENSE for licensing and NOTICE for copyright. */
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
 */
public interface ServletSearchOperation
{


  /**
   * Prepares this servlet search operation for use.
   *
   * @param  config  to initialize this object with
   */
  void initialize(ServletConfig config);


  /**
   * Reads parameters from the request, performs an ldap search, and writes the result to the response.
   *
   * @param  request  servlet request to read search request parameters from
   * @param  response  servlet response to write ldap search result to
   *
   * @throws  LdapException  if an error occurs performing the search
   * @throws  IOException  if an error occurs writing to the response
   */
  void execute(HttpServletRequest request, HttpServletResponse response)
    throws LdapException, IOException;


  /** Closes any resources associated with this servlet search operation. */
  void close();
}
