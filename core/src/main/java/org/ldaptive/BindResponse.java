/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;

/**
 * LDAP bind response defined as:
 *
 * <pre>
   BindResponse ::= [APPLICATION 1] SEQUENCE {
     COMPONENTS OF LDAPResult,
     serverSaslCreds    [7] OCTET STRING OPTIONAL }
 * </pre>
 *
 * @author  Middleware Services
 */
public final class BindResponse extends AbstractResult
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 1;

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10243;

  /** DER path to result code. */
  private static final DERPath RESULT_CODE_PATH = new DERPath("/SEQ/APP(1)/ENUM[0]");

  /** DER path to matched DN. */
  private static final DERPath MATCHED_DN_PATH = new DERPath("/SEQ/APP(1)/OCTSTR[1]");

  /** DER path to diagnostic message. */
  private static final DERPath DIAGNOSTIC_MESSAGE_PATH = new DERPath("/SEQ/APP(1)/OCTSTR[2]");

  /** DER path to referral. */
  private static final DERPath REFERRAL_PATH = new DERPath("/SEQ/APP(1)/CTX(3)/OCTSTR[0]");

  /** DER path to SASL credentials. */
  private static final DERPath SASL_CREDENTIALS_PATH = new DERPath("/SEQ/APP(1)/CTX(7)");

  /** Server SASL credentials. */
  private byte[] serverSaslCreds;


  /**
   * Default constructor.
   */
  private BindResponse() {}


  /**
   * Creates a new bind response.
   *
   * @param  buffer  to decode
   */
  public BindResponse(final DERBuffer buffer)
  {
    final DERParser parser = new DERParser();
    parser.registerHandler(MessageIDHandler.PATH, new MessageIDHandler(this));
    parser.registerHandler(RESULT_CODE_PATH, new ResultCodeHandler(this));
    parser.registerHandler(MATCHED_DN_PATH, new MatchedDNHandler(this));
    parser.registerHandler(DIAGNOSTIC_MESSAGE_PATH, new DiagnosticMessageHandler(this));
    parser.registerHandler(REFERRAL_PATH, new ReferralHandler(this));
    parser.registerHandler(SASL_CREDENTIALS_PATH, new SASLCredsHandler(this));
    parser.registerHandler(ControlsHandler.PATH, new ControlsHandler(this));
    try {
      parser.parse(buffer);
    } catch (Exception e) {
      throw new IllegalArgumentException("Error parsing response", e);
    }
  }


  public byte[] getServerSaslCreds()
  {
    return serverSaslCreds;
  }


  private void setServerSaslCreds(final byte[] creds)
  {
    serverSaslCreds = creds;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof BindResponse && super.equals(o)) {
      final BindResponse v = (BindResponse) o;
      return LdapUtils.areEqual(serverSaslCreds, v.serverSaslCreds);
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
        serverSaslCreds);
  }


  /** Parse handler implementation for the server SASL creds. */
  protected static class SASLCredsHandler extends AbstractParseHandler<BindResponse>
  {


    /**
     * Creates a new server SASL creds handler.
     *
     * @param  response  to configure
     */
    SASLCredsHandler(final BindResponse response)
    {
      super(response);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      if (encoded.remaining() > 0) {
        getObject().setServerSaslCreds(encoded.getRemainingBytes());
      }
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
  public static final class Builder extends AbstractResult.AbstractBuilder<Builder, BindResponse>
  {


    private Builder()
    {
      super(new BindResponse());
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    public Builder serverSaslCreds(final byte[] creds)
    {
      object.setServerSaslCreds(creds);
      return this;
    }
  }
  // CheckStyle:ON
}
