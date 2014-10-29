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
package org.ldaptive.jaas;

import java.util.Map;
import org.ldaptive.SearchRequest;

/**
 * Provides an interface for creating role resolver needed by various JAAS
 * modules.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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
