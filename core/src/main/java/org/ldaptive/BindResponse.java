/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.control.ResponseControl;

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
@Getter
@SuperBuilder
public final class BindResponse extends AbstractResult
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 1;

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10243;

  /** Server SASL credentials. */
  private byte[] serverSaslCreds;


  /**
   * Creates a new instance with the given required parameters.
   *
   * @param  messageID  LDAP protocol message ID.
   * @param  controls  Response controls.
   * @param  resultCode  LDAP protocol result code.
   * @param  matchedDN  DN matched by operation.
   * @param  diagnosticMessage  Informative message returned by server.
   * @param  referralUrls  Zero or more referral URLs.
   * @param  credentials  SASL credentials.
   */
  public BindResponse(
          final int messageID,
          final List<ResponseControl> controls,
          final ResultCode resultCode,
          final String matchedDN,
          final String diagnosticMessage,
          final List<String> referralUrls,
          final byte[] credentials)
  {
    super(messageID, controls, resultCode, matchedDN, diagnosticMessage, referralUrls);
    serverSaslCreds = credentials;
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
  protected int getHashCodeSeed()
  {
    return HASH_CODE_SEED;
  }


  /**
   */
  public static abstract class BindResponseBuilder extends AbstractResult.AbstractBuilder<BindResponse>
  {
    /** DER path to SASL credentials. */
    private static final DERPath SASL_CREDENTIALS_PATH = new DERPath("/SEQ/APP(1)/CTX(7)");


    @Override
    public void registerHandlers(DERParser parser) {
      parser.registerHandler(SASL_CREDENTIALS_PATH, new SASLCredsHandler(this));
    }
  }


  /** Parse handler implementation for the server SASL creds. */
  protected static class SASLCredsHandler extends AbstractParseHandler<BindResponse, BindResponseBuilder>
  {
    /**
     * Creates a new server SASL creds handler.
     *
     * @param  builder  Response builder.
     */
    SASLCredsHandler(final BindResponseBuilder builder)
    {
      super(builder);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      if (encoded.remaining() > 0) {
        getBuilder().serverSaslCreds(encoded.getRemainingBytes());
      }
    }
  }
}
