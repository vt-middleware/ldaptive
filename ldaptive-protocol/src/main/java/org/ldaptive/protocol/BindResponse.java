/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.OctetStringType;

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
public class BindResponse extends AbstractResult
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 1;

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10243;

  /** Server SASL credentials. */
  private String serverSaslCreds;


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
    parser.registerHandler("/SEQ/APP(1)/ENUM[0]", new ResultCodeHandler(this));
    parser.registerHandler("/SEQ/APP(1)/OCTSTR[1]", new MatchedDNHandler(this));
    parser.registerHandler("/SEQ/APP(1)/OCTSTR[2]", new DiagnosticMessageHandler(this));
    parser.registerHandler("/SEQ/APP(1)/CTX(3)/OCTSTR[0]", new ReferralHandler(this));
    parser.registerHandler("/SEQ/APP(1)/CTX(7)", new SASLCredsHandler(this));
    parser.registerHandler(ControlsHandler.PATH, new ControlsHandler(this));
    parser.parse(buffer);
  }


  public String getServerSaslCreds()
  {
    return serverSaslCreds;
  }


  public void setServerSaslCreds(final String creds)
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


  @Override
  public String toString()
  {
    return new StringBuilder(
      super.toString()).append(", ").append("serverSaslCreds=").append(serverSaslCreds).toString();
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
      getObject().setServerSaslCreds(OctetStringType.decode(encoded));
    }
  }


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  protected static Builder builder()
  {
    return new Builder();
  }


  // CheckStyle:OFF
  protected static class Builder extends AbstractResult.AbstractBuilder<Builder, BindResponse>
  {


    protected Builder()
    {
      super(new BindResponse());
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    public Builder serverSaslCreds(final String creds)
    {
      object.setServerSaslCreds(creds);
      return this;
    }
  }
  // CheckStyle:ON
}
