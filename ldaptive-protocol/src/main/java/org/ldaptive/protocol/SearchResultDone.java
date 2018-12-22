/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.nio.ByteBuffer;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.DERParser;

/**
 * LDAP search result entry defined as:
 *
 * <pre>
  SearchResultDone ::= [APPLICATION 5] LDAPResult
 * </pre>
 *
 * @author  Middleware Services
 */
public class SearchResultDone extends AbstractResult
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 5;

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10301;


  /**
   * Default constructor.
   */
  private SearchResultDone() {}


  /**
   * Creates a new search result done.
   *
   * @param  buffer  to decode
   */
  public SearchResultDone(final ByteBuffer buffer)
  {
    final DERParser parser = new DERParser();
    parser.registerHandler(MessageIDHandler.PATH, new MessageIDHandler(this));
    parser.registerHandler("/SEQ/APP(5)/ENUM[0]", new ResultCodeHandler(this));
    parser.registerHandler("/SEQ/APP(5)/OCTSTR[1]", new MatchedDNHandler(this));
    parser.registerHandler("/SEQ/APP(5)/OCTSTR[2]", new DiagnosticMessageHandler(this));
    parser.registerHandler("/SEQ/APP(5)/CTX(3)/OCTSTR[0]", new ReferralHandler(this));
    parser.registerHandler(ControlsHandler.PATH, new ControlsHandler(this));
    parser.parse(buffer);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof SearchResultDone && super.equals(o);
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
  protected static class Builder extends AbstractResult.AbstractBuilder<Builder, SearchResultDone>
  {


    public Builder()
    {
      super(new SearchResultDone());
    }


    @Override
    protected Builder self()
    {
      return this;
    }
  }
  // CheckStyle:ON
}
