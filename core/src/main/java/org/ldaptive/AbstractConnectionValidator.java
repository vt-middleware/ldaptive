/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for connection validator implementations.
 *
 * @author  Middleware Services
 */
public abstract class AbstractConnectionValidator extends AbstractImmutable implements ConnectionValidator
{

  /** Default validation period, value is 30 minutes. */
  public static final Duration DEFAULT_VALIDATE_PERIOD = Duration.ofMinutes(30);

  /** Default per connection validate timeout, value is 5 seconds. */
  public static final Duration DEFAULT_VALIDATE_TIMEOUT = Duration.ofSeconds(5);

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Validation period. */
  private Duration validatePeriod;

  /** Maximum length of time a connection validation should block. */
  private Duration validateTimeout;

  /** Consumer to execute on a successful validation. */
  private Consumer<Connection> onSuccess;

  /** Consumer to execute on a failed validation. */
  private Consumer<Connection> onFailure;

  /** Whether the occurrence of a timeout should result in a validation failure. */
  private boolean timeoutIsFailure = true;


  @Override
  public final Duration getValidatePeriod()
  {
    return validatePeriod;
  }


  public final void setValidatePeriod(final Duration period)
  {
    checkImmutable();
    if (period == null || period.isNegative() || period.isZero()) {
      throw new IllegalArgumentException("Period cannot be null, negative or zero");
    }
    validatePeriod = period;
  }

  @Override
  public final Duration getValidateTimeout()
  {
    return validateTimeout;
  }


  /**
   * Sets the validate timeout.
   *
   * @param  timeout  to set
   */
  public final void setValidateTimeout(final Duration timeout)
  {
    checkImmutable();
    if (timeout == null || timeout.isNegative()) {
      throw new IllegalArgumentException("Timeout cannot be null or negative");
    }
    validateTimeout = timeout;
  }


  /**
   * Returns a consumer to handle a connection that has been successfully validated.
   *
   * @return  success consumer
   */
  public final Consumer<Connection> getOnSuccess()
  {
    return onSuccess;
  }


  /**
   * Sets a consumer to handle a connection that has been successfully validated.
   *
   * @param  consumer  to invoke on success
   */
  public final void setOnSuccess(final Consumer<Connection> consumer)
  {
    checkImmutable();
    onSuccess = consumer;
  }


  /**
   * Returns a consumer to handle a connection that has failed validation.
   *
   * @return  failure consumer
   */
  public final Consumer<Connection> getOnFailure()
  {
    return onFailure;
  }


  /**
   * Sets a consumer to handle a connection that has failed validation.
   *
   * @param  consumer  to invoke on failure
   */
  public final void setOnFailure(final Consumer<Connection> consumer)
  {
    checkImmutable();
    onFailure = consumer;
  }


  /**
   * Returns whether a timeout should be considered a validation failure.
   *
   * @return  whether a timeout should be considered a validation failure
   */
  public final boolean getTimeoutIsFailure()
  {
    return timeoutIsFailure;
  }


  /**
   * Sets whether a timeout should be considered a validation failure.
   *
   * @param  failure  whether a timeout should be considered a validation failure
   */
  public final void setTimeoutIsFailure(final boolean failure)
  {
    checkImmutable();
    timeoutIsFailure = failure;
  }


  @Override
  public Boolean apply(final Connection conn)
  {
    if (conn == null) {
      if (onFailure != null) {
        onFailure.accept(null);
      }
      return false;
    }
    final Boolean result = applyAsync(conn).get();
    if (result && onSuccess != null) {
      onSuccess.accept(conn);
    } else if (!result && onFailure != null) {
      onFailure.accept(conn);
    }
    return result;
  }


  @Override
  public Supplier<Boolean> applyAsync(final Connection conn)
  {
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicBoolean result = new AtomicBoolean();
    applyAsync(conn, value -> {
      result.compareAndSet(false, value);
      latch.countDown();
    });
    return () -> {
      try {
        if (Duration.ZERO.equals(getValidateTimeout())) {
          // waits indefinitely for the validation response
          latch.await();
        } else {
          if (!latch.await(getValidateTimeout().toMillis(), TimeUnit.MILLISECONDS) && !timeoutIsFailure) {
            logger.debug("Connection validator timeout ignored for {}", conn);
            result.compareAndSet(false, true);
          }
        }
      } catch (Exception e) {
        logger.debug("Validating {} threw unexpected exception", conn, e);
      }
      return result.get();
    };
  }


  @Override
  public String toString()
  {
    return
      getClass().getName() + "@" + hashCode() + "::" +
      "validatePeriod=" + validatePeriod + ", " +
      "validateTimeout=" + validateTimeout + ", " +
      "onSuccess=" + onSuccess + ", " +
      "onFailure=" + onFailure + ", " +
      "timeoutIsFailure=" + timeoutIsFailure;
  }


  /**
   * Base class for validator builders.
   *
   * @param  <B>  type of builder
   * @param  <T>  type of validator
   */
  protected abstract static class AbstractBuilder<B, T extends AbstractConnectionValidator>
  {

    /** Validator to build. */
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
    public B makeImmutable()
    {
      object.makeImmutable();
      return self();
    }


    /**
     * Sets the validation period.
     *
     * @param  period  to set
     *
     * @return  this builder
     */
    public B period(final Duration period)
    {
      object.setValidatePeriod(period);
      return self();
    }


    /**
     * Sets the validation timeout.
     *
     * @param  timeout  to set
     *
     * @return  this builder
     */
    public B timeout(final Duration timeout)
    {
      object.setValidateTimeout(timeout);
      return self();
    }


    public B onSuccess(final Consumer<Connection> consumer)
    {
      object.setOnSuccess(consumer);
      return self();
    }


    public B onFailure(final Consumer<Connection> consumer)
    {
      object.setOnFailure(consumer);
      return self();
    }


    /**
     * Sets whether timeout is a validation failure.
     *
     * @param  failure  whether timeout is a validation failure
     *
     * @return  this builder
     */
    public B timeoutIsFailure(final boolean failure)
    {
      object.setTimeoutIsFailure(failure);
      return self();
    }


    /**
     * Returns the connection validator.
     *
     * @return  connection validator
     */
    public T build()
    {
      return object;
    }
  }
}
