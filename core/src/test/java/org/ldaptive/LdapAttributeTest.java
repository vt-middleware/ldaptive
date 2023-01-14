/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.testng.Assert;
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
    Assert.assertEquals(la.getStringValue(), "William");
    Assert.assertEquals(la.getStringValues().size(), 1);
    Assert.assertEquals(la.getStringValues().iterator().next(), "William");
    Assert.assertTrue(Arrays.equals("William".getBytes(), la.getBinaryValue()));
    Assert.assertEquals(la.size(), 1);
    Assert.assertEquals(la, new LdapAttribute("givenName", "William"));
    try {
      la.addStringValues((String[]) null);
      Assert.fail("Should have thrown NullPointerException");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), NullPointerException.class);
    }
    la.addStringValues((String) null);
    Assert.assertEquals(la.size(), 1);
    la.clear();
    Assert.assertEquals(la.size(), 0);
  }


  /** Tests create with two values. */
  @Test
  public void twoAttributes()
  {
    final LdapAttribute la = new LdapAttribute("givenName", "Bill", "William");
    Assert.assertEquals(la.getStringValues().size(), 2);
    Assert.assertEquals(la.size(), 2);
    Assert.assertEquals(la, new LdapAttribute("givenName", "Bill", "William"));
    la.clear();
    Assert.assertEquals(la.size(), 0);
  }


  /** Tests for {@link LdapAttribute#addStringValues(String...)}. */
  @Test
  public void addStringValue()
  {
    final LdapAttribute la = new LdapAttribute("cn", "William Wallace");
    Assert.assertEquals(la.getStringValue(), "William Wallace");
    Assert.assertEquals(la.getBinaryValue(), "William Wallace".getBytes(StandardCharsets.UTF_8));
    Assert.assertEquals(la.getStringValues().size(), 1);
    Assert.assertEquals(la.getBinaryValues().size(), 1);
    Assert.assertEquals(la, new LdapAttribute("cn", "William Wallace"));
    try {
      la.addStringValues((String[]) null);
      Assert.fail("Should have thrown NullPointerException");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), NullPointerException.class);
    }
    la.addStringValues((String) null);
    la.addBinaryValues("Bill".getBytes(StandardCharsets.UTF_8));
    Assert.assertEquals(la.getBinaryValues().size(), 2);
    Assert.assertEquals(la.getStringValues().size(), 2);
    Assert.assertTrue(la.getStringValues().contains("Bill"));
    Assert.assertTrue(la.getStringValues().contains("William Wallace"));
    Assert.assertTrue(
      la.getBinaryValues().stream().anyMatch(
        b -> Arrays.equals(b, "Bill".getBytes(StandardCharsets.UTF_8))));
    Assert.assertTrue(
      la.getBinaryValues().stream().anyMatch(
        b -> Arrays.equals(b, "William Wallace".getBytes(StandardCharsets.UTF_8))));
    la.clear();
    Assert.assertEquals(la.size(), 0);
  }


  /** Tests for {@link LdapAttribute#addBinaryValues(byte[]...)}. */
  @Test
  public void addBinaryValue()
  {
    final LdapAttribute la = new LdapAttribute("jpegPhoto", "image".getBytes());
    Assert.assertTrue(Arrays.equals("image".getBytes(), la.getBinaryValue()));
    Assert.assertEquals(la.getStringValue(), "aW1hZ2U=");
    Assert.assertEquals(la.getBinaryValues().size(), 1);
    Assert.assertEquals(la.getStringValues().size(), 1);
    Assert.assertEquals(la, new LdapAttribute("jpegPhoto", "image".getBytes()));
    try {
      la.addBinaryValues((byte[][]) null);
      Assert.fail("Should have thrown NullPointerException");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), NullPointerException.class);
    }
    la.addBinaryValues((byte[]) null);
    la.addStringValues("QmlsbA==");
    Assert.assertEquals(la.getBinaryValues().size(), 2);
    Assert.assertEquals(la.getStringValues().size(), 2);
    Assert.assertTrue(la.getStringValues().contains("aW1hZ2U="));
    Assert.assertTrue(la.getStringValues().contains("QmlsbA=="));
    Assert.assertTrue(
      la.getBinaryValues().stream().anyMatch(b -> Arrays.equals(b, "Bill".getBytes(StandardCharsets.UTF_8))));
    Assert.assertTrue(
      la.getBinaryValues().stream().anyMatch(b -> Arrays.equals(b, "image".getBytes())));
    la.clear();
    Assert.assertEquals(la.size(), 0);
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
    Assert.assertNotNull(la.getStringValue());
    Assert.assertNotNull(la.getStringValues());
    Assert.assertNotNull(la.getBinaryValue());
    Assert.assertNotNull(la.getBinaryValues());
    Assert.assertEquals(la.getStringValues().size(), 2);
    Assert.assertEquals(la.getBinaryValues().size(), 2);
    Assert.assertTrue(la.getStringValues().contains("Bill Wallace"));
    Assert.assertTrue(la.getStringValues().contains("William Wallace"));
    Assert.assertEqualsNoOrder(commonNames.toArray(new String[2]), la.getStringValues().toArray(new String[2]));
    Assert.assertEquals(binaryCommonNames.size(), la.getBinaryValues().size());
    for (byte[] b : binaryCommonNames) {
      Assert.assertTrue(la.getBinaryValues().stream().anyMatch(a -> Arrays.equals(a, b)));
    }
    la.clear();
    Assert.assertEquals(la.size(), 0);
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
    Assert.assertNotNull(la.getStringValue());
    Assert.assertNotNull(la.getStringValues());
    Assert.assertNotNull(la.getBinaryValue());
    Assert.assertNotNull(la.getBinaryValues());
    Assert.assertEquals(la.getStringValues().size(), 2);
    Assert.assertEquals(la.getBinaryValues().size(), 2);
    Assert.assertTrue(la.getStringValues().contains("aW1hZ2Ux"));
    Assert.assertTrue(la.getStringValues().contains("aW1hZ2Uy"));
    Assert.assertEqualsNoOrder(stringJpegPhotos.toArray(new String[2]), la.getStringValues().toArray(new String[2]));
    Assert.assertEquals(la.getBinaryValues().size(), jpegPhotos.size());
    for (byte[] b : jpegPhotos) {
      Assert.assertTrue(la.getBinaryValues().stream().anyMatch(a -> Arrays.equals(a, b)));
    }
    la.clear();
    Assert.assertEquals(la.size(), 0);
  }


  /** Tests attribute options. */
  @Test
  public void attributeOptions()
  {
    LdapAttribute la = new LdapAttribute("cn", "William Wallace");
    Assert.assertEquals(la.getName(), "cn");
    Assert.assertEquals(la.getName(true), "cn");
    Assert.assertEquals(la.getName(false), "cn");
    Assert.assertNotNull(la.getOptions());
    Assert.assertEquals(la.getOptions().size(), 0);

    la = new LdapAttribute("cn;lang-ru", "Уильям Уоллес");
    Assert.assertEquals(la.getName(), "cn;lang-ru");
    Assert.assertEquals(la.getName(true), "cn;lang-ru");
    Assert.assertEquals(la.getName(false), "cn");
    Assert.assertNotNull(la.getOptions());
    Assert.assertEquals(la.getOptions().size(), 1);
    Assert.assertEquals(la.getOptions().get(0), "lang-ru");

    la = new LdapAttribute("cn;lang-lv;dynamic", "Viljams Voless");
    Assert.assertEquals(la.getName(), "cn;lang-lv;dynamic");
    Assert.assertEquals(la.getName(true), "cn;lang-lv;dynamic");
    Assert.assertEquals(la.getName(false), "cn");
    Assert.assertNotNull(la.getOptions());
    Assert.assertEquals(la.getOptions().size(), 2);
    Assert.assertEquals(la.getOptions().get(0), "lang-lv");
    Assert.assertEquals(la.getOptions().get(1), "dynamic");
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
    Assert.assertEquals(
      LdapAttribute.builder().name("uid").values("1", "2", "3").build(),
      LdapAttribute.builder().name("UID").values("1", "2", "3").build());
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


  /** Test for add and then remove methods. */
  @Test
  public void testAddRemoveStringValues()
  {
    final LdapAttribute la = new LdapAttribute("sn");
    la.addStringValues("Smith", "Johnson");
    Assert.assertEquals(la.size(), 2);
    la.addStringValues(List.of("Williams", "Brown"));
    Assert.assertEquals(la.size(), 4);
    la.addStringValues("Jones");
    Assert.assertEquals(la.size(), 5);
    la.removeStringValues("Smith", "Johnson");
    Assert.assertEquals(la.size(), 3);
    la.removeStringValues(List.of("Williams", "Brown"));
    Assert.assertEquals(la.size(), 1);
    la.removeStringValues("Jones");
    Assert.assertEquals(la.size(), 0);
  }


  /** Test for add and then remove methods. */
  @Test
  public void testAddRemoveBinaryValues()
  {
    final LdapAttribute la = new LdapAttribute("jpegPhoto");
    la.addBinaryValues("image1".getBytes(), "image2".getBytes());
    Assert.assertEquals(la.size(), 2);
    la.addBinaryValues(List.of("image3".getBytes(), "image4".getBytes()));
    Assert.assertEquals(la.size(), 4);
    la.addBinaryValues("image5".getBytes());
    Assert.assertEquals(la.size(), 5);
    la.removeBinaryValues("image1".getBytes(), "image2".getBytes());
    Assert.assertEquals(la.size(), 3);
    la.removeBinaryValues(List.of("image3".getBytes(), "image4".getBytes()));
    Assert.assertEquals(la.size(), 1);
    la.removeBinaryValues("image5".getBytes());
    Assert.assertEquals(la.size(), 0);
  }


  /** Test for add and then remove methods. */
  @Test
  public void testAddRemoveBufferValues()
  {
    final LdapAttribute la = new LdapAttribute("jpegPhoto");
    la.addBufferValues(ByteBuffer.wrap("image1".getBytes()), ByteBuffer.wrap("image2".getBytes()));
    Assert.assertEquals(la.size(), 2);
    la.addBufferValues(List.of(ByteBuffer.wrap("image3".getBytes()), ByteBuffer.wrap("image4".getBytes())));
    Assert.assertEquals(la.size(), 4);
    la.addBufferValues(ByteBuffer.wrap("image5".getBytes()));
    Assert.assertEquals(la.size(), 5);
    la.removeBufferValues(ByteBuffer.wrap("image1".getBytes()), ByteBuffer.wrap("image2".getBytes()));
    Assert.assertEquals(la.size(), 3);
    la.removeBufferValues(List.of(ByteBuffer.wrap("image3".getBytes()), ByteBuffer.wrap("image4".getBytes())));
    Assert.assertEquals(la.size(), 1);
    la.removeBufferValues(ByteBuffer.wrap("image5".getBytes()));
    Assert.assertEquals(la.size(), 0);
  }


  /** Test for add and then remove methods. */
  @Test
  public void testAddValues()
  {
    final LdapAttribute la = new LdapAttribute("sn");
    la.addValues(s -> s.getBytes(StandardCharsets.UTF_8), "Smith", "Johnson");
    Assert.assertEquals(la.size(), 2);
    la.addValues(s -> s.getBytes(StandardCharsets.UTF_8), List.of("Williams", "Brown"));
    Assert.assertEquals(la.size(), 4);
    la.addValues(s -> s.getBytes(StandardCharsets.UTF_8), "Jones");
    Assert.assertEquals(la.size(), 5);
    la.removeStringValues("Smith", "Johnson");
    Assert.assertEquals(la.size(), 3);
    la.removeStringValues(List.of("Williams", "Brown"));
    Assert.assertEquals(la.size(), 1);
    la.removeStringValues("Jones");
    Assert.assertEquals(la.size(), 0);
  }
}
