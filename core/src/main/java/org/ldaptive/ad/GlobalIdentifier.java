/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad;

import java.nio.ByteBuffer;
import java.util.StringTokenizer;
import org.ldaptive.LdapUtils;

/**
 * Class to represent an active directory GUID. Provides conversion from binary
 * to string and vice versa.
 *
 * @author  Middleware Services
 */
public final class GlobalIdentifier
{


  /** Default constructor. */
  private GlobalIdentifier() {}


  /**
   * Converts the supplied GUID to it's string format.
   *
   * @param  guid  to convert
   *
   * @return  string format of the GUID
   */
  public static String toString(final byte[] guid)
  {
    // CheckStyle:MagicNumber OFF

    // create a byte buffer for reading the guid
    final ByteBuffer guidBuffer = ByteBuffer.wrap(guid);

    // string identifier
    final StringBuilder sb = new StringBuilder("{");
    // encode the first 4 bytes, big endian
    guidBuffer.limit(4);
    sb.append(LdapUtils.hexEncode(getBytes(guidBuffer, true)));

    // encode the next 2 bytes, big endian
    guidBuffer.limit(6);
    sb.append("-").append(LdapUtils.hexEncode(getBytes(guidBuffer, true)));

    // encode the next 2 bytes, big endian
    guidBuffer.limit(8);
    sb.append("-").append(LdapUtils.hexEncode(getBytes(guidBuffer, true)));

    // encode the next 2 bytes, little endian
    guidBuffer.limit(10);
    sb.append("-").append(LdapUtils.hexEncode(getBytes(guidBuffer, false)));

    // encode the last 6 bytes, little endian
    guidBuffer.limit(guidBuffer.capacity());
    sb.append("-").append(LdapUtils.hexEncode(getBytes(guidBuffer, false)));
    sb.append("}");

    return sb.toString();
    // CheckStyle:MagicNumber ON
  }


  /**
   * Converts the supplied GUID to it's binary format.
   *
   * @param  guid  to convert
   *
   * @return  binary format of the GUID
   */
  public static byte[] toBytes(final String guid)
  {
    // CheckStyle:MagicNumber OFF

    // remove the enclosing brackets {...}
    final StringTokenizer st = new StringTokenizer(
      guid.substring(1, guid.length() - 1),
      "-");
    // first token is 4 bytes, big endian
    final String data1 = st.nextToken();
    // second token is 2 bytes, big endian
    final String data2 = st.nextToken();
    // third token is 2 bytes, big endian
    final String data3 = st.nextToken();
    // fourth token is 2 bytes, little endian
    final String data4 = st.nextToken();
    // fifth token is 6 bytes, little endian
    final String data5 = st.nextToken();

    final ByteBuffer guidBuffer = ByteBuffer.allocate(16);
    putBytes(guidBuffer, LdapUtils.hexDecode(data1.toCharArray()), true);
    putBytes(guidBuffer, LdapUtils.hexDecode(data2.toCharArray()), true);
    putBytes(guidBuffer, LdapUtils.hexDecode(data3.toCharArray()), true);
    putBytes(guidBuffer, LdapUtils.hexDecode(data4.toCharArray()), false);
    putBytes(guidBuffer, LdapUtils.hexDecode(data5.toCharArray()), false);

    return guidBuffer.array();
    // CheckStyle:MagicNumber ON
  }


  /**
   * Reads bytes from the supplied byte buffer. The byte buffer limit must be
   * set appropriately by the caller.
   *
   * @param  buffer  to read bytes from
   * @param  bigEndian  whether to return the bytes as big endian
   *
   * @return  long value
   */
  private static byte[] getBytes(
    final ByteBuffer buffer,
    final boolean bigEndian)
  {
    // CheckStyle:MagicNumber OFF
    final byte[] bytes = new byte[buffer.limit() - buffer.position()];
    if (bigEndian) {
      int offset = bytes.length - 1;
      while (buffer.hasRemaining()) {
        bytes[offset--] = (byte) (buffer.get() & 0xFF);
      }
    } else {
      int offset = 0;
      while (buffer.hasRemaining()) {
        bytes[offset++] = (byte) (buffer.get() & 0xFF);
      }
    }
    return bytes;
    // CheckStyle:MagicNumber ON
  }


  /**
   * Writes a long into the supplied byte buffer. The byte buffer limit must be
   * set appropriately by the caller.
   *
   * @param  buffer  to write long to
   * @param  bytes  to write
   * @param  bigEndian  whether to write the bytes as big endian
   */
  private static void putBytes(
    final ByteBuffer buffer,
    final byte[] bytes,
    final boolean bigEndian)
  {
    // CheckStyle:MagicNumber OFF
    if (bigEndian) {
      for (int i = bytes.length - 1; i >= 0; i--) {
        buffer.put((byte) (bytes[i] & 0xFF));
      }
    } else {
      for (byte b : bytes) {
        buffer.put((byte) (b & 0xFF));
      }
    }
    // CheckStyle:MagicNumber ON
  }
}
