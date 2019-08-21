/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import org.ldaptive.CompareOperation;
import org.ldaptive.CompareRequest;
import org.ldaptive.CompareResponse;

/**
 * Executes multiple ldap compare operations asynchronously.
 *
 * @author  Middleware Services
 */
public class CompareOperationWorker extends AbstractOperationWorker<CompareOperation, CompareRequest, CompareResponse>
{


  /**
   * Default constructor.
   */
  public CompareOperationWorker()
  {
    super(new CompareOperation());
  }


  /**
   * Creates a new compare operation worker.
   *
   * @param  op  compare operation to execute
   */
  public CompareOperationWorker(final CompareOperation op)
  {
    super(op);
  }
}
