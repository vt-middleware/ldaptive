/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control.util;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.ldaptive.AbstractTest;
import org.ldaptive.AttributeModification;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.TestControl;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;
import static org.ldaptive.TestUtils.*;

/**
 * Unit test for {@link NotificationClient}.
 *
 * @author  Middleware Services
 */
public class NotificationClientTest extends AbstractTest
{


  /**
   * @param  dn  to search on.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ncSearchDn")
  @Test(groups = "control-util")
  public void execute(final String dn)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
      return;
    }

    final ConnectionFactory cf = createConnectionFactory();
    try {
      final NotificationClient client = new NotificationClient(cf);

      final SearchRequest request = new SearchRequest("ou=test,dc=middleware,dc=vt,dc=edu", "(objectClass=*)");
      request.setSearchScope(SearchScope.ONELEVEL);

      final BlockingQueue<NotificationClient.NotificationItem> results = client.execute(request);

      NotificationClient.NotificationItem item = results.poll(5, TimeUnit.SECONDS);
      assertThat(item).isNotNull();
      if (item.isException()) {
        throw item.getException();
      }
      assertThat(item.getResult().getMessageID()).isGreaterThan(0);

      final ModifyOperation modify = new ModifyOperation(cf);
      modify.execute(
        new ModifyRequest(
          dn,
          new AttributeModification(
            AttributeModification.Type.REPLACE,
            new LdapAttribute("sn", Integer.toString(new Random().nextInt(1000000))))));

      item = results.poll(5, TimeUnit.SECONDS);
      assertThat(item).isNotNull();
      assertThat(item.isEntry()).isTrue();
      assertThat(item.getEntry()).isNotNull();

      client.abandon();
    } finally {
      final ModifyOperation modify = new ModifyOperation(cf);
      modify.execute(
        new ModifyRequest(
          dn,
          new AttributeModification(AttributeModification.Type.REPLACE, new LdapAttribute("sn", "Admin"))));
    }
  }
}
