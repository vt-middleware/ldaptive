/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.ldaptive.Connection;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.Response;

/**
 * Contains the base implementation for pooling connections. The main design objective for the supplied pooling
 * implementations is to provide a pool that does not block on connection creation or destruction. This is what accounts
 * for the multiple locks available on this class. The pool is backed by two queues, one for available connections and
 * one for active connections. Connections that are available via {@link #getConnection()} exist in the available queue.
 * Connections that are actively in use exist in the active queue. This implementation uses FIFO operations for each
 * queue.
 *
 * @author  Middleware Services
 */
public abstract class AbstractConnectionPool extends AbstractPool<Connection> implements ConnectionPool
{

  /** Lock for the entire pool. */
  protected final ReentrantLock poolLock = new ReentrantLock();

  /** Condition for notifying threads that a connection was returned. */
  protected final Condition poolNotEmpty = poolLock.newCondition();

  /** Lock for check ins. */
  protected final ReentrantLock checkInLock = new ReentrantLock();

  /** Lock for check outs. */
  protected final ReentrantLock checkOutLock = new ReentrantLock();

  /** List of available connections in the pool. */
  protected Queue<PooledConnectionProxy> available;

  /** List of connections in use. */
  protected Queue<PooledConnectionProxy> active;

  /** Connection factory to create connections with. */
  private DefaultConnectionFactory connectionFactory;

  /** Whether to connect to the ldap on connection creation. Default value is {@value}. */
  private boolean connectOnCreate = true;

  /** Type of queue. LIFO or FIFO. Default value is {@value}. */
  private QueueType queueType = QueueType.LIFO;

  /** Executor for scheduling pool tasks. */
  private ScheduledExecutorService poolExecutor;

  /** Whether {@link #initialize()} has been invoked. */
  private boolean initialized;

  /**
   * Whether {@link #initialize()} should throw if pooling configuration requirements are not met. Default value is
   * {@value}.
   */
  private boolean failFastInitialize = true;


  /**
   * Returns the connection factory for this pool.
   *
   * @return  connection factory
   */
  public DefaultConnectionFactory getConnectionFactory()
  {
    return connectionFactory;
  }


  /**
   * Sets the connection factory for this pool.
   *
   * @param  cf  connection factory
   */
  public void setConnectionFactory(final DefaultConnectionFactory cf)
  {
    logger.trace("setting connectionFactory: {}", cf);
    connectionFactory = cf;
  }


  /**
   * Returns whether connections will attempt to connect after creation. Default is true.
   *
   * @return  whether connections will attempt to connect after creation
   */
  public boolean getConnectOnCreate()
  {
    return connectOnCreate;
  }


  /**
   * Sets whether newly created connections will attempt to connect. Default is true.
   *
   * @param  b  connect on create
   */
  public void setConnectOnCreate(final boolean b)
  {
    logger.trace("setting connectOnCreate: {}", b);
    connectOnCreate = b;
  }


  /**
   * Returns the type of queue used for this connection pool.
   *
   * @return  queue type
   */
  public QueueType getQueueType()
  {
    return queueType;
  }


  /**
   * Sets the type of queue used for this connection pool. This property may have an impact on the success of the prune
   * strategy.
   *
   * @param  type  of queue
   */
  public void setQueueType(final QueueType type)
  {
    logger.trace("setting queueType: {}", type);
    queueType = type;
  }


  /**
   * Returns whether {@link #initialize()} should throw if pooling configuration requirements are not met.
   *
   * @return  whether {@link #initialize()} should throw
   */
  public boolean getFailFastInitialize()
  {
    return failFastInitialize;
  }


  /**
   * Sets whether {@link #initialize()} should throw if pooling configuration requirements are not met.
   *
   * @param  b  whether {@link #initialize()} should throw
   */
  public void setFailFastInitialize(final boolean b)
  {
    logger.trace("setting failFastInitialize: {}", b);
    failFastInitialize = b;
  }


  /**
   * Returns whether this pool has been initialized.
   *
   * @return  whether this pool has been initialized
   */
  public boolean isInitialized()
  {
    return initialized;
  }


  /**
   * Used to determine whether {@link #initialize()} has been invoked for this pool.
   *
   * @throws  IllegalStateException  if this pool has not been initialized
   */
  protected void throwIfNotInitialized()
  {
    if (!initialized) {
      throw new IllegalStateException("Pool has not been initialized");
    }
  }


