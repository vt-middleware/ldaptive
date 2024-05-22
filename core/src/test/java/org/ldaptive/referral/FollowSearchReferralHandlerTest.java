/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.MockConnectionFactory;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.transport.DefaultSearchOperationHandle;
import org.ldaptive.transport.mock.MockConnection;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test for {@link FollowSearchReferralHandler}.
 *
 * @author  Middleware Services
 */
public class FollowSearchReferralHandlerTest
{


  @Test(groups = "handlers")
  public void applyNoReferral()
  {
    final FollowSearchReferralHandler handler = new FollowSearchReferralHandler();
    final SearchResponse response = SearchResponse.builder()
      .messageID(2)
      .resultCode(ResultCode.SUCCESS)
      .entry(LdapEntry.builder()
        .messageID(2)
        .dn("uid=1,ou=test,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("uid").values("1").build())
        .build())
      .build();
    Assert.assertEquals(
      handler.apply(response),
      response);
  }


  @Test(groups = "handlers")
  public void applyStateless()
  {
    final AtomicInteger referralDepth = new AtomicInteger();
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://directory.ldaptive.org").build();
    final MockConnection conn = new MockConnection(config);
    conn.setOpenPredicate(ldapURL -> true);
    conn.setSearchOperationFunction(req -> new DefaultSearchOperationHandle(req, conn, Duration.ofSeconds(5)));
    conn.setWriteConsumer(h -> {
      h.messageID(1);
      h.sent();
      if (referralDepth.getAndIncrement() < 5) {
        ((DefaultSearchOperationHandle) h).result(
          SearchResponse.builder()
            .messageID(1)
            .resultCode(ResultCode.REFERRAL)
            .referralURLs("ldap://ds1.ldaptive.org:389/dc=ldaptive,dc=org??sub?")
            .build());
      } else {
        ((DefaultSearchOperationHandle) h).entry(LdapEntry.builder()
          .messageID(1)
          .dn("uid=1,ou=test,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values("1").build())
          .build());
        ((DefaultSearchOperationHandle) h).result(
          SearchResponse.builder().messageID(1).resultCode(ResultCode.SUCCESS).build());
        referralDepth.getAndSet(0);
      }
      // close connection since it will be reused by the next handler invocation
      conn.close();
    });
    final SearchRequest request = SearchRequest.builder()
      .dn("ou=test,dc=ldaptive,dc=org")
      .filter("(uid=1)")
      .build();
    final DefaultSearchOperationHandle handle = new DefaultSearchOperationHandle(
      request, MockConnection.builder(config).openPredicate(ldapURL -> true).build(), Duration.ofSeconds(5));

    final FollowSearchReferralHandler handler = new FollowSearchReferralHandler(url -> new MockConnectionFactory(conn));
    handler.setRequest(request);
    handler.setHandle(handle);
    final SearchResponse response = SearchResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.REFERRAL)
      .referralURLs(
        "ldap://ds1.ldaptive.org:389/dc=ldaptive,dc=org??sub?")
      .build();
    for (int i = 0; i < 20; i++) {
      Assert.assertEquals(
        handler.apply(response),
        SearchResponse.builder()
          .messageID(1)
          .resultCode(ResultCode.SUCCESS)
          .entry(
            LdapEntry.builder()
              .messageID(1)
              .dn("uid=1,ou=test,dc=ldaptive,dc=org")
              .attributes(LdapAttribute.builder().name("uid").values("1").build())
              .build())
          .build());
    }
  }


  @Test(groups = "handlers")
  public void applyReferral()
  {
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://directory.ldaptive.org").build();
    final MockConnection conn = new MockConnection(config);
    conn.setOpenPredicate(ldapURL -> true);
    conn.setSearchOperationFunction(req -> new DefaultSearchOperationHandle(req, conn, Duration.ofSeconds(5)));
    conn.setWriteConsumer(h -> {
      h.messageID(1);
      h.sent();
      ((DefaultSearchOperationHandle) h).entry(LdapEntry.builder()
        .messageID(1)
        .dn("uid=1,ou=test,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("uid").values("1").build())
        .build());
      ((DefaultSearchOperationHandle) h).result(
        SearchResponse.builder().messageID(1).resultCode(ResultCode.SUCCESS).build());
    });
    final SearchRequest request = SearchRequest.builder()
      .dn("ou=test,dc=ldaptive,dc=org")
      .filter("(uid=1)")
      .build();
    final DefaultSearchOperationHandle handle = new DefaultSearchOperationHandle(
      request, MockConnection.builder(config).openPredicate(ldapURL -> true).build(), Duration.ofSeconds(5));

    final FollowSearchReferralHandler handler = new FollowSearchReferralHandler(url -> new MockConnectionFactory(conn));
    handler.setRequest(request);
    handler.setHandle(handle);
    final SearchResponse response = SearchResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.REFERRAL)
      .referralURLs(
        "ldap://ds1.ldaptive.org:389/dc=ldaptive,dc=org??sub?",
        "ldap://ds2.ldaptive.org:389/dc=ldaptive,dc=org??sub?")
      .build();
    Assert.assertEquals(
      handler.apply(response),
      SearchResponse.builder()
        .messageID(1)
        .resultCode(ResultCode.SUCCESS)
        .entry(
          LdapEntry.builder()
            .messageID(1)
            .dn("uid=1,ou=test,dc=ldaptive,dc=org")
            .attributes(LdapAttribute.builder().name("uid").values("1").build())
            .build())
        .build());
  }


  @Test(groups = "handlers")
  public void applyReferralLimit()
  {
    final AtomicInteger count = new AtomicInteger(1);
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://placeholder.ldaptive.org").build();
    final MockConnection conn = new MockConnection(config);
    conn.setOpenPredicate(ldapURL -> true);
    conn.setSearchOperationFunction(req -> new DefaultSearchOperationHandle(req, conn, Duration.ofSeconds(5)));
    conn.setWriteConsumer(h -> {
      h.messageID(1);
      h.sent();
      ((DefaultSearchOperationHandle) h).result(
        SearchResponse.builder()
          .messageID(1)
          .resultCode(ResultCode.REFERRAL)
          .referralURLs("ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?")
          .build());
      // close connection since it will be reused by the next handler invocation
      conn.close();
    });
    final SearchRequest request = SearchRequest.builder()
      .dn("ou=test,dc=ldaptive,dc=org")
      .filter("(uid=1)")
      .build();
    final DefaultSearchOperationHandle handle = new DefaultSearchOperationHandle(
      request,
      MockConnection.builder(
        ConnectionConfig.builder().url("ldap://directory.ldaptive.org").build())
        .openPredicate(ldapURL -> true)
        .build(),
      Duration.ofSeconds(5));

    final FollowSearchReferralHandler handler = new FollowSearchReferralHandler(
      2, url -> new MockConnectionFactory(conn));
    handler.setRequest(request);
    handler.setHandle(handle);
    final SearchResponse response = SearchResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.REFERRAL)
      .referralURLs("ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?")
      .build();
    try {
      handler.apply(response);
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), RuntimeException.class);
      Assert.assertEquals(e.getCause().getClass(), LdapException.class);
      Assert.assertEquals(((LdapException) e.getCause()).getResultCode(), ResultCode.REFERRAL_LIMIT_EXCEEDED);
    }
  }


  @Test(groups = "handlers")
  public void applyThrowsRuntimeException()
  {
    final AtomicInteger count = new AtomicInteger(1);
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://placeholder.ldaptive.org").build();
    final MockConnection conn = new MockConnection(config);
    conn.setOpenPredicate(ldapURL -> true);
    conn.setSearchOperationFunction(req -> new DefaultSearchOperationHandle(req, conn, Duration.ofSeconds(5)));
    conn.setWriteConsumer(h -> {
      if (count.getAndIncrement() == 2) {
        h.messageID(1);
        h.sent();
        ((DefaultSearchOperationHandle) h).result(
          SearchResponse.builder()
            .messageID(1)
            .resultCode(ResultCode.REFERRAL)
            .referralURLs("ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?")
            .build());
        // close connection since it will be reused by the next handler invocation
        conn.close();
      } else {
        throw new RuntimeException("Test Exception");
      }
    });
    final SearchRequest request = SearchRequest.builder()
      .dn("ou=test,dc=ldaptive,dc=org")
      .filter("(uid=1)")
      .build();
    final DefaultSearchOperationHandle handle = new DefaultSearchOperationHandle(
      request,
      MockConnection.builder(
          ConnectionConfig.builder().url("ldap://directory.ldaptive.org").build())
        .openPredicate(ldapURL -> true)
        .build(),
      Duration.ofSeconds(5));

    final FollowSearchReferralHandler handler = new FollowSearchReferralHandler(
      10, url -> new MockConnectionFactory(conn));
    handler.setRequest(request);
    handler.setHandle(handle);
    final SearchResponse response = SearchResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.REFERRAL)
      .referralURLs("ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?")
      .build();

    try {
      handler.apply(response);
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
      Assert.assertTrue(
        e.getMessage().startsWith("Search result handler org.ldaptive.referral.FollowSearchReferralHandler"));
    }
  }


  @Test(groups = "handlers")
  public void applyThrowsLdapException()
  {
    final AtomicInteger count = new AtomicInteger(1);
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://placeholder.ldaptive.org").build();
    final MockConnection conn = new MockConnection(config);
    conn.setOpenPredicate(ldapURL -> true);
    conn.setSearchOperationFunction(req -> new DefaultSearchOperationHandle(req, conn, Duration.ofSeconds(5)));
    conn.setWriteConsumer(h -> {
      if (count.getAndIncrement() == 2) {
        h.messageID(1);
        h.sent();
        ((DefaultSearchOperationHandle) h).result(
          SearchResponse.builder()
            .messageID(1)
            .resultCode(ResultCode.REFERRAL)
            .referralURLs("ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?")
            .build());
        // close connection since it will be reused by the next handler invocation
        conn.close();
      } else {
        throw new RuntimeException(new LdapException("Test Exception"));
      }
    });
    final SearchRequest request = SearchRequest.builder()
      .dn("ou=test,dc=ldaptive,dc=org")
      .filter("(uid=1)")
      .build();
    final DefaultSearchOperationHandle handle = new DefaultSearchOperationHandle(
      request,
      MockConnection.builder(
          ConnectionConfig.builder().url("ldap://directory.ldaptive.org").build())
        .openPredicate(ldapURL -> true)
        .build(),
      Duration.ofSeconds(5));

    final FollowSearchReferralHandler handler = new FollowSearchReferralHandler(
      10, url -> new MockConnectionFactory(conn));
    handler.setRequest(request);
    handler.setHandle(handle);
    final SearchResponse response = SearchResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.REFERRAL)
      .referralURLs("ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?")
      .build();

    Assert.assertEquals(
      handler.apply(response),
      SearchResponse.builder()
        .messageID(1)
        .resultCode(ResultCode.REFERRAL)
        .referralURLs("ldap://ds1.ldaptive.org:389/dc=ldaptive,dc=org??sub?")
        .build());
  }
}
