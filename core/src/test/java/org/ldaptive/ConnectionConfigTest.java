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

    cc.assertMutable();
    cc.getSslConfig().assertMutable();
    ((Freezable) cc.getConnectionInitializers()[0]).assertMutable();
    ((Freezable) cc.getConnectionStrategy()).assertMutable();
    ((Freezable) cc.getConnectionValidator()).assertMutable();

    cc.freeze();
    TestUtils.testImmutable(cc);
    TestUtils.testImmutable(cc.getSslConfig());
    TestUtils.testImmutable((Freezable) cc.getConnectionInitializers()[0]);
    TestUtils.testImmutable((Freezable) cc.getConnectionStrategy());
    TestUtils.testImmutable((Freezable) cc.getConnectionValidator());
  }
}
