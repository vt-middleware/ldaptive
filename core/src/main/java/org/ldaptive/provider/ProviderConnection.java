/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.ConnectionStrategy;
import org.ldaptive.DnsSrvConnectionStrategy;
import org.ldaptive.LdapException;
import org.ldaptive.LdapURL;
import org.ldaptive.LdapURLSet;
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

  /** Factory that produced this instance. */
  protected final ConnectionFactory connectionFactory;

  /** Executor for scheduling tasks. */
  private final ScheduledExecutorService executor;


  /**
   * Creates a new instance.
   *
   * @param factory The factory that produced this connection.
   */
  public ProviderConnection(final ConnectionFactory factory)
  {
    connectionFactory = factory;
    final ConnectionStrategy strategy = connectionFactory.getConnectionConfig().getConnectionStrategy();
    if (factory.getLdapURLSet().getActiveUrls().size() > 1 || strategy instanceof DnsSrvConnectionStrategy) {
      executor = Executors.newSingleThreadScheduledExecutor(
        r -> {
          final Thread t = new Thread(r);
          t.setDaemon(true);
          return t;
        });
      executor.scheduleAtFixedRate(
        () -> {
          final LdapURLSet urlSet = connectionFactory.getLdapURLSet();
          while (urlSet.hasInactiveUrls()) {
            urlSet.doWithNextInactiveUrl(strategy.getInactiveCondition().and(this::test));
          }
        },
        strategy.getInactivePeriod().toMillis(),
        strategy.getInactivePeriod().toMillis(),
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
    final ConnectionStrategy strategy = connectionFactory.getConnectionConfig().getConnectionStrategy();
    final LdapURLSet urlSet = connectionFactory.getLdapURLSet();
    LdapException lastThrown = null;
    LdapURL url = null;
    do {
      try {
        url = urlSet.doWithNextActiveUrl(this::wrappedOpen);
        LOGGER.debug("Successfully connected to {} using strategy {}", url.getHostnameWithSchemeAndPort(), strategy);
        lastThrown = null;
      } catch (LdapException e) {
        lastThrown = e;
        LOGGER.debug("LDAP connection error using strategy {}", strategy, e);
      }
    } while (!isOpen() && urlSet.hasActiveUrls());
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


  /**
   * Provides an unchecked wrapper around {@link #open()}.
   *
   * @param url LDAP URL to attempt to connect to.
   */
  private void wrappedOpen(final LdapURL url)
  {
    try {
      open(url);
    } catch (LdapException e) {
      throw new RuntimeException(e);
    }
  }
}
