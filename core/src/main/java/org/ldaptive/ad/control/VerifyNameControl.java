/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control;

import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.asn1.UniversalDERTag;
import org.ldaptive.control.AbstractControl;
import org.ldaptive.control.RequestControl;

/**
 * Request control for active directory servers to use an extended form of an object distinguished name. Control is
 * defined as:
 *
 * <pre>
   verifyNameValue ::= SEQUENCE {
     Flags       INTEGER
     ServerName  OCTET STRING
   }
 * </pre>
 *
 * <p>See http://msdn.microsoft.com/en-us/library/cc223328.aspx</p>
 *
 * @author  Middleware Services
 */
public class VerifyNameControl extends AbstractControl implements RequestControl
{

  /** OID of this control. */
  public static final String OID = "1.2.840.113556.1.4.1338";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 977;

  /** Global catalog server to contact. */
  private String serverName;


  /** Default constructor. */
  public VerifyNameControl()
  {
    super(OID);
  }


  /**
   * Creates a new verify name control.
   *
   * @param  name  server name
   */
  public VerifyNameControl(final String name)
  {
    super(OID);
    setServerName(name);
  }


  /**
   * Creates a new verify name control.
   *
   * @param  name  server name
   * @param  critical  whether this control is critical
   */
  public VerifyNameControl(final String name, final boolean critical)
  {
    super(OID, critical);
    setServerName(name);
  }


  @Override
  public boolean hasValue()
  {
    return true;
  }


  /**
   * Returns the server name.
   *
   * @return  server name
   */
  public String getServerName()
  {
    return serverName;
  }


  /**
   * Sets the server name.
   *
   * @param  name  server name
   */
  public void setServerName(final String name)
  {
    serverName = LdapUtils.assertNotNullArg(name, "Server name cannot be null");
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof VerifyNameControl && super.equals(o)) {
      final VerifyNameControl v = (VerifyNameControl) o;
      return LdapUtils.areEqual(serverName, v.serverName);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, getOID(), getCriticality(), serverName);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "criticality=" + getCriticality() + ", " +
      "serverName=" + serverName + "]";
  }


  @Override
  public byte[] encode()
  {
    LdapUtils.assertNotNullState(serverName, "Server name cannot be null");
    final ConstructedDEREncoder se = new ConstructedDEREncoder(
      UniversalDERTag.SEQ,
      new IntegerType(0),
      new OctetStringType(serverName));
    return se.encode();
  }
}
