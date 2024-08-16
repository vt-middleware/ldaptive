/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import java.time.Duration;
import org.ldaptive.AddRequest;
import org.ldaptive.AddResponse;
import org.ldaptive.LdapAttribute;
import org.ldaptive.MockConnectionFactory;
import org.ldaptive.ResultCode;
import org.ldaptive.transport.DefaultOperationHandle;
import org.ldaptive.transport.TransportConnection;
import org.ldaptive.transport.mock.MockConnection;

/**
 * Unit test for {@link FollowAddReferralHandler}.
 *
 * @author  Middleware Services
 */
public class FollowAddReferralHandlerTest extends AbstractFollowReferralHandlerTest<AddRequest, AddResponse>
{


  @Override
  FollowAddReferralHandler createHandler()
  {
    return new FollowAddReferralHandler();
  }


  @Override
  FollowAddReferralHandler createHandler(final MockConnection<AddRequest, AddResponse> conn)
  {
    return new FollowAddReferralHandler(url -> new MockConnectionFactory(conn));
  }


  @Override
  FollowAddReferralHandler createHandler(final int limit, final MockConnection<AddRequest, AddResponse> conn)
  {
    return new FollowAddReferralHandler(limit, url -> new MockConnectionFactory(conn));
  }


  @Override
  FollowAddReferralHandler createHandler(
    final MockConnection<AddRequest, AddResponse> conn, final boolean throwOnFailure)
  {
    return new FollowAddReferralHandler(url -> new MockConnectionFactory(conn), throwOnFailure);
  }


  @Override
  FollowAddReferralHandler createHandler(
    final int limit, final MockConnection<AddRequest, AddResponse> conn, final boolean throwOnFailure)
  {
    return new FollowAddReferralHandler(limit, url -> new MockConnectionFactory(conn), throwOnFailure);
  }


  @Override
  void setOperationFunction(final MockConnection<AddRequest, AddResponse> conn)
  {
    conn.setAddOperationFunction(req -> createOperationHandle(req, conn));
  }


  @Override
  DefaultOperationHandle<AddRequest, AddResponse> createOperationHandle(
    final AddRequest request, final TransportConnection conn)
  {
    return new DefaultOperationHandle<>(request, conn, Duration.ofSeconds(5));
  }


  @Override
  AddRequest createRequest()
  {
    return AddRequest.builder()
      .dn("ou=test,dc=ldaptive,dc=org")
      .attributes(LdapAttribute.builder().name("uid").values("1").build())
      .build();
  }


  @Override
  AddResponse createResponse(final ResultCode code)
  {
    return AddResponse.builder()
      .messageID(1)
      .resultCode(code)
      .build();
  }


  @Override
  AddResponse createSuccessResponse()
  {
    return AddResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.SUCCESS)
      .build();
  }


  @Override
  AddResponse createReferralResponse(final String... urls)
  {
    return AddResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.REFERRAL)
      .referralURLs(urls)
      .build();
  }
}
