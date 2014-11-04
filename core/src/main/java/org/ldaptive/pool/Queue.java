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
package org.ldaptive.pool;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Provides a wrapper around a {@link Deque} to support LIFO and FIFO
 * operations.
 *
 * @param  <T>  type of object in the queue
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class Queue<T> implements Iterable<T>
{

  /** How will objects be inserted into the queue. */
  private final QueueType queueType;

  /** Underlying queue. */
  private final Deque<T> queue;


  /**
   * Creates a new queue.
   *
   * @param  type  how will objects be inserted into the queue
   */
  public Queue(final QueueType type)
  {
    queueType = type;
    queue = new LinkedList<>();
  }


  /**
   * Adds an object to the queue based on the queue type. See {@link
   * Deque#offerFirst(Object)} and {@link Deque#offerLast(Object)}.
   *
   * @param  t  to add
   */
  public void add(final T t)
  {
    if (QueueType.LIFO == queueType) {
      queue.addFirst(t);
    } else if (QueueType.FIFO == queueType) {
      queue.addLast(t);
    } else {
      throw new IllegalStateException("Unknown queue type: " + queueType);
    }
  }


  /**
   * Removes the first element in the queue. See {@link Deque#removeFirst()}.
   *
   * @return  first element in the queue
   */
  public T remove()
  {
    return queue.removeFirst();
  }


  /**
   * Removes the supplied element from the queue. See {@link
   * Deque#remove(Object)}.
   *
   * @param  t  to remove
   *
   * @return  whether t was removed
   */
  public boolean remove(final T t)
  {
    return queue.remove(t);
  }


  /**
   * Retrieves, but does not remove, the first element in the queue. See {@link
   * Deque#getFirst()}.
   *
   * @return  first element in the queue
   */
  public T element()
  {
    return queue.getFirst();
  }


  /**
   * Returns whether t is in the queue. See {@link Deque#contains(Object)}.
   *
   * @param  t  that may be in the queue
   *
   * @return  whether t is in the queue
   */
  public boolean contains(final T t)
  {
    return queue.contains(t);
  }


  /**
   * Returns whether or not the queue is empty. See {@link Deque#isEmpty()}}.
   *
   * @return  whether the queue is empty
   */
  public boolean isEmpty()
  {
    return queue.isEmpty();
  }


  /**
   * Returns the number of elements in the queue. See {@link Deque#size()}.
   *
   * @return  number of elements in the queue
   */
  public int size()
  {
    return queue.size();
  }


  /** {@inheritDoc} */
  @Override
  public Iterator<T> iterator()
  {
    return queue.iterator();
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::queueType=%s, queue=%s]",
        getClass().getName(),
        hashCode(),
        queueType,
        queue);
  }
}
