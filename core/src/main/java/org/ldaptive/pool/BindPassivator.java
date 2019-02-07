/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

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
public class BindPassivator implements Passivator<Connection>
{

  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /** Bind request to perform passivation with. */
  private BindRequest bindRequest;


  /** Creates a new bind passivator. */
  public BindPassivator()
  {
    bindRequest = new AnonymousBindRequest();
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
    if (c != null) {
      try {
        final Result result = c.operation(bindRequest).execute();
        return result.isSuccess();
      } catch (Exception e) {
        logger.debug("passivation failed for bind request {}", bindRequest, e);
      }
    }
    return false;
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("bindRequest=").append(bindRequest).append("]").toString();
  }
}
