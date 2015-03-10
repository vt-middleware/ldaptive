/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import org.ldaptive.BindOperation;
import org.ldaptive.BindRequest;
import org.ldaptive.Connection;
import org.ldaptive.Response;
import org.ldaptive.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Passivates a connection by performing a bind operation on it.
 *
 * @author  Middleware Services
 */
public class BindPassivator implements Passivator<Connection>
{

  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /** Bind request to perform passivation with. */
  private BindRequest bindRequest;


  /** Creates a new bind passivator. */
  public BindPassivator()
  {
    bindRequest = new BindRequest();
  }


  /**
   * Creates a new bind passivator.
   *
   * @param  br  to use for binds
   */
  public BindPassivator(final BindRequest br)
  {
    bindRequest = br;
  }


  /**
   * Returns the bind request.
   *
   * @return  bind request
   */
  public BindRequest getBindRequest()
  {
    return bindRequest;
  }


  /**
   * Sets the bind request.
   *
   * @param  br  bind request
   */
  public void setBindRequest(final BindRequest br)
  {
    bindRequest = br;
  }


  @Override
  public boolean passivate(final Connection c)
  {
    boolean success = false;
    if (c != null) {
      try {
        final BindOperation bind = new BindOperation(c);
        final Response<Void> response = bind.execute(bindRequest);
        success = ResultCode.SUCCESS == response.getResultCode();
      } catch (Exception e) {
        logger.debug("passivation failed for bind request {}", bindRequest, e);
      }
    }
    return success;
  }


  @Override
  public String toString()
  {
    return String.format("[%s@%d::bindRequest=%s]", getClass().getName(), hashCode(), bindRequest);
  }
}
