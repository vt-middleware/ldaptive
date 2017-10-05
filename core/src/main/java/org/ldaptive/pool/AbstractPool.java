/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains functionality common to pool implementations.
 *
 * @param  <T>  type of object being pooled
 *
 * @author  Middleware Services
 */
public abstract class AbstractPool<T>
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Pool name. */
  private String name;

  /** Pool config. */
  private PoolConfig poolConfig;

  /** For activating pooled objects. */
  private Activator<T> activator;

  /** For passivating pooled objects. */
  private Passivator<T> passivator;

  /** For validating pooled objects. */
  private Validator<T> validator;

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
  public Activator<T> getActivator()
  {
    return activator;
  }


  /**
   * Sets the activator for this pool.
   *
   * @param  a  activator
   */
  public void setActivator(final Activator<T> a)
  {
    logger.trace("setting activator: {}", a);
    activator = a;
  }


  /**
   * Prepare the object to exit the pool for use.
   *
   * @param  t  pooled object
   *
   * @return  whether the object successfully activated
   */
  public boolean activate(final T t)
  {
    final boolean success;
    if (activator == null) {
      success = true;
      logger.trace("no activator configured");
    } else {
      success = activator.activate(t);
      logger.trace("activation for {} = {}", t, success);
    }
    return success;
  }


  /**
   * Returns the passivator for this pool.
   *
   * @return  passivator
   */
  public Passivator<T> getPassivator()
  {
    return passivator;
  }


  /**
   * Sets the passivator for this pool.
   *
   * @param  p  passivator
   */
  public void setPassivator(final Passivator<T> p)
  {
    logger.trace("setting passivator: {}", p);
    passivator = p;
  }


  /**
   * Prepare the object to enter the pool after use.
   *
   * @param  t  pooled object
   *
   * @return  whether the object successfully passivated
   */
  public boolean passivate(final T t)
  {
    final boolean success;
    if (passivator == null) {
      success = true;
      logger.trace("no passivator configured");
    } else {
      success = passivator.passivate(t);
      logger.trace("passivation for {} = {}", t, success);
    }
    return success;
  }


  /**
   * Returns the validator for this pool.
   *
   * @return  validator
   */
  public Validator<T> getValidator()
  {
    return validator;
  }


  /**
   * Sets the validator for this pool.
   *
   * @param  v  validator
   */
  public void setValidator(final Validator<T> v)
  {
    logger.trace("setting validator: {}", v);
    validator = v;
  }


  /**
   * Verify the object is still viable for use in the pool.
   *
   * @param  t  pooled object
   *
   * @return  whether the object is viable
   */
  public boolean validate(final T t)
  {
    final boolean success;
    if (validator == null) {
      success = true;
      logger.warn("validate called, but no validator configured");
    } else {
      success = validator.validate(t);
      logger.trace("validation for {} = {}", t, success);
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
