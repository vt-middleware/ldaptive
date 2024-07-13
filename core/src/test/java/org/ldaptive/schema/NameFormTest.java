/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.util.Collections;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link NameForm}.
 *
 * @author  Middleware Services
 */
public class NameFormTest
{


  /**
   * Test data for name form.
   *
   * @return  name form and string definition
   */
  @DataProvider(name = "definitions")
  public Object[][] createDefinitions()
  {
    return
      new Object[][] {
        new Object[] {
          new NameForm("1.3.6.1.1.10.15.1", null, null, false, null, null, null, null),
          "( 1.3.6.1.1.10.15.1 )",
          new DefinitionFunction[] {new NameForm.DefaultDefinitionFunction(), new NameForm.RegexDefinitionFunction()},
        },
        new Object[] {
          new NameForm(
            "1.3.6.1.1.10.15.1",
            new String[] {"uddiBusinessEntityNameForm"},
            null,
            false,
            null,
            null,
            null,
            null),
          "( 1.3.6.1.1.10.15.1 NAME 'uddiBusinessEntityNameForm' )",
          new DefinitionFunction[] {new NameForm.DefaultDefinitionFunction(), new NameForm.RegexDefinitionFunction()},
        },
        new Object[] {
          new NameForm(
            "1.3.6.1.1.10.15.1",
            new String[] {"uddiBusinessEntityNameForm"},
            null,
            false,
            "uddiBusinessEntity",
            null,
            null,
            null),
          "( 1.3.6.1.1.10.15.1 NAME 'uddiBusinessEntityNameForm' OC uddiBusinessEntity )",
          new DefinitionFunction[] {new NameForm.DefaultDefinitionFunction(), new NameForm.RegexDefinitionFunction()},
        },
        new Object[] {
          new NameForm(
            "1.3.6.1.1.10.15.1",
            new String[] {"uddiBusinessEntityNameForm"},
            null,
            false,
            "uddiBusinessEntity",
            new String[] {"uddiBusinessKey"},
            null,
            null),
          "( 1.3.6.1.1.10.15.1 NAME 'uddiBusinessEntityNameForm' OC uddiBusinessEntity MUST uddiBusinessKey )",
          new DefinitionFunction[] {new NameForm.DefaultDefinitionFunction(), new NameForm.RegexDefinitionFunction()},
        },
        new Object[] {
          new NameForm(
            "1.3.6.1.1.10.15.1",
            new String[] {"uddiBusinessEntityNameForm"},
            null,
            false,
            "uddiBusinessEntity",
            new String[] {"uddiBusinessKey"},
            null,
            new Extensions("X-ORIGIN", Collections.singletonList("RFC 4403"))),
          "( 1.3.6.1.1.10.15.1 NAME 'uddiBusinessEntityNameForm' OC uddiBusinessEntity MUST uddiBusinessKey " +
            "X-ORIGIN 'RFC 4403' )",
          new DefinitionFunction[] {new NameForm.DefaultDefinitionFunction(), new NameForm.RegexDefinitionFunction()},
        },
      };
  }


  /**
   * @param  nameForm  to compare
   * @param  definition  to parse
   * @param  functions  to parse the definition
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "schema", dataProvider = "definitions")
  public void parse(final NameForm nameForm, final String definition, final DefinitionFunction<NameForm>[] functions)
    throws Exception
  {
    for (DefinitionFunction<NameForm> func : functions) {
      final NameForm parsed = func.parse(definition);
      assertThat(parsed).isEqualTo(nameForm);
      assertThat(parsed.format()).isEqualTo(definition);
      assertThat(parsed.format()).isEqualTo(nameForm.format());
    }
  }
}
