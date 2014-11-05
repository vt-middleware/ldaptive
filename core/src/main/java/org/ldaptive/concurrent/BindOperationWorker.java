/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import java.util.concurrent.ExecutorService;
import org.ldaptive.BindOperation;
import org.ldaptive.BindRequest;

/**
 * Executes an ldap bind operation on a separate thread.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class BindOperationWorker
  extends AbstractOperationWorker<BindRequest, Void>
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
