/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import java.nio.ByteBuffer;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.OctetStringType;

/**
 * Response control indicating a password that will expire. See
 * http://tools.ietf.org/html/draft-vchu-ldap-pwd-policy-00. Control is defined
 * as:
 *
 * <pre>
   controlValue ::= secondsUntilExpiration  OCTET STRING
 * </pre>
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class PasswordExpiringControl extends AbstractControl
  implements ResponseControl
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


  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(HASH_CODE_SEED, getOID(), getCriticality());
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::criticality=%s, timeBeforeExpiration=%s]",
        getClass().getName(),
        hashCode(),
        getCriticality(),
        timeBeforeExpiration);
  }


  /** {@inheritDoc} */
  @Override
  public void decode(final byte[] berValue)
  {
    logger.trace("decoding control: {}", LdapUtils.base64Encode(berValue));

    final String time = OctetStringType.decode(ByteBuffer.wrap(berValue));
    setTimeBeforeExpiration(Integer.valueOf(time));
  }
}
