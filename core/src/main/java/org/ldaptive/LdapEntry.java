/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
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
public class LdapEntry extends AbstractMessage implements Freezable
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 4;

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10303;

  /** DER path to LDAP DN. */
  private static final DERPath LDAP_DN_PATH = new DERPath("/SEQ/APP(4)/OCTSTR[0]");

  /** DER path to attributes. */
  private static final DERPath ATTRIBUTES_PATH = new DERPath("/SEQ/APP(4)/SEQ/SEQ");

  /** LDAP attributes on the entry. */
  private final Map<String, LdapAttribute> attributes = new LinkedHashMap<>();

  /** LDAP DN of the entry. */
  private String ldapDn;

  /** Parsed LDAP DN. */
  private Dn parsedDn;

  /** Normalized LDAP DN. */
  private String normalizedDn;

  /** Whether this object has been marked immutable. */
  private volatile boolean immutable;


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
    try {
      parser.parse(buffer);
    } catch (Exception e) {
      throw new IllegalArgumentException("Error parsing response", e);
    }
  }


  @Override
  public void freeze()
  {
    immutable = true;
    if (parsedDn != null) {
      parsedDn.freeze();
    }
    attributes.values().forEach(Freezable::freeze);
  }


  @Override
  public final boolean isFrozen()
  {
    return immutable;
  }


  @Override
  public final void assertMutable()
  {
    if (immutable) {
      throw new IllegalStateException("Cannot modify immutable object");
    }
  }


  /**
   * Returns the ldap DN.
   *
   * @return  ldap DN
   */
  public final String getDn()
  {
    return ldapDn;
  }


  /**
   * Returns the parsed ldap DN. Parsing is performed using {@link org.ldaptive.dn.DefaultDnParser}.
   *
   * @return  parsed ldap DN or null if {@link #ldapDn} is null or could not be parsed
   */
  public final Dn getParsedDn()
  {
    return parsedDn;
  }


  /**
   * Returns the normalized ldap DN. Normalization is performed using {@link org.ldaptive.dn.DefaultRDnNormalizer}.
   *
   * @return  normalized ldap DN or null if {@link #ldapDn} is null or could not be parsed
   */
  public final String getNormalizedDn()
  {
    return normalizedDn;
  }


  /**
   * Sets the ldap DN.
   *
   * @param  dn  ldap DN
   */
  public final void setDn(final String dn)
  {
    assertMutable();
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
   * Returns whether this entry contains an attribute with the supplied name.
   *
   * @param  name  of the attribute to match
   *
   * @return  whether this entry contains an attribute with the supplied name
   */
  public boolean hasAttribute(final String name)
  {
    if (name != null) {
      return attributes.containsKey(LdapUtils.toLowerCase(name));
    }
    return false;
  }


  /**
   * Provides a null safe invocation of {@link LdapAttribute#hasValue(byte[])}.
   *
   * @param  name  of the attribute containing a value
   * @param  value  to find
   *
   * @return  whether value exists in the attribute with the supplied name
   */
  public boolean hasAttributeValue(final String name, final byte[] value)
  {
    final LdapAttribute attr = getAttribute(name);
    if (attr != null) {
      return attr.hasValue(value);
    }
    return false;
  }


  /**
   * Provides a null safe invocation of {@link LdapAttribute#hasValue(String)}.
   *
   * @param  name  of the attribute containing a value
   * @param  value  to find
   *
   * @return  whether value exists in the attribute with the supplied name
   */
  public boolean hasAttributeValue(final String name, final String value)
  {
    final LdapAttribute attr = getAttribute(name);
    if (attr != null) {
      return attr.hasValue(value);
    }
    return false;
  }


  /**
   * Returns the ldap attributes in this entry. Collection will be immutable if {@link #immutable} is true.
   *
   * @return  ldap attributes
   */
  public Collection<LdapAttribute> getAttributes()
  {
    return immutable ? Collections.unmodifiableCollection(attributes.values()) : attributes.values();
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
   * @return  ldap attribute or null if the attribute does not exist
   */
  public LdapAttribute getAttribute(final String name)
  {
    if (name != null) {
      return attributes.get(LdapUtils.toLowerCase(name));
    }
    return null;
  }


  /**
   * Provides a null safe invocation of {@link LdapAttribute#getValue(Function)}.
   *
   * @param  <T>  type of decoded attribute
   * @param  name  of the attribute whose values should be returned
   * @param  func  to decode attribute value with
   *
   * @return  single attribute value or null if the attribute does not exist
   */
  public <T> T getAttributeValue(final String name, final Function<byte[], T> func)
  {
    final LdapAttribute attr = getAttribute(name);
    if (attr != null) {
      return attr.getValue(func);
    }
    return null;
  }


  /**
   * Provides a null safe invocation of {@link LdapAttribute#getValues(Function)}.
   *
   * @param  <T>  type of decoded attribute
   * @param  name  of the attribute whose values should be returned
   * @param  func  to decode attribute value with
   *
   * @return  collection of attribute values or an empty collection if the attribute does not exist
   */
  public <T> Collection<T> getAttributeValues(final String name, final Function<byte[], T> func)
  {
    final LdapAttribute attr = getAttribute(name);
    if (attr != null) {
      return attr.getValues(func);
    }
    return Collections.emptyList();
  }


  /**
   * Provides a null safe invocation of {@link LdapAttribute#getBinaryValue()}.
   *
   * @param  name  of the attribute whose values should be returned
   *
   * @return  single byte array attribute value or null if the attribute does not exist
   */
  public byte[] getAttributeBinaryValue(final String name)
  {
    final LdapAttribute attr = getAttribute(name);
    if (attr != null) {
      return attr.getBinaryValue();
    }
    return null;
  }


  /**
   * Provides a null safe invocation of {@link LdapAttribute#getBinaryValues()}.
   *
   * @param  name  of the attribute whose values should be returned
   *
   * @return  collection of byte array attribute values or an empty collection if the attribute does not exist
   */
  public Collection<byte[]> getAttributeBinaryValues(final String name)
  {
    final LdapAttribute attr = getAttribute(name);
    if (attr != null) {
      return attr.getBinaryValues();
    }
    return Collections.emptyList();
  }


  /**
   * Provides a null safe invocation of {@link LdapAttribute#getStringValue()}.
   *
   * @param  name  of the attribute whose values should be returned
   *
   * @return  single string attribute value or null if the attribute does not exist
   */
  public String getAttributeStringValue(final String name)
  {
    final LdapAttribute attr = getAttribute(name);
    if (attr != null) {
      return attr.getStringValue();
    }
    return null;
  }


  /**
   * Provides a null safe invocation of {@link LdapAttribute#getStringValues()}.
   *
   * @param  name  of the attribute whose values should be returned
   *
   * @return  collection of string attribute values or an empty collection if the attribute does not exist
   */
  public Collection<String> getAttributeStringValues(final String name)
  {
    final LdapAttribute attr = getAttribute(name);
    if (attr != null) {
      return attr.getStringValues();
    }
    return Collections.emptyList();
  }


  /**
   * Processes an attribute using the supplied consumer. The consumer is invoked only if the attribute exists.
   *
   * @param  name  of the attribute
   * @param  func  to process the attribute if it exists
   *
   * @return  whether the consumer was invoked
   */
  public boolean processAttribute(final String name, final Consumer<LdapAttribute> func)
  {
    return mapAttribute(
      name,
      attr -> {
        func.accept(attr);
        return true;
      },
      false);
  }


  /**
   * Returns a mapped value from an attribute using the supplied function or null if the attribute doesn't exist.
   *
   * @param  <T>  type of mapped value
   * @param  name  of the attribute
   * @param  func  to map the attribute with
   *
   * @return  mapped ldap attribute
   */
  public <T> T mapAttribute(final String name, final Function<LdapAttribute, T> func)
  {
    return mapAttribute(name, func, null);
  }


  /**
   * Returns a mapped value from an attribute using the supplied function or defaultValue if the attribute doesn't
   * exist.
   *
   * @param  <T>  type of mapped value
   * @param  name  of the attribute
   * @param  func  to map the attribute with
   * @param  defaultValue  to return if no attribute with name exists
   *
   * @return  mapped ldap attribute
   */
  public <T> T mapAttribute(final String name, final Function<LdapAttribute, T> func, final T defaultValue)
  {
    final LdapAttribute attr = getAttribute(name);
    if (attr != null) {
      return func.apply(attr);
    }
    return defaultValue;
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
   * Adds attributes to the entry. If an attribute with the same name already exists, it is replaced.
   *
   * @param  attrs  attributes to add
   */
  public void addAttributes(final LdapAttribute... attrs)
  {
    assertMutable();
    for (LdapAttribute a : attrs) {
      attributes.put(LdapUtils.toLowerCase(a.getName(), false), a);
    }
  }


  /**
   * Adds attributes to the entry. If an attribute with the same name already exists, it is replaced.
   *
   * @param  attrs  attributes to add
   */
  public void addAttributes(final Collection<LdapAttribute> attrs)
  {
    assertMutable();
    attrs.forEach(a -> attributes.put(LdapUtils.toLowerCase(a.getName(), false), a));
  }


  /**
   * Merges attributes into this entry. If an attribute with the same name already exists, the values of the supplied
   * attribute are added to the existing attribute. If the supplied attribute doesn't exist, the attribute is added to
   * this entry.
   *
   * @param  attrs  attributes to merge
   */
  public void mergeAttributes(final LdapAttribute... attrs)
  {
    assertMutable();
    for (LdapAttribute a : attrs) {
      final String lowerName = LdapUtils.toLowerCase(a.getName(), false);
      if (!attributes.containsKey(lowerName)) {
        attributes.put(lowerName, LdapAttribute.copy(a));
      } else {
        attributes.get(lowerName).merge(a);
      }
    }
  }


  /**
   * Merges attributes into this entry. If an attribute with the same name already exists, the values of the supplied
   * attribute are added to the existing attribute. If the supplied attribute doesn't exist, the attribute is added to
   * this entry.
   *
   * @param  attrs  attributes to merge
   */
  public void mergeAttributes(final Collection<LdapAttribute> attrs)
  {
    assertMutable();
    attrs.forEach(a -> {
      final String lowerName = LdapUtils.toLowerCase(a.getName(), false);
      if (!attributes.containsKey(lowerName)) {
        attributes.put(lowerName, LdapAttribute.copy(a));
      } else {
        attributes.get(lowerName).merge(a);
      }
    });
  }


  /**
   * Removes the attribute with the supplied name.
   *
   * @param  name  of attribute to remove
   */
  public void removeAttribute(final String name)
  {
    assertMutable();
    attributes.remove(LdapUtils.toLowerCase(name, false));
  }


  /**
   * Removes an attribute from this ldap attributes.
   *
   * @param  attrs  attribute to remove
   */
  public void removeAttributes(final LdapAttribute... attrs)
  {
    assertMutable();
    for (LdapAttribute a : attrs) {
      attributes.remove(LdapUtils.toLowerCase(a.getName(), false));
    }
  }


  /**
   * Removes the attribute(s) from this ldap attributes.
   *
   * @param  attrs  collection of ldap attributes to remove
   */
  public void removeAttributes(final Collection<LdapAttribute> attrs)
  {
    assertMutable();
    attrs.forEach(a -> attributes.remove(LdapUtils.toLowerCase(a.getName(), false)));
  }


  /**
   * Returns the number of attributes.
   *
   * @return  number of attributes
   */
  public final int size()
  {
    return attributes.size();
  }


  /** Removes all the attributes from this entry. */
  public final void clear()
  {
    assertMutable();
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
   * Creates a mutable copy of the supplied entry.
   *
   * @param  entry  to copy
   *
   * @return  new ldap entry instance
   */
  public static LdapEntry copy(final LdapEntry entry)
  {
    final LdapEntry copy = new LdapEntry();
    copy.copyValues(entry);
    copy.ldapDn = entry.ldapDn;
    copy.parsedDn = entry.parsedDn != null ? Dn.copy(entry.parsedDn) : null;
    copy.normalizedDn = entry.normalizedDn;
    for (Map.Entry<String, LdapAttribute> e : entry.attributes.entrySet()) {
      copy.attributes.put(e.getKey(), LdapAttribute.copy(e.getValue()));
    }
    return copy;
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
    if (le.isFrozen()) {
      sorted.freeze();
    }
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
          LdapAttribute.builder().name(p.getName().get()).binaryValuesInternal(p.getValues().get()).build());
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

    /** Attribute values. */
    private final List<byte[]> values = new ArrayList<>();

    /** Attribute name. */
    private String name;


    /**
     * Creates a new attribute parser.
     */
    public AttributeParser()
    {
      parser.registerHandler(NAME_PATH, (p, e) -> name = OctetStringType.decode(e));
      parser.registerHandler(VALUES_PATH, (p, e) -> values.add(e.getRemainingBytes()));
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
    public Optional<List<byte[]>> getValues()
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


    public Builder freeze()
    {
      object.freeze();
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
