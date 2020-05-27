/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import java.util.ArrayList;
import java.util.List;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.ContextType;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.UniversalDERTag;

/**
 * LDAP password modify request defined as:
 *
 * <pre>
   PasswdModifyRequestValue ::= SEQUENCE {
     userIdentity    [0]  OCTET STRING OPTIONAL
     oldPasswd       [1]  OCTET STRING OPTIONAL
     newPasswd       [2]  OCTET STRING OPTIONAL }
 * </pre>
 *
 * @author  Middleware Services
 */
public class PasswordModifyRequest extends ExtendedRequest
{

  /** OID of this request. */
  public static final String OID = "1.3.6.1.4.1.4203.1.11.1";


  /**
   * Creates a new password modify request.
   */
  public PasswordModifyRequest()
  {
    super(OID);
  }


  /**
   * Creates a new password modify request.
   *
   * @param  identity  to modify or null
   */
  public PasswordModifyRequest(final String identity)
  {
    this(identity, null, null);
  }


  /**
   * Creates a new password modify request.
   *
   * @param  identity  to modify or null
   * @param  oldPass  current password for the dn or null
   */
  public PasswordModifyRequest(final String identity, final String oldPass)
  {
    this(identity, oldPass, null);
  }


  /**
   * Creates a new password modify request.
   *
   * @param  identity  to modify or null
   * @param  oldPass  current password for the dn or null
   * @param  newPass  desired password for the dn or null
   */
  public PasswordModifyRequest(final String identity, final String oldPass, final String newPass)
  {
    super(OID);
    final List<DEREncoder> l = new ArrayList<>();
    if (identity != null) {
      l.add(new ContextType(0, identity));
    }
    if (oldPass != null) {
      l.add(new ContextType(1, oldPass));
    }
    if (newPass != null) {
      l.add(new ContextType(2, newPass));
    }
    final ConstructedDEREncoder se = new ConstructedDEREncoder(
      UniversalDERTag.SEQ,
      l.stream().toArray(size -> new DEREncoder[size]));
    setRequestValue(se.encode());
  }
}
