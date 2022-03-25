/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for parsing LDAP URLs. See RFC 4516. Expects URLs of the form scheme://hostname:port/baseDn?attrs?scope?filter.
 * This implementation does not support URL extensions.
 *
 * @author  Middleware Services
 */
// CheckStyle:HiddenField OFF
public class LdapURL
{

  /** Pattern to match LDAP URL. */
  protected static final Pattern URL_PATTERN = Pattern.compile(
    "([lL][dD][aA][pP][sSiI]?)://(\\[[0-9A-Fa-f:]+\\]|[^:/]+)?" +
      "(?::(\\d+))?" +
      "(?:/(?:([^?]+))?" +
      "(?:\\?([^?]*))?" +
      "(?:\\?([^?]*))?" +
      "(?:\\?(.*))?)?");

  /** Default LDAP port, value is {@value}. */
  protected static final int DEFAULT_LDAP_PORT = 389;

  /** Default LDAPS port, value is {@value}. */
  protected static final int DEFAULT_LDAPS_PORT = 636;

  /** Default base DN, value is {@value}. */
  protected static final String DEFAULT_BASE_DN = "";

  /** Default search filter value is '(objectClass=*)'. */
  protected static final String DEFAULT_FILTER = "(objectClass=*)";

  /** Default scope, value is {@link SearchScope#OBJECT}. */
  protected static final SearchScope DEFAULT_SCOPE = SearchScope.OBJECT;

  /** Default return attributes, value is all user attributes. */
  protected static final String[] DEFAULT_ATTRIBUTES = ReturnAttributes.ALL_USER.value();

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10333;

  /** Scheme of the ldap url. */
  private String scheme;

  /** Hostname of the ldap url. */
  private String hostname;

  /** Port of the ldap url. */
  private int port;

  /** Base DN of the ldap url. */
  private String baseDn;

  /** Attributes of the ldap url. */
  private String[] attributes;

  /** Search scope of the ldap url. */
  private SearchScope scope;

  /** Search filter of the ldap url. */
  private String filter;

  /** Metadata that describes connection failures on this URL. */
  private LdapURLRetryMetadata retryMetadata;

  /**
   * False if the last connection attempt to this URL failed, which should result in updating {@link #retryMetadata},
   * otherwise true.
   */
  private boolean active = true;

  /** IP address resolved for this URL. */
  private InetAddress inetAddress;


  /** Private constructor. */
  private LdapURL() {}


  /**
   * Creates a new ldap url.
   *
   * @param  hostname  LDAP server hostname
   * @param  port  TCP port the LDAP server is listening on
   */
  public LdapURL(final String hostname, final int port)
  {
    this("ldap://" + hostname + ":" + port);
  }


  /**
   * Creates a new ldap url.
   *
   * @param  url  LDAP url
   */
  public LdapURL(final String url)
  {
    parseURL(url);
  }


  /**
   * Creates a new ldap url.
   *
   * @param  scheme  url scheme
   * @param  hostname  url hostname
   * @param  port  url port
   * @param  baseDn  base DN
   * @param  attributes  attributes
   * @param  scope  search scope
   * @param  filter  search filter
   */
  protected LdapURL(
    final String scheme,
    final String hostname,
    final int port,
    final String baseDn,
    final String[] attributes,
    final SearchScope scope,
    final String filter)
  {
    if (scheme == null) {
      throw new IllegalArgumentException("Scheme cannot be null");
    }
    this.scheme = scheme;
    this.hostname = hostname;
    this.port = port;
    this.baseDn = baseDn;
    this.attributes = attributes;
    this.scope = scope;
    this.filter = filter;
  }


  /**
   * Returns the scheme.
   *
   * @return  scheme
   */
  public String getScheme()
  {
    return scheme;
  }


  /**
   * Returns the hostname.
   *
   * @return  hostname
   */
  public String getHostname()
  {
    return hostname;
  }


