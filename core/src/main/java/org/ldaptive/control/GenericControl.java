/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.DERBuffer;

/**
 * LDAP control defined as:
 *
 * <pre>
   Control ::= SEQUENCE {
     controlType             LDAPOID,
     criticality             BOOLEAN DEFAULT FALSE,
     controlValue            OCTET STRING OPTIONAL }
 * </pre>
 *
 * @author  Middleware Services
 */
public class GenericControl extends AbstractControl implements RequestControl, ResponseControl
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 7039;

  /** control value. */
  private byte[] value;


  /**
   * Creates a new generic control.
   *
   * @param  oid  control OID
   * @param  encoded  control value
   */
  public GenericControl(final String oid, final byte[] encoded)
  {
    this(oid, false, encoded);
  }


  /**
   * Creates a new generic control.
   *
   * @param  oid  control OID
   * @param  encoded  control value
   */
  public GenericControl(final String oid, final DERBuffer encoded)
  {
    this(oid, false, encoded);
  }


  /**
   * Creates a new generic control.
   *
   * @param  oid  control OID
   * @param  critical  whether this control is critical
   * @param  encoded  control value
   */
  public GenericControl(final String oid, final boolean critical, final byte[] encoded)
  {
    super(oid, critical);
    value = encoded;
  }


  /**
   * Creates a new generic control.
   *
   * @param  oid  control OID
   * @param  critical  whether this control is critical
   * @param  encoded  control value
   */
  public GenericControl(final String oid, final boolean critical, final DERBuffer encoded)
  {
    super(oid, critical);
    decode(encoded);
  }


  @Override
  public boolean hasValue()
  {
    return value != null;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof GenericControl && super.equals(o)) {
      final GenericControl v = (GenericControl) o;
      return LdapUtils.areEqual(value, v.value);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, getOID(), getCriticality(), value);
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::oid=%s, criticality=%s, value=%s]",
        getClass().getName(),
        hashCode(),
        getOID(),
        getCriticality(),
        LdapUtils.base64Encode(value));
  }


  @Override
  public byte[] encode()
  {
    return value;
  }


  @Override
  public void decode(final DERBuffer encoded)
  {
    value = encoded.getRemainingBytes();
  }
}
