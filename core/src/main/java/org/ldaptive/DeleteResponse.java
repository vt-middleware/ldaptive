/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;

/**
 * LDAP delete response defined as:
 *
 * <pre>
   DelResponse ::= [APPLICATION 11] LDAPResult
 * </pre>
 *
 * @author  Middleware Services
 */
public class DeleteResponse extends AbstractResult
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 11;

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10253;


  /**
   * Default constructor.
   */
  private DeleteResponse() {}


  /**
   * Creates a new delete response.
   *
   * @param  buffer  to decode
   */
  public DeleteResponse(final DERBuffer buffer)
  {
    final DERParser parser = new DERParser();
    parser.registerHandler(MessageIDHandler.PATH, new MessageIDHandler(this));
    parser.registerHandler("/SEQ/APP(11)/ENUM[0]", new ResultCodeHandler(this));
    parser.registerHandler("/SEQ/APP(11)/OCTSTR[1]", new MatchedDNHandler(this));
    parser.registerHandler("/SEQ/APP(11)/OCTSTR[2]", new DiagnosticMessageHandler(this));
    parser.registerHandler("/SEQ/APP(11)/CTX(3)/OCTSTR[0]", new ReferralHandler(this));
    parser.registerHandler(ControlsHandler.PATH, new ControlsHandler(this));
    parser.parse(buffer);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof DeleteResponse && super.equals(o);
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
  public static class Builder extends AbstractResult.AbstractBuilder<Builder, DeleteResponse>
  {


    protected Builder()
    {
      super(new DeleteResponse());
    }


    @Override
    protected Builder self()
    {
      return this;
    }
  }
  // CheckStyle:ON
}
