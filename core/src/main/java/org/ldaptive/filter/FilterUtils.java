/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.ldaptive.LdapUtils;

/**
 * Provides utility methods for this package.
 *
 * @author  Middleware Services
 */
public final class FilterUtils
{


  /** Default constructor. */
  private FilterUtils() {}


  /**
   * Escapes the supplied string per RFC 4515.
   *
   * @param  s  to escape
   *
   * @return  escaped string
   */
  public static String escape(final String s)
  {
    final StringBuilder sb = new StringBuilder(s.length());
    final byte[] utf8 = s.getBytes(StandardCharsets.UTF_8);
    // CheckStyle:MagicNumber OFF
    // optimize if ASCII
    if (s.length() == utf8.length) {
      for (byte b : utf8) {
        if (b <= 0x1F || b == 0x28 || b == 0x29 || b == 0x2A || b == 0x5C || b == 0x7F) {
          sb.append('\\').append(LdapUtils.hexEncode(b));
        } else {
          sb.append((char) b);
        }
      }
    } else {
      int multiByte = 0;
      for (byte b : utf8) {
        if (multiByte > 0) {
          sb.append('\\').append(LdapUtils.hexEncode(b));
          multiByte--;
        } else if ((b & 0x7F) == b) {
          if (b <= 0x1F || b == 0x28 || b == 0x29 || b == 0x2A || b == 0x5C || b == 0x7F) {
            sb.append('\\').append(LdapUtils.hexEncode(b));
          } else {
            sb.append((char) b);
          }
        } else {
          // 2 byte character
          if ((b & 0xE0) == 0xC0) {
            multiByte = 1;
            // 3 byte character
          } else if ((b & 0xF0) == 0xE0) {
            multiByte = 2;
            // 4 byte character
          } else if ((b & 0xF8) == 0xF0) {
            multiByte = 3;
          } else {
            throw new IllegalStateException("Could not read UTF-8 string encoding");
          }
          sb.append('\\').append(LdapUtils.hexEncode(b));
        }
      }
    }
    // CheckStyle:MagicNumber ON
    return sb.toString();
  }


  /**
   * Convenience method for parsing an array of assertion values. See {@link #parseAssertionValue(String)}.
   *
   * @param  value  array of assertion values
   *
   * @return  assertion value bytes
   */
  public static byte[][] parseAssertionValue(final String... value)
  {
    final byte[][] bytes = new byte[value.length][];
    for (int i = 0; i < value.length; i++) {
      bytes[i] = parseAssertionValue(value[i]);
    }
    return bytes;
  }


  /**
   * Decodes hex characters in the attribute assertion.
   *
   * @param  value  to parse
   *
   * @return  assertion value bytes
   *
   * @throws  IllegalArgumentException  if the value contains \0, ( or )
   */
  public static byte[] parseAssertionValue(final String value)
  {
    final ByteArrayOutputStream bytes = new ByteArrayOutputStream(value.length());
    for (int i = 0; i < value.length(); i++) {
      final char c = value.charAt(i);
      if (c == '\0' || c == '(' || c == ')') {
        throw new IllegalArgumentException("Assertion value contains unescaped characters");
      } else if (c == '\\') {
        final char[] hexValue = new char[]{value.charAt(++i), value.charAt(++i)};
        try {
          bytes.write(LdapUtils.hexDecode(hexValue));
        } catch (IOException e) {
          throw new IllegalArgumentException("Could not hex decode " + Arrays.toString(hexValue) + " in " + value);
        }
      } else {
        bytes.write(c);
      }
    }
    return bytes.toByteArray();
  }
}
