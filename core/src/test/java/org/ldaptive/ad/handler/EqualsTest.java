/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.handler;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for objects that override equals.
 *
 * @author  Middleware Services
 */
public class EqualsTest
{


  /**
   * Handler classes.
   *
   * @return  test data
   */
  @DataProvider(name = "handler-classes")
  public Object[][] handlerClasses()
  {
    return
      new Object[][] {
        new Object[] {
          ObjectGuidHandler.class,
        },
        new Object[] {
          ObjectSidHandler.class,
        },
        new Object[] {
          PrimaryGroupIdHandler.class,
        },
        new Object[] {
          RangeEntryHandler.class,
        },
      };
  }


  @Test(dataProvider = "handler-classes")
  public void handlers(final Class<?> clazz)
  {
    EqualsVerifier.forClass(clazz)
      .suppress(Warning.STRICT_INHERITANCE)
      .suppress(Warning.NONFINAL_FIELDS)
      .withIgnoredFields("logger", "connection", "request", "handle", "immutable")
      .verify();
  }
}
