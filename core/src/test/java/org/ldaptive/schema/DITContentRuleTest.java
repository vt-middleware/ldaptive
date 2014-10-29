/*
  $Id: DITContentRuleTest.java 3005 2014-07-02 14:20:47Z dfisher $

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
 * Unit tests for {@link DITContentRule}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
 */
public class DITContentRuleTest
{


  /**
   * Test data for DIT content rule.
   *
   * @return  DIT content rule and string definition
   */
  @DataProvider(name = "definitions")
  public Object[][] createDefinitions()
  {
    return
      new Object[][] {
        new Object[] {
          new DITContentRule(
            "12.16.840.1.113730.3.2.2",
            null,
            null,
            false,
            null,
            null,
            null,
            null,
            null),
          "( 12.16.840.1.113730.3.2.2 )",
        },
        new Object[] {
          new DITContentRule(
            "12.16.840.1.113730.3.2.2",
            new String[] {"dcrPerson"},
            null,
            false,
            null,
            null,
            null,
            null,
            null),
          "( 12.16.840.1.113730.3.2.2 NAME 'dcrPerson' )",
        },
        new Object[] {
          new DITContentRule(
            "12.16.840.1.113730.3.2.2",
            new String[] {"dcrPerson"},
            "inetOrgPerson entries may only be members of the uidObject aux " +
              "class",
            false,
            null,
            null,
            null,
            null,
            null),
          "( 12.16.840.1.113730.3.2.2 NAME 'dcrPerson' " +
            "DESC 'inetOrgPerson entries may only be members of the " +
            "uidObject aux class' )",
        },
        new Object[] {
          new DITContentRule(
            "12.16.840.1.113730.3.2.2",
            new String[] {"dcrPerson"},
            "inetOrgPerson entries may only be members of the uidObject aux " +
              "class",
            false,
            new String[] {"1.3.6.1.1.3.1"},
            null,
            null,
            null,
            null),
          "( 12.16.840.1.113730.3.2.2 NAME 'dcrPerson' " +
            "DESC 'inetOrgPerson entries may only be members of the " +
            "uidObject aux class' AUX 1.3.6.1.1.3.1 )",
        },
      };
  }


  /**
   * @param  contentRule  to compare
   * @param  definition  to parse
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"schema"},
    dataProvider = "definitions"
  )
  public void parse(final DITContentRule contentRule, final String definition)
    throws Exception
  {
    final DITContentRule parsed = DITContentRule.parse(definition);
    Assert.assertEquals(contentRule, parsed);
    Assert.assertEquals(definition, parsed.format());
    Assert.assertEquals(contentRule.format(), parsed.format());
  }
}
