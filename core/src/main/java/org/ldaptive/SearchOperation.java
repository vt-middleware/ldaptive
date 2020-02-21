/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;
import org.ldaptive.filter.Filter;
import org.ldaptive.filter.FilterParser;
import org.ldaptive.handler.LdapEntryHandler;
import org.ldaptive.handler.SearchReferenceHandler;
import org.ldaptive.handler.SearchResultHandler;

/**
 * Executes an ldap search operation.
 *
 * @author  Middleware Services
 */
public class SearchOperation extends AbstractOperation<SearchRequest, SearchResponse>
{

  /** Search request to execute. */
  private SearchRequest request;

  /** Filter template. */
  private FilterTemplate filterTemplate;

  /** Functions to handle response entries. */
  private LdapEntryHandler[] entryHandlers;

  /** Functions to handle response references. */
  private SearchReferenceHandler[] referenceHandlers;

  /** Functions to handle response results. */
  private SearchResultHandler[] searchResultHandlers;


  /**
   * Default constructor.
   */
  public SearchOperation()
  {
    setRequest(new SearchRequest());
  }


  /**
   * Creates a new search operation.
   *
   * @param  factory  connection factory
   */
  public SearchOperation(final ConnectionFactory factory)
  {
    super(factory);
    setRequest(new SearchRequest());
  }


  /**
   * Creates a new search operation.
   *
   * @param  factory  connection factory
   * @param  req  search request
   */
  public SearchOperation(final ConnectionFactory factory, final SearchRequest req)
  {
    super(factory);
    setRequest(req);
  }


  /**
   * Creates a new search operation.
   *
   * @param  factory  connection factory
   * @param  baseDN  to search from
   */
  public SearchOperation(final ConnectionFactory factory, final String baseDN)
  {
    super(factory);
    setRequest(new SearchRequest(baseDN));
  }


  public SearchRequest getRequest()
  {
    return request;
  }


  public void setRequest(final SearchRequest req)
  {
    request = req;
  }


  public FilterTemplate getTemplate()
  {
    return filterTemplate;
  }


  public void setTemplate(final FilterTemplate template)
  {
    filterTemplate = template;
  }


  public LdapEntryHandler[] getEntryHandlers()
  {
    return entryHandlers;
  }


  public void setEntryHandlers(final LdapEntryHandler... handlers)
  {
    entryHandlers = handlers;
  }


  public SearchReferenceHandler[] getReferenceHandlers()
  {
    return referenceHandlers;
  }


  public void setReferenceHandlers(final SearchReferenceHandler... handlers)
  {
    referenceHandlers = handlers;
  }


  public SearchResultHandler[] getSearchResultHandlers()
  {
    return searchResultHandlers;
  }


  public void setSearchResultHandlers(final SearchResultHandler... handlers)
  {
    searchResultHandlers = handlers;
  }


