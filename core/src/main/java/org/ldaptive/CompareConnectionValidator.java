/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;

/**
 * Validates a connection is healthy by performing a compare operation. Unless {@link
 * #setValidResultCodes(ResultCode...)} is set, validation is considered successful if the compare result contains any
 * result code.
 *
 * @author  Middleware Services
 */
public class CompareConnectionValidator extends AbstractOperationConnectionValidator<CompareRequest, CompareResponse>
{


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
    setRequest(request);
  }


  /**
   * Returns the compare request.
   *
   * @return  compare request
   *
   * @deprecated  use {@link AbstractOperationConnectionValidator#getRequest()}
   */
  @Deprecated
  public CompareRequest getCompareRequest()
  {
    return getRequest();
  }


  /**
   * Sets the compare request.
   *
   * @param  cr  compare request
   *
   * @deprecated  use {@link AbstractOperationConnectionValidator#setRequest(Request)}
   */
  @Deprecated
  public void setCompareRequest(final CompareRequest cr)
  {
    setRequest(cr);
  }


  @Override
  protected OperationHandle<CompareRequest, CompareResponse> performOperation(final Connection conn)
  {
    return conn.operation(getRequest());
  }

  @Override
  public String toString()
  {
    return "[" + super.toString() + "]";
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
    AbstractOperationConnectionValidator.AbstractBuilder<
      CompareRequest, CompareResponse, CompareConnectionValidator.Builder, CompareConnectionValidator>
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
  }
}
