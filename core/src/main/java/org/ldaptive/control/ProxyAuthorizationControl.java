/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.OctetStringType;

/**
 * Request control for proxy authorization. See RFC 4370. Control is defined as:
 *
 * <pre>
   controlValue ::= OCTET STRING  -- authorizationId
 * </pre>
 *
 * @author  Middleware Services
 */
public class ProxyAuthorizationControl extends AbstractControl implements RequestControl
{

  /** OID of this control. */
  public static final String OID = "2.16.840.1.113730.3.4.18";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 7001;

  /** empty byte array used for anonymous authz. */
  private static final byte[] EMPTY_AUTHZ = new byte[0];

  /** authorization identity. */
  private String authorizationId;


  /** Default constructor. */
  public ProxyAuthorizationControl()
  {
    super(OID, true);
  }


  /**
   * Creates a new proxy authorization control.
   *
   * @param  id  authorization identity
   */
  public ProxyAuthorizationControl(final String id)
  {
    super(OID, true);
    setAuthorizationId(id);
  }


  @Override
  public boolean hasValue()
  {
    return true;
  }


  /**
   * Returns the authorization identity.
   *
   * @return  authorization identity
   */
  public String getAuthorizationId()
  {
    return authorizationId;
  }


  /**
   * Sets the authorization identity.
   *
   * @param  id  authorization identity
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
    if (o instanceof ProxyAuthorizationControl && super.equals(o)) {
      final ProxyAuthorizationControl v = (ProxyAuthorizationControl) o;
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
  public byte[] encode()
  {
    return authorizationId != null ? OctetStringType.toBytes(authorizationId) : EMPTY_AUTHZ;
  }
}
