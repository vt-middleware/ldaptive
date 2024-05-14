/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.ssl.SslConfig;
import org.testng.annotations.Test;

/**
 * Unit test for {@link ConnectionConfig}.
 *
 * @author  Middleware Services
 */
public class ConnectionConfigTest
{


  @Test
  public void immutable()
  {
    final ConnectionConfig cc = new ConnectionConfig();
    cc.setSslConfig(new SslConfig());
    cc.setConnectionInitializers(new BindConnectionInitializer());
    cc.setConnectionStrategy(new ActivePassiveConnectionStrategy());
    cc.setConnectionValidator(new SearchConnectionValidator());

    cc.checkImmutable();
    cc.getSslConfig().checkImmutable();
    ((Immutable) cc.getConnectionInitializers()[0]).checkImmutable();
    ((Immutable) cc.getConnectionStrategy()).checkImmutable();
    ((Immutable) cc.getConnectionValidator()).checkImmutable();

    cc.makeImmutable();
    TestUtils.testImmutable(cc);
    TestUtils.testImmutable(cc.getSslConfig());
    TestUtils.testImmutable((Immutable) cc.getConnectionInitializers()[0]);
    TestUtils.testImmutable((Immutable) cc.getConnectionStrategy());
    TestUtils.testImmutable((Immutable) cc.getConnectionValidator());
  }
}
