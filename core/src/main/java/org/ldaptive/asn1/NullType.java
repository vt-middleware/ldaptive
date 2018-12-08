/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

/**
 * Convenience type for a tag with a null value.
 *
 * @author  Middleware Services
 */
public class NullType extends AbstractDERType implements DEREncoder
{


  /**
   * Creates a new null type.
   *
   * @param  tag  der tag associated with this type
   */
  public NullType(final DERTag tag)
  {
    super(tag);
  }


  @Override
  public byte[] encode()
  {
    return encode((byte[]) null);
  }
}
