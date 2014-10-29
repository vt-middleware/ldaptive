/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive.concurrent;

import java.util.concurrent.ExecutorService;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;

/**
 * Executes an ldap modify operation on a separate thread.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class ModifyOperationWorker
  extends AbstractOperationWorker<ModifyRequest, Void>
{


  /**
   * Creates a new modify operation worker.
   *
   * @param  op  modify operation to execute
   */
  public ModifyOperationWorker(final ModifyOperation op)
  {
    super(op);
  }


  /**
   * Creates a new modify operation worker.
   *
   * @param  op  modify operation to execute
   * @param  es  executor service
   */
  public ModifyOperationWorker(
    final ModifyOperation op,
    final ExecutorService es)
  {
    super(op, es);
  }
}
