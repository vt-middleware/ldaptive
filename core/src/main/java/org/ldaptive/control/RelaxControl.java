/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;

/**
 * Relax request control. See https://tools.ietf.org/html/draft-zeilenga-ldap-relax-03.
 *
 * @author  Middleware Services
 */
public class RelaxControl extends AbstractControl implements RequestControl
{

  /** OID of this control. */
  public static final String OID = "1.3.6.1.4.1.4203.666.5.12";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10711;


  /** Default constructor. */
  public RelaxControl()
  {
    super(OID, true);
  }


  @Override
  public boolean hasValue()
  {
    return false;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof RelaxControl && super.equals(o);
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, getOID(), getCriticality());
  }


  @Override
  public byte[] encode()
  {
    return null;
  }
}
