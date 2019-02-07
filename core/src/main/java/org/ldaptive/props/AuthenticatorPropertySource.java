/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.io.Reader;
import java.util.Properties;
import java.util.Set;
import org.ldaptive.ConnectionFactoryManager;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.auth.AuthenticationHandler;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.CompareAuthenticationHandler;
import org.ldaptive.auth.DnResolver;
import org.ldaptive.auth.EntryResolver;
import org.ldaptive.auth.SearchDnResolver;
import org.ldaptive.auth.SearchEntryResolver;
import org.ldaptive.auth.SimpleBindAuthenticationHandler;

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
      if (dnResolver instanceof SearchDnResolver) {
        final SearchDnResolverPropertySource dnPropSource = new SearchDnResolverPropertySource(
          (SearchDnResolver) dnResolver,
          propertiesDomain,
          properties);
        dnPropSource.initialize();
      } else {
        final SimplePropertySource<DnResolver> sPropSource = new SimplePropertySource<>(
          dnResolver,
          propertiesDomain,
          properties);
        sPropSource.initialize();
        if (dnResolver instanceof ConnectionFactoryManager) {
          final ConnectionFactoryManager resolverManager = (ConnectionFactoryManager) dnResolver;
          if (resolverManager.getConnectionFactory() == null) {
            initConnectionFactoryManager(resolverManager);
          }
        }
      }
    }

    // initialize the EntryResolver if supplied
    final EntryResolver entryResolver = object.getEntryResolver();
    if (entryResolver != null) {
      if (entryResolver instanceof SearchEntryResolver) {
        final SearchEntryResolverPropertySource entryPropSource = new SearchEntryResolverPropertySource(
          (SearchEntryResolver) entryResolver,
          propertiesDomain,
          properties);
        entryPropSource.initialize();
      } else {
        final SimplePropertySource<EntryResolver> sPropSource = new SimplePropertySource<>(
          entryResolver,
          propertiesDomain,
          properties);
        sPropSource.initialize();
        if (entryResolver instanceof ConnectionFactoryManager) {
          final ConnectionFactoryManager resolverManager = (ConnectionFactoryManager) entryResolver;
          if (resolverManager.getConnectionFactory() == null) {
            initConnectionFactoryManager(resolverManager);
          }
        }
      }
    }

    // initialize a BindAuthenticationHandler by default
    AuthenticationHandler authHandler = object.getAuthenticationHandler();
    if (authHandler == null) {
      authHandler = new SimpleBindAuthenticationHandler();
      final SimpleBindAuthenticationHandlerPropertySource ahPropSource =
        new SimpleBindAuthenticationHandlerPropertySource(
          (SimpleBindAuthenticationHandler) authHandler,
          propertiesDomain,
          properties);
      ahPropSource.initialize();
      object.setAuthenticationHandler(authHandler);
    } else {
      if (authHandler instanceof SimpleBindAuthenticationHandler) {
        final SimpleBindAuthenticationHandlerPropertySource ahPropSource =
          new SimpleBindAuthenticationHandlerPropertySource(
            (SimpleBindAuthenticationHandler) authHandler,
            propertiesDomain,
            properties);
        ahPropSource.initialize();
      } else if (authHandler instanceof CompareAuthenticationHandler) {
        final CompareAuthenticationHandlerPropertySource ahPropSource =
          new CompareAuthenticationHandlerPropertySource(
            (CompareAuthenticationHandler) authHandler,
            propertiesDomain,
            properties);
        ahPropSource.initialize();
      } else {
        final SimplePropertySource<AuthenticationHandler> sPropSource = new SimplePropertySource<>(
          authHandler,
          propertiesDomain,
          properties);
        sPropSource.initialize();
        if (authHandler instanceof ConnectionFactoryManager) {
          final ConnectionFactoryManager handlerManager = (ConnectionFactoryManager) authHandler;
          if (handlerManager.getConnectionFactory() == null) {
            initConnectionFactoryManager(handlerManager);
          }
        }
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
   * Returns the property names for this property source.
   *
   * @return  all property names
   */
  public static Set<String> getProperties()
  {
    return INVOKER.getProperties();
  }
}
