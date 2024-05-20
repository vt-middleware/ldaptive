/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.time.Duration;
import org.ldaptive.AbstractFreezable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for prune strategy implementations.
 *
 * @author  Middleware Services
 */
public abstract class AbstractPruneStrategy extends AbstractFreezable implements PruneStrategy
{

  /** Default prune period in seconds. Value is 5 minutes. */
  protected static final Duration DEFAULT_PRUNE_PERIOD = Duration.ofMinutes(5);

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Prune period. */
  private Duration prunePeriod;


  @Override
  public final Duration getPrunePeriod()
  {
    return prunePeriod;
  }


  /**
   * Sets the prune period.
   *
   * @param  period  to set
   */
  public final void setPrunePeriod(final Duration period)
  {
    assertMutable();
    if (period == null || period.isNegative() || period.isZero()) {
      throw new IllegalArgumentException("Prune period cannot be null, negative or zero");
    }
    prunePeriod = period;
  }


  /**
   * Base class for prune strategy builders.
   *
   * @param  <B>  type of builder
   * @param  <T>  type of validator
   */
  protected abstract static class AbstractBuilder<B, T extends AbstractPruneStrategy>
  {

    /** Prune strategy to build. */
    protected final T object;


    /**
     * Creates a new abstract builder.
     *
     * @param  t  validator to build
     */
    protected AbstractBuilder(final T t)
    {
      object = t;
    }


    /**
     * Returns this builder.
     *
     * @return  builder
     */
    protected abstract B self();


    /**
     * Makes this instance immutable.
     *
     * @return  this builder
     */
    public B freeze()
    {
      object.freeze();
      return self();
    }


    /**
     * Sets the prune period.
     *
     * @param  period  to set
     *
     * @return  this builder
     */
    public B period(final Duration period)
    {
      object.setPrunePeriod(period);
      return self();
    }


    /**
     * Returns the prune strategy.
     *
     * @return  prune strategy
     */
    public T build()
    {
      return object;
    }
  }
}
