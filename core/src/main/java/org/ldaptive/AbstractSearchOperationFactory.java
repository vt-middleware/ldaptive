/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.handler.ExceptionHandler;
import org.ldaptive.handler.LdapEntryHandler;
import org.ldaptive.handler.ResultHandler;
import org.ldaptive.handler.SearchResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for classes that perform searches.
 *
 * @author  Middleware Services
 */
public abstract class AbstractSearchOperationFactory implements ConnectionFactoryManager
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Connection factory. */
  private ConnectionFactory factory;

  /** Functions to handle entries. */
  private LdapEntryHandler[] entryHandlers;

  /** Functions to handle exceptions. */
  private ExceptionHandler exceptionHandler;

  /** Functions to handle response results. */
  private ResultHandler[] resultHandlers;

  /** Functions to handle search response results. */
  private SearchResultHandler[] searchResultHandlers;


  /**
   * Returns the connection factory.
   *
   * @return  connection factory
   */
  public ConnectionFactory getConnectionFactory()
  {
    return factory;
  }


  /**
   * Sets the connection factory.
   *
   * @param  cf  connection factory
   */
  public void setConnectionFactory(final ConnectionFactory cf)
  {
    factory = cf;
  }


  /**
   * Returns the search entry handlers.
   *
   * @return  search entry handlers
   */
  public LdapEntryHandler[] getEntryHandlers()
  {
    return entryHandlers;
  }


  /**
   * Sets the search entry handlers.
   *
   * @param  handlers  search entry handlers
   */
  public void setEntryHandlers(final LdapEntryHandler... handlers)
  {
    entryHandlers = handlers;
  }


  /**
   * Returns the search exception handler.
   *
   * @return  search exception handler
   */
  public ExceptionHandler getExceptionHandler()
  {
    return exceptionHandler;
  }


  /**
   * Sets the search exception handler.
   *
   * @param  handler  search exception handler
   */
  public void setExceptionHandler(final ExceptionHandler handler)
  {
    exceptionHandler = handler;
  }


  /**
   * Returns the search result handlers.
   *
   * @return  search result handlers
   */
  public ResultHandler[] getResultHandlers()
  {
    return resultHandlers;
  }


  /**
   * Sets the search result handlers.
   *
   * @param  handlers  search result handlers
   */
  public void setResultHandlers(final ResultHandler... handlers)
  {
    resultHandlers = handlers;
  }


  /**
   * Returns the search result handlers.
   *
   * @return  search result handlers
   */
  public SearchResultHandler[] getSearchResultHandlers()
  {
    return searchResultHandlers;
  }


  /**
   * Sets the search result handlers.
   *
   * @param  handlers  search result handlers
   */
  public void setSearchResultHandlers(final SearchResultHandler... handlers)
  {
    searchResultHandlers = handlers;
  }


  /**
   * Creates a new search operation configured with the properties on this factory.
   *
   * @return  search operation
   */
  protected SearchOperation createSearchOperation()
  {
    return createSearchOperation(factory);
  }


  /**
   * Creates a new search operation configured with the properties on this factory.
   *
   * @param  cf  connection factory to set on the search operation
   *
   * @return  search operation
   */
  protected SearchOperation createSearchOperation(final ConnectionFactory cf)
  {
    final SearchOperation op = new SearchOperation(cf);
    if (entryHandlers != null) {
      op.setEntryHandlers(entryHandlers);
    }
    if (exceptionHandler != null) {
      op.setExceptionHandler(exceptionHandler);
    }
    if (resultHandlers != null) {
      op.setResultHandlers(resultHandlers);
    }
    if (searchResultHandlers != null) {
      op.setSearchResultHandlers(searchResultHandlers);
    }
    return op;
  }
}
