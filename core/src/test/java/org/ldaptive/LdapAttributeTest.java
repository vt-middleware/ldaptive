/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Unit test for {@link LdapAttribute}.
 *
 * @author  Middleware Services
 */
public class LdapAttributeTest
{


  /** Tests default sort behavior. */
  @Test(groups = "bean")
  public void defaultSortBehavior()
  {
    final LdapAttribute la = new LdapAttribute("givenName");
    AssertJUnit.assertEquals(SortBehavior.getDefaultSortBehavior(), la.getSortBehavior());
    AssertJUnit.assertEquals(0, la.size());
    AssertJUnit.assertNull(la.getStringValue());
    AssertJUnit.assertNull(la.getBinaryValue());
    AssertJUnit.assertEquals("givenName", la.getName());
    la.setName("sn");
    AssertJUnit.assertEquals("sn", la.getName());
    la.clear();
    AssertJUnit.assertEquals(0, la.size());
  }


  /** Tests ordered sort behavior. */
  @Test(groups = "bean")
  public void orderedSortBehavior()
  {
    final LdapAttribute la = new LdapAttribute(SortBehavior.ORDERED);
    AssertJUnit.assertEquals(SortBehavior.ORDERED, la.getSortBehavior());
    la.addStringValue("William", "Bill");

    final String[] values = la.getStringValues().toArray(new String[2]);
    AssertJUnit.assertEquals("William", values[0]);
    AssertJUnit.assertEquals("Bill", values[1]);
    la.clear();
    AssertJUnit.assertEquals(0, la.size());
  }


  /** Tests sorted sort behavior. */
  @Test(groups = "bean")
  public void sortedSortBehavior()
  {
    final LdapAttribute la = new LdapAttribute(SortBehavior.SORTED);
    AssertJUnit.assertEquals(SortBehavior.SORTED, la.getSortBehavior());
    la.addStringValue("William", "Bill");

    final String[] values = la.getStringValues().toArray(new String[2]);
    AssertJUnit.assertEquals("Bill", values[0]);
    AssertJUnit.assertEquals("William", values[1]);
    la.clear();
    AssertJUnit.assertEquals(0, la.size());
  }


  /** Tests create with one value. */
  @Test(groups = "bean")
  public void createOne()
  {
    final LdapAttribute la = new LdapAttribute("givenName", "William");
    AssertJUnit.assertEquals("William", la.getStringValue());
    AssertJUnit.assertEquals(1, la.getStringValues().size());
    AssertJUnit.assertEquals("William", la.getStringValues().iterator().next());
    AssertJUnit.assertTrue(Arrays.equals("William".getBytes(), la.getBinaryValue()));
    AssertJUnit.assertEquals(1, la.size());
    AssertJUnit.assertEquals(la, new LdapAttribute("givenName", "William"));
    try {
      la.addStringValue((String) null);
      AssertJUnit.fail("Should have thrown IllegalArgumentException");
    } catch (Exception e) {
      AssertJUnit.assertEquals(IllegalArgumentException.class, e.getClass());
    }
    try {
      la.addBinaryValue("Bill".getBytes());
      AssertJUnit.fail("Should have thrown IllegalArgumentException");
    } catch (Exception e) {
      AssertJUnit.assertEquals(IllegalArgumentException.class, e.getClass());
    }
    la.clear();
    AssertJUnit.assertEquals(0, la.size());
  }


  /** Tests create with two values. */
  @Test(groups = "bean")
  public void createTwo()
  {
    final LdapAttribute la = new LdapAttribute("givenName", "Bill", "William");
    AssertJUnit.assertEquals(2, la.getStringValues().size());
    AssertJUnit.assertEquals(2, la.size());
    AssertJUnit.assertEquals(la, new LdapAttribute("givenName", "William", "Bill"));
    la.removeStringValue("William");
    AssertJUnit.assertEquals(1, la.size());
    la.clear();
    AssertJUnit.assertEquals(0, la.size());
  }


  /** Tests various string input. */
  @Test(groups = "bean")
  public void stringValue()
  {
    final LdapAttribute la = new LdapAttribute("cn", "William Wallace");
    AssertJUnit.assertEquals("William Wallace", la.getStringValue());
    AssertJUnit.assertEquals("William Wallace".getBytes(StandardCharsets.UTF_8), la.getBinaryValue());
    AssertJUnit.assertEquals(1, la.getStringValues().size());
    AssertJUnit.assertEquals(1, la.getBinaryValues().size());
    AssertJUnit.assertEquals(la, new LdapAttribute("cn", "William Wallace"));
    try {
      la.addStringValue((String[]) null);
      AssertJUnit.fail("Should have thrown NullPointerException");
    } catch (Exception e) {
      AssertJUnit.assertEquals(NullPointerException.class, e.getClass());
    }
    try {
      la.addBinaryValue("Bill".getBytes());
      AssertJUnit.fail("Should have thrown IllegalArgumentException");
    } catch (Exception e) {
      AssertJUnit.assertEquals(IllegalArgumentException.class, e.getClass());
    }
    la.clear();
    AssertJUnit.assertEquals(0, la.size());
  }


