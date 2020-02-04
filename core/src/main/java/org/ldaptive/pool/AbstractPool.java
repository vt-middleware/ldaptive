/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import org.ldaptive.Connection;
import org.ldaptive.ConnectionValidator;
import org.ldaptive.SearchConnectionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains functionality common to pool implementations.
 *
 * @author  Middleware Services
 */
public abstract class AbstractPool
{

  /** Default min pool size, value is {@value}. */
  public static final int DEFAULT_MIN_POOL_SIZE = 3;

  /** Default max pool size, value is {@value}. */
  public static final int DEFAULT_MAX_POOL_SIZE = 10;

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Pool name. */
  private String name;

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
  private ConnectionActivator activator;

  /** For passivating connections. */
  private ConnectionPassivator passivator;

  /** For validating connections. */
  private ConnectionValidator validator = new SearchConnectionValidator();

  /** For removing connections. */
  private PruneStrategy pruneStrategy = new IdlePruneStrategy();


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
   * @param  size  min pool size
   */
  public void setMinPoolSize(final int size)
  {
    if (size < 0) {
      throw new IllegalArgumentException("Minimum pool size must be greater than 0");
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
   * @param  size  max pool size
   */
  public void setMaxPoolSize(final int size)
  {
    if (size < 0) {
      throw new IllegalArgumentException("Maximum pool size must be greater than 0");
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
   * Prepare the connection to exit the pool for use. No-op if no activator is configured.
   *
   * @param  conn pooled connection
   *
   * @return  whether the connection successfully activated
   */
  public boolean activate(final Connection conn)
  {
    boolean success = false;
    if (activator == null) {
      success = true;
      logger.trace("no activator configured");
    } else {
      try {
        success = activator.apply(conn);
      } catch (Exception e) {
        logger.warn("activate threw exception", e);
      }
      logger.trace("activation for {} = {}", conn, success);
    }
    return success;
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
   * Prepare the connection to reenter the pool after use. No-op if no passivator is configured.
   *
   * @param  conn  pooled connection
   *
   * @return  whether the connection successfully passivated
   */
  public boolean passivate(final Connection conn)
  {
    boolean success = false;
    if (passivator == null) {
      success = true;
      logger.trace("no passivator configured");
    } else {
      try {
        success = passivator.apply(conn);
      } catch (Exception e) {
        logger.warn("passivate threw exception", e);
      }
      logger.trace("passivation for {} = {}", conn, success);
    }
    return success;
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
    logger.trace("setting validatorStrategy: {}", cv);
    validator = cv;
  }


  /**
   * Verify the connection is still viable for use in the pool.
   *
   * @param  conn  pooled connection
   *
   * @return  whether the connection is viable
   */
  public boolean validate(final Connection conn)
  {
    boolean success = false;
    try {
      success = validator.apply(conn);
    } catch (Exception e) {
      logger.warn("validate threw exception", e);
    }
    logger.trace("validation for {} = {}", conn, success);
    return success;
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
}
