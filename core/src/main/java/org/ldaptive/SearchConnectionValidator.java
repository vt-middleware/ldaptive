/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Validates a connection is healthy by performing a search operation. Validation is considered successful if the search
 * result contains a result code.
 *
 * @author  Middleware Services
 */
public class SearchConnectionValidator extends AbstractConnectionValidator
{

  /** Search request to perform validation with. */
  private SearchRequest searchRequest;


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
    setSearchRequest(request);
  }


  /**
   * Returns the search request.
   *
   * @return  search request
   */
  public SearchRequest getSearchRequest()
  {
    return searchRequest;
  }


  /**
   * Sets the search request.
   *
   * @param  sr  search request
   */
  public void setSearchRequest(final SearchRequest sr)
  {
    searchRequest = sr;
  }


  @Override
  public void applyAsync(final Connection conn, final Consumer<Boolean> function)
  {
    if (conn == null) {
      function.accept(false);
    } else {
      final SearchOperationHandle h = conn.operation(searchRequest);
      // note that validation doesn't require a SUCCESS result code
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
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("validatePeriod=").append(getValidatePeriod()).append(", ")
      .append("validateTimeout=").append(getValidateTimeout()).append(", ")
      .append("searchRequest=").append(searchRequest).append("]").toString();
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
    AbstractConnectionValidator.AbstractBuilder<SearchConnectionValidator.Builder, SearchConnectionValidator>
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


    /**
     * Sets the search request to use for validation.
     *
     * @param  request  compare request
     *
     * @return  this builder
     */
    public Builder request(final SearchRequest request)
    {
      object.setSearchRequest(request);
      return self();
    }
  }
}
