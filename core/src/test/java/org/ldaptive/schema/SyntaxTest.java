/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.util.Collections;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link Syntax}.
 *
 * @author  Middleware Services
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
          new DefinitionFunction[] {new Syntax.DefaultDefinitionFunction(), new Syntax.RegexDefinitionFunction()},
        },
        new Object[] {
          new Syntax("1.3.6.1.4.1.1466.115.121.1.5", "Binary", null),
          "( 1.3.6.1.4.1.1466.115.121.1.5 DESC 'Binary' )",
          new DefinitionFunction[] {new Syntax.DefaultDefinitionFunction(), new Syntax.RegexDefinitionFunction()},
        },
        new Object[] {
          new Syntax(
            "1.3.6.1.4.1.1466.115.121.1.5",
            "Binary",
            new Extensions("X-NOT-HUMAN-READABLE", Collections.singletonList("TRUE"))),
          "( 1.3.6.1.4.1.1466.115.121.1.5 DESC 'Binary' X-NOT-HUMAN-READABLE 'TRUE' )",
          new DefinitionFunction[] {new Syntax.DefaultDefinitionFunction(), new Syntax.RegexDefinitionFunction()},
        },
      };
  }


  /**
   * @param  attributeSyntax  to compare
   * @param  definition  to parse
   * @param  functions  to parse the definition
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "schema", dataProvider = "definitions")
  public void parse(
    final Syntax attributeSyntax, final String definition, final DefinitionFunction<Syntax>[] functions)
    throws Exception
  {
    for (DefinitionFunction<Syntax> func : functions) {
      final Syntax parsed = func.parse(definition);
      assertThat(parsed).isEqualTo(attributeSyntax);
      assertThat(parsed.format()).isEqualTo(definition);
      assertThat(parsed.format()).isEqualTo(attributeSyntax.format());
    }
  }
}
