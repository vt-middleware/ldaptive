/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jmeter.sampler;

import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.jmeter.setup_teardown.TestPlanConfig;

/**
 * Fully implemented instance of {@link org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient} that uses
 * a {@link org.ldaptive.PooledConnectionFactory} to run LDAP Search tests in a multi-threaded environment.
 *
 * @author Middleware Services
 */
public class PooledConnectionSearchSampler extends AbstractSearchLoadSampler
{

  @Override
  public PooledConnectionFactory connectionFactory()
  {
    return TestPlanConfig.getPooledConnectionFactory();
  }
}
