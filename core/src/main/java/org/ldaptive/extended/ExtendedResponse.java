/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import java.util.Arrays;
import org.ldaptive.AbstractResult;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.asn1.OctetStringType;

/**
 * LDAP extended response defined as:
 *
 * <pre>
   ExtendedResponse ::= [APPLICATION 24] SEQUENCE {
     COMPONENTS OF LDAPResult,
     responseName     [10] LDAPOID OPTIONAL,
     responseValue    [11] OCTET STRING OPTIONAL }
 * </pre>
 *
 * @author  Middleware Services
 */
public class ExtendedResponse extends AbstractResult
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 24;

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10259;

  /** DER path to result code. */
  private static final DERPath RESULT_CODE_PATH = new DERPath("/SEQ/APP(24)/ENUM[0]");

  /** DER path to matched DN. */
  private static final DERPath MATCHED_DN_PATH = new DERPath("/SEQ/APP(24)/OCTSTR[1]");

  /** DER path to diagnostic message. */
  private static final DERPath DIAGNOSTIC_MESSAGE_PATH = new DERPath("/SEQ/APP(24)/OCTSTR[2]");

  /** DER path to referral. */
  private static final DERPath REFERRAL_PATH = new DERPath("/SEQ/APP(24)/CTX(3)/OCTSTR[0]");

  /** DER path to name. */
  private static final DERPath NAME_PATH = new DERPath("/SEQ/APP(24)/CTX(10)");

  /** DER path to value. */
  private static final DERPath VALUE_PATH = new DERPath("/SEQ/APP(24)/CTX(11)");

  /** Response name. */
  private String responseName;

  /** Response value. */
  private byte[] responseValue;


  /**
   * Default constructor.
   */
  protected ExtendedResponse() {}


  /**
   * Creates a new extended response.
   *
   * @param  buffer  to decode
   */
  public ExtendedResponse(final DERBuffer buffer)
  {
    final DERParser parser = new DERParser();
    parser.registerHandler(MessageIDHandler.PATH, new MessageIDHandler(this));
    parser.registerHandler(RESULT_CODE_PATH, new ResultCodeHandler(this));
    parser.registerHandler(MATCHED_DN_PATH, new MatchedDNHandler(this));
    parser.registerHandler(DIAGNOSTIC_MESSAGE_PATH, new DiagnosticMessageHandler(this));
    parser.registerHandler(REFERRAL_PATH, new ReferralHandler(this));
    parser.registerHandler(NAME_PATH, new ResponseNameHandler(this));
    parser.registerHandler(VALUE_PATH, new ResponseValueHandler(this));
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
    if (o instanceof ExtendedResponse && super.equals(o)) {
      final ExtendedResponse v = (ExtendedResponse) o;
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
        getResultCode(),
        getMatchedDN(),
        getDiagnosticMessage(),
        getReferralURLs(),
        responseName,
        responseValue);
  }


  @Override
  public String toString()
  {
    return super.toString() + ", " +
      "responseName=" + responseName + ", " +
      "responseValue=" + Arrays.toString(responseValue);
  }


  /** Parse handler implementation for the response name. */
  protected static class ResponseNameHandler extends AbstractParseHandler<ExtendedResponse>
  {


    /**
     * Creates a new response name handler.
     *
     * @param  response  to configure
     */
    ResponseNameHandler(final ExtendedResponse response)
    {
      super(response);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setResponseName(OctetStringType.decode(encoded));
    }
  }


  /** Parse handler implementation for the response value. */
  protected static class ResponseValueHandler extends AbstractParseHandler<ExtendedResponse>
  {


    /**
     * Creates a new response value handler.
     *
     * @param  response  to configure
     */
    ResponseValueHandler(final ExtendedResponse response)
    {
      super(response);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setResponseValue(encoded.getRemainingBytes());
    }
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


  // CheckStyle:OFF
  public static class Builder extends AbstractResult.AbstractBuilder<Builder, ExtendedResponse>
  {


    protected Builder()
    {
      super(new ExtendedResponse());
    }


    protected Builder(final ExtendedResponse r)
    {
      super(r);
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    public Builder responseName(final String name)
    {
      object.responseName = name;
      return this;
    }


    public Builder responseValue(final byte[] value)
    {
      object.responseValue = value;
      return this;
    }
  }
  // CheckStyle:ON
}
