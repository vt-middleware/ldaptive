/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
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
public class LdapEntry extends AbstractMessage
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 4;

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10303;

  /** DER path to LDAP DN. */
  private static final DERPath LDAP_DN_PATH = new DERPath("/SEQ/APP(4)/OCTSTR[0]");

  /** DER path to attributes. */
  private static final DERPath ATTRIBUTES_PATH = new DERPath("/SEQ/APP(4)/SEQ/SEQ");

  /** LDAP DN of the entry. */
  private String ldapDn;

  /** LDAP attributes on the entry. */
  private Map<String, LdapAttribute> attributes = new LinkedHashMap<>();


  /**
   * Default constructor.
   */
  public LdapEntry() {}


  /**
   * Creates a new search result entry.
   *
   * @param  buffer  to decode
   */
  public LdapEntry(final DERBuffer buffer)
  {
    final DERParser parser = new DERParser();
    parser.registerHandler(MessageIDHandler.PATH, new MessageIDHandler(this));
    parser.registerHandler(LDAP_DN_PATH, new LdapDnHandler(this));
    parser.registerHandler(ATTRIBUTES_PATH, new AttributesHandler(this));
    parser.registerHandler(ControlsHandler.PATH, new ControlsHandler(this));
    parser.parse(buffer);
  }


  /**
   * Returns the ldap DN.
   *
   * @return  ldap DN
   */
  public String getDn()
  {
    return ldapDn;
  }


  /**
   * Sets the ldap DN.
   *
   * @param  dn  ldap DN
   */
  public void setDn(final String dn)
  {
    ldapDn = dn;
  }


  /**
   * Returns the ldap attributes.
   *
   * @return  ldap attributes
   */
  public Collection<LdapAttribute> getAttributes()
  {
    return attributes.values();
  }


  /**
   * Returns a single attribute of this attributes. If multiple attributes exist the first attribute returned by the
   * underlying iterator is used. If no attributes exist null is returned.
   *
   * @return  single attribute
   */
  public LdapAttribute getAttribute()
  {
    if (attributes.isEmpty()) {
      return null;
    }
    return attributes.values().iterator().next();
  }


  /**
   * Returns the attribute with the supplied name.
   *
   * @param  name  of the attribute to return
   *
   * @return  ldap attribute
   */
  public LdapAttribute getAttribute(final String name)
  {
    if (name != null) {
      return attributes.get(name.toLowerCase());
    }
    return null;
  }


  /**
   * Returns the attribute names in this entry.
   *
   * @return  string array of attribute names
   */
  public String[] getAttributeNames()
  {
    return attributes.values().stream().map(LdapAttribute::getName).toArray(String[]::new);
  }


  /**
   * Adds attributes to the entry.
   *
   * @param  attrs  attributes to add
   */
  public void addAttributes(final LdapAttribute... attrs)
  {
    for (LdapAttribute a : attrs) {
      attributes.put(a.getName().toLowerCase(), a);
    }
  }


  /**
   * Adds attributes to the entry.
   *
   * @param  attrs  attributes to add
   */
  public void addAttributes(final Collection<LdapAttribute> attrs)
  {
    attrs.forEach(a -> attributes.put(a.getName().toLowerCase(), a));
  }


  /**
   * Removes the attribute with the supplied name.
   *
   * @param  name  of attribute to remove
   */
  public void removeAttribute(final String name)
  {
    attributes.remove(name.toLowerCase());
  }


  /**
   * Removes an attribute from this ldap attributes.
   *
   * @param  attrs  attribute to remove
   */
  public void removeAttributes(final LdapAttribute... attrs)
  {
    for (LdapAttribute a : attrs) {
      attributes.remove(a.getName().toLowerCase());
    }
  }


  /**
   * Removes the attribute(s) from this ldap attributes.
   *
   * @param  attrs  collection of ldap attributes to remove
   */
  public void removeAttributes(final Collection<LdapAttribute> attrs)
  {
    attrs.forEach(a -> attributes.remove(a.getName().toLowerCase()));
  }


  /**
   * Returns the number of attributes.
   *
   * @return  number of attributes
   */
  public int size()
  {
    return attributes.size();
  }


  /** Removes all the attributes. */
  public void clear()
  {
    attributes.clear();
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof LdapEntry && super.equals(o)) {
      final LdapEntry v = (LdapEntry) o;
      return LdapUtils.areEqual(
        ldapDn != null ? ldapDn.toLowerCase() : null,
        v.ldapDn != null ? v.ldapDn.toLowerCase() : null) &&
        LdapUtils.areEqual(attributes, v.attributes);
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
        ldapDn != null ? ldapDn.toLowerCase() : null,
        attributes);
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      super.toString()).append(", ")
      .append("dn=").append(ldapDn).append(", ")
      .append("attributes=").append(attributes != null ? attributes.values() : null).toString();
  }


  /**
   * Returns a new entry whose attributes are sorted naturally by name without options.
   *
   * @param  le  entry to sort
   *
   * @return  sorted entry
   */
  public static LdapEntry sort(final LdapEntry le)
  {
    final LdapEntry sorted = new LdapEntry();
    sorted.copyValues(le);
    sorted.setDn(le.getDn());
    sorted.addAttributes(
      le.getAttributes().stream()
        .map(LdapAttribute::sort)
        .sorted(Comparator.comparing(o -> o.getName(false))).collect(Collectors.toCollection(LinkedHashSet::new)));
    return sorted;
  }


  /**
   * Returns the list of attribute modifications needed to change the supplied target entry into the supplied source
   * entry. See {@link #computeModifications(LdapEntry, LdapEntry, boolean)}.
   *
   * @param  source  ldap entry containing new data
   * @param  target  ldap entry containing existing data
   *
   * @return  attribute modifications needed to change target into source or an empty array
   */
  public static AttributeModification[] computeModifications(final LdapEntry source, final LdapEntry target)
  {
    return computeModifications(source, target, true);
  }


  /**
   * Returns the list of attribute modifications needed to change the supplied target entry into the supplied source
   * entry. This implementation performs a byte comparison on the attribute values to determine changes.
   *
   * @param  source  ldap entry containing new data
   * @param  target  ldap entry containing existing data
   * @param  useReplace  whether to use a single REPLACE modification or individual ADD/DELETE for attribute values
   *
   * @return  attribute modifications needed to change target into source or an empty array
   */
  public static AttributeModification[] computeModifications(
    final LdapEntry source, final LdapEntry target, final boolean useReplace)
  {
    final List<AttributeModification> mods = new ArrayList<>();
    for (LdapAttribute sourceAttr : source.getAttributes()) {
      final LdapAttribute targetAttr = target.getAttribute(sourceAttr.getName());
      if (targetAttr == null) {
        mods.add(new AttributeModification(AttributeModification.Type.ADD, sourceAttr));
      } else if (!targetAttr.equals(sourceAttr)) {
        if (useReplace) {
          mods.add(new AttributeModification(AttributeModification.Type.REPLACE, sourceAttr));
        } else {
          final Collection<byte[]> sourceValues = sourceAttr.getBinaryValues();
          final Collection<byte[]> targetValues = targetAttr.getBinaryValues();

          final Set<byte[]> valuesToAdd = sourceValues.stream()
            .filter(sv -> targetValues.stream().noneMatch(tv -> Arrays.equals(sv, tv)))
            .collect(Collectors.toSet());
          if (!valuesToAdd.isEmpty()) {
            mods.add(
              new AttributeModification(
                AttributeModification.Type.ADD,
                LdapAttribute.builder().name(sourceAttr.getName()).binaryValues(valuesToAdd).build()));
          }

          final Set<byte[]> valuesToDelete = targetValues.stream()
            .filter(tv -> sourceValues.stream().noneMatch(sv -> Arrays.equals(tv, sv)))
            .collect(Collectors.toSet());
          if (!valuesToDelete.isEmpty()) {
            mods.add(
              new AttributeModification(
                AttributeModification.Type.DELETE,
                LdapAttribute.builder().name(sourceAttr.getName()).binaryValues(valuesToDelete).build()));
          }
        }
      }
    }
    for (LdapAttribute targetAttr : target.getAttributes()) {
      final LdapAttribute sourceAttr = source.getAttribute(targetAttr.getName());
      if (sourceAttr == null) {
        mods.add(
          new AttributeModification(
            AttributeModification.Type.DELETE,
            LdapAttribute.builder().name(targetAttr.getName()).build()));
      }
    }
    return mods.toArray(AttributeModification[]::new);
  }


  /** Parse handler implementation for the LDAP DN. */
  protected static class LdapDnHandler extends AbstractParseHandler<LdapEntry>
  {


    /**
     * Creates a new ldap dn handler.
     *
     * @param  response  to configure
     */
    LdapDnHandler(final LdapEntry response)
    {
      super(response);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setDn(OctetStringType.decode(encoded));
    }
  }


  /** Parse handler implementation for the attributes. */
  protected static class AttributesHandler extends AbstractParseHandler<LdapEntry>
  {


    /**
     * Creates a new attributes handler.
     *
     * @param  response  to configure
     */
    AttributesHandler(final LdapEntry response)
    {
      super(response);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      final AttributeParser p = new AttributeParser();
      p.parse(encoded);

      if (p.getName().isEmpty()) {
        throw new IllegalArgumentException("Could not parse attribute");
      }
      if (p.getValues().isEmpty()) {
        getObject().addAttributes(LdapAttribute.builder().name(p.getName().get()).build());
      } else {
        getObject().addAttributes(
          LdapAttribute.builder().name(p.getName().get()).bufferValues(p.getValues().get()).build());
      }
    }
  }


  /**
   * Parses a buffer containing an attribute name and it's values.
   */
  protected static class AttributeParser
  {

    /** DER path to name. */
    private static final DERPath NAME_PATH = new DERPath("/OCTSTR");

    /** DER path to values. */
    private static final DERPath VALUES_PATH = new DERPath("/SET/OCTSTR");

    /** Parser for decoding LDAP attributes. */
    private final DERParser parser = new DERParser();

    /** Attribute name. */
    private String name;

    /** Attribute values. */
    private List<ByteBuffer> values = new ArrayList<>();


    /**
     * Creates a new attribute parser.
     */
    public AttributeParser()
    {
      parser.registerHandler(NAME_PATH, (p, e) -> name = OctetStringType.decode(e));
      parser.registerHandler(VALUES_PATH, (p, e) -> values.add(ByteBuffer.wrap(e.getRemainingBytes())));
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
    public Optional<List<ByteBuffer>> getValues()
    {
      return values.isEmpty() ? Optional.empty() : Optional.of(values);
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
  public static class Builder extends AbstractMessage.AbstractBuilder<Builder, LdapEntry>
  {


    protected Builder()
    {
      super(new LdapEntry());
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    public Builder dn(final String dn)
    {
      object.setDn(dn);
      return this;
    }


    public Builder attributes(final LdapAttribute... attrs)
    {
      object.addAttributes(attrs);
      return this;
    }


    public Builder attributes(final Collection<LdapAttribute> attrs)
    {
      object.addAttributes(attrs);
      return this;
    }
  }
  // CheckStyle:ON
}
