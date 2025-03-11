/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.url;

import java.util.Arrays;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchScope;
import org.ldaptive.dn.DefaultDnParser;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.DnParser;
import org.ldaptive.filter.DefaultFilterFunction;
import org.ldaptive.filter.Filter;
import org.ldaptive.filter.FilterFunction;
import org.ldaptive.filter.PresenceFilter;

/**
 * Class to represent an LDAP URL. See RFC 4516. Expects URLs of the form
 * scheme://hostname:port/baseDn?attrs?scope?filter. This implementation does not support URL extensions.
 *
 * @author  Middleware Services
 */
public final class Url
{

  /** Default LDAP port, value is {@value}. */
  static final int DEFAULT_LDAP_PORT = 389;

  /** Default LDAPS port, value is {@value}. */
  static final int DEFAULT_LDAPS_PORT = 636;

  /** Default LDAPI port, value is {@value}. */
  static final int DEFAULT_LDAPI_PORT = 0;

  /** Default base DN, value is {@value}. */
  static final String DEFAULT_BASE_DN = "";

  /** Default parsed base DN. */
  static final Dn DEFAULT_PARSED_BASE_DN = new Dn();

  /** Default attribute filter value is '(objectClass=*)'. */
  static final String DEFAULT_FILTER = "(objectClass=*)";

  /** Default parsed attribute filter. */
  static final Filter DEFAULT_PARSED_FILTER = new PresenceFilter("objectClass");

  /** Default scope, value is {@link SearchScope#OBJECT}. */
  static final SearchScope DEFAULT_SCOPE = SearchScope.OBJECT;

  /** Default return attributes, value is all user attributes. */
  static final String[] DEFAULT_ATTRIBUTES = new String[0];

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 11003;

  /** For parsing DNs. */
  private static final DnParser DN_PARSER = new DefaultDnParser();

  /** For parsing filters. */
  private static final FilterFunction FILTER_FUNCTION = new DefaultFilterFunction();

  /** LDAP scheme. */
  private static final String LDAP_SCHEME = "ldap";

  /** LDAPS scheme. */
  private static final String LDAPS_SCHEME = "ldaps";

  /** LDAPI scheme. */
  private static final String LDAPI_SCHEME = "ldapi";

  /** Scheme of the ldap url. */
  private final String scheme;

  /** Hostname of the ldap url. */
  private final String hostname;

  /** Port of the ldap url. */
  private final int port;

  /** Base DN of the ldap url. */
  private final String baseDn;

  /** Parsed base DN of the ldap url. */
  private final Dn parsedBaseDn;

  /** Attributes of the ldap url. */
  private final String[] attributes;

  /** Search scope of the ldap url. */
  private final SearchScope scope;

  /** Search filter of the ldap url. */
  private final String filter;

  /** Parsed filter of the ldap url. */
  private final Filter parsedFilter;


