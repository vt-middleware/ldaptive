/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import org.ldaptive.asn1.ApplicationDERTag;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.IntegerType;

/**
 * LDAP abandon request defined as:
 *
 * <pre>
   AbandonRequest ::= [APPLICATION 16] MessageID
 * </pre>
 *
 * @author  Middleware Services
 */
public class AbandonRequest extends AbstractRequestMessage
{

  /** Protocol operation identifier. */
  public static final int PROTOCOL_OP = 16;

  /** Protocol message ID. */
  private final int messageID;


  /**
   * Creates a new abandon request.
   *
   * @param  id  message ID
   */
  public AbandonRequest(final int id)
  {
    messageID = id;
  }


  @Override
  protected DEREncoder[] getRequestEncoders(final int id)
  {
    return new DEREncoder[] {
      new IntegerType(id),
      new IntegerType(new ApplicationDERTag(PROTOCOL_OP, false), messageID),
    };
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ")
      .append("messageID=").append(messageID).toString();
  }
}