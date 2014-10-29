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
package org.ldaptive.beans.reflect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Reflection transcoder for an object that implements {@link List}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2917 $ $Date: 2014-03-21 16:06:36 -0400 (Fri, 21 Mar 2014) $
 */
public class ListReflectionTranscoder
  extends AbstractCollectionReflectionTranscoder
{


  /**
   * Creates a new list reflection transcoder.
   *
   * @param  c  class that is a list
   * @param  transcoder  to operate on elements of the list
   */
  public ListReflectionTranscoder(
    final Class<?> c,
    final SingleValueReflectionTranscoder<?> transcoder)
  {
    super(c, transcoder);
  }


  /**
   * Creates a new list reflection transcoder.
   *
   * @param  c  class that is a list
   * @param  transcoder  to operate on elements of the list
   */
  public ListReflectionTranscoder(
    final Class<?> c,
    final ArrayReflectionTranscoder transcoder)
  {
    super(c, transcoder);
  }


  /** {@inheritDoc} */
  @Override
  protected <T> Collection<T> createCollection(final Class<T> clazz)
  {
    final Class<?> type = getType();
    List<T> l;
    if (LinkedList.class.isAssignableFrom(type)) {
      l = new LinkedList<T>();
    } else {
      l = new ArrayList<T>();
    }
    return l;
  }
}
