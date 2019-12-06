/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import java.security.PrivilegedAction;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.ldaptive.BindResponse;
import org.ldaptive.LdapException;
import org.ldaptive.sasl.GssApiBindRequest;
import org.ldaptive.sasl.SaslClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GssApiSaslClient implements SaslClient<GssApiBindRequest>
{

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(GssApiSaslClient.class);


  /**
   * Performs a GSSAPI SASL bind.
   *
   * @param  conn  to perform the bind on
   * @param  request  SASL request to perform
   *
   * @return  final result of the bind process
   *
   * @throws  LoginException  if an error occurs
   */
  public BindResponse bind(final TransportConnection conn, final GssApiBindRequest request)
    throws LoginException
  {
    final LoginContext context = new LoginContext("ldaptive-gssapi", request);
    context.login();
    final BindResponse result = Subject.doAs(
      context.getSubject(), (PrivilegedAction<BindResponse>) () -> {
        try {
          return conn.operation(request);
        } catch (LdapException e) {
          LOGGER.warn("SASL GSSAPI operation failed for {}", this, e);
        }
        return null;
      });
    if (result == null) {
      throw new LoginException("SASL GSSAPI operation failed");
    }
    return result;
  }
}
