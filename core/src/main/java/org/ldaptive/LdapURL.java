/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing LDAP URLs. See RFC 4516. Supports a space delimited format for representing multiple URLs.
 * Expects URLs of the form scheme://hostname:port/baseDn?attrs?scope?filter. This implementation does not support URL
 * extensions.
 *
 * @author  Middleware Services
 */
public class LdapURL
{

  /** Default delimiter for ldap urls. */
  private static final String DEFAULT_DELIMITER = " ";

  /** Pattern to match LDAP URLs. */
  private static final Pattern URL_PATTERN = Pattern.compile(
    "([lL][dD][aA][pP][sSiI]?)://([^:/]+)?" +
      "(?::(\\d+))?" +
      "(?:/(?:([^?]+))?" +
      "(?:\\?([^?]*))?" +
      "(?:\\?([^?]*))?" +
      "(?:\\?(.*))?)?");

  /** URL entries. */
  private final List<Entry> ldapEntries = new ArrayList<>();


  /**
   * Creates a new ldap url.
   *
   * @param  url  space delimited list of ldap urls
   */
  public LdapURL(final String url)
  {
    this(url, DEFAULT_DELIMITER);
  }


  /**
   * Creates a new ldap url.
   *
   * @param  url  space delimited list of ldap urls
   * @param  delimiter  to split url with
   */
  public LdapURL(final String url, final String delimiter)
  {
    final String[] urls = url.split(delimiter);

    for (String s : urls) {
      ldapEntries.add(parseEntry(s));
    }
  }


  /**
   * Matches the supplied url against a pattern and reads it's components.
   *
   * @param  url  to parse
   *
   * @return  entry
   */
  protected Entry parseEntry(final String url)
  {
    final Matcher m = URL_PATTERN.matcher(url);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid LDAP URL: " + url);
    }

    // CheckStyle:MagicNumber OFF
    final String scheme = m.group(1).toLowerCase();
    String hostname = m.group(2);
    if (hostname != null) {
      // check for ipv6 address
      if (hostname.startsWith("[") && hostname.endsWith("]")) {
        hostname = hostname.substring(1, hostname.length() - 1).trim();
      }
    }

    final int port = m.group(3) != null ? Integer.parseInt(m.group(3)) : -1;
    final String baseDn = m.group(4) != null ? LdapUtils.percentDecode(m.group(4)) : null;
    final String[] attributes = m.group(5) != null ? m.group(5).length() > 0 ? m.group(5).split(",") : null : null;
    final String scope = m.group(6);
    SearchScope searchScope = null;
    if (scope != null && scope.length() > 0) {
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

    final SearchFilter filter = m.group(7) != null
      ? m.group(7).length() > 0 ? new SearchFilter(LdapUtils.percentDecode(m.group(7))) : null : null;

    return new Entry(scheme, hostname, port, baseDn, attributes, searchScope, filter);
    // CheckStyle:MagicNumber ON
  }


  /**
   * Returns the first entry of this ldap url.
   *
   * @return  first entry
   */
  public Entry getEntry()
  {
    return ldapEntries.get(0);
  }


  /**
   * Returns the last entry of this ldap url.
   *
   * @return  last entry
   */
  public Entry getLastEntry()
  {
    return ldapEntries.get(ldapEntries.size() - 1);
  }


  /**
   * Returns a list of all the ldap url entries in this ldap url.
   *
   * @return  ldap url entries
   */
  public List<Entry> getEntries()
  {
    return Collections.unmodifiableList(ldapEntries);
  }


  /**
   * Returns a list of all the URLs in this ldap url.
   *
   * @return  ldap urls
   */
  public String[] getUrls()
  {
    final String[] entries = new String[ldapEntries.size()];
    for (int i = 0; i < ldapEntries.size(); i++) {
      entries[i] = ldapEntries.get(i).getUrl();
    }
    return entries;
  }


