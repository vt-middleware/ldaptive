/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import org.ldaptive.BindResponse;
import org.ldaptive.ResultCode;
import org.ldaptive.sasl.DefaultSaslClientRequest;
import org.ldaptive.sasl.Mechanism;
import org.ldaptive.sasl.QualityOfProtection;
import org.ldaptive.sasl.SaslClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SASL client that negotiates the details of the bind operation.
 *
 * @author  Middleware Services
 */
public class DefaultSaslClient implements SaslClient<DefaultSaslClientRequest>
{

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSaslClient.class);

  /** Underlying SASL client. */
  private javax.security.sasl.SaslClient client;


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
  @Override
  public BindResponse bind(final TransportConnection conn, final DefaultSaslClientRequest request)
    throws SaslException
  {
    BindResponse response;
    final String serverName = conn.getLdapURL().getHostname();
    try {
      client = Sasl.createSaslClient(
        new String[]{request.getMechanism().mechanism()},
        request.getAuthorizationID(),
        "ldap",
        serverName,
        request.getSaslProperties(),
        request);

      byte[] bytes = client.hasInitialResponse() ? client.evaluateChallenge(new byte[0]) : null;
      response = conn.operation(request.createBindRequest(bytes)).execute();
      if (ResultCode.SASL_BIND_IN_PROGRESS != response.getResultCode()) {
        return response;
      }
      while (!client.isComplete() && ResultCode.SASL_BIND_IN_PROGRESS == response.getResultCode()) {
        bytes = client.evaluateChallenge(response.getServerSaslCreds());
        response = conn.operation(request.createBindRequest(bytes)).execute();
      }
      if (ResultCode.SASL_BIND_IN_PROGRESS == response.getResultCode()) {
        throw new SaslException(
          "SASL client error: client completed but bind still in progress for " + request + " with " + response);
      }
      if (!client.isComplete() && response.getServerSaslCreds() != null) {
        // server may return additional data for the client in the last response
        client.evaluateChallenge(response.getServerSaslCreds());
      }
      if (!client.isComplete() && ResultCode.SUCCESS == response.getResultCode()) {
        throw new SaslException("SASL client error: client did not complete for " + request + " with " + response);
      }
      return response;
    } catch (SaslException e) {
      dispose();
      throw e;
    } catch (Throwable e) {
      dispose();
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
   * @return  QOP or null if the underlying sasl client has not completed
   */
  public QualityOfProtection getQualityOfProtection()
  {
    if (!client.isComplete()) {
      return null;
    }
    return QualityOfProtection.fromString((String) client.getNegotiatedProperty(Sasl.QOP));
  }


  /**
   * Disposes the underlying SASL client. See {@link javax.security.sasl.SaslClient#dispose()}.
   */
  public void dispose()
  {
    if (client != null) {
      try {
        client.dispose();
      } catch (SaslException e) {
        LOGGER.warn("Error disposing of SASL client", e);
      } finally {
        client = null;
      }
    }
  }
}
