/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import java.util.concurrent.ExecutorService;
import org.ldaptive.DeleteOperation;
import org.ldaptive.DeleteRequest;
import org.ldaptive.DeleteResponse;

/**
 * Executes an ldap delete operation on a separate thread.
 *
 * @author  Middleware Services
 */
public class DeleteOperationWorker extends AbstractOperationWorker<DeleteRequest, DeleteResponse>
{


  /**
   * Creates a new delete operation worker.
   *
   * @param  op  delete operation to execute
   */
  public DeleteOperationWorker(final DeleteOperation op)
  {
    super(op);
  }


  /**
   * Creates a new delete operation worker.
   *
   * @param  op  delete operation to execute
   * @param  es  executor service
   */
  public DeleteOperationWorker(final DeleteOperation op, final ExecutorService es)
  {
    super(op, es);
  }
}
