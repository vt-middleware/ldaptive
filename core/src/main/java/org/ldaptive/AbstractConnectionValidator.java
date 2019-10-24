/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;

/**
 * Base class for connection validator implementations.
 *
 * @author  Middleware Services
 */
public abstract class AbstractConnectionValidator implements ConnectionValidator
{

  /** Default validation period, value is 30 minutes. */
  public static final Duration DEFAULT_VALIDATE_PERIOD = Duration.ofMinutes(30);

  /** Default per connection validate timeout, value is 5 seconds. */
  public static final Duration DEFAULT_VALIDATE_TIMEOUT = Duration.ofSeconds(5);

  /** Validation period. */
  private Duration validatePeriod;

  /** Maximum length of time a connection validation should block. */
  private Duration validateTimeout;


  @Override
  public Duration getValidatePeriod()
  {
    return validatePeriod;
  }


  public void setValidatePeriod(final Duration period)
  {
    if (period == null || period.isNegative()) {
      throw new IllegalArgumentException("Period cannot be null or negative");
    }
    validatePeriod = period;
  }

  @Override
  public Duration getValidateTimeout()
  {
    return validateTimeout;
  }


  public void setValidateTimeout(final Duration timeout)
  {
    if (timeout == null || timeout.isNegative()) {
      throw new IllegalArgumentException("Timeout cannot be null or negative");
    }
    validateTimeout = timeout;
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


    /**
     * Returns the message.
     *
     * @return  message
     */
    public T build()
    {
      return object;
    }
  }
}
