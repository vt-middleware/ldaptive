/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link LdapAttribute}.
 *
 * @author  Middleware Services
 */
public class LdapAttributeTest
{


  /** Tests create with no value. */
  @Test
  public void nullName()
  {
    try {
      new LdapAttribute(null);
      fail("Should have thrown IllegalArgumentException");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    try {
      new LdapAttribute(null, "Jones");
      fail("Should have thrown IllegalArgumentException");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    try {
      new LdapAttribute(null, "Jones".getBytes(StandardCharsets.UTF_8));
      fail("Should have thrown IllegalArgumentException");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
  }


  /** Tests create with no value. */
  @Test
  public void noValue()
  {
    final LdapAttribute la = new LdapAttribute("givenName");
    assertThat(la.getName()).isEqualTo("givenName");
    assertThat(la.getOptions()).isEmpty();
    assertThat(la.getStringValue()).isNull();
    assertThat(la.getBinaryValue()).isNull();
    assertThat(la.getValue((Function<byte[], Object>) b -> b)).isNull();
    assertThat(la.getStringValues()).isEmpty();
    assertThat(la.getBinaryValues()).isEmpty();
    assertThat(la.getValues((Function<byte[], Object>) b -> b)).isEmpty();
    assertThat(la).isEqualTo(new LdapAttribute("givenName"));
    try {
      la.addStringValues((String[]) null);
      fail("Should have thrown NullPointerException");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(NullPointerException.class);
    }
    assertThat(la.size()).isEqualTo(0);
    la.clear();
    assertThat(la.size()).isEqualTo(0);
  }


  /** Tests create with one value. */
  @Test
  public void oneValue()
  {
    final LdapAttribute la = new LdapAttribute("cn", "William Wallace");
    assertThat(la.getName()).isEqualTo("cn");
    assertThat(la.getOptions()).isEmpty();
    assertThat(la.getStringValue()).isEqualTo("William Wallace");
    assertThat(la.getStringValues())
      .hasSize(1)
      .containsExactly("William Wallace");
    assertThat(la.getBinaryValue()).isEqualTo("William Wallace".getBytes(StandardCharsets.UTF_8));
    assertThat(la.getBinaryValues())
      .hasSize(1)
      .containsExactly("William Wallace".getBytes(StandardCharsets.UTF_8));
    assertThat(la.size()).isEqualTo(1);
    assertThat(la).isEqualTo(new LdapAttribute("cn", "William Wallace"));
    try {
      la.addStringValues((String[]) null);
      fail("Should have thrown NullPointerException");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(NullPointerException.class);
    }
    la.addStringValues((String) null);
    assertThat(la.size()).isEqualTo(1);
    la.clear();
    assertThat(la.size()).isEqualTo(0);
  }


  /** Tests create with two values. */
  @Test
  public void twoValues()
  {
    final LdapAttribute la = new LdapAttribute("givenName", "Bill", "William");
    assertThat(la.getOptions()).isEmpty();
    assertThat(la.getStringValues()).hasSize(2);
    assertThat(la).isEqualTo(new LdapAttribute("givenName", "Bill", "William"));
    assertThat(la.size()).isEqualTo(2);
    la.clear();
    assertThat(la.size()).isEqualTo(0);
  }


  /** Tests create with same values. */
  @Test
  public void sameValues()
  {
    final LdapAttribute la = new LdapAttribute("givenName", "same", "same");
    assertThat(la.getOptions()).isEmpty();
    assertThat(la.getStringValues()).hasSize(1);
    assertThat(la.getBinaryValues()).hasSize(1);
    assertThat(la).isEqualTo(new LdapAttribute("givenName", "same", "same", "same"));
    assertThat(la.size()).isEqualTo(1);
    la.clear();
    assertThat(la.size()).isEqualTo(0);
  }


  /** Tests for {@link LdapAttribute#addStringValues(String...)}. */
  @Test
  public void addStringValue()
  {
    final LdapAttribute la = new LdapAttribute("cn", "William Wallace");
    assertThat(la.getStringValue()).isEqualTo("William Wallace");
    assertThat(la.getBinaryValue()).isEqualTo("William Wallace".getBytes(StandardCharsets.UTF_8));
    assertThat(la.getStringValues()).hasSize(1);
    assertThat(la.getBinaryValues()).hasSize(1);
    assertThat(la).isEqualTo(new LdapAttribute("cn", "William Wallace"));
    try {
      la.addStringValues((String[]) null);
      fail("Should have thrown NullPointerException");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(NullPointerException.class);
    }
    la.addStringValues((String) null);
    la.addBinaryValues("Bill".getBytes(StandardCharsets.UTF_8));
    assertThat(la.getBinaryValues()).hasSize(2);
    assertThat(la.getStringValues())
      .hasSize(2)
      .containsExactly("William Wallace", "Bill");
    assertThat(la.getBinaryValues())
      .anyMatch(b -> Arrays.equals(b, "Bill".getBytes(StandardCharsets.UTF_8)))
      .anyMatch(b -> Arrays.equals(b, "William Wallace".getBytes(StandardCharsets.UTF_8)));
    la.clear();
    assertThat(la.size()).isEqualTo(0);
  }


  /** Tests for {@link LdapAttribute#addBinaryValues(byte[]...)}. */
  @Test
  public void addBinaryValue()
  {
    final LdapAttribute la = new LdapAttribute("jpegPhoto", "image".getBytes());
    assertThat(Arrays.equals("image".getBytes(), la.getBinaryValue())).isTrue();
    assertThat(la.getStringValue()).isEqualTo("aW1hZ2U=");
    assertThat(la.getBinaryValues()).hasSize(1);
    assertThat(la.getStringValues()).hasSize(1);
    assertThat(la).isEqualTo(new LdapAttribute("jpegPhoto", "image".getBytes()));
    try {
      la.addBinaryValues((byte[][]) null);
      fail("Should have thrown NullPointerException");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(NullPointerException.class);
    }
    la.addBinaryValues((byte[]) null);
    la.addStringValues("QmlsbA==");
    assertThat(la.getBinaryValues()).hasSize(2);
    assertThat(la.getStringValues())
      .hasSize(2)
      .containsExactly("aW1hZ2U=", "QmlsbA==");
    assertThat(la.getBinaryValues())
      .anyMatch(b -> Arrays.equals(b, "Bill".getBytes(StandardCharsets.UTF_8)))
      .anyMatch(b -> Arrays.equals(b, "image".getBytes()));
    la.clear();
    assertThat(la.size()).isEqualTo(0);
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
    assertThat(la.getStringValue()).isNotNull();
    assertThat(la.getStringValues())
      .isNotNull()
      .hasSize(2)
      .containsExactly("Bill Wallace", "William Wallace")
      .containsExactly(commonNames.toArray(new String[2]));
    assertThat(la.getBinaryValue()).isNotNull();
    assertThat(la.getBinaryValues())
      .isNotNull()
      .hasSize(2);
    assertThat(binaryCommonNames).hasSize(la.getBinaryValues().size());
    for (byte[] b : binaryCommonNames) {
      assertThat(la.getBinaryValues()).anyMatch(a -> Arrays.equals(a, b));
    }
    la.clear();
    assertThat(la.size()).isEqualTo(0);
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
    assertThat(la.getStringValue()).isNotNull();
    assertThat(la.getStringValues())
      .isNotNull()
      .hasSize(2)
      .containsExactly("aW1hZ2Ux", "aW1hZ2Uy")
      .containsExactly(stringJpegPhotos.toArray(new String[2]));
    assertThat(la.getBinaryValue()).isNotNull();
    assertThat(la.getBinaryValues())
      .isNotNull()
      .hasSize(2)
      .hasSize(jpegPhotos.size());
    for (byte[] b : jpegPhotos) {
      assertThat(la.getBinaryValues()).anyMatch(a -> Arrays.equals(a, b));
    }
    la.clear();
    assertThat(la.size()).isEqualTo(0);
  }


  /** Tests attribute options. */
  @Test
  public void attributeOptions()
  {
    LdapAttribute la = new LdapAttribute("cn", "William Wallace");
    assertThat(la.getName()).isEqualTo("cn");
    assertThat(la.getName(true)).isEqualTo("cn");
    assertThat(la.getName(false)).isEqualTo("cn");
    assertThat(la.getOptions())
      .isNotNull()
      .isEmpty();

    la = new LdapAttribute("cn;lang-ru", "Уильям Уоллес");
    assertThat(la.getName()).isEqualTo("cn;lang-ru");
    assertThat(la.getName(true)).isEqualTo("cn;lang-ru");
    assertThat(la.getName(false)).isEqualTo("cn");
    assertThat(la.getOptions())
      .isNotNull()
      .hasSize(1)
      .containsExactly("lang-ru");

    la = new LdapAttribute("cn;lang-lv;dynamic", "Viljams Voless");
    assertThat(la.getName()).isEqualTo("cn;lang-lv;dynamic");
    assertThat(la.getName(true)).isEqualTo("cn;lang-lv;dynamic");
    assertThat(la.getName(false)).isEqualTo("cn");
    assertThat(la.getOptions())
      .isNotNull()
      .hasSize(2)
      .containsExactly("lang-lv", "dynamic");
  }


  /** Test for {@link LdapAttribute#equals(Object)}. */
  @Test
  public void testEquals()
  {
    final LdapAttribute la1 = LdapAttribute.builder().build();
    assertThat(la1).isEqualTo(la1);
    assertThat(LdapAttribute.builder().build()).isEqualTo(LdapAttribute.builder().build());
    assertThat(LdapAttribute.builder().name("uid").values("1").build())
      .isEqualTo(LdapAttribute.builder().name("uid").values("1").build());
    assertThat(LdapAttribute.builder().name("uid").values("1").binary(true).build())
      .isEqualTo(LdapAttribute.builder().name("uid").values("1").binary(true).build());
    assertThat(LdapAttribute.builder().name("uid").values("1").binary(true).build())
      .isEqualTo(LdapAttribute.builder().name("uid").values("1").binary(false).build());
    assertThat(LdapAttribute.builder().name("uid").values("1", "2", "3").build())
      .isEqualTo(LdapAttribute.builder().name("uid").values("1", "2", "3").build());
    assertThat(LdapAttribute.builder().name("uid").values("1", "2", "3").build())
      .isEqualTo(LdapAttribute.builder().name("UID").values("1", "2", "3").build());
    assertThat(LdapAttribute.builder().name("uuid").values("1").build())
      .isNotEqualTo(LdapAttribute.builder().name("uid").values("1").build());
    assertThat(LdapAttribute.builder().name("uid").values("2").build())
      .isNotEqualTo(LdapAttribute.builder().name("uid").values("1").build());
    assertThat(LdapAttribute.builder().name("uid").values("1", "2", "3" , "4").build())
      .isNotEqualTo(LdapAttribute.builder().name("uid").values("1", "2", "3").build());
  }


  /** Test for add and then remove methods. */
  @Test
  public void testAddRemoveStringValues()
  {
    final LdapAttribute la = new LdapAttribute("sn");
    la.addStringValues("Smith", "Johnson");
    assertThat(la.size()).isEqualTo(2);
    la.addStringValues(List.of("Williams", "Brown"));
    assertThat(la.size()).isEqualTo(4);
    la.addStringValues("Jones");
    assertThat(la.size()).isEqualTo(5);
    la.removeStringValues("Smith", "Johnson");
    assertThat(la.size()).isEqualTo(3);
    la.removeStringValues(List.of("Williams", "Brown"));
    assertThat(la.size()).isEqualTo(1);
    la.removeStringValues("Jones");
    assertThat(la.size()).isEqualTo(0);
  }


  /** Test for add and then remove methods. */
  @Test
  public void testAddRemoveBinaryValues()
  {
    final LdapAttribute la = new LdapAttribute("jpegPhoto");
    la.addBinaryValues("image1".getBytes(), "image2".getBytes());
    assertThat(la.size()).isEqualTo(2);
    la.addBinaryValues(List.of("image3".getBytes(), "image4".getBytes()));
    assertThat(la.size()).isEqualTo(4);
    la.addBinaryValues("image5".getBytes());
    assertThat(la.size()).isEqualTo(5);
    la.removeBinaryValues("image1".getBytes(), "image2".getBytes());
    assertThat(la.size()).isEqualTo(3);
    la.removeBinaryValues(List.of("image3".getBytes(), "image4".getBytes()));
    assertThat(la.size()).isEqualTo(1);
    la.removeBinaryValues("image5".getBytes());
    assertThat(la.size()).isEqualTo(0);
  }


  /** Test for add and then remove methods. */
  @Test
  public void testAddRemoveValues()
  {
    final LdapAttribute la = new LdapAttribute("sn");
    la.addValues(s -> s.getBytes(StandardCharsets.UTF_8), "Smith", "Johnson");
    assertThat(la.size()).isEqualTo(2);
    assertThat(la.getStringValues()).containsExactly("Smith", "Johnson");
    la.addValues(s -> s.getBytes(StandardCharsets.UTF_8), List.of("Williams", "Brown"));
    assertThat(la.getStringValues()).containsExactly("Smith", "Johnson", "Williams", "Brown");
    assertThat(la.size()).isEqualTo(4);
    la.addValues(s -> s.getBytes(StandardCharsets.UTF_8), "Jones");
    assertThat(la.getStringValues()).containsExactly("Smith", "Johnson", "Williams", "Brown", "Jones");
    assertThat(la.size()).isEqualTo(5);
    la.removeValues(s -> s.getBytes(StandardCharsets.UTF_8), "Smith", "Johnson");
    assertThat(la.size()).isEqualTo(3);
    assertThat(la.getStringValues()).containsExactly("Williams", "Brown", "Jones");
    la.removeValues(s -> s.getBytes(StandardCharsets.UTF_8), List.of("Williams", "Brown"));
    assertThat(la.size()).isEqualTo(1);
    assertThat(la.getStringValues()).containsExactly("Jones");
    la.removeValues(s -> s.getBytes(StandardCharsets.UTF_8), "Jones");
    assertThat(la.size()).isEqualTo(0);
    assertThat(la.getStringValues()).isEmpty();
    assertThat(la.getBinaryValues()).isEmpty();
    assertThat(la.getValues(o -> new byte[0])).isEmpty();
  }


  /** Test for hasValue methods. */
  @Test
  public void testHasValue()
  {
    final LdapAttribute la1 = new LdapAttribute("sn", "Smith", "Johnson", "Williams", "Brown", "Jones");
    assertThat(la1.hasValue("Brown")).isTrue();
    assertThat(la1.hasValue("brown")).isFalse();
    assertThat(la1.hasValue("Brown".getBytes())).isTrue();
    assertThat(la1.hasValue("brown".getBytes())).isFalse();
    assertThat(la1.hasValue(s -> s.getBytes(StandardCharsets.UTF_8), "Brown")).isTrue();
    assertThat(la1.hasValue(s -> s.getBytes(StandardCharsets.UTF_8), "brown")).isFalse();

    final LdapAttribute la2 = new LdapAttribute("jpegPhoto", "image".getBytes());
    assertThat(la2.hasValue("aW1hZ2U=")).isTrue();
    assertThat(la2.hasValue("aW1hZ2")).isFalse();
    assertThat(la2.hasValue(" ")).isFalse();
  }


  @Test
  public void immutable()
  {
    final LdapAttribute attr = LdapAttribute.builder().name("givenName").values("bob").build();
    attr.assertMutable();
    attr.setName("gn");
    attr.setBinary(true);
    attr.addStringValues("robert");

    attr.freeze();
    try {
      attr.setName("sn");
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
    }
    try {
      attr.setBinary(true);
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
    }
    try {
      attr.addStringValues("foo");
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
    }
    try {
      attr.addStringValues(List.of("foo"));
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
    }
    try {
      attr.addBinaryValues("foo".getBytes(StandardCharsets.UTF_8));
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
    }
    try {
      attr.addBinaryValues(List.of("foo".getBytes(StandardCharsets.UTF_8)));
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
    }
    try {
      attr.addValues((Function<Object, byte[]>) o -> o.toString().getBytes(StandardCharsets.UTF_8), "foo");
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
    }
    try {
      attr.addValues((Function<Object, byte[]>) o -> o.toString().getBytes(StandardCharsets.UTF_8), List.of("foo"));
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
    }
    try {
      attr.removeStringValues("foo");
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
    }
    try {
      attr.removeStringValues(List.of("foo"));
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
    }
    try {
      attr.removeBinaryValues("foo".getBytes(StandardCharsets.UTF_8));
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
    }
    try {
      attr.removeBinaryValues(List.of("foo".getBytes(StandardCharsets.UTF_8)));
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
    }
    try {
      attr.removeValues((Function<Object, byte[]>) o -> o.toString().getBytes(StandardCharsets.UTF_8), "foo");
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
    }
    try {
      attr.removeValues((Function<Object, byte[]>) o -> o.toString().getBytes(StandardCharsets.UTF_8), List.of("foo"));
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
    }
    try {
      attr.getStringValues().add("foo");
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(UnsupportedOperationException.class);
    }
    try {
      attr.getBinaryValues().add("foo".getBytes(StandardCharsets.UTF_8));
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(UnsupportedOperationException.class);
    }
    try {
      attr.getValues((Function<byte[], Object>) bytes -> bytes).add("foo".getBytes(StandardCharsets.UTF_8));
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(UnsupportedOperationException.class);
    }
    try {
      attr.merge(new LdapAttribute("foo"));
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
    }

    final byte[] value = attr.getBinaryValue();
    value[0] = 0x01;
    assertThat(attr.getBinaryValue()).containsOnly(attr.getBinaryValue());
    assertThat(attr.getBinaryValue()).doesNotContain(0x01, 0);
  }


  /** Test for copy method. */
  @Test
  public void copy()
  {
    final LdapAttribute la1 = new LdapAttribute("sn", "Smith", "Johnson", "Williams", "Brown", "Jones");
    final LdapAttribute cp1 = LdapAttribute.copy(la1);
    assertThat(cp1).isEqualTo(la1);
    assertThat(la1.isFrozen()).isFalse();
    assertThat(cp1.isFrozen()).isFalse();

    final LdapAttribute la2 = LdapAttribute.builder()
      .name("jpegPhoto")
      .values("image".getBytes())
      .freeze()
      .build();
    final LdapAttribute cp2 = LdapAttribute.copy(la2);
    assertThat(cp2).isEqualTo(la2);
    assertThat(la2.isFrozen()).isTrue();
    assertThat(cp2.isFrozen()).isFalse();
  }


  /** Test for sort method. */
  @Test
  public void sort()
  {
    final LdapAttribute la1 = new LdapAttribute("sn", "Smith", "Johnson", "Williams", "Brown", "Jones");
    final LdapAttribute sort1 = LdapAttribute.sort(la1);
    assertThat(sort1).isEqualTo(la1);
    assertThat(sort1.getStringValues())
      .containsExactly("Brown", "Johnson", "Jones", "Smith", "Williams");

    final LdapAttribute la2 = new LdapAttribute(
      "bytes",
      new byte[] {0x05, 0x06, 0x07}, new byte[] {0x08, 0x09, 0x10}, new byte[] {0x02, 0x03, 0x04});
    final LdapAttribute sort2 = LdapAttribute.sort(la2);
    assertThat(sort2).isEqualTo(la2);
    assertThat(sort2.getBinaryValues())
      .containsExactly(new byte[] {0x02, 0x03, 0x04}, new byte[] {0x05, 0x06, 0x07}, new byte[] {0x08, 0x09, 0x10});
  }


  /** Test for merge method. */
  @Test
  public void merge()
  {
    final LdapAttribute la1 = LdapAttribute.builder().name("sn").values("Smith").build();
    la1.merge(new LdapAttribute());
    assertThat(la1).isEqualTo(LdapAttribute.builder().name("sn").values("Smith").build());
    la1.merge(LdapAttribute.builder().name("sn").values("Johnson", "Smith").build());
    assertThat(la1).isEqualTo(LdapAttribute.builder().name("sn").values("Smith", "Johnson").build());

    la1.merge(LdapAttribute.builder().name("Sn").values("Williams", "Brown").build());
    assertThat(la1)
      .isEqualTo(LdapAttribute.builder().name("sn").values("Smith", "Johnson",  "Williams", "Brown").build());

    la1.merge(LdapAttribute.builder().name("gn").values("Johnson", "Bob").build());
    assertThat(la1)
      .isEqualTo(LdapAttribute.builder().name("sn").values("Smith", "Johnson",  "Williams", "Brown", "Bob").build());

    final LdapAttribute la2 = LdapAttribute.builder()
      .name("jpegPhoto")
      .values("image1".getBytes())
      .binary(true)
      .build();
    la2.merge(LdapAttribute.builder().name("JPEGPHOTO").values("image2".getBytes()).binary(true).build());
    assertThat(la2)
      .isEqualTo(LdapAttribute.builder().name("jpegPhoto").values("image1".getBytes(), "image2".getBytes()).build());

    la2.merge(LdapAttribute.builder().name("jpegphoto").binary(false).values("image3").build());
    assertThat(la2)
      .isEqualTo(
        LdapAttribute.builder()
          .name("jpegPhoto")
          .values("image1".getBytes(), "image2".getBytes(), "image3".getBytes())
          .build());
  }


  /** Test for toString method. */
  @Test
  public void testToString()
  {
    assertThat(LdapAttribute.builder()
      .name("cn").values("William Wallace", "Bill Wallace").build().toString())
      .isEqualTo(
        "org.ldaptive.LdapAttribute@-1809968692::name=cn, values=[William Wallace, Bill Wallace], binary=false");

    assertThat(LdapAttribute.builder()
      .name("cn").binary(true).values("William Wallace".getBytes(), "Bill Wallace".getBytes()).build().toString())
      .isEqualTo(
        "org.ldaptive.LdapAttribute@-1809968692::" +
          "name=cn, values=[V2lsbGlhbSBXYWxsYWNl, QmlsbCBXYWxsYWNl], binary=true");

    assertThat(LdapAttribute.builder()
      .name("jpegPhoto").values("image".getBytes()).build().toString())
      .isEqualTo(
        "org.ldaptive.LdapAttribute@-1523536282::name=jpegPhoto, values=[aW1hZ2U=], binary=true");
  }
}
