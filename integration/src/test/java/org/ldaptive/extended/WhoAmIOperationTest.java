/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import org.ldaptive.AbstractTest;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.Connection;
import org.ldaptive.Response;
import org.ldaptive.TestControl;
import org.ldaptive.TestUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Unit test for {@link WhoAmIOperation}.
 *
 * @author  Middleware Services
 */
public class WhoAmIOperationTest extends AbstractTest
{


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"extended"})
  public void whoami()
    throws Exception
  {
    // AD supports whoami, but returns a completely different value
    if (TestControl.isActiveDirectory()) {
      return;
    }

    final Connection conn = TestUtils.createConnection();
    try {
      conn.open();
      final WhoAmIOperation whoami = new WhoAmIOperation(conn);
      final Response<String> res = whoami.execute(new WhoAmIRequest());
      final BindConnectionInitializer ci =
        (BindConnectionInitializer)
          conn.getConnectionConfig().getConnectionInitializer();
      AssertJUnit.assertEquals("dn:" + ci.getBindDn(), res.getResult());
    } finally {
      conn.close();
    }
  }
}
