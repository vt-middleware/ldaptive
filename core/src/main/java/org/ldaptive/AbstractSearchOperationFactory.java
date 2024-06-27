/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;
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
public abstract class AbstractSearchOperationFactory extends AbstractFreezable implements ConnectionFactoryManager
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


  @Override
  public void freeze()
  {
    super.freeze();
    freeze(factory);
    freeze(requestHandlers);
    freeze(resultHandlers);
    freeze(controlHandlers);
    freeze(referralHandlers);
    freeze(intermediateResponseHandlers);
    freeze(exceptionHandler);
    freeze(unsolicitedNotificationHandlers);
    freeze(entryHandlers);
    freeze(referralHandlers);
    freeze(searchResultHandlers);
  }


  /**
   * Returns the connection factory.
   *
   * @return  connection factory
   */
  @Override
  public ConnectionFactory getConnectionFactory()
  {
    return factory;
  }


  /**
   * Sets the connection factory.
   *
   * @param  cf  connection factory
   */
  @Override
  public void setConnectionFactory(final ConnectionFactory cf)
  {
    assertMutable();
    factory = cf;
  }


  /**
   * Returns the search request handlers.
   *
   * @return  search request handlers
   */
  public RequestHandler<SearchRequest>[] getRequestHandlers()
  {
    return LdapUtils.copyArray(requestHandlers);
  }


  /**
   * Sets the search request handlers.
   *
   * @param  handlers  search request handler
   */
  @SuppressWarnings("unchecked")
  public void setRequestHandlers(final RequestHandler<SearchRequest>... handlers)
  {
    assertMutable();
    requestHandlers = LdapUtils.copyArray(handlers);
  }


  /**
   * Returns the search result handlers.
   *
   * @return  search result handlers
   */
  public ResultHandler[] getResultHandlers()
  {
    return LdapUtils.copyArray(resultHandlers);
  }


  /**
   * Sets the search result handlers.
   *
   * @param  handlers  search result handlers
   */
  public void setResultHandlers(final ResultHandler... handlers)
  {
    assertMutable();
    resultHandlers = LdapUtils.copyArray(handlers);
  }


  /**
   * Returns the control handlers.
   *
   * @return  control handlers
   */
  public ResponseControlHandler[] getControlHandlers()
  {
    return LdapUtils.copyArray(controlHandlers);
  }


  /**
   * Sets the control handlers.
   *
   * @param  handlers  control handlers
   */
  public void setControlHandlers(final ResponseControlHandler... handlers)
  {
    assertMutable();
    controlHandlers = LdapUtils.copyArray(handlers);
  }


  /**
   * Returns the referral handlers.
   *
   * @return  referral handlers
   */
  public ReferralHandler[] getReferralHandlers()
  {
    return LdapUtils.copyArray(referralHandlers);
  }


  /**
   * Sets the referral handlers.
   *
   * @param  handlers  referral handlers
   */
  public void setReferralHandlers(final ReferralHandler... handlers)
  {
    assertMutable();
    referralHandlers = LdapUtils.copyArray(handlers);
  }


  /**
   * Returns the intermediate response handlers.
   *
   * @return  intermediate response handlers
   */
  public IntermediateResponseHandler[] getIntermediateResponseHandlers()
  {
    return LdapUtils.copyArray(intermediateResponseHandlers);
  }


  /**
   * Sets the intermediate response handlers.
   *
   * @param  handlers  intermediate response handlers
   */
  public void setIntermediateResponseHandlers(final IntermediateResponseHandler... handlers)
  {
    assertMutable();
    intermediateResponseHandlers = LdapUtils.copyArray(handlers);
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
    assertMutable();
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
    assertMutable();
    throwCondition = function;
  }


  /**
   * Returns the unsolicited notification handlers.
   *
   * @return  unsolicited notification handlers
   */
  public UnsolicitedNotificationHandler[] getUnsolicitedNotificationHandlers()
  {
    return LdapUtils.copyArray(unsolicitedNotificationHandlers);
  }


  /**
   * Sets the unsolicited notification handlers.
   *
   * @param  handlers  unsolicited notification handlers
   */
  public void setUnsolicitedNotificationHandlers(final UnsolicitedNotificationHandler... handlers)
  {
    assertMutable();
    unsolicitedNotificationHandlers = LdapUtils.copyArray(handlers);
  }


  /**
   * Returns the search entry handlers.
   *
   * @return  search entry handlers
   */
  public LdapEntryHandler[] getEntryHandlers()
  {
    return LdapUtils.copyArray(entryHandlers);
  }


  /**
   * Sets the search entry handlers.
   *
   * @param  handlers  search entry handlers
   */
  public void setEntryHandlers(final LdapEntryHandler... handlers)
  {
    assertMutable();
    entryHandlers = LdapUtils.copyArray(handlers);
  }


  /**
   * Returns the search reference handlers.
   *
   * @return  search reference handlers
   */
  public SearchReferenceHandler[] getReferenceHandlers()
  {
    return LdapUtils.copyArray(referenceHandlers);
  }


  /**
   * Sets the search reference handlers.
   *
   * @param  handlers  search reference handlers
   */
  public void setReferenceHandlers(final SearchReferenceHandler... handlers)
  {
    assertMutable();
    referenceHandlers = LdapUtils.copyArray(handlers);
  }


  /**
   * Returns the search result handlers.
   *
   * @return  search result handlers
   */
  public SearchResultHandler[] getSearchResultHandlers()
  {
    return LdapUtils.copyArray(searchResultHandlers);
  }


  /**
   * Sets the search result handlers.
   *
   * @param  handlers  search result handlers
   */
  public void setSearchResultHandlers(final SearchResultHandler... handlers)
  {
    assertMutable();
    searchResultHandlers = LdapUtils.copyArray(handlers);
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


  @Override
  public String toString()
  {
    return getClass().getName() + "@" + hashCode() + "::" +
      "factory=" + factory + ", " +
      (requestHandlers != null ? "requestHandlers=" + Arrays.toString(requestHandlers) + ", " : "") +
      (resultHandlers != null ? "resultHandlers=" + Arrays.toString(resultHandlers) + ", " : "") +
      (controlHandlers != null ? "controlHandlers=" + Arrays.toString(controlHandlers) + ", " : "") +
      (referralHandlers != null ? "referralHandlers=" + Arrays.toString(referralHandlers) + ", " : "") +
      (intermediateResponseHandlers != null ?
        "intermediateResponseHandlers=" + Arrays.toString(intermediateResponseHandlers) + ", " : "") +
      (exceptionHandler != null ? "exceptionHandler=" + exceptionHandler + ", " : "") +
      (throwCondition != null ? "throwCondition=" + throwCondition + ", " : "") +
      (unsolicitedNotificationHandlers != null ?
        "unsolicitedNotificationHandlers=" + Arrays.toString(unsolicitedNotificationHandlers) + ", " : "") +
      (entryHandlers != null ? "entryHandlers=" + Arrays.toString(entryHandlers) + ", " : "") +
      (referenceHandlers != null ? "referenceHandlers=" + Arrays.toString(referenceHandlers) + ", " : "") +
      (searchResultHandlers != null ? "searchResultHandlers=" + Arrays.toString(searchResultHandlers) : "");
  }
}
