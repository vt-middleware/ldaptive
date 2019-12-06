/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.io.Reader;
import java.util.Properties;
import java.util.Set;
import org.ldaptive.auth.SearchEntryResolver;

/**
 * Reads properties specific to {@link SearchEntryResolver} and returns an initialized object of that type.
 *
 * @author  Middleware Services
 */
public final class SearchEntryResolverPropertySource
  extends AbstractConnectionFactoryManagerPropertySource<SearchEntryResolver>
{

  /** Invoker for search entry resolver. */
  private static final SearchOperationFactoryPropertyInvoker INVOKER =
    new SearchOperationFactoryPropertyInvoker(SearchEntryResolver.class);


  /**
   * Creates a new search entry resolver property source using the default properties file.
   *
   * @param  resolver  search entry resolver to invoke properties on
   */
  public SearchEntryResolverPropertySource(final SearchEntryResolver resolver)
  {
    this(resolver, PROPERTIES_FILE);
  }


  /**
   * Creates a new search entry resolver property source.
   *
   * @param  resolver  search entry resolver to invoke properties on
   * @param  paths  to read properties from
   */
  public SearchEntryResolverPropertySource(final SearchEntryResolver resolver, final String... paths)
  {
    this(resolver, loadProperties(paths));
  }


  /**
   * Creates a new search entry resolver property source.
   *
   * @param  resolver  search entry resolver to invoke properties on
   * @param  readers  to read properties from
   */
  public SearchEntryResolverPropertySource(final SearchEntryResolver resolver, final Reader... readers)
  {
    this(resolver, loadProperties(readers));
  }


  /**
   * Creates a new search entry resolver property source.
   *
   * @param  resolver  search entry resolver to invoke properties on
   * @param  props  to read properties from
   */
  public SearchEntryResolverPropertySource(final SearchEntryResolver resolver, final Properties props)
  {
    this(resolver, PropertyDomain.AUTH, props);
  }


  /**
   * Creates a new search entry resolver property source.
   *
   * @param  resolver  search entry resolver to invoke properties on
   * @param  domain  that properties are in
   * @param  props  to read properties from
   */
  public SearchEntryResolverPropertySource(
    final SearchEntryResolver resolver,
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
