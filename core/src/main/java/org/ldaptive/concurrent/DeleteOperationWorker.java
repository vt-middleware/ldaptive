/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import java.util.concurrent.ExecutorService;
import org.ldaptive.DeleteOperation;
import org.ldaptive.DeleteRequest;

/**
 * Executes an ldap delete operation on a separate thread.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class DeleteOperationWorker
  extends AbstractOperationWorker<DeleteRequest, Void>
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
  public DeleteOperationWorker(
    final DeleteOperation op,
    final ExecutorService es)
  {
    super(op, es);
  }
}
