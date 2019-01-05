/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Converts UUIDs to and from their DER encoded format. See RFC 4122.
 *
 * @author  Middleware Services
 */
public class UuidType extends AbstractDERType implements DEREncoder
{

  /** Number of bytes in a uuid. */
  private static final int UUID_LENGTH = 16;

  /** UUID to encode. */
  private final byte[] derItem;


  /**
   * Creates a new uuid type.
   *
   * @param  item  to DER encode
   */
  public UuidType(final UUID item)
  {
    super(UniversalDERTag.OCTSTR);
    derItem = toBytes(item);
  }


  /**
   * Creates a new uuid type.
   *
   * @param  tag  der tag associated with this type
   * @param  item  to DER encode
   *
   * @throws  IllegalArgumentException  if the der tag is constructed
   */
  public UuidType(final DERTag tag, final UUID item)
  {
    super(tag);
    if (tag.isConstructed()) {
      throw new IllegalArgumentException("DER tag must not be constructed");
    }
    derItem = toBytes(item);
  }


  @Override
  public byte[] encode()
  {
    return encode(derItem);
  }


  /**
   * Converts bytes in the buffer to a uuid by reading from the current position to the limit.
   *
   * @param  encoded  buffer containing DER-encoded data where the buffer is positioned at the start of uuid bytes and
   *                  the limit is set beyond the last byte of uuid data.
   *
   * @return  decoded bytes as a uuid.
   */
  public static UUID decode(final DERBuffer encoded)
  {
    final long mostSig = readLong(encoded);
    final long leastSig = readLong(encoded);
    return new UUID(mostSig, leastSig);
  }


  /**
   * Reads the next 8 bytes from the supplied buffer to create a long.
   *
   * @param  buffer  to read
   *
   * @return  UUID component integer
   */
  protected static long readLong(final DERBuffer buffer)
  {
    // CheckStyle:MagicNumber OFF
    return
      (((long) buffer.get()) << 56) |
      (((long) buffer.get() & 0xff) << 48) |
      (((long) buffer.get() & 0xff) << 40) |
      (((long) buffer.get() & 0xff) << 32) |
      (((long) buffer.get() & 0xff) << 24) |
      (((long) buffer.get() & 0xff) << 16) |
      (((long) buffer.get() & 0xff) <<  8) |
      (((long) buffer.get() & 0xff));
    // CheckStyle:MagicNumber ON
  }


  /**
   * Converts the supplied uuid to a byte array.
   *
   * @param  uuid  to convert
   *
   * @return  byte array
   */
  public static byte[] toBytes(final UUID uuid)
  {
    final ByteBuffer buffer = ByteBuffer.wrap(new byte[UUID_LENGTH]);
    buffer.putLong(uuid.getMostSignificantBits());
    buffer.putLong(uuid.getLeastSignificantBits());
    return buffer.array();
  }
}
