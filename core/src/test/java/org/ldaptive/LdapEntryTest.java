/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.HashSet;
import java.util.Set;
import org.ldaptive.asn1.DefaultDERBuffer;
import org.ldaptive.control.SortResponseControl;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link LdapEntry}.
 *
 * @author  Middleware Services
 */
public class LdapEntryTest
{


  /**
   * Search result entry test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          // simple search with one entry
          new byte[] {
            //preamble
            0x30, 0x49, 0x02, 0x01, 0x02,
            // search result entry
            0x64, 0x44,
            // entry dn
            0x04, 0x11, 0x64, 0x63, 0x3d, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2c, 0x64, 0x63, 0x3d, 0x63, 0x6f,
            0x6d,
            // attribute list
            0x30, 0x2f,
            // sequence of name and value(s)
            0x30, 0x1c,
            0x04, 0x0b, 0x6f, 0x62, 0x6a, 0x65, 0x63, 0x74, 0x43, 0x6c, 0x61, 0x73, 0x73,
            0x31, 0x0d, 0x04, 0x03, 0x74, 0x6f, 0x70, 0x04, 0x06, 0x64, 0x6f, 0x6d, 0x61, 0x69, 0x6e,
            0x30, 0x0f,
            0x04, 0x02, 0x64, 0x63,
            0x31, 0x09, 0x04, 0x07, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65},
          LdapEntry.builder()
            .messageID(2)
            .dn("dc=example,dc=com")
            .attributes(new LdapAttribute("objectClass", "top", "domain"), new LdapAttribute("dc", "example")).build(),
        },
        new Object[] {
          // simple search with types only
          new byte[] {
            //preamble
            0x30, 0x33, 0x02, 0x01, 0x02,
            // search result entry
            0x64, 0x2e,
            // entry dn
            0x04, 0x11, 0x64, 0x63, 0x3d, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2c, 0x64, 0x63, 0x3d, 0x63, 0x6f,
            0x6d,
            // attribute list
            0x30, 0x19,
            // sequence of name and value(s)
            0x30, 0x0f,
            0x04, 0x0b, 0x6f, 0x62, 0x6a, 0x65, 0x63, 0x74, 0x43, 0x6c, 0x61, 0x73, 0x73,
            0x31, 0x00,
            0x30, 0x06,
            0x04, 0x02, 0x64, 0x63,
            0x31, 0x00},
          LdapEntry.builder()
            .messageID(2)
            .dn("dc=example,dc=com")
            .attributes(new LdapAttribute("objectClass"), new LdapAttribute("dc")).build(),
        },
        new Object[] {
          // longer entry
          new byte[] {
            //preamble
            0x30, (byte) 0x82, 0x01, 0x4b, 0x02, 0x01, 0x02,
            // search result entry
            0x64, (byte) 0x82, 0x01, 0x44,
            // entry dn
            0x04, 0x21, 0x75, 0x69, 0x64, 0x3D, 0x38, 0x31, 0x38, 0x30, 0x33, 0x37, 0x2C, 0x6F, 0x75, 0x3D, 0x50, 0x65,
            0x6F, 0x70, 0x6C, 0x65, 0x2C, 0x64, 0x63, 0x3D, 0x76, 0x74, 0x2C, 0x64, 0x63, 0x3D, 0x65, 0x64, 0x75,
            // attribute list
            0x30, (byte) 0x82, 0x01, 0x1d,
            // sequence of name and value(s)
            0x30, 0x23,
            0x04, 0x0B, 0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x43, 0x6C, 0x61, 0x73, 0x73,
            0x31, 0x14, 0x04, 0x12, 0x76, 0x69, 0x72, 0x67, 0x69, 0x6E, 0x69, 0x61, 0x54, 0x65, 0x63, 0x68, 0x50, 0x65,
            0x72, 0x73, 0x6F, 0x6E,
            0x30, 0x12,
            0x04, 0x05, 0x75, 0x75, 0x70, 0x69, 0x64,
            0x31, 0x09, 0x04, 0x07, 0x64, 0x66, 0x69, 0x73, 0x68, 0x65, 0x72,
            0x30, 0x0f,
            0x04, 0x03, 0x75, 0x69, 0x64,
            0x31, 0x08, 0x04, 0x06, 0x38, 0x31, 0x38, 0x30, 0x33, 0x37,
            0x30, 0x17,
            0x04, 0x02, 0x63, 0x6E,
            0x31, 0x11, 0x04, 0x0F, 0x44, 0x61, 0x6E, 0x69, 0x65, 0x6C, 0x20, 0x57, 0x20, 0x46, 0x69, 0x73, 0x68, 0x65,
            0x72,
            0x30, (byte) 0x81, (byte) 0xb7,
            0x04, 0x13, 0x73, 0x75, 0x70, 0x70, 0x72, 0x65, 0x73, 0x73, 0x65, 0x64, 0x41, 0x74, 0x74, 0x72, 0x69, 0x62,
            0x75, 0x74, 0x65,
            0x31, (byte) 0x81, (byte) 0x9F,
            0x04, 0x14, 0x6D, 0x61, 0x69, 0x6C, 0x50, 0x72, 0x65, 0x66, 0x65, 0x72, 0x72, 0x65, 0x64, 0x41, 0x64, 0x64,
            0x72, 0x65, 0x73, 0x73,
            0x04, 0x14, 0x75, 0x73, 0x65, 0x72, 0x53, 0x4D, 0x49, 0x4D, 0x45, 0x43, 0x65, 0x72, 0x74, 0x69, 0x66, 0x69,
            0x63, 0x61, 0x74, 0x65,
            0x04, 0x04, 0x6D, 0x61, 0x69, 0x6C,
            0x04, 0x0B, 0x6D, 0x61, 0x69, 0x6C, 0x41, 0x63, 0x63, 0x6F, 0x75, 0x6E, 0x74,
            0x04, 0x12, 0x6C, 0x6F, 0x63, 0x61, 0x6C, 0x50, 0x6F, 0x73, 0x74, 0x61, 0x6C, 0x41, 0x64, 0x64, 0x72, 0x65,
            0x73, 0x73,
            0x04, 0x15, 0x6D, 0x61, 0x69, 0x6C, 0x46, 0x6F, 0x72, 0x77, 0x61, 0x72, 0x64, 0x69, 0x6E, 0x67, 0x41, 0x64,
            0x64, 0x72, 0x65, 0x73, 0x73,
            0x04, 0x09, 0x68, 0x6F, 0x6D, 0x65, 0x50, 0x68, 0x6F, 0x6E, 0x65,
            0x04, 0x09, 0x6D, 0x61, 0x69, 0x6C, 0x41, 0x6C, 0x69, 0x61, 0x73,
            0x04, 0x11, 0x68, 0x6F, 0x6D, 0x65, 0x50, 0x6F, 0x73, 0x74, 0x61, 0x6C, 0x41, 0x64, 0x64, 0x72, 0x65, 0x73,
            0x73,
            0x04, 0x0A, 0x6C, 0x6F, 0x63, 0x61, 0x6C, 0x50, 0x68, 0x6F, 0x6E, 0x65,
          },
          LdapEntry.builder()
            .messageID(2)
            .dn("uid=818037,ou=People,dc=vt,dc=edu")
            .attributes(
              new LdapAttribute("uid", "818037"),
              new LdapAttribute("objectClass", "virginiaTechPerson"),
              new LdapAttribute("uupid", "dfisher"),
              new LdapAttribute("uid", "818037"),
              new LdapAttribute("cn", "Daniel W Fisher"),
              new LdapAttribute(
                "suppressedAttribute",
                "mailPreferredAddress",
                "userSMIMECertificate",
                "mail",
                "mailAccount",
                "localPostalAddress",
                "mailForwardingAddress",
                "homePhone",
                "mailAlias",
                "homePostalAddress",
                "localPhone")).build(),
        },
        new Object[] {
          // empty entry
          new byte[] {
            //preamble
            0x30, 0x09, 0x02, 0x01, 0x04,
            // search result entry
            0x64, 0x04,
            // entry dn
            0x04, 0x00,
            // attribute list
            0x30, 0x00},
          LdapEntry.builder()
            .messageID(4)
            .dn("").build(),
        },
      };
  }


  /**
   * @param  berValue  encoded response.
   * @param  response  expected decoded response.
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "response")
  public void encode(final byte[] berValue, final LdapEntry response)
    throws Exception
  {
    assertThat(new LdapEntry(new DefaultDERBuffer(berValue))).isEqualTo(response);
  }


  /**
   * Tests ordered ldap attribute values.
   */
  @Test
  public void orderedEntries()
  {
    final LdapAttribute attr1 = new LdapAttribute("givenName", "John");
    final LdapAttribute attr2 = new LdapAttribute("sn", "Doe");
    final LdapEntry le = new LdapEntry();
    le.addAttributes(attr2, attr1);

    final LdapAttribute[] attrs = le.getAttributes().toArray(LdapAttribute[]::new);
    assertThat(attr2).isEqualTo(attrs[0]);
    assertThat(attr1).isEqualTo(attrs[1]);
    le.clear();
    assertThat(le.size()).isEqualTo(0);
  }


