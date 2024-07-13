/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import static org.assertj.core.api.Assertions.*;

/**
 * Utility methods for ldap tests.
 *
 * @author  Middleware Services
 */
public final class TestUtils
{

  /** Default constructor. */
  private TestUtils() {}


  /**
   * Confirms that the supplied object has been marked immutable.
   *
   * @param  i  immutable to test
   */
  public static void testImmutable(final Freezable i)
  {
    try {
      i.assertMutable();
      fail("Should have thrown exception for immutable: %s", i);
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
    }
  }
}
