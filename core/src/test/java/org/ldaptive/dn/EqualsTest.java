/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

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
  public void dn()
  {
    EqualsVerifier.forClass(Dn.class)
      .suppress(Warning.STRICT_INHERITANCE)
      .verify();
  }


  @Test
  public void rDn()
  {
    EqualsVerifier.forClass(RDn.class)
      .suppress(Warning.STRICT_INHERITANCE)
      .verify();
  }


  @Test
  public void nameValue()
  {
    EqualsVerifier.forClass(NameValue.class)
      .suppress(Warning.STRICT_INHERITANCE)
      .verify();
  }
}
