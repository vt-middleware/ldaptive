/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERBuffer;
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
  public SearchResultEntry(final DERBuffer buffer)
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
    public void handle(final DERParser parser, final DERBuffer encoded)
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
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      final AttributeParser p = new AttributeParser();
      p.parse(encoded);

      if (!p.getName().isPresent()) {
        throw new IllegalArgumentException("Could not parse attribute");
      }
      if (!p.getValues().isPresent()) {
        getObject().addAttributes(new Attribute(p.getName().get()));
      } else {
        getObject().addAttributes(new Attribute(p.getName().get(), p.getValues().get()));
      }
    }
  }


  /**
   * Parses a buffer containing an attribute name and it's values.
   */
  protected static class AttributeParser
  {

    /** Parser for decoding LDAP attributes. */
    private final DERParser parser = new DERParser();

    /** Attribute name. */
    private String name;

    /** Attribute values. */
    private List<byte[]> values = new ArrayList<>();


    /**
     * Creates a new attribute parser.
     */
    public AttributeParser()
    {
      parser.registerHandler("/OCTSTR", (p, e) -> name = OctetStringType.decode(e));
      parser.registerHandler("/SET/OCTSTR", (p, e) -> values.add(e.getRemainingBytes()));
    }


    /**
     * Examines the supplied buffer and parses an LDAP attribute if one is found.
     *
     * @param  buffer  to parse
     */
    public void parse(final DERBuffer buffer)
    {
      parser.parse(buffer);
    }


    /**
     * Returns the attribute name.
     *
     * @return  attribute name or empty
     */
    public Optional<String> getName()
    {
      return Optional.ofNullable(name);
    }


    /**
     * Returns the attribute values.
     *
     * @return  attribute values or empty
     */
    public Optional<byte[][]> getValues()
    {
      return values.isEmpty() ? Optional.empty() : Optional.of(values.stream().toArray(byte[][]::new));
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
