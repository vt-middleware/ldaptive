/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.time.Duration;
import org.ldaptive.handler.MergeAttributeEntryHandler;
import org.ldaptive.handler.RecursiveResultHandler;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.sasl.Mechanism;
import org.ldaptive.sasl.QualityOfProtection;
import org.ldaptive.sasl.SaslConfig;
import org.ldaptive.sasl.SecurityStrength;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link PropertyValueParser}.
 *
 * @author  Middleware Services
 */
public class PropertyValueParserTest
{


  /**
   * Property test data.
   *
   * @return  configuration properties
   */
  @DataProvider(name = "properties")
  public Object[][] createProperties()
  {
    final String p1 = "org.ldaptive.handler.RecursiveResultHandler" +
      "{{searchAttribute=member}{mergeAttributes=mail,department}}";
    final RecursiveResultHandler o1 = new RecursiveResultHandler();
    o1.setSearchAttribute("member");
    o1.setMergeAttributes("mail", "department");

    final String p2 = "org.ldaptive.handler.MergeAttributeEntryHandler{ }";
    final MergeAttributeEntryHandler o2 = new MergeAttributeEntryHandler();

    final String p3 = "org.ldaptive.pool.IdlePruneStrategy{{prunePeriod=PT1M}{idleTime=PT2M}";
    final IdlePruneStrategy o3 = new IdlePruneStrategy();
    o3.setPrunePeriod(Duration.ofMinutes(1));
    o3.setIdleTime(Duration.ofMinutes(2));

    final String p4 = "org.ldaptive.sasl.SaslConfig{{mechanism=CRAM_MD5}}";
    final SaslConfig o4 = new SaslConfig();
    o4.setMechanism(Mechanism.CRAM_MD5);

    final String p5 = "{{mechanism=DIGEST_MD5}" +
      "{authorizationId=test1}{mutualAuthentication=true}{qualityOfProtection=AUTH}{securityStrength=HIGH,MEDIUM}}";
    final SaslConfig o5 = new SaslConfig();
    o5.setMechanism(Mechanism.DIGEST_MD5);
    o5.setAuthorizationId("test1");
    o5.setMutualAuthentication(true);
    o5.setQualityOfProtection(QualityOfProtection.AUTH);
    o5.setSecurityStrength(SecurityStrength.HIGH, SecurityStrength.MEDIUM);

    final String p6 = "{mechanism=EXTERNAL}";
    final SaslConfig o6 = new SaslConfig();
    o6.setMechanism(Mechanism.EXTERNAL);

    return
      new Object[][] {
        new Object[] {p1, null, o1, },
        new Object[] {p2, null, o2, },
        new Object[] {p3, null, o3, },
        new Object[] {p4, null, o4, },
        new Object[] {p5, SaslConfig.class, o5, },
        new Object[] {p6, SaslConfig.class, o6, },
      };
  }


  /**
   * @param  property  to test
   * @param  type  of object or null
   * @param  initialized  object to compare
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "props", dataProvider = "properties")
  public void initializeType(final String property, final Class<?> type, final Object initialized)
    throws Exception
  {
    final PropertyValueParser parser;
    if (type != null) {
      parser = new PropertyValueParser(property, type.getName());
      assertThat(PropertyValueParser.isParamsOnlyConfig(property)).isTrue();
    } else {
      parser = new PropertyValueParser(property);
      assertThat(PropertyValueParser.isConfig(property)).isTrue();
    }

    final Object o = parser.initializeType();
    assertThat(initialized.toString().split("::")[1]).isEqualTo(o.toString().split("::")[1]);
  }
}
