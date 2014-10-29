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

import java.util.Collection;
import java.util.concurrent.Future;
import org.ldaptive.Request;
import org.ldaptive.Response;

/**
 * Interface for ldap operation workers. Operation workers leverage the
 * java.util.concurrent package to execute non-blocking operations.
 *
 * @param  <Q>  type of ldap request
 * @param  <S>  type of ldap response
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface OperationWorker<Q extends Request, S>
{


  /**
   * Execute an ldap operation on a separate thread.
   *
   * @param  request  containing the data required by this operation
   *
   * @return  future response for this operation
   */
  Future<Response<S>> execute(Q request);


  /**
   * Execute an ldap operation for each request on a separate thread.
   *
   * @param  requests  containing the data required by this operation
   *
   * @return  future responses for this operation
   */
  Collection<Future<Response<S>>> execute(Q... requests);


  /**
   * Execute an ldap operation for each request on a separate thread and waits
   * for each operation to complete.
   *
   * @param  requests  containing the data required by this operation
   *
   * @return  responses for this operation
   */
  Collection<Response<S>> executeToCompletion(Q... requests);
}
