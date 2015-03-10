/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Class to represent an active directory SID. Provides conversion from binary to string and vice versa.
 *
 * @author  Middleware Services
 */
public final class SecurityIdentifier
{


  /** Default constructor. */
  private SecurityIdentifier() {}


  /**
   * Converts the supplied SID to it's string format.
   *
   * @param  sid  to convert
   *
   * @return  string format of the SID
   */
  public static String toString(final byte[] sid)
  {
    // CheckStyle:MagicNumber OFF

    // format of SID: S-R-X-Y1-Y2...-Yn
    // S: static 'S', indicating string
    // R: revision
    // X: authority
    // Yn: sub-authority

    // create a byte buffer for reading the sid
    final ByteBuffer sidBuffer = ByteBuffer.wrap(sid);

    // string identifier
    final StringBuilder sb = new StringBuilder("S");

    // byte[0] is the revision
    sb.append("-").append(sidBuffer.get() & 0xFF);

    // byte[1] is the count of sub-authorities
    final int countSubAuth = sidBuffer.get() & 0xFF;

    // byte[2] - byte[7] is the authority (48 bits)
    sidBuffer.limit(8);
    sb.append("-").append(getLong(sidBuffer, true));

    // byte[8] - ? is the sub-authorities,
    // (32 bits per authority, little endian)
    for (int i = 0; i < countSubAuth; i++) {
      // values are unsigned, so get 4 bytes as a long
      sidBuffer.limit(sidBuffer.position() + 4);
      sb.append("-").append(getLong(sidBuffer, false));
    }

    return sb.toString();
    // CheckStyle:MagicNumber ON
  }


  /**
   * Converts the supplied SID to it's binary format.
   *
   * @param  sid  to convert
   *
   * @return  binary format of the SID
   */
  public static byte[] toBytes(final String sid)
  {
    // CheckStyle:MagicNumber OFF

    // format of SID: S-R-X-Y1-Y2...-Yn
    // S: static 'S', indicating string
    // R: revision
    // X: authority
    // Yn: sub-authority

    final StringTokenizer st = new StringTokenizer(sid, "-");
    // first token is the 'S'
    st.nextToken();

    // second token is the revision
    final int revision = Integer.valueOf(st.nextToken());
    // third token is the authority
    final long authority = Long.valueOf(st.nextToken());
    // remaining token are the sub authorities
    final List<String> subAuthorities = new ArrayList<>();
    while (st.hasMoreTokens()) {
      subAuthorities.add(st.nextToken());
    }

    // revision is 1 byte
    // sub-authorities count is 1 byte
    // authority is 6 bytes
    // 4 bytes for each sub-authority
    final int size = 8 + (4 * subAuthorities.size());
    final ByteBuffer sidBuffer = ByteBuffer.allocate(size);
    sidBuffer.put((byte) (revision & 0xFF));
    sidBuffer.put((byte) (subAuthorities.size() & 0xFF));
    sidBuffer.limit(8);
    putLong(sidBuffer, authority, true);
    for (String subAuthority : subAuthorities) {
      sidBuffer.limit(sidBuffer.position() + 4);
      putLong(sidBuffer, Long.valueOf(subAuthority), false);
    }

    return sidBuffer.array();
    // CheckStyle:MagicNumber ON
  }


  /**
   * Reads a long from the supplied byte buffer. The byte buffer limit must be set appropriately by the caller.
   *
   * @param  buffer  to read long from
   * @param  bigEndian  whether to read the bytes as big endian
   *
   * @return  long value
   */
  private static long getLong(final ByteBuffer buffer, final boolean bigEndian)
  {
    // CheckStyle:MagicNumber OFF
    long value = buffer.get() & 0xFF;
    if (bigEndian) {
      // shift the value to the right, or with the next byte
      while (buffer.hasRemaining()) {
        value <<= Byte.SIZE;
        value |= buffer.get() & 0xFF;
      }
    } else {
      // shift the next byte to the right, or with the value
      int offset = Byte.SIZE;
      while (buffer.hasRemaining()) {
        value |= (buffer.get() & 0xFF) << offset;
        offset += Byte.SIZE;
      }
    }
    return value & 0xFFFFFFFFL;
    // CheckStyle:MagicNumber ON
  }


  /**
   * Writes a long into the supplied byte buffer. The byte buffer limit must be set appropriately by the caller.
   *
   * @param  buffer  to write long to
   * @param  value  to write
   * @param  bigEndian  whether to write the bytes as big endian
   */
  private static void putLong(final ByteBuffer buffer, final long value, final boolean bigEndian)
  {
    // CheckStyle:MagicNumber OFF
    if (bigEndian) {
      int offset = Byte.SIZE * (buffer.limit() - buffer.position() - 1);
      while (buffer.hasRemaining()) {
        // get the high bits and decrement down
        buffer.put((byte) ((value >> offset) & 0xFF));
        offset -= Byte.SIZE;
      }
    } else {
      int offset = 0;
      while (buffer.hasRemaining()) {
        // get the low bits and increment up
        buffer.put((byte) ((value >> offset) & 0xFF));
        offset += Byte.SIZE;
      }
    }
    // CheckStyle:MagicNumber ON
  }
}
