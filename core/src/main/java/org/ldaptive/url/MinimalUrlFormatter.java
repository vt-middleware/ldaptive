/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.url;

import org.ldaptive.dn.Dn;
import org.ldaptive.filter.Filter;

/**
 * Formats an LDAP URL using the exact properties contained in the LDAP URL.
 *
 * @author  Middleware Services
 */
public class MinimalUrlFormatter extends AbstractUrlFormatter
{


  @Override
  protected String formatScheme(final String scheme)
  {
    return scheme;
  }


  @Override
  protected String formatHostname(final String hostname)
  {
    return hostname;
  }


  @Override
  protected String formatBaseDn(final String baseDn, final Dn parsedBaseDn)
  {
    return baseDn;
  }


  @Override
  protected String formatAttribute(final String attrName)
  {
    return attrName;
  }


  @Override
  protected String formatFilter(final String filter, final Filter parsedFilter)
  {
    return filter;
  }
}
