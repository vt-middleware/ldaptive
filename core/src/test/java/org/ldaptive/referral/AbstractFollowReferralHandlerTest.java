/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import java.util.concurrent.atomic.AtomicInteger;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.LdapException;
import org.ldaptive.Request;
import org.ldaptive.Result;
import org.ldaptive.ResultCode;
import org.ldaptive.handler.ReferralResultHandler;
import org.ldaptive.transport.DefaultOperationHandle;
import org.ldaptive.transport.MessageFunctional;
import org.ldaptive.transport.TransportConnection;
import org.ldaptive.transport.mock.MockConnection;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Base class for referral handler tests.
 *
 * @param  <Q>  type of referral request
 * @param  <S>  type of referral response
 *
 * @author  Middleware Services
 */
public abstract class AbstractFollowReferralHandlerTest<Q extends Request, S extends Result>
{


  abstract ReferralResultHandler<S> createHandler();


  abstract ReferralResultHandler<S> createHandler(MockConnection<Q, S> conn);


  abstract ReferralResultHandler<S> createHandler(int limit, MockConnection<Q, S> conn);


  abstract ReferralResultHandler<S> createHandler(MockConnection<Q, S> conn, boolean throwOnFailure);


  abstract ReferralResultHandler<S> createHandler(int limit, MockConnection<Q, S> conn, boolean throwOnFailure);


  abstract Q createRequest();


  abstract S createResponse(ResultCode code);


  abstract S createSuccessResponse();


  abstract S createReferralResponse(String... url);


  abstract void setOperationFunction(MockConnection<Q, S> conn);


  abstract DefaultOperationHandle<Q, S> createOperationHandle(Q request, TransportConnection conn);


  @Test(groups = "referral")
  public void applyNoReferral()
  {
    final ReferralResultHandler<S> handler = createHandler();
    final S response = createSuccessResponse();
    assertThat(handler.apply(response)).isEqualTo(response);
  }


  @Test(groups = "referral")
  @SuppressWarnings("unchecked")
  public void applyStateless()
  {
    final Q request = createRequest();
    final S referralResponse = createReferralResponse("ldap://ds1.ldaptive.org:389/dc=ldaptive,dc=org??sub?");
    final S successResponse = createSuccessResponse();
    final AtomicInteger referralDepth = new AtomicInteger();
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://directory.ldaptive.org").build();
    final MockConnection<Q, S> conn = new MockConnection<>(config);
    conn.setOpenPredicate(ldapURL -> true);
    setOperationFunction(conn);
    conn.setWriteConsumer(h -> {
      h.messageID(1);
      h.sent();
      if (referralDepth.getAndIncrement() < 5) {
        h.result(referralResponse);
      } else {
        h.result(successResponse);
        referralDepth.getAndSet(0);
      }
      // close connection since it will be reused by the next handler invocation
      conn.close();
    });
    final DefaultOperationHandle<Q, S> handle = createOperationHandle(
      request, MockConnection.builder(config).openPredicate(ldapURL -> true).build());

    final ReferralResultHandler<S> handler = createHandler(conn);
    ((MessageFunctional<Q, S>) handler).setRequest(request);
    ((MessageFunctional<Q, S>) handler).setHandle(handle);
    for (int i = 0; i < 20; i++) {
      assertThat(handler.apply(referralResponse)).isEqualTo(successResponse);
    }
  }


  @Test(groups = "referral")
  @SuppressWarnings("unchecked")
  public void applyReferral()
  {
    final Q request = createRequest();
    final S successResponse = createSuccessResponse();
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://directory.ldaptive.org").build();
    final MockConnection<Q, S> conn = new MockConnection<>(config);
    conn.setOpenPredicate(ldapURL -> true);
    setOperationFunction(conn);
    conn.setWriteConsumer(h -> {
      h.messageID(1);
      h.sent();
      h.result(successResponse);
    });
    final DefaultOperationHandle<Q, S> handle = createOperationHandle(
      request, MockConnection.builder(config).openPredicate(ldapURL -> true).build());

    final ReferralResultHandler<S> handler = createHandler(conn);
    ((MessageFunctional<Q, S>) handler).setRequest(request);
    ((MessageFunctional<Q, S>) handler).setHandle(handle);
    final S referralResponse = createReferralResponse(
      "ldap://ds1.ldaptive.org:389/dc=ldaptive,dc=org??sub?",
      "ldap://ds2.ldaptive.org:389/dc=ldaptive,dc=org??sub?");
    assertThat(handler.apply(referralResponse)).isEqualTo(successResponse);
  }