  /**
   * Returns a list of all the hostnames including their scheme and port in this ldap url.
   *
   * @return  ldap url hostnames with scheme and port
   */
  public String[] getHostnamesWithSchemeAndPort()
  {
    final String[] entries = new String[ldapEntries.size()];
    for (int i = 0; i < ldapEntries.size(); i++) {
      entries[i] = ldapEntries.get(i).getHostnameWithSchemeAndPort();
    }
    return entries;
  }


  /**
   * Returns a list of all the hostnames in this ldap url.
   *
   * @return  ldap url hostnames
   */
  public String[] getHostnames()
  {
    final String[] entries = new String[ldapEntries.size()];
    for (int i = 0; i < ldapEntries.size(); i++) {
      entries[i] = ldapEntries.get(i).getHostname();
    }
    return entries;
  }


  /**
   * Returns the number of entries in this ldap url.
   *
   * @return  number of entries in this ldap url
   */
  public int size()
  {
    return ldapEntries.size();
  }


  @Override
  public String toString()
  {
    return String.format("[%s@%d::ldapEntries=%s]", getClass().getName(), hashCode(), ldapEntries);
  }


  /** Represents a single LDAP URL entry. */
  public static class Entry
  {

    /** Default LDAP port, value is {@value}. */
    protected static final int DEFAULT_LDAP_PORT = 389;

    /** Default LDAPS port, value is {@value}. */
    protected static final int DEFAULT_LDAPS_PORT = 636;

    /** Default base DN, value is {@value}. */
    protected static final String DEFAULT_BASE_DN = "";

    /** Default search filter value is '(objectClass=*)'. */
    protected static final SearchFilter DEFAULT_FILTER = new SearchFilter("(objectClass=*)");

    /** Default scope, value is {@link SearchScope#OBJECT}. */
    protected static final SearchScope DEFAULT_SCOPE = SearchScope.OBJECT;

    /** Default return attributes, value is none. */
    protected static final String[] DEFAULT_ATTRIBUTES = ReturnAttributes.DEFAULT.value();

    /** Scheme of the ldap url. */
    private final String urlScheme;

    /** Hostname of the ldap url. */
    private final String urlHostname;

    /** Port of the ldap url. */
    private final int urlPort;

    /** Base DN of the ldap url. */
    private final String urlBaseDn;

    /** Attributes of the ldap url. */
    private final String[] urlAttributes;

    /** Search scope of the ldap url. */
    private final SearchScope urlScope;

    /** Search filter of the ldap url. */
    private final SearchFilter urlFilter;


    /**
     * Creates a new entry.
     *
     * @param  scheme  entryScheme
     * @param  hostname  entryHostname
     * @param  port  entryPort
     * @param  baseDn  base DN
     * @param  attributes  attributes
     * @param  scope  search scope
     * @param  filter  search filter
     */
    public Entry(
      final String scheme,
      final String hostname,
      final int port,
      final String baseDn,
      final String[] attributes,
      final SearchScope scope,
      final SearchFilter filter)
    {
      if (scheme == null) {
        throw new IllegalArgumentException("Scheme cannot be null");
      }
      urlScheme = scheme;
      urlHostname = hostname;
      urlPort = port;
      urlBaseDn = baseDn;
      urlAttributes = attributes;
      urlScope = scope;
      urlFilter = filter;
    }


    /**
     * Returns the entryScheme.
     *
     * @return  entryScheme
     */
    public String getScheme()
    {
      return urlScheme;
    }


    /**
     * Returns the entryHostname.
     *
     * @return  entryHostname
     */
    public String getHostname()
    {
      return urlHostname;
    }


    /**
     * Returns the entryPort.
     *
     * @return  entryPort
     */
    public int getPort()
    {
      if (urlPort == -1) {
        return "ldaps".equals(urlScheme) ? DEFAULT_LDAPS_PORT : DEFAULT_LDAP_PORT;
      }
      return urlPort;
    }


