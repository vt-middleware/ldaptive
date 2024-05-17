/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import org.ldaptive.AbstractFreezable;
import org.ldaptive.AnonymousBindRequest;
import org.ldaptive.BindRequest;
import org.ldaptive.Connection;
import org.ldaptive.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Passivates a connection by performing a bind operation on it.
 *
 * @author  Middleware Services
 */
public class BindConnectionPassivator extends AbstractFreezable implements ConnectionPassivator
{

  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /** Bind request to perform passivation with. */
  private BindRequest bindRequest;


  /** Creates a new bind passivator. */
  public BindConnectionPassivator()
  {
    this(new AnonymousBindRequest());
  }


  /**
   * Creates a new bind passivator.
   *
   * @param  br  to use for binds
   */
  public BindConnectionPassivator(final BindRequest br)
  {
    bindRequest = br;
  }


  /**
   * Returns the bind request.
   *
   * @return  bind request
   */
  public final BindRequest getBindRequest()
  {
    return bindRequest;
  }


  /**
   * Sets the bind request.
   *
   * @param  br  bind request
   */
  public final void setBindRequest(final BindRequest br)
  {
    assertMutable();
    bindRequest = br;
  }


  @Override
  public Boolean apply(final Connection conn)
  {
    if (conn != null) {
      try {
        final Result result = conn.operation(bindRequest).execute();
        return result.isSuccess();
      } catch (Exception e) {
        logger.debug("Passivation failed for bind request {}", bindRequest, e);
      }
    }
    return false;
  }


  @Override
  public String toString()
  {
    return "[" + getClass().getName() + "@" + hashCode() + "::" + "bindRequest=" + bindRequest + "]";
  }
}
