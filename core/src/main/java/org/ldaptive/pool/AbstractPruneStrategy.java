/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
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

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Prune period. */
  private Duration prunePeriod;


  @Override
  public Duration getPrunePeriod()
  {
    return prunePeriod;
  }


  /**
   * Sets the prune period.
   *
   * @param  period  to set
   */
  public void setPrunePeriod(final Duration period)
  {
    assertMutable();
    if (period == null || period.isNegative() || period.isZero()) {
      throw new IllegalArgumentException("Prune period cannot be null, negative or zero");
    }
    prunePeriod = period;
  }


  @Override
  public void accept(final Supplier<Iterator<PooledConnectionProxy>> connections)
  {
    final List<Predicate<PooledConnectionProxy>> predicates = getPruneConditions();
    logger.trace("prune strategy {} has {} conditions", this, predicates.size());
    // prune all available connections for each predicate
    for (Predicate<PooledConnectionProxy> predicate : predicates) {
      final Iterator<PooledConnectionProxy> iterator = connections.get();
      while (iterator.hasNext()) {
        final PooledConnectionProxy pc = iterator.next();
        if (predicate.test(pc)) {
          logger.trace("prune approved on {} with {}", pc, this);
          iterator.remove();
          pc.getConnection().close();
          logger.trace("prune removed {} from {}", pc, this);
        } else {
          logger.trace("prune denied on {} with {}", pc, this);
        }
      }
    }
  }


  /**
   * Returns the predicates used by this prune strategy. Each predicate will be invoked for every connection to be
   * pruned.
   *
   * @return  List of predicates that are evaluated in order. Connections are removed from the pool immediately upon
   * predicate returning true; subsequent predicates operate on a reduced pool.
   */
  protected abstract List<Predicate<PooledConnectionProxy>> getPruneConditions();


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
