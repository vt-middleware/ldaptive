/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import java.time.Duration;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapAttribute;
import org.ldaptive.MockConnectionFactory;
import org.ldaptive.ModifyRequest;
import org.ldaptive.ModifyResponse;
import org.ldaptive.ResultCode;
import org.ldaptive.transport.DefaultOperationHandle;
import org.ldaptive.transport.TransportConnection;
import org.ldaptive.transport.mock.MockConnection;

/**
 * Unit test for {@link FollowModifyReferralHandler}.
 *
 * @author  Middleware Services
 */
public class FollowModifyReferralHandlerTest extends AbstractFollowReferralHandlerTest<ModifyRequest, ModifyResponse>
{


  @Override
  FollowModifyReferralHandler createHandler()
  {
    return new FollowModifyReferralHandler();
  }


  @Override
  FollowModifyReferralHandler createHandler(final MockConnection<ModifyRequest, ModifyResponse> conn)
  {
    return new FollowModifyReferralHandler(url -> new MockConnectionFactory(conn));
  }


  @Override
  FollowModifyReferralHandler createHandler(final int limit, final MockConnection<ModifyRequest, ModifyResponse> conn)
  {
    return new FollowModifyReferralHandler(limit, url -> new MockConnectionFactory(conn));
  }


  @Override
  FollowModifyReferralHandler createHandler(
    final MockConnection<ModifyRequest, ModifyResponse> conn, final boolean throwOnFailure)
  {
    return new FollowModifyReferralHandler(url -> new MockConnectionFactory(conn), throwOnFailure);
  }


  @Override
  FollowModifyReferralHandler createHandler(
    final int limit, final MockConnection<ModifyRequest, ModifyResponse> conn, final boolean throwOnFailure)
  {
    return new FollowModifyReferralHandler(limit, url -> new MockConnectionFactory(conn), throwOnFailure);
  }


  @Override
  void setOperationFunction(final MockConnection<ModifyRequest, ModifyResponse> conn)
  {
    conn.setModifyOperationFunction(req -> createOperationHandle(req, conn));
  }


  @Override
  DefaultOperationHandle<ModifyRequest, ModifyResponse> createOperationHandle(
    final ModifyRequest request, final TransportConnection conn)
  {
    return new DefaultOperationHandle<>(request, conn, Duration.ofSeconds(5));
  }


  @Override
  ModifyRequest createRequest()
  {
    return ModifyRequest.builder()
      .dn("uid=1,ou=test,dc=ldaptive,dc=org")
      .modifications(
        new AttributeModification(
          AttributeModification.Type.ADD, LdapAttribute.builder().name("uidNumber").values("1").build()))
      .build();
  }


  @Override
  ModifyResponse createResponse(final ResultCode code)
  {
    return ModifyResponse.builder()
      .messageID(1)
      .resultCode(code)
      .build();
  }


  @Override
  ModifyResponse createSuccessResponse()
  {
    return ModifyResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.SUCCESS)
      .build();
  }


  @Override
  ModifyResponse createReferralResponse(final String... urls)
  {
    return ModifyResponse.builder()
      .messageID(1)
      .resultCode(ResultCode.REFERRAL)
      .referralURLs(urls)
      .build();
  }
}
