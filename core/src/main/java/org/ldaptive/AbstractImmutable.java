/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Base class for objects that can be made immutable.
 *
 * @author  Middleware Services
 */
public abstract class AbstractImmutable implements Immutable
{

  /** Whether this object has been marked immutable. */
  private volatile boolean immutable;


  @Override
  public void makeImmutable()
  {
    immutable = true;
  }


  @Override
  public final boolean isImmutable()
  {
    return immutable;
  }


  @Override
  public final void checkImmutable()
  {
    if (immutable) {
      throw new IllegalStateException("Cannot modify immutable object");
    }
  }


  /**
   * Iterates over the supplied objects and invokes {@link Immutable#makeImmutable()} for any object that is an instance
   * of {@link Immutable}.
   *
   * @param  objects  to make immutable
   */
  protected static void makeImmutable(final Object[] objects)
  {
    if (objects != null) {
      for (Object o : objects) {
        makeImmutable(o);
      }
    }
  }


  /**
   * Makes the supplied object immutable if it is an instance of {@link Immutable}.
   *
   * @param  object  to make immutable
   */
  protected static void makeImmutable(final Object object)
  {
    if (object instanceof Immutable) {
      ((Immutable) object).makeImmutable();
    }
  }
}
