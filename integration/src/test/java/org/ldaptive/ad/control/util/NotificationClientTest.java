/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control.util;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.ldaptive.AbstractTest;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModificationType;
import org.ldaptive.Connection;
import org.ldaptive.LdapAttribute;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.TestControl;
import org.ldaptive.TestUtils;
import org.ldaptive.async.AsyncRequest;
import org.testng.AssertJUnit;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link NotificationClient}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2993 $ $Date: 2014-06-02 17:16:40 -0400 (Mon, 02 Jun 2014) $
 */
public class NotificationClientTest extends AbstractTest
{


  /**
   * @param  dn  to search on.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ncSearchDn")
  @Test(groups = {"control-util"})
  public void execute(final String dn)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
      return;
    }

    // provider doesn't support this control
    if (TestControl.isApacheProvider()) {
      return;
    }

    Connection conn = TestUtils.createConnection();
    try {
      conn.open();
      final NotificationClient client = new NotificationClient(conn);

      final SearchRequest request = new SearchRequest(
        "ou=test,dc=middleware,dc=vt,dc=edu",
        new SearchFilter("(objectClass=*)"));
      request.setSearchScope(SearchScope.ONELEVEL);
      final BlockingQueue<NotificationClient.NotificationItem> results =
        client.execute(request);

      NotificationClient.NotificationItem item = results.poll(
        5, TimeUnit.SECONDS);
      AssertJUnit.assertNotNull(item);
      if (item.isException()) {
        throw item.getException();
      }
      AssertJUnit.assertTrue(item.isAsyncRequest());
      AssertJUnit.assertTrue(item.getAsyncRequest().getMessageId() > 0);
      final AsyncRequest asyncRequest = item.getAsyncRequest();

      final ModifyOperation modify = new ModifyOperation(conn);
      modify.execute(
        new ModifyRequest(
          dn,
          new AttributeModification(
            AttributeModificationType.REPLACE,
            new LdapAttribute(
              "sn",
              Integer.toString(new Random().nextInt(1000000))))));

      item = results.poll(5, TimeUnit.SECONDS);
      AssertJUnit.assertNotNull(item);
      AssertJUnit.assertTrue(item.isEntry());
      AssertJUnit.assertNotNull(item.getEntry());

      asyncRequest.abandon();

    } finally {
      final ModifyOperation modify = new ModifyOperation(conn);
      modify.execute(
        new ModifyRequest(
          dn,
          new AttributeModification(
            AttributeModificationType.REPLACE,
            new LdapAttribute("sn", "Admin"))));

      conn.close();
    }
  }
}
