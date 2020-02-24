/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates a connection is healthy by performing a compare operation. Validation is considered successful if the
 * compare result contains a result code.
 *
 * @author  Middleware Services
 */
public class CompareConnectionValidator extends AbstractConnectionValidator
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Compare request to perform validation with. */
  private CompareRequest compareRequest;


  /** Creates a new compare validator. */
  public CompareConnectionValidator()
  {
    this(CompareRequest.builder().dn("").name("objectClass").value("top").build());
  }


  /**
   * Creates a new compare validator.
   *
   * @param  cr  to use for compares
   */
  public CompareConnectionValidator(final CompareRequest cr)
  {
    this(DEFAULT_VALIDATE_PERIOD, DEFAULT_VALIDATE_TIMEOUT, cr);
  }


  /**
   * Creates a new compare validator.
   *
   * @param  period  execution period
   * @param  timeout  execution timeout
   * @param  request  to use for searches
   */
  public CompareConnectionValidator(final Duration period, final Duration timeout, final CompareRequest request)
  {
    setValidatePeriod(period);
    setValidateTimeout(timeout);
    setCompareRequest(request);
  }


  /**
   * Returns the compare request.
   *
   * @return  compare request
   */
  public CompareRequest getCompareRequest()
  {
    return compareRequest;
  }


  /**
   * Sets the compare request.
   *
   * @param  cr  compare request
   */
  public void setCompareRequest(final CompareRequest cr)
  {
    compareRequest = cr;
  }


  @Override
  public Boolean apply(final Connection conn)
  {
    if (conn != null) {
      try {
        final CompareResponse response = conn.operation(compareRequest).execute();
        // note that validation doesn't require a TRUE result code
        return response.getResultCode() != null;
      } catch (Exception e) {
        logger.debug("validation failed for compare request {}", compareRequest, e);
      }
    }
    return false;
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("validatePeriod=").append(getValidatePeriod()).append(", ")
      .append("validateTimeout=").append(getValidateTimeout()).append(", ")
      .append("compareRequest=").append(compareRequest).append("]").toString();
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


  /** Compare validator builder. */
  public static class Builder extends
    AbstractConnectionValidator.AbstractBuilder<CompareConnectionValidator.Builder, CompareConnectionValidator>
  {


    /**
     * Creates a new builder.
     */
    protected Builder()
    {
      super(new CompareConnectionValidator());
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    /**
     * Sets the compare request to use for validation.
     *
     * @param  request  compare request
     *
     * @return  this builder
     */
    public Builder request(final CompareRequest request)
    {
      object.setCompareRequest(request);
      return self();
    }
  }
}
