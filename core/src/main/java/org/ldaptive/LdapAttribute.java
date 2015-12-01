/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import org.ldaptive.io.ValueTranscoder;

/**
 * Simple bean representing an ldap attribute. Contains a name and a collection of values.
 *
 * @author  Middleware Services
 */
public class LdapAttribute extends AbstractLdapBean
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 313;

  /** serial version uid. */
  private static final long serialVersionUID = -3902233717232754155L;

  /** Name for this attribute. */
  private String attributeName;

  /** Values for this attribute. */
  private final LdapAttributeValues<?> attributeValues;


  /** Default constructor. */
  public LdapAttribute()
  {
    this(SortBehavior.getDefaultSortBehavior(), false);
  }


  /**
   * Creates a new ldap attribute.
   *
   * @param  sb  sort behavior of this attribute
   */
  public LdapAttribute(final SortBehavior sb)
  {
    this(sb, false);
  }


  /**
   * Creates a new ldap attribute.
   *
   * @param  binary  whether this attribute contains binary values
   */
  public LdapAttribute(final boolean binary)
  {
    this(SortBehavior.getDefaultSortBehavior(), binary);
  }


  /**
   * Creates a new ldap attribute.
   *
   * @param  sb  sort behavior of this attribute
   * @param  binary  whether this attribute contains binary values
   */
  public LdapAttribute(final SortBehavior sb, final boolean binary)
  {
    super(sb);
    if (binary) {
      attributeValues = new LdapAttributeValues<>(byte[].class);
    } else {
      attributeValues = new LdapAttributeValues<>(String.class);
    }
  }


  /**
   * Creates a new ldap attribute.
   *
   * @param  name  of this attribute
   */
  public LdapAttribute(final String name)
  {
    this();
    setName(name);
  }


  /**
   * Creates a new ldap attribute.
   *
   * @param  name  of this attribute
   * @param  values  of this attribute
   */
  public LdapAttribute(final String name, final String... values)
  {
    this(false);
    setName(name);
    addStringValue(values);
  }


  /**
   * Creates a new ldap attribute.
   *
   * @param  name  of this attribute
   * @param  values  of this attribute
   */
  public LdapAttribute(final String name, final byte[]... values)
  {
    this(true);
    setName(name);
    addBinaryValue(values);
  }


  /**
   * Returns the name of this attribute. Includes options if they exist.
   *
   * @return  attribute name
   */
  public String getName()
  {
    return getName(true);
  }


  /**
   * Returns the name of this attribute with or without options.
   *
   * @param  withOptions  whether options should be included in the name
   *
   * @return  attribute name
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
   * Sets the name of this attribute.
   *
   * @param  name  to set
   */
  public void setName(final String name)
  {
    attributeName = name;
  }


  /**
   * Returns the options for this attribute. Returns an empty array if attribute contains no options.
   *
   * @return  options parsed from the attribute name
   */
  public String[] getOptions()
  {
    String[] options = null;
    if (attributeName.indexOf(";") > 0) {
      final String[] split = attributeName.split(";");
      if (split.length > 1) {
        options = new String[split.length - 1];
        System.arraycopy(split, 1, options, 0, options.length);
      }
    }
    return options != null ? options : new String[0];
  }


  /**
   * Returns the values of this attribute as strings. Binary data is base64 encoded. The return collection cannot be
   * modified.
   *
   * @return  collection of string attribute values
   */
  public Collection<String> getStringValues()
  {
    return attributeValues.getStringValues();
  }


  /**
   * Returns a single string value of this attribute. See {@link #getStringValues()}.
   *
   * @return  single string attribute value
   */
  public String getStringValue()
  {
    final Collection<String> values = getStringValues();
    if (values.isEmpty()) {
      return null;
    }
    return values.iterator().next();
  }


  /**
   * Returns the values of this attribute as byte arrays. String data is UTF-8 encoded. The return collection cannot be
   * modified.
   *
   * @return  collection of byte array attribute values
   */
  public Collection<byte[]> getBinaryValues()
  {
    return attributeValues.getBinaryValues();
  }


  /**
   * Returns a single byte array value of this attribute. See {@link #getBinaryValues()}.
   *
   * @return  single byte array attribute value
   */
  public byte[] getBinaryValue()
  {
    final Collection<byte[]> values = getBinaryValues();
    if (values.isEmpty()) {
      return null;
    }
    return values.iterator().next();
  }


  /**
   * Returns whether this ldap attribute contains a value of type byte[].
   *
   * @return  whether this ldap attribute contains a value of type byte[]
   */
  public boolean isBinary()
  {
    return attributeValues.isType(byte[].class);
  }


  /**
   * Returns the values of this attribute decoded by the supplied transcoder.
   *
   * @param  <T>  type of decoded attributes
   * @param  transcoder  to decode attribute values with
   *
   * @return  collection of decoded attribute values
   */
  public <T> Collection<T> getValues(final ValueTranscoder<T> transcoder)
  {
    final Collection<T> values = createSortBehaviorCollection(transcoder.getType());
    if (isBinary()) {
      for (byte[] b : getBinaryValues()) {
        values.add(transcoder.decodeBinaryValue(b));
      }
    } else {
      for (String s : getStringValues()) {
        values.add(transcoder.decodeStringValue(s));
      }
    }
    return values;
  }


  /**
   * Returns a single decoded value of this attribute. See {@link #getValues(ValueTranscoder)}.
   *
   * @param  <T>  type of decoded attributes
   * @param  transcoder  to decode attribute values with
   *
   * @return  single decoded attribute value
   */
  public <T> T getValue(final ValueTranscoder<T> transcoder)
  {
    final Collection<T> t = getValues(transcoder);
    if (t.isEmpty()) {
      return null;
    }
    return t.iterator().next();
  }


  /**
   * Adds the supplied string as a value for this attribute.
   *
   * @param  value  to add
   *
   * @throws  NullPointerException  if value is null
   */
  public void addStringValue(final String... value)
  {
    for (String s : value) {
      attributeValues.add(s);
    }
  }


  /**
   * Adds all the strings in the supplied collection as values for this attribute. See {@link
   * #addStringValue(String...)}.
   *
   * @param  values  to add
   */
  public void addStringValues(final Collection<String> values)
  {
    for (String value : values) {
      addStringValue(value);
    }
  }


  /**
   * Adds the supplied byte array as a value for this attribute.
   *
   * @param  value  to add
   *
   * @throws  NullPointerException  if value is null
   */
  public void addBinaryValue(final byte[]... value)
  {
    for (byte[] b : value) {
      attributeValues.add(b);
    }
  }


  /**
   * Adds all the byte arrays in the supplied collection as values for this attribute. See {@link
   * #addBinaryValue(byte[][])}.
   *
   * @param  values  to add
   */
  public void addBinaryValues(final Collection<byte[]> values)
  {
    for (byte[] value : values) {
      addBinaryValue(value);
    }
  }


  /**
   * Adds the supplied values for this attribute by encoding them with the supplied transcoder.
   *
   * @param  <T>  type attribute to encode
   * @param  transcoder  to encode value with
   * @param  value  to encode and add
   *
   * @throws  NullPointerException  if value is null
   */
  @SuppressWarnings("unchecked")
  public <T> void addValue(final ValueTranscoder<T> transcoder, final T... value)
  {
    for (T t : value) {
      if (isBinary()) {
        attributeValues.add(transcoder.encodeBinaryValue(t));
      } else {
        attributeValues.add(transcoder.encodeStringValue(t));
      }
    }
  }


  /**
   * Adds all the values in the supplied collection for this attribute by encoding them with the supplied transcoder.
   * See {@link #addValue(ValueTranscoder, Object...)}.
   *
   * @param  <T>  type attribute to encode
   * @param  transcoder  to encode value with
   * @param  values  to encode and add
   */
  @SuppressWarnings("unchecked")
  public <T> void addValues(final ValueTranscoder<T> transcoder, final Collection<T> values)
  {
    for (T value : values) {
      addValue(transcoder, value);
    }
  }


  /**
   * Removes the supplied value from the attribute values if it exists.
   *
   * @param  value  to remove
   */
  public void removeStringValue(final String... value)
  {
    for (String s : value) {
      attributeValues.remove(s);
    }
  }


  /**
   * Removes the supplied values from the attribute values if they exists. See {@link #removeStringValue(String...)}.
   *
   * @param  values  to remove
   */
  public void removeStringValues(final Collection<String> values)
  {
    for (String value : values) {
      removeStringValue(value);
    }
  }


  /**
   * Removes the supplied value from the attribute values if it exists.
   *
   * @param  value  to remove
   */
  public void removeBinaryValue(final byte[]... value)
  {
    for (byte[] b : value) {
      attributeValues.remove(b);
    }
  }


  /**
   * Removes the supplied values from the attribute values if they exists. See {@link #removeBinaryValue(byte[][])}.
   *
   * @param  values  to remove
   */
  public void removeBinaryValues(final Collection<byte[]> values)
  {
    for (byte[] value : values) {
      removeBinaryValue(value);
    }
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
    return String.format("[%s%s]", attributeName, attributeValues);
  }


  /**
   * Returns an implementation of collection for the sort behavior of this bean. This implementation returns HashSet for
   * {@link SortBehavior#UNORDERED}, LinkedHashSet for {@link SortBehavior#ORDERED}, and TreeSet for {@link
   * SortBehavior#SORTED}.
   *
   * @param  <E>  contained in the collection
   * @param  c  type contained in the collection
   *
   * @return  collection corresponding to the sort behavior
   */
  protected <E> Collection<E> createSortBehaviorCollection(final Class<E> c)
  {
    Collection<E> values = null;
    if (SortBehavior.UNORDERED == getSortBehavior()) {
      values = new HashSet<>();
    } else if (SortBehavior.ORDERED == getSortBehavior()) {
      values = new LinkedHashSet<>();
    } else if (SortBehavior.SORTED == getSortBehavior()) {
      if (!c.isAssignableFrom(Comparable.class)) {
        values = new TreeSet<>(getComparator(c));
      } else {
        values = new TreeSet<>();
      }
    }
    return values;
  }


  /**
   * Returns a comparator for the supplied class type. Should not be invoked for classes that have a natural ordering.
   * Returns a comparator that uses {@link Object#toString()} for unknown types.
   *
   * @param  <E>  type of class
   * @param  c  type to compare
   *
   * @return  comparator for use with the supplied type
   */
  private static <E> Comparator<E> getComparator(final Class<E> c)
  {
    if (c.isAssignableFrom(byte[].class)) {
      return
        new Comparator<E>() {
        @Override
        public int compare(final E o1, final E o2)
        {
          final ByteBuffer bb1 = ByteBuffer.wrap((byte[]) o1);
          final ByteBuffer bb2 = ByteBuffer.wrap((byte[]) o2);
          return bb1.compareTo(bb2);
        }
      };
    } else {
      return
        new Comparator<E>() {
        @Override
        public int compare(final E o1, final E o2)
        {
          return o1.toString().compareTo(o2.toString());
        }
      };
    }
  }


  /**
   * Creates a new ldap attribute. The collection of values is inspected for either String or byte[] and the appropriate
   * attribute is created.
   *
   * @param  sb  sort behavior
   * @param  name  of this attribute
   * @param  values  of this attribute
   *
   * @return  ldap attribute
   *
   * @throws  IllegalArgumentException  if values contains something other than String or byte[]
   */
  public static LdapAttribute createLdapAttribute(
    final SortBehavior sb,
    final String name,
    final Collection<Object> values)
  {
    final Collection<String> stringValues = new ArrayList<>();
    final Collection<byte[]> binaryValues = new ArrayList<>();
    for (Object value : values) {
      if (value instanceof byte[]) {
        binaryValues.add((byte[]) value);
      } else if (value instanceof String) {
        stringValues.add((String) value);
      } else {
        throw new IllegalArgumentException("Values must contain either String or byte[]");
      }
    }

    LdapAttribute la;
    if (!binaryValues.isEmpty()) {
      la = new LdapAttribute(sb, true);
      la.setName(name);
      la.addBinaryValues(binaryValues);
    } else {
      la = new LdapAttribute(sb, false);
      la.setName(name);
      la.addStringValues(stringValues);
    }
    return la;
  }


  /**
   * Escapes the supplied string value per RFC 4514 section 2.4.
   *
   * @param  value  to escape
   *
   * @return  escaped value
   */
  public static String escapeValue(final String value)
  {
    final int len = value.length();
    final StringBuilder sb = new StringBuilder(len);
    char ch;
    for (int i = 0; i < len; i++) {
      ch = value.charAt(i);
      switch (ch) {

      case '"':
      case '#':
      case '+':
      case ',':
      case ';':
      case '<':
      case '=':
      case '>':
      case '\\':
        sb.append('\\').append(ch);
        break;

      case ' ':
        // escape first space and last space
        if (i == 0 || i + 1 == len) {
          sb.append('\\').append(ch);
        } else {
          sb.append(ch);
        }
        break;

      case 0:
        // escape null
        sb.append("\\00");
        break;

      default:
        // escape non-printable ASCII characters
        // CheckStyle:MagicNumber OFF
        if (ch < ' ' || ch == 127) {
          sb.append(LdapUtils.hexEncode(ch));
        } else {
          sb.append(ch);
        }
        // CheckStyle:MagicNumber ON
        break;
      }
    }
    return sb.toString();
  }


  /**
   * Simple bean for ldap attribute values.
   *
   * @param  <T>  type of values
   *
   * @author  Middleware Services
   */
  private class LdapAttributeValues<T> implements Serializable
  {

    /** hash code seed. */
    private static final int HASH_CODE_SEED = 317;

    /** serial version uid. */
    private static final long serialVersionUID = 8075255677989836494L;

    /** Type of values. */
    private final Class<T> type;

    /** Collection of values. */
    private final Collection<T> values;


    /**
     * Creates a new ldap attribute values.
     *
     * @param  t  type of values
     *
     * @throws  IllegalArgumentException  if t is not a String or byte[]
     */
    LdapAttributeValues(final Class<T> t)
    {
      if (!(t.isAssignableFrom(String.class) || t.isAssignableFrom(byte[].class))) {
        throw new IllegalArgumentException("Only String and byte[] values are supported");
      }
      type = t;
      values = createSortBehaviorCollection(type);
    }


    /**
     * Returns whether this ldap attribute values is of the supplied type.
     *
     * @param  c  type to check
     *
     * @return  whether this ldap attribute values is of the supplied type
     */
    public boolean isType(final Class<?> c)
    {
      return type.isAssignableFrom(c);
    }


    /**
     * Returns the values in string format. If the type of this values is String, values are returned as is. If the type
     * of this values is byte[], values are base64 encoded. See {@link #convertValuesToString(Collection)}.
     *
     * @return  unmodifiable collection
     */
    @SuppressWarnings("unchecked")
    public Collection<String> getStringValues()
    {
      if (isType(String.class)) {
        return Collections.unmodifiableCollection((Collection<String>) values);
      }
      return Collections.unmodifiableCollection(convertValuesToString((Collection<byte[]>) values));
    }


    /**
     * Returns the values in binary format. If the type of this values is byte[], values are returned as is. If the type
     * of this values is String, values are UTF-8 encoded. See {@link #convertValuesToByteArray(Collection)}.
     *
     * @return  unmodifiable collection
     */
    @SuppressWarnings("unchecked")
    public Collection<byte[]> getBinaryValues()
    {
      if (isType(byte[].class)) {
        return Collections.unmodifiableCollection((Collection<byte[]>) values);
      }
      return Collections.unmodifiableCollection(convertValuesToByteArray((Collection<String>) values));
    }


    /**
     * Adds the supplied object to this values.
     *
     * @param  o  to add
     *
     * @throws  IllegalArgumentException  if o is null or if o is not the correct type
     */
    public void add(final Object o)
    {
      checkValue(o);
      values.add(type.cast(o));
    }


    /**
     * Removes the supplied object from this values if it exists.
     *
     * @param  o  to remove
     *
     * @throws  IllegalArgumentException  if o is null or if o is not the correct type
     */
    public void remove(final Object o)
    {
      checkValue(o);
      values.remove(type.cast(o));
    }


    /**
     * Determines if the supplied object is acceptable to use in this values.
     *
     * @param  o  object to check
     *
     * @throws  IllegalArgumentException  if o is null or if o is not the correct type
     */
    private void checkValue(final Object o)
    {
      if (o == null) {
        throw new IllegalArgumentException("Value cannot be null");
      }
      if (!isType(o.getClass())) {
        throw new IllegalArgumentException(
          String.format(
            "Attribute %s does not support values of type %s",
            attributeName,
            o.getClass().isArray() ? o.getClass().getComponentType() : o.getClass().getName()));
      }
    }


    /**
     * Returns the number of values.
     *
     * @return  number of values
     */
    public int size()
    {
      return values.size();
    }


    /** Removes all the values. */
    public void clear()
    {
      values.clear();
    }


    @Override
    public int hashCode()
    {
      return LdapUtils.computeHashCode(HASH_CODE_SEED, values);
    }


    @Override
    public String toString()
    {
      return getStringValues().toString();
    }


    /**
     * Base64 encodes the supplied collection of values.
     *
     * @param  v  values to encode
     *
     * @return  collection of string values
     */
    protected Collection<String> convertValuesToString(final Collection<byte[]> v)
    {
      final Collection<String> c = createSortBehaviorCollection(String.class);
      for (byte[] value : v) {
        c.add(LdapUtils.base64Encode(value));
      }
      return c;
    }


    /**
     * UTF-8 encodes the supplied collection of values.
     *
     * @param  v  values to encode
     *
     * @return  collection of byte array values
     */
    protected Collection<byte[]> convertValuesToByteArray(final Collection<String> v)
    {
      final Collection<byte[]> c = createSortBehaviorCollection(byte[].class);
      for (String value : v) {
        c.add(LdapUtils.utf8Encode(value));
      }
      return c;
    }
  }
}
