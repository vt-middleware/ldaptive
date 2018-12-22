/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.OctetStringType;

/**
 * LDAP search result entry defined as:
 *
 * <pre>
   SearchResultEntry ::= [APPLICATION 4] SEQUENCE {
     objectName      LDAPDN,
     attributes      PartialAttributeList }

   PartialAttributeList ::= SEQUENCE OF
     partialAttribute PartialAttribute

   PartialAttribute ::= SEQUENCE {
     type       AttributeDescription,
     vals       SET OF value AttributeValue }
 * </pre>
 *
 * @author  Middleware Services
 */
public class SearchResultEntry extends AbstractMessage
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 4;

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10303;

  /** LDAP DN of the entry. */
  private String ldapDN;

  /** LDAP attributes on the entry. */
  private Map<String, Attribute> attributes = new LinkedHashMap<>();


  /**
   * Default constructor.
   */
  private SearchResultEntry() {}


  /**
   * Creates a new search result entry.
   *
   * @param  buffer  to decode
   */
  public SearchResultEntry(final ByteBuffer buffer)
  {
    final DERParser parser = new DERParser();
    parser.registerHandler(MessageIDHandler.PATH, new MessageIDHandler(this));
    parser.registerHandler("/SEQ/APP(4)/OCTSTR[0]", new LdapDnHandler(this));
    parser.registerHandler("/SEQ/APP(4)/SEQ/SEQ", new AttributesHandler(this));
    parser.registerHandler(ControlsHandler.PATH, new ControlsHandler(this));
    parser.parse(buffer);
  }


  public String getLdapDN()
  {
    return ldapDN;
  }


  public void setLdapDN(final String dn)
  {
    ldapDN = dn;
  }


  public Attribute[] getAttributes()
  {
    return attributes.values().stream().toArray(Attribute[]::new);
  }


  /**
   * Adds attributes to the entry.
   *
   * @param  attr  attributes to add
   */
  public void addAttributes(final Attribute... attr)
  {
    for (Attribute a : attr) {
      attributes.put(a.getName(), a);
    }
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof SearchResultEntry && super.equals(o)) {
      final SearchResultEntry v = (SearchResultEntry) o;
      return LdapUtils.areEqual(ldapDN, v.ldapDN) && LdapUtils.areEqual(attributes, v.attributes);
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
        ldapDN,
        attributes);
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      super.toString()).append(", ")
      .append("dn=").append(ldapDN).append(", ")
      .append("attributes=").append(attributes != null ? attributes.values() : null).toString();
  }


  /** Parse handler implementation for the LDAP DN. */
  protected static class LdapDnHandler extends AbstractParseHandler<SearchResultEntry>
  {


    /**
     * Creates a new ldap dn handler.
     *
     * @param  response  to configure
     */
    LdapDnHandler(final SearchResultEntry response)
    {
      super(response);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      getObject().setLdapDN(OctetStringType.decode(encoded));
    }
  }


  /** Parse handler implementation for the attributes. */
  protected static class AttributesHandler extends AbstractParseHandler<SearchResultEntry>
  {


    /**
     * Creates a new attributes handler.
     *
     * @param  response  to configure
     */
    AttributesHandler(final SearchResultEntry response)
    {
      super(response);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      final String[] name = {null};
      final List<byte[]> vals = new ArrayList<>();
      final DERParser p = new DERParser();
      p.registerHandler("/OCTSTR", (p1, e1) -> name[0] = OctetStringType.decode(e1));
      p.registerHandler("/SET/OCTSTR", (p1, e1) -> vals.add(OctetStringType.readBuffer(e1)));
      p.parse(encoded);
      if (name[0] == null) {
        throw new IllegalArgumentException("Could not parse attribute");
      }
      if (vals.isEmpty()) {
        getObject().addAttributes(new Attribute(name[0]));
      } else {
        getObject().addAttributes(new Attribute(name[0], vals.toArray(new byte[vals.size()][])));
      }
    }
  }


  // CheckStyle:OFF
  protected static class Builder extends AbstractMessage.AbstractBuilder<Builder, SearchResultEntry>
  {


    public Builder()
    {
      super(new SearchResultEntry());
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    public Builder ldapDN(final String dn)
    {
      object.setLdapDN(dn);
      return this;
    }


    public Builder attributes(final Attribute... attr)
    {
      object.addAttributes(attr);
      return this;
    }
  }
  // CheckStyle:ON
}
