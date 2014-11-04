/*
  $Id: WhoAmIOperationTest.java 2669 2013-03-15 20:32:36Z dfisher $

  Copyright (C) 2003-2010 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2669 $
  Updated: $Date: 2013-03-15 16:32:36 -0400 (Fri, 15 Mar 2013) $
*/
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
 * @version  $Revision: 2669 $ $Date: 2013-03-15 16:32:36 -0400 (Fri, 15 Mar 2013) $
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