  /**
   * Initialize this pool for use. Once invoked the pool config is made immutable. See {@link
   * PoolConfig#makeImmutable()}.
   *
   * @throws  IllegalStateException  if this pool has already been initialized, the pooling configuration is
   *                                 inconsistent or the pool does not contain at least one connection and it's minimum
   *                                 size is greater than zero
   */
  @Override
  public void initialize()
  {
    if (initialized) {
      throw new IllegalStateException("Pool has already been initialized");
    }
    logger.debug("beginning pool initialization for {}", this);

    // sanity check the configuration
    if ((getPoolConfig().isValidatePeriodically() ||
         getPoolConfig().isValidateOnCheckIn() ||
         getPoolConfig().isValidateOnCheckOut()) && getValidator() == null) {
      throw new IllegalStateException(
        "Validation is enabled, but no validator has been configured");
    }
    if ((!getPoolConfig().isValidatePeriodically() &&
         !getPoolConfig().isValidateOnCheckIn() &&
         !getPoolConfig().isValidateOnCheckOut()) && getValidator() != null) {
      throw new IllegalStateException(
        "Validator configured, but no validate flag has been set");
    }

    getPoolConfig().makeImmutable();

    if (getPruneStrategy() == null) {
      setPruneStrategy(new IdlePruneStrategy());
      logger.debug("no prune strategy configured, using default prune strategy: {}", getPruneStrategy());
    }

    available = new Queue<>(queueType);
    active = new Queue<>(queueType);

    IllegalStateException growException = null;
    try {
      grow(getPoolConfig().getMinPoolSize(), true);
    } catch (IllegalStateException e) {
      growException = e;
    }
    if (available.isEmpty() && getPoolConfig().getMinPoolSize() > 0) {
      if (failFastInitialize) {
        throw new IllegalStateException(
          "Could not initialize pool size",
          growException != null ? growException.getCause() : null);
      } else {
        logger.warn("Could not initialize pool size, pool is empty");
      }
    }
    logger.debug("initialized available queue: {}", available);

    poolExecutor = Executors.newSingleThreadScheduledExecutor(
      new ThreadFactory() {
        @Override
        public Thread newThread(final Runnable r)
        {
          final Thread t = new Thread(r);
          t.setDaemon(true);
          return t;
        }
      });

    poolExecutor.scheduleAtFixedRate(
      new Runnable() {
        @Override
        public void run()
        {
          logger.debug("begin prune task for {}", AbstractConnectionPool.this);
          try {
            prune();
          } catch (Exception e) {
            logger.error("prune task failed for {}", AbstractConnectionPool.this);
          }
          logger.debug("end prune task for {}", AbstractConnectionPool.this);
        }
      },
      getPruneStrategy().getPrunePeriod(),
      getPruneStrategy().getPrunePeriod(),
      TimeUnit.SECONDS);
    logger.debug("prune pool task scheduled for {}", this);

    poolExecutor.scheduleAtFixedRate(
      new Runnable() {
        @Override
        public void run()
        {
          logger.debug("begin validate task for {}", AbstractConnectionPool.this);
          try {
            validate();
          } catch (Exception e) {
            logger.error("validation task failed for {}", AbstractConnectionPool.this);
          }
          logger.debug("end validate task for {}", AbstractConnectionPool.this);
        }
      },
      getPoolConfig().getValidatePeriod(),
      getPoolConfig().getValidatePeriod(),
      TimeUnit.SECONDS);
    logger.debug("validate pool task scheduled for {}", this);

    initialized = true;
    logger.info("pool initialized {}", this);
  }


  /**
   * Attempts to grow the pool to the supplied size. If the pool size is greater than or equal to the supplied size,
   * this method is a no-op.
   *
   * @param  size  to grow the pool to
   */
  protected void grow(final int size)
  {
    grow(size, false);
  }


