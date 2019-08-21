/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.ModifyResponse;

/**
 * Executes multiple ldap modify operations asynchronously.
 *
 * @author  Middleware Services
 */
public class ModifyOperationWorker extends AbstractOperationWorker<ModifyOperation, ModifyRequest, ModifyResponse>
{


  /**
   * Default constructor.
   */
  public ModifyOperationWorker()
  {
    super(new ModifyOperation());
  }


  /**
   * Creates a new modify operation worker.
   *
   * @param  op  modify operation to execute
   */
  public ModifyOperationWorker(final ModifyOperation op)
  {
    super(op);
  }
}
