/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

import org.ldaptive.BindResponse;
import org.ldaptive.transport.TransportConnection;

/**
 * SASL client that negotiates the details of the bind operation.
 *
 * @param  <T>  type of request
 *
 * @author  Middleware Services
 */
public interface SaslClient<T>
{


  /**
   * Performs a SASL bind.
   *
   * @param  conn  to perform the bind on
   * @param  request  SASL request to perform
   *
   * @return  final result of the bind process
   *
   * @throws  Exception  if an error occurs
   */
  BindResponse bind(TransportConnection conn, T request)
    throws Exception;
}
