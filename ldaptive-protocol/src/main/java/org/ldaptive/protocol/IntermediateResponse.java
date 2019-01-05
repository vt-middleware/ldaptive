/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.util.Arrays;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.OctetStringType;

/**
 * LDAP extended response defined as:
 *
 * <pre>
   IntermediateResponse ::= [APPLICATION 25] SEQUENCE {
     responseName     [0] LDAPOID OPTIONAL,
     responseValue    [1] OCTET STRING OPTIONAL }
 * </pre>
 *
 * @author  Middleware Services
 */
public class IntermediateResponse extends AbstractMessage
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 25;

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10267;

  /** Response name. */
  private String responseName;

  /** Response value. */
  private byte[] responseValue;


  /**
   * Default constructor.
   */
  private IntermediateResponse() {}


  /**
   * Creates a new intermediate response.
   *
   * @param  buffer  to decode
   */
  public IntermediateResponse(final DERBuffer buffer)
  {
    final DERParser parser = new DERParser();
    parser.registerHandler(MessageIDHandler.PATH, new MessageIDHandler(this));
    parser.registerHandler("/SEQ/APP(25)/CTX(0)", new ResponseNameHandler(this));
    parser.registerHandler("/SEQ/APP(25)/CTX(1)", new ResponseValueHandler(this));
    parser.registerHandler(ControlsHandler.PATH, new ControlsHandler(this));
    parser.parse(buffer);
  }


  public String getResponseName()
  {
    return responseName;
  }


  public void setResponseName(final String name)
  {
    responseName = name;
  }


  public byte[] getResponseValue()
  {
    return responseValue;
  }


  public void setResponseValue(final byte[] value)
  {
    responseValue = value;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof IntermediateResponse && super.equals(o)) {
      final IntermediateResponse v = (IntermediateResponse) o;
      return LdapUtils.areEqual(responseName, v.responseName) &&
        LdapUtils.areEqual(responseValue, v.responseValue);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        getMessageID(),
        getControls(),
        responseName,
        responseValue);
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      super.toString()).append(", ")
      .append("responseName=").append(responseName).append(", ")
      .append("responseValue=").append(Arrays.toString(responseValue)).toString();
  }


  /** Parse handler implementation for the response name. */
  protected static class ResponseNameHandler extends AbstractParseHandler<IntermediateResponse>
  {


    /**
     * Creates a new response name handler.
     *
     * @param  response  to configure
     */
    ResponseNameHandler(final IntermediateResponse response)
    {
      super(response);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      if (encoded.remaining() > 0) {
        getObject().setResponseName(OctetStringType.decode(encoded));
      }
    }
  }


  /** Parse handler implementation for the response value. */
  protected static class ResponseValueHandler extends AbstractParseHandler<IntermediateResponse>
  {


    /**
     * Creates a new response value handler.
     *
     * @param  response  to configure
     */
    ResponseValueHandler(final IntermediateResponse response)
    {
      super(response);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      if (encoded.remaining() > 0) {
        final DERParser p = new DERParser();
        p.readTag(encoded).getTagNo();
        p.readLength(encoded);
        getObject().setResponseValue(encoded.getRemainingBytes());
      }
    }
  }


  // CheckStyle:OFF
  protected static class Builder extends AbstractMessage.AbstractBuilder<Builder, IntermediateResponse>
  {


    public Builder()
    {
      super(new IntermediateResponse());
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    public Builder responseName(final String name)
    {
      object.setResponseName(name);
      return this;
    }


    public Builder responseValue(final byte[] value)
    {
      object.setResponseValue(value);
      return this;
    }
  }
  // CheckStyle:ON
}
