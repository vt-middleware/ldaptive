/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import java.nio.ByteBuffer;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.OctetStringType;

/**
 * Response control for authorization identity. See RFC 3829. Control value
 * contains the authorizationId.
 *
 * @author  Middleware Services
 */
public class AuthorizationIdentityResponseControl extends AbstractControl
  implements ResponseControl
{

  /** OID of this control. */
  public static final String OID = "2.16.840.1.113730.3.4.15";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 7019;

  /** Authorization identity. */
  private String authorizationId;


  /** Default constructor. */
  public AuthorizationIdentityResponseControl()
  {
    super(OID);
  }


  /**
   * Creates a new authorization identity response control.
   *
   * @param  critical  whether this control is critical
   */
  public AuthorizationIdentityResponseControl(final boolean critical)
  {
    super(OID, critical);
  }


  /**
   * Creates a new authorization identity response control.
   *
   * @param  id  authorization id
   */
  public AuthorizationIdentityResponseControl(final String id)
  {
    this(id, false);
  }


  /**
   * Creates a new authorization identity response control.
   *
   * @param  id  authorization id
   * @param  critical  whether this control is critical
   */
  public AuthorizationIdentityResponseControl(
    final String id,
    final boolean critical)
  {
    super(OID, critical);
    setAuthorizationId(id);
  }


  /**
   * Returns the authorization id.
   *
   * @return  authorization id
   */
  public String getAuthorizationId()
  {
    return authorizationId;
  }


  /**
   * Sets the authorization identity.
   *
   * @param  id  authorization id
   */
  public void setAuthorizationId(final String id)
  {
    authorizationId = id;
  }


  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        getOID(),
        getCriticality(),
        authorizationId);
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::criticality=%s, authorizationId=%s]",
        getClass().getName(),
        hashCode(),
        getCriticality(),
        authorizationId);
  }


  /** {@inheritDoc} */
  @Override
  public void decode(final byte[] berValue)
  {
    logger.trace("decoding control: {}", LdapUtils.base64Encode(berValue));
    setAuthorizationId(OctetStringType.decode(ByteBuffer.wrap(berValue)));
  }
}