  /**
   * Sends a search request. See {@link SearchOperationHandle#send()}.
   *
   * @param  filter  search filter
   *
   * @return  search operation handle
   *
   * @throws  org.ldaptive.filter.FilterParseException  if the filter cannot be parsed
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchOperationHandle send(final String filter)
    throws LdapException
  {
    return send(null, FilterParser.parse(filter), null, (LdapEntryHandler[]) null);
  }


  /**
   * Sends a search request. See {@link SearchOperationHandle#send()}.
   *
   * @param  template  filter tempalte
   *
   * @return  search operation handle
   *
   * @throws  org.ldaptive.filter.FilterParseException  if the filter cannot be parsed
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchOperationHandle send(final FilterTemplate template)
    throws LdapException
  {
    return send(null, FilterParser.parse(template.format()), null, (LdapEntryHandler[]) null);
  }


  /**
   * Sends a search request. See {@link SearchOperationHandle#send()}.
   *
   * @param  filter  search filter
\   *
   * @return  search operation handle
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchOperationHandle send(final Filter filter)
    throws LdapException
  {
    return send(null, filter, null, (LdapEntryHandler[]) null);
  }


  /**
   * Sends a search request. See {@link SearchOperationHandle#send()}.
   *
   * @param  filter  search filter
   * @param  returnAttributes  attributes to return
   *
   * @return  search operation handle
   *
   * @throws  org.ldaptive.filter.FilterParseException  if the filter cannot be parsed
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchOperationHandle send(final String filter, final String... returnAttributes)
    throws LdapException
  {
    return send(null, FilterParser.parse(filter), returnAttributes, (LdapEntryHandler[]) null);
  }


  /**
   * Sends a search request. See {@link SearchOperationHandle#send()}.
   *
   * @param  template  filter template
   * @param  returnAttributes  attributes to return
   *
   * @return  search operation handle
   *
   * @throws  org.ldaptive.filter.FilterParseException  if the filter cannot be parsed
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchOperationHandle send(final FilterTemplate template, final String... returnAttributes)
    throws LdapException
  {
    return send(null, FilterParser.parse(template.format()), returnAttributes, (LdapEntryHandler[]) null);
  }


  /**
   * Sends a search request. See {@link SearchOperationHandle#send()}.
   *
   * @param  filter  search filter
   * @param  returnAttributes  attributes to return
   *
   * @return  search operation handle
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchOperationHandle send(final Filter filter, final String... returnAttributes)
    throws LdapException
  {
    return send(null, filter, returnAttributes, (LdapEntryHandler[]) null);
  }


  /**
   * Sends a search request. See {@link SearchOperationHandle#send()}.
   *
   * @param  filter  search filter
   * @param  returnAttributes  attributes to return
   * @param  handlers  entry handlers
   *
   * @return  search operation handle
   *
   * @throws  org.ldaptive.filter.FilterParseException  if the filter cannot be parsed
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchOperationHandle send(
    final String filter,
    final String[] returnAttributes,
    final LdapEntryHandler... handlers)
    throws LdapException
  {
    return send(null, FilterParser.parse(filter), returnAttributes, handlers);
  }


  /**
   * Sends a search request. See {@link SearchOperationHandle#send()}.
   *
   * @param  template  filter template
   * @param  returnAttributes  attributes to return
   * @param  handlers  entry handlers
   *
   * @return  search operation handle
   *
   * @throws  org.ldaptive.filter.FilterParseException  if the filter cannot be parsed
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchOperationHandle send(
    final FilterTemplate template,
    final String[] returnAttributes,
    final LdapEntryHandler... handlers)
    throws LdapException
  {
    return send(null, FilterParser.parse(template.format()), returnAttributes, handlers);
  }


  /**
   * Sends a search request. See {@link SearchOperationHandle#send()}.
   *
   * @param  filter  search filter
   * @param  returnAttributes  attributes to return
   * @param  handlers  entry handlers
   *
   * @return  search operation handle
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchOperationHandle send(
    final Filter filter,
    final String[] returnAttributes,
    final LdapEntryHandler... handlers)
    throws LdapException
  {
    return send(null, filter, returnAttributes, handlers);
  }


  /**
   * Sends a search request. See {@link SearchOperationHandle#send()}.
   *
   * @param  baseDN  base DN
   * @param  filter  search filter
   * @param  returnAttributes  attributes to return
   * @param  handlers  entry handlers
   *
   * @return  search operation handle
   *
   * @throws  org.ldaptive.filter.FilterParseException  if the filter cannot be parsed
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchOperationHandle send(
    final String baseDN,
    final String filter,
    final String[] returnAttributes,
    final LdapEntryHandler... handlers)
    throws LdapException
  {
    return send(baseDN, FilterParser.parse(filter), returnAttributes, handlers);
  }


  /**
   * Sends a search request. See {@link SearchOperationHandle#send()}.
   *
   * @param  baseDN  base DN
   * @param  template  filter template
   * @param  returnAttributes  attributes to return
   * @param  handlers  entry handlers
   *
   * @return  search operation handle
   *
   * @throws  org.ldaptive.filter.FilterParseException  if the filter cannot be parsed
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchOperationHandle send(
    final String baseDN,
    final FilterTemplate template,
    final String[] returnAttributes,
    final LdapEntryHandler... handlers)
    throws LdapException
  {
    return send(baseDN, FilterParser.parse(template.format()), returnAttributes, handlers);
  }


  /**
   * Sends a search request. See {@link SearchOperationHandle#send()}.
   *
   * @param  baseDN  base DN
   * @param  filter  search filter
   * @param  returnAttributes  attributes to return
   * @param  handlers  entry handlers
   *
   * @return  search operation handle
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchOperationHandle send(
    final String baseDN,
    final Filter filter,
    final String[] returnAttributes,
    final LdapEntryHandler... handlers)
    throws LdapException
  {
    final Connection conn = getConnectionFactory().getConnection();
    try {
      conn.open();
    } catch (Exception e) {
      conn.close();
      throw e;
    }
    final SearchRequest req = configureRequest(baseDN, filter, returnAttributes);
    if (handlers != null) {
      return configureHandle(conn.operation(req)).onEntry(handlers).onComplete(conn::close).send();
    } else {
      return configureHandle(conn.operation(req)).onComplete(conn::close).send();
    }
  }


  /**
   * Sends the supplied search request.
   *
   * @param  req  search request to send
   *
   * @return  search operation handle
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  @Override
  public SearchOperationHandle send(final SearchRequest req)
    throws LdapException
  {
    final Connection conn = getConnectionFactory().getConnection();
    try {
      conn.open();
    } catch (Exception e) {
      conn.close();
      throw e;
    }
    return configureHandle(conn.operation(req)).onComplete(conn::close).send();
  }


  /**
   * Sends a search request. See {@link SearchOperationHandle#send()}.
   *
   * @return  search operation handle
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchOperationHandle send()
    throws LdapException
  {
    final Connection conn = getConnectionFactory().getConnection();
    try {
      conn.open();
    } catch (Exception e) {
      conn.close();
      throw e;
    }
    final SearchRequest req = configureRequest(null, null, null);
    return configureHandle(conn.operation(req)).onComplete(conn::close).send();
  }


  /**
   * Sends a search request. See {@link SearchOperationHandle#send()}.
   *
   * @param  factory  connection factory
   * @param  req  search request
   *
   * @return  search operation handle
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public static SearchOperationHandle send(final ConnectionFactory factory, final SearchRequest req)
    throws LdapException
  {
    final Connection conn = factory.getConnection();
    try {
      conn.open();
    } catch (Exception e) {
      conn.close();
      throw e;
    }
    return conn.operation(req).onComplete(conn::close).send();
  }


  /**
   * Executes a search request. See {@link SearchOperationHandle#execute()}.
   *
   * @param  filter  search filter
   *
   * @return  search result
   *
   * @throws  org.ldaptive.filter.FilterParseException  if the filter cannot be parsed
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchResponse execute(final String filter)
    throws LdapException
  {
    return execute(FilterParser.parse(filter), null, (LdapEntryHandler[]) null);
  }


  /**
   * Executes a search request. See {@link SearchOperationHandle#execute()}.
   *
   * @param  template  filter template
   *
   * @return  search result
   *
   * @throws  org.ldaptive.filter.FilterParseException  if the filter cannot be parsed
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchResponse execute(final FilterTemplate template)
    throws LdapException
  {
    return execute(FilterParser.parse(template.format()), null, (LdapEntryHandler[]) null);
  }


  /**
   * Executes a search request. See {@link SearchOperationHandle#execute()}.
   *
   * @param  filter  search filter
   *
   * @return  search result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchResponse execute(final Filter filter)
    throws LdapException
  {
    return execute(null, filter, null, (LdapEntryHandler[]) null);
  }


  /**
   * Executes a search request. See {@link SearchOperationHandle#execute()}.
   *
   * @param  filter  search filter
   * @param  returnAttributes  attributes to return
   *
   * @return  search result
   *
   * @throws  org.ldaptive.filter.FilterParseException  if the filter cannot be parsed
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchResponse execute(final String filter, final String... returnAttributes)
    throws LdapException
  {
    return execute(FilterParser.parse(filter), returnAttributes, (LdapEntryHandler[]) null);
  }


  /**
   * Executes a search request. See {@link SearchOperationHandle#execute()}.
   *
   * @param  template  filter template
   * @param  returnAttributes  attributes to return
   *
   * @return  search result
   *
   * @throws  org.ldaptive.filter.FilterParseException  if the filter cannot be parsed
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchResponse execute(final FilterTemplate template, final String... returnAttributes)
    throws LdapException
  {
    return execute(FilterParser.parse(template.format()), returnAttributes, (LdapEntryHandler[]) null);
  }


  /**
   * Executes a search request. See {@link SearchOperationHandle#execute()}.
   *
   * @param  filter  search filter
   * @param  returnAttributes  attributes to return
   *
   * @return  search result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchResponse execute(final Filter filter, final String... returnAttributes)
    throws LdapException
  {
    return execute(null, filter, returnAttributes, (LdapEntryHandler[]) null);
  }


  /**
   * Executes a search request. See {@link SearchOperationHandle#execute()}.
   *
   * @param  filter  search filter
   * @param  returnAttributes  attributes to return
   * @param  handlers  entry handlers
   *
   * @return  search result
   *
   * @throws  org.ldaptive.filter.FilterParseException  if the filter cannot be parsed
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchResponse execute(
    final String filter,
    final String[] returnAttributes,
    final LdapEntryHandler... handlers)
    throws LdapException
  {
    return execute(FilterParser.parse(filter), returnAttributes, handlers);
  }


  /**
   * Executes a search request. See {@link SearchOperationHandle#execute()}.
   *
   * @param  template  filter template
   * @param  returnAttributes  attributes to return
   * @param  handlers  entry handlers
   *
   * @return  search result
   *
   * @throws  org.ldaptive.filter.FilterParseException  if the filter cannot be parsed
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchResponse execute(
    final FilterTemplate template,
    final String[] returnAttributes,
    final LdapEntryHandler... handlers)
    throws LdapException
  {
    return execute(FilterParser.parse(template.format()), returnAttributes, handlers);
  }


  /**
   * Executes a search request. See {@link SearchOperationHandle#execute()}.
   *
   * @param  filter  search filter
   * @param  returnAttributes  attributes to return
   * @param  handlers  entry handlers
   *
   * @return  search result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchResponse execute(
    final Filter filter,
    final String[] returnAttributes,
    final LdapEntryHandler... handlers)
    throws LdapException
  {
    return execute(null, filter, returnAttributes, handlers);
  }


  /**
   * Executes a search request. See {@link SearchOperationHandle#execute()}.
   *
   * @param  baseDN  base DN
   * @param  filter  search filter
   * @param  returnAttributes  attributes to return
   * @param  handlers  entry handlers
   *
   * @return  search result
   *
   * @throws  org.ldaptive.filter.FilterParseException  if the filter cannot be parsed
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchResponse execute(
    final String baseDN,
    final String filter,
    final String[] returnAttributes,
    final LdapEntryHandler... handlers)
    throws LdapException
  {
    return execute(baseDN, FilterParser.parse(filter), returnAttributes, handlers);
  }


  /**
   * Executes a search request. See {@link SearchOperationHandle#execute()}.
   *
   * @param  baseDN  base DN
   * @param  template  filter template
   * @param  returnAttributes  attributes to return
   * @param  handlers  entry handlers
   *
   * @return  search result
   *
   * @throws  org.ldaptive.filter.FilterParseException  if the filter cannot be parsed
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchResponse execute(
    final String baseDN,
    final FilterTemplate template,
    final String[] returnAttributes,
    final LdapEntryHandler... handlers)
    throws LdapException
  {
    return execute(baseDN, FilterParser.parse(template.format()), returnAttributes, handlers);
  }


  /**
   * Executes a search request. See {@link SearchOperationHandle#execute()}.
   *
   * @param  baseDN  base DN
   * @param  filter  search filter
   * @param  returnAttributes  attributes to return
   * @param  handlers  entry handlers
   *
   * @return  search result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchResponse execute(
    final String baseDN,
    final Filter filter,
    final String[] returnAttributes,
    final LdapEntryHandler... handlers)
    throws LdapException
  {
    try (Connection conn = getConnectionFactory().getConnection()) {
      conn.open();
      final SearchRequest req = configureRequest(baseDN, filter, returnAttributes);
      if (handlers != null) {
        return configureHandle(conn.operation(req)).onEntry(handlers).execute();
      } else {
        return configureHandle(conn.operation(req)).execute();
      }
    }
  }


  @Override
  public SearchResponse execute(final SearchRequest req)
    throws LdapException
  {
    try (Connection conn = getConnectionFactory().getConnection()) {
      conn.open();
      return configureHandle(conn.operation(req)).execute();
    }
  }


  /**
   * Executes a search request using {@link #getRequest()}. See {@link SearchOperationHandle#execute()}.
   *
   * @return  search result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public SearchResponse execute()
    throws LdapException
  {
    try (Connection conn = getConnectionFactory().getConnection()) {
      conn.open();
      final SearchRequest req = configureRequest(null, null, null);
      return configureHandle(conn.operation(req)).execute();
    }
  }


  /**
   * Executes a search request. See {@link SearchOperationHandle#execute()}.
   *
   * @param  factory  connection factory
   * @param  req  search request
   *
   * @return  search result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public static SearchResponse execute(final ConnectionFactory factory, final SearchRequest req)
    throws LdapException
  {
    try (Connection conn = factory.getConnection()) {
      conn.open();
      return conn.operation(req).execute();
    }
  }


  /**
   * Creates a new request from {@link #getRequest()} and applies any non-null supplied properties.
   *
   * @param  baseDN  base DN
   * @param  filter  search filter
   * @param  returnAttributes  attributes to return
   *
   * @return  configured search request
   */
  protected SearchRequest configureRequest(
    final String baseDN,
    final Filter filter,
    final String[] returnAttributes)
  {
    final SearchRequest.Builder builder = SearchRequest.builder(SearchRequest.copy(getRequest()));
    if (baseDN != null) {
      builder.dn(baseDN);
    }
    if (filter != null) {
      builder.filter(filter);
    } else if (getTemplate() != null) {
      builder.filter(getTemplate());
    }
    if (returnAttributes != null) {
      builder.returnAttributes(returnAttributes);
    }
    return builder.build();
  }