  /**
   * Tests create with one entry.
   */
  @Test
  public void oneEntry()
  {
    final LdapAttribute attr1 = new LdapAttribute("givenName", "John");
    final LdapEntry le = LdapEntry.builder().dn("uid=1").attributes(attr1).build();
    assertThat(attr1)
      .isEqualTo(le.getAttribute())
      .isEqualTo(le.getAttribute("givenName"))
      .isEqualTo(le.getAttribute("givenname"));
    assertThat("givenName").isEqualTo(le.getAttributeNames()[0]);
    assertThat(le.size()).isEqualTo(1);
    assertThat(le).isEqualTo(LdapEntry.builder().dn("uid=1").attributes(attr1).build());
    le.clear();
    assertThat(le.size()).isEqualTo(0);
  }


  /**
   * Tests create with two entries.
   */
  @Test
  public void twoEntries()
  {
    final LdapAttribute attr1 = new LdapAttribute("givenName", "John");
    final LdapAttribute attr2 = new LdapAttribute("sn", "Doe");
    final LdapEntry le = LdapEntry.builder().dn("uid=1").attributes(attr2, attr1).build();
    assertThat(attr1)
      .isEqualTo(le.getAttribute("givenName"))
      .isEqualTo(le.getAttribute("GIVENNAME"));
    assertThat(attr2)
      .isEqualTo(le.getAttribute("sn"))
      .isEqualTo(le.getAttribute("SN"));
    assertThat(le.getAttributeNames().length).isEqualTo(2);
    assertThat(le.size()).isEqualTo(2);
    assertThat(le).isEqualTo(LdapEntry.builder().dn("uid=1").attributes(attr1, attr2).build());
    le.removeAttributes(attr2);
    assertThat(le.size()).isEqualTo(1);
    le.clear();
    assertThat(le.size()).isEqualTo(0);
  }


