/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.util.Properties;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.ConnectionFactoryManager;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.PooledConnectionFactory;

/**
 * Property source for classes that contain a connection factory.
 *
 * @param  <T>  type of connection factory manager
 *
 * @author  Middleware Services
 */
public abstract class AbstractConnectionFactoryManagerPropertySource<T extends ConnectionFactoryManager>
  extends AbstractPropertySource<T>
{


  /**
   * Creates a new search dn resolver property source.
   *
   * @param  resolver  search dn resolver to invoke properties on
   * @param  domain  that properties are in
   * @param  props  to read properties from
   */
  public AbstractConnectionFactoryManagerPropertySource(
    final T resolver,
    final PropertyDomain domain,
    final Properties props)
  {
    super(resolver, domain, props);
  }


  @Override
  public void initialize()
  {
    ConnectionFactory cf = object.getConnectionFactory();
    if (cf == null) {
      cf = new DefaultConnectionFactory();
      final DefaultConnectionFactoryPropertySource cfPropSource = new DefaultConnectionFactoryPropertySource(
        (DefaultConnectionFactory) cf,
        propertiesDomain,
        properties);
      cfPropSource.initialize();
      object.setConnectionFactory(cf);
    } else {
      if (cf instanceof DefaultConnectionFactory) {
        final DefaultConnectionFactoryPropertySource cfPropSource = new DefaultConnectionFactoryPropertySource(
          (DefaultConnectionFactory) cf,
          propertiesDomain,
          properties);
        cfPropSource.initialize();
      } else if (cf instanceof PooledConnectionFactory) {
        final PooledConnectionFactoryPropertySource cfPropSource = new PooledConnectionFactoryPropertySource(
          (PooledConnectionFactory) cf,
          propertiesDomain,
          properties);
        cfPropSource.initialize();
      } else {
        final SimplePropertySource<ConnectionFactory> sPropSource = new SimplePropertySource<>(
          cf,
          propertiesDomain,
          properties);
        sPropSource.initialize();
      }
    }
  }
}
