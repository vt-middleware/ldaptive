/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.asn1.ApplicationDERTag;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.NullType;

/**
 * LDAP unbind request defined as:
 *
 * <pre>
   UnbindRequest ::= [APPLICATION 2] NULL
 * </pre>
 *
 * @author  Middleware Services
 */
public class UnbindRequest extends AbstractRequestMessage
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 2;


  @Override
  protected DEREncoder[] getRequestEncoders(final int id)
  {
    return new DEREncoder[] {
      new IntegerType(id),
      new NullType(new ApplicationDERTag(PROTOCOL_OP, false)),
    };
  }
}
