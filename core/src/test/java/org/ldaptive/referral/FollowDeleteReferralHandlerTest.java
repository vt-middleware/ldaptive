/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import java.time.Duration;
import org.ldaptive.DeleteRequest;
import org.ldaptive.DeleteResponse;
import org.ldaptive.MockConnectionFactory;
import org.ldaptive.ResultCode;
import org.ldaptive.transport.DefaultOperationHandle;
import org.ldaptive.transport.TransportConnection;
import org.ldaptive.transport.mock.MockConnection;

/**
 * Unit test for {@link FollowDeleteReferralHandler}.
 *
 * @author  Middleware Services
 */
public class FollowDeleteReferralHandlerTest extends AbstractFollowReferralHandlerTest<DeleteRequest, DeleteResponse>
{


  @Override
  FollowDeleteReferralHandler createHandler()
  {
    return new FollowDeleteReferralHandler();
  }


  @Override
  FollowDeleteReferralHandler createHandler(final MockConnection<DeleteRequest, DeleteResponse> conn)
  {
    return new FollowDeleteReferralHandler(url -> new MockConnectionFactory(conn));
  }


  @Override
  FollowDeleteReferralHandler createHandler(final int limit, final MockConnection<DeleteRequest, DeleteResponse> conn)
  {
    return new FollowDeleteReferralHandler(limit, url -> new MockConnectionFactory(conn));
  }


  @Override
  FollowDeleteReferralHandler createHandler(
    final MockConnection<DeleteRequest, DeleteResponse> conn, final boolean throwOnFailure)
  {
    return new FollowDeleteReferralHandler(url -> new MockConnectionFactory(conn), throwOnFailure);
  }


  @Override
  FollowDeleteReferralHandler createHandler(
    final int limit, final MockConnection<DeleteRequest, DeleteResponse> conn, final boolean throwOnFailure)
  {
    return new FollowDeleteReferralHandler(limit, url -> new MockConnectionFactory(conn), throwOnFailure);
  }


  @Override
  void setOperationFunction(final MockConnection<DeleteRequest, DeleteResponse> conn)
  {
    conn.setDeleteOperationFunction(req -> createOperationHandle(req, conn));
  }


  @Override
  DefaultOperationHandle<DeleteRequest, DeleteResponse> createOperationHandle(
    final DeleteRequest request, final TransportConnection conn)
  {
    return new DefaultOperationHandle<>(request, conn, Duration.ofSeconds(5));
  }


  @Override
  DeleteRequest createRequest()
  {
    return DeleteRequest.builder()
      .dn("uid=1,ou=test,dc=ldaptive,dc=org")
      .build();
  }


  @Override
  DeleteResponse createResponse(final ResultCode code)
  {
    return DeleteResponse.builder()
      .messageID(1)
      .resultCode(code)
      .build();
  }


  @Override
  DeleteResponse createSuccessResponse()
  {
    return DeleteResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.SUCCESS)
      .build();
  }


  @Override
  DeleteResponse createReferralResponse(final String... urls)
  {
    return DeleteResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.REFERRAL)
      .referralURLs(urls)
      .build();
  }
}
