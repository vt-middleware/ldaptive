/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

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
   * Filter classes.
   *
   * @return  test data
   */
  @DataProvider(name = "filter-classes")
  public Object[][] filterClasses()
  {
    return
      new Object[][] {
        new Object[] {
          AndFilter.class,
        },
        new Object[] {
          ApproximateFilter.class,
        },
        new Object[] {
          EqualityFilter.class,
        },
        new Object[] {
          ExtensibleFilter.class,
        },
        new Object[] {
          GreaterOrEqualFilter.class,
        },
        new Object[] {
          LessOrEqualFilter.class,
        },
        new Object[] {
          NotFilter.class,
        },
        new Object[] {
          OrFilter.class,
        },
        new Object[] {
          PresenceFilter.class,
        },
        new Object[] {
          SubstringFilter.class,
        },
      };
  }


  @Test(dataProvider = "filter-classes")
  public void filters(final Class<?> clazz)
  {
    EqualsVerifier.forClass(clazz)
      .suppress(Warning.STRICT_INHERITANCE)
      .suppress(Warning.NONFINAL_FIELDS)
      .verify();
  }
}
