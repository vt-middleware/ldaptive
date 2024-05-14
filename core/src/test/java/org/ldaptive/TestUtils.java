/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.Assert;

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
  public static void testImmutable(final Immutable i)
  {
    try {
      i.checkImmutable();
      Assert.fail("Should have thrown exception for immutable: " + i);
    } catch (Exception e) {
      Assert.assertEquals(IllegalStateException.class, e.getClass());
    }
  }
}
