/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.io.Reader;
import java.util.Properties;
import java.util.Set;
import org.ldaptive.auth.BindAuthenticationHandler;

/**
 * Reads properties specific to {@link BindAuthenticationHandler} and returns an initialized object of that type.
 *
 * @author  Middleware Services
 */
public final class BindAuthenticationHandlerPropertySource extends AbstractPropertySource<BindAuthenticationHandler>
{

  /** Invoker for bind authentication handler. */
  private static final BindAuthenticationHandlerPropertyInvoker INVOKER = new BindAuthenticationHandlerPropertyInvoker(
    BindAuthenticationHandler.class);


  /**
   * Creates a new bind authentication handler property source using the default properties file.
   *
   * @param  handler  bind authentication handler to invoke properties on
   */
  public BindAuthenticationHandlerPropertySource(final BindAuthenticationHandler handler)
  {
    this(handler, PROPERTIES_FILE);
  }


  /**
   * Creates a new bind authentication handler property source.
   *
   * @param  handler  bind authentication handler to invoke properties on
   * @param  paths  to read properties from
   */
  public BindAuthenticationHandlerPropertySource(final BindAuthenticationHandler handler, final String... paths)
  {
    this(handler, loadProperties(paths));
  }


  /**
   * Creates a new bind authentication handler property source.
   *
   * @param  handler  bind authentication handler to invoke properties on
   * @param  readers  to read properties from
   */
  public BindAuthenticationHandlerPropertySource(final BindAuthenticationHandler handler, final Reader... readers)
  {
    this(handler, loadProperties(readers));
  }


  /**
   * Creates a new bind authentication handler property source.
   *
   * @param  handler  bind authentication handler to invoke properties on
   * @param  props  to read properties from
   */
  public BindAuthenticationHandlerPropertySource(final BindAuthenticationHandler handler, final Properties props)
  {
    this(handler, PropertyDomain.AUTH, props);
  }


  /**
   * Creates a new bind authentication handler property source.
   *
   * @param  handler  bind authentication handler to invoke properties on
   * @param  domain  that properties are in
   * @param  props  to read properties from
   */
  public BindAuthenticationHandlerPropertySource(
    final BindAuthenticationHandler handler,
    final PropertyDomain domain,
    final Properties props)
  {
    super(handler, domain, props);
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
