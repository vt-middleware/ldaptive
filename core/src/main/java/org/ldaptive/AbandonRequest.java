/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

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
  private int messageID;


  /**
   * Default constructor.
   */
  private AbandonRequest() {}


  /**
   * Creates a new abandon request.
   *
   * @param  id  message ID
   */
  public AbandonRequest(final int id)
  {
    messageID = id;
  }


  public int getMessageID()
  {
    return messageID;
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


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static Builder builder()
  {
    return new Builder();
  }


  /** Abandon request builder. */
  public static class Builder extends AbstractRequestMessage.AbstractBuilder<AbandonRequest.Builder, AbandonRequest>
  {


    /**
     * Default constructor.
     */
    protected Builder()
    {
      super(new AbandonRequest());
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    /**
     * Sets the message ID.
     *
     * @param  id  message ID
     *
     * @return  this builder
     */
    public Builder id(final int id)
    {
      object.messageID = id;
      return self();
    }
  }
}
