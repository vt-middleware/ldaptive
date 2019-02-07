/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.util.HashMap;
import java.util.Map;
import org.ldaptive.ConnectionFactoryManager;
import org.ldaptive.auth.AggregateAuthenticationHandler;
import org.ldaptive.auth.AggregateDnResolver;
import org.ldaptive.auth.AuthenticationRequest;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.props.AuthenticationRequestPropertySource;
import org.ldaptive.props.AuthenticatorPropertySource;

/**
 * Provides a module authenticator factory implementation that uses the properties package in this library.
 *
 * @author  Middleware Services
 */
public class PropertiesAuthenticatorFactory extends AbstractPropertiesFactory implements AuthenticatorFactory
{

  /** Object CACHE. */
  private static final Map<String, Authenticator> CACHE = new HashMap<>();


  @Override
  public Authenticator createAuthenticator(final Map<String, ?> jaasOptions)
  {
    final Authenticator a;
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
  protected Authenticator createAuthenticatorInternal(final Map<String, ?> options)
  {
    final Authenticator a = new Authenticator();
    final AuthenticatorPropertySource source = new AuthenticatorPropertySource(a, createProperties(options));
    source.initialize();
    return a;
  }


  @Override
  public AuthenticationRequest createAuthenticationRequest(final Map<String, ?> jaasOptions)
  {
    final AuthenticationRequest ar = new AuthenticationRequest();
    final AuthenticationRequestPropertySource source = new AuthenticationRequestPropertySource(
      ar,
      createProperties(jaasOptions));
    source.initialize();
    logger.trace("Created authentication request {} from {}", ar, jaasOptions);
    return ar;
  }


  /** Iterates over the CACHE and closes any managed dn resolvers and managed authentication handlers. */
  public static void close()
  {
    for (Map.Entry<String, Authenticator> e : CACHE.entrySet()) {
      final Authenticator a = e.getValue();
      if (a.getDnResolver() instanceof ConnectionFactoryManager) {
        ((ConnectionFactoryManager) a.getDnResolver()).getConnectionFactory().close();
      } else if (a.getDnResolver() instanceof AggregateDnResolver) {
        ((AggregateDnResolver) a.getDnResolver()).getDnResolvers().values().stream()
          .filter(ConnectionFactoryManager.class::isInstance)
          .map(ConnectionFactoryManager.class::cast)
          .forEach(r -> r.getConnectionFactory().close());
      }

      if (a.getAuthenticationHandler() instanceof ConnectionFactoryManager) {
        ((ConnectionFactoryManager) a.getAuthenticationHandler()).getConnectionFactory().close();
      } else if (a.getAuthenticationHandler() instanceof AggregateAuthenticationHandler) {
        final AggregateAuthenticationHandler ah = (AggregateAuthenticationHandler) a.getAuthenticationHandler();
        ah.getAuthenticationHandlers().values().stream()
          .filter(ConnectionFactoryManager.class::isInstance)
          .map(ConnectionFactoryManager.class::cast)
          .forEach(h -> h.getConnectionFactory().close());
      }
    }
  }
}
