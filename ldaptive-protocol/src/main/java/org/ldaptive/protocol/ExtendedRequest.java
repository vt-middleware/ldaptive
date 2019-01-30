/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import org.ldaptive.asn1.ApplicationDERTag;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.ContextDERTag;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;

/**
 * LDAP extended request defined as:
 *
 * <pre>
   ExtendedRequest ::= [APPLICATION 23] SEQUENCE {
     requestName      [0] LDAPOID,
     requestValue     [1] OCTET STRING OPTIONAL }
 * </pre>
 *
 * @author  Middleware Services
 */
public class ExtendedRequest extends AbstractRequestMessage
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 23;

  /** Extended request name. */
  private String requestName;

  /** Extended request value. */
  private byte[] requestValue;


  /**
   * Default constructor.
   */
  private ExtendedRequest() {}


  /**
   * Creates a new extended request.
   *
   * @param  name  of this request
   */
  public ExtendedRequest(final String name)
  {
    requestName = name;
  }


  /**
   * Creates a new extended request.
   *
   * @param  name  of this request
   * @param  value  of this request
   */
  public ExtendedRequest(final String name, final byte[] value)
  {
    requestName = name;
    requestValue = value;
  }


  /**
   * Sets the request value. Protected method available for extension.
   *
   * @param  value  request value
   */
  protected void setRequestValue(final byte[] value)
  {
    requestValue = value;
  }


  @Override
  protected DEREncoder[] getRequestEncoders(final int id)
  {
    if (requestValue == null) {
      return new DEREncoder[] {
        new IntegerType(id),
        new ConstructedDEREncoder(
          new ApplicationDERTag(PROTOCOL_OP, true),
          new OctetStringType(new ContextDERTag(0, false), requestName)),
      };
    } else {
      return new DEREncoder[] {
        new IntegerType(id),
        new ConstructedDEREncoder(
          new ApplicationDERTag(PROTOCOL_OP, true),
          new OctetStringType(new ContextDERTag(0, false), requestName),
          new OctetStringType(new ContextDERTag(1, false), requestValue)),
      };
    }
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ").append("requestName=").append(requestName).toString();
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


  /** Extended request builder. */
  public static class Builder extends AbstractRequestMessage.AbstractBuilder<ExtendedRequest.Builder, ExtendedRequest>
  {


    /**
     * Default constructor.
     */
    protected Builder()
    {
      super(new ExtendedRequest());
    }


    /**
     * Creates a new builder.
     *
     * @param  r  extended request to build
     */
    protected Builder(final ExtendedRequest r)
    {
      super(r);
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    /**
     * Sets the request name.
     *
     * @param  name  request name
     *
     * @return  this builder
     */
    public Builder name(final String name)
    {
      object.requestName = name;
      return self();
    }


    /**
     * Sets the request value.
     *
     * @param  value  request value
     *
     * @return  this builder
     */
    public Builder value(final byte[] value)
    {
      object.requestValue = value;
      return self();
    }
  }
}
