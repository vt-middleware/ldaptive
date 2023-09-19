/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.dn.Dn;

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

  /** Parsed LDAP DN. */
  private Dn parsedDn;

  /** Normalized LDAP DN. */
  private String normalizedDn;

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
   * Returns the parsed ldap DN. Parsing is performed using {@link org.ldaptive.dn.DefaultDnParser}.
   *
   * @return  parsed ldap DN or null if {@link #ldapDn} is null or could not be parsed
   */
  public Dn getParsedDn()
  {
    return parsedDn;
  }


  /**
   * Returns the normalized ldap DN. Normalization is performed using {@link org.ldaptive.dn.DefaultRDnNormalizer}.
   *
   * @return  normalized ldap DN or null if {@link #ldapDn} is null or could not be parsed
   */
  public String getNormalizedDn()
  {
    return normalizedDn;
  }


  /**
   * Sets the ldap DN.
   *
   * @param  dn  ldap DN
   */
  public void setDn(final String dn)
  {
    ldapDn = dn;
    if (ldapDn != null) {
      try {
        parsedDn = new Dn(ldapDn);
      } catch (Exception e) {
        parsedDn = null;
      }
      if (parsedDn != null) {
        normalizedDn = parsedDn.format();
      }
    }
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
   * Returns a single attribute of this entry. If multiple attributes exist the first attribute returned by the
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
      return attributes.get(LdapUtils.toLowerCase(name));
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
      attributes.put(LdapUtils.toLowerCase(a.getName()), a);
    }
  }


  /**
   * Adds attributes to the entry.
   *
   * @param  attrs  attributes to add
   */
  public void addAttributes(final Collection<LdapAttribute> attrs)
  {
    attrs.forEach(a -> attributes.put(LdapUtils.toLowerCase(a.getName()), a));
  }


  /**
   * Removes the attribute with the supplied name.
   *
   * @param  name  of attribute to remove
   */
  public void removeAttribute(final String name)
  {
    attributes.remove(LdapUtils.toLowerCase(name));
  }


  /**
   * Removes an attribute from this ldap attributes.
   *
   * @param  attrs  attribute to remove
   */
  public void removeAttributes(final LdapAttribute... attrs)
  {
    for (LdapAttribute a : attrs) {
      attributes.remove(LdapUtils.toLowerCase(a.getName()));
    }
  }


  /**
   * Removes the attribute(s) from this ldap attributes.
   *
   * @param  attrs  collection of ldap attributes to remove
   */
  public void removeAttributes(final Collection<LdapAttribute> attrs)
  {
    attrs.forEach(a -> attributes.remove(LdapUtils.toLowerCase(a.getName())));
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
      // compare normalizedDn if not null, else compare Dn
      return LdapUtils.areEqual(
        normalizedDn != null ? normalizedDn : ldapDn,
        normalizedDn != null ? v.normalizedDn : v.normalizedDn != null ? v.normalizedDn : v.ldapDn) &&
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
        normalizedDn != null ? normalizedDn : ldapDn,
        attributes);
  }


  @Override
  public String toString()
  {
    return super.toString() + ", " +
      "dn=" + ldapDn + ", " +
      "attributes=" + (attributes != null ? attributes.values() : null);
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
        if (sourceAttr.size() > 0) {
          mods.add(new AttributeModification(AttributeModification.Type.ADD, sourceAttr));
        } else {
          // perform a replace if attribute has no values to avoid potential schema issues
          mods.add(new AttributeModification(AttributeModification.Type.REPLACE, sourceAttr));
        }
      } else if (!targetAttr.equals(sourceAttr)) {
        if (useReplace) {
          mods.add(new AttributeModification(AttributeModification.Type.REPLACE, sourceAttr));
        } else {
          final LdapAttribute toAdd = new LdapAttribute(sourceAttr.getName());
          sourceAttr.getBinaryValues().stream()
            .filter(sv -> !targetAttr.hasValue(sv))
            .forEach(toAdd::addBinaryValues);
          if (toAdd.size() > 0) {
            mods.add(new AttributeModification(AttributeModification.Type.ADD, toAdd));
          }

          final LdapAttribute toDelete = new LdapAttribute(sourceAttr.getName());
          targetAttr.getBinaryValues().stream()
            .filter(tv -> !sourceAttr.hasValue(tv))
            .forEach(toDelete::addBinaryValues);
          if (toDelete.size() > 0) {
            mods.add(new AttributeModification(AttributeModification.Type.DELETE, toDelete));
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
   * Parses a buffer containing an attribute name and its values.
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
