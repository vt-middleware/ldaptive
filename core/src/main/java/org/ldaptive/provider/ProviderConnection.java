/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.ldaptive.ActivePassiveConnectionStrategy;
import org.ldaptive.ConnectException;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionStrategy;
import org.ldaptive.DnsSrvConnectionStrategy;
import org.ldaptive.LdapException;
import org.ldaptive.LdapURL;
import org.ldaptive.RetryMetadata;
import org.ldaptive.UnbindRequest;
import org.ldaptive.control.RequestControl;
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

  /** Provides host connection configuration. */
  protected final ConnectionConfig connectionConfig;

  /** Connection strategy for this connection. Default value is {@link ActivePassiveConnectionStrategy}. */
  private final ConnectionStrategy connectionStrategy;

  /** Executor for scheduling tasks. */
  private final ScheduledExecutorService executor;


  /**
   * Creates a new provider connection.
   *
   * @param  config  connection configuration
   */
  public ProviderConnection(final ConnectionConfig config)
  {
    connectionConfig = config;
    if (connectionConfig.getConnectionStrategy() == null) {
      if (connectionConfig.getLdapUrl().startsWith("dns:")) {
        connectionStrategy = new DnsSrvConnectionStrategy();
      } else {
        connectionStrategy = new ActivePassiveConnectionStrategy();
      }
    } else {
      connectionStrategy = connectionConfig.getConnectionStrategy();
    }
    synchronized (connectionStrategy) {
      if (!connectionStrategy.isInitialized()) {
        connectionStrategy.initialize(connectionConfig.getLdapUrl());
      }
    }

    if (connectionStrategy.active().size() > 1 || connectionStrategy instanceof DnsSrvConnectionStrategy) {
      executor = Executors.newSingleThreadScheduledExecutor(
        r -> {
          final Thread t = new Thread(r);
          t.setDaemon(true);
          return t;
        });
      executor.scheduleAtFixedRate(
        () -> {
          for (Map.Entry<RetryMetadata, Map.Entry<Integer, LdapURL>> entry : connectionStrategy.inactive().entrySet()) {
            // ensure inactive URL waits at least the inactive period before testing
            if (Instant.now().isAfter(entry.getKey().getFailureTime().plus(connectionStrategy.getInactivePeriod()))) {
              if (connectionStrategy.getInactiveCondition().test(entry.getKey())) {
                if (test(entry.getValue().getValue())) {
                  connectionStrategy.success(entry.getValue().getValue());
                } else {
                  entry.getKey().incrementAttempts();
                }
              }
            }
          }
        },
        connectionStrategy.getInactivePeriod().toMillis(),
        connectionStrategy.getInactivePeriod().toMillis(),
        TimeUnit.MILLISECONDS);
      LOGGER.debug("inactive connection strategy task scheduled for {}", this);
    } else {
      executor = null;
    }
  }


  @Override
  public synchronized void open()
    throws LdapException
  {
    LOGGER.debug("Opening connection {}", this);
    if (isOpen()) {
      throw new ConnectException("Connection is already open");
    }

    LdapException lastThrown = null;
    final List<LdapURL> urls = connectionStrategy.apply();
    if (urls == null || urls.isEmpty()) {
      throw new ConnectException("Connection strategy did not produced any LDAP URLs");
    }
    for (LdapURL url : urls) {
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
    if (lastThrown != null) {
      throw lastThrown;
    }
  }


  @Override
  public synchronized void close(final RequestControl... controls)
  {
    if (executor != null) {
      executor.shutdown();
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


  /**
   * Report back to the connection that the supplied handle has received a response and is done.
   *
   * @param  handle  that is done
   */
  protected abstract void done(DefaultOperationHandle handle);
}
