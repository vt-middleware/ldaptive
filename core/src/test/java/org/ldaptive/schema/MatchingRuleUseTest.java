/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link MatchingRuleUse}.
 *
 * @author  Middleware Services
 */
public class MatchingRuleUseTest
{


  /**
   * Test data for matching rule use.
   *
   * @return  matching rule use and string definition
   */
  @DataProvider(name = "definitions")
  public Object[][] createDefinitions()
  {
    return
      new Object[][] {
        new Object[] {
          new MatchingRuleUse("1.2.840.113556.1.4.804", null, null, false, null, null),
          "( 1.2.840.113556.1.4.804 )",
          new DefinitionFunction[] {
            new MatchingRuleUse.DefaultDefinitionFunction(),
            new MatchingRuleUse.RegexDefinitionFunction(),
          },
        },
        new Object[] {
          new MatchingRuleUse("1.2.840.113556.1.4.804", new String[] {"integerBitOrMatch"}, null, false, null, null),
          "( 1.2.840.113556.1.4.804 NAME 'integerBitOrMatch' )",
          new DefinitionFunction[] {
            new MatchingRuleUse.DefaultDefinitionFunction(),
            new MatchingRuleUse.RegexDefinitionFunction(),
          },
        },
        new Object[] {
          new MatchingRuleUse(
            "1.2.840.113556.1.4.804",
            new String[] {"integerBitOrMatch"},
            null,
            false,
            new String[] {
              "supportedLDAPVersion",
              "entryTtl",
              "uidNumber",
              "gidNumber",
              "olcConcurrency",
              "olcConnMaxPending",
              "olcConnMaxPendingAuth",
              "olcIdleTimeout",
              "olcIndexSubstrIfMinLen",
              "olcIndexSubstrIfMaxLen",
              "olcIndexSubstrAnyLen",
              "olcIndexSubstrAnyStep",
              "olcIndexIntLen",
              "olcListenerThreads",
              "olcLocalSSF",
              "olcMaxDerefDepth",
              "olcReplicationInterval",
              "olcSockbufMaxIncoming",
              "olcSockbufMaxIncomingAuth",
              "olcThreads",
              "olcToolThreads",
              "olcWriteTimeout",
              "olcDbCacheFree",
              "olcDbCacheSize",
              "olcDbDNcacheSize",
              "olcDbIDLcacheSize",
              "olcDbSearchStack",
              "olcDbShmKey",
              "olcDbMaxReaders",
              "olcDbMaxSize",
              "mailPreferenceOption",
            },
            null),
          "( 1.2.840.113556.1.4.804 NAME 'integerBitOrMatch' APPLIES ( supportedLDAPVersion $ entryTtl $ uidNumber $ " +
            "gidNumber $ olcConcurrency $ olcConnMaxPending $ olcConnMaxPendingAuth $ olcIdleTimeout $ " +
            "olcIndexSubstrIfMinLen $ olcIndexSubstrIfMaxLen $ olcIndexSubstrAnyLen $ olcIndexSubstrAnyStep $ " +
            "olcIndexIntLen $ olcListenerThreads $ olcLocalSSF $ olcMaxDerefDepth $ olcReplicationInterval $ " +
            "olcSockbufMaxIncoming $ olcSockbufMaxIncomingAuth $ olcThreads $ olcToolThreads $ olcWriteTimeout $ " +
            "olcDbCacheFree $ olcDbCacheSize $ olcDbDNcacheSize $ olcDbIDLcacheSize $ olcDbSearchStack $ " +
            "olcDbShmKey $ olcDbMaxReaders $ olcDbMaxSize $ mailPreferenceOption ) )",
          new DefinitionFunction[] {
            new MatchingRuleUse.DefaultDefinitionFunction(),
            new MatchingRuleUse.RegexDefinitionFunction(),
          },
        },
      };
  }


  /**
   * @param  matchingRule  to compare
   * @param  definition  to parse
   * @param  functions  to parse the definition
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "schema", dataProvider = "definitions")
  public void parse(
    final MatchingRuleUse matchingRule, final String definition, final DefinitionFunction<MatchingRuleUse>[] functions)
    throws Exception
  {
    for (DefinitionFunction<MatchingRuleUse> func : functions) {
      final MatchingRuleUse parsed = func.parse(definition);
      assertThat(parsed).isEqualTo(matchingRule);
      assertThat(parsed.format()).isEqualTo(definition);
      assertThat(parsed.format()).isEqualTo(matchingRule.format());
    }
  }
}
