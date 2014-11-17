/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import org.ldaptive.LdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a basic implementation for other connection factories to inherit.
 *
 * @param  <T>  type of provider config for this connection factory
 *
 * @author  Middleware Services
 */
public abstract class
AbstractProviderConnectionFactory<T extends ProviderConfig>
  implements ProviderConnectionFactory<T>
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Provider configuration. */
  private final T providerConfig;

  /** Factory metadata. */
  private final DefaultConnectionFactoryMetadata metadata;


  /**
   * Creates a new abstract connection factory. Once invoked the supplied
   * provider config is made immutable. See {@link
   * ProviderConfig#makeImmutable()}.
   *
   * @param  url  of the ldap to connect to
   * @param  config  provider configuration
   */
  public AbstractProviderConnectionFactory(final String url, final T config)
  {
    if (url == null) {
      throw new IllegalArgumentException("LDAP URL cannot be null");
    }
    metadata = new DefaultConnectionFactoryMetadata(url);
    providerConfig = config;
    providerConfig.makeImmutable();
  }


  /** {@inheritDoc} */
  @Override
  public T getProviderConfig()
  {
    return providerConfig;
  }


  /**
   * Returns the connection factory metadata.
   *
   * @return  metadata
   */
  protected ConnectionFactoryMetadata getMetadata()
  {
    return metadata;
  }


  /** {@inheritDoc} */
  @Override
  public ProviderConnection create()
    throws LdapException
  {
    LdapException lastThrown = null;
    final String[] urls = providerConfig.getConnectionStrategy().getLdapUrls(
      metadata);
    if (urls == null || urls.length == 0) {
      throw new ConnectionException(
        "Connection strategy " + providerConfig.getConnectionStrategy() +
        " did not produce any LDAP URLs for " + metadata);
    }
    ProviderConnection conn = null;
    for (String url : urls) {
      try {
        logger.trace(
          "[{}] Attempting connection to {} for strategy {}",
          new Object[] {
            metadata,
            url,
            providerConfig.getConnectionStrategy(),
          });
        conn = createInternal(url);
        metadata.incrementCount();
        lastThrown = null;
        break;
      } catch (ConnectionException e) {
        lastThrown = e;
        logger.debug("Error connecting to LDAP URL: {}", url, e);
      }
    }
    if (lastThrown != null) {
      throw lastThrown;
    }
    return conn;
  }


  /**
   * Create the provider connection and prepare the connection for use.
   *
   * @param  url  to connect to
   *
   * @return  provider connection
   *
   * @throws  LdapException  if a connection cannot be established
   */
  protected abstract ProviderConnection createInternal(final String url)
    throws LdapException;


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::metadata=%s, providerConfig=%s]",
        getClass().getName(),
        hashCode(),
        metadata,
        providerConfig);
  }


  /** Provides an object to track the connection count. */
  private class DefaultConnectionFactoryMetadata
    implements ConnectionFactoryMetadata
  {

    /** ldap url. */
    private final String ldapUrl;

    /** connection count. */
    private int count;


    /**
     * Creates a new default connection factory metadata.
     *
     * @param  s  ldap url
     */
    public DefaultConnectionFactoryMetadata(final String s)
    {
      ldapUrl = s;
    }


    /** {@inheritDoc} */
    @Override
    public String getLdapUrl()
    {
      return ldapUrl;
    }


    /** {@inheritDoc} */
    @Override
    public int getConnectionCount()
    {
      return count;
    }


    /** Increments the connection count. */
    private void incrementCount()
    {
      count++;
      // reset the count if it exceeds the size of an integer
      if (count < 0) {
        count = 0;
      }
    }


    /** {@inheritDoc} */
    @Override
    public String toString()
    {
      return String.format("[ldapUrl=%s, count=%s]", ldapUrl, count);
    }
  }
}
