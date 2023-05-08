/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.nio.ByteBuffer;
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
public class LdapAttribute
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10223;

  /** List of attribute names known to use binary syntax. */
  private static final String[] DEFAULT_BINARY_ATTRIBUTES = new String[] {
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

  /** Attribute name. */
  private String attributeName;

  /** Attribute values. */
  private Set<ByteBuffer> attributeValues = new LinkedHashSet<>();

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
  public void setName(final String type)
  {
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
  public boolean isBinary()
  {
    return binary;
  }


  /**
   * Sets whether this ldap attribute is binary.
   *
   * @param  b  whether this ldap attribute is binary
   */
  public void setBinary(final boolean b)
  {
    binary = b;
  }


  /**
   * Checks whether attrNames matches the name of this attribute.  If a match is found this attribute is set as binary.
   *
   * @param  attrNames  custom binary attribute names
   */
  public void configureBinary(final String... attrNames)
  {
    if (binary) {
      return;
    }
    if (attrNames != null && attrNames.length > 0) {
      for (String s : attrNames) {
        if (attributeName.equals(s)) {
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
  public String getName()
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
  public String getName(final boolean withOptions)
  {
    if (withOptions) {
      return attributeName;
    } else {
      final int optionIndex = attributeName.indexOf(";");
      return optionIndex > 0 ? attributeName.substring(0, optionIndex) : attributeName;
    }
  }


  /**
   * Returns any options that may exist on the attribute description.
   *
   * @return  attribute description options
   */
  public List<String> getOptions()
  {
    if (attributeName.indexOf(";") > 0) {
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
    return attributeValues.isEmpty() ? null : attributeValues.iterator().next().array();
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
    return attributeValues.stream().map(ByteBuffer::array).collect(Collectors.toUnmodifiableList());
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
    final ByteBuffer val = attributeValues.iterator().next();
    return binary ? LdapUtils.base64Encode(val.array()) : LdapUtils.utf8Encode(val.array());
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
    return attributeValues.stream().map(v -> {
      if (binary) {
        return LdapUtils.base64Encode(v.array());
      }
      return LdapUtils.utf8Encode(v.array(), false);
    }).collect(Collectors.toUnmodifiableList());
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
    return attributeValues.isEmpty() ? null : func.apply(attributeValues.iterator().next().array());
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
    return attributeValues.stream()
      .filter(Objects::nonNull)
      .map(ByteBuffer::array)
      .map(func).collect(Collectors.toUnmodifiableList());
  }


  /**
   * Adds the supplied byte array as a value for this attribute.
   *
   * @param  value  to add, null values are discarded
   */
  public void addBinaryValues(final byte[]... value)
  {
    Stream.of(value).filter(Objects::nonNull).map(ByteBuffer::wrap).forEach(attributeValues::add);
  }


  /**
   * Adds all the byte arrays in the supplied collection as values for this attribute.
   *
   * @param  values  to add, null values are discarded
   */
  public void addBinaryValues(final Collection<byte[]> values)
  {
    values.stream().filter(Objects::nonNull).map(ByteBuffer::wrap).forEach(attributeValues::add);
  }


  /**
   * Adds the supplied string as a value for this attribute.
   *
   * @param  value  to add, null values are discarded
   */
  public void addStringValues(final String... value)
  {
    Stream.of(value)
      .filter(Objects::nonNull)
      .map(s -> toByteArray(s, true))
      .filter(Objects::nonNull)
      .map(ByteBuffer::wrap)
      .forEach(attributeValues::add);
  }


  /**
   * Adds all the strings in the supplied collection as values for this attribute.
   *
   * @param  values  to add, null values are discarded
   */
  public void addStringValues(final Collection<String> values)
  {
    values.stream()
      .filter(Objects::nonNull)
      .map(s -> toByteArray(s, true))
      .filter(Objects::nonNull)
      .map(ByteBuffer::wrap)
      .forEach(attributeValues::add);
  }


  /**
   * Adds all the buffers in the supplied collection as values for this attribute.
   *
   * @param  values  to add, null values are discarded
   */
  public void addBufferValues(final ByteBuffer... values)
  {
    Stream.of(values).filter(Objects::nonNull).forEach(attributeValues::add);
  }


  /**
   * Adds all the buffers in the supplied collection as values for this attribute.
   *
   * @param  values  to add, null values are discarded
   */
  public void addBufferValues(final Collection<ByteBuffer> values)
  {
    values.stream().filter(Objects::nonNull).forEach(attributeValues::add);
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
    Stream.of(value)
      .filter(Objects::nonNull)
      .map(func)
      .filter(Objects::nonNull)
      .map(ByteBuffer::wrap)
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
    values.stream()
      .filter(Objects::nonNull)
      .map(func)
      .filter(Objects::nonNull)
      .map(ByteBuffer::wrap)
      .forEach(attributeValues::add);
  }


  /**
   * Removes the supplied byte array as a value from this attribute.
   *
   * @param  value  to remove, null values are discarded
   */
  public void removeBinaryValues(final byte[]... value)
  {
    Stream.of(value).filter(Objects::nonNull).map(ByteBuffer::wrap).forEach(attributeValues::remove);
  }


  /**
   * Removes all the byte arrays in the supplied collection as values from this attribute.
   *
   * @param  values  to remove, null values are discarded
   */
  public void removeBinaryValues(final Collection<byte[]> values)
  {
    values.stream().filter(Objects::nonNull).map(ByteBuffer::wrap).forEach(attributeValues::remove);
  }


  /**
   * Removes the supplied string as a value from this attribute.
   *
   * @param  value  to remove, null values are discarded
   */
  public void removeStringValues(final String... value)
  {
    Stream.of(value)
      .filter(Objects::nonNull)
      .map(s -> toByteArray(s, true))
      .filter(Objects::nonNull)
      .map(ByteBuffer::wrap)
      .forEach(attributeValues::remove);
  }


  /**
   * Removes all the strings in the supplied collection as values from this attribute.
   *
   * @param  values  to remove, null values are discarded
   */
  public void removeStringValues(final Collection<String> values)
  {
    values.stream()
      .filter(Objects::nonNull)
      .map(s -> toByteArray(s, true))
      .filter(Objects::nonNull)
      .map(ByteBuffer::wrap)
      .forEach(attributeValues::remove);
  }


  /**
   * Removes all the buffers in the supplied collection as values from this attribute.
   *
   * @param  values  to remove, null values are discarded
   */
  public void removeBufferValues(final ByteBuffer... values)
  {
    Stream.of(values).filter(Objects::nonNull).forEach(attributeValues::remove);
  }


  /**
   * Removes all the buffers in the supplied collection as values from this attribute.
   *
   * @param  values  to remove, null values are discarded
   */
  public void removeBufferValues(final Collection<ByteBuffer> values)
  {
    values.stream().filter(Objects::nonNull).forEach(attributeValues::remove);
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
    return attributeValues.stream().anyMatch(bb -> Arrays.equals(bb.array(), value));
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
    return attributeValues.stream().anyMatch(bb -> Arrays.equals(bb.array(), toByteArray(value, false)));
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
    return attributeValues.stream().anyMatch(bb -> Arrays.equals(bb.array(), func.apply(value)));
  }


  /**
   * Returns the number of values in this ldap attribute.
   *
   * @return  number of values in this ldap attribute
   */
  public int size()
  {
    return attributeValues.size();
  }


  /** Removes all the values in this ldap attribute. */
  public void clear()
  {
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
      return LdapUtils.areEqual(
               attributeName != null ? attributeName.toLowerCase() : null,
               v.attributeName != null ? v.attributeName.toLowerCase() : null) &&
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
        attributeName != null ? attributeName.toLowerCase() : null,
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
   * Returns a new attribute whose values are sorted. String values are sorted naturally. Binary values are sorted using
   * {@link ByteBuffer#compareTo(ByteBuffer)}.
   *
   * @param  la  attribute to sort
   *
   * @return  sorted attribute
   */
  public static LdapAttribute sort(final LdapAttribute la)
  {
    final LdapAttribute sorted = new LdapAttribute(la.getName());
    if (la.isBinary()) {
      sorted.setBinary(true);
      final Set<byte[]> newValues = la.getBinaryValues().stream().sorted(
        (o1, o2) -> {
          final ByteBuffer bb1 = ByteBuffer.wrap(o1);
          final ByteBuffer bb2 = ByteBuffer.wrap(o2);
          return bb1.compareTo(bb2);
        }).collect(Collectors.toCollection(LinkedHashSet::new));
      sorted.addBinaryValues(newValues);
    } else {
      final Set<String> newValues = la.getStringValues().stream()
        .sorted(Comparator.comparing(String::toString)).collect(Collectors.toCollection(LinkedHashSet::new));
      sorted.addStringValues(newValues);
    }
    return sorted;
  }


  /**
   * Converts the supplied string value to a byte array respecting the {@link #binary} flag. If this attribute is
   * binary, value is expected to be in base64 format. Otherwise, value is UTF-8 encoded.
   *
   * @param  value  to convert
   * @param  throwOnError  whether to throw if a base64 decode error occurs
   *
   * @return  binary value
   *
   * @throws  IllegalArgumentException  if attribute is binary, value cannot be base64 decoded and throwOnError is true
   */
  private byte[] toByteArray(final String value, final boolean throwOnError)
  {
    if (binary) {
      try {
        return LdapUtils.base64Decode(value);
      } catch (IllegalArgumentException e) {
        if (throwOnError) {
          throw new IllegalArgumentException("Error decoding " + value + " for " + attributeName, e);
        }
        return null;
      }
    }
    return LdapUtils.utf8Encode(value, false);
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
  public static class Builder
  {


    private final LdapAttribute object = new LdapAttribute();


    protected Builder() {}


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


    public Builder values(final ByteBuffer... values)
    {
      object.addBufferValues(values);
      return this;
    }


    public Builder bufferValues(final Collection<ByteBuffer> values)
    {
      object.addBufferValues(values);
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
