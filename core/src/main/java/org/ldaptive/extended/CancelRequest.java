/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.UniversalDERTag;

/**
 * LDAP cancel request defined as:
 *
 * <pre>
   ExtendedRequest ::= [APPLICATION 23] SEQUENCE {
     requestName      [0] LDAPOID,
     requestValue     [1] OCTET STRING OPTIONAL }

   cancelRequestValue ::= SEQUENCE {
     cancelID        MessageID -- MessageID is as defined in [RFC2251]
   }
 * </pre>
 *
 * @author  Middleware Services
 */
public class CancelRequest extends ExtendedRequest
{

  /** OID of this request. */
  public static final String OID = "1.3.6.1.1.8";


  /**
   * Creates a new cancel request.
   *
   * @param  id  of the message to cancel
   */
  public CancelRequest(final int id)
  {
    super(OID);
    final ConstructedDEREncoder se = new ConstructedDEREncoder(UniversalDERTag.SEQ, new IntegerType(id));
    setRequestValue(se.encode());
  }
}
