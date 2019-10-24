/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.io.Reader;
import java.util.Properties;
import java.util.Set;
import org.ldaptive.SearchConnectionValidator;

/**
 * Reads properties specific to {@link SearchConnectionValidator} and returns an initialized object of that type.
 *
 * @author  Middleware Services
 */
public final class SearchConnectionValidatorPropertySource extends AbstractPropertySource<SearchConnectionValidator>
{

  /** Invoker for search connection validator. */
  private static final SimplePropertyInvoker INVOKER = new SimplePropertyInvoker(SearchConnectionValidator.class);


  /**
   * Creates a new search connection validator property source using the default properties file.
   *
   * @param  cv  connection validator to invoke properties on
   */
  public SearchConnectionValidatorPropertySource(final SearchConnectionValidator cv)
  {
    this(cv, PROPERTIES_FILE);
  }


  /**
   * Creates a new search connection validator property source.
   *
   * @param  cv  connection validator to invoke properties on
   * @param  paths  to read properties from
   */
  public SearchConnectionValidatorPropertySource(final SearchConnectionValidator cv, final String... paths)
  {
    this(cv, loadProperties(paths));
  }


  /**
   * Creates a new search connection validator property source.
   *
   * @param  cv  connection validator to invoke properties on
   * @param  readers  to read properties from
   */
  public SearchConnectionValidatorPropertySource(final SearchConnectionValidator cv, final Reader... readers)
  {
    this(cv, loadProperties(readers));
  }


  /**
   * Creates a new search connection validator property source.
   *
   * @param  cv  connection validator to invoke properties on
   * @param  props  to read properties from
   */
  public SearchConnectionValidatorPropertySource(final SearchConnectionValidator cv, final Properties props)
  {
    this(cv, PropertyDomain.POOL, props);
  }


  /**
   * Creates a new search connection validator property source.
   *
   * @param  cv  connection validator to invoke properties on
   * @param  domain  that properties are in
   * @param  props  to read properties from
   */
  public SearchConnectionValidatorPropertySource(
    final SearchConnectionValidator cv,
    final PropertyDomain domain,
    final Properties props)
  {
    super(cv, domain, props);
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
