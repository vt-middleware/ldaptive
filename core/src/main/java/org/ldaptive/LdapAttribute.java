/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * LDAP attribute defined as:
 *
 * <pre>
   Attribute ::= PartialAttribute(WITH COMPONENTS {
     ...,
     vals (SIZE(1..MAX))})
 * </pre>
 *
 * @author  Middleware Services
 */
public class LdapAttribute extends AbstractFreezable
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10223;

  /** List of attribute names known to use binary syntax. */
  private static final String[] DEFAULT_BINARY_ATTRIBUTES = {
    "photo",
    "personalSignature",
    "audio",
    "jpegPhoto",
    "javaSerializedData",
    "thumbnailPhoto",
    "thumbnailLogo",
    "userCertificate",
    "cACertificate",
    "authorityRevocationList",
    "certificateRevocationList",
    "crossCertificatePair",
    "x500UniqueIdentifier",
  };

  /** List of custom binary attribute names. */
  private static final String[] BINARY_ATTRIBUTES;

  /** Attribute values. */
  private final Collection<AttributeValue> attributeValues = new LinkedHashSet<>();

  /** Attribute name. */
  private String attributeName;

  /** Whether this attribute is binary and string representations should be base64 encoded. */
  private boolean binary;

  static {
    // Configure custom binary attribute names
    final String[] split = System.getProperty("org.ldaptive.attribute.binary", "").split(",");
    BINARY_ATTRIBUTES = LdapUtils.concatArrays(DEFAULT_BINARY_ATTRIBUTES, split);
  }


  /** Default constructor. */
  public LdapAttribute() {}


  /**
   * Creates a new attribute.
   *
   * @param  type  attribute description
   */
  public LdapAttribute(final String type)
  {
    setName(type);
  }


  /**
   * Creates a new attribute.
   *
   * @param  type  attribute description
   * @param  value  attribute values
   */
  public LdapAttribute(final String type, final byte[]... value)
  {
    setName(type);
    addBinaryValues(value);
  }


  /**
   * Creates a new attribute.
   *
   * @param  type  attribute description
   * @param  value  attribute values
   */
  public LdapAttribute(final String type, final String... value)
  {
    setName(type);
    addStringValues(value);
  }


  /**
   * Sets the name. This method has the side effect of setting this attribute as binary if the name has an option of
   * 'binary' or the name matches one of {@link #BINARY_ATTRIBUTES}.
   *
   * @param  type  attribute name
   */
  public final void setName(final String type)
  {
    assertMutable();
    if (type == null) {
      throw new IllegalArgumentException("Attribute type cannot be null");
    }
    attributeName = type;
    if (getOptions().contains("binary") || Stream.of(BINARY_ATTRIBUTES).anyMatch(attributeName::equals)) {
      setBinary(true);
    }
  }


  /**
   * Returns whether this ldap attribute is binary.
   *
   * @return  whether this ldap attribute is binary
   */
  public final boolean isBinary()
  {
    return binary;
  }


  /**
   * Sets whether this ldap attribute is binary.
   *
   * @param  b  whether this ldap attribute is binary
   */
  public final void setBinary(final boolean b)
  {
    assertMutable();
    binary = b;
  }


  /**
   * Checks whether attrNames matches the name of this attribute.  If a match is found this attribute is set as binary.
   *
   * @param  attrNames  custom binary attribute names
   */
  public final void configureBinary(final String... attrNames)
  {
    assertMutable();
    if (binary) {
      return;
    }
    if (attrNames != null && attrNames.length > 0) {
      for (String s : attrNames) {
        if (attributeName.equalsIgnoreCase(s)) {
          binary = true;
          break;
        }
      }
    }
  }


  /**
   * Returns the attribute description with options.
   *
   * @return  attribute description
   */
  public final String getName()
  {
    return attributeName;
  }


  /**
   * Returns the attribute description with or without options.
   *
   * @param  withOptions  whether the attribute description should include options
   *
   * @return  attribute description
   */
  public final String getName(final boolean withOptions)
  {
    if (withOptions) {
      return attributeName;
    } else {
      final int optionIndex = attributeName.indexOf(';');
      return optionIndex > 0 ? attributeName.substring(0, optionIndex) : attributeName;
    }
  }


  /**
   * Returns any options that may exist on the attribute description.
   *
   * @return  attribute description options
   */
  public final List<String> getOptions()
  {
    if (attributeName.indexOf(';') > 0) {
      final String[] split = attributeName.split(";");
      if (split.length > 1) {
        return IntStream.range(1, split.length).mapToObj(i -> split[i]).collect(Collectors.toUnmodifiableList());
      }
    }
    return Collections.emptyList();
  }


  /**
   * Returns a single byte array value of this attribute.
   *
   * @return  single byte array attribute value or null if this attribute is empty
   */
  public byte[] getBinaryValue()
  {
    return attributeValues.isEmpty() ? null : attributeValues.iterator().next().getValue(true);
  }


  /**
   * Returns the values of this attribute as byte arrays. The return collection cannot be modified.
   *
   * @return  collection of string attribute values
   */
  public Collection<byte[]> getBinaryValues()
  {
    if (attributeValues.isEmpty()) {
      return Collections.emptySet();
    }
    return attributeValues.stream().map(av -> av.getValue(true)).collect(Collectors.toUnmodifiableList());
  }


  /**
   * Returns a single string value of this attribute.
   *
   * @return  single string attribute value or null if this attribute is empty
   */
  public String getStringValue()
  {
    if (attributeValues.isEmpty()) {
      return null;
    }
    return attributeValues.iterator().next().getStringValue(binary);
  }


  /**
   * Returns the values of this attribute as strings. Binary data is base64 encoded. The return collection cannot be
   * modified.
   *
   * @return  collection of string attribute values
   */
  public Collection<String> getStringValues()
  {
    if (attributeValues.isEmpty()) {
      return Collections.emptySet();
    }
    return attributeValues.stream().map(v -> v.getStringValue(binary)).collect(Collectors.toUnmodifiableList());
  }


  /**
   * Returns a single decoded value of this attribute.
   *
   * @param  <T>  type of decoded attribute
   * @param  func  to decode attribute value with
   *
   * @return  single decoded attribute value or null if this attribute is empty
   */
  public <T> T getValue(final Function<byte[], T> func)
  {
    return attributeValues.isEmpty() ? null : func.apply(attributeValues.iterator().next().getValue(true));
  }


  /**
   * Returns the values of this attribute decoded by the supplied function.
   *
   * @param  <T>  type of decoded attributes
   * @param  func  to decode attribute values with
   *
   * @return  collection of decoded attribute values, null values are discarded
   */
  public <T> Collection<T> getValues(final Function<byte[] , T> func)
  {
    return attributeValues.stream().map(av -> av.getValue(true)).map(func).collect(Collectors.toUnmodifiableList());
  }


  /**
   * Adds the supplied byte array as a value for this attribute.
   *
   * @param  value  to add, null values are discarded
   */
  public void addBinaryValues(final byte[]... value)
  {
    assertMutable();
    Stream.of(value).filter(Objects::nonNull).map(b -> new AttributeValue(b, true)).forEach(attributeValues::add);
  }


  /**
   * Adds all the byte arrays in the supplied collection as values for this attribute.
   *
   * @param  values  to add, null values are discarded
   */
  public void addBinaryValues(final Collection<byte[]> values)
  {
    assertMutable();
    values.stream().filter(Objects::nonNull).map(b -> new AttributeValue(b, true)).forEach(attributeValues::add);
  }


  /**
   * Adds all the byte arrays in the supplied collection as values for this attribute. This method does not create a
   * copy of the supplied byte arrays.
   *
   * @param  values  to add
   */
  void addBinaryValuesInternal(final Collection<byte[]> values)
  {
    assertMutable();
    values.stream().map(b -> new AttributeValue(b, false)).forEach(attributeValues::add);
  }


  /**
   * Adds the supplied string as a value for this attribute.
   *
   * @param  value  to add, null values are discarded
   */
  public void addStringValues(final String... value)
  {
    assertMutable();
    Stream.of(value)
      .filter(Objects::nonNull)
      .map(s -> AttributeValue.fromString(s, binary))
      .forEach(attributeValues::add);
  }


  /**
   * Adds all the strings in the supplied collection as values for this attribute.
   *
   * @param  values  to add, null values are discarded
   */
  public void addStringValues(final Collection<String> values)
  {
    assertMutable();
    values.stream()
      .filter(Objects::nonNull)
      .map(s -> AttributeValue.fromString(s, binary))
      .forEach(attributeValues::add);
  }


  /**
   * Adds the supplied values for this attribute by encoding them with the supplied function.
   *
   * @param  <T>  type attribute to encode
   * @param  func  to encode value with
   * @param  value  to encode and add, null values are discarded
   */
  @SuppressWarnings("unchecked")
  public <T> void addValues(final Function<T, byte[]> func, final T... value)
  {
    assertMutable();
    Stream.of(value)
      .filter(Objects::nonNull)
      .map(func)
      .filter(Objects::nonNull)
      .map(b -> new AttributeValue(b, true))
      .forEach(attributeValues::add);
  }


  /**
   * Adds all the values in the supplied collection for this attribute by encoding them with the supplied function.
   * See {@link #addValues(Function, Object...)}.
   *
   * @param  <T>  type attribute to encode
   * @param  func  to encode value with
   * @param  values  to encode and add, null values are discarded
   */
  public <T> void addValues(final Function<T, byte[]> func, final Collection<T> values)
  {
    assertMutable();
    values.stream()
      .filter(Objects::nonNull)
      .map(func)
      .filter(Objects::nonNull)
      .map(b -> new AttributeValue(b, true))
      .forEach(attributeValues::add);
  }


  /**
   * Removes the supplied byte array as a value from this attribute.
   *
   * @param  value  to remove, null values are discarded
   */
  public void removeBinaryValues(final byte[]... value)
  {
    assertMutable();
    Stream.of(value)
      .filter(Objects::nonNull)
      .map(b -> new AttributeValue(b, false))
      .forEach(attributeValues::remove);
  }


  /**
   * Removes all the byte arrays in the supplied collection as values from this attribute.
   *
   * @param  values  to remove, null values are discarded
   */
  public void removeBinaryValues(final Collection<byte[]> values)
  {
    assertMutable();
    values.stream()
      .filter(Objects::nonNull)
      .map(b -> new AttributeValue(b, false))
      .forEach(attributeValues::remove);
  }


  /**
   * Removes the supplied string as a value from this attribute.
   *
   * @param  value  to remove, null values are discarded
   */
  public void removeStringValues(final String... value)
  {
    assertMutable();
    Stream.of(value)
      .filter(Objects::nonNull)
      .map(s -> AttributeValue.fromString(s, binary))
      .forEach(attributeValues::remove);
  }


  /**
   * Removes all the strings in the supplied collection as values from this attribute.
   *
   * @param  values  to remove, null values are discarded
   */
  public void removeStringValues(final Collection<String> values)
  {
    assertMutable();
    values.stream()
      .filter(Objects::nonNull)
      .map(s -> AttributeValue.fromString(s, binary))
      .forEach(attributeValues::remove);
  }


  /**
   * Returns whether the supplied value exists in this attribute.
   *
   * @param  value  to find
   *
   * @return  whether value exists
   */
  public boolean hasValue(final byte[] value)
  {
    return attributeValues.stream().anyMatch(av -> av.equalsValue(value));
  }


  /**
   * Returns whether the supplied value exists in this attribute.
   *
   * @param  value  to find
   *
   * @return  whether value exists
   */
  public boolean hasValue(final String value)
  {
    return attributeValues.stream().anyMatch(av -> av.equalsValue(value, binary));
  }


  /**
   * Returns whether the supplied value exists in this attribute.
   *
   * @param  <T>  type attribute to encode
   * @param  func  to encode value with
   * @param  value  to find
   *
   * @return  whether value exists
   */
  public <T> boolean hasValue(final Function<T, byte[]> func, final T value)
  {
    return attributeValues.stream().anyMatch(av -> av.equalsValue(func.apply(value)));
  }


  /**
   * Returns the number of values in this ldap attribute.
   *
   * @return  number of values in this ldap attribute
   */
  public final int size()
  {
    return attributeValues.size();
  }


  /** Removes all the values in this ldap attribute. */
  public final void clear()
  {
    assertMutable();
    attributeValues.clear();
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof LdapAttribute) {
      final LdapAttribute v = (LdapAttribute) o;
      return LdapUtils.areEqual(LdapUtils.toLowerCase(attributeName), LdapUtils.toLowerCase(v.attributeName)) &&
             LdapUtils.areEqual(attributeValues, v.attributeValues);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        LdapUtils.toLowerCase(attributeName),
        attributeValues);
  }


  @Override
  public String toString()
  {
    return getClass().getName() + "@" + hashCode() + "::" +
      "name=" + attributeName + ", " +
      "values=" + getStringValues() + ", " +
      "binary=" + binary;
  }


  /**
   * Creates a mutable copy of the supplied attribute.
   *
   * @param  attr  to copy
   *
   * @return  new ldap attribute instance
   */
  public static LdapAttribute copy(final LdapAttribute attr)
  {
    final LdapAttribute ldapAttribute = new LdapAttribute();
    ldapAttribute.attributeName = attr.attributeName;
    for (AttributeValue av : attr.attributeValues) {
      ldapAttribute.attributeValues.add(AttributeValue.copy(av));
    }
    ldapAttribute.binary = attr.binary;
    return ldapAttribute;
  }


  /**
   * Returns a new attribute whose values are sorted. String values are sorted naturally. Binary values are sorted using
   * {@link Arrays#compare(byte[], byte[])}.
   *
   * @param  la  attribute to sort
   *
   * @return  new ldap attribute with sorted values
   */
  public static LdapAttribute sort(final LdapAttribute la)
  {
    final LdapAttribute sorted = new LdapAttribute(la.getName());
    if (la.isBinary()) {
      sorted.setBinary(true);
      final Set<byte[]> newValues = la.getBinaryValues().stream()
        .sorted(new ByteArrayComparator()).collect(Collectors.toCollection(LinkedHashSet::new));
      sorted.addBinaryValues(newValues);
    } else {
      final Set<String> newValues = la.getStringValues().stream()
        .sorted(new StringComparator()).collect(Collectors.toCollection(LinkedHashSet::new));
      sorted.addStringValues(newValues);
    }
    if (la.isFrozen()) {
      sorted.freeze();
    }
    return sorted;
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


  /**
   * Container for an attribute value. Supports helpers methods related to byte arrays and the notion of `binary`
   * attribute values which are base64 encoded.
   *
   * @author  Middleware Services
   */
  private static final class AttributeValue
  {

    /**
     * hash code seed.
     */
    private static final int HASH_CODE_SEED = 10723;

    /**
     * attribute value.
     */
    private final byte[] value;


    /**
     * Creates a new attribute value.
     *
     * @param bytes byte array value
     * @param copy  whether to copy the supplied bytes
     */
    AttributeValue(final byte[] bytes, final boolean copy)
    {
      if (bytes == null) {
        throw new IllegalArgumentException("Attribute value cannot be null");
      }
      value = copy ? LdapUtils.copyArray(bytes) : bytes;
    }


    @Override
    public boolean equals(final Object o)
    {
      if (o == this) {
        return true;
      }
      if (o instanceof AttributeValue) {
        final AttributeValue v = (AttributeValue) o;
        return LdapUtils.areEqual(value, v.value);
      }
      return false;
    }


    @Override
    public int hashCode()
    {
      return LdapUtils.computeHashCode(HASH_CODE_SEED, (Object) value);
    }


    /**
     * Returns the value of this attribute value.
     *
     * @param copy whether to create a copy of the underlying value
     * @return value
     */
    byte[] getValue(final boolean copy)
    {
      return copy ? LdapUtils.copyArray(value) : value;
    }


    /**
     * Returns the value of this attribute value as a string.
     *
     * @param base64 whether value should be a base64 encoded string
     * @return string value
     */
    String getStringValue(final boolean base64)
    {
      return base64 ? LdapUtils.base64Encode(value) : LdapUtils.utf8Encode(value);
    }


    /**
     * Returns whether the supplied byte array equals this attribute value.
     *
     * @param bytes to compare
     * @return whether the supplied byte array equals this attribute value
     */
    boolean equalsValue(final byte[] bytes)
    {
      return Arrays.equals(value, bytes);
    }


    /**
     * Returns whether the supplied string equals this attribute value.
     *
     * @param string to compare
     * @param base64 whether the string is base64 encoded
     * @return whether the supplied string equals this attribute value
     */
    boolean equalsValue(final String string, final boolean base64)
    {
      final byte[] bytes = base64 ? base64Decode(string, false) : LdapUtils.utf8Encode(string, false);
      return Arrays.equals(value, bytes);
    }


    /**
     * Creates a new attribute value from the supplied string.
     *
     * @param string to create attribute value from
     * @param base64 whether string should be base64 decoded
     * @return new attribute value
     */
    static AttributeValue fromString(final String string, final boolean base64)
    {
      if (base64) {
        return new AttributeValue(base64Decode(string, true), false);
      }
      return new AttributeValue(LdapUtils.utf8Encode(string, false), false);
    }


    /**
     * Creates a copy of the supplied attribute value.
     *
     * @param  value  to create copy of
     *
     * @return  new attribute value
     */
    static AttributeValue copy(final AttributeValue value)
    {
      return new AttributeValue(value.getValue(true), false);
    }


    /**
     * Base64 decodes the supplied string.
     *
     * @param string       to decode
     * @param throwOnError whether throw if string cannot be decoded
     * @return base64 decoded bytes or null if throwOnError if false and string cannot be decoded
     */
    private static byte[] base64Decode(final String string, final boolean throwOnError)
    {
      try {
        return LdapUtils.base64Decode(string);
      } catch (IllegalArgumentException e) {
        if (throwOnError) {
          throw new IllegalArgumentException("Error decoding value: " + string, e);
        }
        return null;
      }
    }
  }


  /**
   * Comparator for byte arrays. See {@link Arrays#compare(byte[], byte[])}.
   */
  private static final class ByteArrayComparator implements Comparator<byte[]>, Serializable
  {

    /**
     * serialVersionUid.
     */
    private static final long serialVersionUID = -7798424594763024564L;


    @Override
    public int compare(final byte[] o1, final byte[] o2)
    {
      return Arrays.compare(o1, o2);
    }
  }


  /**
   * Comparator for strings. See {@link String#compareTo(String)}.
   */
  private static final class StringComparator implements Comparator<String>, Serializable
  {

    /**
     * serialVersionUid.
     */
    private static final long serialVersionUID = 4891167994745573424L;


    @Override
    public int compare(final String o1, final String o2)
    {
      return o1.compareTo(o2);
    }
  }


  // CheckStyle:OFF
  public static class Builder
  {


    private final LdapAttribute object = new LdapAttribute();


    protected Builder() {}


    public Builder freeze()
    {
      object.freeze();
      return this;
    }


    public Builder name(final String name)
    {
      object.setName(name);
      return this;
    }


    @SuppressWarnings("unchecked")
    public <T> Builder values(final Function<T, byte[]> func, final T... value)
    {
      object.addValues(func, value);
      return this;
    }


    public Builder values(final byte[]... values)
    {
      object.addBinaryValues(values);
      return this;
    }


    public Builder binaryValues(final Collection<byte[]> values)
    {
      object.addBinaryValues(values);
      return this;
    }


    Builder binaryValuesInternal(final Collection<byte[]> values)
    {
      object.addBinaryValuesInternal(values);
      return this;
    }


    public Builder values(final String... values)
    {
      object.addStringValues(values);
      return this;
    }


    public Builder stringValues(final Collection<String> values)
    {
      object.addStringValues(values);
      return this;
    }


    public Builder binary(final boolean b)
    {
      object.setBinary(b);
      return this;
    }


    public LdapAttribute build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
