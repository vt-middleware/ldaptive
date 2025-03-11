/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.net.InetAddress;
import org.ldaptive.url.DefaultUrlParser;
import org.ldaptive.url.Url;
import org.ldaptive.url.UrlParser;

/**
 * Class to represent an LDAP URL in order to make connections to an LDAP server. URL components are parsed according to
 * RFC 4516. See {@link Url}
 *
 * @author  Middleware Services
 */
public final class LdapURL
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10333;

  /** Parsed URL properties. */
  private Url url;

  /** Metadata that describes connection failures on this URL. */
  private LdapURLRetryMetadata retryMetadata;

  /**
   * False if the last connection attempt to this URL failed, which should result in updating {@link #retryMetadata},
   * otherwise true.
   */
  private boolean active = true;

  /** IP address resolved for this URL. */
  private InetAddress inetAddress;

  /** Priority of this URL. Lower numbers indicate higher priority. */
  private long priority;

  /** Private constructor. */
  private LdapURL() {}


  /**
   * Creates a new LDAP URL with the supplied {@link Url}.
   *
   * @param  ldapURL  to set
   */
  private LdapURL(final Url ldapURL)
  {
    LdapUtils.assertNotNullArgOr(ldapURL, u -> u.getHostname() == null, "URL and hostname cannot be null");
    url = ldapURL;
  }


  /**
   * Creates a new ldap url.
   *
   * @param  hostname  LDAP server hostname
   * @param  port  TCP port the LDAP server is listening on
   */
  public LdapURL(final String hostname, final int port)
  {
    this("ldap://" + (hostname != null ? hostname : "") + ":" + port);
  }


  /**
   * Creates a new ldap url.
   *
   * @param  ldapUrl  string representation of LDAP URL
   */
  public LdapURL(final String ldapUrl)
  {
    this(ldapUrl, new DefaultUrlParser());
  }


  /**
   * Creates a new ldap url.
   *
   * @param  ldapUrl  string representation of LDAP URL
   * @param  parser  to parse the url
   */
  public LdapURL(final String ldapUrl, final UrlParser parser)
  {
    this(LdapUtils.assertNotNullArg(parser, "URL parser cannot be null").parse(ldapUrl));
  }


  /**
   * Returns the scheme.
   *
   * @return  scheme
   */
  public String getScheme()
  {
    return url.getScheme();
  }


  /**
   * Returns whether the scheme is 'ldaps'.
   *
   * @return  whether the scheme is 'ldaps'
   */
  public boolean isSchemeLdaps()
  {
    return url.isSchemeLdaps();
  }


  /**
   * Returns the hostname.
   *
   * @return  hostname
   */
  public String getHostname()
  {
    return url.getHostname();
  }


  /**
   * Returns the port. If no port was supplied, returns the default port for the scheme.
   *
   * @return  port
   */
  public int getPort()
  {
    return url.getPort();
  }


  public Url getUrl()
  {
    return url;
  }


  /**
   * Returns the hostname:port.
   *
   * @return  hostname:port
   */
  public String getHostnameWithPort()
  {
    return getHostname() + ":" + getPort();
  }


  /**
   * Returns the scheme://hostname:port.
   *
   * @return  scheme://hostname:port
   */
  public String getHostnameWithSchemeAndPort()
  {
    return getScheme() + "://" + getHostname() + ":" + getPort();
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
   * Returns the priority of this URL. Lower numbers indicate a higher priority.
   *
   * @return  priority for this URL.
   */
  public long getPriority()
  {
    return priority;
  }


  /**
   * Sets the priority of this URL.
   *
   * @param  p  priority for this URL
   */
  void setPriority(final long p)
  {
    if (p < 0) {
      throw new IllegalArgumentException("Priority cannot be negative");
    }
    priority = p;
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
    url.url = ldapURL.url;
    url.retryMetadata = ldapURL.retryMetadata;
    url.active = ldapURL.active;
    url.inetAddress = ldapURL.inetAddress;
    url.priority = ldapURL.priority;
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
      return LdapUtils.areEqual(url, v.url) &&
        LdapUtils.areEqual(inetAddress, v.inetAddress);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(
      HASH_CODE_SEED,
      url,
      inetAddress);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "url=" + getHostnameWithSchemeAndPort() + ", " +
      "inetAddress=" + inetAddress + "]";
  }
}
