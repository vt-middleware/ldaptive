/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Base class for validators that use an operation to perform validation. By default, validation is considered
 * successful if the operation result contains any result code. Stricter validation can be configured by setting {@link
 * #validResultCodes}.
 *
 * @param  <Q>  type of request
 * @param  <S>  type of result
 *
 * @author  Middleware Services
 */
public abstract class AbstractOperationConnectionValidator<Q extends Request, S extends Result>
  extends AbstractConnectionValidator
{

  /** Operation request. */
  private Q request;

  /** Valid result codes. */
  private ResultCode[] validResultCodes;


  /**
   * Returns the operation request.
   *
   * @return  operation request
   */
  public final Q getRequest()
  {
    return request;
  }


  /**
   * Sets the operation request.
   *
   * @param  req  operation request
   */
  public final void setRequest(final Q req)
  {
    checkImmutable();
    request = req;
  }


  /**
   * Returns the valid result codes.
   *
   * @return  valid result codes
   */
  public final ResultCode[] getValidResultCodes()
  {
    return LdapUtils.copyArray(validResultCodes);
  }


  /**
   * Sets the valid result codes.
   *
   * @param  codes  that represent a valid connection
   */
  public final void setValidResultCodes(final ResultCode... codes)
  {
    checkImmutable();
    validResultCodes = LdapUtils.copyArray(codes);
  }


  /**
   * Perform the operation for this validator.
   *
   * @param  conn  to validate
   *
   * @return  operation handle
   */
  protected abstract OperationHandle<Q, S> performOperation(Connection conn);


  @Override
  public void applyAsync(final Connection conn, final Consumer<Boolean> function)
  {
    if (conn == null) {
      function.accept(false);
    } else {
      final OperationHandle<Q, S> h = performOperation(conn);
      h.onResult(r -> {
        if (validResultCodes != null) {
          function.accept(Arrays.stream(validResultCodes).anyMatch(rc -> rc.equals(r.getResultCode())));
        } else {
          function.accept(r.getResultCode() != null);
        }
      });
      h.onException(e -> {
        if (e != null && e.getResultCode() == ResultCode.LDAP_TIMEOUT && !getTimeoutIsFailure()) {
          logger.debug("Connection validator timeout ignored for {}", conn);
          function.accept(true);
        } else {
          logger.debug("Connection validator failed for {}", conn, e);
          function.accept(false);
        }
      });
      h.send();
    }
  }


  @Override
  public String toString()
  {
    return super.toString() + ", request=" + request + ", validResultCodes=" + Arrays.toString(validResultCodes);
  }


  /**
   * Base class for operation validator builders.
   *
   * @param  <Q>  type of request
   * @param  <S>  type of result
   * @param  <B>  type of builder
   * @param  <T>  type of validator
   */
  protected abstract static class AbstractBuilder
    <Q extends Request, S extends Result, B, T extends AbstractOperationConnectionValidator<Q, S>> extends
      AbstractConnectionValidator.AbstractBuilder<B, T>
  {


    /**
     * Creates a new abstract builder.
     *
     * @param  t  validator to build
     */
    protected AbstractBuilder(final T t)
    {
      super(t);
    }


    /**
     * Returns this builder.
     *
     * @return  builder
     */
    protected abstract B self();


    /**
     * Sets the request to use for validation.
     *
     * @param  request  operation request
     *
     * @return  this builder
     */
    public B request(final Q request)
    {
      object.setRequest(request);
      return self();
    }


    /**
     * Sets the result codes to use for validation.
     *
     * @param  codes  valid result codes
     *
     * @return  this builder
     */
    public B validResultCodes(final ResultCode... codes)
    {
      object.setValidResultCodes(codes);
      return self();
    }
  }
}
