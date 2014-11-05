/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides common implementations for configuration objects.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public abstract class AbstractConfig
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Whether this config has been marked immutable. */
  private boolean immutable;


  /** Make this config immutable. */
  public void makeImmutable()
  {
    immutable = true;
  }


  /**
   * Verifies if this config is immutable.
   *
   * @throws  IllegalStateException  if this config is immutable
   */
  public void checkImmutable()
  {
    if (immutable) {
      throw new IllegalStateException("Cannot modify immutable object");
    }
  }


  /**
   * Verifies that a string is not null or empty.
   *
   * @param  s  to verify
   * @param  allowNull  whether null strings are valid
   *
   * @throws  IllegalArgumentException  if the string is null or empty
   */
  protected void checkStringInput(final String s, final boolean allowNull)
  {
    if (allowNull) {
      if (s != null && "".equals(s)) {
        throw new IllegalArgumentException("Input cannot be empty");
      }
    } else {
      if (s == null || "".equals(s)) {
        throw new IllegalArgumentException("Input cannot be null or empty");
      }
    }
  }
}
