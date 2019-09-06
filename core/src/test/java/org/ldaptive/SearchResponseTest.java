/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.asn1.DefaultDERBuffer;
import org.ldaptive.control.PagedResultsControl;
import org.ldaptive.control.PasswordPolicyControl;
import org.testng.Assert;
import org.testng.AssertJUnit;
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
    AssertJUnit.assertEquals(entry1, sr.getEntry());
    AssertJUnit.assertEquals(entry1, sr.getEntry("uid=1"));
    AssertJUnit.assertEquals(entry1, sr.getEntry("UID=1"));
    AssertJUnit.assertEquals(1, sr.entrySize());
    AssertJUnit.assertEquals(1, sr.getEntryDns().size());
    AssertJUnit.assertEquals("uid=1", sr.getEntryDns().iterator().next());
    AssertJUnit.assertEquals(sr, SearchResponse.builder().entry(entry1).build());
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
    AssertJUnit.assertEquals(entry1, sr.getEntry("uid=1"));
    AssertJUnit.assertEquals(entry1, sr.getEntry("UID=1"));
    AssertJUnit.assertEquals(entry2, sr.getEntry("UID=2"));
    AssertJUnit.assertEquals(entry2, sr.getEntry("uid=2"));
    AssertJUnit.assertEquals(2, sr.entrySize());
    AssertJUnit.assertEquals(2, sr.getEntryDns().size());
    AssertJUnit.assertEquals(sr, SearchResponse.builder().entry(entry1, entry2).build());
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
    AssertJUnit.assertEquals(entry2, entries[0]);
    AssertJUnit.assertEquals(entry1, entries[1]);
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
    AssertJUnit.assertEquals(0, sr.subResult(2, 2).entrySize());
    AssertJUnit.assertEquals(1, sr.subResult(1, 2).entrySize());
    AssertJUnit.assertEquals(2, sr.subResult(0, 2).entrySize());
    try {
      sr.subResult(-1, 1);
      AssertJUnit.fail("Should have thrown IndexOutOfBoundsException");
    } catch (Exception e) {
      AssertJUnit.assertEquals(IndexOutOfBoundsException.class, e.getClass());
    }
    try {
      sr.subResult(0, 3);
      AssertJUnit.fail("Should have thrown IndexOutOfBoundsException");
    } catch (Exception e) {
      AssertJUnit.assertEquals(IndexOutOfBoundsException.class, e.getClass());
    }
    try {
      sr.subResult(1, 0);
      AssertJUnit.fail("Should have thrown IndexOutOfBoundsException");
    } catch (Exception e) {
      AssertJUnit.assertEquals(IndexOutOfBoundsException.class, e.getClass());
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
}
