/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ldaptive.io.Hex;

/**
 * Provides utility methods for this package.
 *
 * @author  Middleware Services
 */
public final class LdapUtils
{

  /** Size of buffer in bytes to use when reading files. */
  private static final int READ_BUFFER_SIZE = 128;

  /** Prime number to assist in calculating hash codes. */
  private static final int HASH_CODE_PRIME = 113;

  /** Pattern to match ipv4 addresses. */
  private static final Pattern IPV4_PATTERN = Pattern.compile(
    "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)" +
    "(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

  /** Pattern to match ipv6 addresses. */
  private static final Pattern IPV6_STD_PATTERN = Pattern.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");

  /** Pattern to match ipv6 hex compressed addresses. */
  private static final Pattern IPV6_HEX_COMPRESSED_PATTERN = Pattern.compile(
    "^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::" +
    "((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$");

  /** Pattern that matches control characters. */
  private static final Pattern CNTRL_PATTERN = Pattern.compile("\\p{Cntrl}");


  /** Default constructor. */
  private LdapUtils() {}


  /**
   * This will convert the supplied value to a base64 encoded string. Returns null if the supplied byte array is null.
   *
   * @param  value  to base64 encode
   *
   * @return  base64 encoded value
   */
  public static String base64Encode(final byte... value)
  {
    return value != null ? new String(Base64.getEncoder().encode(value), StandardCharsets.UTF_8) : null;
  }


  /**
   * This will convert the supplied value to a base64 encoded string. Returns null if the supplied string is null.
   *
   * @param  value  to base64 encode
   *
   * @return  base64 encoded value
   */
  public static String base64Encode(final String value)
  {
    return value != null ? base64Encode(value.getBytes(StandardCharsets.UTF_8)) : null;
  }


  /**
   * This will convert the supplied value to a UTF-8 encoded string. Returns null if the supplied byte array is null.
   *
   * @param  value  to UTF-8 encode
   *
   * @return  UTF-8 encoded value
   */
  public static String utf8Encode(final byte[] value)
  {
    return utf8Encode(value, true);
  }


  /**
   * This will convert the supplied value to a UTF-8 encoded string.
   *
   * @param  value  to UTF-8 encode
   * @param  allowNull  whether to throw {@link NullPointerException} if value is null
   *
   * @return  UTF-8 encoded value
   *
   * @throws  NullPointerException  if allowNull is false and value is null
   */
  public static String utf8Encode(final byte[] value, final boolean allowNull)
  {
    if (!allowNull && value == null) {
      throw new NullPointerException("Cannot UTF-8 encode null value");
    }
    return value != null ? new String(value, StandardCharsets.UTF_8) : null;
  }


  /**
   * This will convert the supplied value to a UTF-8 encoded byte array. Returns null if the supplied string is null.
   *
   * @param  value  to UTF-8 encode
   *
   * @return  UTF-8 encoded value
   */
  public static byte[] utf8Encode(final String value)
  {
    return utf8Encode(value, true);
  }


  /**
   * This will convert the supplied value to a UTF-8 encoded byte array.
   *
   * @param  value  to UTF-8 encode
   * @param  allowNull  whether to throw {@link NullPointerException} if value is null
   *
   * @return  UTF-8 encoded value
   *
   * @throws  NullPointerException  if allowNull is false and value is null
   */
  public static byte[] utf8Encode(final String value, final boolean allowNull)
  {
    if (!allowNull && value == null) {
      throw new NullPointerException("Cannot UTF-8 encode null value");
    }
    return value != null ? value.getBytes(StandardCharsets.UTF_8) : null;
  }


  /**
   * This will convert the supplied value to a hex encoded string. Returns null if the supplied byte array is null.
   *
   * @param  value  to hex encode
   *
   * @return  hex encoded value
   */
  public static char[] hexEncode(final byte... value)
  {
    return value != null ? Hex.encode(value) : null;
  }


  /**
   * This will convert the supplied value to a hex encoded string. Returns null if the supplied char array is null.
   *
   * @param  value  to hex encode
   *
   * @return  hex encoded value
   */
  public static char[] hexEncode(final char... value)
  {
    return value != null ? hexEncode(utf8Encode(String.valueOf(value))) : null;
  }


  /**
   * Implementation of percent encoding as described in RFC 3986 section 2.1.
   *
   * @param  value  to encode
   *
   * @return  percent encoded value
   */
  public static String percentEncode(final String value)
  {
    if (value == null) {
      return null;
    }

    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      final char ch = value.charAt(i);
      // uppercase
      if (ch >= 'A' && ch <= 'Z') {
        sb.append(ch);
      // lowercase
      } else if (ch >= 'a' && ch <= 'z') {
        sb.append(ch);
      // digit
      } else if (ch >= '0' && ch <= '9') {
        sb.append(ch);
      } else {
        // unreserved and reserved
        switch (ch) {

        case '-':
        case '.':
        case '_':
        case '~':
        case '!':
        case '$':
        case '&':
        case '\'':
        case '(':
        case ')':
        case '*':
        case '+':
        case ',':
        case ';':
        case '=':
          sb.append(ch);
          break;

        default:
          sb.append("%");
          // CheckStyle:MagicNumber OFF
          if (ch <= 0x7F) {
            sb.append(hexEncode((byte) (ch & 0x7F)));
          } else {
            sb.append(hexEncode(utf8Encode(String.valueOf(ch))));
          }
          // CheckStyle:MagicNumber ON
        }
      }
    }
    return sb.toString();
  }


