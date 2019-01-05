/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;

/**
 * LDAP modify DN response defined as:
 *
 * <pre>
   ModifyDNResponse ::= [APPLICATION 13] LDAPResult
 * </pre>
 *
 * @author  Middleware Services
 */
public class ModifyDnResponse extends AbstractResult
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 13;

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10271;


  /**
   * Default constructor.
   */
  private ModifyDnResponse() {}


  /**
   * Creates a new modify DN response.
   *
   * @param  buffer  to decode
   */
  public ModifyDnResponse(final DERBuffer buffer)
  {
    final DERParser parser = new DERParser();
    parser.registerHandler(MessageIDHandler.PATH, new MessageIDHandler(this));
    parser.registerHandler("/SEQ/APP(13)/ENUM[0]", new ResultCodeHandler(this));
    parser.registerHandler("/SEQ/APP(13)/OCTSTR[1]", new MatchedDNHandler(this));
    parser.registerHandler("/SEQ/APP(13)/OCTSTR[2]", new DiagnosticMessageHandler(this));
    parser.registerHandler("/SEQ/APP(13)/CTX(3)/OCTSTR[0]", new ReferralHandler(this));
    parser.registerHandler(ControlsHandler.PATH, new ControlsHandler(this));
    parser.parse(buffer);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof ModifyDnResponse && super.equals(o);
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
  protected static class Builder extends AbstractResult.AbstractBuilder<Builder, ModifyDnResponse>
  {


    public Builder()
    {
      super(new ModifyDnResponse());
    }


    @Override
    protected Builder self()
    {
      return this;
    }
  }
  // CheckStyle:ON
}
