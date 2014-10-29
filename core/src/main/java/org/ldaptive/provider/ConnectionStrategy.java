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

/**
 * Interface to describe various connection strategies. Each strategy returns an
 * ordered list of URLs to attempt when opening a connection.
 *
 * @author  Middleware Services
 * @version  $Revision: 2974 $ $Date: 2014-04-21 15:29:45 -0400 (Mon, 21 Apr 2014) $
 */
public interface ConnectionStrategy
{

  /** default strategy. */
  ConnectionStrategy DEFAULT =
    new ConnectionStrategies.DefaultConnectionStrategy();

  /** active-passive strategy. */
  ConnectionStrategy ACTIVE_PASSIVE =
    new ConnectionStrategies.ActivePassiveConnectionStrategy();

  /** round robin strategy. */
  ConnectionStrategy ROUND_ROBIN =
    new ConnectionStrategies.RoundRobinConnectionStrategy();

  /** random strategy. */
  ConnectionStrategy RANDOM =
    new ConnectionStrategies.RandomConnectionStrategy();


  /**
   * Returns an ordered list of URLs to attempt to open.
   *
   * @param  metadata  which can be used to produce the URL list
   *
   * @return  array of ldap URLs
   */
  String[] getLdapUrls(ConnectionFactoryMetadata metadata);
}
