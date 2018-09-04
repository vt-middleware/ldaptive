/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import org.ldaptive.Connection;
import org.ldaptive.LdapUtils;
import org.ldaptive.Response;
import org.ldaptive.ResultCode;
import org.ldaptive.control.ResponseControl;

/**
 * Response object for authentication handlers.
 *
 * @author  Middleware Services
 */
public class AuthenticationHandlerResponse extends Response<Boolean>
{

  /** Connection that authentication occurred on. */
  private final Connection connection;


  /**
   * Creates a new authentication response.
   *
   * @param  success  authentication result
   * @param  rc  result code from the underlying ldap operation
   * @param  conn  connection the authentication occurred on
   */
  public AuthenticationHandlerResponse(final boolean success, final ResultCode rc, final Connection conn)
  {
    super(success, rc);
    connection = conn;
  }


  /**
   * Creates a new authentication response.
   *
   * @param  success  authentication result
   * @param  rc  result code from the underlying ldap operation
   * @param  conn  connection the authentication occurred on
   * @param  msg  authentication message
   */
  public AuthenticationHandlerResponse(
    final boolean success,
    final ResultCode rc,
    final Connection conn,
    final String msg)
  {
    super(success, rc, msg, null, null, null, -1);
    connection = conn;
  }


  /**
   * Creates a new ldap response.
   *
   * @param  success  authentication result
   * @param  rc  result code from the underlying ldap operation
   * @param  conn  connection the authentication occurred on
   * @param  msg  authentication message
   * @param  controls  response controls from the underlying ldap operation
   * @param  msgId  message id from the underlying ldap operation
   */
  public AuthenticationHandlerResponse(
    final boolean success,
    final ResultCode rc,
    final Connection conn,
    final String msg,
    final ResponseControl[] controls,
    final int msgId)
  {
    super(success, rc, msg, null, controls, null, msgId);
    connection = conn;
  }


  /**
   * Returns the connection that the ldap operation occurred on.
   *
   * @return  connection
   */
  public Connection getConnection()
  {
    return connection;
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::connection=%s, result=%s, resultCode=%s, message=%s, controls=%s]",
        getClass().getName(),
        hashCode(),
        connection,
        getResult(),
        getResultCode(),
        encodeCntrlChars ? LdapUtils.percentEncodeControlChars(getMessage()) : getMessage(),
        Arrays.toString(getControls()));
  }
}
