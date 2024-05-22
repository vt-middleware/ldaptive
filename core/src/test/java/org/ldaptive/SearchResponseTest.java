/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.ad.control.DirSyncControl;
import org.ldaptive.asn1.DefaultDERBuffer;
import org.ldaptive.control.PagedResultsControl;
import org.ldaptive.control.PasswordPolicyControl;
import org.ldaptive.control.SortResponseControl;
import org.ldaptive.control.VirtualListViewResponseControl;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SearchResponse}.
 *
 * @author  Middleware Services
 */
public class SearchResponseTest
{


  /**
   * Search result done response test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          // success search result done response
          new byte[] {
            //preamble
            0x30, 0x0c, 0x02, 0x01, 0x02,
            // search result done
            0x65, 0x07,
            // success result
            0x0a, 0x01, 0x00,
            // no matched DN
            0x04, 0x00,
            // no diagnostic message
            0x04, 0x00},
          SearchResponse.builder().messageID(2)
            .resultCode(ResultCode.SUCCESS)
            .matchedDN("")
            .diagnosticMessage("").build(),
        },
        new Object[] {
          // success search result done response with 2 response controls
          new byte[] {
            //preamble
            0x30, 0x62, 0x02, 0x01, 0x02,
            // search result done
            0x65, 0x07,
            // success result
            0x0a, 0x01, 0x00,
            // no matched DN
            0x04, 0x00,
            // no diagnostic message
            0x04, 0x00,
            (byte) 0xa0, 0x54,
            // SEQ
            0x30, 0x1f,
            // sort response control OID
            0x04, 0x16, 0x31, 0x2e, 0x32, 0x2e, 0x38, 0x34, 0x30, 0x2e, 0x31, 0x31, 0x33, 0x35, 0x35, 0x36, 0x2e, 0x31,
            0x2e, 0x34, 0x2e, 0x34, 0x37, 0x34,
            // sort response control value
            0x04, 0x05, 0x30, 0x03, 0x0a, 0x01, 0x00,
            // SEQ
            0x30, 0x31,
            // virtual list view response control OID
            0x04, 0x18, 0x32, 0x2e, 0x31, 0x36, 0x2e, 0x38, 0x34, 0x30, 0x2e, 0x31, 0x2e, 0x31, 0x31, 0x33, 0x37, 0x33,
            0x30, 0x2e, 0x33, 0x2e, 0x34, 0x2e, 0x31, 0x30,
            // virtual list view response control value
            0x04, 0x15, 0x30, 0x13, 0x02, 0x01, 0x03, 0x02, 0x01, 0x04, 0x0a, 0x01, 0x00, 0x04, 0x08, 0x50, 0x3d, 0x16,
            (byte) 0xec, 0x13, 0x7f, 0x00, 0x00,
          },
          SearchResponse.builder().messageID(2)
            .resultCode(ResultCode.SUCCESS)
            .matchedDN("")
            .diagnosticMessage("")
            .controls(
              new SortResponseControl(ResultCode.SUCCESS, false),
              new VirtualListViewResponseControl(
                3,
                4,
                ResultCode.SUCCESS,
                new byte[] {
                  0x50, 0x3d, 0x16, (byte) 0xec, 0x13, 0x7f, 0x00, 0x00,
                })
            ).build(),
        },
        new Object[] {
          // success search result done response with 2 response controls, one is critical
          new byte[] {
            //preamble
            0x30, 0x65, 0x02, 0x01, 0x02,
            // search result done
            0x65, 0x07,
            // success result
            0x0a, 0x01, 0x00,
            // no matched DN
            0x04, 0x00,
            // no diagnostic message
            0x04, 0x00,
            (byte) 0xa0, 0x57,
            // SEQ
            0x30, 0x22,
            // sort response control OID
            0x04, 0x16, 0x31, 0x2e, 0x32, 0x2e, 0x38, 0x34, 0x30, 0x2e, 0x31, 0x31, 0x33, 0x35, 0x35, 0x36, 0x2e, 0x31,
            0x2e, 0x34, 0x2e, 0x34, 0x37, 0x34,
            // Criticality true
            0x01, 0x01, (byte) 0xff,
            // sort response control value
            0x04, 0x05, 0x30, 0x03, 0x0a, 0x01, 0x00,
            // SEQ
            0x30, 0x31,
            // virtual list view response control OID
            0x04, 0x18, 0x32, 0x2e, 0x31, 0x36, 0x2e, 0x38, 0x34, 0x30, 0x2e, 0x31, 0x2e, 0x31, 0x31, 0x33, 0x37, 0x33,
            0x30, 0x2e, 0x33, 0x2e, 0x34, 0x2e, 0x31, 0x30,
            // virtual list view response control value
            0x04, 0x15, 0x30, 0x13, 0x02, 0x01, 0x03, 0x02, 0x01, 0x04, 0x0a, 0x01, 0x00, 0x04, 0x08, 0x50, 0x3d, 0x16,
            (byte) 0xec, 0x13, 0x7f, 0x00, 0x00,
          },
          SearchResponse.builder().messageID(2)
            .resultCode(ResultCode.SUCCESS)
            .matchedDN("")
            .diagnosticMessage("")
            .controls(
              new SortResponseControl(ResultCode.SUCCESS, true),
              new VirtualListViewResponseControl(
                3,
                4,
                ResultCode.SUCCESS,
                new byte[] {
                  0x50, 0x3d, 0x16, (byte) 0xec, 0x13, 0x7f, 0x00, 0x00,
                })
            ).build(),
        },
        new Object[] {
          // success search result done response dirsync control
          new byte[] {
            //preamble
            0x30, 0x3c, 0x02, 0x01, 0x02,
            // search result done
            0x65, 0x07,
            // success result
            0x0a, 0x01, 0x00,
            // no matched DN
            0x04, 0x00,
            // no diagnostic message
            0x04, 0x00,
            // DirSyncControl
            (byte) 0xa0, 0x2e,
            // SEQ
            0x30, 0x2c,
            // OID  1.2.840.113556.1.4.841
            0x04, 0x16, 0x31, 0x2e, 0x32, 0x2e, 0x38, 0x34, 0x30, 0x2e, 0x31, 0x31, 0x33, 0x35, 0x35, 0x36, 0x2e, 0x31,
            0x2e, 0x34, 0x2e, 0x38, 0x34, 0x31,
            // DirSyncControlValue
            0x04, 0x12,
            // SEQ
            0x30, 0x10,
            // flags
            0x02, 0x01, 0x00,
            // maxAttrCount
            0x02, 0x01, 0x00,
            // cookie
            0x04, 0x08, (byte) 0xd6, (byte) 0x9b, 0x4f, (byte) 0xf2, (byte) 0x72, 0x01, 0x00, 0x00,
          },
          SearchResponse.builder().messageID(2)
            .resultCode(ResultCode.SUCCESS)
            .matchedDN("")
            .diagnosticMessage("")
            .controls(
                new DirSyncControl(
                  null,
                  new byte[] {
                    (byte) 0xd6, (byte) 0x9b, 0x4f, (byte) 0xf2, (byte) 0x72, 0x01, 0x00, 0x00,
                  },
                  0,
                  false)
                ).build(),
        },
        new Object[] {
          // success search result done response criticality dirsync control
          new byte[] {
            //preamble
            0x30, 0x3f, 0x02, 0x01, 0x02,
            // search result done
            0x65, 0x07,
            // success result
            0x0a, 0x01, 0x00,
            // no matched DN
            0x04, 0x00,
            // no diagnostic message
            0x04, 0x00,
            // DirSyncControl
            (byte) 0xa0, 0x31,
            // SEQ
            0x30, 0x2f,
            // OID  1.2.840.113556.1.4.841
            0x04, 0x16, 0x31, 0x2e, 0x32, 0x2e, 0x38, 0x34, 0x30, 0x2e, 0x31, 0x31, 0x33, 0x35, 0x35, 0x36, 0x2e, 0x31,
            0x2e, 0x34, 0x2e, 0x38, 0x34, 0x31,
            // Criticality true
            0x01, 0x01, (byte) 0xff,
            // DirSyncControlValue
            0x04, 0x12,
            // SEQ
            0x30, 0x10,
            // flags
            0x02, 0x01, 0x00,
            // maxAttrCount
            0x02, 0x01, 0x00,
            // cookie
            0x04, 0x08, (byte) 0xd6, (byte) 0x9b, 0x4f, (byte) 0xf2, (byte) 0x72, 0x01, 0x00, 0x00,
          },
          SearchResponse.builder().messageID(2)
            .resultCode(ResultCode.SUCCESS)
            .matchedDN("")
            .diagnosticMessage("")
            .controls(
                new DirSyncControl(
                  null,
                  new byte[] {
                    (byte) 0xd6, (byte) 0x9b, 0x4f, (byte) 0xf2, (byte) 0x72, 0x01, 0x00, 0x00,
                  },
                  0,
                  true)
                ).build(),
        },
        new Object[] {
          // referral search result done response with referrals
          new byte[] {
            // preamble
            0x30, 0x6f, 0x02, 0x01, 0x02,
            // search result done
            0x65, 0x6a,
            // referral result
            0x0a, 0x01, 0x0a,
            // matched DN
            0x04, 0x19, 0x6f, 0x75, 0x3d, 0x72, 0x65, 0x66, 0x65, 0x72, 0x72, 0x61, 0x6c, 0x73, 0x2c, 0x64, 0x63, 0x3d,
            0x76, 0x74, 0x2c, 0x64, 0x63, 0x3d, 0x65, 0x64, 0x75,
            // no diagnostic message
            0x04, 0x00,
            // referral URL
            (byte) 0xa3, 0x48, 0x04, 0x46, 0x6c, 0x64, 0x61, 0x70, 0x3a, 0x2f, 0x2f, 0x6c, 0x64, 0x61, 0x70, 0x2d, 0x74,
            0x65, 0x73, 0x74, 0x2d, 0x31, 0x2e, 0x6d, 0x69, 0x64, 0x64, 0x6c, 0x65, 0x77, 0x61, 0x72, 0x65, 0x2e, 0x76,
            0x74, 0x2e, 0x65, 0x64, 0x75, 0x3a, 0x31, 0x30, 0x33, 0x38, 0x39, 0x2f, 0x6f, 0x75, 0x3d, 0x70, 0x65, 0x6f,
            0x70, 0x6c, 0x65, 0x2c, 0x64, 0x63, 0x3d, 0x76, 0x74, 0x2c, 0x64, 0x63, 0x3d, 0x65, 0x64, 0x75, 0x3f, 0x3f,
            0x6f, 0x6e, 0x65},
          SearchResponse.builder().messageID(2)
            .resultCode(ResultCode.REFERRAL)
            .matchedDN("ou=referrals,dc=vt,dc=edu")
            .diagnosticMessage("")
            .referralURLs("ldap://ldap-test-1.middleware.vt.edu:10389/ou=people,dc=vt,dc=edu??one").build(),
        },
        new Object[] {
          // success search result done response with paged results
          new byte[] {
            // preamble
            0x30, 0x39, 0x02, 0x01, 0x02,
            // search result done
            0x65, 0x07,
            // success result
            0x0a, 0x01, 0x00,
            // no matched DN
            0x04, 0x00,
            // no diagnostic message
            0x04, 0x00,
            // response control
            (byte) 0xa0, 0x2b, 0x30, 0x29,
            // control oid
            0x04, 0x16, 0x31, 0x2e, 0x32, 0x2e, 0x38, 0x34, 0x30, 0x2e, 0x31, 0x31, 0x33, 0x35, 0x35, 0x36, 0x2e, 0x31,
            0x2e, 0x34, 0x2e, 0x33, 0x31, 0x39,
            // paged results value
            0x04, 0x0f, 0x30, 0x0d, 0x02, 0x01, 0x00, 0x04, 0x08, 0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00},
          SearchResponse.builder().messageID(2)
            .resultCode(ResultCode.SUCCESS)
            .matchedDN("")
            .diagnosticMessage("")
            .controls(
              new PagedResultsControl(0, new byte[] {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}, false))
            .build(),
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
  public void encode(final byte[] berValue, final SearchResponse response)
    throws Exception
  {
    Assert.assertEquals(new SearchResponse(new DefaultDERBuffer(berValue)), response);
  }


  /**
   * Smoke tests for search response with one entry.
   */
  @Test
  public void oneEntry()
  {
    final LdapEntry entry1 = LdapEntry.builder()
      .dn("uid=1").build();
    final SearchResponse sr = new SearchResponse();
    sr.addEntries(entry1);
    Assert.assertEquals(entry1, sr.getEntry());
    Assert.assertEquals(entry1, sr.getEntry("uid=1"));
    Assert.assertEquals(entry1, sr.getEntry("UID=1"));
    Assert.assertEquals(sr.entrySize(), 1);
    Assert.assertEquals(sr.getEntryDns().size(), 1);
    Assert.assertEquals(sr.getEntryDns().iterator().next(), "uid=1");
    Assert.assertEquals(sr, SearchResponse.builder().entry(entry1).build());
  }


