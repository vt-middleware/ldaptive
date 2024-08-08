/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.handler;

import java.time.Duration;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.transport.DefaultSearchOperationHandle;
import org.ldaptive.transport.mock.MockConnection;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link RangeEntryHandler}.
 *
 * @author  Middleware Services
 */
public class RangeEntryHandlerTest
{

  /** Handler to test. */
  private final RangeEntryHandler handler = new RangeEntryHandler();


  /**
   * Range entries.
   *
   * @return  test data
   */
  @DataProvider(name = "range-entries")
  public Object[][] rangeEntries()
  {
    return
      new Object[][] {
        new Object[] {
          LdapEntry.builder()
            .messageID(1)
            .dn("cn=test-group,ou=groups,dc=ldaptive,dc=org")
            .attributes(LdapAttribute.builder()
              .name("member;Range=0-4")
              .values("alpha", "bravo", "charlie", "delta", "echo")
              .build())
            .build(),
          LdapEntry.builder()
            .messageID(1)
            .dn("cn=test-group,ou=groups,dc=ldaptive,dc=org")
            .attributes(LdapAttribute.builder()
              .name("member;Range=5-*")
              .values("foxtrot", "golf", "hotel", "india")
              .build())
            .build(),
        },
      };
  }


  @Test(dataProvider = "range-entries")
  public void apply(final LdapEntry entry1, final LdapEntry entry2)
  {
    final MockConnection<SearchRequest, SearchResponse> conn = new MockConnection<>(
      ConnectionConfig.builder().url("ldap://directory.ldaptive.org").build());
    conn.setSearchOperationFunction(
      req -> new DefaultSearchOperationHandle(req, conn, Duration.ofSeconds(1)));
    conn.setWriteConsumer(h -> {
      h.messageID(1);
      h.sent();
      ((DefaultSearchOperationHandle) h).entry(entry2);
      h.result(
        SearchResponse.builder().messageID(1).resultCode(ResultCode.SUCCESS).build());
    });
    handler.setConnection(conn);
    final SearchResponse response =
      SearchResponse.builder().messageID(1).resultCode(ResultCode.SUCCESS).entry(entry1).build();
    final SearchResponse rangeResponse = handler.apply(response);
    assertThat(rangeResponse).isEqualTo(
      SearchResponse.builder()
        .messageID(1)
        .resultCode(ResultCode.SUCCESS)
        .entry(LdapEntry.builder()
          .messageID(1)
          .dn("cn=test-group,ou=groups,dc=ldaptive,dc=org")
          .attributes(LdapAttribute.builder()
            .name("member")
            .stringValues(
              Stream.concat(
                entry1.getAttribute().getStringValues().stream(),
                entry2.getAttribute().getStringValues().stream())
                .collect(Collectors.toList()))
            .build())
          .build())
        .build());
  }
}
