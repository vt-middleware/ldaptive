/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.security.sasl.SaslException;
import org.ldaptive.BindResponse;
import org.ldaptive.sasl.GssApiBindRequest;
import org.ldaptive.sasl.SaslClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GSSAPI SASL client that implements the JAAS details to perform an LDAP bind with a kerberos principal. If a specific
 * JAAS name is set on the {@link GssApiBindRequest} that configuration will be used. Else if no JAAS configuration
 * properties are supplied a configuration with the name 'ldaptive-gssapi' will be attempted. Otherwise the
 * 'com.sun.security.auth.module.Krb5LoginModule' is instantiated and used with any options provided from {@link
 * GssApiBindRequest}. This allows configuration to occur both from a JAAS login configuration file or by setting
 * properties directly on the request.
 *
 * @author  Middleware Services
 */
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
   * @throws  SaslException  if an error occurs
   */
  public BindResponse bind(final TransportConnection conn, final GssApiBindRequest request)
    throws LoginException, SaslException
  {
    final Subject subject;
    if (request.getJaasName() != null) {
      if (request.getJaasRefreshConfig()) {
        try {
          Configuration.getConfiguration().refresh();
        } catch (Exception e) {
          LOGGER.warn("Could not refresh JAAS configuration", e);
        }
      }
      LOGGER.debug("Invoking JAAS configuration {} for request {}", request.getJaasName() , request);
      final LoginContext context = new LoginContext(request.getJaasName(), request);
      context.login();
      subject = context.getSubject();
    } else {
      final LoginModule loginModule;
      try {
        loginModule = (LoginModule) Class.forName(request.getJaasLoginModule()).getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        LOGGER.error("Error creating new instance of JAAS module for GSSAPI", e);
        throw new SaslException(
          "Could not instantiate JAAS module '" + request.getJaasLoginModule() + "' for GSSAPI", e);
      }

      LOGGER.debug("Invoking module {} for request {}", loginModule, request);
      subject = new Subject();
      final Map<String, String> state = new HashMap<>();
      loginModule.initialize(subject, request, state, request.getJaasOptions());
      if (loginModule.login()) {
        loginModule.commit();
      } else {
        throw new LoginException("Login failed for " + request + " using " + loginModule);
      }
    }

    final Exception[] doAsException = new Exception[1];
    final BindResponse result = Subject.doAs(
      subject, (PrivilegedAction<BindResponse>) () -> {
        try {
          return conn.operation(request);
        } catch (Exception e) {
          LOGGER.warn("SASL GSSAPI operation failed for {} / exception: {}", this, e);
          doAsException[0] = e;
        }
        return null;
      });
    if (result == null) {
      throw new SaslException("SASL GSSAPI operation failed for " + request, doAsException[0]);
    }
    return result;
  }
}
