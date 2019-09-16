/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jmeter.setup_teardown;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

/**
 * Sampler that must be attached to a {@link org.apache.jmeter.threads.PostThreadGroup}. All global teardown operations
 * should occur there.
 *
 * @author Middleware Services
 */
public class TeardownSampler extends AbstractJavaSamplerClient
{


  @Override
  public void teardownTest(final JavaSamplerContext context)
  {
    TestPlanConfig.teardownConfiguration();
  }


  @Override
  public SampleResult runTest(final JavaSamplerContext context)
  {
    return null;
  }
}
