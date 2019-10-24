/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import org.ldaptive.AbstractConfig;

/**
 * Contains all the configuration data that the pooling implementations need to control the pool.
 *
 * @author  Middleware Services
 */
public class PoolConfig extends AbstractConfig
{

  /** Default min pool size, value is {@value}. */
  public static final int DEFAULT_MIN_POOL_SIZE = 3;

  /** Default max pool size, value is {@value}. */
  public static final int DEFAULT_MAX_POOL_SIZE = 10;

  /** Default validate on check in, value is {@value}. */
  public static final boolean DEFAULT_VALIDATE_ON_CHECKIN = false;

  /** Default validate on check out, value is {@value}. */
  public static final boolean DEFAULT_VALIDATE_ON_CHECKOUT = false;

  /** Default validate periodically, value is {@value}. */
  public static final boolean DEFAULT_VALIDATE_PERIODICALLY = false;

  /** Minimum pool size. */
  private int minPoolSize = DEFAULT_MIN_POOL_SIZE;

  /** Maximum pool size. */
  private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;

  /** Whether the ldap object should be validated when returned to the pool. */
  private boolean validateOnCheckIn = DEFAULT_VALIDATE_ON_CHECKIN;

  /** Whether the ldap object should be validated when given from the pool. */
  private boolean validateOnCheckOut = DEFAULT_VALIDATE_ON_CHECKOUT;

  /** Whether the pool should be validated periodically. */
  private boolean validatePeriodically = DEFAULT_VALIDATE_PERIODICALLY;


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
    checkImmutable();
    if (size >= 0) {
      logger.trace("setting minPoolSize: {}", size);
      minPoolSize = size;
    }
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
    checkImmutable();
    if (size >= 0) {
      logger.trace("setting maxPoolSize: {}", size);
      maxPoolSize = size;
    }
  }


  /**
   * Returns the validate on check in flag. Default value is {@link #DEFAULT_VALIDATE_ON_CHECKIN}.
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
    checkImmutable();
    logger.trace("setting validateOnCheckIn: {}", b);
    validateOnCheckIn = b;
  }


  /**
   * Returns the validate on check out flag. Default value is {@link #DEFAULT_VALIDATE_ON_CHECKOUT}.
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
    checkImmutable();
    logger.trace("setting validateOnCheckOut: {}", b);
    validateOnCheckOut = b;
  }


  /**
   * Returns the validate periodically flag. Default value is {@link #DEFAULT_VALIDATE_PERIODICALLY}.
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
    checkImmutable();
    logger.trace("setting validatePeriodically: {}", b);
    validatePeriodically = b;
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("minPoolSize=").append(minPoolSize).append(", ")
      .append("maxPoolSize=").append(maxPoolSize).append(", ")
      .append("validateOnCheckIn=").append(validateOnCheckIn).append(", ")
      .append("validateOnCheckOut=").append(validateOnCheckOut).append(", ")
      .append("validatePeriodically=").append(validatePeriodically).append("]").toString();
  }


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static Builder builder()
  {
    return new Builder();
  }


  // CheckStyle:OFF
  public static class Builder
  {


    private final PoolConfig object = new PoolConfig();


    protected Builder() {}


    public Builder min(final int size)
    {
      object.setMinPoolSize(size);
      return this;
    }


    public Builder max(final int size)
    {
      object.setMaxPoolSize(size);
      return this;
    }


    public Builder validateOnCheckIn(final boolean b)
    {
      object.setValidateOnCheckIn(b);
      return this;
    }


    public Builder validateOnCheckOut(final boolean b)
    {
      object.setValidateOnCheckOut(b);
      return this;
    }


    public Builder validatePeriodically(final boolean b)
    {
      object.setValidatePeriodically(b);
      return this;
    }


    public PoolConfig build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
