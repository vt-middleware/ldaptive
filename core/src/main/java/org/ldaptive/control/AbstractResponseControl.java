/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import java.util.concurrent.atomic.AtomicBoolean;
import org.ldaptive.Freezable;

/**
 * Base class for response controls.
 *
 * @author  Middleware Services
 */
public abstract class AbstractResponseControl extends AbstractControl implements ResponseControl, Freezable
{

  /** Whether this object has been marked immutable. */
  private final AtomicBoolean immutable = new AtomicBoolean();


  /**
   * Creates a new abstract response control.
   *
   * @param  id  OID of this control
   */
  public AbstractResponseControl(final String id)
  {
    super(id);
  }


  /**
   * Creates a new abstract response control.
   *
   * @param  id  OID of this control
   * @param  b  whether this control is critical
   */
  public AbstractResponseControl(final String id, final boolean b)
  {
    super(id, b);
  }


  @Override
  public void freeze()
  {
    immutable.set(true);
  }


  @Override
  public boolean isFrozen()
  {
    return immutable.get();
  }


  @Override
  public void assertMutable()
  {
    if (immutable.get()) {
      throw new IllegalStateException("Cannot modify immutable object");
    }
  }


  /**
   * Sets this control as immutable, throws if the control has already been frozen.
   *
   * @throws  IllegalStateException  if the control has already been frozen
   */
  protected void freezeAndAssertMutable()
  {
    if (!immutable.compareAndSet(false, true)) {
      throw new IllegalStateException("Cannot modify immutable object");
    }
  }
}
