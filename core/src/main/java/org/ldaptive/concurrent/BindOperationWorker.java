/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import java.util.concurrent.ExecutorService;
import org.ldaptive.BindOperation;
import org.ldaptive.BindRequest;
import org.ldaptive.BindResponse;

/**
 * Executes an ldap bind operation on a separate thread.
 *
 * @author  Middleware Services
 */
public class BindOperationWorker extends AbstractOperationWorker<BindRequest, BindResponse>
{


  /**
   * Creates a new bind operation worker.
   *
   * @param  op  bind operation to execute
   */
  public BindOperationWorker(final BindOperation op)
  {
    super(op);
  }


  /**
   * Creates a new bind operation worker.
   *
   * @param  op  bind operation to execute
   * @param  es  executor service
   */
  public BindOperationWorker(final BindOperation op, final ExecutorService es)
  {
    super(op, es);
  }
}
