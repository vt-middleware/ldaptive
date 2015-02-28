/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import java.util.Arrays;
import org.ldaptive.AbstractRequest;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.UniversalDERTag;

/**
 * Contains the data required to perform an ldap cancel operation. See RFC 3909.
 *
 * @author  Middleware Services
 */
public class CancelRequest extends AbstractRequest implements ExtendedRequest
{

  /** OID of this extended request. */
  public static final String OID = "1.3.6.1.1.8";

  /** message id to cancel. */
  private int messageId;


  /** Default constructor. */
  public CancelRequest() {}


  /**
   * Creates a new cancel request.
   *
   * @param  id  of the message to cancel
   */
  public CancelRequest(final int id)
  {
    setMessageId(id);
  }


  /**
   * Returns the message id to cancel.
   *
   * @return  message id
   */
  public int getMessageId()
  {
    return messageId;
  }


  /**
   * Sets the message id to cancel.
   *
   * @param  id  of the message to cancel
   */
  public void setMessageId(final int id)
  {
    messageId = id;
  }


  @Override
  public byte[] encode()
  {
    final ConstructedDEREncoder se = new ConstructedDEREncoder(
      UniversalDERTag.SEQ,
      new IntegerType(getMessageId()));
    return se.encode();
  }


  @Override
  public String getOID()
  {
    return OID;
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::messageId=%s, controls=%s, referralHandler=%s, " +
        "intermediateResponseHandlers=%s]",
        getClass().getName(),
        hashCode(),
        messageId,
        Arrays.toString(getControls()),
        getReferralHandler(),
        Arrays.toString(getIntermediateResponseHandlers()));
  }
}
