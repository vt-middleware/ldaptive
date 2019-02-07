/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.extended;

import org.ldaptive.extended.ExtendedRequest;

/**
 * LDAP fast bind request defined as:
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
public class FastBindRequest extends ExtendedRequest
{

  /** OID of this extended request. */
  public static final String OID = "1.2.840.113556.1.4.1781";


  /**
   * Default constructor.
   */
  public FastBindRequest()
  {
    super(OID);
  }
}
