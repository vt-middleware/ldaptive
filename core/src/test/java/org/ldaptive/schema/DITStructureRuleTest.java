/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.util.Collections;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link DITStructureRule}.
 *
 * @author  Middleware Services
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
          new DefinitionFunction[] {
            new DITStructureRule.DefaultDefinitionFunction(),
            new DITStructureRule.RegexDefinitionFunction(),
          },
        },
        new Object[] {
          new DITStructureRule(2, new String[] {"uddiContactStructureRule"}, null, false, null, null, null),
          "( 2 NAME 'uddiContactStructureRule' )",
          new DefinitionFunction[] {
            new DITStructureRule.DefaultDefinitionFunction(),
            new DITStructureRule.RegexDefinitionFunction(),
          },
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
          new DefinitionFunction[] {
            new DITStructureRule.DefaultDefinitionFunction(),
            new DITStructureRule.RegexDefinitionFunction(),
          },
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
          "( 2 NAME 'uddiContactStructureRule' FORM uddiContactNameForm SUP 1 )",
          new DefinitionFunction[] {
            new DITStructureRule.DefaultDefinitionFunction(),
            new DITStructureRule.RegexDefinitionFunction(),
          },
        },
        new Object[] {
          new DITStructureRule(
            2,
            new String[] {"uddiContactStructureRule"},
            null,
            false,
            "uddiContactNameForm",
            new int[] {1},
            new Extensions("X-ORIGIN", Collections.singletonList("RFC 4403"))),
          "( 2 NAME 'uddiContactStructureRule' FORM uddiContactNameForm SUP 1 X-ORIGIN 'RFC 4403' )",
          new DefinitionFunction[] {
            new DITStructureRule.DefaultDefinitionFunction(),
            new DITStructureRule.RegexDefinitionFunction(),
          },
        },
        new Object[] {
          new DITStructureRule(1, new String[] {"domainStructureRule"}, null, false, "domainNameForm", null, null),
          "( 1 NAME 'domainStructureRule' FORM domainNameForm )",
          new DefinitionFunction[] {
            new DITStructureRule.DefaultDefinitionFunction(),
            new DITStructureRule.RegexDefinitionFunction(),
          },
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
          "( 2 NAME 'organizationalUnitStructureRule' FORM organizationalUnitNameForm SUP 1 )",
          new DefinitionFunction[] {
            new DITStructureRule.DefaultDefinitionFunction(),
            new DITStructureRule.RegexDefinitionFunction(),
          },
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
          "( 3 NAME 'inetOrgPersonStructureRule' FORM inetOrgPersonNameForm SUP 2 )",
          new DefinitionFunction[] {
            new DITStructureRule.DefaultDefinitionFunction(),
            new DITStructureRule.RegexDefinitionFunction(),
          },
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
          "( 4 NAME 'groupOfNamesStructureRule' FORM groupOfNamesNameForm SUP ( 2 3 ) )",
          new DefinitionFunction[] {
            new DITStructureRule.DefaultDefinitionFunction(),
            new DITStructureRule.RegexDefinitionFunction(),
          },
        },
      };
  }


  /**
   * @param  structureRule  to compare
   * @param  definition  to parse
   * @param  functions  to parse the definition
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "schema", dataProvider = "definitions")
  public void parse(
    final DITStructureRule structureRule,
    final String definition,
    final DefinitionFunction<DITStructureRule>[] functions)
    throws Exception
  {
    for (DefinitionFunction<DITStructureRule> func : functions) {
      final DITStructureRule parsed = func.parse(definition);
      assertThat(parsed).isEqualTo(structureRule);
      assertThat(parsed.format()).isEqualTo(definition);
      assertThat(parsed.format()).isEqualTo(structureRule.format());
    }
  }
}