  /**
   * Copy constructor.
   *
   * @param  ldapURL  to copy properties from
   */
  private Url(final Url ldapURL)
  {
    scheme = ldapURL.scheme;
    hostname = ldapURL.hostname;
    port = ldapURL.port;
    baseDn = ldapURL.baseDn;
    parsedBaseDn = ldapURL.parsedBaseDn;
    attributes = ldapURL.attributes;
    scope = ldapURL.scope;
    filter = ldapURL.filter;
    parsedFilter = ldapURL.parsedFilter;
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
  // CheckStyle:HiddenField OFF
  // CheckStyle:ParameterNumber OFF
  Url(
    final String scheme,
    final String hostname,
    final int port,
    final String baseDn,
    final String[] attributes,
    final SearchScope scope,
    final String filter)
  {
    validateScheme(scheme);
    validatePort(port, true);
    this.scheme = scheme;
    this.hostname = hostname;
    this.port = port;
    this.baseDn = baseDn;
    try {
      this.parsedBaseDn = this.baseDn != null ? new Dn(this.baseDn, DN_PARSER) : null;
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid baseDN: " + this.baseDn, e);
    }
    this.attributes = attributes;
    this.scope = scope;
    this.filter = filter;
    try {
      this.parsedFilter = this.filter != null ? FILTER_FUNCTION.parse(this.filter) : null;
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid filter: " + this.filter, e);
    }
  }
  // CheckStyle:ParameterNumber ON
  // CheckStyle:HiddenField ON


  /**
   * Creates a new ldap url.
   *
   * @param  url  LDAP url
   */
  public Url(final String url)
  {
    this(url, new DefaultUrlParser());
  }


  /**
   * Creates a new ldap url.
   *
   * @param  url  LDAP url
   * @param  parser  to parse the url
   */
  public Url(final String url, final UrlParser parser)
  {
    this(LdapUtils.assertNotNullArg(parser, "URL parser cannot be null").parse(url));
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
   * Returns whether the scheme is 'ldaps'.
   *
   * @return  whether the scheme is 'ldaps'
   */
  public boolean isSchemeLdaps()
  {
    return LDAPS_SCHEME.equals(scheme);
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
      final int defaultPort;
      switch (scheme) {
      case LDAP_SCHEME:
        defaultPort = DEFAULT_LDAP_PORT;
        break;
      case LDAPS_SCHEME:
        defaultPort = DEFAULT_LDAPS_PORT;
        break;
      case LDAPI_SCHEME:
        defaultPort = DEFAULT_LDAPI_PORT;
        break;
      default:
        throw new IllegalStateException("Unknown scheme: " + scheme);
      }
      return defaultPort;
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
   * Returns the parsed base DN.
   *
   * @return  parsed baseDn
   */
  public Dn getParsedBaseDn()
  {
    return parsedBaseDn == null ? DEFAULT_PARSED_BASE_DN : parsedBaseDn;
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
   * @return  whether an attributes were supplied in this url
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
   * Returns the parsed filter.
   *
   * @return  parsed filter
   */
  public Filter getParsedFilter()
  {
    return parsedFilter == null ? DEFAULT_PARSED_FILTER : parsedFilter;
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
   * Returns a string representation of this LDAP URL. Uses a {@link MinimalUrlFormatter} by default.
   *
   * @return  string form of the LDAP URL
   */
  public String format()
  {
    return format(new MinimalUrlFormatter());
  }


  /**
   * Returns a string representation of this LDAP URL.
   *
   * @param  formatter  to produce the string
   *
   * @return  string form of the LDAP URL
   */
  public String format(final UrlFormatter formatter)
  {
    return formatter.format(this);
  }


  /**
   * Determines whether the supplied scheme is valid. Must be one of 'ldap', 'ldaps' or 'ldapi'.
   *
   * @param  scheme  to validate
   */
  static void validateScheme(final String scheme)
  {
    if (!LDAP_SCHEME.equals(scheme) && !LDAPS_SCHEME.equals(scheme) && !LDAPI_SCHEME.equals(scheme)) {
      throw new IllegalArgumentException("Invalid LDAP URL scheme: " + scheme);
    }
  }


  /**
   * Determines whether the supplied port is valid.
   *
   * @param  port  to validate
   * @param  allowDefault  whether to allow default port of -1
   */
  static void validatePort(final int port, final boolean allowDefault)
  {
    if (port == -1 && allowDefault) {
      return;
    }
    // CheckStyle:MagicNumber OFF
    if (port <= 0 || port > 65535) {
      throw new IllegalArgumentException("Invalid port number: " + port);
    }
    // CheckStyle:MagicNumber ON
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof Url) {
      final Url v = (Url) o;
      return LdapUtils.areEqual(scheme, v.scheme) &&
        LdapUtils.areEqual(hostname, v.hostname) &&
        LdapUtils.areEqual(port, v.port) &&
        LdapUtils.areEqual(baseDn, v.baseDn) &&
        LdapUtils.areEqual(parsedBaseDn, v.parsedBaseDn) &&
        LdapUtils.areEqual(attributes, v.attributes) &&
        LdapUtils.areEqual(scope, v.scope) &&
        LdapUtils.areEqual(filter, v.filter) &&
        LdapUtils.areEqual(parsedFilter, v.parsedFilter);
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
      parsedBaseDn,
      attributes,
      scope,
      filter,
      parsedFilter);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "scheme=" + scheme + ", " +
      "hostname=" + hostname + ", " +
      "port=" + port + ", " +
      "baseDn=" + baseDn + ", " +
      "parsedBaseDn=" + parsedBaseDn + ", " +
      "attributes=" + Arrays.toString(attributes) + ", " +
      "scope=" + scope + ", " +
      "filter=" + filter + ", " +
      "parsedFilter=" + parsedFilter + "]";
  }
}
