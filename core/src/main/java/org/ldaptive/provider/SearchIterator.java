/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import org.ldaptive.LdapException;
import org.ldaptive.Response;

/**
 * Search results iterator.
 *
 * @author  Middleware Services
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
   * Returns the response data associated with this search or null if this iterator has more ldap entries to return.
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
