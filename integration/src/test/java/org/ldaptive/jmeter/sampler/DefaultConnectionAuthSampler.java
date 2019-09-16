/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jmeter.sampler;

import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.jmeter.setup_teardown.TestPlanConfig;

/**
 * Fully implemented instance of {@link org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient} that uses
 * a {@link org.ldaptive.DefaultConnectionFactory} to run LDAP Authentication tests in a multi-threaded environment.
 *
 * @author Middleware Services
 */
public class DefaultConnectionAuthSampler extends AbstractAuthLoadSampler
{

  @Override
  public DefaultConnectionFactory connectionFactory()
  {
    return TestPlanConfig.getAuthDefaultConnectionFactory();
  }
}
