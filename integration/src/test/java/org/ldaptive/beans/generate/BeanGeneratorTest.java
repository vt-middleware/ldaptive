/* See LICENSE for licensing and NOTICE for copyright. */
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
 */
public class BeanGeneratorTest extends AbstractTest
{


  /** @throws  Exception  On test failure. */
  @Test(groups = {"beans-generate"})
  public void generate()
    throws Exception
  {
    if (TestControl.isActiveDirectory()) {
      return;
    }

    final DefaultConnectionFactory factory = new DefaultConnectionFactory(
      TestUtils.readConnectionConfig("classpath:/org/ldaptive/ldap.setup.properties"));
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


  /** @throws  Exception  On test failure. */
  @Test(groups = {"beans-generate"})
  public void generateAD()
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
      return;
    }

    final DefaultConnectionFactory factory = new DefaultConnectionFactory(
      TestUtils.readConnectionConfig("classpath:/org/ldaptive/ldap.setup.properties"));
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
