/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.ldaptive.auth.AuthenticationHandlerResponse;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.extended.ExtendedResponse;
import org.ldaptive.extended.IntermediateResponse;
import org.ldaptive.extended.NoticeOfDisconnection;
import org.ldaptive.extended.SyncInfoMessage;
import org.ldaptive.extended.UnsolicitedNotification;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for objects that override equals.
 *
 * @author  Middleware Services
 */
public class EqualsTest
{


  /**
   * Response classes.
   *
   * @return  test data
   */
  @DataProvider(name = "response-classes")
  public Object[][] responseClasses()
  {
    return
      new Object[][] {
        new Object[] {
          AddResponse.class,
        },
        new Object[] {
          BindResponse.class,
        },
        new Object[] {
          CompareResponse.class,
        },
        new Object[] {
          DeleteResponse.class,
        },
        new Object[] {
          ModifyDnResponse.class,
        },
        new Object[] {
          ModifyResponse.class,
        },
        new Object[] {
          SearchResponse.class,
        },
        new Object[] {
          AuthenticationHandlerResponse.class,
        },
        new Object[] {
          AuthenticationResponse.class,
        },
        new Object[] {
          ExtendedResponse.class,
        },
        new Object[] {
          IntermediateResponse.class,
        },
        new Object[] {
          NoticeOfDisconnection.class,
        },
        new Object[] {
          SyncInfoMessage.class,
        },
        new Object[] {
          UnsolicitedNotification.class,
        },
      };
  }


  @Test
  public void ldapAttribute()
  {
    EqualsVerifier
      .forRelaxedEqualExamples(
        LdapAttribute.builder().name("uid").values("abrown").build(),
        LdapAttribute.builder().name("UID").values("abrown").build())
      .andUnequalExample(LdapAttribute.builder().name("id").values("abrown").build())
      .suppress(Warning.STRICT_INHERITANCE)
      .suppress(Warning.NONFINAL_FIELDS)
      .verify();
  }


  @Test
  public void ldapEntry()
  {
    EqualsVerifier
      .forRelaxedEqualExamples(
        LdapEntry.builder()
          .dn("uid=abrown,ou=people,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("abrown").build()).build(),
        LdapEntry.builder()
          .dn("UID=abrown,ou=people,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("UID").values("abrown").build()).build())
      .andUnequalExample(
        LdapEntry.builder()
          .dn("id=abrown,ou=people,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("id").values("abrown").build()).build())
      .suppress(Warning.STRICT_INHERITANCE)
      .suppress(Warning.NONFINAL_FIELDS)
      .verify();
  }


  @Test
  public void searchRequest()
  {
    EqualsVerifier.forClass(SearchRequest.class)
      .suppress(Warning.STRICT_INHERITANCE)
      .suppress(Warning.NONFINAL_FIELDS)
      .withIgnoredFields("logger")
      .verify();
  }


  @Test
  public void searchResultReference()
  {
    EqualsVerifier.forClass(SearchResultReference.class)
      .suppress(Warning.STRICT_INHERITANCE)
      .suppress(Warning.NONFINAL_FIELDS)
      .withIgnoredFields("immutableOnConstruct", "immutable")
      .verify();
  }


  @Test
  public void ldapURL()
  {
    EqualsVerifier.forClass(LdapURL.class)
      .suppress(Warning.STRICT_INHERITANCE)
      .suppress(Warning.NONFINAL_FIELDS)
      .withIgnoredFields("retryMetadata", "active")
      .verify();
  }


  @Test
  public void filterTemplate()
  {
    EqualsVerifier.forClass(FilterTemplate.class)
      .suppress(Warning.STRICT_INHERITANCE)
      .suppress(Warning.NONFINAL_FIELDS)
      .verify();
  }


  @Test
  public void singleConnectionFactoryConnectionProxy()
  {
    EqualsVerifier.forClass(SingleConnectionFactory.ConnectionProxy.class)
      .suppress(Warning.STRICT_INHERITANCE)
      .verify();
  }


  @Test(dataProvider = "response-classes")
  public void responses(final Class<?> clazz)
  {
    EqualsVerifier.forClass(clazz)
      .suppress(Warning.STRICT_INHERITANCE)
      .suppress(Warning.NONFINAL_FIELDS)
      .withIgnoredFields("immutableOnConstruct", "immutable")
      .verify();
  }
}
