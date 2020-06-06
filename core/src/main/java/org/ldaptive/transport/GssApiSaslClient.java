/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.ldaptive.BindResponse;
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
    final LoginModule loginModule;
    try {
      loginModule = (LoginModule) Class.forName(request.getJaasLoginModule()).getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      LOGGER.error("Error creating new instance of JAAS module for GSSAPI", e);
      throw new LoginException(
        "Could not instantiate JAAS module '" + request.getJaasLoginModule() + "' for GSSAPI");
    }

    LOGGER.debug("Invoking module {} for request {}", loginModule, request);
    final Subject subject = new Subject();
    final Map<String, String> state = new HashMap<>();
    loginModule.initialize(subject, request, state, request.getJaasOptions());
    if (loginModule.login()) {
      loginModule.commit();
    } else {
      throw new LoginException("Login failed for " + request + " using " + loginModule);
    }

    final BindResponse result = Subject.doAs(
      subject, (PrivilegedAction<BindResponse>) () -> {
        try {
          return conn.operation(request);
        } catch (Exception e) {
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
