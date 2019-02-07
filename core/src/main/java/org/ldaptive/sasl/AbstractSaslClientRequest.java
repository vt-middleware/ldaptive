/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

import java.util.Arrays;
import java.util.Map;
import org.ldaptive.control.RequestControl;

/**
 * Base class for SASL client requests.
 *
 * @author  Middleware Services
 */
public abstract class AbstractSaslClientRequest implements SaslClientRequest
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


  @Override
  public String getAuthorizationID()
  {
    return null;
  }


  @Override
  public Map<String, ?> getSaslProperties()
  {
    return null;
  }


  @Override
  public SaslBindRequest createBindRequest(final byte[] saslCredentials)
  {
    final SaslBindRequest req = new SaslBindRequest(getMechanism(), saslCredentials);
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
