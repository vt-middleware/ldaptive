/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;

/**
 * LDAP add response defined as:
 *
 * <pre>
   AddResponse ::= [APPLICATION 9] LDAPResult
 * </pre>
 *
 * @author  Middleware Services
 */
public final class AddResponse extends AbstractResult
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 9;

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10211;

  /** DER path to result code. */
  private static final DERPath RESULT_CODE_PATH = new DERPath("/SEQ/APP(9)/ENUM[0]");

  /** DER path to matched DN. */
  private static final DERPath MATCHED_DN_PATH = new DERPath("/SEQ/APP(9)/OCTSTR[1]");

  /** DER path to diagnostic message. */
  private static final DERPath DIAGNOSTIC_MESSAGE_PATH = new DERPath("/SEQ/APP(9)/OCTSTR[2]");

  /** DER path to referral. */
  private static final DERPath REFERRAL_PATH = new DERPath("/SEQ/APP(9)/CTX(3)/OCTSTR[0]");


  /**
   * Default constructor.
   */
  private AddResponse() {}


  /**
   * Creates a new add response.
   *
   * @param  buffer  to decode
   */
  public AddResponse(final DERBuffer buffer)
  {
    final DERParser parser = new DERParser();
    parser.registerHandler(MessageIDHandler.PATH, new MessageIDHandler(this));
    parser.registerHandler(RESULT_CODE_PATH, new ResultCodeHandler(this));
    parser.registerHandler(MATCHED_DN_PATH, new MatchedDNHandler(this));
    parser.registerHandler(DIAGNOSTIC_MESSAGE_PATH, new DiagnosticMessageHandler(this));
    parser.registerHandler(REFERRAL_PATH, new ReferralHandler(this));
    parser.registerHandler(ControlsHandler.PATH, new ControlsHandler(this));
    parser.parse(buffer);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof AddResponse && super.equals(o);
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
        getReferralURLs());
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
  public static final class Builder extends AbstractResult.AbstractBuilder<Builder, AddResponse>
  {


    private Builder()
    {
      super(new AddResponse());
    }


    @Override
    protected Builder self()
    {
      return this;
    }
  }
  // CheckStyle:ON
}
