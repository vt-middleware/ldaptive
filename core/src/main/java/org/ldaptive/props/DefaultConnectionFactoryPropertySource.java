/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.io.Reader;
import java.util.Properties;
import java.util.Set;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.DefaultConnectionFactory;

/**
 * Reads properties specific to {@link org.ldaptive.DefaultConnectionFactory} and returns an initialized object of that
 * type.
 *
 * @author  Middleware Services
 */
public final class DefaultConnectionFactoryPropertySource extends AbstractPropertySource<DefaultConnectionFactory>
{

  /** Invoker for connection factory. */
  private static final DefaultConnectionFactoryPropertyInvoker INVOKER = new DefaultConnectionFactoryPropertyInvoker(
    DefaultConnectionFactory.class);


  /**
   * Creates a new default connection factory property source using the default properties file.
   *
   * @param  cf  connection factory to invoke properties on
   */
  public DefaultConnectionFactoryPropertySource(final DefaultConnectionFactory cf)
  {
    this(cf, PROPERTIES_FILE);
  }


  /**
   * Creates a new default connection factory property source.
   *
   * @param  cf  connection factory to invoke properties on
   * @param  paths  to read properties from
   */
  public DefaultConnectionFactoryPropertySource(final DefaultConnectionFactory cf, final String... paths)
  {
    this(cf, loadProperties(paths));
  }


  /**
   * Creates a new default connection factory property source.
   *
   * @param  cf  connection factory to invoke properties on
   * @param  readers  to read properties from
   */
  public DefaultConnectionFactoryPropertySource(final DefaultConnectionFactory cf, final Reader... readers)
  {
    this(cf, loadProperties(readers));
  }


  /**
   * Creates a new default connection factory property source.
   *
   * @param  cf  connection factory to invoke properties on
   * @param  props  to read properties from
   */
  public DefaultConnectionFactoryPropertySource(final DefaultConnectionFactory cf, final Properties props)
  {
    this(cf, PropertyDomain.LDAP, props);
  }


  /**
   * Creates a new default connection factory property source.
   *
   * @param  cf  connection factory to invoke properties on
   * @param  domain  that properties are in
   * @param  props  to read properties from
   */
  public DefaultConnectionFactoryPropertySource(
    final DefaultConnectionFactory cf,
    final PropertyDomain domain,
    final Properties props)
  {
    super(cf, domain, props);
  }


  @Override
  public void initialize()
  {
    initializeObject(INVOKER);

    final ConnectionConfig cc = new ConnectionConfig();
    final ConnectionConfigPropertySource ccPropSource = new ConnectionConfigPropertySource(
      cc,
      propertiesDomain,
      properties);
    ccPropSource.initialize();
    object.setConnectionConfig(cc);
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
