/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.url;

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
  public void url()
  {
    EqualsVerifier.forClass(Url.class)
      .suppress(Warning.STRICT_INHERITANCE)
      .suppress(Warning.NONFINAL_FIELDS)
      .verify();
  }
}
