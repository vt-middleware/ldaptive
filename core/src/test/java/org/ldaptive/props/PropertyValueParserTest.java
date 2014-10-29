/*
  $Id: PropertyValueParserTest.java 3005 2014-07-02 14:20:47Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3005 $
  Updated: $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive.props;

import org.ldaptive.handler.MergeAttributeEntryHandler;
import org.ldaptive.handler.RecursiveEntryHandler;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.sasl.CramMd5Config;
import org.ldaptive.sasl.Mechanism;
import org.ldaptive.sasl.QualityOfProtection;
import org.ldaptive.sasl.SaslConfig;
import org.ldaptive.sasl.SecurityStrength;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link PropertyValueParser}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
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
    final String p1 = "org.ldaptive.handler.RecursiveEntryHandler" +
      "{{searchAttribute=member}{mergeAttributes=mail,department}}";
    final RecursiveEntryHandler o1 = new RecursiveEntryHandler();
    o1.setSearchAttribute("member");
    o1.setMergeAttributes("mail", "department");

    final String p2 = "org.ldaptive.handler.MergeAttributeEntryHandler{ }";
    final MergeAttributeEntryHandler o2 = new MergeAttributeEntryHandler();

    final String p3 =
      "org.ldaptive.pool.IdlePruneStrategy{{prunePeriod=60}{idleTime=120}";
    final IdlePruneStrategy o3 = new IdlePruneStrategy();
    o3.setPrunePeriod(60);
    o3.setIdleTime(120);

    final String p4 = "org.ldaptive.sasl.CramMd5Config" +
      "{{securityStrength=LOW}{qualityOfProtection=AUTH}}";
    final CramMd5Config o4 = new CramMd5Config();
    o4.setSecurityStrength(SecurityStrength.LOW);
    o4.setQualityOfProtection(QualityOfProtection.AUTH);

    final String p5 = "{{mechanism=DIGEST_MD5}{authorizationId=test1}}";
    final SaslConfig o5 = new SaslConfig();
    o5.setMechanism(Mechanism.DIGEST_MD5);
    o5.setAuthorizationId("test1");

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
  @Test(
    groups = {"props"},
    dataProvider = "properties"
  )
  public void initializeType(
    final String property,
    final Class<?> type,
    final Object initialized)
    throws Exception
  {
    PropertyValueParser parser = null;
    if (type != null) {
      parser = new PropertyValueParser(property, type.getName());
      Assert.assertTrue(PropertyValueParser.isParamsOnlyConfig(property));
    } else {
      parser = new PropertyValueParser(property);
      Assert.assertTrue(PropertyValueParser.isConfig(property));
    }

    final Object o = parser.initializeType();
    Assert.assertEquals(
      initialized.toString().split("::")[1],
      o.toString().split("::")[1]);
  }
}
