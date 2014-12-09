/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.io.Reader;
import java.util.Properties;
import java.util.Set;
import org.ldaptive.auth.SearchDnResolver;

/**
 * Reads properties specific to {@link SearchDnResolver} and returns an
 * initialized object of that type.
 *
 * @author  Middleware Services
 */
public final class SearchDnResolverPropertySource
  extends AbstractPropertySource<SearchDnResolver>
{

  /** Invoker for search dn resolver. */
  private static final SimplePropertyInvoker INVOKER =
    new SimplePropertyInvoker(SearchDnResolver.class);


  /**
   * Creates a new search dn resolver property source using the default
   * properties file.
   *
   * @param  resolver  search dn resolver to invoke properties on
   */
  public SearchDnResolverPropertySource(final SearchDnResolver resolver)
  {
    this(resolver, PROPERTIES_FILE);
  }


  /**
   * Creates a new search dn resolver property source.
   *
   * @param  resolver  search dn resolver to invoke properties on
   * @param  paths  to read properties from
   */
  public SearchDnResolverPropertySource(
    final SearchDnResolver resolver,
    final String... paths)
  {
    this(resolver, loadProperties(paths));
  }


  /**
   * Creates a new search dn resolver property source.
   *
   * @param  resolver  search dn resolver to invoke properties on
   * @param  readers  to read properties from
   */
  public SearchDnResolverPropertySource(
    final SearchDnResolver resolver,
    final Reader... readers)
  {
    this(resolver, loadProperties(readers));
  }


  /**
   * Creates a new search dn resolver property source.
   *
   * @param  resolver  search dn resolver to invoke properties on
   * @param  props  to read properties from
   */
  public SearchDnResolverPropertySource(
    final SearchDnResolver resolver,
    final Properties props)
  {
    this(resolver, PropertyDomain.AUTH, props);
  }


  /**
   * Creates a new search dn resolver property source.
   *
   * @param  resolver  search dn resolver to invoke properties on
   * @param  domain  that properties are in
   * @param  props  to read properties from
   */
  public SearchDnResolverPropertySource(
    final SearchDnResolver resolver,
    final PropertyDomain domain,
    final Properties props)
  {
    super(resolver, domain, props);
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