  /**
   * Smoke tests for search response with two entries.
   */
  @Test
  public void twoEntries()
  {
    final LdapEntry entry1 = LdapEntry.builder()
      .dn("uid=1").build();
    final LdapEntry entry2 = LdapEntry.builder()
      .dn("uid=2").build();
    final SearchResponse sr = new SearchResponse();
    sr.addEntries(entry1, entry2);
    Assert.assertEquals(entry1, sr.getEntry("uid=1"));
    Assert.assertEquals(entry1, sr.getEntry("UID=1"));
    Assert.assertEquals(entry2, sr.getEntry("UID=2"));
    Assert.assertEquals(entry2, sr.getEntry("uid=2"));
    Assert.assertEquals(sr.entrySize(), 2);
    Assert.assertEquals(sr.getEntryDns().size(), 2);
    Assert.assertEquals(sr, SearchResponse.builder().entry(entry1, entry2).build());
  }


  /**
   * Tests ordered ldap entries.
   */
  @Test
  public void orderedEntries()
  {
    final LdapEntry entry1 = LdapEntry.builder()
      .dn("uid=1").build();
    final LdapEntry entry2 = LdapEntry.builder()
      .dn("uid=2").build();
    final SearchResponse sr = new SearchResponse();
    sr.addEntries(entry2, entry1);

    final LdapEntry[] entries = sr.getEntries().toArray(LdapEntry[]::new);
    Assert.assertEquals(entry2, entries[0]);
    Assert.assertEquals(entry1, entries[1]);
  }