  /** Test for {@link LdapEntry#removeAttribute(String)}. */
  @Test
  public void removeAttribute()
  {
    final LdapAttribute attr1 = new LdapAttribute("givenName", "John");
    final LdapAttribute attr2 = new LdapAttribute("sn", "Doe");
    final Set<LdapAttribute> s = new HashSet<>();
    s.add(attr1);

    final LdapEntry le = LdapEntry.builder().dn("uid=1").attributes(s).build();
    le.addAttributes(attr2);
    le.removeAttribute("GIVENNAME");
    assertThat(le.size()).isEqualTo(1);
    le.clear();
    assertThat(le.size()).isEqualTo(0);
  }


  /** Test {@link LdapEntry#equals(Object)}. */
  @Test
  public void testEquals()
  {
    final LdapEntry le1 = LdapEntry.builder().build();
    assertThat(le1).isEqualTo(le1);
    assertThat(LdapEntry.builder().build()).isEqualTo(LdapEntry.builder().build());
    assertThat(
      LdapEntry.builder()
        .dn("uid=1,dc=ldaptive,dc=org")
        .attributes(
          LdapAttribute.builder().name("uid").values("1").build(),
          LdapAttribute.builder().name("givenName").values("Steve").build(),
          LdapAttribute.builder().name("sn").values("McQueen").build())
        .build()).isEqualTo(
      LdapEntry.builder()
        .dn("uid=1,dc=ldaptive,dc=org")
        .attributes(
          LdapAttribute.builder().name("uid").values("1").build(),
          LdapAttribute.builder().name("givenName").values("Steve").build(),
          LdapAttribute.builder().name("sn").values("McQueen").build())
        .build());
    assertThat(
      LdapEntry.builder()
        .dn("uid=1,dc=ldaptive,dc=org")
        .attributes(
          LdapAttribute.builder().name("uid").values("1").build(),
          LdapAttribute.builder().name("givenName").values("Steve").build(),
          LdapAttribute.builder().name("sn").values("McQueen").build())
        .build()).isEqualTo(
      LdapEntry.builder()
        .dn("UID=1,dc=ldaptive,dc=org")
        .attributes(
          LdapAttribute.builder().name("uid").values("1").build(),
          LdapAttribute.builder().name("givenName").values("Steve").build(),
          LdapAttribute.builder().name("SN").values("McQueen").build())
        .build());
    assertThat(
      LdapEntry.builder()
        .dn("uid=2,dc=ldaptive,dc=org")
        .attributes(
          LdapAttribute.builder().name("uid").values("1").build(),
          LdapAttribute.builder().name("givenName").values("Steve").build(),
          LdapAttribute.builder().name("sn").values("McQueen").build())
        .build()).isNotEqualTo(
      LdapEntry.builder()
        .dn("uid=1,dc=ldaptive,dc=org")
        .attributes(
          LdapAttribute.builder().name("uid").values("1").build(),
          LdapAttribute.builder().name("givenName").values("Steve").build(),
          LdapAttribute.builder().name("sn").values("McQueen").build())
        .build());
    assertThat(
      LdapEntry.builder()
        .dn("uid=1,dc=ldaptive,dc=org")
        .attributes(
          LdapAttribute.builder().name("uid").values("2").build(),
          LdapAttribute.builder().name("givenName").values("Steve").build(),
          LdapAttribute.builder().name("sn").values("McQueen").build())
        .build()).isNotEqualTo(
      LdapEntry.builder()
        .dn("uid=1,dc=ldaptive,dc=org")
        .attributes(
          LdapAttribute.builder().name("uid").values("1").build(),
          LdapAttribute.builder().name("givenName").values("Steve").build(),
          LdapAttribute.builder().name("sn").values("McQueen").build())
        .build());
  }


