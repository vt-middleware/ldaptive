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
package org.ldaptive.provider;

import org.ldaptive.LdapException;
import org.ldaptive.Response;

/**
 * Search results iterator.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface SearchIterator
{


  /**
   * Returns true if the iteration has more elements.
   *
   * @return  true if the iterator has more elements
   *
   * @throws  LdapException  if an error occurs
   */
  boolean hasNext()
    throws LdapException;


  /**
   * Returns the next element in the iteration.
   *
   * @return  the next element in the iteration
   *
   * @throws  LdapException  if an error occurs
   */
  SearchItem next()
    throws LdapException;


  /**
   * Returns the response data associated with this search or null if this
   * iterator has more ldap entries to return.
   *
   * @return  response data
   */
  Response<Void> getResponse();


  /**
   * Close any resources associated with this iterator.
   *
   * @throws  LdapException  if an error occurs
   */
  void close()
    throws LdapException;
}