  @Test(groups = "referral")
  @SuppressWarnings("unchecked")
  public void applyReferralLimit()
  {
    final AtomicInteger count = new AtomicInteger(1);
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://placeholder.ldaptive.org").build();
    final MockConnection<Q, S> conn = new MockConnection<>(config);
    conn.setOpenPredicate(ldapURL -> true);
    setOperationFunction(conn);
    conn.setWriteConsumer(h -> {
      final S referralResponse = createReferralResponse(
        "ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?");
      h.messageID(1);
      h.sent();
      h.result(referralResponse);
      // close connection since it will be reused by the next handler invocation
      conn.close();
    });
    final Q request = createRequest();
    final DefaultOperationHandle<Q, S> handle = createOperationHandle(
      request,
      MockConnection.builder(
          ConnectionConfig.builder().url("ldap://directory.ldaptive.org").build())
        .openPredicate(ldapURL -> true)
        .build());

    final ReferralResultHandler<S> handler = createHandler(2, conn);
    ((MessageFunctional<Q, S>) handler).setRequest(request);
    ((MessageFunctional<Q, S>) handler).setHandle(handle);
    final S referralResponse = createReferralResponse(
      "ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?");
    try {
      handler.apply(referralResponse);
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(RuntimeException.class);
      assertThat(e.getCause()).isExactlyInstanceOf(LdapException.class);
      assertThat(((LdapException) e.getCause()).getResultCode()).isEqualTo(ResultCode.REFERRAL_LIMIT_EXCEEDED);
    }
  }


  @Test(groups = "referral")
  @SuppressWarnings("unchecked")
  public void applyNotSuccess()
  {
    final AtomicInteger count = new AtomicInteger(1);
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://placeholder.ldaptive.org").build();
    final MockConnection<Q, S> conn = new MockConnection<>(config);
    conn.setOpenPredicate(ldapURL -> true);
    setOperationFunction(conn);
    conn.setWriteConsumer(h -> {
      h.messageID(1);
      h.sent();
      h.result(createResponse(ResultCode.NO_SUCH_OBJECT));
      // close connection since it will be reused by the next handler invocation
      conn.close();
    });
    final Q request = createRequest();
    final DefaultOperationHandle<Q, S> handle = createOperationHandle(
      request,
      MockConnection.builder(
          ConnectionConfig.builder().url("ldap://directory.ldaptive.org").build())
        .openPredicate(ldapURL -> true)
        .build());

    final ReferralResultHandler<S> handler = createHandler(conn);
    ((MessageFunctional<Q, S>) handler).setRequest(request);
    ((MessageFunctional<Q, S>) handler).setHandle(handle);
    final S referralResponse = createReferralResponse(
      "ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?");

    assertThat(handler.apply(referralResponse))
      .isEqualTo(createReferralResponse("ldap://ds1.ldaptive.org:389/dc=ldaptive,dc=org??sub?"));
  }


  @Test(groups = "referral")
  @SuppressWarnings("unchecked")
  public void applyNotSuccessWithFailure()
  {
    final AtomicInteger count = new AtomicInteger(1);
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://placeholder.ldaptive.org").build();
    final MockConnection<Q, S> conn = new MockConnection<>(config);
    conn.setOpenPredicate(ldapURL -> true);
    setOperationFunction(conn);
    conn.setWriteConsumer(h -> {
      h.messageID(1);
      h.sent();
      h.result(createResponse(ResultCode.NO_SUCH_OBJECT));
      // close connection since it will be reused by the next handler invocation
      conn.close();
    });
    final Q request = createRequest();
    final DefaultOperationHandle<Q, S> handle = createOperationHandle(
      request,
      MockConnection.builder(
          ConnectionConfig.builder().url("ldap://directory.ldaptive.org").build())
        .openPredicate(ldapURL -> true)
        .build());

    final ReferralResultHandler<S> handler = createHandler(conn, true);
    ((MessageFunctional<Q, S>) handler).setRequest(request);
    ((MessageFunctional<Q, S>) handler).setHandle(handle);
    final S referralResponse = createReferralResponse(
      "ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?");

    try {
      handler.apply(referralResponse);
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(RuntimeException.class);
      assertThat(e.getCause()).isExactlyInstanceOf(LdapException.class);
      assertThat(e.getCause().getMessage()).startsWith("Could not follow referral");
    }
  }


  @Test(groups = "referral")
  @SuppressWarnings("unchecked")
  public void applyThrowsRuntimeException()
  {
    final AtomicInteger count = new AtomicInteger(1);
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://placeholder.ldaptive.org").build();
    final MockConnection<Q, S> conn = new MockConnection<>(config);
    conn.setOpenPredicate(ldapURL -> true);
    setOperationFunction(conn);
    conn.setWriteConsumer(h -> {
      if (count.getAndIncrement() == 2) {
        h.messageID(1);
        h.sent();
        h.result(createReferralResponse(
          "ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?"));
        // close connection since it will be reused by the next handler invocation
        conn.close();
      } else {
        throw new RuntimeException("Test Exception");
      }
    });
    final Q request = createRequest();
    final DefaultOperationHandle<Q, S> handle = createOperationHandle(
      request,
      MockConnection.builder(
          ConnectionConfig.builder().url("ldap://directory.ldaptive.org").build())
        .openPredicate(ldapURL -> true)
        .build());

    final ReferralResultHandler<S> handler = createHandler(10, conn);
    ((MessageFunctional<Q, S>) handler).setRequest(request);
    ((MessageFunctional<Q, S>) handler).setHandle(handle);
    final S referralResponse = createReferralResponse(
      "ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?");

    assertThat(handler.apply(referralResponse))
      .isEqualTo(createReferralResponse("ldap://ds1.ldaptive.org:389/dc=ldaptive,dc=org??sub?"));
  }


