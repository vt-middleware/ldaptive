/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.Request;
import org.ldaptive.Response;
import org.ldaptive.handler.Handler;
import org.ldaptive.handler.HandlerResult;

/**
 * Provides handling of an ldap referral.
 *
 * @param  <Q>  type of ldap request
 * @param  <S>  type of ldap response
 *
 * @author  Middleware Services
 */
public interface ReferralHandler<Q extends Request, S> extends Handler<Q, Response<S>>
{


  @Override
  HandlerResult<Response<S>> handle(Connection conn, Q request, Response<S> response)
    throws LdapException;


  /**
   * Initialize the request for use with this referral handler.
   *
   * @param  request  to initialize for this referral handler
   */
  void initializeRequest(Q request);
}
