/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.generate;

import org.ldaptive.schema.Schema;
import org.ldaptive.schema.SchemaFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link BeanGenerator}.
 *
 * @author  Middleware Services
 */
public class BeanGeneratorTest
{


  /**
   * Test data for bean generator.
   *
   * @return  custom objects
   *
   * @throws  Exception  if a schema cannot be read
   */
  @DataProvider(name = "schemas")
  public Object[][] createSchemas()
    throws Exception
  {
    return
      new Object[][] {
        new Object[] {
          SchemaFactory.createSchema(BeanGeneratorTest.class.getResourceAsStream("/subschema.ldif")),
          "org.ldaptive.beans.schema.subschema",
          new String[] {"inetOrgPerson"},
        },
        new Object[] {
          SchemaFactory.createSchema(BeanGeneratorTest.class.getResourceAsStream("/allschema.ldif")),
          "org.ldaptive.beans.schema.allschema",
          new String[] {"inetOrgPerson"},
        },
        new Object[] {
          SchemaFactory.createSchema(BeanGeneratorTest.class.getResourceAsStream("/opendj_schema.ldif")),
          "org.ldaptive.beans.schema.opendj",
          new String[] {"inetOrgPerson", "groupOfUniqueNames"},
        },
      };
  }


  /**
   * @param  schema  to generate beans from
   * @param  packageName  to generate beans in
   * @param  objectClasses  to generate beans for
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "beans", dataProvider = "schemas")
  public void generate(final Schema schema, final String packageName, final String[] objectClasses)
    throws Exception
  {
    final BeanGenerator generator = new BeanGenerator();
    generator.setSchema(schema);
    generator.setObjectClasses(objectClasses);
    generator.setPackageName(packageName);
    generator.setUseOperationalAttributes(true);
    generator.setUseOptionalAttributes(true);
    generator.setIncludeSuperiorClasses(true);
    generator.generate();
    generator.write("target/generated-test-sources/ldaptive");
  }
}
