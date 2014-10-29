/*
  $Id: LdapUtils.java 3006 2014-07-02 14:22:50Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3006 $
  Updated: $Date: 2014-07-02 10:22:50 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;
import org.ldaptive.io.Base64;
import org.ldaptive.io.Hex;

/**
 * Provides utility methods for this package.
 *
 * @author  Middleware Services
 * @version  $Revision: 3006 $ $Date: 2014-07-02 10:22:50 -0400 (Wed, 02 Jul 2014) $
 */
public final class LdapUtils
{

  /** UTF-8 character set. */
  private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

  /** Size of buffer in bytes to use when reading files. */
  private static final int READ_BUFFER_SIZE = 128;

  /** Prime number to assist in calculating hash codes. */
  private static final int HASH_CODE_PRIME = 113;

  /** Pattern to match ipv4 addresses. */
  private static final Pattern IPV4_PATTERN = Pattern.compile(
    "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)" +
    "(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

  /** Pattern to match ipv6 addresses. */
  private static final Pattern IPV6_STD_PATTERN = Pattern.compile(
    "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");

  /** Pattern to match ipv6 hex compressed addresses. */
  private static final Pattern IPV6_HEX_COMPRESSED_PATTERN = Pattern.compile(
    "^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::" +
    "((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$");

  /** Prefix used to indicate a classpath resource. */
  private static final String CLASSPATH_PREFIX = "classpath:";

  /** Prefix used to indicate a file resource. */
  private static final String FILE_PREFIX = "file:";


  /** Default constructor. */
  private LdapUtils() {}


  /**
   * This will convert the supplied value to a base64 encoded string. Returns
   * null if the supplied byte array is null.
   *
   * @param  value  to base64 encode
   *
   * @return  base64 encoded value
   */
  public static String base64Encode(final byte[] value)
  {
    return
      value != null ?
        new String(Base64.encodeToByte(value, false), UTF8_CHARSET) : null;
  }


  /**
   * This will convert the supplied value to a base64 encoded string. Returns
   * null if the supplied string is null.
   *
   * @param  value  to base64 encode
   *
   * @return  base64 encoded value
   */
  public static String base64Encode(final String value)
  {
    return value != null ? base64Encode(value.getBytes(UTF8_CHARSET)) : null;
  }


  /**
   * This will convert the supplied value to a UTF-8 encoded string. Returns
   * null if the supplied byte array is null.
   *
   * @param  value  to UTF-8 encode
   *
   * @return  UTF-8 encoded value
   */
  public static String utf8Encode(final byte[] value)
  {
    return value != null ? new String(value, UTF8_CHARSET) : null;
  }


  /**
   * This will convert the supplied value to a UTF-8 encoded byte array. Returns
   * null if the supplied string is null.
   *
   * @param  value  to UTF-8 encode
   *
   * @return  UTF-8 encoded value
   */
  public static byte[] utf8Encode(final String value)
  {
    return value != null ? value.getBytes(UTF8_CHARSET) : null;
  }


  /**
   * This will convert the supplied value to a hex encoded string. Returns null
   * if the supplied byte array is null.
   *
   * @param  value  to hex encode
   *
   * @return  hex encoded value
   */
  public static char[] hexEncode(final byte[] value)
  {
    return value != null ? Hex.encode(value) : null;
  }


  /**
   * This will convert the supplied value to a hex encoded string. Returns null
   * if the supplied char array is null.
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
   * This will decode the supplied value as a base64 encoded string to a byte[].
   * Returns null if the supplied string is null.
   *
   * @param  value  to base64 decode
   *
   * @return  base64 decoded value
   */
  public static byte[] base64Decode(final String value)
  {
    return value != null ? Base64.decode(value.getBytes()) : null;
  }


  /**
   * This will decode the supplied value as a hex encoded string to a byte[].
   * Returns null if the supplied character array is null.
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
   * Reads the data at the supplied URL and returns it as a byte array.
   *
   * @param  url  to read
   *
   * @return  bytes read from the URL
   *
   * @throws  IOException  if an error occurs reading data
   */
  public static byte[] readURL(final URL url)
    throws IOException
  {
    return readInputStream(url.openStream());
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
    try {
      final byte[] buffer = new byte[READ_BUFFER_SIZE];
      int length;
      while ((length = is.read(buffer)) != -1) {
        data.write(buffer, 0, length);
      }
    } finally {
      is.close();
      data.close();
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
   * Determines equality of the supplied objects by delegating to their hashCode
   * methods.
   *
   * @param  o1  to test equality of
   * @param  o2  to test equality of
   *
   * @return  whether o1 equals o2
   */
  public static boolean areEqual(final Object o1, final Object o2)
  {
    if (o1 == null) {
      return o2 == null;
    }
    return
      o2 != null &&
      (o1 == o2 ||
        o1.getClass() == o2.getClass() && o1.hashCode() == o2.hashCode());
  }


  /**
   * Computes a hash code for the supplied objects using the supplied seed. If a
   * Collection type is found it is iterated over.
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
   * Computes a hash code for the supplied object. Checks for arrays of
   * primitives and Objects then delegates to the {@link Arrays} class.
   * Otherwise {@link Object#hashCode()} is invoked.
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
      hc += Arrays.hashCode((Object[]) object);
    } else {
      hc += object.hashCode();
    }
    return hc;
  }


  /**
   * Returns whether the supplied string represents an IP address. Matches both
   * IPv4 and IPv6 addresses.
   *
   * @param  s  to match
   *
   * @return  whether the supplied string represents an IP address
   */
  public static boolean isIPAddress(final String s)
  {
    return
      s != null &&
      (IPV4_PATTERN.matcher(s).matches() ||
        IPV6_STD_PATTERN.matcher(s).matches() ||
        IPV6_HEX_COMPRESSED_PATTERN.matcher(s).matches());
  }


  /**
   * Parses the supplied path and returns an input stream based on the prefix in
   * the path. If a path is prefixed with the string "classpath:" it is
   * interpreted as a classpath specification. If a path is prefixed with the
   * string "file:" it is interpreted as a file path.
   *
   * @param  path  that designates a resource
   *
   * @return  input stream to read the resource
   *
   * @throws  IOException  if the resource cannot be read
   * @throws  IllegalArgumentException  if path is not prefixed with either
   * 'classpath:' or 'file:'
   */
  public static InputStream getResource(final String path)
    throws IOException
  {
    InputStream is;
    if (path.startsWith(CLASSPATH_PREFIX)) {
      is = LdapUtils.class.getResourceAsStream(
        path.substring(CLASSPATH_PREFIX.length()));
    } else if (path.startsWith(FILE_PREFIX)) {
      is = new FileInputStream(new File(path.substring(FILE_PREFIX.length())));
    } else {
      throw new IllegalArgumentException(
        "path '" + path + "' must start with either " + CLASSPATH_PREFIX +
        " or " + FILE_PREFIX);
    }
    return is;
  }
}