  /**
   * Converts all characters &lt;= 0x1F and 0x7F to percent encoded hex.
   *
   * @param  value  to encode control characters in
   *
   * @return  string with percent encoded hex characters
   */
  public static String percentEncodeControlChars(final String value)
  {
    if (value != null) {
      final Matcher m = CNTRL_PATTERN.matcher(value);
      if (m.find()) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
          final char ch = value.charAt(i);
          // CheckStyle:MagicNumber OFF
          if (ch <= 0x1F || ch == 0x7F) {
            sb.append("%");
            sb.append(hexEncode((byte) (ch & 0x7F)));
          } else {
            sb.append(ch);
          }
          // CheckStyle:MagicNumber ON
        }
        return sb.toString();
      }
    }
    return value;
  }


  /**
   * Removes the space character from both the beginning and end of the supplied value.
   *
   * @param  value  to trim space character from
   *
   * @return  trimmed value or same value if no trim was performed
   */
  public static String trimSpace(final String value)
  {
    if (value == null || value.isEmpty()) {
      return value;
    }

    int startIndex = 0;
    int endIndex = value.length();
    while (startIndex < endIndex && value.charAt(startIndex) == ' ') {
      startIndex++;
    }
    while (startIndex < endIndex && value.charAt(endIndex - 1) == ' ') {
      endIndex--;
    }
    if (startIndex == 0 && endIndex == value.length()) {
      return value;
    }
    return value.substring(startIndex, endIndex);
  }


  /**
   * Changes the supplied value by replacing multiple spaces with a single space.
   *
   * @param  value  to compress spaces
   * @param  trim  whether to remove any leading or trailing space characters
   *
   * @return  normalized value or value if no compress was performed
   */
  public static String compressSpace(final String value, final boolean trim)
  {
    if (value == null || value.isEmpty()) {
      return value;
    }

    final StringBuilder sb = new StringBuilder();
    boolean foundSpace = false;
    for (int i = 0; i < value.length(); i++) {
      final char ch = value.charAt(i);
      if (ch == ' ') {
        if (i == value.length() - 1) {
          // last char is a space
          sb.append(ch);
        }
        foundSpace = true;
      } else {
        if (foundSpace) {
          sb.append(' ');
        }
        sb.append(ch);
        foundSpace = false;
      }
    }

    if (sb.length() == 0 && foundSpace) {
      return trim ? "" : " ";
    }
    if (trim) {
      if (sb.length() > 0 && sb.charAt(0) == ' ') {
        sb.deleteCharAt(0);
      }
      if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ' ') {
        sb.deleteCharAt(sb.length() - 1);
      }
    }
    return sb.toString();
  }


  /**
   * This will decode the supplied value as a base64 encoded string to a byte[]. Returns null if the supplied string is
   * null.
   *
   * @param  value  to base64 decode
   *
   * @return  base64 decoded value
   */
  public static byte[] base64Decode(final String value)
  {
    try {
      return value != null ? Base64.getDecoder().decode(value.getBytes(StandardCharsets.UTF_8)) : null;
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Error decoding value: " + value, e);
    }
  }


  /**
   * This will decode the supplied value as a hex encoded string to a byte[]. Returns null if the supplied character
   * array is null.
   *
   * @param  value  to hex decode
   *
   * @return  hex decoded value
   */
  public static byte[] hexDecode(final char[] value)
  {
    return value != null ? Hex.decode(value) : null;
  }


  /**
   * Implementation of percent decoding as described in RFC 3986 section 2.1.
   *
   * @param  value  to decode
   *
   * @return  percent decoded value
   */
  public static String percentDecode(final String value)
  {
    if (value == null || !value.contains("%")) {
      return value;
    }

    final StringBuilder sb = new StringBuilder();
    int pos = 0;
    while (pos < value.length()) {
      final char c = value.charAt(pos++);
      if (c == '%') {
        final char[] hex = new char[] {
          value.charAt(pos++),
          value.charAt(pos++),
        };
        sb.append(utf8Encode(hexDecode(hex)));
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }


  /**
   * See {@link #shouldBase64Encode(byte[])}.
   *
   * @param  value  to inspect
   *
   * @return  whether the value should be base64 encoded
   */
  public static boolean shouldBase64Encode(final String value)
  {
    return shouldBase64Encode(value.getBytes(StandardCharsets.UTF_8));
  }


  /**
   * Determines whether the supplied value should be base64 encoded. See http://www.faqs.org/rfcs/rfc2849.html for more
   * details.
   *
   * @param  value  to inspect
   *
   * @return  whether the value should be base64 encoded
   */
  public static boolean shouldBase64Encode(final byte[] value)
  {
    if (value == null || value.length == 0) {
      return false;
    }

    boolean encode = false;

    // CheckStyle:MagicNumber OFF
    // check first byte in value
    switch (value[0] & 0xFF) {
    // check for SP
    case 0x20:
    // check for colon(:)
    case 0x3A:
    // check for left arrow(<)
    case 0x3C:
      encode = true;
      break;
    default:
      break;
    }

    if (!encode) {
      // check for SP at last byte in value
      if ((value[value.length - 1] & 0xFF) == 0x20) {
        encode = true;
      } else {
        // check remaining bytes in the value
        for (final byte b : value) {
          switch (b & 0xFF) {
          // check for NUL
          case 0x00:
            // check for LF
          case 0x0A:
            // check for CR
          case 0x0D:
            encode = true;
            break;

          default:
            // check for any character above 127
            if ((b & 0x80) != 0x00) {
              encode = true;
            }
            break;
          }
          if (encode) {
            break;
          }
        }
      }
    }
    // CheckStyle:MagicNumber ON

    return encode;
  }


  /**
   * Converts the supplied string to lower case. If the string contains non-ascii characters, {@link Locale#ROOT} is
   * used.
   *
   * @param  s  to lower case
   *
   * @return  new lower case string
   */
  public static String toLowerCase(final String s)
  {
    if (s == null || s.isEmpty()) {
      return s;
    }
    // CheckStyle:MagicNumber OFF
    // if string contains non-ascii, use locale specific lowercase
    if (s.chars().anyMatch(c -> c > 0x7F)) {
      return s.toLowerCase(Locale.ROOT);
    }
    return toLowerCaseAscii(s);
  }


  /**
   * Converts the supplied string to lower case. Uses {@link Character#isUpperCase(int)} for non ascii characters.
   *
   * @param  s  to lower case
   *
   * @return  new lower case string
   */
  private static String toLowerCaseNonAscii(final String s)
  {
    if (s == null || s.isEmpty()) {
      return s;
    }
    final int len = s.length();
    final StringBuilder sb = new StringBuilder(len);
    char ch;
    for (int i = 0; i < len; i++) {
      ch = s.charAt(i);
      if (ch <= 0x7F) {
        if (ch >= 'A' && ch <= 'Z') {
          sb.append((char) (ch + 32));
        } else {
          sb.append(ch);
        }
      } else {
        if (i + 1 < len && Character.isHighSurrogate(ch)) {
          final char lowSurr = s.charAt(++i);
          final int codePoint = Character.toCodePoint(ch, lowSurr);
          if (Character.isUpperCase(codePoint)) {
            final int lowerCodePoint = Character.toLowerCase(codePoint);
            sb.append(Character.highSurrogate(lowerCodePoint));
            sb.append(Character.lowSurrogate(lowerCodePoint));
          } else {
            sb.append(Character.highSurrogate(codePoint));
            sb.append(Character.lowSurrogate(codePoint));
          }
        } else if (Character.isUpperCase(ch)) {
          sb.append(Character.toLowerCase(ch));
        } else {
          sb.append(ch);
        }
      }
    }
    return sb.toString();
  }


  /**
   * Converts the characters A-Z to a-z.
   *
   * @param  s  to lower case
   *
   * @return  new string with lower case alphabetical characters
   *
   * @throws  IllegalArgumentException  if the supplied string contains non-ascii characters
   */
  public static String toLowerCaseAscii(final String s)
  {
    if (s == null || s.isEmpty()) {
      return s;
    }
    // mutate A-Z to a-z
    // CheckStyle:MagicNumber OFF
    final char[] chars = s.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if (chars[i] > 0x7F) {
        throw new IllegalArgumentException("String contains non-ascii characters: " + s);
      } else if (chars[i] >= 'A' && chars[i] <= 'Z') {
        chars[i] = (char) (chars[i] + 32);
      }
    }
    // CheckStyle:MagicNumber ON
    return new String(chars);
  }


  /**
   * Converts the supplied string to upper case. If the string contains non-ascii characters, {@link Locale#ROOT} is
   * used.
   *
   * @param  s  to upper case
   *
   * @return  new upper case string
   */
  public static String toUpperCase(final String s)
  {
    if (s == null || s.isEmpty()) {
      return s;
    }
    // CheckStyle:MagicNumber OFF
    // if string contains non-ascii, use locale specific uppercase
    if (s.chars().anyMatch(c -> c > 0x7F)) {
      return s.toUpperCase(Locale.ROOT);
    }
    return toUpperCaseAscii(s);
  }


  /**
   * Converts the supplied string to upper case. Uses {@link Character#isLowerCase(int)} for non ascii characters.
   *
   * @param  s  to upper case
   *
   * @return  new upper case string
   */
  private static String toUpperCaseNonAscii(final String s)
  {
    if (s == null || s.isEmpty()) {
      return s;
    }
    final int len = s.length();
    final StringBuilder sb = new StringBuilder(len);
    char ch;
    for (int i = 0; i < len; i++) {
      ch = s.charAt(i);
      if (ch <= 0x7F) {
        if (ch >= 'a' && ch <= 'z') {
          sb.append((char) (ch - 32));
        } else {
          sb.append(ch);
        }
      } else {
        if (i + 1 < len && Character.isHighSurrogate(ch)) {
          final char lowSurr = s.charAt(++i);
          final int codePoint = Character.toCodePoint(ch, lowSurr);
          if (Character.isLowerCase(codePoint)) {
            final int upperCodePoint = Character.toUpperCase(codePoint);
            sb.append(Character.highSurrogate(upperCodePoint));
            sb.append(Character.lowSurrogate(upperCodePoint));
          } else {
            sb.append(Character.highSurrogate(codePoint));
            sb.append(Character.lowSurrogate(codePoint));
          }
        } else if (Character.isLowerCase(ch)) {
          sb.append(Character.toUpperCase(ch));
        } else {
          sb.append(ch);
        }
      }
    }
    return sb.toString();
  }


  /**
   * Converts the characters a-z to A-Z.
   *
   * @param  s  to upper case
   *
   * @return  new string with upper case alphabetical characters
   *
   * @throws  IllegalArgumentException  if the supplied string contains non-ascii characters
   */
  public static String toUpperCaseAscii(final String s)
  {
    if (s == null || s.isEmpty()) {
      return s;
    }
    // mutate a-z to A-Z
    // CheckStyle:MagicNumber OFF
    final char[] chars = s.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if (chars[i] > 0x7F) {
        throw new IllegalArgumentException("String contains non-ascii characters: " + s);
      } else if (chars[i] >= 'a' && chars[i] <= 'z') {
        chars[i] = (char) (chars[i] - 32);
      }
    }
    // CheckStyle:MagicNumber ON
    return new String(chars);
  }


  /**
   * Reads the data in the supplied stream and returns it as a byte array.
   *
   * @param  is  stream to read
   *
   * @return  bytes read from the stream
   *
   * @throws  IOException  if an error occurs reading data
   */
  public static byte[] readInputStream(final InputStream is)
    throws IOException
  {
    final ByteArrayOutputStream data = new ByteArrayOutputStream();
    try (is; data) {
      final byte[] buffer = new byte[READ_BUFFER_SIZE];
      int length;
      while ((length = is.read(buffer)) != -1) {
        data.write(buffer, 0, length);
      }
    }
    return data.toByteArray();
  }


  /**
   * Concatenates multiple arrays together.
   *
   * @param  <T>  type of array
   * @param  first  array to concatenate. Cannot be null.
   * @param  rest  of the arrays to concatenate. May be null.
   *
   * @return  array containing the concatenation of all parameters
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] concatArrays(final T[] first, final T[]... rest)
  {
    int totalLength = first.length;
    for (T[] array : rest) {
      if (array != null) {
        totalLength += array.length;
      }
    }

    final T[] result = Arrays.copyOf(first, totalLength);

    int offset = first.length;
    for (T[] array : rest) {
      if (array != null) {
        System.arraycopy(array, 0, result, offset, array.length);
        offset += array.length;
      }
    }
    return result;
  }


  /**
   * Determines equality of the supplied objects. Array types are automatically detected.
   *
   * @param  o1  to test equality of
   * @param  o2  to test equality of
   *
   * @return  whether o1 equals o2
   */
  public static boolean areEqual(final Object o1, final Object o2)
  {
    if (o1 == o2) {
      return true;
    }
    final boolean areEqual;
    if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
      areEqual = Arrays.equals((boolean[]) o1, (boolean[]) o2);
    } else if (o1 instanceof byte[] && o2 instanceof byte[]) {
      areEqual = Arrays.equals((byte[]) o1, (byte[]) o2);
    } else if (o1 instanceof char[] && o2 instanceof char[]) {
      areEqual = Arrays.equals((char[]) o1, (char[]) o2);
    } else if (o1 instanceof double[] && o2 instanceof double[]) {
      areEqual = Arrays.equals((double[]) o1, (double[]) o2);
    } else if (o1 instanceof float[] && o2 instanceof float[]) {
      areEqual = Arrays.equals((float[]) o1, (float[]) o2);
    } else if (o1 instanceof int[] && o2 instanceof int[]) {
      areEqual = Arrays.equals((int[]) o1, (int[]) o2);
    } else if (o1 instanceof long[] && o2 instanceof long[]) {
      areEqual = Arrays.equals((long[]) o1, (long[]) o2);
    } else if (o1 instanceof short[] && o2 instanceof short[]) {
      areEqual = Arrays.equals((short[]) o1, (short[]) o2);
    } else if (o1 instanceof Object[] && o2 instanceof Object[]) {
      areEqual = Arrays.deepEquals((Object[]) o1, (Object[]) o2);
    } else {
      areEqual = o1 != null && o1.equals(o2);
    }
    return areEqual;
  }


  /**
   * Computes a hash code for the supplied objects using the supplied seed. If a Collection type is found it is iterated
   * over.
   *
   * @param  seed  odd/prime number
   * @param  objects  to calculate hashCode for
   *
   * @return  hash code for the supplied objects
   */
  public static int computeHashCode(final int seed, final Object... objects)
  {
    if (objects == null || objects.length == 0) {
      return seed * HASH_CODE_PRIME;
    }

    int hc = seed;
    for (Object object : objects) {
      hc = HASH_CODE_PRIME * hc;
      if (object != null) {
        if (object instanceof List<?> || object instanceof Queue<?>) {
          int index = 1;
          for (Object o : (Collection<?>) object) {
            hc += computeHashCode(o) * index++;
          }
        } else if (object instanceof Collection<?>) {
          for (Object o : (Collection<?>) object) {
            hc += computeHashCode(o);
          }
        } else {
          hc += computeHashCode(object);
        }
      }
    }
    return hc;
  }


  /**
   * Computes a hash code for the supplied object. Checks for arrays of primitives and Objects then delegates to the
   * {@link Arrays} class. Otherwise {@link Object#hashCode()} is invoked.
   *
   * @param  object  to calculate hash code for
   *
   * @return  hash code
   */
  private static int computeHashCode(final Object object)
  {
    int hc = 0;
    if (object instanceof boolean[]) {
      hc += Arrays.hashCode((boolean[]) object);
    } else if (object instanceof byte[]) {
      hc += Arrays.hashCode((byte[]) object);
    } else if (object instanceof char[]) {
      hc += Arrays.hashCode((char[]) object);
    } else if (object instanceof double[]) {
      hc += Arrays.hashCode((double[]) object);
    } else if (object instanceof float[]) {
      hc += Arrays.hashCode((float[]) object);
    } else if (object instanceof int[]) {
      hc += Arrays.hashCode((int[]) object);
    } else if (object instanceof long[]) {
      hc += Arrays.hashCode((long[]) object);
    } else if (object instanceof short[]) {
      hc += Arrays.hashCode((short[]) object);
    } else if (object instanceof Object[]) {
      hc += Arrays.deepHashCode((Object[]) object);
    } else {
      hc += object.hashCode();
    }
    return hc;
  }


  /**
   * Returns whether the supplied string represents an IP address. Matches both IPv4 and IPv6 addresses.
   *
   * @param  s  to match
   *
   * @return  whether the supplied string represents an IP address
   */
  public static boolean isIPAddress(final String s)
  {
    return
      s != null &&
      (IPV4_PATTERN.matcher(s).matches() || IPV6_STD_PATTERN.matcher(s).matches() ||
        IPV6_HEX_COMPRESSED_PATTERN.matcher(s).matches());
  }


  /**
   * Looks for the supplied system property value and loads a class with that name. The default constructor for that
   * class is then returned.
   *
   * @param  property  whose value is a class
   *
   * @return  class constructor or null if no system property was found
   *
   * @throws  IllegalArgumentException  if an error occurs instantiating the constructor
   */
  public static Constructor<?> createConstructorFromProperty(final String property)
  {
    final String clazz = System.getProperty(property);
    if (clazz != null) {
      try {
        return Class.forName(clazz).getDeclaredConstructor();
      } catch (Exception e) {
        throw new IllegalArgumentException("Error getting declared constructor for " + clazz, e);
      }
    }
    return null;
  }
}
