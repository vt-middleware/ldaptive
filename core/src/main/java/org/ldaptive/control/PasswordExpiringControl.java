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
public class PasswordExpiringControl extends AbstractControl implements ResponseControl
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
    setTimeBeforeExpiration(time);
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
    setTimeBeforeExpiration(time);
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


  /**
   * Sets the time in seconds until password expiration.
   *
   * @param  time  in seconds until expiration
   */
  public void setTimeBeforeExpiration(final int time)
  {
    timeBeforeExpiration = time;
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
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("criticality=").append(getCriticality()).append(", ")
      .append("timeBeforeExpiration=").append(timeBeforeExpiration).append("]").toString();
  }


  @Override
  public void decode(final DERBuffer encoded)
  {
    final String time = OctetStringType.decode(encoded);
    setTimeBeforeExpiration(Integer.valueOf(time));
  }
}
