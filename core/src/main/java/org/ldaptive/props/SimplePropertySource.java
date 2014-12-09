/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.io.Reader;
import java.util.Properties;

/**
 * Reads simple properties and returns an initialized object of the supplied
 * type.
 *
 * @param  <T>  type of object to invoke properties on
 *
 * @author  Middleware Services
 */
public final class SimplePropertySource<T> extends AbstractPropertySource<T>
{

  /** Invoker for simple properties. */
  private final SimplePropertyInvoker invoker;


  /**
   * Creates a new simple property source using the default properties file.
   *
   * @param  t  object to invoke properties on
   */
  public SimplePropertySource(final T t)
  {
    this(t, PROPERTIES_FILE);
  }


  /**
   * Creates a new simple property source.
   *
   * @param  t  object to invoke properties on
   * @param  paths  to read properties from
   */
  public SimplePropertySource(final T t, final String... paths)
  {
    this(t, loadProperties(paths));
  }


  /**
   * Creates a new simple property source.
   *
   * @param  t  object to invoke properties on
   * @param  readers  to read properties from
   */
  public SimplePropertySource(final T t, final Reader... readers)
  {
    this(t, loadProperties(readers));
  }


  /**
   * Creates a new simple property source.
   *
   * @param  t  object to invoke properties on
   * @param  props  to read properties from
   */
  public SimplePropertySource(final T t, final Properties props)
  {
    this(t, PropertyDomain.LDAP, props);
  }


  /**
   * Creates a new simple property source.
   *
   * @param  t  object to invoke properties on
   * @param  domain  that properties are in
   * @param  props  to read properties from
   */
  public SimplePropertySource(
    final T t,
    final PropertyDomain domain,
    final Properties props)
  {
    super(t, domain, props);
    invoker = new SimplePropertyInvoker(t.getClass());
  }


  @Override
  public void initialize()
  {
    initializeObject(invoker);
  }
}
