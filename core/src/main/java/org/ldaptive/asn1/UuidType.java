/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Converts UUIDs to and from their DER encoded format. See RFC 4122.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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


  /** {@inheritDoc} */
  @Override
  public byte[] encode()
  {
    return encode(derItem);
  }


  /**
   * Converts bytes in the buffer to a uuid by reading from the current position
   * to the limit.
   *
   * @param  encoded  buffer containing DER-encoded data where the buffer is
   * positioned at the start of uuid bytes and the limit is set beyond the last
   * byte of uuid data.
   *
   * @return  decoded bytes as a uuid.
   */
  public static UUID decode(final ByteBuffer encoded)
  {
    final ByteBuffer buffer = ByteBuffer.wrap(readBuffer(encoded));
    final long mostSig = buffer.getLong();
    final long leastSig = buffer.getLong();
    return new UUID(mostSig, leastSig);
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
