/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.ldaptive.LdapUtils;

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
public class Attribute
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10223;

  /** Empty byte array. */
  private static final byte[][] EMPTY_BYTE_ARRAY = new byte[0][];

  /** Empty string array. */
  private static final String[] EMPTY_STRING_ARRAY = new String[0];

  /** Attribute name. */
  private final String name;

  /** Attribute values. */
  private final byte[][] values;


  /**
   * Creates a new attribute.
   *
   * @param  type  attribute description
   */
  public Attribute(final String type)
  {
    name = type;
    values = EMPTY_BYTE_ARRAY;
  }


  /**
   * Creates a new attribute.
   *
   * @param  type  attribute description
   * @param  vals  attribute values
   */
  public Attribute(final String type, final byte[]... vals)
  {
    name = type;
    values = vals != null ? vals : EMPTY_BYTE_ARRAY;
  }


  /**
   * Creates a new attribute.
   *
   * @param  type  attribute description
   * @param  vals  attribute values
   */
  public Attribute(final String type, final String... vals)
  {
    name = type;
    values = vals != null ? stringsToBytes(vals) : EMPTY_BYTE_ARRAY;
  }


  /**
   * Converts the supplied strings into an array of byte arrays. Strings are decoded in UTF-8.
   *
   * @param  vals  to convert
   *
   * @return  array of byte arrays
   */
  private static byte[][] stringsToBytes(final String... vals)
  {
    if (vals == null) {
      return null;
    } else if (vals.length == 0) {
      return EMPTY_BYTE_ARRAY;
    }
    return Stream.of(vals).map(v -> v != null ? v.getBytes(StandardCharsets.UTF_8) : null).toArray(byte[][]::new);
  }


  /**
   * Converts the supplied array of byte arrays into an array of string. Strings are encoded in UTF-8.
   *
   * @param  encodeBinary  whether to base64 encode binary values
   * @param  vals  to convert
   *
   * @return  array of strings
   */
  private static String[] bytestoStrings(final boolean encodeBinary, final byte[]... vals)
  {
    if (vals == null) {
      return null;
    } else if (vals.length == 0) {
      return EMPTY_STRING_ARRAY;
    }
    return Stream.of(vals).map(v -> {
      if (v == null) {
        return null;
      } else if (encodeBinary && LdapUtils.shouldBase64Encode(v)) {
        return LdapUtils.base64Encode(v);
      }
      return new String(v, StandardCharsets.UTF_8);
    }).toArray(String[]::new);
  }


  public String getName()
  {
    return name;
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
      return name;
    } else {
      final int optionIndex = name.indexOf(";");
      return optionIndex > 0 ? name.substring(0, optionIndex) : name;
    }
  }


  /**
   * Returns any options that may exist on the attribute description.
   *
   * @return  attribute description options
   */
  public String[] getOptions()
  {
    String[] options = null;
    if (name.indexOf(";") > 0) {
      final String[] split = name.split(";");
      if (split.length > 1) {
        options = IntStream.range(1, split.length).mapToObj(i -> split[i]).toArray(String[]::new);
      }
    }
    return options != null ? options : EMPTY_STRING_ARRAY;
  }


  public byte[] getValue()
  {
    return values.length > 0 ? values[0] : null;
  }


  public byte[][] getValues()
  {
    return values;
  }


  public String getStringValue()
  {
    return values.length > 0 && values[0] != null ? new String(values[0], StandardCharsets.UTF_8) : null;
  }


  public String[] getStringValues()
  {
    return bytestoStrings(false, values);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof Attribute) {
      final Attribute v = (Attribute) o;
      return LdapUtils.areEqual(name, v.name) && LdapUtils.areEqual(values, v.values);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, name, values);
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("name=").append(name).append(", ")
      .append("values=").append(Arrays.toString(bytestoStrings(true, values))).toString();
  }
}