  /**
   * Attempts to grow the pool to the supplied size. If the pool size is greater than or equal to the supplied size,
   * this method is a no-op.
   *
   * @param  size  to grow the pool to
   * @param  throwOnFailure  whether to throw illegal state exception
   *
   * @throws  IllegalStateException  if the pool cannot grow to the supplied size and {@link
   *                                 #createAvailableConnection(boolean)} throws
   */
  protected void grow(final int size, final boolean throwOnFailure)
  {
    logger.trace("waiting for pool lock to initialize pool {}", poolLock.getQueueLength());

    int count = 0;
    poolLock.lock();
    try {
      IllegalStateException lastThrown = null;
      int currentPoolSize = active.size() + available.size();
      logger.debug("checking connection pool size >= {} for {}", size, this);
      while (currentPoolSize < size && count < size * 2) {
        try {
          final PooledConnectionProxy pc = createAvailableConnection(throwOnFailure);
          if (pc != null && getPoolConfig().isValidateOnCheckIn()) {
            if (validate(pc.getConnection())) {
              logger.trace("connection passed initialize validation: {}", pc);
            } else {
              logger.warn("connection failed initialize validation: {}", pc);
              removeAvailableConnection(pc);
            }
          }
        } catch (IllegalStateException e) {
          lastThrown = e;
        }
        currentPoolSize = active.size() + available.size();
        count++;
      }
      if (lastThrown != null && currentPoolSize < size) {
        throw lastThrown;
      }
    } finally {
      poolLock.unlock();
    }
  }


  /**
   * Empty this pool, freeing any resources.
   *
   * @throws  IllegalStateException  if this pool has not been initialized
   */
  @Override
  public void close()
  {
    throwIfNotInitialized();
    logger.debug("closing connection pool of size {} for {}", available.size() + active.size(), this);
    poolLock.lock();
    try {
      while (!available.isEmpty()) {
        final PooledConnectionProxy pc = available.remove();
        pc.getConnection().close();
        logger.trace("destroyed connection: {}", pc);
      }
      while (!active.isEmpty()) {
        final PooledConnectionProxy pc = active.remove();
        pc.getConnection().close();
        logger.trace("destroyed connection: {}", pc);
      }
      logger.debug("pool closed");
    } finally {
      poolLock.unlock();
    }

    logger.debug("shutting down executor");
    poolExecutor.shutdown();
    logger.debug("executor shutdown");
    logger.info("pool closed {}", this);
    initialized = false;
  }


  /**
   * Returns a connection from the pool.
   *
   * @return  connection
   *
   * @throws  PoolException  if this operation fails
   * @throws  BlockingTimeoutException  if this pool is configured with a block time and it occurs
   * @throws  PoolInterruptedException  if this pool is configured with a block time and the current thread is
   *                                    interrupted
   * @throws  IllegalStateException  if this pool has not been initialized
   */
  @Override
  public abstract Connection getConnection()
    throws PoolException;


  /**
   * Returns a connection to the pool.
   *
   * @param  c  connection
   *
   * @throws  IllegalStateException  if this pool has not been initialized
   */
  public abstract void putConnection(final Connection c);


  /**
   * Create a new connection. If {@link #connectOnCreate} is true, the connection will be opened.
   *
   * @return  pooled connection
   */
  protected PooledConnectionProxy createConnection()
  {
    return createConnection(false);
  }


  /**
   * Create a new connection. If {@link #connectOnCreate} is true, the connection will be opened.
   *
   * @param  throwOnFailure  whether to throw illegal state exception
   *
   * @return  pooled connection
   *
   * @throws  IllegalStateException  if {@link #connectOnCreate} is true and the connection cannot be opened
   */
  protected PooledConnectionProxy createConnection(final boolean throwOnFailure)
  {
    Connection c = connectionFactory.getConnection();
    Response<Void> r = null;
    if (connectOnCreate) {
      try {
        r = c.open();
      } catch (LdapException e) {
        logger.error("{} unable to connect to the ldap", this, e);
        c = null;
        if (throwOnFailure) {
          throw new IllegalStateException("unable to connect to the ldap", e);
        }
      }
    }
    if (c != null) {
      return new DefaultPooledConnectionProxy(c, r);
    } else {
      return null;
    }
  }


  /**
   * Create a new connection and place it in the available pool.
   *
   * @return  connection that was placed in the available pool
   */
  protected PooledConnectionProxy createAvailableConnection()
  {
    return createAvailableConnection(false);
  }