    /**
     * Returns whether a port was supplied in this entry.
     *
     * @return  whether a port was supplied in this entry
     */
    public boolean isDefaultPort()
    {
      return urlPort == -1;
    }


    /**
     * Returns the base DN.
     *
     * @return  baseDn
     */
    public String getBaseDn()
    {
      return urlBaseDn == null ? DEFAULT_BASE_DN : urlBaseDn;
    }


    /**
     * Returns whether a base DN was supplied in this entry.
     *
     * @return  whether a base DN was supplied in this entry
     */
    public boolean isDefaultBaseDn()
    {
      return urlBaseDn == null;
    }


    /**
     * Returns the attributes.
     *
     * @return  attributes
     */
    public String[] getAttributes()
    {
      return urlAttributes == null ? DEFAULT_ATTRIBUTES : urlAttributes;
    }


    /**
     * Returns whether attributes were supplied in this entry.
     *
     * @return  whether a attributes were supplied in this entry
     */
    public boolean isDefaultAttributes()
    {
      return urlAttributes == null;
    }


    /**
     * Returns the scope.
     *
     * @return  scope
     */
    public SearchScope getScope()
    {
      return urlScope == null ? DEFAULT_SCOPE : urlScope;
    }


    /**
     * Returns whether a scope was supplied in this entry.
     *
     * @return  whether a scope was supplied in this entry
     */
    public boolean isDefaultScope()
    {
      return urlScope == null;
    }


    /**
     * Returns the filter.
     *
     * @return  filter
     */
    public SearchFilter getFilter()
    {
      return urlFilter == null ? DEFAULT_FILTER : urlFilter;
    }


    /**
     * Returns whether a filter was supplied in this entry.
     *
     * @return  whether a filter was supplied in this entry
     */
    public boolean isDefaultFilter()
    {
      return urlFilter == null;
    }


    /**
     * Returns the formatted URL as scheme://hostname:port/baseDn?attrs?scope?filter.
     *
     * @return  url
     */
    public String getUrl()
    {

      final StringBuilder sb = new StringBuilder(urlScheme).append("://");
      final String hostname = getHostname();
      if (hostname != null) {
        // ipv6 address
        if (hostname.indexOf(":") != -1) {
          sb.append("[").append(hostname).append("]");
        } else {
          sb.append(hostname);
        }
      }
      sb.append(":").append(getPort());
      sb.append("/").append(LdapUtils.percentEncode(getBaseDn()));
      sb.append("?");

      final String[] attrs = getAttributes();
      for (int i = 0; i < attrs.length; i++) {
        sb.append(attrs[i]);
        if (i + 1 < attrs.length) {
          sb.append(",");
        }
      }
      sb.append("?");

      final SearchScope scope = getScope();
      if (SearchScope.OBJECT == scope) {
        sb.append("base");
      } else if (SearchScope.ONELEVEL == scope) {
        sb.append("one");
      } else if (SearchScope.SUBTREE == scope) {
        sb.append("sub");
      }
      sb.append("?").append(LdapUtils.percentEncode(getFilter().format()));
      return sb.toString();
    }


    /**
     * Returns the hostname:port.
     *
     * @return  hostname:port
     */
    public String getHostnameWithPort()
    {
      return String.format("%s:%s", getHostname(), getPort());
    }


    /**
     * Returns the scheme://hostname:port.
     *
     * @return  scheme://hostname:port
     */
    public String getHostnameWithSchemeAndPort()
    {
      return String.format("%s://%s:%s", getScheme(), getHostname(), getPort());
    }


    @Override
    public String toString()
    {
      return
        String.format(
          "[%s@%d::scheme=%s, hostname=%s, port=%s, baseDn=%s, " +
          "attributes=%s, scope=%s, filter=%s]",
          getClass().getName(),
          hashCode(),
          urlScheme,
          urlHostname,
          urlPort,
          urlBaseDn,
          Arrays.toString(urlAttributes),
          urlScope,
          urlFilter);
    }
  }
}
