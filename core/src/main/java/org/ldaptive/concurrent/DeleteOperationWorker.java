/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import org.ldaptive.DeleteOperation;
import org.ldaptive.DeleteRequest;
import org.ldaptive.DeleteResponse;

/**
 * Executes multiple ldap delete operations asynchronously.
 *
 * @author  Middleware Services
 */
public class DeleteOperationWorker extends AbstractOperationWorker<DeleteOperation, DeleteRequest, DeleteResponse>
{


  /**
   * Default constructor.
   */
  public DeleteOperationWorker()
  {
    super(new DeleteOperation());
  }


  /**
   * Creates a new delete operation worker.
   *
   * @param  op  delete operation to execute
   */
  public DeleteOperationWorker(final DeleteOperation op)
  {
    super(op);
  }
}