  /**
   * Create a new connection and place it in the available pool.
   *
   * @param  throwOnFailure  whether to throw illegal state exception
   *
   * @return  connection that was placed in the available pool
   *
   * @throws  IllegalStateException  if {@link #createConnection(boolean)} throws
   */
  protected PooledConnectionProxy createAvailableConnection(final boolean throwOnFailure)
  {
    final PooledConnectionProxy pc = createConnection(throwOnFailure);
    if (pc != null) {
      poolLock.lock();
      try {
        available.add(pc);
        pc.getPooledConnectionStatistics().addAvailableStat();
        logger.trace("added available connection: {}", pc);
      } finally {
        poolLock.unlock();
      }
    } else {
      logger.warn("unable to create available connection");
    }
    return pc;
  }


  /**
   * Create a new connection and place it in the active pool.
   *
   * @return  connection that was placed in the active pool
   */
  protected PooledConnectionProxy createActiveConnection()
  {
    return createActiveConnection(false);
  }


  /**
   * Create a new connection and place it in the active pool.
   *
   * @param  throwOnFailure  whether to throw illegal state exception
   *
   * @return  connection that was placed in the active pool
   *
   * @throws  IllegalStateException  if {@link #createConnection(boolean)} throws
   */
  protected PooledConnectionProxy createActiveConnection(final boolean throwOnFailure)
  {
    final PooledConnectionProxy pc = createConnection(throwOnFailure);
    if (pc != null) {
      poolLock.lock();
      try {
        active.add(pc);
        pc.getPooledConnectionStatistics().addActiveStat();
        logger.trace("added active connection: {}", pc);
      } finally {
        poolLock.unlock();
      }
    } else {
      logger.warn("unable to create active connection");
    }
    return pc;
  }


  /**
   * Remove a connection from the available pool.
   *
   * @param  pc  connection that is in the available pool
   */
  protected void removeAvailableConnection(final PooledConnectionProxy pc)
  {
    boolean destroy = false;
    poolLock.lock();
    try {
      if (available.remove(pc)) {
        destroy = true;
      } else {
        logger.warn("attempt to remove unknown available connection: {}", pc);
      }
    } finally {
      poolLock.unlock();
    }
    if (destroy) {
      pc.getConnection().close();
      logger.trace("destroyed connection: {}", pc);
    }
  }


  /**
   * Remove a connection from the active pool.
   *
   * @param  pc  connection that is in the active pool
   */
  protected void removeActiveConnection(final PooledConnectionProxy pc)
  {
    boolean destroy = false;
    poolLock.lock();
    try {
      if (active.remove(pc)) {
        destroy = true;
      } else {
        logger.warn("attempt to remove unknown active connection: {}", pc);
      }
    } finally {
      poolLock.unlock();
    }
    if (destroy) {
      pc.getConnection().close();
      logger.trace("destroyed connection: {}", pc);
    }
  }


  /**
   * Remove a connection from both the available and active pools.
   *
   * @param  pc  connection that is in both the available and active pools
   */
  protected void removeAvailableAndActiveConnection(final PooledConnectionProxy pc)
  {
    boolean destroy = false;
    poolLock.lock();
    try {
      if (available.remove(pc)) {
        destroy = true;
      } else {
        logger.debug("attempt to remove unknown available connection: {}", pc);
      }
      if (active.remove(pc)) {
        destroy = true;
      } else {
        logger.debug("attempt to remove unknown active connection: {}", pc);
      }
    } finally {
      poolLock.unlock();
    }
    if (destroy) {
      pc.getConnection().close();
      logger.trace("destroyed connection: {}", pc);
    }
  }


  /**
   * Attempts to activate and validate a connection. Performed before a connection is returned from {@link
   * #getConnection()}.
   *
   * @param  pc  connection
   *
   * @throws  PoolException  if this method fails
   * @throws  ActivationException  if the connection cannot be activated
   * @throws  ValidationException  if the connection cannot be validated
   */
  protected void activateAndValidateConnection(final PooledConnectionProxy pc)
    throws PoolException
  {
    if (!activate(pc.getConnection())) {
      logger.warn("connection failed activation: {}", pc);
      removeAvailableAndActiveConnection(pc);
      throw new ActivationException("Activation of connection failed");
    }
    if (getPoolConfig().isValidateOnCheckOut() && !validate(pc.getConnection())) {
      logger.warn("connection failed check out validation: {}", pc);
      removeAvailableAndActiveConnection(pc);
      throw new ValidationException("Validation of connection failed");
    }
  }


