/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Validates a connection is healthy by performing a compare operation. Validation is considered successful if the
 * compare result contains a result code.
 *
 * @author  Middleware Services
 */
public class CompareConnectionValidator extends AbstractConnectionValidator
{

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
  public void applyAsync(final Connection conn, final Consumer<Boolean> function)
  {
    if (conn == null) {
      function.accept(false);
    } else {
      final CompareOperationHandle h = conn.operation(compareRequest);
      // note that validation doesn't require a TRUE result code
      h.onResult(r -> function.accept(r.getResultCode() != null));
      h.onException(e -> {
        logger.debug("Connection validator failed for {}", conn, e);
        function.accept(false);
      });
      h.send();
    }
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "validatePeriod=" + getValidatePeriod() + ", " +
      "validateTimeout=" + getValidateTimeout() + ", " +
      "compareRequest=" + compareRequest + "]";
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
