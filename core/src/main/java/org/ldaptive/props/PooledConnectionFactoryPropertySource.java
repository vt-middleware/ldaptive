/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.io.Reader;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.PooledConnectionFactory;

/**
 * Reads properties specific to {@link PooledConnectionFactory} and returns an initialized object of that type.
 *
 * @author  Middleware Services
 */
public final class PooledConnectionFactoryPropertySource extends AbstractPropertySource<PooledConnectionFactory>
{

  /** Invoker for connection factory. */
  private static final PooledConnectionFactoryPropertyInvoker INVOKER = new PooledConnectionFactoryPropertyInvoker(
    PooledConnectionFactory.class);


  /**
   * Creates a new pooled connection factory property source using the default properties file.
   *
   * @param  cf  connection factory to invoke properties on
   */
  public PooledConnectionFactoryPropertySource(final PooledConnectionFactory cf)
  {
    this(cf, PROPERTIES_FILE);
  }


  /**
   * Creates a new pooled connection factory property source.
   *
   * @param  cf  connection factory to invoke properties on
   * @param  paths  to read properties from
   */
  public PooledConnectionFactoryPropertySource(final PooledConnectionFactory cf, final String... paths)
  {
    this(cf, loadProperties(paths));
  }


  /**
   * Creates a new pooled connection factory property source.
   *
   * @param  cf  connection factory to invoke properties on
   * @param  readers  to read properties from
   */
  public PooledConnectionFactoryPropertySource(final PooledConnectionFactory cf, final Reader... readers)
  {
    this(cf, loadProperties(readers));
  }


  /**
   * Creates a new pooled connection factory property source.
   *
   * @param  cf  connection factory to invoke properties on
   * @param  props  to read properties from
   */
  public PooledConnectionFactoryPropertySource(final PooledConnectionFactory cf, final Properties props)
  {
    this(cf, PropertyDomain.POOL, props);
  }


  /**
   * Creates a new pooled connection factory property source.
   *
   * @param  cf  connection factory to invoke properties on
   * @param  domain  that properties are in
   * @param  props  to read properties from
   */
  public PooledConnectionFactoryPropertySource(
    final PooledConnectionFactory cf,
    final PropertyDomain domain,
    final Properties props)
  {
    super(cf, domain, props);
  }


  @Override
  public void initialize()
  {
    initializeObject(INVOKER);

    ConnectionConfig cc = object.getConnectionConfig();
    if (cc == null) {
      cc = new ConnectionConfig();
      final ConnectionConfigPropertySource ccPropSource = new ConnectionConfigPropertySource(
        cc,
        propertiesDomain,
        properties);
      ccPropSource.initialize();
      object.setConnectionConfig(cc);
    }

    final BlockingConnectionPoolPropertySource cpPropSource = new BlockingConnectionPoolPropertySource(
      object,
      propertiesDomain,
      properties);
    cpPropSource.initialize();
    object.initialize();
  }


  /**
   * Returns the property names for this property source.
   *
   * @return  all property names
   */
  public static Set<String> getProperties()
  {
    return Collections.emptySet();
  }
}
