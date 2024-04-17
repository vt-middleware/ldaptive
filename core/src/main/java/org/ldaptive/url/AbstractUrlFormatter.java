/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.url;

import org.ldaptive.LdapUtils;
import org.ldaptive.SearchScope;
import org.ldaptive.dn.Dn;
import org.ldaptive.filter.Filter;

/**
 * Base implementation for LDAP URL formatters.
 *
 * @author  Middleware Services
 */
public abstract class AbstractUrlFormatter implements UrlFormatter
{


  @Override
  public String format(final Url url)
  {
    final StringBuilder sb = new StringBuilder(formatScheme(url.getScheme())).append("://");
    final String hostname = formatHostname(url.getHostname());
    if (hostname != null) {
      // ipv6 address
      if (hostname.contains(":")) {
        sb.append('[').append(hostname).append(']');
      } else {
        sb.append(hostname);
      }
    }
    if (!"ldapi".equals(url.getScheme())) {
      sb.append(':').append(url.getPort());
    }
    sb.append('/').append(LdapUtils.percentEncode(formatBaseDn(url.getBaseDn(), url.getParsedBaseDn())));
    sb.append('?');

    final String[] attrs = url.getAttributes();
    for (int i = 0; i < attrs.length; i++) {
      sb.append(formatAttribute(attrs[i]));
      if (i + 1 < attrs.length) {
        sb.append(',');
      }
    }
    sb.append('?');

    final SearchScope scope = url.getScope();
    if (SearchScope.OBJECT == scope) {
      sb.append("base");
    } else if (SearchScope.ONELEVEL == scope) {
      sb.append("one");
    } else if (SearchScope.SUBTREE == scope) {
      sb.append("sub");
    } else if (SearchScope.SUBORDINATE == scope) {
      sb.append("subordinates");
    }
    sb.append('?').append(LdapUtils.percentEncode(formatFilter(url.getFilter(), url.getParsedFilter())));
    return sb.toString();
  }


  /**
   * Formats the supplied scheme.
   *
   * @param  scheme  to format
   *
   * @return  formatted scheme
   */
  protected abstract String formatScheme(String scheme);


  /**
   * Formats the supplied hostname.
   *
   * @param  hostname  to format
   *
   * @return  formatted hostname
   */
  protected abstract String formatHostname(String hostname);


  /**
   * Formats the supplied baseDN.
   *
   * @param  baseDn  to format
   * @param  parsedBaseDn  to supplement formatting options
   *
   * @return  formatted baseDN
   */
  protected abstract String formatBaseDn(String baseDn, Dn parsedBaseDn);


  /**
   * Formats the supplied attribute name.
   *
   * @param  attrName  to format
   *
   * @return  formatted attribute name
   */
  protected abstract String formatAttribute(String attrName);


  /**
   * Formats the supplied filter string.
   *
   * @param  filter  to format
   * @param  parsedFilter  to supplement formatting options
   *
   * @return  formatted filter string
   */
  protected abstract String formatFilter(String filter, Filter parsedFilter);
}
