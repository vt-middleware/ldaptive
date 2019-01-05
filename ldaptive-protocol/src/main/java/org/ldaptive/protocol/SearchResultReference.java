/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.util.ArrayList;
import java.util.List;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.OctetStringType;

/**
 * LDAP search result entry defined as:
 *
 * <pre>
   SearchResultReference ::= [APPLICATION 19] SEQUENCE
     SIZE (1..MAX) OF uri URI * </pre>
 *
 * @author  Middleware Services
 */
public class SearchResultReference extends AbstractMessage
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 19;

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10313;

  /** List of references. */
  private List<String> uris = new ArrayList<>();


  /**
   * Default constructor.
   */
  private SearchResultReference() {}


  /**
   * Creates a new search result reference.
   *
   * @param  buffer  to decode
   */
  public SearchResultReference(final DERBuffer buffer)
  {
    final DERParser parser = new DERParser();
    parser.registerHandler(MessageIDHandler.PATH, new MessageIDHandler(this));
    parser.registerHandler("/SEQ/APP(19)/OCTSTR", new ReferralUriHandler(this));
    parser.registerHandler(ControlsHandler.PATH, new ControlsHandler(this));
    parser.parse(buffer);
  }


  public String[] getUris()
  {
    return uris.stream().toArray(String[]::new);
  }


  /**
   * Adds a new URI to this reference.
   *
   * @param  uri  to add
   */
  private void addUris(final String... uri)
  {
    for (String s : uri) {
      uris.add(s);
    }
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof SearchResultReference && super.equals(o)) {
      final SearchResultReference v = (SearchResultReference) o;
      return LdapUtils.areEqual(uris, v.uris);
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
        uris);
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      super.toString()).append(", ")
      .append("URIs=").append(uris).toString();
  }


  /** Parse handler implementation for the referral URL. */
  protected static class ReferralUriHandler extends AbstractParseHandler<SearchResultReference>
  {


    /**
     * Creates a new referral URI handler.
     *
     * @param  response  to configure
     */
    ReferralUriHandler(final SearchResultReference response)
    {
      super(response);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().addUris(OctetStringType.decode(encoded));
    }
  }


  // CheckStyle:OFF
  protected static class Builder extends AbstractMessage.AbstractBuilder<Builder, SearchResultReference>
  {


    public Builder()
    {
      super(new SearchResultReference());
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    public Builder uris(final String... uri)
    {
      object.addUris(uri);
      return this;
    }
  }
  // CheckStyle:ON
}
