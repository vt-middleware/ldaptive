/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link DITStructureRule}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
 */
public class DITStructureRuleTest
{


  /**
   * Test data for DIT structure rule.
   *
   * @return  DIT structure rule and string definition
   */
  @DataProvider(name = "definitions")
  public Object[][] createDefinitions()
  {
    return
      new Object[][] {
        new Object[] {
          new DITStructureRule(2, null, null, false, null, null, null),
          "( 2 )",
        },
        new Object[] {
          new DITStructureRule(
            2,
            new String[] {"uddiContactStructureRule"},
            null,
            false,
            null,
            null,
            null),
          "( 2 NAME 'uddiContactStructureRule' )",
        },
        new Object[] {
          new DITStructureRule(
            2,
            new String[] {"uddiContactStructureRule"},
            null,
            false,
            "uddiContactNameForm",
            null,
            null),
          "( 2 NAME 'uddiContactStructureRule' FORM uddiContactNameForm )",
        },
        new Object[] {
          new DITStructureRule(
            2,
            new String[] {"uddiContactStructureRule"},
            null,
            false,
            "uddiContactNameForm",
            new int[] {1},
            null),
          "( 2 NAME 'uddiContactStructureRule' FORM uddiContactNameForm " +
            "SUP 1 )",
        },
        new Object[] {
          new DITStructureRule(
            2,
            new String[] {"uddiContactStructureRule"},
            null,
            false,
            "uddiContactNameForm",
            new int[] {1},
            new Extensions("X-ORIGIN", Arrays.asList("RFC 4403"))),
          "( 2 NAME 'uddiContactStructureRule' FORM uddiContactNameForm " +
            "SUP 1 X-ORIGIN 'RFC 4403' )",
        },
        new Object[] {
          new DITStructureRule(
            1,
            new String[] {"domainStructureRule"},
            null,
            false,
            "domainNameForm",
            null,
            null),
          "( 1 NAME 'domainStructureRule' FORM domainNameForm )",
        },
        new Object[] {
          new DITStructureRule(
            2,
            new String[] {"organizationalUnitStructureRule"},
            null,
            false,
            "organizationalUnitNameForm",
            new int[] {1},
            null),
          "( 2 NAME 'organizationalUnitStructureRule' " +
            "FORM organizationalUnitNameForm SUP 1 )",
        },
        new Object[] {
          new DITStructureRule(
            3,
            new String[] {"inetOrgPersonStructureRule"},
            null,
            false,
            "inetOrgPersonNameForm",
            new int[] {2},
            null),
          "( 3 NAME 'inetOrgPersonStructureRule' FORM inetOrgPersonNameForm " +
            "SUP 2 )",
        },
        new Object[] {
          new DITStructureRule(
            4,
            new String[] {"groupOfNamesStructureRule"},
            null,
            false,
            "groupOfNamesNameForm",
            new int[] {2, 3},
            null),
          "( 4 NAME 'groupOfNamesStructureRule' FORM groupOfNamesNameForm " +
            "SUP ( 2 3 ) )",
        },
      };
  }


  /**
   * @param  structureRule  to compare
   * @param  definition  to parse
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"schema"},
    dataProvider = "definitions"
  )
  public void parse(
    final DITStructureRule structureRule,
    final String definition)
    throws Exception
  {
    final DITStructureRule parsed = DITStructureRule.parse(definition);
    Assert.assertEquals(structureRule, parsed);
    Assert.assertEquals(definition, parsed.format());
    Assert.assertEquals(structureRule.format(), parsed.format());
  }
}
