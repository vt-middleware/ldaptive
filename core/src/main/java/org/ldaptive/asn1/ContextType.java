/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.math.BigInteger;

/**
 * Converts context types to their DER encoded format.
 *
 * @author  Middleware Services
 */
public class ContextType extends AbstractDERType implements DEREncoder
{

  /** Data to encode. */
  private final byte[] derItem;


  /**
   * Creates a new context type.
   *
   * @param  index  of this item in the context
   * @param  item  to encode
   */
  public ContextType(final int index, final byte[] item)
  {
    super(new ContextDERTag(index, false));
    derItem = item;
  }


  /**
   * Creates a new context type.
   *
   * @param  index  of this item in the context
   * @param  item  to encode
   */
  public ContextType(final int index, final String item)
  {
    this(index, OctetStringType.toBytes(item));
  }


  /**
   * Creates a new context type.
   *
   * @param  index  of this item in the context
   * @param  item  to encode
   */
  public ContextType(final int index, final boolean item)
  {
    this(index, BooleanType.toBytes(item));
  }


  /**
   * Creates a new context type.
   *
   * @param  index  of this item in the context
   * @param  item  to encode
   */
  public ContextType(final int index, final BigInteger item)
  {
    this(index, IntegerType.toBytes(item));
  }


  /** {@inheritDoc} */
  @Override
  public byte[] encode()
  {
    return encode(derItem);
  }
}
