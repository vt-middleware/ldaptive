/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.io.Reader;
import java.util.Properties;
import java.util.Set;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.PoolConfig;

/**
 * Reads properties specific to {@link BlockingConnectionPool} and returns an initialized object of that type.
 *
 * @author  Middleware Services
 */
public final class BlockingConnectionPoolPropertySource extends AbstractPropertySource<BlockingConnectionPool>
{

  /** Invoker for connection factory. */
  private static final BlockingConnectionPoolPropertyInvoker INVOKER = new BlockingConnectionPoolPropertyInvoker(
    BlockingConnectionPool.class);


  /**
   * Creates a new blocking connection pool property source using the default properties file.
   *
   * @param  cp  connection pool to invoke properties on
   */
  public BlockingConnectionPoolPropertySource(final BlockingConnectionPool cp)
  {
    this(cp, PROPERTIES_FILE);
  }


  /**
   * Creates a new blocking connection pool property source.
   *
   * @param  cp  connection pool to invoke properties on
   * @param  paths  to read properties from
   */
  public BlockingConnectionPoolPropertySource(final BlockingConnectionPool cp, final String... paths)
  {
    this(cp, loadProperties(paths));
  }


  /**
   * Creates a new blocking connection pool property source.
   *
   * @param  cp  connection pool to invoke properties on
   * @param  readers  to read properties from
   */
  public BlockingConnectionPoolPropertySource(final BlockingConnectionPool cp, final Reader... readers)
  {
    this(cp, loadProperties(readers));
  }


  /**
   * Creates a new blocking connection pool property source.
   *
   * @param  cp  connection pool to invoke properties on
   * @param  props  to read properties from
   */
  public BlockingConnectionPoolPropertySource(final BlockingConnectionPool cp, final Properties props)
  {
    this(cp, PropertyDomain.POOL, props);
  }


  /**
   * Creates a new blocking connection pool property source.
   *
   * @param  cp  connection pool to invoke properties on
   * @param  domain  that properties are in
   * @param  props  to read properties from
   */
  public BlockingConnectionPoolPropertySource(
    final BlockingConnectionPool cp,
    final PropertyDomain domain,
    final Properties props)
  {
    super(cp, domain, props);
  }


  @Override
  public void initialize()
  {
    initializeObject(INVOKER);

    DefaultConnectionFactory cf = object.getConnectionFactory();
    if (cf == null) {
      cf = new DefaultConnectionFactory();

      final DefaultConnectionFactoryPropertySource cfPropSource = new DefaultConnectionFactoryPropertySource(
        cf,
        propertiesDomain,
        properties);
      cfPropSource.initialize();
      object.setConnectionFactory(cf);
    }

    PoolConfig pc = object.getPoolConfig();
    if (pc == null) {
      pc = new PoolConfig();

      final PoolConfigPropertySource pcPropSource = new PoolConfigPropertySource(pc, propertiesDomain, properties);
      pcPropSource.initialize();
      object.setPoolConfig(pc);
    } else {
      final SimplePropertySource<PoolConfig> sPropSource = new SimplePropertySource<>(pc, propertiesDomain, properties);
      sPropSource.initialize();
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