  /**
   * Attempts to validate and passivate a connection. Performed when a connection is given to {@link
   * #putConnection(Connection)}.
   *
   * @param  pc  connection
   *
   * @return  whether both validate and passivation succeeded
   */
  protected boolean validateAndPassivateConnection(final PooledConnectionProxy pc)
  {
    if (!pc.getConnection().isOpen()) {
      logger.debug("connection not open: {}", pc);
      return false;
    }

    boolean valid = false;
    if (getPoolConfig().isValidateOnCheckIn()) {
      if (!validate(pc.getConnection())) {
        logger.warn("connection failed check in validation: {}", pc);
      } else {
        valid = true;
      }
    } else {
      valid = true;
    }
    if (valid && !passivate(pc.getConnection())) {
      valid = false;
      logger.warn("connection failed passivation: {}", pc);
    }
    return valid;
  }


  /**
   * Attempts to reduce the size of the pool back to it's configured minimum. {@link PoolConfig#setMinPoolSize(int)}.
   *
   * @throws  IllegalStateException  if this pool has not been initialized
   */
  public void prune()
  {
    throwIfNotInitialized();
    logger.trace("waiting for pool lock to prune {}", poolLock.getQueueLength());
    poolLock.lock();
    try {
      if (!available.isEmpty()) {
        final int minPoolSize = getPoolConfig().getMinPoolSize();
        int currentPoolSize = active.size() + available.size();
        if (currentPoolSize > minPoolSize) {
          logger.debug("pruning available pool of size {} for {}", available.size(), this);

          final int numConnToPrune = available.size();
          final Iterator<PooledConnectionProxy> connIter = available.iterator();
          for (int i = 0; i < numConnToPrune && currentPoolSize > minPoolSize; i++) {
            final PooledConnectionProxy pc = connIter.next();
            if (getPruneStrategy().prune(pc)) {
              connIter.remove();
              pc.getConnection().close();
              logger.trace("destroyed connection: {}", pc);
              currentPoolSize--;
            }
          }
          if (numConnToPrune == available.size()) {
            logger.debug("prune strategy did not remove any connections");
          } else {
            logger.debug("available pool size pruned to {}", available.size());
          }
        } else {
          logger.debug("pool size is {}, no connections pruned for {}", currentPoolSize, this);
        }
      } else {
        logger.debug("no available connections, no connections pruned for {}", this);
      }
    } finally {
      poolLock.unlock();
    }
  }


  /**
   * Attempts to validate all objects in the pool. {@link PoolConfig#setValidatePeriodically(boolean)}.
   *
   * @throws  IllegalStateException  if this pool has not been initialized
   */
  public void validate()
  {
    throwIfNotInitialized();
    poolLock.lock();
    try {
      if (!available.isEmpty()) {
        if (getPoolConfig().isValidatePeriodically()) {
          logger.debug("validate available pool of size {} for {}", available.size(), this);

          final List<PooledConnectionProxy> remove = new ArrayList<>();
          for (PooledConnectionProxy pc : available) {
            logger.trace("validating {}", pc);
            if (validate(pc.getConnection())) {
              logger.trace("connection passed validation: {}", pc);
            } else {
              logger.warn("connection failed validation: {}", pc);
              remove.add(pc);
            }
          }
          for (PooledConnectionProxy pc : remove) {
            logger.trace("removing {} from the pool", pc);
            available.remove(pc);
            pc.getConnection().close();
            logger.trace("destroyed connection: {}", pc);
          }
        }
      } else {
        logger.debug("no available connections, no validation performed for {}", this);
      }
      grow(getPoolConfig().getMinPoolSize());
      logger.debug("pool size after validation is {}", available.size() + active.size());
    } finally {
      poolLock.unlock();
    }
  }


  @Override
  public int availableCount()
  {
    if (available == null) {
      return 0;
    }
    return available.size();
  }


  @Override
  public int activeCount()
  {
    if (active == null) {
      return 0;
    }
    return active.size();
  }


  @Override
  public Set<PooledConnectionStatistics> getPooledConnectionStatistics()
  {
    throwIfNotInitialized();

    final Set<PooledConnectionStatistics> stats = Collections.unmodifiableSet(
      new HashSet<PooledConnectionStatistics>());
    poolLock.lock();
    try {
      for (PooledConnectionProxy cp : available) {
        stats.add(cp.getPooledConnectionStatistics());
      }
      for (PooledConnectionProxy cp : active) {
        stats.add(cp.getPooledConnectionStatistics());
      }
    } finally {
      poolLock.unlock();
    }
    return stats;
  }


