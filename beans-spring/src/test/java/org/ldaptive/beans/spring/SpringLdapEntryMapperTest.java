/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring;

import org.ldaptive.LdapEntry;
import org.ldaptive.beans.LdapEntryMapper;
import org.ldaptive.beans.reflect.BinaryCustomObject;
import org.ldaptive.beans.reflect.BooleanCustomObject;
import org.ldaptive.beans.reflect.CustomObject;
import org.ldaptive.beans.reflect.FloatCustomObject;
import org.ldaptive.beans.reflect.IntCustomObject;
import org.ldaptive.beans.reflect.IntegerCustomObject;
import org.ldaptive.beans.reflect.StringCustomObject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link SpringLdapEntryMapper} implementations.
 *
 * @author  Middleware Services
 */
public class SpringLdapEntryMapperTest
{


  /**
   * Test data for string based object.
   *
   * @return  custom objects
   */
  @DataProvider(name = "objects")
  public Object[][] createCustomObjects()
  {
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
          StringCustomObject.createCustomObject(StringCustomObject.Spring.class),
          stringEntry,
          springMapper,
        },
        new Object[] {
          IntegerCustomObject.createCustomObject(IntegerCustomObject.Spring.class),
          integerEntry,
          springMapper,
        },
        new Object[] {
          IntCustomObject.createCustomObject(IntCustomObject.Spring.class),
          integerEntry,
          springMapper,
        },
        new Object[] {
          FloatCustomObject.createCustomObject(FloatCustomObject.Spring.class),
          floatEntry,
          springMapper,
        },
        new Object[] {
          BooleanCustomObject.createCustomObject(BooleanCustomObject.Spring.class),
          booleanEntry,
          springMapper,
        },
        new Object[] {
          BinaryCustomObject.createCustomObject(BinaryCustomObject.Spring.class),
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
  @Test(groups = "beans", dataProvider = "objects")
  public void mapToLdapEntry(
    final CustomObject object,
    final LdapEntry entry,
    final LdapEntryMapper<CustomObject> mapper)
    throws Exception
  {
    final LdapEntry mapped = new LdapEntry();
    object.initialize();
    mapper.map(object, mapped);
    assertThat(mapped).isEqualTo(entry);
  }


  /**
   * @param  object  to compare with mapped object
   * @param  entry  initialized with data
   * @param  mapper  to invoke
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "beans", dataProvider = "objects")
  public void mapToObject(final CustomObject object, final LdapEntry entry, final LdapEntryMapper<CustomObject> mapper)
    throws Exception
  {
    final CustomObject mapped = object.getClass().getDeclaredConstructor().newInstance();
    mapper.map(entry, mapped);
    mapped.initialize();
    assertThat(mapped).isEqualTo(object);
  }
}
