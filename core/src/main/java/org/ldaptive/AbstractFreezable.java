/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Base class for objects that can be made immutable.
 *
 * @author  Middleware Services
 */
public abstract class AbstractFreezable implements Freezable
{

  /** Whether this object has been marked immutable. */
  private volatile boolean immutable;


  @Override
  public void freeze()
  {
    immutable = true;
  }


  @Override
  public final boolean isFrozen()
  {
    return immutable;
  }


  @Override
  public final void assertMutable()
  {
    if (immutable) {
      throw new IllegalStateException("Cannot modify immutable object");
    }
  }


  /**
   * Iterates over the supplied objects and invokes {@link Freezable#freeze()} for any object that is an instance
   * of {@link Freezable}.
   *
   * @param  objects  to make immutable
   */
  protected static void freeze(final Object[] objects)
  {
    if (objects != null) {
      for (Object o : objects) {
        freeze(o);
      }
    }
  }


  /**
   * Makes the supplied object immutable if it is an instance of {@link Freezable}.
   *
   * @param  object  to make immutable
   */
  protected static void freeze(final Object object)
  {
    if (object instanceof Freezable) {
      ((Freezable) object).freeze();
    }
  }
}
