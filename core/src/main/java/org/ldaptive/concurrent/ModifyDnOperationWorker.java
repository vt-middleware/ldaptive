/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import org.ldaptive.ModifyDnOperation;
import org.ldaptive.ModifyDnRequest;
import org.ldaptive.ModifyDnResponse;

/**
 * Executes multiple ldap modify DN operations asynchronously.
 *
 * @author  Middleware Services
 */
public class ModifyDnOperationWorker
  extends AbstractOperationWorker<ModifyDnOperation, ModifyDnRequest, ModifyDnResponse>
{


  /**
   * Default constructor.
   */
  public ModifyDnOperationWorker()
  {
    super(new ModifyDnOperation());
  }


  /**
   * Creates a new modify dn operation worker.
   *
   * @param  op  modify dn operation to execute
   */
  public ModifyDnOperationWorker(final ModifyDnOperation op)
  {
    super(op);
  }
}
