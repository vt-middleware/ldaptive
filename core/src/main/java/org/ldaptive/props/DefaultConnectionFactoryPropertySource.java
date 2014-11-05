/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.io.Reader;
import java.util.Properties;
import java.util.Set;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.provider.ProviderConfig;

/**
 * Reads properties specific to {@link org.ldaptive.DefaultConnectionFactory}
 * and returns an initialized object of that type.
 *
 * @author  Middleware Services
 * @version  $Revision: 2999 $ $Date: 2014-06-11 13:29:32 -0400 (Wed, 11 Jun 2014) $
 */
public final class DefaultConnectionFactoryPropertySource
  extends AbstractPropertySource<DefaultConnectionFactory>
{

  /** Invoker for connection factory. */
  private static final DefaultConnectionFactoryPropertyInvoker INVOKER =
    new DefaultConnectionFactoryPropertyInvoker(DefaultConnectionFactory.class);


  /**
   * Creates a new default connection factory property source using the default
   * properties file.
   *
   * @param  cf  connection factory to invoke properties on
   */
  public DefaultConnectionFactoryPropertySource(
    final DefaultConnectionFactory cf)
  {
    this(cf, PROPERTIES_FILE);
  }


  /**
   * Creates a new default connection factory property source.
   *
   * @param  cf  connection factory to invoke properties on
   * @param  paths  to read properties from
   */
  public DefaultConnectionFactoryPropertySource(
    final DefaultConnectionFactory cf,
    final String... paths)
  {
    this(cf, loadProperties(paths));
  }


  /**
   * Creates a new default connection factory property source.
   *
   * @param  cf  connection factory to invoke properties on
   * @param  readers  to read properties from
   */
  public DefaultConnectionFactoryPropertySource(
    final DefaultConnectionFactory cf,
    final Reader... readers)
  {
    this(cf, loadProperties(readers));
  }


  /**
   * Creates a new default connection factory property source.
   *
   * @param  cf  connection factory to invoke properties on
   * @param  props  to read properties from
   */
  public DefaultConnectionFactoryPropertySource(
    final DefaultConnectionFactory cf,
    final Properties props)
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


  /** {@inheritDoc} */
  @Override
  @SuppressWarnings("unchecked")
  public void initialize()
  {
    initializeObject(INVOKER);

    final ConnectionConfig cc = new ConnectionConfig();
    final ConnectionConfigPropertySource ccPropSource =
      new ConnectionConfigPropertySource(cc, propertiesDomain, properties);
    ccPropSource.initialize();
    object.setConnectionConfig(cc);

    final ProviderConfig pc = new ProviderConfig();
    final ProviderConfigPropertySource pcPropSource =
      new ProviderConfigPropertySource(pc, propertiesDomain, properties);
    pcPropSource.initialize();
    object.getProvider().getProviderConfig().setConnectionStrategy(
      pc.getConnectionStrategy());
    if (pc.getOperationExceptionResultCodes() != null) {
      object.getProvider().getProviderConfig().setOperationExceptionResultCodes(
        pc.getOperationExceptionResultCodes());
    }
    if (pc.getControlProcessor() != null) {
      object.getProvider().getProviderConfig().setControlProcessor(
        pc.getControlProcessor());
    }
    if (!extraProps.isEmpty()) {
      object.getProvider().getProviderConfig().setProperties(extraProps);
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
