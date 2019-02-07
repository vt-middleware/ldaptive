/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.io.Reader;
import java.util.Properties;
import java.util.Set;
import org.ldaptive.auth.CompareAuthenticationHandler;

/**
 * Reads properties specific to {@link CompareAuthenticationHandler} and returns an initialized object of that type.
 *
 * @author  Middleware Services
 */
public final class CompareAuthenticationHandlerPropertySource
  extends AbstractConnectionFactoryManagerPropertySource<CompareAuthenticationHandler>
{

  /** Invoker for compare authentication handler. */
  private static final CompareAuthenticationHandlerPropertyInvoker INVOKER =
    new CompareAuthenticationHandlerPropertyInvoker(CompareAuthenticationHandler.class);


  /**
   * Creates a new compare authentication handler property source using the default properties file.
   *
   * @param  handler  compare authentication handler to invoke properties on
   */
  public CompareAuthenticationHandlerPropertySource(final CompareAuthenticationHandler handler)
  {
    this(handler, PROPERTIES_FILE);
  }


  /**
   * Creates a new compare authentication handler property source.
   *
   * @param  handler  compare authentication handler to invoke properties on
   * @param  paths  to read properties from
   */
  public CompareAuthenticationHandlerPropertySource(final CompareAuthenticationHandler handler, final String... paths)
  {
    this(handler, loadProperties(paths));
  }


  /**
   * Creates a new compare authentication handler property source.
   *
   * @param  handler  compare authentication handler to invoke properties on
   * @param  readers  to read properties from
   */
  public CompareAuthenticationHandlerPropertySource(final CompareAuthenticationHandler handler, final Reader... readers)
  {
    this(handler, loadProperties(readers));
  }


  /**
   * Creates a new compare authentication handler property source.
   *
   * @param  handler  compare authentication handler to invoke properties on
   * @param  props  to read properties from
   */
  public CompareAuthenticationHandlerPropertySource(final CompareAuthenticationHandler handler, final Properties props)
  {
    this(handler, PropertyDomain.AUTH, props);
  }


  /**
   * Creates a new compare authentication handler property source.
   *
   * @param  handler  compare authentication handler to invoke properties on
   * @param  domain  that properties are in
   * @param  props  to read properties from
   */
  public CompareAuthenticationHandlerPropertySource(
    final CompareAuthenticationHandler handler,
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
