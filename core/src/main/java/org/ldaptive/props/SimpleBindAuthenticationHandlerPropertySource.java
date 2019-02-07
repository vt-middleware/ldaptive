/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.io.Reader;
import java.util.Properties;
import java.util.Set;
import org.ldaptive.auth.SimpleBindAuthenticationHandler;

/**
 * Reads properties specific to {@link SimpleBindAuthenticationHandler} and returns an initialized object of that type.
 *
 * @author  Middleware Services
 */
public final class SimpleBindAuthenticationHandlerPropertySource
  extends AbstractConnectionFactoryManagerPropertySource<SimpleBindAuthenticationHandler>
{

  /** Invoker for simple bind authentication handler. */
  private static final SimpleBindAuthenticationHandlerPropertyInvoker INVOKER =
    new SimpleBindAuthenticationHandlerPropertyInvoker(SimpleBindAuthenticationHandler.class);


  /**
   * Creates a new simple bind authentication handler property source using the default properties file.
   *
   * @param  handler  simple bind authentication handler to invoke properties on
   */
  public SimpleBindAuthenticationHandlerPropertySource(final SimpleBindAuthenticationHandler handler)
  {
    this(handler, PROPERTIES_FILE);
  }


  /**
   * Creates a new simple bind authentication handler property source.
   *
   * @param  handler  simple bind authentication handler to invoke properties on
   * @param  paths  to read properties from
   */
  public SimpleBindAuthenticationHandlerPropertySource(
    final SimpleBindAuthenticationHandler handler,
    final String... paths)
  {
    this(handler, loadProperties(paths));
  }


  /**
   * Creates a new simple bind authentication handler property source.
   *
   * @param  handler  simple bind authentication handler to invoke properties on
   * @param  readers  to read properties from
   */
  public SimpleBindAuthenticationHandlerPropertySource(
    final SimpleBindAuthenticationHandler handler,
    final Reader... readers)
  {
    this(handler, loadProperties(readers));
  }


  /**
   * Creates a new simple bind authentication handler property source.
   *
   * @param  handler  simple bind authentication handler to invoke properties on
   * @param  props  to read properties from
   */
  public SimpleBindAuthenticationHandlerPropertySource(
    final SimpleBindAuthenticationHandler handler,
    final Properties props)
  {
    this(handler, PropertyDomain.AUTH, props);
  }


  /**
   * Creates a new simple bind authentication handler property source.
   *
   * @param  handler  simple bind authentication handler to invoke properties on
   * @param  domain  that properties are in
   * @param  props  to read properties from
   */
  public SimpleBindAuthenticationHandlerPropertySource(
    final SimpleBindAuthenticationHandler handler,
    final PropertyDomain domain,
    final Properties props)
  {
    super(handler, domain, props);
  }


  @Override
  public void initialize()
  {
    initializeObject(INVOKER);
    super.initialize();
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
