/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.ldaptive.LdapUtils;
import org.ldaptive.protocol.SearchFilter;

/**
 * Base class for search filter implementations.
 *
 * @author  Middleware Services
 */
public abstract class AbstractSearchFilter implements SearchFilter
{


  /**
   * Convenience method for parsing an array of assertion values. See {@link #parseAssertionValue(String)}.
   *
   * @param  value  array of assertion values
   *
   * @return  assertion value bytes
   */
  protected static byte[][] parseAssertionValue(final String... value)
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
   */
  protected static byte[] parseAssertionValue(final String value)
  {
    final ByteArrayOutputStream bytes = new ByteArrayOutputStream(value.length());
    for (int i = 0; i < value.length(); i++) {
      final char c = value.charAt(i);
      switch(c) {
      case '\\':
        final char[] hexValue = new char[] {value.charAt(++i), value.charAt(++i)};
        try {
          bytes.write(LdapUtils.hexDecode(hexValue));
        } catch (IOException e) {
          throw new IllegalArgumentException("Could not hex decode " + hexValue + " in " + value);
        }
        break;
      default:
        bytes.write(c);
        break;
      }
    }
    return bytes.toByteArray();
  }
}
