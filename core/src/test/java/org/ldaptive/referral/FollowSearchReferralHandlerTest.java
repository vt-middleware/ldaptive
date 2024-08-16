/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import java.time.Duration;
import org.ldaptive.MockConnectionFactory;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.transport.DefaultSearchOperationHandle;
import org.ldaptive.transport.TransportConnection;
import org.ldaptive.transport.mock.MockConnection;

/**
 * Unit test for {@link FollowSearchReferralHandler}.
 *
 * @author  Middleware Services
 */
public class FollowSearchReferralHandlerTest extends AbstractFollowReferralHandlerTest<SearchRequest, SearchResponse>
{


  @Override
  FollowSearchReferralHandler createHandler()
  {
    return new FollowSearchReferralHandler();
  }


  @Override
  FollowSearchReferralHandler createHandler(final MockConnection<SearchRequest, SearchResponse> conn)
  {
    return new FollowSearchReferralHandler(url -> new MockConnectionFactory(conn));
  }


  @Override
  FollowSearchReferralHandler createHandler(final int limit, final MockConnection<SearchRequest, SearchResponse> conn)
  {
    return new FollowSearchReferralHandler(limit, url -> new MockConnectionFactory(conn));
  }


  @Override
  FollowSearchReferralHandler createHandler(
    final MockConnection<SearchRequest, SearchResponse> conn, final boolean throwOnFailure)
  {
    return new FollowSearchReferralHandler(url -> new MockConnectionFactory(conn), throwOnFailure);
  }


  @Override
  FollowSearchReferralHandler createHandler(
    final int limit, final MockConnection<SearchRequest, SearchResponse> conn, final boolean throwOnFailure)
  {
    return new FollowSearchReferralHandler(limit, url -> new MockConnectionFactory(conn), throwOnFailure);
  }


  @Override
  void setOperationFunction(final MockConnection<SearchRequest, SearchResponse> conn)
  {
    conn.setSearchOperationFunction(req -> createOperationHandle(req, conn));
  }


  @Override
  DefaultSearchOperationHandle createOperationHandle(final SearchRequest request, final TransportConnection conn)
  {
    return new DefaultSearchOperationHandle(request, conn, Duration.ofSeconds(5));
  }


  @Override
  SearchRequest createRequest()
  {
    return SearchRequest.builder()
      .dn("ou=test,dc=ldaptive,dc=org")
      .filter("(uid=1)")
      .build();
  }


  @Override
  SearchResponse createResponse(final ResultCode code)
  {
    return SearchResponse.builder()
      .messageID(1)
      .resultCode(code)
      .build();
  }


  @Override
  SearchResponse createSuccessResponse()
  {
    return SearchResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.SUCCESS)
      .build();
  }


  @Override
  SearchResponse createReferralResponse(final String... urls)
  {
    return SearchResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.REFERRAL)
      .referralURLs(urls)
      .build();
  }
}
