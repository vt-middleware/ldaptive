/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;

/**
 * Converts object identifiers to and from their DER encoded format.
 *
 * @author  Middleware Services
 * @version  $Revision: 3068 $ $Date: 2014-10-24 13:22:32 -0400 (Fri, 24 Oct 2014) $
 */
public class OidType extends AbstractDERType implements DEREncoder
{

  /** Integer to encode. */
  private final byte[] derItem;


  /**
   * Creates a new oid type.
   *
   * @param  item  to DER encode
   */
  public OidType(final String item)
  {
    super(UniversalDERTag.OID);
    derItem = toBytes(parse(item));
  }


  /**
   * Creates a new oid type.
   *
   * @param  item  to DER encode
   */
  public OidType(final int[] item)
  {
    super(UniversalDERTag.OID);
    derItem = toBytes(item);
  }


  /**
   * Creates a new oid type.
   *
   * @param  tag  der tag associated with this type
   * @param  item  to DER encode
   *
   * @throws  IllegalArgumentException  if the der tag is constructed
   */
  public OidType(final DERTag tag, final String item)
  {
    super(tag);
    if (tag.isConstructed()) {
      throw new IllegalArgumentException("DER tag must not be constructed");
    }
    derItem = toBytes(parse(item));
  }


  /**
   * Creates a new oid type.
   *
   * @param  tag  der tag associated with this type
   * @param  item  to DER encode
   *
   * @throws  IllegalArgumentException  if the der tag is constructed
   */
  public OidType(final DERTag tag, final int[] item)
  {
    super(tag);
    if (tag.isConstructed()) {
      throw new IllegalArgumentException("DER tag must not be constructed");
    }
    derItem = toBytes(item);
  }


  /** {@inheritDoc} */
  @Override
  public byte[] encode()
  {
    return encode(derItem);
  }


  /**
   * Converts bytes in the buffer to an OID by reading from the current position
   * to the limit, which assumes the bytes of the integer are in big-endian
   * order.
   *
   * @param  encoded  buffer containing DER-encoded data where the buffer is
   * positioned at the start of OID bytes and the limit is set beyond the last
   * byte of OID data.
   *
   * @return  decoded bytes as an OID.
   */
  public static String decode(final ByteBuffer encoded)
  {
    final ByteBuffer buffer = ByteBuffer.wrap(readBuffer(encoded));
    final StringBuilder sb = new StringBuilder();
    final int firstId = buffer.get();
    // CheckStyle:MagicNumber OFF
    if (firstId < 40) {
      sb.append("0").append(".").append(firstId).append(".");
    } else if (firstId < 80) {
      sb.append("1").append(".").append(firstId - 40).append(".");
    } else {
      sb.append("2").append(".").append(firstId - 80).append(".");
    }
    // CheckStyle:MagicNumber ON
    while (buffer.hasRemaining()) {
      sb.append(readInt(buffer)).append(".");
    }
    sb.setLength(sb.length() - 1);
    return sb.toString();
  }


  /**
   * Converts the supplied list of oid components to a byte array.
   *
   * @param  oid  to convert
   *
   * @return  byte array
   *
   * @throws  IllegalArgumentException  if the oid is not valid. See {@link
   * #isValid(int[])}
   */
  public static byte[] toBytes(final int[] oid)
  {
    isValid(oid);

    final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try {
      try {
        // CheckStyle:MagicNumber OFF
        if (oid[0] < 2) {
          // should always fit into one byte, since oid[1] must be <= 38
          bytes.write((oid[0] * 40) + oid[1]);
        } else {
          bytes.write(toBytes((oid[0] * 40) + oid[1]));
        }
        for (int i = 2; i < oid.length; i++) {
          bytes.write(toBytes(oid[i]));
        }
        // CheckStyle:MagicNumber ON
      } finally {
        bytes.close();
      }
    } catch (IOException e) {
      throw new IllegalStateException("Byte conversion failed", e);
    }
    return bytes.toByteArray();
  }


  /**
   * Checks whether the supplied oid is valid. Oids must meet the following
   * criteria:
   *
   * <ul>
   *   <li>must not be null and must have at least 2 elements</li>
   *   <li>components must not be negative</li>
   *   <li>first component must be 0, 1, or 2</li>
   *   <li>if first component 0 or 1, second component must be <= 38</li>
   * </ul>
   *
   * @param  oid  to check
   *
   * @throws  IllegalArgumentException  if the oid is not valid.
   */
  protected static void isValid(final int[] oid)
  {
    // CheckStyle:MagicNumber OFF
    if (oid == null || oid.length < 2) {
      throw new IllegalArgumentException(
        "OIDs must have at least two components");
    }
    if (oid[0] < 0 || oid[0] > 2) {
      throw new IllegalArgumentException("The first OID must be 0, 1, or 2");
    }
    if (oid[0] < 2 && oid[1] > 39) {
      throw new IllegalArgumentException(
        "The second OID must be less than or equal to 38");
    }
    for (int i : oid) {
      if (i < 0) {
        throw new IllegalArgumentException("OIDs cannot be negative");
      }
    }
    // CheckStyle:MagicNumber ON
  }


  /**
   * Converts the supplied oid component to a byte array. The length of the byte
   * array is the minimal size needed to contain the oid component.
   *
   * @param  component  to convert to bytes
   *
   * @return  oid bytes
   */
  protected static byte[] toBytes(final int component)
  {
    // CheckStyle:MagicNumber OFF
    final byte[] buffer = new byte[4];
    int size = 0;
    int val = component;
    while (val != 0) {
      if (size > 0) {
        buffer[size++] = (byte) ((val & 0x7F) | 0x80);
      } else {
        buffer[size++] = (byte) (val & 0x7F);
      }
      val >>>= 7;
    }

    final byte[] bytes = new byte[size];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = buffer[--size];
    }
    return bytes;
    // CheckStyle:MagicNumber ON
  }


  /**
   * Reads the necessary encoded bytes from the supplied buffer to create an
   * integer.
   *
   * @param  buffer  to read
   *
   * @return  OID component integer
   */
  protected static int readInt(final ByteBuffer buffer)
  {
    // CheckStyle:MagicNumber OFF
    int val = 0;
    for (int i = 0; i < 4; i++) {
      final byte b = buffer.get();
      if (i == 0 && b == 0x80) {
        throw new IllegalArgumentException("Component starts with 0x80");
      }
      val <<= 7;
      val |= b & 0x7F;
      if ((b & 0x80) == 0) {
        return val;
      }
    }
    // CheckStyle:MagicNumber ON
    throw new IllegalArgumentException("Integer greater than 4 bytes in size");
  }


  /**
   * Converts the supplied oid into an array on integers.
   *
   * @param  oid  to parse
   *
   * @return  array of oid components
   *
   * @throws  IllegalArgumentException  if the oid is not valid. See {@link
   * #isValid(int[])}
   */
  public static int[] parse(final String oid)
  {
    if (oid == null) {
      throw new IllegalArgumentException("OID cannot be null");
    }

    final StringTokenizer st = new StringTokenizer(oid, ".");
    final int[] oids = new int[st.countTokens()];
    int i = 0;
    while (st.hasMoreTokens()) {
      try {
        oids[i++] = Integer.parseInt(st.nextToken());
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(e);
      }
    }
    isValid(oids);
    return oids;
  }
}
