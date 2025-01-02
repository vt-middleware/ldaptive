/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.OctetStringType;

/**
 * Response control indicating a password that will expire. See
 * http://tools.ietf.org/html/draft-vchu-ldap-pwd-policy-00. Control is defined as:
 *
 * <pre>
   controlValue ::= secondsUntilExpiration  OCTET STRING
 * </pre>
 *
 * @author  Middleware Services
 */
public class PasswordExpiringControl extends AbstractResponseControl
{

  /** OID of this control. */
  public static final String OID = "2.16.840.1.113730.3.4.5";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 797;

  /** time in seconds until expiration. */
  private int timeBeforeExpiration;


  /** Default constructor. */
  public PasswordExpiringControl()
  {
    super(OID);
  }


  /**
   * Creates a new password expiring control.
   *
   * @param  critical  whether this control is critical
   */
  public PasswordExpiringControl(final boolean critical)
  {
    super(OID, critical);
  }


  /**
   * Creates a new password expiring control.
   *
   * @param  time  in seconds until expiration
   */
  public PasswordExpiringControl(final int time)
  {
    super(OID);
    timeBeforeExpiration = time;
    freeze();
  }


  /**
   * Creates a new password expiring control.
   *
   * @param  time  in seconds until expiration
   * @param  critical  whether this control is critical
   */
  public PasswordExpiringControl(final int time, final boolean critical)
  {
    super(OID, critical);
    timeBeforeExpiration = time;
    freeze();
  }


  /**
   * Returns the time in seconds until password expiration.
   *
   * @return  time in seconds until expiration
   */
  public int getTimeBeforeExpiration()
  {
    return timeBeforeExpiration;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof PasswordExpiringControl && super.equals(o)) {
      final PasswordExpiringControl v = (PasswordExpiringControl) o;
      return LdapUtils.areEqual(timeBeforeExpiration, v.timeBeforeExpiration);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, getOID(), getCriticality(), timeBeforeExpiration);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "criticality=" + getCriticality() + ", " +
      "timeBeforeExpiration=" + timeBeforeExpiration + "]";
  }


  @Override
  public void decode(final DERBuffer encoded)
  {
    freezeAndAssertMutable();
    try {
      final String time = OctetStringType.decode(encoded);
      timeBeforeExpiration = Integer.parseInt(time);
    } catch (Exception e) {
      throw new IllegalArgumentException("Error parsing response", e);
    }
  }
}
