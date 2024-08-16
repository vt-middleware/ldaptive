/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import java.time.Duration;
import org.ldaptive.CompareRequest;
import org.ldaptive.CompareResponse;
import org.ldaptive.MockConnectionFactory;
import org.ldaptive.ResultCode;
import org.ldaptive.transport.DefaultCompareOperationHandle;
import org.ldaptive.transport.TransportConnection;
import org.ldaptive.transport.mock.MockConnection;

/**
 * Unit test for {@link FollowCompareReferralHandler}.
 *
 * @author  Middleware Services
 */
public class FollowCompareReferralHandlerTest extends AbstractFollowReferralHandlerTest<CompareRequest, CompareResponse>
{


  @Override
  FollowCompareReferralHandler createHandler()
  {
    return new FollowCompareReferralHandler();
  }


  @Override
  FollowCompareReferralHandler createHandler(final MockConnection<CompareRequest, CompareResponse> conn)
  {
    return new FollowCompareReferralHandler(url -> new MockConnectionFactory(conn));
  }


  @Override
  FollowCompareReferralHandler createHandler(
    final int limit, final MockConnection<CompareRequest, CompareResponse> conn)
  {
    return new FollowCompareReferralHandler(limit, url -> new MockConnectionFactory(conn));
  }


  @Override
  FollowCompareReferralHandler createHandler(
    final MockConnection<CompareRequest, CompareResponse> conn, final boolean throwOnFailure)
  {
    return new FollowCompareReferralHandler(url -> new MockConnectionFactory(conn), throwOnFailure);
  }


  @Override
  FollowCompareReferralHandler createHandler(
    final int limit, final MockConnection<CompareRequest, CompareResponse> conn, final boolean throwOnFailure)
  {
    return new FollowCompareReferralHandler(limit, url -> new MockConnectionFactory(conn), throwOnFailure);
  }


  @Override
  void setOperationFunction(final MockConnection<CompareRequest, CompareResponse> conn)
  {
    conn.setCompareOperationFunction(req -> createOperationHandle(req, conn));
  }


  @Override
  DefaultCompareOperationHandle createOperationHandle(
    final CompareRequest request, final TransportConnection conn)
  {
    return new DefaultCompareOperationHandle(request, conn, Duration.ofSeconds(5));
  }


  @Override
  CompareRequest createRequest()
  {
    return CompareRequest.builder()
      .dn("ou=test,dc=ldaptive,dc=org")
      .name("uid")
      .value("1")
      .build();
  }


  @Override
  CompareResponse createResponse(final ResultCode code)
  {
    return CompareResponse.builder()
      .messageID(1)
      .resultCode(code)
      .build();
  }


  @Override
  CompareResponse createSuccessResponse()
  {
    return CompareResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.COMPARE_TRUE)
      .build();
  }


  @Override
  CompareResponse createReferralResponse(final String... urls)
  {
    return CompareResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.REFERRAL)
      .referralURLs(urls)
      .build();
  }
}
