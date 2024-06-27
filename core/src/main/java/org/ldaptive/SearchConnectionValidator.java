/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;

/**
 * Validates a connection is healthy by performing a search operation. Unless {@link
 * #setValidResultCodes(ResultCode...)} is set, validation is considered successful if the search result contains any
 * result code.
 *
 * @author  Middleware Services
 */
public class SearchConnectionValidator extends AbstractOperationConnectionValidator<SearchRequest, SearchResponse>
{


  /** Creates a new search validator. */
  public SearchConnectionValidator()
  {
    this(SearchRequest.objectScopeSearchRequest("", ReturnAttributes.NONE.value()));
  }


  /**
   * Creates a new search validator.
   *
   * @param  sr  to use for searches
   */
  public SearchConnectionValidator(final SearchRequest sr)
  {
    this(DEFAULT_VALIDATE_PERIOD, DEFAULT_VALIDATE_TIMEOUT, sr);
  }


  /**
   * Creates a new search validator.
   *
   * @param  period  execution period
   * @param  timeout  execution timeout
   * @param  request  to use for searches
   */
  public SearchConnectionValidator(final Duration period, final Duration timeout, final SearchRequest request)
  {
    setValidatePeriod(period);
    setValidateTimeout(timeout);
    setRequest(request);
  }


  /**
   * Returns the search request.
   *
   * @return  search request
   *
   * @deprecated  use {@link AbstractOperationConnectionValidator#getRequest()}
   */
  @Deprecated
  public SearchRequest getSearchRequest()
  {
    return getRequest();
  }


  /**
   * Sets the search request.
   *
   * @param  sr  search request
   *
   * @deprecated  use {@link AbstractOperationConnectionValidator#setRequest(Request)}
   */
  @Deprecated
  public void setSearchRequest(final SearchRequest sr)
  {
    assertMutable();
    setRequest(sr);
  }


  @Override
  protected OperationHandle<SearchRequest, SearchResponse> performOperation(final Connection conn)
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


  /** Search validator builder. */
  public static class Builder extends
    AbstractOperationConnectionValidator.AbstractBuilder<
      SearchRequest, SearchResponse, SearchConnectionValidator.Builder, SearchConnectionValidator>
  {


    /**
     * Creates a new builder.
     */
    protected Builder()
    {
      super(new SearchConnectionValidator());
    }


    @Override
    protected Builder self()
    {
      return this;
    }
  }
}
