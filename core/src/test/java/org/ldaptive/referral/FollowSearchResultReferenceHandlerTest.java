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
import org.ldaptive.SearchResultReference;
import org.ldaptive.transport.DefaultSearchOperationHandle;
import org.ldaptive.transport.mock.MockConnection;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link FollowSearchResultReferenceHandler}.
 *
 * @author  Middleware Services
 */
public class FollowSearchResultReferenceHandlerTest
{


  @Test(groups = "referral")
  public void applyNoReference()
  {
    final FollowSearchResultReferenceHandler handler = new FollowSearchResultReferenceHandler();
    final SearchResponse response = SearchResponse.builder()
      .messageID(2)
      .resultCode(ResultCode.SUCCESS)
      .entry(LdapEntry.builder()
        .messageID(2)
        .dn("uid=1,ou=test,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("uid").values("1").build())
        .build())
      .build();
    assertThat(handler.apply(response)).isEqualTo(response);
  }


  @Test(groups = "referral")
  public void applyStateless()
  {
    final AtomicInteger referralDepth = new AtomicInteger();
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://directory.ldaptive.org").build();
    final MockConnection<SearchRequest, SearchResponse> conn = new MockConnection<>(config);
    conn.setOpenPredicate(ldapURL -> true);
    conn.setSearchOperationFunction(req -> new DefaultSearchOperationHandle(req, conn, Duration.ofSeconds(5)));
    conn.setWriteConsumer(h -> {
      h.messageID(referralDepth.incrementAndGet());
      h.sent();
      ((DefaultSearchOperationHandle) h).entry(LdapEntry.builder()
        .messageID(referralDepth.get())
        .dn("uid=2,ou=test,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("uid").values("2").build())
        .build());
      h.result(
        SearchResponse.builder().messageID(referralDepth.get()).resultCode(ResultCode.SUCCESS).build());
      referralDepth.getAndSet(0);
      // close connection since it will be reused by the next handler invocation
      conn.close();
    });
    final SearchRequest request = SearchRequest.builder()
      .dn("ou=test,dc=ldaptive,dc=org")
      .filter("(uid=1)")
      .build();
    final DefaultSearchOperationHandle handle = new DefaultSearchOperationHandle(
      request, MockConnection.builder(config).openPredicate(ldapURL -> true).build(), Duration.ofSeconds(5));

    final FollowSearchResultReferenceHandler handler = new FollowSearchResultReferenceHandler(
      url -> new MockConnectionFactory(conn));
    handler.setRequest(request);
    handler.setHandle(handle);
    final SearchResponse response = SearchResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.SUCCESS)
      .entry(LdapEntry.builder()
        .messageID(1)
        .dn("uid=1,ou=test,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("uid").values("1").build())
        .build())
      .reference(SearchResultReference.builder()
        .messageID(1)
        .uris(
          "ldap://ds1.ldaptive.org:389/dc=ldaptive,dc=org??sub?",
          "ldap://ds2.ldaptive.org:389/dc=ldaptive,dc=org??sub?")
        .build())
      .build();
    for (int i = 0; i < 20; i++) {
      final SearchResponse sr = handler.apply(response);
      assertThat(sr).isEqualTo(
        SearchResponse.builder()
          .messageID(1)
          .resultCode(ResultCode.SUCCESS)
          .entry(
            LdapEntry.builder()
              .messageID(1)
              .dn("uid=1,ou=test,dc=ldaptive,dc=org")
              .attributes(LdapAttribute.builder().name("uid").values("1").build())
              .build(),
            LdapEntry.builder()
              .messageID(1)
              .dn("uid=2,ou=test,dc=ldaptive,dc=org")
              .attributes(LdapAttribute.builder().name("uid").values("2").build())
              .build())
          .build());
    }
  }


  @Test(groups = "referral")
  public void applyReference()
  {
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://directory.ldaptive.org").build();
    final MockConnection<SearchRequest, SearchResponse> conn = new MockConnection<>(config);
    conn.setOpenPredicate(ldapURL -> true);
    conn.setSearchOperationFunction(req -> new DefaultSearchOperationHandle(req, conn, Duration.ofSeconds(5)));
    conn.setWriteConsumer(h -> {
      h.messageID(5);
      h.sent();
      ((DefaultSearchOperationHandle) h).entry(LdapEntry.builder()
        .messageID(5)
        .dn("uid=2,ou=test,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("uid").values("2").build())
        .build());
      h.result(
        SearchResponse.builder().messageID(5).resultCode(ResultCode.SUCCESS).build());
    });
    final SearchRequest request = SearchRequest.builder()
      .dn("ou=test,dc=ldaptive,dc=org")
      .filter("(uid=1)")
      .build();
    final DefaultSearchOperationHandle handle = new DefaultSearchOperationHandle(
      request, MockConnection.builder(config).openPredicate(ldapURL -> true).build(), Duration.ofSeconds(5));

    final FollowSearchResultReferenceHandler handler = new FollowSearchResultReferenceHandler(
      url -> new MockConnectionFactory(conn));
    handler.setRequest(request);
    handler.setHandle(handle);
    final SearchResponse response = SearchResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.SUCCESS)
      .entry(LdapEntry.builder()
        .messageID(1)
        .dn("uid=1,ou=test,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("uid").values("1").build())
        .build())
      .reference(SearchResultReference.builder()
        .messageID(1)
        .uris(
          "ldap://ds1.ldaptive.org:389/dc=ldaptive,dc=org??sub?",
          "ldap://ds2.ldaptive.org:389/dc=ldaptive,dc=org??sub?")
        .build())
      .build();
    final SearchResponse sr = handler.apply(response);
    assertThat(sr).isEqualTo(
      SearchResponse.builder()
        .messageID(1)
        .resultCode(ResultCode.SUCCESS)
        .entry(
          LdapEntry.builder()
            .messageID(1)
            .dn("uid=1,ou=test,dc=ldaptive,dc=org")
            .attributes(LdapAttribute.builder().name("uid").values("1").build())
            .build(),
          LdapEntry.builder()
            .messageID(5)
            .dn("uid=2,ou=test,dc=ldaptive,dc=org")
            .attributes(LdapAttribute.builder().name("uid").values("2").build())
            .build())
        .build());
  }


  @Test(groups = "referral")
  public void applyMultiReference()
  {
    final AtomicInteger count = new AtomicInteger(1);
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://directory.ldaptive.org").build();
    final MockConnection<SearchRequest, SearchResponse> conn = new MockConnection<>(config);
    conn.setOpenPredicate(ldapURL -> true);
    conn.setSearchOperationFunction(req -> new DefaultSearchOperationHandle(req, conn, Duration.ofSeconds(5)));
    conn.setWriteConsumer(h -> {
      h.messageID(5);
      h.sent();
      ((DefaultSearchOperationHandle) h).entry(LdapEntry.builder()
        .messageID(5)
        .dn("uid=" + count.incrementAndGet() + ",ou=test,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("uid").values(String.valueOf(count.get())).build())
        .build());
      h.result(
        SearchResponse.builder().messageID(5).resultCode(ResultCode.SUCCESS).build());
    });
    final SearchRequest request = SearchRequest.builder()
      .dn("ou=test,dc=ldaptive,dc=org")
      .filter("(uid=1)")
      .build();
    final DefaultSearchOperationHandle handle = new DefaultSearchOperationHandle(
      request, MockConnection.builder(config).openPredicate(ldapURL -> true).build(), Duration.ofSeconds(5));

    final FollowSearchResultReferenceHandler handler = new FollowSearchResultReferenceHandler(
      url -> new MockConnectionFactory(conn));
    handler.setRequest(request);
    handler.setHandle(handle);
    final SearchResponse response = SearchResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.SUCCESS)
      .entry(LdapEntry.builder()
        .messageID(1)
        .dn("uid=1,ou=test,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("uid").values("1").build())
        .build())
      .reference(
        SearchResultReference.builder()
          .messageID(1)
          .uris("ldap://ds1.ldaptive.org:389/dc=ldaptive,dc=org??sub?")
          .build(),
        SearchResultReference.builder()
          .messageID(1)
          .uris("ldap://ds2.ldaptive.org:389/dc=ldaptive,dc=org??sub?")
          .build())
      .build();
    final SearchResponse sr = handler.apply(response);
    assertThat(sr).isEqualTo(
      SearchResponse.builder()
        .messageID(1)
        .resultCode(ResultCode.SUCCESS)
        .entry(
          LdapEntry.builder()
            .messageID(1)
            .dn("uid=1,ou=test,dc=ldaptive,dc=org")
            .attributes(LdapAttribute.builder().name("uid").values("1").build())
            .build(),
          LdapEntry.builder()
            .messageID(5)
            .dn("uid=2,ou=test,dc=ldaptive,dc=org")
            .attributes(LdapAttribute.builder().name("uid").values("2").build())
            .build(),
          LdapEntry.builder()
            .messageID(5)
            .dn("uid=3,ou=test,dc=ldaptive,dc=org")
            .attributes(LdapAttribute.builder().name("uid").values("3").build())
            .build())
        .build());
  }


  @Test(groups = "referral")
  public void applyReferenceLimit()
  {
    final AtomicInteger count = new AtomicInteger(1);
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://placeholder.ldaptive.org").build();
    final MockConnection<SearchRequest, SearchResponse> conn = new MockConnection<>(config);
    conn.setOpenPredicate(ldapURL -> true);
    conn.setSearchOperationFunction(req -> new DefaultSearchOperationHandle(req, conn, Duration.ofSeconds(5)));
    conn.setWriteConsumer(h -> {
      h.messageID(1);
      h.sent();
      ((DefaultSearchOperationHandle) h).entry(
        LdapEntry.builder()
          .messageID(1)
          .dn("uid=" + count.get() + ",ou=test,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder().name("uid").values(String.valueOf(count.get())).build())
          .build());
      ((DefaultSearchOperationHandle) h).reference(
        SearchResultReference.builder()
          .messageID(1)
          .uris("ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?")
          .build());
      h.result(
        SearchResponse.builder()
          .messageID(1)
          .resultCode(ResultCode.SUCCESS)
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

    final FollowSearchResultReferenceHandler handler = new FollowSearchResultReferenceHandler(
      2, url -> new MockConnectionFactory(conn));
    handler.setRequest(request);
    handler.setHandle(handle);
    final SearchResponse response = SearchResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.SUCCESS)
      .entry(LdapEntry.builder()
        .messageID(1)
        .dn("uid=" + count.get() + ",ou=test,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("uid").values(String.valueOf(count.get())).build())
        .build())
      .reference(SearchResultReference.builder()
        .messageID(1)
        .uris("ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?")
        .build())
      .build();
    try {
      handler.apply(response);
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(RuntimeException.class);
      assertThat(e.getCause()).isExactlyInstanceOf(LdapException.class);
      assertThat(((LdapException) e.getCause()).getResultCode()).isEqualTo(ResultCode.REFERRAL_LIMIT_EXCEEDED);
    }
  }


  @Test(groups = "referral")
  public void applyNotSuccess()
  {
    final AtomicInteger count = new AtomicInteger(1);
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://placeholder.ldaptive.org").build();
    final MockConnection<SearchRequest, SearchResponse> conn = new MockConnection<>(config);
    conn.setOpenPredicate(ldapURL -> true);
    conn.setSearchOperationFunction(req -> new DefaultSearchOperationHandle(req, conn, Duration.ofSeconds(5)));
    conn.setWriteConsumer(h -> {
      h.messageID(1);
      h.sent();
      h.result(SearchResponse.builder()
        .messageID(1)
        .resultCode(ResultCode.NO_SUCH_OBJECT)
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

    final FollowSearchResultReferenceHandler handler = new FollowSearchResultReferenceHandler(
      url -> new MockConnectionFactory(conn));
    handler.setRequest(request);
    handler.setHandle(handle);
    final SearchResponse response = SearchResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.SUCCESS)
      .entry(LdapEntry.builder()
        .messageID(1)
        .dn("uid=" + count.get() + ",ou=test,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("uid").values(String.valueOf(count.get())).build())
        .build())
      .reference(SearchResultReference.builder()
        .messageID(1)
        .uris("ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?")
        .build())
      .build();

    final SearchResponse sr = handler.apply(response);
    assertThat(sr).isEqualTo(
      SearchResponse.builder()
        .messageID(1)
        .resultCode(ResultCode.SUCCESS)
        .entry(
          LdapEntry.builder()
            .messageID(1)
            .dn("uid=1,ou=test,dc=ldaptive,dc=org")
            .attributes(LdapAttribute.builder().name("uid").values("1").build())
            .build())
        .reference(SearchResultReference.builder()
          .messageID(1)
          .uris("ldap://ds1.ldaptive.org:389/dc=ldaptive,dc=org??sub?")
          .build())
        .build());
  }


  @Test(groups = "referral")
  public void applyNotSuccessWithFailure()
  {
    final AtomicInteger count = new AtomicInteger(1);
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://placeholder.ldaptive.org").build();
    final MockConnection<SearchRequest, SearchResponse> conn = new MockConnection<>(config);
    conn.setOpenPredicate(ldapURL -> true);
    conn.setSearchOperationFunction(req -> new DefaultSearchOperationHandle(req, conn, Duration.ofSeconds(5)));
    conn.setWriteConsumer(h -> {
      h.messageID(1);
      h.sent();
      h.result(SearchResponse.builder()
        .messageID(1)
        .resultCode(ResultCode.NO_SUCH_OBJECT)
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

    final FollowSearchResultReferenceHandler handler = new FollowSearchResultReferenceHandler(
      url -> new MockConnectionFactory(conn), true);
    handler.setRequest(request);
    handler.setHandle(handle);
    final SearchResponse response = SearchResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.SUCCESS)
      .entry(LdapEntry.builder()
        .messageID(1)
        .dn("uid=" + count.get() + ",ou=test,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("uid").values(String.valueOf(count.get())).build())
        .build())
      .reference(SearchResultReference.builder()
        .messageID(1)
        .uris("ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?")
        .build())
      .build();

    try {
      handler.apply(response);
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(RuntimeException.class);
      assertThat(e.getCause()).isExactlyInstanceOf(LdapException.class);
      assertThat(e.getCause().getMessage()).startsWith("Could not follow referral");
    }
  }


  @Test(groups = "referral")
  public void applyThrowsRuntimeException()
  {
    final AtomicInteger count = new AtomicInteger(1);
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://placeholder.ldaptive.org").build();
    final MockConnection<SearchRequest, SearchResponse> conn = new MockConnection<>(config);
    conn.setOpenPredicate(ldapURL -> true);
    conn.setSearchOperationFunction(req -> new DefaultSearchOperationHandle(req, conn, Duration.ofSeconds(5)));
    conn.setWriteConsumer(h -> {
      if (count.getAndIncrement() == 2) {
        h.messageID(1);
        h.sent();
        ((DefaultSearchOperationHandle) h).entry(
          LdapEntry.builder()
            .messageID(1)
            .dn("uid=" + count.get() + ",ou=test,dc=ldaptive,dc=org")
            .attributes(LdapAttribute.builder().name("uid").values(String.valueOf(count.get())).build())
            .build());
        ((DefaultSearchOperationHandle) h).reference(
          SearchResultReference.builder()
            .messageID(1)
            .uris("ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?")
            .build());
        h.result(
          SearchResponse.builder()
            .messageID(1)
            .resultCode(ResultCode.SUCCESS)
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

    final FollowSearchResultReferenceHandler handler = new FollowSearchResultReferenceHandler(
      10, url -> new MockConnectionFactory(conn));
    handler.setRequest(request);
    handler.setHandle(handle);
    final SearchResponse response = SearchResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.SUCCESS)
      .entry(LdapEntry.builder()
        .messageID(1)
        .dn("uid=" + count.get() + ",ou=test,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("uid").values(String.valueOf(count.get())).build())
        .build())
      .reference(SearchResultReference.builder()
        .messageID(1)
        .uris("ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?")
        .build())
      .build();

    assertThat(handler.apply(response)).isEqualTo(
      SearchResponse.builder()
        .messageID(1)
        .resultCode(ResultCode.SUCCESS)
        .entry(
          LdapEntry.builder()
            .messageID(1)
            .dn("uid=1,ou=test,dc=ldaptive,dc=org")
            .attributes(LdapAttribute.builder().name("uid").values("1").build())
            .build(),
          LdapEntry.builder()
            .messageID(1)
            .dn("uid=3,ou=test,dc=ldaptive,dc=org")
            .attributes(LdapAttribute.builder().name("uid").values("3").build())
            .build())
        .reference(SearchResultReference.builder()
          .messageID(1)
          .uris("ldap://ds3.ldaptive.org:389/dc=ldaptive,dc=org??sub?")
          .build())
        .build());
  }


  @Test(groups = "referral")
  public void applyThrowsRuntimeExceptionWithFailure()
  {
    final AtomicInteger count = new AtomicInteger(1);
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://placeholder.ldaptive.org").build();
    final MockConnection<SearchRequest, SearchResponse> conn = new MockConnection<>(config);
    conn.setOpenPredicate(ldapURL -> true);
    conn.setSearchOperationFunction(req -> new DefaultSearchOperationHandle(req, conn, Duration.ofSeconds(5)));
    conn.setWriteConsumer(h -> {
      if (count.getAndIncrement() == 2) {
        h.messageID(1);
        h.sent();
        ((DefaultSearchOperationHandle) h).entry(
          LdapEntry.builder()
            .messageID(1)
            .dn("uid=" + count.get() + ",ou=test,dc=ldaptive,dc=org")
            .attributes(LdapAttribute.builder().name("uid").values(String.valueOf(count.get())).build())
            .build());
        ((DefaultSearchOperationHandle) h).reference(
          SearchResultReference.builder()
            .messageID(1)
            .uris("ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?")
            .build());
        h.result(
          SearchResponse.builder()
            .messageID(1)
            .resultCode(ResultCode.SUCCESS)
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

    final FollowSearchResultReferenceHandler handler = new FollowSearchResultReferenceHandler(
      10, url -> new MockConnectionFactory(conn), true);
    handler.setRequest(request);
    handler.setHandle(handle);
    final SearchResponse response = SearchResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.SUCCESS)
      .entry(LdapEntry.builder()
        .messageID(1)
        .dn("uid=" + count.get() + ",ou=test,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("uid").values(String.valueOf(count.get())).build())
        .build())
      .reference(SearchResultReference.builder()
        .messageID(1)
        .uris("ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?")
        .build())
      .build();

    try {
      handler.apply(response);
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
      assertThat(e.getMessage())
        .startsWith("Search result handler org.ldaptive.referral.FollowSearchResultReferenceHandler");
    }
  }


  @Test(groups = "referral")
  public void applyThrowsLdapException()
  {
    final AtomicInteger count = new AtomicInteger(1);
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://placeholder.ldaptive.org").build();
    final MockConnection<SearchRequest, SearchResponse> conn = new MockConnection<>(config);
    conn.setOpenPredicate(ldapURL -> true);
    conn.setSearchOperationFunction(req -> new DefaultSearchOperationHandle(req, conn, Duration.ofSeconds(5)));
    conn.setWriteConsumer(h -> {
      if (count.getAndIncrement() == 2) {
        h.messageID(1);
        h.sent();
        ((DefaultSearchOperationHandle) h).entry(
          LdapEntry.builder()
            .messageID(1)
            .dn("uid=" + count.get() + ",ou=test,dc=ldaptive,dc=org")
            .attributes(LdapAttribute.builder().name("uid").values(String.valueOf(count.get())).build())
            .build());
        ((DefaultSearchOperationHandle) h).reference(
          SearchResultReference.builder()
            .messageID(1)
            .uris("ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?")
            .build());
        h.result(
          SearchResponse.builder()
            .messageID(1)
            .resultCode(ResultCode.SUCCESS)
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

    final FollowSearchResultReferenceHandler handler = new FollowSearchResultReferenceHandler(
      10, url -> new MockConnectionFactory(conn));
    handler.setRequest(request);
    handler.setHandle(handle);
    final SearchResponse response = SearchResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.SUCCESS)
      .entry(LdapEntry.builder()
        .messageID(1)
        .dn("uid=" + count.get() + ",ou=test,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("uid").values(String.valueOf(count.get())).build())
        .build())
      .reference(SearchResultReference.builder()
        .messageID(1)
        .uris("ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?")
        .build())
      .build();

    assertThat(handler.apply(response)).isEqualTo(
      SearchResponse.builder()
        .messageID(1)
        .resultCode(ResultCode.SUCCESS)
        .entry(
          LdapEntry.builder()
            .messageID(1)
            .dn("uid=1,ou=test,dc=ldaptive,dc=org")
            .attributes(LdapAttribute.builder().name("uid").values("1").build())
            .build(),
          LdapEntry.builder()
            .messageID(1)
            .dn("uid=3,ou=test,dc=ldaptive,dc=org")
            .attributes(LdapAttribute.builder().name("uid").values("3").build())
            .build())
        .reference(SearchResultReference.builder()
          .messageID(1)
          .uris("ldap://ds3.ldaptive.org:389/dc=ldaptive,dc=org??sub?")
          .build())
        .build());
  }


  @Test(groups = "referral")
  public void applyThrowsLdapExceptionWithFailure()
  {
    final AtomicInteger count = new AtomicInteger(1);
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://placeholder.ldaptive.org").build();
    final MockConnection<SearchRequest, SearchResponse> conn = new MockConnection<>(config);
    conn.setOpenPredicate(ldapURL -> true);
    conn.setSearchOperationFunction(req -> new DefaultSearchOperationHandle(req, conn, Duration.ofSeconds(5)));
    conn.setWriteConsumer(h -> {
      if (count.getAndIncrement() == 2) {
        h.messageID(1);
        h.sent();
        ((DefaultSearchOperationHandle) h).entry(
          LdapEntry.builder()
            .messageID(1)
            .dn("uid=" + count.get() + ",ou=test,dc=ldaptive,dc=org")
            .attributes(LdapAttribute.builder().name("uid").values(String.valueOf(count.get())).build())
            .build());
        ((DefaultSearchOperationHandle) h).reference(
          SearchResultReference.builder()
            .messageID(1)
            .uris("ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?")
            .build());
        h.result(
          SearchResponse.builder()
            .messageID(1)
            .resultCode(ResultCode.SUCCESS)
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

    final FollowSearchResultReferenceHandler handler = new FollowSearchResultReferenceHandler(
      10, url -> new MockConnectionFactory(conn), true);
    handler.setRequest(request);
    handler.setHandle(handle);
    final SearchResponse response = SearchResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.SUCCESS)
      .entry(LdapEntry.builder()
        .messageID(1)
        .dn("uid=" + count.get() + ",ou=test,dc=ldaptive,dc=org")
        .attributes(LdapAttribute.builder().name("uid").values(String.valueOf(count.get())).build())
        .build())
      .reference(SearchResultReference.builder()
        .messageID(1)
        .uris("ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?")
        .build())
      .build();

    try {
      handler.apply(response);
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(RuntimeException.class);
      assertThat(e.getCause()).isExactlyInstanceOf(LdapException.class);
      assertThat(e.getCause().getMessage()).isEqualTo("Test Exception");
    }
  }
}
