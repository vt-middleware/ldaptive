/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.nio.ByteBuffer;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.DERParser;

/**
 * LDAP compare response defined as:
 *
 * <pre>
   CompareResponse ::= [APPLICATION 15] LDAPResult
 * </pre>
 *
 * @author  Middleware Services
 */
public class CompareResponse extends AbstractResult
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 15;

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10247;


  /**
   * Default constructor.
   */
  private CompareResponse() {}


  /**
   * Creates a new compare response.
   *
   * @param  buffer  to decode
   */
  public CompareResponse(final ByteBuffer buffer)
  {
    final DERParser parser = new DERParser();
    parser.registerHandler(MessageIDHandler.PATH, new MessageIDHandler(this));
    parser.registerHandler("/SEQ/APP(15)/ENUM[0]", new ResultCodeHandler(this));
    parser.registerHandler("/SEQ/APP(15)/OCTSTR[1]", new MatchedDNHandler(this));
    parser.registerHandler("/SEQ/APP(15)/OCTSTR[2]", new DiagnosticMessageHandler(this));
    parser.registerHandler("/SEQ/APP(15)/CTX(3)/OCTSTR[0]", new ReferralHandler(this));
    parser.registerHandler(ControlsHandler.PATH, new ControlsHandler(this));
    parser.parse(buffer);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof CompareResponse && super.equals(o);
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


  // CheckStyle:OFF
  protected static class Builder extends AbstractResult.AbstractBuilder<Builder, CompareResponse>
  {


    public Builder()
    {
      super(new CompareResponse());
    }


    @Override
    protected Builder self()
    {
      return this;
    }
  }
  // CheckStyle:ON
}