  /** Tests multiple string input. */
  @Test(groups = "bean")
  public void stringValues()
  {
    final List<String> commonNames = new ArrayList<>();
    commonNames.add("Bill Wallace");
    commonNames.add("William Wallace");

    final List<byte[]> binaryCommonNames = new ArrayList<>();
    binaryCommonNames.add("Bill Wallace".getBytes(StandardCharsets.UTF_8));
    binaryCommonNames.add("William Wallace".getBytes(StandardCharsets.UTF_8));

    LdapAttribute la = new LdapAttribute(SortBehavior.UNORDERED);
    la.setName("cn");
    la.addStringValue(commonNames.get(0));
    la.addStringValue(commonNames.get(1));
    AssertJUnit.assertNotNull(la.getStringValue());
    AssertJUnit.assertNotNull(la.getStringValues());
    AssertJUnit.assertEquals(2, la.getStringValues().size());
    AssertJUnit.assertNotNull(la.getBinaryValue());
    AssertJUnit.assertNotNull(la.getBinaryValues());
    AssertJUnit.assertEquals(2, la.getBinaryValues().size());
    la.clear();
    AssertJUnit.assertEquals(0, la.size());

    la = new LdapAttribute(SortBehavior.ORDERED);
    la.setName("cn");
    la.addStringValue(commonNames.get(0));
    la.addStringValue(commonNames.get(1));
    AssertJUnit.assertEquals("Bill Wallace", la.getStringValue());
    AssertJUnit.assertArrayEquals(commonNames.toArray(new String[2]), la.getStringValues().toArray(new String[2]));
    AssertJUnit.assertEquals(2, la.getStringValues().size());
    AssertJUnit.assertEquals("Bill Wallace".getBytes(StandardCharsets.UTF_8), la.getBinaryValue());
    AssertJUnit.assertArrayEquals(
      binaryCommonNames.toArray(new byte[2][0]),
      la.getBinaryValues().toArray(new byte[2][0]));
    AssertJUnit.assertEquals(2, la.getBinaryValues().size());
    la.clear();
    AssertJUnit.assertEquals(0, la.size());

    la = new LdapAttribute(SortBehavior.SORTED);
    la.setName("cn");
    la.addStringValue(commonNames.get(0));
    la.addStringValue(commonNames.get(1));
    AssertJUnit.assertEquals("Bill Wallace", la.getStringValue());
    AssertJUnit.assertArrayEquals(commonNames.toArray(new String[2]), la.getStringValues().toArray(new String[2]));
    AssertJUnit.assertEquals(2, la.getStringValues().size());
    AssertJUnit.assertEquals("Bill Wallace".getBytes(StandardCharsets.UTF_8), la.getBinaryValue());
    AssertJUnit.assertArrayEquals(
      binaryCommonNames.toArray(new byte[2][0]),
      la.getBinaryValues().toArray(new byte[2][0]));
    AssertJUnit.assertEquals(2, la.getBinaryValues().size());
    la.clear();
    AssertJUnit.assertEquals(0, la.size());
  }


  /** Tests various binary input. */
  @Test(groups = "bean")
  public void binaryValue()
  {
    final LdapAttribute la = new LdapAttribute("jpegPhoto", "image".getBytes());
    AssertJUnit.assertTrue(Arrays.equals("image".getBytes(), la.getBinaryValue()));
    AssertJUnit.assertEquals(1, la.getBinaryValues().size());
    AssertJUnit.assertEquals("aW1hZ2U=", la.getStringValue());
    AssertJUnit.assertEquals(1, la.getStringValues().size());
    AssertJUnit.assertEquals(la, new LdapAttribute("jpegPhoto", "image".getBytes()));
    try {
      la.addBinaryValue((byte[][]) null);
      AssertJUnit.fail("Should have thrown NullPointerException");
    } catch (Exception e) {
      AssertJUnit.assertEquals(NullPointerException.class, e.getClass());
    }
    try {
      la.addStringValue("Bill");
      AssertJUnit.fail("Should have thrown IllegalArgumentException");
    } catch (Exception e) {
      AssertJUnit.assertEquals(IllegalArgumentException.class, e.getClass());
    }
    la.clear();
    AssertJUnit.assertEquals(0, la.size());
  }


