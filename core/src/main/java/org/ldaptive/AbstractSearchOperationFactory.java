/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.handler.ExceptionHandler;
import org.ldaptive.handler.IntermediateResponseHandler;
import org.ldaptive.handler.LdapEntryHandler;
import org.ldaptive.handler.ReferralHandler;
import org.ldaptive.handler.RequestHandler;
import org.ldaptive.handler.ResponseControlHandler;
import org.ldaptive.handler.ResultHandler;
import org.ldaptive.handler.ResultPredicate;
import org.ldaptive.handler.SearchReferenceHandler;
import org.ldaptive.handler.SearchResultHandler;
import org.ldaptive.handler.UnsolicitedNotificationHandler;
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

  /** Functions to handle requests. */
  private RequestHandler<SearchRequest>[] requestHandlers;

  /** Functions to handle response results. */
  private ResultHandler[] resultHandlers;

  /** Functions to handle response controls. */
  private ResponseControlHandler[] controlHandlers;

  /** Functions to handle referrals. */
  private ReferralHandler[] referralHandlers;

  /** Functions to handle intermediate responses. */
  private IntermediateResponseHandler[] intermediateResponseHandlers;

  /** Function to handle exceptions. */
  private ExceptionHandler exceptionHandler;

  /** Function to test results. */
  private ResultPredicate throwCondition;

  /** Functions to handle unsolicited notifications. */
  private UnsolicitedNotificationHandler[] unsolicitedNotificationHandlers;

  /** Functions to handle entries. */
  private LdapEntryHandler[] entryHandlers;

  /** Functions to handle response references. */
  private SearchReferenceHandler[] referenceHandlers;

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
   * Returns the search request handlers.
   *
   * @return  search request handlers
   */
  public RequestHandler<SearchRequest>[] getRequestHandlers()
  {
    return requestHandlers;
  }


  /**
   * Sets the search request handlers.
   *
   * @param  handlers  search request handler
   */
  @SuppressWarnings("unchecked")
  public void setRequestHandlers(final RequestHandler<SearchRequest>... handlers)
  {
    requestHandlers = handlers;
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
   * Returns the control handlers.
   *
   * @return  control handlers
   */
  public ResponseControlHandler[] getControlHandlers()
  {
    return controlHandlers;
  }


  /**
   * Sets the control handlers.
   *
   * @param  handlers  control handlers
   */
  public void setControlHandlers(final ResponseControlHandler... handlers)
  {
    controlHandlers = handlers;
  }


  /**
   * Returns the referral handlers.
   *
   * @return  referral handlers
   */
  public ReferralHandler[] getReferralHandlers()
  {
    return referralHandlers;
  }


  /**
   * Sets the referral handlers.
   *
   * @param  handlers  referral handlers
   */
  public void setReferralHandlers(final ReferralHandler... handlers)
  {
    referralHandlers = handlers;
  }


  /**
   * Returns the intermediate response handlers.
   *
   * @return  intermediate response handlers
   */
  public IntermediateResponseHandler[] getIntermediateResponseHandlers()
  {
    return intermediateResponseHandlers;
  }


  /**
   * Sets the intermediate response handlers.
   *
   * @param  handlers  intermediate response handlers
   */
  public void setIntermediateResponseHandlers(final IntermediateResponseHandler... handlers)
  {
    intermediateResponseHandlers = handlers;
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
   * Returns the throw condition.
   *
   * @return  throw condition
   */
  public ResultPredicate getThrowCondition()
  {
    return throwCondition;
  }


  /**
   * Sets the throw condition.
   *
   * @param  function  throw condition
   */
  public void setThrowCondition(final ResultPredicate function)
  {
    throwCondition = function;
  }


  /**
   * Returns the unsolicited notification handlers.
   *
   * @return  unsolicited notification handlers
   */
  public UnsolicitedNotificationHandler[] getUnsolicitedNotificationHandlers()
  {
    return unsolicitedNotificationHandlers;
  }


  /**
   * Sets the unsolicited notification handlers.
   *
   * @param  handlers  unsolicited notification handlers
   */
  public void setUnsolicitedNotificationHandlers(final UnsolicitedNotificationHandler... handlers)
  {
    unsolicitedNotificationHandlers = handlers;
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
   * Returns the search reference handlers.
   *
   * @return  search reference handlers
   */
  public SearchReferenceHandler[] getReferenceHandlers()
  {
    return referenceHandlers;
  }


  /**
   * Sets the search reference handlers.
   *
   * @param  handlers  search reference handlers
   */
  public void setReferenceHandlers(final SearchReferenceHandler... handlers)
  {
    referenceHandlers = handlers;
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
    if (requestHandlers != null) {
      op.setRequestHandlers(requestHandlers);
    }
    if (resultHandlers != null) {
      op.setResultHandlers(resultHandlers);
    }
    if (controlHandlers != null) {
      op.setControlHandlers(controlHandlers);
    }
    if (referralHandlers != null) {
      op.setReferralHandlers(referralHandlers);
    }
    if (intermediateResponseHandlers != null) {
      op.setIntermediateResponseHandlers(intermediateResponseHandlers);
    }
    if (exceptionHandler != null) {
      op.setExceptionHandler(exceptionHandler);
    }
    if (throwCondition != null) {
      op.setThrowCondition(throwCondition);
    }
    if (unsolicitedNotificationHandlers != null) {
      op.setUnsolicitedNotificationHandlers(unsolicitedNotificationHandlers);
    }
    if (entryHandlers != null) {
      op.setEntryHandlers(entryHandlers);
    }
    if (referenceHandlers != null) {
      op.setReferenceHandlers(referenceHandlers);
    }
    if (searchResultHandlers != null) {
      op.setSearchResultHandlers(searchResultHandlers);
    }
    return op;
  }
}
