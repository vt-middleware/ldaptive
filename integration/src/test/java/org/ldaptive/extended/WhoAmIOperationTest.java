/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import org.ldaptive.AbstractTest;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.TestControl;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;
import static org.ldaptive.TestUtils.*;

/**
 * Unit test for the WhoAmI extended operation.
 *
 * @author  Middleware Services
 */
public class WhoAmIOperationTest extends AbstractTest
{


  /** @throws  Exception  On test failure. */
  @Test(groups = "extended")
  public void whoami()
    throws Exception
  {
    // AD supports whoami, but returns a completely different value
    if (TestControl.isActiveDirectory()) {
      return;
    }

    final DefaultConnectionFactory cf = (DefaultConnectionFactory) createConnectionFactory();
    final ExtendedOperation whoami = new ExtendedOperation(cf);
    final ExtendedResponse res = whoami.execute(new WhoAmIRequest());
    final BindConnectionInitializer ci =
      (BindConnectionInitializer) cf.getConnectionConfig().getConnectionInitializers()[0];
    assertThat(WhoAmIResponseParser.parse(res)).isEqualTo("dn:" + ci.getBindDn());
  }
}
