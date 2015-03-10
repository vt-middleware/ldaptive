/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.io.Reader;
import java.util.Properties;
import java.util.Set;
import org.ldaptive.ConnectionFactoryManager;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.auth.AuthenticationHandler;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.BindAuthenticationHandler;
import org.ldaptive.auth.DnResolver;
import org.ldaptive.auth.SearchDnResolver;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.PooledConnectionFactoryManager;

/**
 * Reads properties specific to {@link org.ldaptive.auth.Authenticator} and returns an initialized object of that type.
 *
 * @author  Middleware Services
 */
public final class AuthenticatorPropertySource extends AbstractPropertySource<Authenticator>
{

  /** Invoker for authenticator. */
  private static final AuthenticatorPropertyInvoker INVOKER = new AuthenticatorPropertyInvoker(Authenticator.class);


  /**
   * Creates a new authenticator property source using the default properties file.
   *
   * @param  a  authenticator to set properties on
   */
  public AuthenticatorPropertySource(final Authenticator a)
  {
    this(a, PROPERTIES_FILE);
  }


  /**
   * Creates a new authenticator property source.
   *
   * @param  a  authenticator to set properties on
   * @param  paths  to read properties from
   */
  public AuthenticatorPropertySource(final Authenticator a, final String... paths)
  {
    this(a, loadProperties(paths));
  }


  /**
   * Creates a new authenticator property source.
   *
   * @param  a  authenticator to set properties on
   * @param  readers  to read properties from
   */
  public AuthenticatorPropertySource(final Authenticator a, final Reader... readers)
  {
    this(a, loadProperties(readers));
  }


  /**
   * Creates a new authenticator property source.
   *
   * @param  a  authenticator to set properties on
   * @param  props  to read properties from
   */
  public AuthenticatorPropertySource(final Authenticator a, final Properties props)
  {
    this(a, PropertyDomain.AUTH, props);
  }


  /**
   * Creates a new authenticator property source.
   *
   * @param  a  authenticator to set properties on
   * @param  domain  that properties are in
   * @param  props  to read properties from
   */
  public AuthenticatorPropertySource(final Authenticator a, final PropertyDomain domain, final Properties props)
  {
    super(a, domain, props);
  }


  @Override
  public void initialize()
  {
    initializeObject(INVOKER);

    // initialize a SearchDnResolver by default
    DnResolver dnResolver = object.getDnResolver();
    if (dnResolver == null) {
      dnResolver = new SearchDnResolver();

      final SearchDnResolverPropertySource dnPropSource = new SearchDnResolverPropertySource(
        (SearchDnResolver) dnResolver,
        propertiesDomain,
        properties);
      dnPropSource.initialize();
      object.setDnResolver(dnResolver);
    } else {
      final SimplePropertySource<DnResolver> sPropSource = new SimplePropertySource<>(
        dnResolver,
        propertiesDomain,
        properties);
      sPropSource.initialize();
    }
    if (dnResolver instanceof PooledConnectionFactoryManager) {
      final PooledConnectionFactoryManager cfm = (PooledConnectionFactoryManager) dnResolver;
      if (cfm.getConnectionFactory() == null) {
        initPooledConnectionFactoryManager(cfm);
      }
    }
    if (dnResolver instanceof ConnectionFactoryManager) {
      final ConnectionFactoryManager cfm = (ConnectionFactoryManager) dnResolver;
      if (cfm.getConnectionFactory() == null) {
        initConnectionFactoryManager(cfm);
      }
    }

    // initialize a BindAuthenticationHandler by default
    AuthenticationHandler authHandler = object.getAuthenticationHandler();
    if (authHandler == null) {
      authHandler = new BindAuthenticationHandler();

      final BindAuthenticationHandlerPropertySource ahPropSource = new BindAuthenticationHandlerPropertySource(
        (BindAuthenticationHandler) authHandler,
        propertiesDomain,
        properties);
      ahPropSource.initialize();
      object.setAuthenticationHandler(authHandler);
    } else {
      final SimplePropertySource<AuthenticationHandler> sPropSource = new SimplePropertySource<>(
        authHandler,
        propertiesDomain,
        properties);
      sPropSource.initialize();
    }
    if (authHandler instanceof PooledConnectionFactoryManager) {
      final PooledConnectionFactoryManager cfm = (PooledConnectionFactoryManager) authHandler;
      if (cfm.getConnectionFactory() == null) {
        initPooledConnectionFactoryManager(cfm);
      }
    }
    if (authHandler instanceof ConnectionFactoryManager) {
      final ConnectionFactoryManager cfm = (ConnectionFactoryManager) authHandler;
      if (cfm.getConnectionFactory() == null) {
        initConnectionFactoryManager(cfm);
      }
    }
  }


  /**
   * Initializes the supplied connection factory manager using the properties in this property source.
   *
   * @param  cfm  to initialize
   */
  private void initConnectionFactoryManager(final ConnectionFactoryManager cfm)
  {
    final DefaultConnectionFactory cf = new DefaultConnectionFactory();
    final DefaultConnectionFactoryPropertySource cfPropSource = new DefaultConnectionFactoryPropertySource(
      cf,
      propertiesDomain,
      properties);
    cfPropSource.initialize();
    cfm.setConnectionFactory(cf);
  }


  /**
   * Initializes the supplied connection factory manager using the properties in this property source.
   *
   * @param  cfm  to initialize
   */
  private void initPooledConnectionFactoryManager(final PooledConnectionFactoryManager cfm)
  {
    final PooledConnectionFactory cf = new PooledConnectionFactory();
    final PooledConnectionFactoryPropertySource cfPropSource = new PooledConnectionFactoryPropertySource(
      cf,
      propertiesDomain,
      properties);
    cfPropSource.initialize();
    cfm.setConnectionFactory(cf);
  }


  /**
   * Returns the property names for this property source.
   *
   * @return  all property names
   */
  public static Set<String> getProperties()
  {
    return INVOKER.getProperties();
  }
}
