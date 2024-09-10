/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;
import org.ldaptive.handler.ExceptionHandler;
import org.ldaptive.handler.IntermediateResponseHandler;
import org.ldaptive.handler.ReferralHandler;
import org.ldaptive.handler.RequestHandler;
import org.ldaptive.handler.ResponseControlHandler;
import org.ldaptive.handler.ResultHandler;
import org.ldaptive.handler.ResultPredicate;
import org.ldaptive.handler.UnsolicitedNotificationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for classes that need to configure an operation.
 *
 * @param  <Q>  type of request
 * @param  <S>  type of result
 *
 * @author  Middleware Services
 */
public abstract class AbstractOperationFactory<Q extends Request, S extends Result>  extends AbstractFreezable
  implements ConnectionFactoryManager
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Connection factory. */
  private ConnectionFactory factory;

  /** Functions to handle requests. */
  private RequestHandler<Q>[] requestHandlers;

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
    freeze(referralHandlers);
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
   * Returns the request handlers.
   *
   * @return  request handlers
   */
  public RequestHandler<Q>[] getRequestHandlers()
  {
    return LdapUtils.copyArray(requestHandlers);
  }


  /**
   * Sets the request handlers.
   *
   * @param  handlers  request handler
   */
  @SuppressWarnings("unchecked")
  public void setRequestHandlers(final RequestHandler<Q>... handlers)
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
   * Initializes the supplied operation with the properties configured on this factory.
   *
   * @param  op  operation to initialize
   */
  protected void initializeOperation(final AbstractOperation<Q, S> op)
  {
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
        "unsolicitedNotificationHandlers=" + Arrays.toString(unsolicitedNotificationHandlers) + ", " : "");
  }
}
