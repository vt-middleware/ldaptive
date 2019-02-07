/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dns;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

/**
 * Interface to provide {@link DirContext} implementations to be used for DNS queries.
 *
 * @author  Middleware Services
 */
public interface DNSContextFactory
{


  /**
   * Creates a new JNDI context.
   *
   * @return  JNDI context
   *
   * @throws NamingException  if an error occurs creating the context
   */
  DirContext create() throws NamingException;
}