  /** Tests multiple string input. */
  @Test(groups = "bean")
  public void binaryValues()
  {
    final List<byte[]> jpegPhotos = new ArrayList<>();
    jpegPhotos.add("image1".getBytes());
    jpegPhotos.add("image2".getBytes());

    final List<String> stringJpegPhotos = new ArrayList<>();
    stringJpegPhotos.add("aW1hZ2Ux");
    stringJpegPhotos.add("aW1hZ2Uy");

    LdapAttribute la = new LdapAttribute(SortBehavior.UNORDERED, true);
    la.setName("jpegPhoto");
    la.addBinaryValue(jpegPhotos.get(0));
    la.addBinaryValue(jpegPhotos.get(1));
    AssertJUnit.assertNotNull(la.getStringValue());
    AssertJUnit.assertNotNull(la.getStringValues());
    AssertJUnit.assertEquals(2, la.getStringValues().size());
    AssertJUnit.assertNotNull(la.getBinaryValue());
    AssertJUnit.assertNotNull(la.getBinaryValues());
    AssertJUnit.assertEquals(2, la.getBinaryValues().size());
    la.clear();
    AssertJUnit.assertEquals(0, la.size());

    la = new LdapAttribute(SortBehavior.ORDERED, true);
    la.setName("jpegPhoto");
    la.addBinaryValue(jpegPhotos.get(0));
    la.addBinaryValue(jpegPhotos.get(1));
    AssertJUnit.assertEquals("aW1hZ2Ux", la.getStringValue());
    AssertJUnit.assertArrayEquals(stringJpegPhotos.toArray(new String[2]), la.getStringValues().toArray(new String[2]));
    AssertJUnit.assertEquals(2, la.getStringValues().size());
    AssertJUnit.assertEquals("image1".getBytes(), la.getBinaryValue());
    AssertJUnit.assertArrayEquals(jpegPhotos.toArray(new byte[2][0]), la.getBinaryValues().toArray(new byte[2][0]));
    AssertJUnit.assertEquals(2, la.getBinaryValues().size());
    la.clear();
    AssertJUnit.assertEquals(0, la.size());

    la = new LdapAttribute(SortBehavior.SORTED, true);
    la.setName("jpegPhoto");
    la.addBinaryValue(jpegPhotos.get(0));
    la.addBinaryValue(jpegPhotos.get(1));
    AssertJUnit.assertEquals("aW1hZ2Ux", la.getStringValue());
    AssertJUnit.assertArrayEquals(stringJpegPhotos.toArray(new String[2]), la.getStringValues().toArray(new String[2]));
    AssertJUnit.assertEquals(2, la.getStringValues().size());
    AssertJUnit.assertEquals("image1".getBytes(), la.getBinaryValue());
    AssertJUnit.assertArrayEquals(jpegPhotos.toArray(new byte[2][0]), la.getBinaryValues().toArray(new byte[2][0]));
    AssertJUnit.assertEquals(2, la.getBinaryValues().size());
    la.clear();
    AssertJUnit.assertEquals(0, la.size());
  }


  /** Tests attribute options. */
  @Test(groups = "bean")
  public void attributeOptions()
  {
    LdapAttribute la = new LdapAttribute("cn", "William Wallace");
    AssertJUnit.assertEquals("cn", la.getName());
    AssertJUnit.assertEquals("cn", la.getName(true));
    AssertJUnit.assertEquals("cn", la.getName(false));
    AssertJUnit.assertNotNull(la.getOptions());
    AssertJUnit.assertEquals(0, la.getOptions().length);

    la = new LdapAttribute("cn;lang-ru", "Уильям Уоллес");
    AssertJUnit.assertEquals("cn;lang-ru", la.getName());
    AssertJUnit.assertEquals("cn;lang-ru", la.getName(true));
    AssertJUnit.assertEquals("cn", la.getName(false));
    AssertJUnit.assertNotNull(la.getOptions());
    AssertJUnit.assertEquals(1, la.getOptions().length);
    AssertJUnit.assertEquals("lang-ru", la.getOptions()[0]);

    la = new LdapAttribute("cn;lang-lv;dynamic", "Viljams Voless");
    AssertJUnit.assertEquals("cn;lang-lv;dynamic", la.getName());
    AssertJUnit.assertEquals("cn;lang-lv;dynamic", la.getName(true));
    AssertJUnit.assertEquals("cn", la.getName(false));
    AssertJUnit.assertNotNull(la.getOptions());
    AssertJUnit.assertEquals(2, la.getOptions().length);
    AssertJUnit.assertEquals("lang-lv", la.getOptions()[0]);
    AssertJUnit.assertEquals("dynamic", la.getOptions()[1]);
  }
}
