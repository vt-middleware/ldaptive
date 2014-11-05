/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Converts strings to and from their DER encoded format.
 *
 * @author  Middleware Services
 * @version  $Revision: 3049 $ $Date: 2014-09-03 15:30:49 -0400 (Wed, 03 Sep 2014) $
 */
public class OctetStringType extends AbstractDERType implements DEREncoder
{

  /** Character set for this string type. */
  private static final Charset CHARSET = Charset.forName("UTF-8");

  /** String to encode. */
  private final byte[] derItem;


  /**
   * Creates a new octet string type.
   *
   * @param  item  to DER encode
   */
  public OctetStringType(final String item)
  {
    this(item.getBytes(CHARSET));
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
    this(tag, item.getBytes(CHARSET));
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


  /** {@inheritDoc} */
  @Override
  public byte[] encode()
  {
    return encode(derItem);
  }


  /**
   * Converts bytes in the buffer to a string by reading from the current
   * position to the limit, which assumes the bytes of the string are in
   * big-endian order.
   *
   * @param  encoded  buffer containing DER-encoded data where the buffer is
   * positioned at the start of string bytes and the limit is set beyond the
   * last byte of string data.
   *
   * @return  decoded bytes as an string
   */
  public static String decode(final ByteBuffer encoded)
  {
    return new String(readBuffer(encoded), CHARSET);
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
    return s.getBytes(CHARSET);
  }
}