  /**
   * Returns the port. If no port was supplied, returns the default port for the scheme.
   *
   * @return  port
   */
  public int getPort()
  {
    if (port == -1) {
      return "ldaps".equals(scheme) ? DEFAULT_LDAPS_PORT : DEFAULT_LDAP_PORT;
    }
    return port;
  }


  /**
   * Returns false if a port was supplied in this url.
   *
   * @return  false if a port was supplied in this url
   */
  public boolean isDefaultPort()
  {
    return port == -1;
  }


  /**
   * Returns the base DN.
   *
   * @return  baseDn
   */
  public String getBaseDn()
  {
    return baseDn == null ? DEFAULT_BASE_DN : baseDn;
  }


  /**
   * Returns whether a base DN was supplied in this url.
   *
   * @return  whether a base DN was supplied in this url
   */
  public boolean isDefaultBaseDn()
  {
    return baseDn == null;
  }


  /**
   * Returns the attributes.
   *
   * @return  attributes
   */
  public String[] getAttributes()
  {
    return attributes == null ? DEFAULT_ATTRIBUTES : attributes;
  }


  /**
   * Returns whether attributes were supplied in this url.
   *
   * @return  whether a attributes were supplied in this url
   */
  public boolean isDefaultAttributes()
  {
    return attributes == null;
  }


  /**
   * Returns the scope.
   *
   * @return  scope
   */
  public SearchScope getScope()
  {
    return scope == null ? DEFAULT_SCOPE : scope;
  }


  /**
   * Returns whether a scope was supplied in this url.
   *
   * @return  whether a scope was supplied in this url
   */
  public boolean isDefaultScope()
  {
    return scope == null;
  }


  /**
   * Returns the filter.
   *
   * @return  filter
   */
  public String getFilter()
  {
    return filter == null ? DEFAULT_FILTER : filter;
  }


  /**
   * Returns whether a filter was supplied in this url.
   *
   * @return  whether a filter was supplied in this url
   */
  public boolean isDefaultFilter()
  {
    return filter == null;
  }


