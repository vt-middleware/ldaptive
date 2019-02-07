/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.asn1.ApplicationDERTag;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.ContextType;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;

/**
 * LDAP anonymous bind request.
 *
 * @author  Middleware Services
 */
public class AnonymousBindRequest extends AbstractRequestMessage implements BindRequest
{


  @Override
  protected DEREncoder[] getRequestEncoders(final int id)
  {
    return new DEREncoder[] {
      new IntegerType(id),
      new ConstructedDEREncoder(
        new ApplicationDERTag(PROTOCOL_OP, true),
        new IntegerType(VERSION),
        new OctetStringType(""),
        new ContextType(0, (byte[]) null)),
    };
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


  /** Simple bind request builder. */
  public static class Builder extends
    AbstractRequestMessage.AbstractBuilder<AnonymousBindRequest.Builder, AnonymousBindRequest>
  {


    /**
     * Default constructor.
     */
    protected Builder()
    {
      super(new AnonymousBindRequest());
    }


    @Override
    protected Builder self()
    {
      return this;
    }
  }
}