  /**
   * Unit test for {@link SearchResponse#subResult(int, int)}.
   */
  @Test
  public void subResult()
  {
    final LdapEntry entry1 = LdapEntry.builder()
      .dn("uid=1").build();
    final LdapEntry entry2 = LdapEntry.builder()
      .dn("uid=2").build();
    final SearchResponse sr = new SearchResponse();
    sr.addEntries(entry2, entry1);
    Assert.assertEquals(sr.subResult(2, 2).entrySize(), 0);
    Assert.assertEquals(sr.subResult(1, 2).entrySize(), 1);
    Assert.assertEquals(sr.subResult(0, 2).entrySize(), 2);
    try {
      sr.subResult(-1, 1);
      Assert.fail("Should have thrown IndexOutOfBoundsException");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IndexOutOfBoundsException.class);
    }
    try {
      sr.subResult(0, 3);
      Assert.fail("Should have thrown IndexOutOfBoundsException");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IndexOutOfBoundsException.class);
    }
    try {
      sr.subResult(1, 0);
      Assert.fail("Should have thrown IndexOutOfBoundsException");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IndexOutOfBoundsException.class);
    }
  }


  /** Test {@link SearchResponse#equals(Object)}. */
  @Test
  public void testEquals()
  {
    final SearchResponse sr1 = SearchResponse.builder().build();
    Assert.assertEquals(sr1, sr1);
    Assert.assertEquals(SearchResponse.builder().build(), SearchResponse.builder().build());
    Assert.assertEquals(
      SearchResponse.builder()
        .messageID(1)
        .controls(new PasswordPolicyControl())
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("uid=1")
        .diagnosticMessage("sample message")
        .referralURLs("ldap://directory-1.ldaptive.org", "ldap://directory-2.ldaptive.org")
        .entry(LdapEntry.builder()
          .dn("uid=1,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("1").build())
          .build())
        .reference(SearchResultReference.builder().uris("ldap://directory-3.ldaptive.org").build())
        .build(),
      SearchResponse.builder()
        .messageID(1)
        .controls(new PasswordPolicyControl())
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("uid=1")
        .diagnosticMessage("sample message")
        .referralURLs("ldap://directory-1.ldaptive.org", "ldap://directory-2.ldaptive.org")
        .entry(LdapEntry.builder()
          .dn("uid=1,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("1").build())
          .build())
        .reference(SearchResultReference.builder().uris("ldap://directory-3.ldaptive.org").build())
        .build());
    Assert.assertNotEquals(
      SearchResponse.builder()
        .messageID(2)
        .controls(new PasswordPolicyControl())
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("uid=1")
        .diagnosticMessage("sample message")
        .referralURLs("ldap://directory-1.ldaptive.org", "ldap://directory-2.ldaptive.org")
        .entry(LdapEntry.builder()
          .dn("uid=1,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("1").build())
          .build())
        .reference(SearchResultReference.builder().uris("ldap://directory-3.ldaptive.org").build())
        .build(),
      SearchResponse.builder()
        .messageID(1)
        .controls(new PasswordPolicyControl())
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("uid=1")
        .diagnosticMessage("sample message")
        .referralURLs("ldap://directory-1.ldaptive.org", "ldap://directory-2.ldaptive.org")
        .entry(LdapEntry.builder()
          .dn("uid=1,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("1").build())
          .build())
        .reference(SearchResultReference.builder().uris("ldap://directory-3.ldaptive.org").build())
        .build());
    Assert.assertNotEquals(
      SearchResponse.builder()
        .messageID(1)
        .controls(new PasswordPolicyControl())
        .resultCode(ResultCode.NO_SUCH_OBJECT)
        .matchedDN("uid=1")
        .diagnosticMessage("sample message")
        .referralURLs("ldap://directory-1.ldaptive.org", "ldap://directory-2.ldaptive.org")
        .entry(LdapEntry.builder()
          .dn("uid=1,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("1").build())
          .build())
        .reference(SearchResultReference.builder().uris("ldap://directory-3.ldaptive.org").build())
        .build(),
      SearchResponse.builder()
        .messageID(1)
        .controls(new PasswordPolicyControl())
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("uid=1")
        .diagnosticMessage("sample message")
        .referralURLs("ldap://directory-1.ldaptive.org", "ldap://directory-2.ldaptive.org")
        .entry(LdapEntry.builder()
          .dn("uid=1,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("1").build())
          .build())
        .reference(SearchResultReference.builder().uris("ldap://directory-3.ldaptive.org").build())
        .build());
    Assert.assertNotEquals(
      SearchResponse.builder()
        .messageID(1)
        .controls(new PasswordPolicyControl())
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("uid=2")
        .diagnosticMessage("sample message")
        .referralURLs("ldap://directory-1.ldaptive.org", "ldap://directory-2.ldaptive.org")
        .entry(LdapEntry.builder()
          .dn("uid=1,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("1").build())
          .build())
        .reference(SearchResultReference.builder().uris("ldap://directory-3.ldaptive.org").build())
        .build(),
      SearchResponse.builder()
        .messageID(1)
        .controls(new PasswordPolicyControl())
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("uid=1")
        .diagnosticMessage("sample message")
        .referralURLs("ldap://directory-1.ldaptive.org", "ldap://directory-2.ldaptive.org")
        .entry(LdapEntry.builder()
          .dn("uid=1,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("1").build())
          .build())
        .reference(SearchResultReference.builder().uris("ldap://directory-3.ldaptive.org").build())
        .build());
    Assert.assertNotEquals(
      SearchResponse.builder()
        .messageID(1)
        .controls(new PasswordPolicyControl())
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("uid=1")
        .diagnosticMessage("sample message 2")
        .referralURLs("ldap://directory-1.ldaptive.org", "ldap://directory-2.ldaptive.org")
        .entry(LdapEntry.builder()
          .dn("uid=1,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("1").build())
          .build())
        .reference(SearchResultReference.builder().uris("ldap://directory-3.ldaptive.org").build())
        .build(),
      SearchResponse.builder()
        .messageID(1)
        .controls(new PasswordPolicyControl())
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("uid=1")
        .diagnosticMessage("sample message")
        .referralURLs("ldap://directory-1.ldaptive.org", "ldap://directory-2.ldaptive.org")
        .entry(LdapEntry.builder()
          .dn("uid=1,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("1").build())
          .build())
        .reference(SearchResultReference.builder().uris("ldap://directory-3.ldaptive.org").build())
        .build());
    Assert.assertNotEquals(
      SearchResponse.builder()
        .messageID(1)
        .controls(new PasswordPolicyControl())
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("uid=1")
        .diagnosticMessage("sample message")
        .referralURLs("ldap://directory.ldaptive.org", "ldap://directory-2.ldaptive.org")
        .entry(LdapEntry.builder()
          .dn("uid=1,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("1").build())
          .build())
        .reference(SearchResultReference.builder().uris("ldap://directory-3.ldaptive.org").build())
        .build(),
      SearchResponse.builder()
        .messageID(1)
        .controls(new PasswordPolicyControl())
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("uid=1")
        .diagnosticMessage("sample message")
        .referralURLs("ldap://directory-1.ldaptive.org", "ldap://directory-2.ldaptive.org")
        .entry(LdapEntry.builder()
          .dn("uid=1,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("1").build())
          .build())
        .reference(SearchResultReference.builder().uris("ldap://directory-3.ldaptive.org").build())
        .build());
    Assert.assertNotEquals(
      SearchResponse.builder()
        .messageID(1)
        .controls(new PasswordPolicyControl())
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("uid=1")
        .diagnosticMessage("sample message")
        .referralURLs("ldap://directory-1.ldaptive.org", "ldap://directory-2.ldaptive.org")
        .entry(LdapEntry.builder()
          .dn("uid=2,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("1").build())
          .build())
        .reference(SearchResultReference.builder().uris("ldap://directory-3.ldaptive.org").build())
        .build(),
      SearchResponse.builder()
        .messageID(1)
        .controls(new PasswordPolicyControl())
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("uid=1")
        .diagnosticMessage("sample message")
        .referralURLs("ldap://directory-1.ldaptive.org", "ldap://directory-2.ldaptive.org")
        .entry(LdapEntry.builder()
          .dn("uid=1,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("1").build())
          .build())
        .reference(SearchResultReference.builder().uris("ldap://directory-3.ldaptive.org").build())
        .build());
    Assert.assertNotEquals(
      SearchResponse.builder()
        .messageID(1)
        .controls(new PasswordPolicyControl())
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("uid=1")
        .diagnosticMessage("sample message")
        .referralURLs("ldap://directory-1.ldaptive.org", "ldap://directory-2.ldaptive.org")
        .entry(LdapEntry.builder()
          .dn("uid=1,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uuid").values("1").build())
          .build())
        .reference(SearchResultReference.builder().uris("ldap://directory-3.ldaptive.org").build())
        .build(),
      SearchResponse.builder()
        .messageID(1)
        .controls(new PasswordPolicyControl())
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("uid=1")
        .diagnosticMessage("sample message")
        .referralURLs("ldap://directory-1.ldaptive.org", "ldap://directory-2.ldaptive.org")
        .entry(LdapEntry.builder()
          .dn("uid=1,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("1").build())
          .build())
        .reference(SearchResultReference.builder().uris("ldap://directory-3.ldaptive.org").build())
        .build());
    Assert.assertNotEquals(
      SearchResponse.builder()
        .messageID(1)
        .controls(new PasswordPolicyControl())
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("uid=1")
        .diagnosticMessage("sample message")
        .referralURLs("ldap://directory-1.ldaptive.org", "ldap://directory-2.ldaptive.org")
        .entry(LdapEntry.builder()
          .dn("uid=1,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("2").build())
          .build())
        .reference(SearchResultReference.builder().uris("ldap://directory-3.ldaptive.org").build())
        .build(),
      SearchResponse.builder()
        .messageID(1)
        .controls(new PasswordPolicyControl())
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("uid=1")
        .diagnosticMessage("sample message")
        .referralURLs("ldap://directory-1.ldaptive.org", "ldap://directory-2.ldaptive.org")
        .entry(LdapEntry.builder()
          .dn("uid=1,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("1").build())
          .build())
        .reference(SearchResultReference.builder().uris("ldap://directory-3.ldaptive.org").build())
        .build());
    Assert.assertNotEquals(
      SearchResponse.builder()
        .messageID(1)
        .controls(new PasswordPolicyControl())
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("uid=1")
        .diagnosticMessage("sample message")
        .referralURLs("ldap://directory-1.ldaptive.org", "ldap://directory-2.ldaptive.org")
        .entry(LdapEntry.builder()
          .dn("uid=1,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("1").build())
          .build())
        .reference(SearchResultReference.builder().uris("ldap://directory-4.ldaptive.org").build())
        .build(),
      SearchResponse.builder()
        .messageID(1)
        .controls(new PasswordPolicyControl())
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("uid=1")
        .diagnosticMessage("sample message")
        .referralURLs("ldap://directory-1.ldaptive.org", "ldap://directory-2.ldaptive.org")
        .entry(LdapEntry.builder()
          .dn("uid=1,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("1").build())
          .build())
        .reference(SearchResultReference.builder().uris("ldap://directory-3.ldaptive.org").build())
        .build());
  }


  @Test
  public void immutable()
  {
    final SearchResponse response = SearchResponse.builder()
      .messageID(1)
      .controls(new SortResponseControl())
      .entry(
        LdapEntry.builder()
          .messageID(1)
          .dn("uid=1,ou=people,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("givenName").values("bob", "robert").build())
          .attributes(LdapAttribute.builder().name("sn").values("baker").build())
          .build())
      .reference(
        SearchResultReference.builder()
          .messageID(1)
          .uris("ldap://ds1.ldaptive.org", "ldap://ds2.ldaptive.org")
          .build())
      .build();

    response.assertMutable();
    try {
      response.setMessageID(0);
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
    try {
      response.addControls(new SortResponseControl());
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
    try {
      response.setDiagnosticMessage("foo");
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
    try {
      response.setMatchedDN("bar");
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
    try {
      response.setResultCode(ResultCode.LOCAL_ERROR);
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
    response.addEntries(
      LdapEntry.builder()
        .messageID(1)
        .dn("uid=2,ou=people,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("givenName").values("bill", "billy").build())
        .attributes(LdapAttribute.builder().name("sn").values("thompson").build())
        .build());
    response.addReferences(SearchResultReference.builder().uris("ldap://ds3.ldaptive.org").build());
    response.getEntry().setDn("uid=1,ou=robots,dc=ldaptive,dc=org");
    response.getEntry().getAttribute("givenName").addStringValues("rob");

    response.freeze();
    try {
      response.addEntries(LdapEntry.builder()
        .messageID(1)
        .dn("uid=3,ou=people,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("givenName").values("ben").build())
        .build());
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
    try {
      response.getEntry().setDn("uid=1,ou=aliens,dc=ldaptive,dc=org");
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
    try {
      response.getEntry().getAttribute("givenName").addStringValues("william");
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
    try {
      response.getReference().addUris("ldap://ds4.ldaptive.org");
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
  }


  @Test
  public void copy()
  {
    final SearchResponse response1 = SearchResponse.builder()
      .messageID(1)
      .controls(new SortResponseControl())
      .entry(
        LdapEntry.builder()
          .messageID(1)
          .dn("uid=1,ou=people,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("givenName").values("bob", "robert").build())
          .attributes(LdapAttribute.builder().name("sn").values("baker").build())
          .build())
      .reference(
        SearchResultReference.builder()
          .uris("ldap://ds1.ldaptive.org")
          .build())
      .build();
    final SearchResponse copy1 = SearchResponse.copy(response1);
    Assert.assertEquals(copy1, response1);
    Assert.assertFalse(response1.isFrozen());
    Assert.assertFalse(copy1.isFrozen());

    final SearchResponse response2 = SearchResponse.builder()
      .messageID(1)
      .controls(new SortResponseControl())
      .entry(
        LdapEntry.builder()
          .messageID(1)
          .dn("uid=1,ou=people,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("givenName").values("bob", "robert").build())
          .attributes(LdapAttribute.builder().name("sn").values("baker").build())
          .build())
      .reference(
        SearchResultReference.builder()
          .uris("ldap://ds1.ldaptive.org")
          .build())
      .freeze()
      .build();
    final SearchResponse copy2 = SearchResponse.copy(response2);
    Assert.assertEquals(copy2, response2);
    Assert.assertTrue(response2.isFrozen());
    Assert.assertFalse(copy2.isFrozen());
  }


  @Test
  public void sort()
  {
    final SearchResponse response = SearchResponse.builder()
      .messageID(1)
      .controls(new SortResponseControl())
      .entry(
        LdapEntry.builder()
          .messageID(1)
          .dn("uid=bob,ou=people,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("givenName").values("bob", "robert").build())
          .attributes(LdapAttribute.builder().name("sn").values("baker").build())
          .build(),
        LdapEntry.builder()
          .messageID(1)
          .dn("uid=alice,ou=people,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("givenName").values("allison", "alice").build())
          .attributes(LdapAttribute.builder().name("sn").values("abare").build())
          .build())
      .reference(
        SearchResultReference.builder()
          .uris("ldap://ds1.ldaptive.org")
          .build(),
        SearchResultReference.builder()
          .uris("ldap://directory-1.ldaptive.org")
          .build())
      .build();
    final SearchResponse sort = SearchResponse.sort(response);
    Assert.assertNotEquals(sort, response);
    Assert.assertEquals(sort.getEntry().getDn(), "uid=alice,ou=people,dc=ldaptive,dc=org");
    Assert.assertEquals(sort.getEntry().getAttribute("givenName").getStringValue(), "alice");
    Assert.assertEquals(sort.getReference().getUris()[0], "ldap://directory-1.ldaptive.org");
  }


  /*
  @Test
  public void merge()
  {
    final SearchResponse response = SearchResponse.builder()
      .messageID(1)
      .controls(new SortResponseControl())
      .entry(
        LdapEntry.builder()
          .messageID(1)
          .dn("uid=bob,ou=people,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("givenName").values("bob", "robert").build())
          .attributes(LdapAttribute.builder().name("sn").values("baker").build())
          .build(),
        LdapEntry.builder()
          .messageID(1)
          .dn("uid=alice,ou=people,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("givenName").values("allison", "alice").build())
          .attributes(LdapAttribute.builder().name("sn").values("abare").build())
          .build())
      .reference(
        SearchResultReference.builder()
          .uris("ldap://ds1.ldaptive.org")
          .build(),
        SearchResultReference.builder()
          .uris("ldap://directory-1.ldaptive.org")
          .build())
      .build();
    final SearchResponse merge = SearchResponse.merge(response);
    Assert.assertNotEquals(merge, response);
    Assert.assertEquals(merge.getEntry().getDn(), "uid=bob,ou=people,dc=ldaptive,dc=org");
    Assert.assertEquals(
      merge.getEntry().getAttribute("givenName").getStringValues(),
      List.of("bob", "robert", "allison", "alice"));
    Assert.assertEquals(
      merge.getEntry().getAttribute("sn").getStringValues(),
      List.of("baker", "abare"));
    Assert.assertEquals(
      Arrays.asList(merge.getReference().getUris()),
      List.of("ldap://ds1.ldaptive.org", "ldap://directory-1.ldaptive.org"));
  }

   */
}
