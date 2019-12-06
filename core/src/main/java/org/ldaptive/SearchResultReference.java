/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
  private List<String> references = new ArrayList<>();


  /**
   * Default constructor.
   */
  public SearchResultReference() {}


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
    return references.toArray(new String[0]);
  }


  /**
   * Adds a new URI to this reference.
   *
   * @param  uri  to add
   */
  public void addUris(final String... uri)
  {
    for (String s : uri) {
      references.add(s);
    }
  }


  /**
   * Adds a new URI to this reference.
   *
   * @param  uris  to add
   */
  public void addUris(final Collection<String> uris)
  {
    references.addAll(uris);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof SearchResultReference && super.equals(o)) {
      final SearchResultReference v = (SearchResultReference) o;
      return LdapUtils.areEqual(references, v.references);
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
        references);
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      super.toString()).append(", ")
      .append("URIs=").append(references).toString();
  }


  /**
   * Returns a new reference whose URIs are sorted naturally.
   *
   * @param  ref  reference to sort
   *
   * @return  sorted reference
   */
  public static SearchResultReference sort(final SearchResultReference ref)
  {
    final SearchResultReference sorted = new SearchResultReference();
    sorted.copyValues(ref);
    sorted.addUris(Stream.of(ref.getUris()).sorted().collect(Collectors.toList()));
    return sorted;
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
  public static class Builder extends AbstractMessage.AbstractBuilder<Builder, SearchResultReference>
  {


    protected Builder()
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
