/*
  $Id: MatchingRuleTest.java 3005 2014-07-02 14:20:47Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3005 $
  Updated: $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive.schema;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link MatchingRule}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
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
          new MatchingRule(
            "1.3.6.1.1.16.3",
            new String[] {"UUIDOrderingMatch"},
            null,
            false,
            null,
            null),
          "( 1.3.6.1.1.16.3 NAME 'UUIDOrderingMatch' )",
        },
        new Object[] {
          new MatchingRule(
            "1.3.6.1.1.16.3",
            new String[] {"UUIDOrderingMatch"},
            null,
            false,
            "1.3.6.1.1.16.1",
            null),
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
          "( 2.5.13.27 NAME 'generalizedTimeMatch' " +
            "SYNTAX 1.3.6.1.4.1.1466.115.121.1.24 )",
        },
        new Object[] {
          new MatchingRule(
            "2.5.13.6",
            new String[] {"caseExactOrderingMatch"},
            null,
            false,
            "1.3.6.1.4.1.1466.115.121.1.15",
            null),
          "( 2.5.13.6 NAME 'caseExactOrderingMatch' " +
            "SYNTAX 1.3.6.1.4.1.1466.115.121.1.15 )",
        },
      };
  }


  /**
   * @param  matchingRule  to compare
   * @param  definition  to parse
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"schema"},
    dataProvider = "definitions"
  )
  public void parse(final MatchingRule matchingRule, final String definition)
    throws Exception
  {
    final MatchingRule parsed = MatchingRule.parse(definition);
    Assert.assertEquals(matchingRule, parsed);
    Assert.assertEquals(definition, parsed.format());
    Assert.assertEquals(matchingRule.format(), parsed.format());
  }
}
