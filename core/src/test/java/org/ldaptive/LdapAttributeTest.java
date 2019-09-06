/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Unit test for {@link LdapAttribute}.
 *
 * @author  Middleware Services
 */
public class LdapAttributeTest
{


  /** Tests create with one value. */
  @Test
  public void oneAttribute()
  {
    final LdapAttribute la = new LdapAttribute("givenName", "William");
    AssertJUnit.assertEquals("William", la.getStringValue());
    AssertJUnit.assertEquals(1, la.getStringValues().size());
    AssertJUnit.assertEquals("William", la.getStringValues().iterator().next());
    AssertJUnit.assertTrue(Arrays.equals("William".getBytes(), la.getBinaryValue()));
    AssertJUnit.assertEquals(1, la.size());
    AssertJUnit.assertEquals(la, new LdapAttribute("givenName", "William"));
    try {
      la.addStringValues((String[]) null);
      AssertJUnit.fail("Should have thrown NullPointerException");
    } catch (Exception e) {
      AssertJUnit.assertEquals(NullPointerException.class, e.getClass());
    }
    la.addStringValues((String) null);
    AssertJUnit.assertEquals(1, la.size());
    la.clear();
    AssertJUnit.assertEquals(0, la.size());
  }


  /** Tests create with two values. */
  @Test
  public void twoAttributes()
  {
    final LdapAttribute la = new LdapAttribute("givenName", "Bill", "William");
    AssertJUnit.assertEquals(2, la.getStringValues().size());
    AssertJUnit.assertEquals(2, la.size());
    AssertJUnit.assertEquals(la, new LdapAttribute("givenName", "Bill", "William"));
    la.clear();
    AssertJUnit.assertEquals(0, la.size());
  }


  /** Tests for {@link LdapAttribute#addStringValues(String...)}. */
  @Test
  public void addStringValue()
  {
    final LdapAttribute la = new LdapAttribute("cn", "William Wallace");
    AssertJUnit.assertEquals("William Wallace", la.getStringValue());
    AssertJUnit.assertEquals("William Wallace".getBytes(StandardCharsets.UTF_8), la.getBinaryValue());
    AssertJUnit.assertEquals(1, la.getStringValues().size());
    AssertJUnit.assertEquals(1, la.getBinaryValues().size());
    AssertJUnit.assertEquals(la, new LdapAttribute("cn", "William Wallace"));
    try {
      la.addStringValues((String[]) null);
      AssertJUnit.fail("Should have thrown NullPointerException");
    } catch (Exception e) {
      AssertJUnit.assertEquals(NullPointerException.class, e.getClass());
    }
    la.addStringValues((String) null);
    la.addBinaryValues("Bill".getBytes(StandardCharsets.UTF_8));
    AssertJUnit.assertEquals(2, la.getBinaryValues().size());
    AssertJUnit.assertEquals(2, la.getStringValues().size());
    AssertJUnit.assertTrue(la.getStringValues().contains("Bill"));
    AssertJUnit.assertTrue(la.getStringValues().contains("William Wallace"));
    AssertJUnit.assertTrue(
      la.getBinaryValues().stream().anyMatch(
        b -> Arrays.equals(b, "Bill".getBytes(StandardCharsets.UTF_8))));
    AssertJUnit.assertTrue(
      la.getBinaryValues().stream().anyMatch(
        b -> Arrays.equals(b, "William Wallace".getBytes(StandardCharsets.UTF_8))));
    la.clear();
    AssertJUnit.assertEquals(0, la.size());
  }


