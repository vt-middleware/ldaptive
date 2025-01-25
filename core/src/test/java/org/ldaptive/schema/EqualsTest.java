/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

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
   * Schema classes.
   *
   * @return  test data
   */
  @DataProvider(name = "schema-classes")
  public Object[][] schemaClasses()
  {
    return
      new Object[][] {
        new Object[] {
          AttributeType.class,
        },
        new Object[] {
          DITContentRule.class,
        },
        new Object[] {
          DITStructureRule.class,
        },
        new Object[] {
          Extensions.class,
        },
        new Object[] {
          MatchingRule.class,
        },
        new Object[] {
          MatchingRuleUse.class,
        },
        new Object[] {
          NameForm.class,
        },
        new Object[] {
          ObjectClass.class,
        },
        new Object[] {
          Schema.class,
        },
        new Object[] {
          Syntax.class,
        },
      };
  }


  @Test(dataProvider = "schema-classes")
  public void schemas(final Class<?> clazz)
  {
    EqualsVerifier.forClass(clazz)
      .suppress(Warning.STRICT_INHERITANCE)
      .suppress(Warning.NONFINAL_FIELDS)
      .withIgnoredFields("immutable")
      .verify();
  }
}
