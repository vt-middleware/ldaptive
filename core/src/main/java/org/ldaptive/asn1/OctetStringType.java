/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.nio.charset.StandardCharsets;

/**
 * Converts strings to and from their DER encoded format.
 *
 * @author  Middleware Services
 */
public class OctetStringType extends AbstractDERType implements DEREncoder
{

  /** String to encode. */
  private final byte[] derItem;


  /**
   * Creates a new octet string type.
   *
   * @param  item  to DER encode
   */
  public OctetStringType(final String item)
  {
    this(item.getBytes(StandardCharsets.UTF_8));
  }


  /**
   * Creates a new octet string type.
   *
   * @param  item  to DER encode
   */
  public OctetStringType(final byte[] item)
  {
    super(UniversalDERTag.OCTSTR);
    derItem = item;
  }


  /**
   * Creates a new octet string type.
   *
   * @param  tag  der tag associated with this type
   * @param  item  to DER encode
   *
   * @throws  IllegalArgumentException  if the der tag is constructed
   */
  public OctetStringType(final DERTag tag, final String item)
  {
    this(tag, item.getBytes(StandardCharsets.UTF_8));
  }


  /**
   * Creates a new octet string type.
   *
   * @param  tag  der tag associated with this type
   * @param  item  to DER encode
   *
   * @throws  IllegalArgumentException  if the der tag is constructed
   */
  public OctetStringType(final DERTag tag, final byte[] item)
  {
    super(tag);
    if (tag.isConstructed()) {
      throw new IllegalArgumentException("DER tag must not be constructed");
    }
    derItem = item;
  }


  @Override
  public byte[] encode()
  {
    return encode(derItem);
  }


  /**
   * Converts bytes in the buffer to a string by reading from the current position to the limit, which assumes the bytes
   * of the string are in big-endian order.
   *
   * @param  encoded  buffer containing DER-encoded data where the buffer is positioned at the start of string bytes and
   *                  the limit is set beyond the last byte of string data.
   *
   * @return  decoded bytes as an string
   */
  public static String decode(final DERBuffer encoded)
  {
    return new String(encoded.getRemainingBytes(), StandardCharsets.UTF_8);
  }


  /**
   * Converts the supplied string to a byte array using the UTF-8 encoding.
   *
   * @param  s  to convert
   *
   * @return  byte array
   */
  public static byte[] toBytes(final String s)
  {
    return s.getBytes(StandardCharsets.UTF_8);
  }
}