  /** Test {@link LdapEntry#computeModifications(LdapEntry, LdapEntry)}. */
  @Test
  public void testComputeModifications()
  {
    AttributeModification[] mods = LdapEntry.computeModifications(
      LdapEntry.builder()
        .dn("uid=1,ou=groups,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("member").values("fred").build())
        .build(),
      LdapEntry.builder()
        .dn("uid=1,ou=groups,dc=ldaptive,dc=org")
        .build(),
      true);
    assertThat(mods.length).isEqualTo(1);
    assertThat(mods[0].getOperation()).isEqualTo(AttributeModification.Type.ADD);
    assertThat(mods[0].getAttribute()).isEqualTo(LdapAttribute.builder().name("member").values("fred").build());

    mods = LdapEntry.computeModifications(
      LdapEntry.builder()
        .dn("uid=1,ou=groups,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("member").values("fred").build())
        .build(),
      LdapEntry.builder()
        .dn("uid=1,ou=groups,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("member").build())
        .build(),
      true);
    assertThat(mods.length).isEqualTo(1);
    assertThat(mods[0].getOperation()).isEqualTo(AttributeModification.Type.REPLACE);
    assertThat(mods[0].getAttribute()).isEqualTo(LdapAttribute.builder().name("member").values("fred").build());

    mods = LdapEntry.computeModifications(
      LdapEntry.builder()
        .dn("uid=1,ou=groups,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("member").values("fred").build())
        .build(),
      LdapEntry.builder()
        .dn("uid=1,ou=groups,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("member").build())
        .build(),
      false);
    assertThat(mods.length).isEqualTo(1);
    assertThat(mods[0].getOperation()).isEqualTo(AttributeModification.Type.ADD);
    assertThat(mods[0].getAttribute()).isEqualTo(LdapAttribute.builder().name("member").values("fred").build());

    mods = LdapEntry.computeModifications(
      LdapEntry.builder()
        .dn("uid=1,ou=groups,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("member").values("fred", "barney").build())
        .build(),
      LdapEntry.builder()
        .dn("uid=1,ou=groups,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("member").values("fred").build())
        .build(),
      true);
    assertThat(mods.length).isEqualTo(1);
    assertThat(mods[0].getOperation()).isEqualTo(AttributeModification.Type.REPLACE);
    assertThat(
      mods[0].getAttribute()).isEqualTo(LdapAttribute.builder().name("member").values("fred", "barney").build());

    mods = LdapEntry.computeModifications(
      LdapEntry.builder()
        .dn("uid=1,ou=groups,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("member").values("fred", "barney", "wilma").build())
        .build(),
      LdapEntry.builder()
        .dn("uid=1,ou=groups,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("member").values("fred", "barney").build())
        .build(),
      false);
    assertThat(mods.length).isEqualTo(1);
    assertThat(mods[0].getOperation()).isEqualTo(AttributeModification.Type.ADD);
    assertThat(
      mods[0].getAttribute()).isEqualTo(LdapAttribute.builder().name("member").values("wilma").build());

    mods = LdapEntry.computeModifications(
      LdapEntry.builder()
        .dn("uid=1,ou=groups,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("member").values("fred", "barney").build())
        .build(),
      LdapEntry.builder()
        .dn("uid=1,ou=groups,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("member").values("fred", "barney", "wilma").build())
        .build(),
      false);
    assertThat(mods.length).isEqualTo(1);
    assertThat(mods[0].getOperation()).isEqualTo(AttributeModification.Type.DELETE);
    assertThat(
      mods[0].getAttribute()).isEqualTo(LdapAttribute.builder().name("member").values("wilma").build());

    mods = LdapEntry.computeModifications(
      LdapEntry.builder()
        .dn("uid=1,ou=groups,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("member").values("fred").build())
        .build(),
      LdapEntry.builder()
        .dn("uid=1,ou=groups,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("member").values("fred", "barney").build())
        .build(),
      true);
    assertThat(mods.length).isEqualTo(1);
    assertThat(mods[0].getOperation()).isEqualTo(AttributeModification.Type.REPLACE);
    assertThat(
      mods[0].getAttribute()).isEqualTo(LdapAttribute.builder().name("member").values("fred").build());

    mods = LdapEntry.computeModifications(
      LdapEntry.builder()
        .dn("uid=1,ou=groups,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("member").build())
        .build(),
      LdapEntry.builder()
        .dn("uid=1,ou=groups,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("member").values("fred").build())
        .build(),
      true);
    assertThat(mods.length).isEqualTo(1);
    assertThat(mods[0].getOperation()).isEqualTo(AttributeModification.Type.REPLACE);
    assertThat(
      mods[0].getAttribute()).isEqualTo(LdapAttribute.builder().name("member").build());

    mods = LdapEntry.computeModifications(
      LdapEntry.builder()
        .dn("uid=1,ou=groups,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("member").build())
        .build(),
      LdapEntry.builder()
        .dn("uid=1,ou=groups,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("member").values("fred").build())
        .build(),
      false);
    assertThat(mods.length).isEqualTo(1);
    assertThat(mods[0].getOperation()).isEqualTo(AttributeModification.Type.DELETE);
    assertThat(
      mods[0].getAttribute()).isEqualTo(LdapAttribute.builder().name("member").values("fred").build());

    // use replace if source doesn't have the attribute and target has an empty attribute
    mods = LdapEntry.computeModifications(
      LdapEntry.builder()
        .dn("uid=1,ou=groups,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("member").build())
        .build(),
      LdapEntry.builder()
        .dn("uid=1,ou=groups,dc=ldaptive,dc=org")
        .build(),
      true);
    assertThat(mods.length).isEqualTo(1);
    assertThat(mods[0].getOperation()).isEqualTo(AttributeModification.Type.REPLACE);
    assertThat(
      mods[0].getAttribute()).isEqualTo(LdapAttribute.builder().name("member").build());
  }


  @Test
  public void immutable()
  {
    final LdapEntry entry = LdapEntry.builder()
      .messageID(1)
      .controls(new SortResponseControl())
      .attributes(LdapAttribute.builder().name("givenName").values("bob").build())
      .build();

    entry.assertMutable();
    entry.addAttributes(LdapAttribute.builder().name("sn").values("baker").build());
    entry.getAttribute("givenName").addStringValues("robert");

    entry.freeze();
    try {
      entry.addAttributes(LdapAttribute.builder().name("cn").values("robert baker").build());
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
    }
    try {
      entry.getAttribute("givenName").addStringValues("rob");
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
    }
  }


  @Test
  public void copy()
  {
    final LdapEntry entry1 = LdapEntry.builder()
      .messageID(1)
      .controls(new SortResponseControl())
      .attributes(LdapAttribute.builder().name("givenName").values("bob").build())
      .build();
    final LdapEntry copy1 = LdapEntry.copy(entry1);
    assertThat(copy1).isEqualTo(entry1);
    assertThat(entry1.isFrozen()).isFalse();
    assertThat(copy1.isFrozen()).isFalse();

    final LdapEntry entry2 = LdapEntry.builder()
      .messageID(2)
      .controls(new SortResponseControl())
      .dn("uid=bob,ou=people,dc=ldaptive,dc=org")
      .attributes(LdapAttribute.builder().name("givenName").values("bob").build())
      .freeze()
      .build();
    final LdapEntry copy2 = LdapEntry.copy(entry2);
    assertThat(copy2).isEqualTo(entry2);
    assertThat(entry2.isFrozen()).isTrue();
    assertThat(copy2.isFrozen()).isFalse();
  }


  /** Test for sort method. */
  @Test
  public void sort()
  {
    final LdapEntry entry = new LdapEntry();
    entry.setDn("uid=1,ou=people,dc=vt,dc=edu");
    entry.addAttributes(new LdapAttribute("sn", "Smith", "Johnson", "Williams", "Brown", "Jones"));
    entry.addAttributes(new LdapAttribute("giveName", "Bobby", "Bob", "Robert", "John", "James"));
    final LdapEntry sort = LdapEntry.sort(entry);
    assertThat(sort).isEqualTo(entry);
    assertThat(sort.getAttribute().getName()).isEqualTo("giveName");
    assertThat(sort.getAttribute().getStringValues()).containsExactly("Bob", "Bobby", "James", "John", "Robert");
  }
}