  /**
   * Returns the formatted URL as scheme://hostname:port/baseDn?attrs?scope?filter.
   *
   * @return  url
   */
  public String getUrl()
  {

    final StringBuilder sb = new StringBuilder(scheme).append("://");
    final String hostname = getHostname();
    if (hostname != null) {
      // ipv6 address
      if (hostname.contains(":")) {
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
    sb.append("?").append(LdapUtils.percentEncode(getFilter()));
    return sb.toString();
  }


  /**
   * Returns the hostname:port.
   *
   * @return  hostname:port
   */
  public String getHostnameWithPort()
  {
    return new StringBuilder(getHostname() != null ? getHostname() : "null").append(":").append(getPort()).toString();
  }


  /**
   * Returns the scheme://hostname:port.
   *
   * @return  scheme://hostname:port
   */
  public String getHostnameWithSchemeAndPort()
  {
    return new StringBuilder(getScheme()).append("://")
      .append(getHostname() != null ? getHostname() : "null").append(":").append(getPort()).toString();
  }


  /**
   * Returns the retry metadata.
   *
   * @return  metadata describing retry attempts for connections made this URL.
   */
  LdapURLRetryMetadata getRetryMetadata()
  {
    return retryMetadata;
  }


  /**
   * Sets the retry metadata.
   *
   * @param  metadata  retry metadata
   */
  void setRetryMetadata(final LdapURLRetryMetadata metadata)
  {
    retryMetadata = metadata;
  }


  /**
   * Returns whether this URL is currently active.
   *
   * @return  true if this URL can be connected to, false otherwise.
   */
  boolean isActive()
  {
    return active;
  }


  /**
   * Marks this URL as active.
   */
  void activate()
  {
    active = true;
  }


  /**
   * Marks this URL as inactive.
   */
  void deactivate()
  {
    active = false;
  }


  /**
   * Returns the resolved IP address.
   *
   * @return  resolved IP address for this URL.
   */
  public InetAddress getInetAddress()
  {
    return inetAddress;
  }


  /**
   * Sets the resolved IP address.
   *
   * @param  address  IP address for this URL
   */
  void setInetAddress(final InetAddress address)
  {
    inetAddress = address;
  }


  /**
   * Returns a new ldap URL initialized with the supplied URL.
   *
   * @param  ldapURL  ldap URL to read properties from
   *
   * @return  ldap URL
   */
  public static LdapURL copy(final LdapURL ldapURL)
  {
    final LdapURL url = new LdapURL();
    url.scheme = ldapURL.scheme;
    url.hostname = ldapURL.hostname;
    url.port = ldapURL.port;
    url.baseDn = ldapURL.baseDn;
    url.attributes = ldapURL.attributes;
    url.scope = ldapURL.scope;
    url.filter = ldapURL.filter;
    url.retryMetadata = ldapURL.retryMetadata;
    url.active = ldapURL.active;
    url.inetAddress = ldapURL.inetAddress;
    return url;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof LdapURL) {
      final LdapURL v = (LdapURL) o;
      return LdapUtils.areEqual(scheme, v.scheme) &&
        LdapUtils.areEqual(hostname, v.hostname) &&
        LdapUtils.areEqual(port, v.port) &&
        LdapUtils.areEqual(baseDn, v.baseDn) &&
        LdapUtils.areEqual(attributes, v.attributes) &&
        LdapUtils.areEqual(scope, v.scope) &&
        LdapUtils.areEqual(filter, v.filter) &&
        LdapUtils.areEqual(inetAddress, v.inetAddress);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(
      HASH_CODE_SEED,
      scheme,
      hostname,
      port,
      baseDn,
      attributes,
      scope,
      filter,
      inetAddress);
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[")
      .append(getClass().getName()).append("@").append(hashCode()).append("::")
      .append("scheme=").append(scheme).append(", ")
      .append("hostname=").append(hostname).append(", ")
      .append("port=").append(port).append(", ")
      .append("baseDn=").append(baseDn).append(", ")
      .append("attributes=").append(Arrays.toString(attributes)).append(", ")
      .append("scope=").append(scope).append(", ")
      .append("filter=").append(filter).append(", ")
      .append("inetAddress=").append(inetAddress).append("]").toString();
  }


  /**
   * Matches the supplied url against a pattern and reads its components.
   *
   * @param  url  to parse
   */
  protected void parseURL(final String url)
  {
    final Matcher m = URL_PATTERN.matcher(url);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid LDAP URL: " + url);
    }

    // CheckStyle:MagicNumber OFF
    scheme = m.group(1).toLowerCase();
    hostname = m.group(2);
    if (hostname != null) {
      // check for ipv6 address
      if (hostname.startsWith("[") && hostname.endsWith("]")) {
        hostname = hostname.substring(1, hostname.length() - 1).trim();
      }
    }

    port = m.group(3) != null ? Integer.parseInt(m.group(3)) : -1;
    baseDn = m.group(4) != null ? LdapUtils.percentDecode(m.group(4)) : null;
    attributes = m.group(5) != null ? m.group(5).length() > 0 ? m.group(5).split(",") : null : null;
    final String scope = m.group(6);
    if (scope != null && scope.length() > 0) {
      if ("base".equalsIgnoreCase(scope)) {
        this.scope = SearchScope.OBJECT;
      } else if ("one".equalsIgnoreCase(scope)) {
        this.scope = SearchScope.ONELEVEL;
      } else if ("sub".equalsIgnoreCase(scope)) {
        this.scope = SearchScope.SUBTREE;
      } else {
        throw new IllegalArgumentException("Invalid scope: " + scope);
      }
    }

    filter = m.group(7) != null
      ? m.group(7).length() > 0 ? LdapUtils.percentDecode(m.group(7)) : null : null;
    // CheckStyle:MagicNumber ON
  }
}
// CheckStyle:HiddenField ON