  /**
   * Adds configured functions to the supplied handle.
   *
   * @param  handle  to configure
   *
   * @return  configured handle
   */
  protected SearchOperationHandle configureHandle(final SearchOperationHandle handle)
  {
    return handle
      .onEntry(getEntryHandlers())
      .onReference(getReferenceHandlers())
      .onResult(getResultHandlers())
      .onControl(getControlHandlers())
      .onReferral(getReferralHandlers())
      .onIntermediate(getIntermediateResponseHandlers())
      .onException(getExceptionHandler())
      .throwIf(getThrowCondition())
      .onUnsolicitedNotification(getUnsolicitedNotificationHandlers())
      .onSearchResult(getSearchResultHandlers());
  }


  /**
   * Returns a new search operation with the same properties as the supplied operation.
   *
   * @param  operation  to copy
   *
   * @return  copy of the supplied search operation
   */
  public static SearchOperation copy(final SearchOperation operation)
  {
    final SearchOperation op = new SearchOperation();
    op.setResultHandlers(operation.getResultHandlers());
    op.setControlHandlers(operation.getControlHandlers());
    op.setReferralHandlers(operation.getReferralHandlers());
    op.setIntermediateResponseHandlers(operation.getIntermediateResponseHandlers());
    op.setExceptionHandler(operation.getExceptionHandler());
    op.setThrowCondition(operation.getThrowCondition());
    op.setUnsolicitedNotificationHandlers(operation.getUnsolicitedNotificationHandlers());
    op.setEntryHandlers(operation.getEntryHandlers());
    op.setReferenceHandlers(operation.getReferenceHandlers());
    op.setSearchResultHandlers(operation.getSearchResultHandlers());
    op.setConnectionFactory(operation.getConnectionFactory());
    op.setRequest(operation.getRequest());
    op.setTemplate(operation.getTemplate());
    return op;
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ")
      .append("request=").append(request).append(", ")
      .append("template=").append(filterTemplate).append(", ")
      .append("entryHandlers=").append(Arrays.toString(entryHandlers)).append(", ")
      .append("referenceHandlers=").append(Arrays.toString(referenceHandlers)).append(", ")
      .append("searchResultHandlers=").append(Arrays.toString(searchResultHandlers)).toString();
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


  /** Search operation builder. */
  public static class Builder extends AbstractOperation.AbstractBuilder<SearchOperation.Builder, SearchOperation>
  {


    /**
     * Creates a new builder.
     */
    protected Builder()
    {
      super(new SearchOperation());
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    /**
     * Sets the search request.
     *
     * @param  request  to set
     *
     * @return  this builder
     */
    public Builder request(final SearchRequest request)
    {
      object.setRequest(request);
      return self();
    }


    /**
     * Sets the filter template.
     *
     * @param  template  to set
     *
     * @return  this builder
     */
    public Builder template(final FilterTemplate template)
    {
      object.setTemplate(template);
      return self();
    }


    /**
     * Sets the functions to execute when a search result entry is received.
     *
     * @param  handlers  to execute on a search result entry
     *
     * @return  this builder
     */
    public Builder onEntry(final LdapEntryHandler... handlers)
    {
      object.setEntryHandlers(handlers);
      return self();
    }


    /**
     * Sets the functions to execute when a search result reference is received.
     *
     * @param  handlers  to execute on a search result reference
     *
     * @return  this builder
     */
    public Builder onReference(final SearchReferenceHandler... handlers)
    {
      object.setReferenceHandlers(handlers);
      return self();
    }


    /**
     * Sets the functions to execute when a search result is complete.
     *
     * @param  handlers  to execute on a search result
     *
     * @return  this builder
     */
    public Builder onSearchResult(final SearchResultHandler... handlers)
    {
      object.setSearchResultHandlers(handlers);
      return self();
    }
  }
}
