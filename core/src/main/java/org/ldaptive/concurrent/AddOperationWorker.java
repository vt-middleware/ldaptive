/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.AddResponse;

/**
 * Executes multiple ldap add operations asynchronously.
 *
 * @author  Middleware Services
 */
public class AddOperationWorker extends AbstractOperationWorker<AddOperation, AddRequest, AddResponse>
{


  /**
   * Default constructor.
   */
  public AddOperationWorker()
  {
    super(new AddOperation());
  }


  /**
   * Creates a new add operation worker.
   *
   * @param  op  add operation to execute
   */
  public AddOperationWorker(final AddOperation op)
  {
    super(op);
  }
}
