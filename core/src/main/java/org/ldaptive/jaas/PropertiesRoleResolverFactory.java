/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.util.HashMap;
import java.util.Map;
import org.ldaptive.ConnectionFactoryManager;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.SearchRequest;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.PooledConnectionFactoryManager;
import org.ldaptive.props.DefaultConnectionFactoryPropertySource;
import org.ldaptive.props.PooledConnectionFactoryPropertySource;
import org.ldaptive.props.PropertySource.PropertyDomain;
import org.ldaptive.props.SearchRequestPropertySource;

/**
 * Provides a module role resolver factory implementation that uses the
 * properties package in this library.
 *
 * @author  Middleware Services
 */
public class PropertiesRoleResolverFactory extends AbstractPropertiesFactory
  implements RoleResolverFactory
{

  /** Object CACHE. */
  private static final Map<String, RoleResolver> CACHE = new HashMap<>();


  /** {@inheritDoc} */
  @Override
  public RoleResolver createRoleResolver(final Map<String, ?> jaasOptions)
  {
    RoleResolver rr;
    if (jaasOptions.containsKey(CACHE_ID)) {
      final String cacheId = (String) jaasOptions.get(CACHE_ID);
      synchronized (CACHE) {
        if (!CACHE.containsKey(cacheId)) {
          rr = createRoleResolverInternal(jaasOptions);
          logger.trace("Created role resolver: {}", rr);
          CACHE.put(cacheId, rr);
        } else {
          rr = CACHE.get(cacheId);
          logger.trace("Retrieved role resolver from CACHE: {}", rr);
        }
      }
    } else {
      rr = createRoleResolverInternal(jaasOptions);
      logger.trace("Created role resolver {} from {}", rr, jaasOptions);
    }
    return rr;
  }


  /**
   * Initializes a role resolver using a role resolver property source.
   *
   * @param  options  to initialize role resolver
   *
   * @return  role resolver
   */
  protected RoleResolver createRoleResolverInternal(
    final Map<String, ?> options)
  {
    RoleResolver rr;
    if (options.containsKey("roleResolver")) {
      try {
        final String className = (String) options.get("roleResolver");
        rr = (RoleResolver) Class.forName(className).newInstance();
      } catch (ClassNotFoundException |
               IllegalAccessException |
               InstantiationException e) {
        throw new IllegalArgumentException(e);
      }
    } else {
      rr = new SearchRoleResolver();
    }
    if (rr instanceof PooledConnectionFactoryManager) {
      final PooledConnectionFactoryManager cfm =
        (PooledConnectionFactoryManager) rr;
      final PooledConnectionFactory cf = new PooledConnectionFactory();
      final PooledConnectionFactoryPropertySource source =
        new PooledConnectionFactoryPropertySource(
          cf,
          PropertyDomain.AUTH,
          createProperties(options));
      source.initialize();
      cfm.setConnectionFactory(cf);
    }
    if (rr instanceof ConnectionFactoryManager) {
      final ConnectionFactoryManager cfm = (ConnectionFactoryManager) rr;
      final DefaultConnectionFactory cf = new DefaultConnectionFactory();
      final DefaultConnectionFactoryPropertySource source =
        new DefaultConnectionFactoryPropertySource(
          cf,
          PropertyDomain.AUTH,
          createProperties(options));
      source.initialize();
      cfm.setConnectionFactory(cf);
    }
    return rr;
  }


  /** {@inheritDoc} */
  @Override
  public SearchRequest createSearchRequest(final Map<String, ?> jaasOptions)
  {
    final SearchRequest sr = new SearchRequest();
    final SearchRequestPropertySource source = new SearchRequestPropertySource(
      sr,
      PropertyDomain.AUTH,
      createProperties(jaasOptions));
    source.initialize();
    logger.trace("Created search request {} from {}", sr, jaasOptions);
    return sr;
  }


  /** Iterates over the CACHE and closes all role resolvers. */
  public static void close()
  {
    for (RoleResolver rr : CACHE.values()) {
      if (rr instanceof PooledConnectionFactoryManager) {
        final PooledConnectionFactoryManager cfm =
          (PooledConnectionFactoryManager) rr;
        cfm.getConnectionFactory().getConnectionPool().close();
      }
    }
  }
}
