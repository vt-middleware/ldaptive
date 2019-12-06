/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.io.Reader;
import java.util.Properties;
import java.util.Set;
import org.ldaptive.jaas.SearchRoleResolver;

/**
 * Reads properties specific to {@link SearchRoleResolver} and returns an initialized object of that type.
 *
 * @author  Middleware Services
 */
public final class SearchRoleResolverPropertySource
  extends AbstractConnectionFactoryManagerPropertySource<SearchRoleResolver>
{

  /** Invoker for search role resolver. */
  private static final SearchOperationFactoryPropertyInvoker INVOKER =
    new SearchOperationFactoryPropertyInvoker(SearchRoleResolver.class);


  /**
   * Creates a new search role resolver property source using the default properties file.
   *
   * @param  resolver  search role resolver to invoke properties on
   */
  public SearchRoleResolverPropertySource(final SearchRoleResolver resolver)
  {
    this(resolver, PROPERTIES_FILE);
  }


  /**
   * Creates a new search role resolver property source.
   *
   * @param  resolver  search role resolver to invoke properties on
   * @param  paths  to read properties from
   */
  public SearchRoleResolverPropertySource(final SearchRoleResolver resolver, final String... paths)
  {
    this(resolver, loadProperties(paths));
  }


  /**
   * Creates a new search role resolver property source.
   *
   * @param  resolver  search role resolver to invoke properties on
   * @param  readers  to read properties from
   */
  public SearchRoleResolverPropertySource(final SearchRoleResolver resolver, final Reader... readers)
  {
    this(resolver, loadProperties(readers));
  }


  /**
   * Creates a new search role resolver property source.
   *
   * @param  resolver  search role resolver to invoke properties on
   * @param  props  to read properties from
   */
  public SearchRoleResolverPropertySource(final SearchRoleResolver resolver, final Properties props)
  {
    this(resolver, PropertyDomain.AUTH, props);
  }


  /**
   * Creates a new search role resolver property source.
   *
   * @param  resolver  search role resolver to invoke properties on
   * @param  domain  that properties are in
   * @param  props  to read properties from
   */
  public SearchRoleResolverPropertySource(
    final SearchRoleResolver resolver,
    final PropertyDomain domain,
    final Properties props)
  {
    super(resolver, domain, props);
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
