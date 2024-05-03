/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.url;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchScope;
import org.ldaptive.dn.DefaultDnParser;
import org.ldaptive.dn.DnParser;
import org.ldaptive.filter.FilterFunction;
import org.ldaptive.filter.RegexFilterFunction;

/**
 * Parses an LDAP URL string using a regular expression.
 *
 * @author  Middleware Services
 */
public final class RegexUrlParser implements UrlParser
{

  /** Pattern to match LDAP URL. */
  private static final Pattern URL_PATTERN = Pattern.compile(
    "([lL][dD][aA][pP][sSiI]?)://(\\[[0-9A-Fa-f:]+\\]|[^:/]+)?" +
      "(?::(\\d+))?" +
      "(?:/(?:([^?]+))?" +
      "(?:\\?([^?]*))?" +
      "(?:\\?([^?]*))?" +
      "(?:\\?(.*))?)?");

  /** */
  private final DnParser dnParser = new DefaultDnParser();

  /** */
  private final FilterFunction filterFunction = new RegexFilterFunction();


  @Override
  public Url parse(final String url)
  {
    if (url == null || url.isEmpty()) {
      throw new IllegalArgumentException("LDAP URL cannot be empty or null");
    }
    final Matcher m = URL_PATTERN.matcher(url);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid LDAP URL: " + url);
    }

    // CheckStyle:MagicNumber OFF
    final String scheme = LdapUtils.toLowerCaseAscii(m.group(1));
    Url.validateScheme(scheme);
    String hostname = m.group(2);
    if (hostname != null) {
      // check for ipv6 address
      if (hostname.startsWith("[") && hostname.endsWith("]")) {
        hostname = hostname.substring(1, hostname.length() - 1).trim();
      }
      if (hostname.isEmpty()) {
        throw new IllegalArgumentException("Invalid hostname: " + hostname);
      }
    }

    final int port;
    if (m.group(3) == null) {
      port = -1;
    } else {
      port = Integer.parseInt(m.group(3));
      Url.validatePort(port, false);
    }
    final String baseDn = m.group(4) != null ? LdapUtils.percentDecode(m.group(4)) : null;
    final String[] attributes = m.group(5) != null ? !m.group(5).isEmpty() ? m.group(5).split(",") : null : null;
    if (attributes != null) {
      if (attributes.length == 0) {
        throw new IllegalArgumentException("Invalid attribute list: " + Arrays.toString(attributes));
      }
      for (int i = 0; i < attributes.length; i++) {
        attributes[i] = LdapUtils.trimSpace(attributes[i]);
        if (attributes[i].isEmpty()) {
          throw new IllegalArgumentException("Invalid attribute list: " + Arrays.toString(attributes));
        }
      }
    }
    final String scope = m.group(6);
    SearchScope searchScope = null;
    if (scope != null && !scope.isEmpty()) {
      if ("base".equalsIgnoreCase(scope)) {
        searchScope = SearchScope.OBJECT;
      } else if ("one".equalsIgnoreCase(scope)) {
        searchScope = SearchScope.ONELEVEL;
      } else if ("sub".equalsIgnoreCase(scope)) {
        searchScope = SearchScope.SUBTREE;
      } else {
        throw new IllegalArgumentException("Invalid scope: " + scope);
      }
    }

    final String filter = m.group(7) != null ?
      !m.group(7).isEmpty() ? LdapUtils.percentDecode(m.group(7)) : null : null;
    // CheckStyle:MagicNumber ON

    return new Url(scheme, hostname, port, baseDn, attributes, searchScope, filter);
  }
}
