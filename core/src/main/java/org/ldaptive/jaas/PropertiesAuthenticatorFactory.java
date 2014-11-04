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

import java.util.HashMap;
import java.util.Map;
import org.ldaptive.auth.AuthenticationHandler;
import org.ldaptive.auth.AuthenticationRequest;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.pool.PooledConnectionFactoryManager;
import org.ldaptive.props.AuthenticationRequestPropertySource;
import org.ldaptive.props.AuthenticatorPropertySource;

/**
 * Provides a module authenticator factory implementation that uses the
 * properties package in this library.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class PropertiesAuthenticatorFactory extends AbstractPropertiesFactory
  implements AuthenticatorFactory
{

  /** Object CACHE. */
  private static final Map<String, Authenticator> CACHE = new HashMap<>();


  /** {@inheritDoc} */
  @Override
  public Authenticator createAuthenticator(final Map<String, ?> jaasOptions)
  {
    Authenticator a;
    if (jaasOptions.containsKey(CACHE_ID)) {
      final String cacheId = (String) jaasOptions.get(CACHE_ID);
      synchronized (CACHE) {
        if (!CACHE.containsKey(cacheId)) {
          a = createAuthenticatorInternal(jaasOptions);
          logger.trace("Created authenticator: {}", a);
          CACHE.put(cacheId, a);
        } else {
          a = CACHE.get(cacheId);
          logger.trace("Retrieved authenticator from CACHE: {}", a);
        }
      }
    } else {
      a = createAuthenticatorInternal(jaasOptions);
      logger.trace("Created authenticator {} from {}", a, jaasOptions);
    }
    return a;
  }


  /**
   * Initializes an authenticator using an authenticator property source.
   *
   * @param  options  to initialize authenticator
   *
   * @return  authenticator
   */
  protected Authenticator createAuthenticatorInternal(
    final Map<String, ?> options)
  {
    final Authenticator a = new Authenticator();
    final AuthenticatorPropertySource source = new AuthenticatorPropertySource(
      a,
      createProperties(options));
    source.initialize();
    return a;
  }


  /** {@inheritDoc} */
  @Override
  public AuthenticationRequest createAuthenticationRequest(
    final Map<String, ?> jaasOptions)
  {
    final AuthenticationRequest ar = new AuthenticationRequest();
    final AuthenticationRequestPropertySource source =
      new AuthenticationRequestPropertySource(
        ar,
        createProperties(jaasOptions));
    source.initialize();
    logger.trace("Created authentication request {} from {}", ar, jaasOptions);
    return ar;
  }


  /**
   * Iterates over the CACHE and closes any managed dn resolvers and managed
   * authentication handlers.
   */
  public static void close()
  {
    for (Map.Entry<String, Authenticator> e : CACHE.entrySet()) {
      final Authenticator a = e.getValue();
      if (a.getDnResolver() instanceof PooledConnectionFactoryManager) {
        final PooledConnectionFactoryManager cfm =
          (PooledConnectionFactoryManager) a.getDnResolver();
        cfm.getConnectionFactory().getConnectionPool().close();
      }

      final AuthenticationHandler ah = a.getAuthenticationHandler();
      if (ah instanceof PooledConnectionFactoryManager) {
        final PooledConnectionFactoryManager cfm =
          (PooledConnectionFactoryManager) ah;
        cfm.getConnectionFactory().getConnectionPool().close();
      }
    }
  }
}
