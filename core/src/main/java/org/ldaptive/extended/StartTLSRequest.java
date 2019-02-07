/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

/**
 * LDAP startTLS request defined as:
 *
 * <pre>
   ExtendedRequest ::= [APPLICATION 23] SEQUENCE {
     requestName      [0] LDAPOID,
     requestValue     [1] OCTET STRING OPTIONAL }
 * </pre>
 *
 * where the request value is absent.
 *
 * @author  Middleware Services
 */
public class StartTLSRequest extends ExtendedRequest
{

  /** OID of this request. */
  public static final String OID = "1.3.6.1.4.1.1466.20037";


  /**
   * Default constructor.
   */
  public StartTLSRequest()
  {
    super(OID);
  }
}
