/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.reflect;

import org.ldaptive.LdapEntry;
import org.ldaptive.beans.LdapEntryMapper;
import org.ldaptive.beans.spring.SpringLdapEntryMapper;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link LdapEntryMapper} implementations.
 *
 * @author  Middleware Services
 * @version  $Revision: 3012 $ $Date: 2014-07-02 11:23:50 -0400 (Wed, 02 Jul 2014) $
 */
public class LdapEntryMapperTest
{


  /**
   * Test data for string based object.
   *
   * @return  custom objects
   */
  @DataProvider(name = "objects")
  public Object[][] createCustomObjects()
  {
    final DefaultLdapEntryMapper defaultMapper = new DefaultLdapEntryMapper();
    final SpringLdapEntryMapper springMapper = new SpringLdapEntryMapper();

    final LdapEntry stringEntry = StringCustomObject.createLdapEntry();
    final LdapEntry charEntry = StringCustomObject.createLdapEntry();
    charEntry.removeAttribute("col1");
    charEntry.removeAttribute("col2");
    charEntry.removeAttribute("typeArray1");
    charEntry.removeAttribute("typeArray2");
    charEntry.removeAttribute("transcoded");

    final LdapEntry integerEntry = IntegerCustomObject.createLdapEntry();
    final LdapEntry floatEntry = FloatCustomObject.createLdapEntry();
    final LdapEntry booleanEntry = BooleanCustomObject.createLdapEntry();
    final LdapEntry binaryEntry = BinaryCustomObject.createLdapEntry();

    return
      new Object[][] {
        new Object[] {
          StringCustomObject.createCustomObject(
            StringCustomObject.Default.class),
          stringEntry,
          defaultMapper,
        },
        new Object[] {
          StringCustomObject.createCustomObject(
            StringCustomObject.Spring.class),
          stringEntry,
          springMapper,
        },
        new Object[] {
          CharCustomObject.createCustomObject(CharCustomObject.Default.class),
          charEntry,
          defaultMapper,
        },
        new Object[] {
          IntegerCustomObject.createCustomObject(
            IntegerCustomObject.Default.class),
          integerEntry,
          defaultMapper,
        },
        new Object[] {
          IntegerCustomObject.createCustomObject(
            IntegerCustomObject.Spring.class),
          integerEntry,
          springMapper,
        },
        new Object[] {
          IntCustomObject.createCustomObject(IntCustomObject.Default.class),
          integerEntry,
          defaultMapper,
        },
        new Object[] {
          IntCustomObject.createCustomObject(IntCustomObject.Spring.class),
          integerEntry,
          springMapper,
        },
        new Object[] {
          FloatCustomObject.createCustomObject(FloatCustomObject.Default.class),
          floatEntry,
          defaultMapper,
        },
        new Object[] {
          FloatCustomObject.createCustomObject(FloatCustomObject.Spring.class),
          floatEntry,
          springMapper,
        },
        new Object[] {
          BooleanCustomObject.createCustomObject(
            BooleanCustomObject.Default.class),
          booleanEntry,
          defaultMapper,
        },
        new Object[] {
          BooleanCustomObject.createCustomObject(
            BooleanCustomObject.Spring.class),
          booleanEntry,
          springMapper,
        },
        new Object[] {
          BinaryCustomObject.createCustomObject(
            BinaryCustomObject.Default.class),
          binaryEntry,
          defaultMapper,
        },
        new Object[] {
          BinaryCustomObject.createCustomObject(
            BinaryCustomObject.Spring.class),
          binaryEntry,
          springMapper,
        },
      };
  }


  /**
   * @param  object  initialized with data
   * @param  entry  to compare with mapped entry
   * @param  mapper  to invoke
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"beans"},
    dataProvider = "objects"
  )
  public void mapToLdapEntry(
    final CustomObject object,
    final LdapEntry entry,
    final LdapEntryMapper<CustomObject> mapper)
    throws Exception
  {
    final LdapEntry mapped = new LdapEntry();
    object.initialize();
    mapper.map(object, mapped);
    Assert.assertEquals(entry, mapped);
  }


  /**
   * @param  object  to compare with mapped object
   * @param  entry  initialized with data
   * @param  mapper  to invoke
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"beans"},
    dataProvider = "objects"
  )
  public void mapToObject(
    final CustomObject object,
    final LdapEntry entry,
    final LdapEntryMapper<CustomObject> mapper)
    throws Exception
  {
    final CustomObject mapped = object.getClass().newInstance();
    mapper.map(entry, mapped);
    mapped.initialize();
    Assert.assertEquals(object, mapped);
  }
}
