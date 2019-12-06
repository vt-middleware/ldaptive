/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.io.Reader;
import java.util.Properties;
import java.util.Set;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionInitializer;
import org.ldaptive.ssl.SslConfig;

/**
 * Reads properties specific to {@link ConnectionConfig} and returns an initialized object of that type.
 *
 * @author  Middleware Services
 */
public final class ConnectionConfigPropertySource extends AbstractPropertySource<ConnectionConfig>
{

  /** Invoker for connection config. */
  private static final ConnectionConfigPropertyInvoker INVOKER = new ConnectionConfigPropertyInvoker(
    ConnectionConfig.class);


  /**
   * Creates a new connection config property source using the default properties file.
   *
   * @param  cc  connection config to invoke properties on
   */
  public ConnectionConfigPropertySource(final ConnectionConfig cc)
  {
    this(cc, PROPERTIES_FILE);
  }


  /**
   * Creates a new connection config property source.
   *
   * @param  cc  connection config to invoke properties on
   * @param  paths  to read properties from
   */
  public ConnectionConfigPropertySource(final ConnectionConfig cc, final String... paths)
  {
    this(cc, loadProperties(paths));
  }


  /**
   * Creates a new connection config property source.
   *
   * @param  cc  connection config to invoke properties on
   * @param  readers  to read properties from
   */
  public ConnectionConfigPropertySource(final ConnectionConfig cc, final Reader... readers)
  {
    this(cc, loadProperties(readers));
  }


  /**
   * Creates a new connection config property source.
   *
   * @param  cc  connection config to invoke properties on
   * @param  props  to read properties from
   */
  public ConnectionConfigPropertySource(final ConnectionConfig cc, final Properties props)
  {
    this(cc, PropertyDomain.LDAP, props);
  }


  /**
   * Creates a new connection config property source.
   *
   * @param  cc  connection config to invoke properties on
   * @param  domain  that properties are in
   * @param  props  to read properties from
   */
  public ConnectionConfigPropertySource(final ConnectionConfig cc, final PropertyDomain domain, final Properties props)
  {
    super(cc, domain, props);
  }


  @Override
  public void initialize()
  {
    initializeObject(INVOKER);

    SslConfig sc = object.getSslConfig();
    if (sc == null) {
      sc = new SslConfig();
      final SslConfigPropertySource scSource = new SslConfigPropertySource(sc, propertiesDomain, properties);
      scSource.initialize();
      if (!sc.isEmpty()) {
        object.setSslConfig(sc);
      }
    } else {
      final SslConfigPropertySource scSource = new SslConfigPropertySource(sc, propertiesDomain, properties);
      scSource.initialize();
    }


    final ConnectionInitializer[] initializers = object.getConnectionInitializers();
    // configure a bind connection initializer if bind properties are found
    if (initializers == null) {
      final BindConnectionInitializer bci = new BindConnectionInitializer();
      final BindConnectionInitializerPropertySource bciSource = new BindConnectionInitializerPropertySource(
        bci,
        propertiesDomain,
        properties);
      bciSource.initialize();
      if (!bci.isEmpty()) {
        object.setConnectionInitializers(bci);
      }
    } else {
      for (ConnectionInitializer init : initializers) {
        final SimplePropertySource<ConnectionInitializer> sPropSource = new SimplePropertySource<>(
          init,
          propertiesDomain,
          properties);
        sPropSource.initialize();
      }
    }
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
