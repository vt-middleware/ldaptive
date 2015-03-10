/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link MatchingRule}.
 *
 * @author  Middleware Services
 */
public class MatchingRuleTest
{


  /**
   * Test data for matching rule.
   *
   * @return  matching rule and string definition
   */
  @DataProvider(name = "definitions")
  public Object[][] createDefinitions()
  {
    return
      new Object[][] {
        new Object[] {
          new MatchingRule("1.3.6.1.1.16.3", null, null, false, null, null),
          "( 1.3.6.1.1.16.3 )",
        },
        new Object[] {
          new MatchingRule("1.3.6.1.1.16.3", new String[] {"UUIDOrderingMatch"}, null, false, null, null),
          "( 1.3.6.1.1.16.3 NAME 'UUIDOrderingMatch' )",
        },
        new Object[] {
          new MatchingRule("1.3.6.1.1.16.3", new String[] {"UUIDOrderingMatch"}, null, false, "1.3.6.1.1.16.1", null),
          "( 1.3.6.1.1.16.3 NAME 'UUIDOrderingMatch' SYNTAX 1.3.6.1.1.16.1 )",
        },
        new Object[] {
          new MatchingRule(
            "2.5.13.27",
            new String[] {"generalizedTimeMatch"},
            null,
            false,
            "1.3.6.1.4.1.1466.115.121.1.24",
            null),
          "( 2.5.13.27 NAME 'generalizedTimeMatch' SYNTAX 1.3.6.1.4.1.1466.115.121.1.24 )",
        },
        new Object[] {
          new MatchingRule(
            "2.5.13.6",
            new String[] {"caseExactOrderingMatch"},
            null,
            false,
            "1.3.6.1.4.1.1466.115.121.1.15",
            null),
          "( 2.5.13.6 NAME 'caseExactOrderingMatch' SYNTAX 1.3.6.1.4.1.1466.115.121.1.15 )",
        },
      };
  }


  /**
   * @param  matchingRule  to compare
   * @param  definition  to parse
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"schema"}, dataProvider = "definitions")
  public void parse(final MatchingRule matchingRule, final String definition)
    throws Exception
  {
    final MatchingRule parsed = MatchingRule.parse(definition);
    Assert.assertEquals(matchingRule, parsed);
    Assert.assertEquals(definition, parsed.format());
    Assert.assertEquals(matchingRule.format(), parsed.format());
  }
}
