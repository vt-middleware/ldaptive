/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

/**
 * LDAP who am i request defined as:
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
public class WhoAmIRequest extends ExtendedRequest
{

  /** OID of this request. */
  public static final String OID = "1.3.6.1.4.1.4203.1.11.3";


  /**
   * Default constructor.
   */
  public WhoAmIRequest()
  {
    super(OID);
  }
}
