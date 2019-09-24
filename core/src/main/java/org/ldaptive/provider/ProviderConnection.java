/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import java.time.Instant;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.ldaptive.ActivePassiveConnectionStrategy;
import org.ldaptive.ConnectException;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionStrategy;
import org.ldaptive.LdapException;
import org.ldaptive.LdapURL;
import org.ldaptive.RetryMetadata;
import org.ldaptive.UnbindRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for connection implementations.
 *
 * @author  Middleware Services
 */
// CheckStyle:AbstractClassName OFF
public abstract class ProviderConnection implements Connection
// CheckStyle:AbstractClassName ON
{

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ProviderConnection.class);

  /** Only one invocation of open can occur at a time. */
  protected final Lock openLock = new ReentrantLock();

  /** Only one invocation of close can occur at a time. */
  protected final Lock closeLock = new ReentrantLock();

  /** Provides host connection configuration. */
  protected final ConnectionConfig connectionConfig;

  /** Connection strategy for this connection. Default value is {@link ActivePassiveConnectionStrategy}. */
  private final ConnectionStrategy connectionStrategy;

  /** Time of the last successful open for this connection. */
  private Instant lastSuccessfulOpen;


  /**
   * Creates a new provider connection.
   *
   * @param  config  connection configuration
   */
  public ProviderConnection(final ConnectionConfig config)
  {
    connectionConfig = config;
    connectionStrategy = connectionConfig.getConnectionStrategy();
    synchronized (connectionStrategy) {
      if (!connectionStrategy.isInitialized()) {
        connectionStrategy.initialize(connectionConfig.getLdapUrl(), url -> test(url));
      }
    }
  }


  @Override
  public void open()
    throws LdapException
  {
    if (isOpen()) {
      throw new ConnectException("Connection is already open");
    }

    LOGGER.debug("Strategy {} opening connection {}", connectionStrategy, this);
    openLock.lock();
    try {
      LdapException lastThrown = null;
      final RetryMetadata metadata = new RetryMetadata(connectionConfig.getConnectionStrategy(), lastSuccessfulOpen);
      do {
        boolean strategyProducedUrls = false;
        final Iterator<LdapURL> iter = connectionStrategy.iterator();
        while (iter.hasNext()) {
          strategyProducedUrls = true;
          final LdapURL url = iter.next();
          try {
            LOGGER.trace(
              "Attempting connection to {} for strategy {}", url.getHostnameWithSchemeAndPort(), connectionStrategy);
            open(url);
            connectionStrategy.success(url);
            lastThrown = null;
            break;
          } catch (ConnectException e) {
            connectionStrategy.failure(url);
            lastThrown = e;
            LOGGER.debug(
              "Error connecting to {} for strategy {}", url.getHostnameWithSchemeAndPort(), connectionStrategy, e);
          }
        }
        if (!strategyProducedUrls) {
          throw new ConnectException("Connection strategy did not produce any LDAP URLs");
        }
        if (lastThrown != null) {
          metadata.recordFailure(Instant.now());
        }
      } while (lastThrown != null && connectionConfig.getAutoReconnectCondition().test(metadata));
      if (lastThrown != null) {
        throw lastThrown;
      }
      lastSuccessfulOpen = Instant.now();
      LOGGER.debug("Strategy {} opened connection {}", connectionStrategy, this);
    } finally {
      openLock.unlock();
    }
  }


  /**
   * Determine whether the supplied URL is acceptable for use.
   *
   * @param  url  LDAP URL to test
   *
   * @return  whether URL can be become active
   */
  protected abstract boolean test(LdapURL url);


  /**
   * Attempt to open a connection to the supplied LDAP URL.
   *
   * @param  url  LDAP URL to connect to
   *
   * @throws  LdapException  if opening the connection fails
   */
  protected abstract void open(LdapURL url) throws LdapException;


  /**
   * Executes an unbind operation. Clients should close connections using {@link #close()}.
   *
   * @param  request  unbind request
   */
  protected abstract void operation(UnbindRequest request);


  /**
   * Write the request in the supplied handle to the LDAP server. This method does not throw, it should report
   * exceptions to the handle.
   *
   * @param  handle  for the operation write
   */
  protected abstract void write(DefaultOperationHandle handle);
}