  /**
   * Creates a connection proxy using the supplied pool connection.
   *
   * @param  pc  pool connection to create proxy with
   *
   * @return  connection proxy
   */
  protected Connection createConnectionProxy(final PooledConnectionProxy pc)
  {
    return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[] {Connection.class}, pc);
  }


  /**
   * Retrieves the invocation handler from the supplied connection proxy.
   *
   * @param  proxy  connection proxy
   *
   * @return  pooled connection proxy
   */
  protected PooledConnectionProxy retrieveConnectionProxy(final Connection proxy)
  {
    return (PooledConnectionProxy) Proxy.getInvocationHandler(proxy);
  }


  /**
   * Called by the garbage collector on an object when garbage collection determines that there are no more references
   * to the object.
   *
   * @throws  Throwable  if an exception is thrown by this method
   */
  @Override
  protected void finalize()
    throws Throwable
  {
    try {
      close();
    } finally {
      super.finalize();
    }
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::name=%s, poolConfig=%s, activator=%s, passivator=%s, " +
        "validator=%s pruneStrategy=%s, connectOnCreate=%s, " +
        "connectionFactory=%s, initialized=%s, availableCount=%s, " +
        "activeCount=%s]",
        getClass().getName(),
        hashCode(),
        getName(),
        getPoolConfig(),
        getActivator(),
        getPassivator(),
        getValidator(),
        getPruneStrategy(),
        connectOnCreate,
        connectionFactory,
        initialized,
        availableCount(),
        activeCount());
  }


  /**
   * Contains a connection that is participating in this pool. Used to track how long a connection has been in use and
   * override certain method invocations.
   */
  protected class DefaultPooledConnectionProxy implements PooledConnectionProxy
  {

    /** hash code seed. */
    private static final int HASH_CODE_SEED = 503;

    /** Underlying connection. */
    private final Connection conn;

    /** Response produced when the connection was opened. */
    private Response<Void> openResponse;

    /** Time this connection was created. */
    private final long createdTime = System.currentTimeMillis();

    /** Statistics for this connection. */
    private final PooledConnectionStatistics statistics = new PooledConnectionStatistics(
      getPruneStrategy().getStatisticsSize());


    /**
     * Creates a new pooled connection.
     *
     * @param  c  connection to participate in this pool
     * @param  r  response produced by opening the connection
     */
    public DefaultPooledConnectionProxy(final Connection c, final Response<Void> r)
    {
      conn = c;
      openResponse = r;
    }


    @Override
    public ConnectionPool getConnectionPool()
    {
      return AbstractConnectionPool.this;
    }


    @Override
    public Connection getConnection()
    {
      return conn;
    }


    @Override
    public long getCreatedTime()
    {
      return createdTime;
    }


    @Override
    public PooledConnectionStatistics getPooledConnectionStatistics()
    {
      return statistics;
    }


    @Override
    public boolean equals(final Object o)
    {
      return LdapUtils.areEqual(this, o);
    }


    @Override
    public int hashCode()
    {
      return LdapUtils.computeHashCode(HASH_CODE_SEED, conn);
    }


    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(final Object proxy, final Method method, final Object[] args)
      throws Throwable
    {
      Object retValue = null;
      if ("open".equals(method.getName())) {
        // if the connection has been closed, invoke open
        if (!conn.isOpen()) {
          try {
            openResponse = (Response<Void>) method.invoke(conn, args);
          } catch (InvocationTargetException e) {
            throw e.getTargetException();
          }
        }
        retValue = openResponse;
      } else if ("reopen".equals(method.getName())) {
        try {
          openResponse = (Response<Void>) method.invoke(conn, args);
        } catch (InvocationTargetException e) {
          throw e.getTargetException();
        }
        retValue = openResponse;
      } else if ("close".equals(method.getName())) {
        putConnection((Connection) proxy);
      } else {
        try {
          retValue = method.invoke(conn, args);
        } catch (InvocationTargetException e) {
          throw e.getTargetException();
        }
      }
      return retValue;
    }
  }
}
