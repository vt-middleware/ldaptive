/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.util.Map;
import org.ldaptive.SearchRequest;

/**
 * Provides an interface for creating role resolver needed by various JAAS
 * modules.
 *
 * @author  Middleware Services
 */
public interface RoleResolverFactory
{


  /**
   * Creates a new role resolver with the supplied JAAS options.
   *
   * @param  jaasOptions  JAAS configuration options
   *
   * @return  role resolver
   */
  RoleResolver createRoleResolver(Map<String, ?> jaasOptions);


  /**
   * Creates a new search request with the supplied JAAS options.
   *
   * @param  jaasOptions  JAAS configuration options
   *
   * @return  search request
   */
  SearchRequest createSearchRequest(Map<String, ?> jaasOptions);
}
