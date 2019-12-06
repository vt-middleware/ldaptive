/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

import java.util.Arrays;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import org.ldaptive.control.RequestControl;
import org.ldaptive.transport.DefaultSaslClient;

/**
 * Base class for SASL client requests.
 *
 * @author  Middleware Services
 */
// CheckStyle:AbstractClassName OFF
public abstract class DefaultSaslClientRequest implements CallbackHandler
// CheckStyle:AbstractClassName ON
{

  /** LDAP controls. */
  private RequestControl[] controls;


  public RequestControl[] getControls()
  {
    return controls;
  }


  public void setControls(final RequestControl... cntrls)
  {
    controls = cntrls;
  }


  /**
   * Returns the SASL mechanism.
   *
   * @return  SASL mechanism
   */
  public abstract Mechanism getMechanism();


  /**
   * Returns the SASL authorization.
   *
   * @return  SASL authorization
   */
  public String getAuthorizationID()
  {
    return null;
  }


  /**
   * Returns the SASL properties.
   *
   * @return  SASL properties
   */
  public Map<String, ?> getSaslProperties()
  {
    return null;
  }


  /**
   * Returns the SASL client to use for this request.
   *
   * @return  SASL client
   */
  public SaslClient getSaslClient()
  {
    return new DefaultSaslClient();
  }


  /**
   * Creates a new bind request for this client.
   *
   * @param  saslCredentials  to bind with
   *
   * @return  SASL bind request
   */
  public SaslBindRequest createBindRequest(final byte[] saslCredentials)
  {
    final SaslBindRequest req = new SaslBindRequest(getMechanism().mechanism(), saslCredentials);
    req.setControls(getControls());
    return req;
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("controls=").append(Arrays.toString(controls)).toString();
  }
}
