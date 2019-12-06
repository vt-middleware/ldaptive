/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jmeter.setup_teardown;

// CheckStyle:AvoidStaticImport OFF
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.ldaptive.jmeter.sampler.AbstractSampler.whoAmI;
// CheckStyle:AvoidStaticImport ON

/**
 * This class does the initial setup for the tests that will be shared among all {@link ThreadGroup}s in a
 * {@link org.apache.jmeter.testelement.TestPlan}. Therefore, it MUST be the
 * {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerClient} assigned to the
 * {@link org.apache.jmeter.threads.SetupThreadGroup} to help prevent multi-threading issues once the actual testing
 * groups start executing their tests.
 *
 * @author Middleware Services
 */
public class SetupSampler extends AbstractJavaSamplerClient
{

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(SetupSampler.class);


  @Override
  public void setupTest(final JavaSamplerContext context)
  {
    LOGGER.info("Setting up global test config with: " + whoAmI());
    TestPlanConfig.initConnections(TestPlanConfig.ConnectionType.ALL, context);
    final ThreadGroupProperties threadGroupProperties = new ThreadGroupProperties(context);
    final String testDetails =
      "**** Current Test Parameters ****" +
        System.lineSeparator() +
        threadGroupProperties.threadGroupPlanDetails() +
        System.lineSeparator() +
        TestPlanConfig.getProperties().toString();
    System.out.println(testDetails);
  }


  @Override
  public SampleResult runTest(final JavaSamplerContext context)
  {
    return null;
  }
}
