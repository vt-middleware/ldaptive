/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.reflect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Reflection transcoder for an object that implements {@link List}.
 *
 * @author  Middleware Services
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


  @Override
  protected <T> Collection<T> createCollection(final Class<T> clazz)
  {
    final Class<?> type = getType();
    List<T> l;
    if (LinkedList.class.isAssignableFrom(type)) {
      l = new LinkedList<>();
    } else {
      l = new ArrayList<>();
    }
    return l;
  }
}
