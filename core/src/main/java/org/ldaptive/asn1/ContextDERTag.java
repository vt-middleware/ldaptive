/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive.asn1;

/**
 * Generic context-specific tag.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $
 */
public class ContextDERTag extends AbstractDERTag
{

  /** Generic tag name "CTX" for a context-specific type. */
  public static final String TAG_NAME = "CTX";

  /** Context-specific class is 10b in first two high-order bits. */
  public static final int TAG_CLASS = 0x80;


  /**
   * Creates a new context-specific tag with given tag number.
   *
   * @param  number  Tag number.
   * @param  isConstructed  True for constructed tag, false otherwise.
   */
  public ContextDERTag(final int number, final boolean isConstructed)
  {
    super(number, isConstructed);
  }


  /** {@inheritDoc} */
  @Override
  public int getTagByte()
  {
    return super.getTagByte() | TAG_CLASS;
  }


  /** {@inheritDoc} */
  @Override
  public String name()
  {
    return String.format("%s(%s)", TAG_NAME, getTagNo());
  }
}
