/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.url;

import java.util.ArrayList;
import java.util.List;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchScope;
import org.ldaptive.dn.DefaultDnParser;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.DnParser;
import org.ldaptive.filter.DefaultFilterFunction;
import org.ldaptive.filter.Filter;
import org.ldaptive.filter.FilterFunction;

/**
 * Parses an LDAP URL string.
 *
 * @author  Middleware Services
 */
public final class DefaultUrlParser implements UrlParser
{

  /** DN parser. */
  private final DnParser dnParser = new DefaultDnParser();

  /** Filter function. */
  private final FilterFunction filterFunction = new DefaultFilterFunction();


  @Override
  public Url parse(final String url)
  {
    if (url == null || url.isEmpty()) {
      throw new IllegalArgumentException("LDAP URL cannot be empty or null");
    }

    int currentPos = url.indexOf("://");
    if (currentPos < 0) {
      throw new IllegalArgumentException("LDAP URL must designate a scheme using '://'");
    }
    final String scheme = LdapUtils.toLowerCaseAscii(url.substring(0, currentPos));
    Url.validateScheme(scheme);

    // CheckStyle:MagicNumber OFF
    int nextPos = url.indexOf('/', currentPos + 3);
    final Object[] hostAndPort = nextPos < 0 ?
      parseHostAndPort(url.substring(currentPos + 3)) :
      parseHostAndPort(url.substring(currentPos + 3, nextPos));
    final String hostname = (String) hostAndPort[0];
    final int port = (int) hostAndPort[1];
    // CheckStyle:MagicNumber ON

    String baseDn = null;
    Dn parsedBaseDn = null;
    if (nextPos > -1) {
      currentPos = nextPos;
      nextPos = url.indexOf('?', currentPos);
      try {
        baseDn = nextPos < 0 ? LdapUtils.percentDecode(url.substring(currentPos + 1)) :
          LdapUtils.percentDecode(url.substring(currentPos + 1, nextPos));
        if (baseDn.isEmpty()) {
          baseDn = null;
        } else {
          parsedBaseDn = new Dn(baseDn, dnParser);
        }
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid baseDN: " + url, e);
      }
    }

    String[] attrs = null;
    if (nextPos > -1) {
      currentPos = nextPos;
      nextPos = url.indexOf('?', currentPos + 1);
      attrs = nextPos < 0 ? parseAttributes(url.substring(currentPos + 1)) :
        parseAttributes(url.substring(currentPos + 1, nextPos));
    }

    SearchScope scope = null;
    if (nextPos > -1) {
      currentPos = nextPos;
      nextPos = url.indexOf('?', currentPos + 1);
      scope = nextPos < 0 ? parseScope(url.substring(currentPos + 1)) :
        parseScope(url.substring(currentPos + 1, nextPos));
    }

    String filter = null;
    Filter parsedFilter = null;
    if (nextPos > -1) {
      try {
        filter = LdapUtils.percentDecode(url.substring(nextPos + 1));
        if (filter.isEmpty()) {
          filter = null;
        } else {
          parsedFilter = filterFunction.parse(filter);
        }
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid filter: " + url, e);
      }
    }

    return new Url(scheme, hostname, port, baseDn, parsedBaseDn, attrs, scope, filter, parsedFilter);
  }


  /**
   * Parses the supplied string containing a colon delimited hostname and port.
   *
   * @param  hostAndPort  to parse
   *
   * @return  array containing a string hostname in the first index and an integer port in the second index
   */
  private static Object[] parseHostAndPort(final String hostAndPort)
  {
    if (hostAndPort == null) {
      throw new IllegalArgumentException("Host and port cannot be empty or null");
    }
    if (hostAndPort.isEmpty()) {
      return new Object[] {null, -1};
    }

    // check for IPv6 address
    if (hostAndPort.indexOf('[') == 0) {
      final int bracketPos = hostAndPort.indexOf(']');
      if (bracketPos < 0) {
        throw new IllegalArgumentException("IPv6 address is missing closing bracket: " + hostAndPort);
      }
      final String ipv6Address = LdapUtils.trimSpace(hostAndPort.substring(1, bracketPos));
      if (ipv6Address.isEmpty()) {
        throw new IllegalArgumentException("IPv6 address is empty: " + hostAndPort);
      }
      final int ipv6Port;
      if (bracketPos == hostAndPort.length() - 1) {
        ipv6Port = -1;
      } else if (hostAndPort.charAt(bracketPos + 1) != ':') {
        throw new IllegalArgumentException("IPv6 address invalid port designation: " + hostAndPort);
      } else {
        ipv6Port = Integer.parseInt(hostAndPort.substring(bracketPos + 2));
        Url.validatePort(ipv6Port, false);
      }
      return new Object[] {ipv6Address, ipv6Port};
    }

    final int colonPos = hostAndPort.indexOf(':');
    final String hostname;
    final int port;
    if (colonPos < 0) {
      hostname = hostAndPort;
      port = -1;
    } else {
      hostname = hostAndPort.substring(0, colonPos);
      port = Integer.parseInt(hostAndPort.substring(colonPos + 1));
      Url.validatePort(port, false);
    }
    return new Object[] {hostname.isEmpty() ? null : hostname, port};
  }


  /**
   * Parses the supplied comma delimited attributes.
   *
   * @param  attrs  to parse
   *
   * @return  array of attributes
   */
  private static String[] parseAttributes(final String attrs)
  {
    if (attrs == null || attrs.isEmpty()) {
      return null;
    }

    final List<String> parsedAttrs = new ArrayList<>();
    int pos = 0;
    int commaPos = 0;
    while (commaPos > -1) {
      commaPos = attrs.indexOf(',', pos);
      final String attrName = commaPos < 0 ? LdapUtils.trimSpace(attrs.substring(pos)) :
        LdapUtils.trimSpace(attrs.substring(pos, commaPos));
      if (commaPos < 0) {
        if (attrName.isEmpty()) {
          if (!parsedAttrs.isEmpty()) {
            throw new IllegalArgumentException("Invalid attribute list: " + attrs);
          }
        } else {
          parsedAttrs.add(attrName);
        }
      } else {
        if (attrName.isEmpty()) {
          throw new IllegalArgumentException("Invalid attribute list: " + attrs);
        } else {
          parsedAttrs.add(attrName);
          pos = commaPos + 1;
          if (pos >= attrs.length()) {
            throw new IllegalArgumentException("Invalid attribute list: " + attrs);
          }
        }
      }
    }
    return parsedAttrs.isEmpty() ? null : parsedAttrs.toArray(new String[0]);
  }


  /**
   * Parses the supplied scope in string form.
   *
   * @param  scope  to parse
   *
   * @return  search scope
   */
  private static SearchScope parseScope(final String scope)
  {
    final SearchScope ss;
    switch (LdapUtils.toLowerCaseAscii(scope)) {
    case "one":
      ss = SearchScope.ONELEVEL;
      break;
    case "sub":
      ss = SearchScope.SUBTREE;
      break;
    case "subordinates":
      ss = SearchScope.SUBORDINATE;
      break;
    case "base":
    case "":
      ss = SearchScope.OBJECT;
      break;
    default:
      throw new IllegalArgumentException("Unknown search scope: " + scope);
    }
    return ss;
  }
}
