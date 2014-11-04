/*
  $Id: BeanGeneratorTest.java 3040 2014-08-21 14:18:51Z dfisher $

  Copyright (C) 2003-2010 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3040 $
  Updated: $Date: 2014-08-21 10:18:51 -0400 (Thu, 21 Aug 2014) $
*/
package org.ldaptive.beans.generate;

import org.ldaptive.AbstractTest;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.TestControl;
import org.ldaptive.TestUtils;
import org.ldaptive.schema.Schema;
import org.ldaptive.schema.SchemaFactory;
import org.testng.annotations.Test;

/**
 * Unit test for {@link BeanGenerator}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3040 $ $Date: 2014-08-21 10:18:51 -0400 (Thu, 21 Aug 2014) $
 */
public class BeanGeneratorTest extends AbstractTest
{


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-generate"})
  public void generate()
    throws Exception
  {
    if (TestControl.isActiveDirectory()) {
      return;
    }
    final DefaultConnectionFactory factory = new DefaultConnectionFactory(
      TestUtils.readConnectionConfig(
        "classpath:/org/ldaptive/ldap.setup.properties"));
    final Schema schema = SchemaFactory.createSchema(factory);
    final BeanGenerator generator = new BeanGenerator();
    generator.setSchema(schema);
    generator.setExcludedNames("objectClass");
    generator.setObjectClasses("inetOrgPerson");
    generator.setPackageName("org.ldaptive.beans.schema");
    generator.setUseOperationalAttributes(true);
    generator.setUseOptionalAttributes(true);
    generator.setIncludeSuperiorClasses(true);
    generator.generate();
    generator.write("target/generated-test-sources/ldaptive");
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-generate"})
  public void generateAD()
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
      return;
    }
    final DefaultConnectionFactory factory = new DefaultConnectionFactory(
      TestUtils.readConnectionConfig(
        "classpath:/org/ldaptive/ldap.setup.properties"));
    final Schema schema = org.ldaptive.ad.schema.SchemaFactory.createSchema(
      factory,
      "CN=Schema,CN=Configuration,DC=middleware,DC=vt,DC=edu");
    final BeanGenerator generator = new BeanGenerator();
    generator.setSchema(schema);
    generator.setExcludedNames("objectClass");
    generator.setObjectClasses("organizationalPerson");
    generator.setPackageName("org.ldaptive.beans.schema");
    generator.setUseOperationalAttributes(true);
    generator.setUseOptionalAttributes(true);
    generator.setIncludeSuperiorClasses(true);
    generator.generate();
    generator.write("target/generated-test-sources/ldaptive");
  }
}
