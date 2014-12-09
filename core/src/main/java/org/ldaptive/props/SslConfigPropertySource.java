/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.io.Reader;
import java.util.Properties;
import java.util.Set;
import org.ldaptive.ssl.SslConfig;

/**
 * Reads properties specific to {@link SslConfig} and returns an initialized
 * object of that type.
 *
 * @author  Middleware Services
 */
public final class SslConfigPropertySource
  extends AbstractPropertySource<SslConfig>
{

  /** Invoker for ssl config. */
  private static final SslConfigPropertyInvoker INVOKER =
    new SslConfigPropertyInvoker(SslConfig.class);


  /**
   * Creates a new ssl config property source using the default properties file.
   *
   * @param  config  ssl config to invoke properties on
   */
  public SslConfigPropertySource(final SslConfig config)
  {
    this(config, PROPERTIES_FILE);
  }


  /**
   * Creates a new ssl config property source.
   *
   * @param  config  ssl config to invoke properties on
   * @param  paths  to read properties from
   */
  public SslConfigPropertySource(final SslConfig config, final String... paths)
  {
    this(config, loadProperties(paths));
  }


  /**
   * Creates a new ssl config property source.
   *
   * @param  config  ssl config to invoke properties on
   * @param  readers  to read properties from
   */
  public SslConfigPropertySource(
    final SslConfig config,
    final Reader... readers)
  {
    this(config, loadProperties(readers));
  }


  /**
   * Creates a new ssl config property source.
   *
   * @param  config  ssl config to invoke properties on
   * @param  props  to read properties from
   */
  public SslConfigPropertySource(final SslConfig config, final Properties props)
  {
    this(config, PropertyDomain.LDAP, props);
  }


  /**
   * Creates a new sssl config property source.
   *
   * @param  config  ssl config to invoke properties on
   * @param  domain  that properties are in
   * @param  props  to read properties from
   */
  public SslConfigPropertySource(
    final SslConfig config,
    final PropertyDomain domain,
    final Properties props)
  {
    super(config, domain, props);
  }


  @Override
  public void initialize()
  {
    initializeObject(INVOKER);
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
