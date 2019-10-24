/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import org.ldaptive.Connection;
import org.ldaptive.ConnectionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains functionality common to pool implementations.
 *
 * @author  Middleware Services
 */
public abstract class AbstractPool
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Pool name. */
  private String name;

  /** Pool config. */
  private PoolConfig poolConfig;

  /** For activating pooled objects. */
  private ConnectionActivator activator;

  /** For passivating pooled objects. */
  private ConnectionPassivator passivator;

  /** For validating pooled objects. */
  private ConnectionValidator validator;

  /** For removing pooled objects. */
  private PruneStrategy pruneStrategy;


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
   * Returns the configuration for this pool.
   *
   * @return  pool config
   */
  public PoolConfig getPoolConfig()
  {
    return poolConfig;
  }


  /**
   * Sets the configuration for this pool.
   *
   * @param  pc  pool config
   */
  public void setPoolConfig(final PoolConfig pc)
  {
    logger.trace("setting poolConfig: {}", pc);
    poolConfig = pc;
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
   * Prepare the connection to exit the pool for use.
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
   * Prepare the connection to reenter the pool after use.
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
    if (validator == null) {
      success = true;
      logger.warn("validate called, but no validator strategy configured");
    } else {
      try {
        success = validator.apply(conn);
      } catch (Exception e) {
        logger.warn("validate threw exception", e);
      }
      logger.trace("validation for {} = {}", conn, success);
    }
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
