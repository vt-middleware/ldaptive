/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.util.Arrays;
import java.util.Map;
import org.ldaptive.control.RequestControl;

/**
 * Base class for SASL client requests.
 *
 * @author  Middleware Services
 */
public abstract class AbstractSASLClientRequest implements SASLClientRequest
{

  /** LDAP controls. */
  private RequestControl[] controls;


  public RequestControl[] getControls()
  {
    return controls;
  }


  public void setControls(final RequestControl... c)
  {
    controls = c;
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
  public SASLBindRequest createBindRequest(final byte[] saslCredentials)
  {
    final SASLBindRequest req = new SASLBindRequest(getMechanism(), saslCredentials);
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