  /** Tests for {@link LdapAttribute#addBinaryValues(byte[]...)}. */
  @Test
  public void addBinaryValue()
  {
    final LdapAttribute la = new LdapAttribute("jpegPhoto", "image".getBytes());
    AssertJUnit.assertTrue(Arrays.equals("image".getBytes(), la.getBinaryValue()));
    AssertJUnit.assertEquals("aW1hZ2U=", la.getStringValue());
    AssertJUnit.assertEquals(1, la.getBinaryValues().size());
    AssertJUnit.assertEquals(1, la.getStringValues().size());
    AssertJUnit.assertEquals(la, new LdapAttribute("jpegPhoto", "image".getBytes()));
    try {
      la.addBinaryValues((byte[][]) null);
      AssertJUnit.fail("Should have thrown NullPointerException");
    } catch (Exception e) {
      AssertJUnit.assertEquals(NullPointerException.class, e.getClass());
    }
    la.addBinaryValues((byte[]) null);
    la.addStringValues("QmlsbA==");
    AssertJUnit.assertEquals(2, la.getBinaryValues().size());
    AssertJUnit.assertEquals(2, la.getStringValues().size());
    AssertJUnit.assertTrue(la.getStringValues().contains("aW1hZ2U="));
    AssertJUnit.assertTrue(la.getStringValues().contains("QmlsbA=="));
    AssertJUnit.assertTrue(
      la.getBinaryValues().stream().anyMatch(b -> Arrays.equals(b, "Bill".getBytes(StandardCharsets.UTF_8))));
    AssertJUnit.assertTrue(
      la.getBinaryValues().stream().anyMatch(b -> Arrays.equals(b, "image".getBytes())));
    la.clear();
    AssertJUnit.assertEquals(0, la.size());
  }


  /** Tests for {@link LdapAttribute#addStringValues(Collection)}. */
  @Test
  public void addStringValues()
  {
    final Set<String> commonNames = new HashSet<>();
    commonNames.add("Bill Wallace");
    commonNames.add("William Wallace");

    final Set<byte[]> binaryCommonNames = new HashSet<>();
    binaryCommonNames.add("Bill Wallace".getBytes(StandardCharsets.UTF_8));
    binaryCommonNames.add("William Wallace".getBytes(StandardCharsets.UTF_8));

    final LdapAttribute la = new LdapAttribute();
    la.setName("cn");
    la.addStringValues(commonNames);
    AssertJUnit.assertNotNull(la.getStringValue());
    AssertJUnit.assertNotNull(la.getStringValues());
    AssertJUnit.assertNotNull(la.getBinaryValue());
    AssertJUnit.assertNotNull(la.getBinaryValues());
    AssertJUnit.assertEquals(2, la.getStringValues().size());
    AssertJUnit.assertEquals(2, la.getBinaryValues().size());
    AssertJUnit.assertTrue(la.getStringValues().contains("Bill Wallace"));
    AssertJUnit.assertTrue(la.getStringValues().contains("William Wallace"));
    Assert.assertEqualsNoOrder(commonNames.toArray(new String[2]), la.getStringValues().toArray(new String[2]));
    Assert.assertEquals(binaryCommonNames.size(), la.getBinaryValues().size());
    for (byte[] b : binaryCommonNames) {
      Assert.assertTrue(la.getBinaryValues().stream().anyMatch(a -> Arrays.equals(a, b)));
    }
    la.clear();
    AssertJUnit.assertEquals(0, la.size());
  }


  /** Tests for getting string and binary values for binary input. */
  @Test
  public void getBinaryValues()
  {
    final List<byte[]> jpegPhotos = new ArrayList<>();
    jpegPhotos.add("image1".getBytes());
    jpegPhotos.add("image2".getBytes());

    final List<String> stringJpegPhotos = new ArrayList<>();
    stringJpegPhotos.add("aW1hZ2Ux");
    stringJpegPhotos.add("aW1hZ2Uy");

    final LdapAttribute la = new LdapAttribute();
    la.setName("jpegPhoto");
    la.addBinaryValues(jpegPhotos);
    AssertJUnit.assertNotNull(la.getStringValue());
    AssertJUnit.assertNotNull(la.getStringValues());
    AssertJUnit.assertNotNull(la.getBinaryValue());
    AssertJUnit.assertNotNull(la.getBinaryValues());
    AssertJUnit.assertEquals(2, la.getStringValues().size());
    AssertJUnit.assertEquals(2, la.getBinaryValues().size());
    AssertJUnit.assertTrue(la.getStringValues().contains("aW1hZ2Ux"));
    AssertJUnit.assertTrue(la.getStringValues().contains("aW1hZ2Uy"));
    Assert.assertEqualsNoOrder(stringJpegPhotos.toArray(new String[2]), la.getStringValues().toArray(new String[2]));
    Assert.assertEquals(jpegPhotos.size(), la.getBinaryValues().size());
    for (byte[] b : jpegPhotos) {
      Assert.assertTrue(la.getBinaryValues().stream().anyMatch(a -> Arrays.equals(a, b)));
    }
    la.clear();
    AssertJUnit.assertEquals(0, la.size());
  }


