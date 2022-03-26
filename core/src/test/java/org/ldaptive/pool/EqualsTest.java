/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.testng.annotations.Test;

/**
 * Unit test for objects that override equals.
 *
 * @author  Middleware Services
 */
public class EqualsTest
{


  @Test
  public void defaultPooledConnectionProxy()
  {
    EqualsVerifier.forClass(AbstractConnectionPool.DefaultPooledConnectionProxy.class)
      .suppress(Warning.STRICT_INHERITANCE)
      .withIgnoredFields("createdTime", "statistics")
      .verify();
  }
}
