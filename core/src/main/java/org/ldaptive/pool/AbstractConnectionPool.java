/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionValidator;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchConnectionValidator;
import org.ldaptive.concurrent.CallableWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public abstract class AbstractConnectionPool implements ConnectionPool
{

  /** Default min pool size, value is {@value}. */
  public static final int DEFAULT_MIN_POOL_SIZE = 3;

  /** Default max pool size, value is {@value}. */
  public static final int DEFAULT_MAX_POOL_SIZE = 10;

  /** ID used for pool name. */
  private static final AtomicInteger POOL_ID = new AtomicInteger();

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Lock for the entire pool. */
  protected final ReentrantLock poolLock = new ReentrantLock();

  /** Condition for notifying threads that a connection was returned. */
  protected final Condition poolNotEmpty = poolLock.newCondition();

  /** Lock for check outs. */
  protected final ReentrantLock checkOutLock = new ReentrantLock();

  /** List of available connections in the pool. */
  protected Queue<PooledConnectionProxy> available;

  /** List of connections in use. */
  protected Queue<PooledConnectionProxy> active;

  /** Pool name. */
  private String name = "ldaptive-pool-" + POOL_ID.incrementAndGet();

  /** Minimum pool size. */
  private int minPoolSize = DEFAULT_MIN_POOL_SIZE;

  /** Maximum pool size. */
  private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;

  /** Whether the ldap connection should be validated when returned to the pool. */
  private boolean validateOnCheckIn;

  /** Whether the ldap connection should be validated when given from the pool. */
  private boolean validateOnCheckOut;

  /** Whether the pool should be validated periodically. */
  private boolean validatePeriodically;

  /** For activating connections. */
  private ConnectionActivator activator = new ConnectionActivator() {
    @Override
    public Boolean apply(final Connection conn)
    {
      return true;
    }

    @Override
    public String toString()
    {
      return "DEFAULT_ACTIVATOR";
    }
  };

  /** For passivating connections. */
  private ConnectionPassivator passivator = new ConnectionPassivator() {
    @Override
    public Boolean apply(final Connection conn)
    {
      return true;
    }

    @Override
    public String toString()
    {
      return "DEFAULT_PASSIVATOR";
    }
  };

  /** For validating connections. */
  private ConnectionValidator validator = new SearchConnectionValidator();

  /** For removing connections. */
  private PruneStrategy pruneStrategy = new IdlePruneStrategy();

  /** Connection factory to create connections with. */
  private DefaultConnectionFactory connectionFactory;

  /** Whether to connect to the ldap on connection creation. */
  private boolean connectOnCreate = true;

  /** Type of queue. LIFO or FIFO. */
  private QueueType queueType = QueueType.LIFO;

  /** Executor for scheduling pool tasks. */
  private ScheduledExecutorService poolExecutor;

  /** Whether {@link #initialize()} has been successfully invoked. */
  private boolean initialized;

  /** Whether {@link #initialize()} should throw if pooling configuration requirements are not met. */
  private boolean failFastInitialize = true;


  /**
   * Returns the name for this pool.
   *
   * @return  pool name
   */
  public String getName()
  {
    return name;
  }


  /**
   * Sets the name for this pool.
   *
   * @param  s  pool name
   */
  public void setName(final String s)
  {
    logger.trace("setting name: {}", s);
    name = s;
  }


  /**
   * Returns the min pool size. Default value is {@link #DEFAULT_MIN_POOL_SIZE}. This value represents the size of the
   * pool after a prune has occurred.
   *
   * @return  min pool size
   */
  public int getMinPoolSize()
  {
    return minPoolSize;
  }


  /**
   * Sets the min pool size.
   *
   * @param  size  min pool size, greater than or equal to zero
   */
  public void setMinPoolSize(final int size)
  {
    if (size < 0) {
      throw new IllegalArgumentException("Minimum pool size must be greater than or equal to 0 for pool " + getName());
    }
    logger.trace("setting minPoolSize: {}", size);
    minPoolSize = size;
  }


  /**
   * Returns the max pool size. Default value is {@link #DEFAULT_MAX_POOL_SIZE}. This value may or may not be strictly
   * enforced depending on the pooling implementation.
   *
   * @return  max pool size
   */
  public int getMaxPoolSize()
  {
    return maxPoolSize;
  }


  /**
   * Sets the max pool size.
   *
   * @param  size  max pool size, greater than or equal to zero
   */
  public void setMaxPoolSize(final int size)
  {
    // allow a max size of zero for configurations that need to create a pool but don't want it to function
    if (size < 0) {
      throw new IllegalArgumentException("Maximum pool size must be greater than or equal to 0 for pool " + getName());
    }
    logger.trace("setting maxPoolSize: {}", size);
    maxPoolSize = size;
  }


  /**
   * Returns the validate on check in flag.
   *
   * @return  validate on check in
   */
  public boolean isValidateOnCheckIn()
  {
    return validateOnCheckIn;
  }


  /**
   * Sets the validate on check in flag.
   *
   * @param  b  validate on check in
   */
  public void setValidateOnCheckIn(final boolean b)
  {
    logger.trace("setting validateOnCheckIn: {}", b);
    validateOnCheckIn = b;
  }


  /**
   * Returns the validate on check out flag.
   *
   * @return  validate on check in
   */
  public boolean isValidateOnCheckOut()
  {
    return validateOnCheckOut;
  }


  /**
   * Sets the validate on check out flag.
   *
   * @param  b  validate on check out
   */
  public void setValidateOnCheckOut(final boolean b)
  {
    logger.trace("setting validateOnCheckOut: {}", b);
    validateOnCheckOut = b;
  }


  /**
   * Returns the validate periodically flag.
   *
   * @return  validate periodically
   */
  public boolean isValidatePeriodically()
  {
    return validatePeriodically;
  }


  /**
   * Sets the validate periodically flag.
   *
   * @param  b  validate periodically
   */
  public void setValidatePeriodically(final boolean b)
  {
    logger.trace("setting validatePeriodically: {}", b);
    validatePeriodically = b;
  }


  /**
   * Returns the activator for this pool.
   *
   * @return  activator
   */
  public ConnectionActivator getActivator()
  {
    return activator;
  }


  /**
   * Sets the activator for this pool.
   *
   * @param  a  activator
   */
  public void setActivator(final ConnectionActivator a)
  {
    logger.trace("setting activator: {}", a);
    activator = a;
  }


  /**
   * Returns the passivator for this pool.
   *
   * @return  passivator
   */
  public ConnectionPassivator getPassivator()
  {
    return passivator;
  }


  /**
   * Sets the passivator for this pool.
   *
   * @param  p  passivator
   */
  public void setPassivator(final ConnectionPassivator p)
  {
    logger.trace("setting passivator: {}", p);
    passivator = p;
  }


  /**
   * Returns the connection validator for this pool.
   *
   * @return  connection validator
   */
  public ConnectionValidator getValidator()
  {
    return validator;
  }


  /**
   * Sets the connection validator for this pool.
   *
   * @param  cv  connection validator
   */
  public void setValidator(final ConnectionValidator cv)
  {
    logger.trace("setting validator: {}", cv);
    validator = cv;
  }


  /**
   * Returns the prune strategy for this pool.
   *
   * @return  prune strategy
   */
  public PruneStrategy getPruneStrategy()
  {
    return pruneStrategy;
  }


  /**
   * Sets the prune strategy for this pool.
   *
   * @param  ps  prune strategy
   */
  public void setPruneStrategy(final PruneStrategy ps)
  {
    logger.trace("setting pruneStrategy: {}", ps);
    pruneStrategy = ps;
  }


  /**
   * Returns the connection factory for this pool.
   *
   * @return  connection factory
   */
  public DefaultConnectionFactory getDefaultConnectionFactory()
  {
    return connectionFactory;
  }


  /**
   * Sets the connection factory for this pool.
   *
   * @param  cf  connection factory
   */
  public void setDefaultConnectionFactory(final DefaultConnectionFactory cf)
  {
    logger.trace("setting defaultConnectionFactory: {}", cf);
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
      throw new IllegalStateException("Pool " + getName() + " is not initialized");
    }
  }


  /**
   * Initialize this pool for use.
   *
   * @throws  IllegalStateException  if this pool has already been initialized, the pooling configuration is
   *                                 inconsistent or the pool does not contain at least one connection and its minimum
   *                                 size is greater than zero
   */
  @Override
  public synchronized void initialize()
  {
    if (initialized) {
      throw new IllegalStateException("Pool " + getName() + " has already been initialized");
    }
    logger.debug("Beginning pool initialization for {}", this);

    if (pruneStrategy == null) {
      throw new IllegalStateException("No prune strategy configured for pool " + getName());
    }
    if (activator == null) {
      throw new IllegalStateException("No activator configured for pool " + getName());
    }
    if (passivator == null) {
      throw new IllegalStateException("No passivator configured for pool " + getName());
    }

    available = new Queue<>(queueType);
    active = new Queue<>(queueType);

    IllegalStateException growException = null;
    try {
      grow(minPoolSize, true);
    } catch (IllegalStateException e) {
      growException = e;
    }
    if (available.isEmpty() && minPoolSize > 0) {
      if (failFastInitialize) {
        closeAllConnections();
        throw new IllegalStateException(
          "Could not initialize pool size for pool " + getName(),
          growException != null ? growException.getCause() : null);
      } else {
        logger.warn("Could not initialize pool size (pool is empty) for {}", this);
      }
    }
    logger.debug("Initialized available queue {} for {}", available, this);

    final String threadPoolName = name != null ? name + "-" + getClass().getSimpleName() : getClass().getSimpleName();
    poolExecutor = Executors.newSingleThreadScheduledExecutor(
      r -> {
        final Thread t = new Thread(r, "ldaptive-" + threadPoolName + "@" + hashCode());
        t.setDaemon(true);
        return t;
      });

    poolExecutor.scheduleAtFixedRate(
      () -> {
        logger.debug("Begin prune task for {}", AbstractConnectionPool.this);
        try {
          prune();
        } catch (Exception e) {
          logger.warn("Prune task failed for {}", AbstractConnectionPool.this);
        }
        logger.debug("End prune task for {}", AbstractConnectionPool.this);
      },
      pruneStrategy.getPrunePeriod().toMillis(),
      pruneStrategy.getPrunePeriod().toMillis(),
      TimeUnit.MILLISECONDS);
    logger.debug("Prune pool task scheduled for {}", this);

    if (validatePeriodically) {
      poolExecutor.scheduleAtFixedRate(
        () -> {
          logger.debug("Begin validate task for {}", AbstractConnectionPool.this);
          try {
            validate();
          } catch (Exception e) {
            logger.warn("Validation task failed for {}", AbstractConnectionPool.this);
          }
          logger.debug("End validate task for {}", AbstractConnectionPool.this);
        },
        validator.getValidatePeriod().toMillis(),
        validator.getValidatePeriod().toMillis(),
        TimeUnit.MILLISECONDS);
      logger.debug("Validate pool task scheduled for {}", this);
    }

    initialized = true;
    logger.info("Pool initialized for {}", this);
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
    if (checkOutLock.tryLock()) {
      try {
        logger.trace("waiting for pool lock to initialize pool {}", poolLock.getQueueLength());
        poolLock.lock();
        try {
          final int currentPoolSize = active.size() + available.size();
          logger.debug("Checking connection pool size >= {} for {}", size, this);

          final int numConnsToAdd = size - currentPoolSize;
          if (numConnsToAdd > 0) {
            createAvailableConnections(numConnsToAdd, throwOnFailure);
          } else {
            logger.debug(
              "Current pool size {} exceeds requested size {}, grow not performed for {}", currentPoolSize, size, this);
          }
          logger.debug("Pool size after grow is {} for {}", available.size() + active.size(), this);
        } finally {
          poolLock.unlock();
        }
      } finally {
        checkOutLock.unlock();
      }
    } else {
      logger.debug("Grow no-op, checkout is creating a connection for {}", this);
    }
  }


  /**
   * Empty this pool, freeing any resources.
   *
   * @throws  IllegalStateException  if this pool has not been initialized
   */
  @Override
  public synchronized void close()
  {
    throwIfNotInitialized();
    logger.debug("Closing {} of size {}", this, available.size() + active.size());
    poolLock.lock();
    try {
      closeAllConnections();
    } finally {
      poolLock.unlock();
    }

    poolExecutor.shutdown();
    logger.info("Pool {} closed", this);
    initialized = false;
  }


  /**
   * Closes all connections in the pool.
   */
  private synchronized void closeAllConnections()
  {
    poolLock.lock();
    try {
      final List<Callable<PooledConnectionProxy>> removeConns = new ArrayList<>(available.size() + active.size());
      while (!available.isEmpty()) {
        final PooledConnectionProxy pc = available.remove();
        removeConns.add(() -> {
          pc.getConnection().close();
          return pc;
        });
      }
      while (!active.isEmpty()) {
        final PooledConnectionProxy pc = active.remove();
        removeConns.add(() -> {
          pc.getConnection().close();
          return pc;
        });
      }

      if (!removeConns.isEmpty()) {
        final CallableWorker<PooledConnectionProxy> callableWorker = new CallableWorker<>(getClass().getSimpleName());
        try {
          final List<ExecutionException> exceptions = callableWorker.execute(
            removeConns,
            pc -> logger.trace("removed {} from {}", pc, AbstractConnectionPool.this));
          for (ExecutionException e : exceptions) {
            logger.warn("Error closing connection for {}", this, e.getCause() != null ? e.getCause() : e);
          }
        } finally {
          callableWorker.shutdown();
        }
      }
    } finally {
      poolLock.unlock();
    }
  }


  /**
   * Returns a connection from the pool.
   *
   * @return  connection
   *
   * @throws  PoolException  if this operation fails
   * @throws  BlockingTimeoutException  if this pool is configured with a block time and it occurs
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
  public abstract void putConnection(Connection c);


  /**
   * Create a new connection. If {@link #connectOnCreate} is true, the connection will be opened.
   *
   * @param  throwOnFailure  whether to throw illegal state exception
   *
   * @return  pooled connection or null
   *
   * @throws  IllegalStateException  if {@link #connectOnCreate} is true and the connection cannot be opened
   */
  protected PooledConnectionProxy createConnection(final boolean throwOnFailure)
  {
    Connection c = connectionFactory.getConnection();
    if (connectOnCreate) {
      try {
        c.open();
      } catch (Exception e) {
        logger.warn("Unable to open connection for {}}", this, e);
        c.close();
        c = null;
        if (throwOnFailure) {
          throw new IllegalStateException("Unable to open connection for pool " + getName(), e);
        }
      }
    }
    if (c != null) {
      return new DefaultPooledConnectionProxy(c);
    } else {
      return null;
    }
  }


  /**
   * Asynchronously creates new connections and adds them to the available queue if the connection can be successfully
   * passivated and validated. See {@link #passivateAndValidateConnection(PooledConnectionProxy)}. This method can make
   * up to (count * 2) attempts in a best effort to create the number of connections requested.
   *
   * @param  count  number of connections to attempt to create
   * @param  throwOnFailure  whether to throw illegal state exception on any connection creation failure
   *
   * @throws  IllegalStateException  if throwOnFailure is true and count connections are not successfully created
   */
  protected void createAvailableConnections(final int count, final boolean throwOnFailure)
  {
    poolLock.lock();
    try {
      final CallableWorker<PooledConnectionProxy> callableWorker = new CallableWorker<>(getClass().getSimpleName());
      try {
        final AtomicInteger createdCount = new AtomicInteger();
        final List<ExecutionException> exceptions = callableWorker.execute(
          () -> {
            PooledConnectionProxy pc = null;
            int i = 0;
            // make two attempts on each thread to open a connection
            while (pc == null && i < 2) {
              try {
                pc = createConnection(true);
                if (pc != null && connectOnCreate) {
                  if (!passivateAndValidateConnection(pc)) {
                    pc.getConnection().close();
                    pc = null;
                  }
                }
              } catch (IllegalStateException e) {
                if (i == 1) {
                  throw e;
                }
                pc = null;
              }
              i++;
            }
            return pc;
          },
          count,
          pc -> {
            if (pc != null) {
              available.add(pc);
              pc.getPooledConnectionStatistics().addAvailableStat();
              logger.info("Added available connection {} for {}", pc.getConnection(), this);
              createdCount.incrementAndGet();
            }
          });
        if (createdCount.get() < count && throwOnFailure) {
          if (!exceptions.isEmpty()) {
            final ExecutionException e = exceptions.get(0);
            if (e.getCause() instanceof IllegalStateException) {
              throw (IllegalStateException) e.getCause();
            } else {
              throw new IllegalStateException(e.getCause() == null ? e : e.getCause());
            }
          } else {
            throw new IllegalStateException("Could not create the requested number of connections");
          }
        }
      } finally {
        callableWorker.shutdown();
      }
    } finally {
      poolLock.unlock();
    }
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
        logger.info("Added available connection {} for {}", pc.getConnection(), this);
      } finally {
        poolLock.unlock();
      }
    } else {
      logger.warn("Unable to create available connection for {}", this);
    }
    return pc;
  }


  /**
   * Create a new connection and place it in the active queue. This method creates the connection and then attempts to
   * acquire the pool lock in order to add the connection to the active queue. Therefore, this method can be invoked
   * both with and without acquiring the pool lock.
   *
   * @param  throwOnFailure  whether to throw illegal state exception on connection creation failure
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
        logger.info("Added active connection {} for {}", pc.getConnection(), this);
      } finally {
        poolLock.unlock();
      }
    } else {
      logger.warn("Unable to create active connection for {}", this);
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
        logger.warn("Attempt to remove unknown available connection {} from {}", pc.getConnection(), this);
      }
    } finally {
      poolLock.unlock();
    }
    if (destroy) {
      pc.getConnection().close();
      logger.info("Removed {} from {}", pc.getConnection(), this);
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
        logger.warn("Attempt to remove unknown active connection {} from {}", pc.getConnection(), this);
      }
    } finally {
      poolLock.unlock();
    }
    if (destroy) {
      pc.getConnection().close();
      logger.info("Removed {} from {}", pc.getConnection(), this);
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
      }
      if (active.remove(pc)) {
        destroy = true;
      }
    } finally {
      poolLock.unlock();
    }
    if (destroy) {
      pc.getConnection().close();
      logger.info("Removed {} from {}", pc.getConnection(), this);
    }
  }


  /**
   * Attempts to activate and validate a connection. Performed before a connection is returned from {@link
   * #getConnection()}. Validation only occurs if {@link #validateOnCheckOut} is true. If a connection fails either
   * activation or validation it is removed from the pool.
   *
   * @param  pc  connection
   *
   * @throws  PoolException  if either activation or validation fails
   */
  protected void activateAndValidateConnection(final PooledConnectionProxy pc)
    throws PoolException
  {
    if (!activator.apply(pc.getConnection())) {
      logger.warn("Failed activation on {} with {} for {}", pc.getConnection(), activator, this);
      removeAvailableAndActiveConnection(pc);
      throw new ActivationException("Activation of connection failed for pool " + getName());
    }
    if (validateOnCheckOut && !validator.apply(pc.getConnection())) {
      logger.warn("Failed check out validation on {} with {} for {}", pc.getConnection(), validator, this);
      removeAvailableAndActiveConnection(pc);
      throw new ValidationException("Validation of connection failed for pool " + getName());
    }
  }


  /**
   * Attempts to passivate and validate a connection. Performed when a connection is given to {@link
   * #putConnection(Connection)} and when a new connection enters the pool. Validation only occurs if {@link
   * #validateOnCheckIn} is true.
   *
   * @param  pc  connection
   *
   * @return  whether both passivation and validation succeeded
   */
  protected boolean passivateAndValidateConnection(final PooledConnectionProxy pc)
  {
    if (!pc.getConnection().isOpen()) {
      logger.warn("Failed validation on {} for {}, not open", pc.getConnection(), this);
      return false;
    }

    boolean valid = false;
    if (passivator.apply(pc.getConnection())) {
      if (validateOnCheckIn) {
        if (validator.apply(pc.getConnection())) {
          logger.trace("connection {} passed initialize validation", pc);
          valid = true;
        } else {
          logger.warn("Failed check in validation on {} with {} for {}", pc.getConnection(), validator, this);
        }
      } else {
        valid = true;
      }
    } else {
      logger.warn("Failed passivation on {} with {} for {}", pc.getConnection(), passivator, this);
    }
    return valid;
  }


  /**
   * Attempts to reduce the size of the pool back to its configured minimum.
   *
   * @throws  IllegalStateException  if this pool has not been initialized
   */
  public void prune()
  {
    throwIfNotInitialized();
    logger.trace("waiting for pool lock to prune {} for {}", poolLock.getQueueLength(), this);
    poolLock.lock();
    try {
      if (!available.isEmpty()) {
        final int currentPoolSize = active.size() + available.size();
        if (currentPoolSize > minPoolSize) {
          logger.debug("Pruning available pool of size {} for {}", available.size(), this);

          final int numConnAboveMin = currentPoolSize - minPoolSize;
          final int numConnToPrune = available.size() < numConnAboveMin ? available.size() : numConnAboveMin;
          final List<Callable<PooledConnectionProxy>> callables = new ArrayList<>(numConnToPrune);
          for (PooledConnectionProxy pc : available) {
            logger.trace("pruning {} for {}", pc, this);
            callables.add(() -> {
              if (pruneStrategy.apply(pc)) {
                logger.trace("prune approved on {} with {} for {}", pc, pruneStrategy, AbstractConnectionPool.this);
                return pc;
              }
              logger.trace("prune denied on {} with {} for {}", pc, pruneStrategy, AbstractConnectionPool.this);
              return null;
            });
          }

          final AtomicInteger numConnPruned = new AtomicInteger();
          final CallableWorker<PooledConnectionProxy> callableWorker = new CallableWorker<>(getClass().getSimpleName());
          try {
            final List<ExecutionException> exceptions = callableWorker.execute(
              callables,
              pc -> {
                if (pc != null) {
                  if (numConnPruned.get() < numConnToPrune) {
                    logger.trace("prune removing {} from {}", pc, this);
                    available.remove(pc);
                    pc.getConnection().close();
                    logger.trace("prune removed {} from {}", pc, this);
                    numConnPruned.getAndIncrement();
                  } else {
                    logger.trace("prune ignored {} from {}", pc, this);
                  }
                }
              });
            for (ExecutionException e : exceptions) {
              logger.warn("Error pruning connection for {}", this, e.getCause() != null ? e.getCause() : e);
            }
          } finally {
            callableWorker.shutdown();
          }
          if (numConnToPrune == available.size()) {
            logger.debug("Prune strategy did not remove any connections for {}", this);
          } else {
            logger.info("Available pool size pruned to {} for {}", available.size(), this);
          }
        } else {
          logger.debug("Pool size is {}, no connections pruned for {}", currentPoolSize, this);
        }
      } else {
        logger.debug("No available connections, no connections pruned for {}", this);
      }
    } finally {
      poolLock.unlock();
    }
  }


  /**
   * Attempts to validate all connections in the pool.
   *
   * @throws  IllegalStateException  if this pool has not been initialized
   */
  public void validate()
  {
    throwIfNotInitialized();
    poolLock.lock();
    try {
      if (!available.isEmpty()) {
        logger.debug("Validate available pool of size {} for {}", available.size(), this);

        final List<PooledConnectionProxy> remove = new ArrayList<>();
        final Map<PooledConnectionProxy, Supplier<Boolean>> results = new HashMap<>(available.size());
        for (PooledConnectionProxy pc : available) {
          logger.trace("validating {} for {}", pc, this);
          results.put(pc, validator.applyAsync(pc.getConnection()));
        }
        for (Map.Entry<PooledConnectionProxy, Supplier<Boolean>> entry : results.entrySet()) {
          // blocks until a result is received
          final Boolean validateResult = entry.getValue().get();
          if (validateResult != null && validateResult) {
            logger.trace("passed validation on {} with {} for {}", entry.getKey(), validator, this);
          } else {
            logger.warn(
              "Failed validation on {} with {} for {}, {}",
              entry.getKey().getConnection(),
              validator,
              this,
              validateResult == null ? "validator timeout exceeded" : "validator returned false");
            remove.add(entry.getKey());
          }
        }
        for (PooledConnectionProxy pc : remove) {
          logger.trace("validate removing {} from {}", pc, this);
          available.remove(pc);
          pc.getConnection().close();
          logger.trace("validate removed {} from {}", pc, this);
        }
      } else {
        logger.debug("No available connections, no validation performed for {}", this);
      }
      grow(minPoolSize, false);
      logger.debug("Pool size after validation is {} for {}", available.size() + active.size(), this);
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

    final Set<PooledConnectionStatistics> stats = new HashSet<>();
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
    return Collections.unmodifiableSet(stats);
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


  @Override
  public String toString()
  {
    return getClass().getName() + "@" + hashCode() + "::" +
      "name=" + getName() + ", " +
      "minPoolSize=" + minPoolSize + ", " +
      "maxPoolSize=" + maxPoolSize + ", " +
      "validateOnCheckIn=" + validateOnCheckIn + ", " +
      "validateOnCheckOut=" + validateOnCheckOut + ", " +
      "validatePeriodically=" + validatePeriodically + ", " +
      "activator=" + activator + ", " +
      "passivator=" + passivator + ", " +
      "validator=" + validator + ", " +
      "pruneStrategy=" + pruneStrategy + ", " +
      "connectOnCreate=" + connectOnCreate + ", " +
      "connectionFactory=" + connectionFactory + ", " +
      "failFastInitialize=" + failFastInitialize + ", " +
      "initialized=" + initialized + ", " +
      "availableCount=" + availableCount() + ", " +
      "activeCount=" + activeCount();
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

    /** Time this connection was created. */
    private final long createdTime = System.currentTimeMillis();

    /** Statistics for this connection. */
    private final PooledConnectionStatistics statistics = new PooledConnectionStatistics(
      pruneStrategy.getStatisticsSize());


    /**
     * Creates a new pooled connection.
     *
     * @param  c  connection to participate in this pool
     */
    public DefaultPooledConnectionProxy(final Connection c)
    {
      conn = c;
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
      if (o == this) {
        return true;
      }
      if (o instanceof DefaultPooledConnectionProxy) {
        final DefaultPooledConnectionProxy v = (DefaultPooledConnectionProxy) o;
        return LdapUtils.areEqual(conn, v.conn);
      }
      return false;
    }


    @Override
    public int hashCode()
    {
      return LdapUtils.computeHashCode(HASH_CODE_SEED, conn);
    }


    @Override
    public String toString()
    {
      return getClass().getName() + "@" + hashCode() + "::" +
        "conn=" + conn + ", " +
        "createdTime=" + createdTime + ", " +
        "statistics=" + statistics;
    }


    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
      throws Throwable
    {
      Object retValue = null;
      if ("open".equals(method.getName())) {
        // if the connection has been closed, invoke open
        if (!conn.isOpen()) {
          try {
            retValue = method.invoke(conn, args);
          } catch (InvocationTargetException e) {
            throw e.getTargetException();
          }
        }
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
