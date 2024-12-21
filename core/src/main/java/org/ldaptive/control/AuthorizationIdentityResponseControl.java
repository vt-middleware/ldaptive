/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.OctetStringType;

/**
 * Response control for authorization identity. See RFC 3829. Control value contains the authorizationId.
 *
 * @author  Middleware Services
 */
public class AuthorizationIdentityResponseControl extends AbstractControl implements ResponseControl
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
  public AuthorizationIdentityResponseControl(final String id, final boolean critical)
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


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof AuthorizationIdentityResponseControl && super.equals(o)) {
      final AuthorizationIdentityResponseControl v = (AuthorizationIdentityResponseControl) o;
      return LdapUtils.areEqual(authorizationId, v.authorizationId);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, getOID(), getCriticality(), authorizationId);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "criticality=" + getCriticality() + ", " +
      "authorizationId=" + authorizationId + "]";
  }


  @Override
  public void decode(final DERBuffer encoded)
  {
    try {
      setAuthorizationId(OctetStringType.decode(encoded));
    } catch (Exception e) {
      throw new IllegalArgumentException("Error parsing response", e);
    }
  }
}
