/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.nio.ByteBuffer;

/**
 * Provides functionality common to DER types implementations.
 *
 * @author  Middleware Services
 */
public abstract class AbstractDERType
{

  /** Length of short form integers. */
  private static final int SHORT_FORM_INT_LENGTH = 127;

  /** Constructed tag. */
  private final int derTag;


  /**
   * Creates a new abstract der type.
   *
   * @param  tag  to encode for this type
   */
  public AbstractDERType(final DERTag tag)
  {
    derTag = tag.getTagByte();
  }


  /**
   * DER encode the supplied items with the tag associated with this type. If the length is greater than 127 bytes the
   * long form is always expressed using 4 bytes.
   *
   * @param  items  to encode
   *
   * @return  DER encoded items
   */
  protected byte[] encode(final byte[]... items)
  {
    int itemLength = 0;
    for (byte[] b : items) {
      itemLength += b.length;
    }

    byte[] lengthBytes;
    if (itemLength <= SHORT_FORM_INT_LENGTH) {
      lengthBytes = new byte[] {(byte) itemLength};
    } else {
      // use 4 bytes for all long form integers
      // CheckStyle:MagicNumber OFF
      lengthBytes = new byte[] {
        (byte) 0x84,
        (byte) (itemLength >>> 24),
        (byte) (itemLength >>> 16),
        (byte) (itemLength >>> 8),
        (byte) itemLength,
      };
      // CheckStyle:MagicNumber ON
    }

    // add 1 for the type tag, 1 or 5 for the length
    final ByteBuffer encodedItem = ByteBuffer.allocate(itemLength + 1 + lengthBytes.length);
    encodedItem.put((byte) derTag);
    for (byte b : lengthBytes) {
      encodedItem.put(b);
    }
    for (byte[] b : items) {
      encodedItem.put(b);
    }
    return encodedItem.array();
  }


  /**
   * Returns a byte array containing the bytes from {@link ByteBuffer#limit()} to {@link ByteBuffer#position()}.
   *
   * @param  encoded  to read bytes from
   *
   * @return  bytes
   */
  public static byte[] readBuffer(final ByteBuffer encoded)
  {
    final byte[] bytes = new byte[encoded.limit() - encoded.position()];
    encoded.get(bytes);
    return bytes;
  }
}
