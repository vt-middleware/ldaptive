/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

import java.util.function.Function;
import org.ldaptive.LdapUtils;

/**
 * Container for a RDN name value pair.
 *
 * @author  Middleware Services
 */
public final class NameValue
{
  /** hash code seed. */
  private static final int HASH_CODE_SEED = 5011;

  /** Attribute name. */
  private final String attributeName;

  /** Attribute value. */
  private final byte[] attributeValue;


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
    attributeName = LdapUtils.assertNotNullArg(name, "Name cannot be null");
    attributeValue = value;
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
    return attributeValue;
  }


  public String getStringValue()
  {
    return attributeValue != null ? LdapUtils.utf8Encode(attributeValue) : null;
  }


  public <T> T getValue(final Function<byte[], T> func)
  {
    return func.apply(getBinaryValue());
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
    return attributeName + "=" + LdapUtils.utf8Encode(attributeValue);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof NameValue) {
      final NameValue v = (NameValue) o;
      return LdapUtils.areEqual(LdapUtils.toLowerCase(attributeName), LdapUtils.toLowerCase(v.attributeName)) &&
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
        LdapUtils.toLowerCase(attributeName),
        attributeValue);
  }


  @Override
  public String toString()
  {
    return getClass().getName() +
      "@" + hashCode() + "::" +
      "name=" + attributeName + ", " +
      "value=" + getStringValue();
  }
}
