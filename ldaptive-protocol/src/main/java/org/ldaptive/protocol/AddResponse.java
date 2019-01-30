/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;

/**
 * LDAP add response defined as:
 *
 * <pre>
   AddResponse ::= [APPLICATION 9] LDAPResult
 * </pre>
 *
 * @author  Middleware Services
 */
public class AddResponse extends AbstractResult
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 9;

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10211;


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
    parser.registerHandler("/SEQ/APP(9)/ENUM[0]", new ResultCodeHandler(this));
    parser.registerHandler("/SEQ/APP(9)/OCTSTR[1]", new MatchedDNHandler(this));
    parser.registerHandler("/SEQ/APP(9)/OCTSTR[2]", new DiagnosticMessageHandler(this));
    parser.registerHandler("/SEQ/APP(9)/CTX(3)/OCTSTR[0]", new ReferralHandler(this));
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
  protected static Builder builder()
  {
    return new Builder();
  }


  // CheckStyle:OFF
  protected static class Builder extends AbstractResult.AbstractBuilder<Builder, AddResponse>
  {


    protected Builder()
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