  @Test(groups = "referral")
  @SuppressWarnings("unchecked")
  public void applyThrowsRuntimeExceptionWithFailure()
  {
    final AtomicInteger count = new AtomicInteger(1);
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://placeholder.ldaptive.org").build();
    final MockConnection<Q, S> conn = new MockConnection<>(config);
    conn.setOpenPredicate(ldapURL -> true);
    setOperationFunction(conn);
    conn.setWriteConsumer(h -> {
      if (count.getAndIncrement() == 2) {
        h.messageID(1);
        h.sent();
        h.result(createReferralResponse(
          "ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?"));
        // close connection since it will be reused by the next handler invocation
        conn.close();
      } else {
        throw new RuntimeException("Test Exception");
      }
    });
    final Q request = createRequest();
    final DefaultOperationHandle<Q, S> handle = createOperationHandle(
      request,
      MockConnection.builder(
          ConnectionConfig.builder().url("ldap://directory.ldaptive.org").build())
        .openPredicate(ldapURL -> true)
        .build());

    final ReferralResultHandler<S> handler = createHandler(10, conn, true);
    ((MessageFunctional<Q, S>) handler).setRequest(request);
    ((MessageFunctional<Q, S>) handler).setHandle(handle);
    final S referralResponse = createReferralResponse(
      "ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?");

    try {
      handler.apply(referralResponse);
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(RuntimeException.class);
      assertThat(e.getCause()).isExactlyInstanceOf(LdapException.class);
      assertThat(
        e.getCause().getMessage())
        .containsPattern("[Referral|Search] result handler org.ldaptive.referral.Follow.* threw exception");
    }
  }


  @Test(groups = "referral")
  @SuppressWarnings("unchecked")
  public void applyThrowsLdapException()
  {
    final AtomicInteger count = new AtomicInteger(1);
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://placeholder.ldaptive.org").build();
    final MockConnection<Q, S> conn = new MockConnection<>(config);
    conn.setOpenPredicate(ldapURL -> true);
    setOperationFunction(conn);
    conn.setWriteConsumer(h -> {
      if (count.getAndIncrement() == 2) {
        h.messageID(1);
        h.sent();
        h.result(createReferralResponse(
          "ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?"));
        // close connection since it will be reused by the next handler invocation
        conn.close();
      } else {
        throw new RuntimeException(new LdapException("Test Exception"));
      }
    });
    final Q request = createRequest();
    final DefaultOperationHandle<Q, S> handle = createOperationHandle(
      request,
      MockConnection.builder(
          ConnectionConfig.builder().url("ldap://directory.ldaptive.org").build())
        .openPredicate(ldapURL -> true)
        .build());

    final ReferralResultHandler<S> handler = createHandler(10, conn);
    ((MessageFunctional<Q, S>) handler).setRequest(request);
    ((MessageFunctional<Q, S>) handler).setHandle(handle);
    final S referralResponse = createReferralResponse(
      "ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?");

    assertThat(handler.apply(referralResponse))
      .isEqualTo(createReferralResponse("ldap://ds1.ldaptive.org:389/dc=ldaptive,dc=org??sub?"));
  }


  @Test(groups = "referral")
  @SuppressWarnings("unchecked")
  public void applyThrowsLdapExceptionWithFailure()
  {
    final AtomicInteger count = new AtomicInteger(1);
    final ConnectionConfig config = ConnectionConfig.builder().url("ldap://placeholder.ldaptive.org").build();
    final MockConnection<Q, S> conn = new MockConnection<>(config);
    conn.setOpenPredicate(ldapURL -> true);
    setOperationFunction(conn);
    conn.setWriteConsumer(h -> {
      if (count.getAndIncrement() == 2) {
        h.messageID(1);
        h.sent();
        h.result(createReferralResponse(
          "ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?"));
        // close connection since it will be reused by the next handler invocation
        conn.close();
      } else {
        throw new RuntimeException(new LdapException("Test Exception"));
      }
    });
    final Q request = createRequest();
    final DefaultOperationHandle<Q, S> handle = createOperationHandle(
      request,
      MockConnection.builder(
          ConnectionConfig.builder().url("ldap://directory.ldaptive.org").build())
        .openPredicate(ldapURL -> true)
        .build());

    final ReferralResultHandler<S> handler = createHandler(10, conn, true);
    ((MessageFunctional<Q, S>) handler).setRequest(request);
    ((MessageFunctional<Q, S>) handler).setHandle(handle);
    final S referralResponse = createReferralResponse(
      "ldap://ds" + count.getAndIncrement() + ".ldaptive.org:389/dc=ldaptive,dc=org??sub?");

    try {
      handler.apply(referralResponse);
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(RuntimeException.class);
      assertThat(e.getCause()).isExactlyInstanceOf(LdapException.class);
      assertThat(e.getCause().getMessage()).isEqualTo("Test Exception");
    }
  }
}
