/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.util.HashMap;
import java.util.Map;
import org.ldaptive.ConnectionFactoryManager;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.SearchRequest;
import org.ldaptive.props.DefaultConnectionFactoryPropertySource;
import org.ldaptive.props.PropertySource.PropertyDomain;
import org.ldaptive.props.SearchRequestPropertySource;
import org.ldaptive.props.SearchRoleResolverPropertySource;

/**
 * Provides a module role resolver factory implementation that uses the properties package in this library.
 *
 * @author  Middleware Services
 */
public class PropertiesRoleResolverFactory extends AbstractPropertiesFactory implements RoleResolverFactory
{

  /** Object CACHE. */
  private static final Map<String, RoleResolver> CACHE = new HashMap<>();


  @Override
  public RoleResolver createRoleResolver(final Map<String, ?> jaasOptions)
  {
    final RoleResolver rr;
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
  protected RoleResolver createRoleResolverInternal(final Map<String, ?> options)
  {
    final RoleResolver rr;
    if (options.containsKey("roleResolver")) {
      try {
        final String className = (String) options.get("roleResolver");
        rr = (RoleResolver) Class.forName(className).getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        throw new IllegalArgumentException(e);
      }
      if (rr instanceof ConnectionFactoryManager) {
        final ConnectionFactoryManager cfm = (ConnectionFactoryManager) rr;
        if (cfm.getConnectionFactory() == null) {
          final DefaultConnectionFactory cf = new DefaultConnectionFactory();
          final DefaultConnectionFactoryPropertySource cfPropSource = new DefaultConnectionFactoryPropertySource(
            cf,
            PropertyDomain.AUTH,
            createProperties(options));
          cfPropSource.initialize();
          cfm.setConnectionFactory(cf);
        }
      }
    } else {
      rr = new SearchRoleResolver();
      final SearchRoleResolverPropertySource source = new SearchRoleResolverPropertySource(
        (SearchRoleResolver) rr,
        createProperties(options));
      source.initialize();
    }
    return rr;
  }


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
    CACHE.values().stream().filter(rr -> rr instanceof ConnectionFactoryManager).forEach(rr -> {
      final ConnectionFactoryManager cfm = (ConnectionFactoryManager) rr;
      cfm.getConnectionFactory().close();
    });
  }
}
