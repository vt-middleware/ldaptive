/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import java.nio.charset.StandardCharsets;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import org.ldaptive.BindResponse;
import org.ldaptive.ResultCode;
import org.ldaptive.sasl.Mechanism;
import org.ldaptive.sasl.QualityOfProtection;
import org.ldaptive.sasl.SaslClientRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SASL client that negotiates the details of the bind operation.
 *
 * @author  Middleware Services
 */
public class SaslClient
{

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(SaslClient.class);

  /** SASL server hostname. */
  private final String serverName;

  /** Underlying SASL client. */
  private javax.security.sasl.SaslClient client;


  /**
   * Creates a new SASL client.
   *
   * @param  host  SASL server hostname
   */
  public SaslClient(final String host)
  {
    serverName = host;
  }


  /**
   * Returns the underlying SASL client.
   *
   * @return  SASL client
   */
  public javax.security.sasl.SaslClient getClient()
  {
    return client;
  }


  /**
   * Performs a SASL bind.
   *
   * @param  conn  to perform the bind on
   * @param  request  SASL request to perform
   *
   * @return  final result of the bind process
   *
   * @throws  SaslException  if an error occurs
   */
  public BindResponse bind(final ProviderConnection conn, final SaslClientRequest request)
    throws SaslException
  {
    BindResponse response;
    try {
      client = Sasl.createSaslClient(
        new String[]{request.getMechanism()},
        request.getAuthorizationID(),
        "ldap",
        serverName,
        request.getSaslProperties(),
        request);

      byte[] bytes = client.hasInitialResponse() ? client.evaluateChallenge(new byte[0]) : null;
      response = conn.operation(request.createBindRequest(bytes)).execute();
      while (!client.isComplete() &&
        (ResultCode.SASL_BIND_IN_PROGRESS == response.getResultCode() ||
          ResultCode.SUCCESS == response.getResultCode())) {
        bytes = client.evaluateChallenge(response.getServerSaslCreds().getBytes(StandardCharsets.UTF_8));
        if (ResultCode.SUCCESS == response.getResultCode()) {
          if (bytes != null) {
            throw new SaslException("SASL client error: received response after completion");
          }
          break;
        }
        response = conn.operation(request.createBindRequest(bytes)).execute();
      }
      return response;
    } catch (Throwable e) {
      dispose();
      if (e instanceof SaslException) {
        throw (SaslException) e;
      }
      throw new SaslException("SASL bind failed for " + request, e);
    }
  }


  /**
   * Returns the SASL mechanism for this client. See {@link javax.security.sasl.SaslClient#getMechanismName()}.
   *
   * @return  SASL mechanism
   */
  public Mechanism getMechanism()
  {
    return Mechanism.valueOf(client.getMechanismName());
  }


  /**
   * Returns the QOP for this client. See {@link javax.security.sasl.SaslClient#getNegotiatedProperty(String)}.
   *
   * @return  QOP
   */
  public QualityOfProtection getQualityOfProtection()
  {
    return QualityOfProtection.fromString((String) client.getNegotiatedProperty(Sasl.QOP));
  }


  /**
   * Disposes the underly SASL client. See {@link javax.security.sasl.SaslClient#dispose()}.
   */
  public void dispose()
  {
    if (client != null) {
      try {
        client.dispose();
      } catch (SaslException se) {
        LOGGER.warn("Error disposing of SASL client", se);
      } finally {
        client = null;
      }
    }
  }
}
