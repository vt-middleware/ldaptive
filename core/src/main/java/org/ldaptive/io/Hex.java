/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.util.Arrays;

/**
 * Utility for hexidecimal encoding and decoding.
 *
 * @author  Middleware Services
 */
public final class Hex
{

  /** Hexidecimal characters. */
  private static final char[] HEX_CHARS = {
    '0',
    '1',
    '2',
    '3',
    '4',
    '5',
    '6',
    '7',
    '8',
    '9',
    'A',
    'B',
    'C',
    'D',
    'E',
    'F',
  };

  /**
   * Decode table which stores characters from 0 to f. Anything higher than 'f' is invalid so that is the max size of
   * the array.
   */
  private static final byte[] DECODE = new byte['f' + 1];

  /**
   * Initialize the DECODE table.
   */
  // CheckStyle:MagicNumber OFF
  static {
    // set all values to -1 to indicate error
    Arrays.fill(DECODE, (byte) -1);
    // set values for hex 0-9
    for (int i = '0'; i <= '9'; i++) {
      DECODE[i] = (byte) (i - '0');
    }
    // set values for hex A-F
    for (int i = 'A'; i <= 'F'; i++) {
      DECODE[i] = (byte) (i - 'A' + 10);
    }
    // set values for hex a-f
    for (int i = 'a'; i <= 'f'; i++) {
      DECODE[i] = (byte) (i - 'a' + 10);
    }
  }
  // CheckStyle:MagicNumber ON


  /** Default constructor. */
  private Hex() {}


  /**
   * This will convert the supplied value to a hex encoded string. Returns null if the supplied value is null.
   *
   * @param  value  to hex encode
   *
   * @return  hex encoded value
   */
  public static char[] encode(final byte... value)
  {
    if (value == null) {
      return null;
    }

    final int l = value.length;
    final char[] encoded = new char[l << 1];
    // CheckStyle:MagicNumber OFF
    for (int i = 0, j = 0; i < l; i++) {
      encoded[j++] = HEX_CHARS[(0xF0 & value[i]) >>> 4];
      encoded[j++] = HEX_CHARS[0x0F & value[i]];
    }
    // CheckStyle:MagicNumber ON
    return encoded;
  }


  /**
   * This will convert the supplied value from a hex encoded string. Returns null if the supplied value is null.
   *
   * @param  value  to hex decode
   *
   * @return  hex decoded value
   *
   * @throws  IllegalArgumentException  if value is not valid hexidecimal
   */
  public static byte[] decode(final char... value)
  {
    if (value == null) {
      return null;
    }

    final int l = value.length;
    // CheckStyle:MagicNumber OFF
    if ((l & 0x01) != 0) {
      throw new IllegalArgumentException(
        String.format("Cannot decode odd number of characters for %s", String.valueOf(value)));
    }
    // CheckStyle:MagicNumber ON

    final byte[] decoded = new byte[l >> 1];

    // CheckStyle:MagicNumber OFF
    for (int i = 0, j = 0; j < l; i++, j += 2) {
      final int high = decode(value, j) << 4;
      final int low = decode(value, j + 1);
      decoded[i] = (byte) ((high | low) & 0xFF);
    }
    // CheckStyle:MagicNumber ON
    return decoded;
  }


  /**
   * Decodes the supplied character to it's corresponding nibble.
   *
   * @param  hex  to read character from
   * @param  i  index of hex to read
   *
   * @return  0-15 integer
   *
   * @throws  IllegalArgumentException  if the character is not valid hex
   */
  protected static int decode(final char[] hex, final int i)
  {
    final char c = hex[i];
    if (c > 'f') {
      throw new IllegalArgumentException(
        String.format("Invalid hex character '%s' at position %s in %s", c, i, Arrays.toString(hex)));
    }

    final byte b = DECODE[c];
    if (b < 0) {
      throw new IllegalArgumentException(
        String.format("Invalid hex character '%s' at position %s in %s", c, i, Arrays.toString(hex)));
    }
    return b;
  }
}
