/*
  $Id: SyntaxTest.java 3005 2014-07-02 14:20:47Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3005 $
  Updated: $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive.schema;

import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link Syntax}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
 */
public class SyntaxTest
{


  /**
   * Test data for attribute syntax.
   *
   * @return  attribute syntax and string definition
   */
  @DataProvider(name = "definitions")
  public Object[][] createDefinitions()
  {
    return
      new Object[][] {
        new Object[] {
          new Syntax("1.3.6.1.4.1.1466.115.121.1.5", null, null),
          "( 1.3.6.1.4.1.1466.115.121.1.5 )",
        },
        new Object[] {
          new Syntax("1.3.6.1.4.1.1466.115.121.1.5", "Binary", null),
          "( 1.3.6.1.4.1.1466.115.121.1.5 DESC 'Binary' )",
        },
        new Object[] {
          new Syntax(
            "1.3.6.1.4.1.1466.115.121.1.5",
            "Binary",
            new Extensions("X-NOT-HUMAN-READABLE", Arrays.asList("TRUE"))),
          "( 1.3.6.1.4.1.1466.115.121.1.5 DESC 'Binary' " +
            "X-NOT-HUMAN-READABLE 'TRUE' )",
        },
      };
  }


  /**
   * @param  attributeSyntax  to compare
   * @param  definition  to parse
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"schema"},
    dataProvider = "definitions"
  )
  public void parse(final Syntax attributeSyntax, final String definition)
    throws Exception
  {
    final Syntax parsed = Syntax.parse(definition);
    Assert.assertEquals(attributeSyntax, parsed);
    Assert.assertEquals(definition, parsed.format());
    Assert.assertEquals(attributeSyntax.format(), parsed.format());
  }
}
