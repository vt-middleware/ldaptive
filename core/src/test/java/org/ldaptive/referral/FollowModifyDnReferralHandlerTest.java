/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import java.time.Duration;
import org.ldaptive.MockConnectionFactory;
import org.ldaptive.ModifyDnRequest;
import org.ldaptive.ModifyDnResponse;
import org.ldaptive.ResultCode;
import org.ldaptive.transport.DefaultOperationHandle;
import org.ldaptive.transport.TransportConnection;
import org.ldaptive.transport.mock.MockConnection;

/**
 * Unit test for {@link FollowModifyDnReferralHandler}.
 *
 * @author  Middleware Services
 */
public class FollowModifyDnReferralHandlerTest
  extends AbstractFollowReferralHandlerTest<ModifyDnRequest, ModifyDnResponse>
{


  @Override
  FollowModifyDnReferralHandler createHandler()
  {
    return new FollowModifyDnReferralHandler();
  }


  @Override
  FollowModifyDnReferralHandler createHandler(final MockConnection<ModifyDnRequest, ModifyDnResponse> conn)
  {
    return new FollowModifyDnReferralHandler(url -> new MockConnectionFactory(conn));
  }


  @Override
  FollowModifyDnReferralHandler createHandler(
    final int limit, final MockConnection<ModifyDnRequest, ModifyDnResponse> conn)
  {
    return new FollowModifyDnReferralHandler(limit, url -> new MockConnectionFactory(conn));
  }


  @Override
  FollowModifyDnReferralHandler createHandler(
    final MockConnection<ModifyDnRequest, ModifyDnResponse> conn, final boolean throwOnFailure)
  {
    return new FollowModifyDnReferralHandler(url -> new MockConnectionFactory(conn), throwOnFailure);
  }


  @Override
  FollowModifyDnReferralHandler createHandler(
    final int limit, final MockConnection<ModifyDnRequest, ModifyDnResponse> conn, final boolean throwOnFailure)
  {
    return new FollowModifyDnReferralHandler(limit, url -> new MockConnectionFactory(conn), throwOnFailure);
  }


  @Override
  void setOperationFunction(final MockConnection<ModifyDnRequest, ModifyDnResponse> conn)
  {
    conn.setModifyDnOperationFunction(req -> createOperationHandle(req, conn));
  }


  @Override
  DefaultOperationHandle<ModifyDnRequest, ModifyDnResponse> createOperationHandle(
    final ModifyDnRequest request, final TransportConnection conn)
  {
    return new DefaultOperationHandle<>(request, conn, Duration.ofSeconds(5));
  }


  @Override
  ModifyDnRequest createRequest()
  {
    return ModifyDnRequest.builder()
      .oldDN("ou=foo,dc=ldaptive,dc=org")
      .newRDN("ou=bar")
      .build();
  }


  @Override
  ModifyDnResponse createResponse(final ResultCode code)
  {
    return ModifyDnResponse.builder()
      .messageID(1)
      .resultCode(code)
      .build();
  }


  @Override
  ModifyDnResponse createSuccessResponse()
  {
    return ModifyDnResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.SUCCESS)
      .build();
  }


  @Override
  ModifyDnResponse createReferralResponse(final String... urls)
  {
    return ModifyDnResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.REFERRAL)
      .referralURLs(urls)
      .build();
  }
}
