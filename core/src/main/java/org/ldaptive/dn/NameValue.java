/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

import java.nio.ByteBuffer;
import java.util.function.Function;
import org.ldaptive.LdapUtils;

/**
 * Container for a RDN name value pair.
 *
 * @author  Middleware Services
 */
public class NameValue
{
  /** hash code seed. */
  private static final int HASH_CODE_SEED = 5011;

  /** Attribute name. */
  private final String attributeName;

  /** Attribute value. */
  private final ByteBuffer attributeValue;


  /**
   * Creates a new name value.
   *
   * @param  name  of the attribute
   * @param  value  of the attribute
   */
  public NameValue(final String name, final String value)
  {
    this(name, LdapUtils.utf8Encode(value));
  }


  /**
   * Creates a new name value.
   *
   * @param  name  of the attribute
   * @param  value  of the attribute
   */
  public NameValue(final String name, final byte[] value)
  {
    attributeName = name;
    attributeValue = value != null ? ByteBuffer.wrap(value) : null;
  }


  /**
   * Returns the attribute name.
   *
   * @return  attribute name
   */
  public String getName()
  {
    return attributeName;
  }


  public byte[] getBinaryValue()
  {
    return attributeValue != null ? attributeValue.array() : null;
  }


  public String getStringValue()
  {
    return attributeValue != null ? LdapUtils.utf8Encode(attributeValue.array()) : null;
  }


  public <T> T getValue(final Function<byte[], T> func)
  {
    return attributeValue != null ? func.apply(attributeValue.array()) : null;
  }


  /**
   * Returns whether the attribute name matches the supplied name.
   *
   * @param  name  to match
   *
   * @return  whether name matches the attribute name
   */
  public boolean hasName(final String name)
  {
    return attributeName.equalsIgnoreCase(name);
  }


  /**
   * Returns a string representation of this name value, of the form 'name=value'.
   *
   * @return  string form of the name value pair
   */
  public String format()
  {
    final StringBuilder sb = new StringBuilder();
    sb.append(attributeName)
      .append("=").append(LdapUtils.utf8Encode(attributeValue != null ? attributeValue.array() : null));
    return sb.toString();
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof NameValue) {
      final NameValue v = (NameValue) o;
      return LdapUtils.areEqual(
        attributeName != null ? attributeName.toLowerCase() : null,
        v.attributeName != null ? v.attributeName.toLowerCase() : null) &&
        LdapUtils.areEqual(attributeValue, v.attributeValue);
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
        attributeValue);
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("name=").append(attributeName).append(", ")
      .append("value=").append(getStringValue()).toString();
  }
}
