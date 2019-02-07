/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control;

import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.UniversalDERTag;
import org.ldaptive.control.AbstractControl;
import org.ldaptive.control.RequestControl;

/**
 * Request control for active directory servers to use an extended form of an object distinguished name. Control is
 * defined as:
 *
 * <pre>
    extendedDnValue ::= SEQUENCE {
          flag  INTEGER
    }
 * </pre>
 *
 * <p>See http://msdn.microsoft.com/en-us/library/cc223349.aspx</p>
 *
 * @author  Middleware Services
 */
public class ExtendedDnControl extends AbstractControl implements RequestControl
{

  /** OID of this control. */
  public static final String OID = "1.2.840.113556.1.4.529";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 919;

  /** Types of flags. */
  public enum Flag {

    /** hexadecimal format. */
    HEXADECIMAL,

    /** standard format. */
    STANDARD
  }

  /** flag. */
  private Flag flag = Flag.STANDARD;


  /** Default constructor. */
  public ExtendedDnControl()
  {
    super(OID);
  }


  /**
   * Creates a new extended dn control.
   *
   * @param  f  flag
   */
  public ExtendedDnControl(final Flag f)
  {
    super(OID);
    setFlag(f);
  }


  /**
   * Creates a new extended dn control.
   *
   * @param  f  flag
   * @param  critical  whether this control is critical
   */
  public ExtendedDnControl(final Flag f, final boolean critical)
  {
    super(OID, critical);
    setFlag(f);
  }


  @Override
  public boolean hasValue()
  {
    return true;
  }


  /**
   * Returns the flag.
   *
   * @return  flag
   */
  public Flag getFlag()
  {
    return flag;
  }


  /**
   * Sets the flag.
   *
   * @param  f  flag
   */
  public void setFlag(final Flag f)
  {
    flag = f;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof ExtendedDnControl && super.equals(o)) {
      final ExtendedDnControl v = (ExtendedDnControl) o;
      return LdapUtils.areEqual(flag, v.flag);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, getOID(), getCriticality(), flag);
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("criticality=").append(getCriticality()).append(", ")
      .append("flag=").append(flag).append("]").toString();
  }


  @Override
  public byte[] encode()
  {
    final ConstructedDEREncoder se = new ConstructedDEREncoder(
      UniversalDERTag.SEQ,
      new IntegerType(getFlag().ordinal()));
    return se.encode();
  }
}
