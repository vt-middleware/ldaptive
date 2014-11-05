/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.io.Reader;
import java.util.Properties;
import java.util.Set;
import org.ldaptive.provider.ProviderConfig;

/**
 * Reads properties specific to {@link ProviderConfig} and returns an
 * initialized object of that type.
 *
 * @author  Middleware Services
 */
public final class ProviderConfigPropertySource
  extends AbstractPropertySource<ProviderConfig<?>>
{

  /** Invoker for provider config. */
  private static final ProviderConfigPropertyInvoker INVOKER =
    new ProviderConfigPropertyInvoker(ProviderConfig.class);


  /**
   * Creates a new provider config property source using the default properties
   * file.
   *
   * @param  pc  provider config to invoke properties on
   */
  public ProviderConfigPropertySource(final ProviderConfig pc)
  {
    this(pc, PROPERTIES_FILE);
  }


  /**
   * Creates a new provider config property source.
   *
   * @param  pc  provider config to invoke properties on
   * @param  paths  to read properties from
   */
  public ProviderConfigPropertySource(
    final ProviderConfig pc,
    final String... paths)
  {
    this(pc, loadProperties(paths));
  }


  /**
   * Creates a new provider config property source.
   *
   * @param  pc  provider config to invoke properties on
   * @param  readers  to read properties from
   */
  public ProviderConfigPropertySource(
    final ProviderConfig pc,
    final Reader... readers)
  {
    this(pc, loadProperties(readers));
  }


  /**
   * Creates a new provider config property source.
   *
   * @param  pc  provider config to invoke properties on
   * @param  props  to read properties from
   */
  public ProviderConfigPropertySource(
    final ProviderConfig pc,
    final Properties props)
  {
    this(pc, PropertyDomain.LDAP, props);
  }


  /**
   * Creates a new provider config property source.
   *
   * @param  pc  provider config to invoke properties on
   * @param  domain  that properties are in
   * @param  props  to read properties from
   */
  public ProviderConfigPropertySource(
    final ProviderConfig pc,
    final PropertyDomain domain,
    final Properties props)
  {
    super(pc, domain, props);
  }


  /** {@inheritDoc} */
  @Override
  public void initialize()
  {
    initializeObject(INVOKER);
    object.setProperties(extraProps);
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