  /** Tests attribute options. */
  @Test
  public void attributeOptions()
  {
    LdapAttribute la = new LdapAttribute("cn", "William Wallace");
    AssertJUnit.assertEquals("cn", la.getName());
    AssertJUnit.assertEquals("cn", la.getName(true));
    AssertJUnit.assertEquals("cn", la.getName(false));
    AssertJUnit.assertNotNull(la.getOptions());
    AssertJUnit.assertEquals(0, la.getOptions().size());

    la = new LdapAttribute("cn;lang-ru", "Уильям Уоллес");
    AssertJUnit.assertEquals("cn;lang-ru", la.getName());
    AssertJUnit.assertEquals("cn;lang-ru", la.getName(true));
    AssertJUnit.assertEquals("cn", la.getName(false));
    AssertJUnit.assertNotNull(la.getOptions());
    AssertJUnit.assertEquals(1, la.getOptions().size());
    AssertJUnit.assertEquals("lang-ru", la.getOptions().get(0));

    la = new LdapAttribute("cn;lang-lv;dynamic", "Viljams Voless");
    AssertJUnit.assertEquals("cn;lang-lv;dynamic", la.getName());
    AssertJUnit.assertEquals("cn;lang-lv;dynamic", la.getName(true));
    AssertJUnit.assertEquals("cn", la.getName(false));
    AssertJUnit.assertNotNull(la.getOptions());
    AssertJUnit.assertEquals(2, la.getOptions().size());
    AssertJUnit.assertEquals("lang-lv", la.getOptions().get(0));
    AssertJUnit.assertEquals("dynamic", la.getOptions().get(1));
  }


  /** Test for {@link LdapAttribute#equals(Object)}. */
  @Test
  public void testEquals()
  {
    final LdapAttribute la1 = LdapAttribute.builder().build();
    Assert.assertEquals(la1, la1);
    Assert.assertEquals(LdapAttribute.builder().build(), LdapAttribute.builder().build());
    Assert.assertEquals(
      LdapAttribute.builder().name("uid").values("1").build(),
      LdapAttribute.builder().name("uid").values("1").build());
    Assert.assertEquals(
      LdapAttribute.builder().name("uid").values("1").binary(true).build(),
      LdapAttribute.builder().name("uid").values("1").binary(true).build());
    Assert.assertEquals(
      LdapAttribute.builder().name("uid").values("1").binary(true).build(),
      LdapAttribute.builder().name("uid").values("1").binary(false).build());
    Assert.assertEquals(
      LdapAttribute.builder().name("uid").values("1", "2", "3").build(),
      LdapAttribute.builder().name("uid").values("1", "2", "3").build());
    Assert.assertNotEquals(
      LdapAttribute.builder().name("uuid").values("1").build(),
      LdapAttribute.builder().name("uid").values("1").build());
    Assert.assertNotEquals(
      LdapAttribute.builder().name("uid").values("2").build(),
      LdapAttribute.builder().name("uid").values("1").build());
    Assert.assertNotEquals(
      LdapAttribute.builder().name("uid").values("1", "2", "3" , "4").build(),
      LdapAttribute.builder().name("uid").values("1", "2", "3").build());
  }
}
