/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jmeter.sampler;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.jmeter.setup_teardown.TestPlanConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class that provides most of the setup for all {@link org.apache.jmeter.protocol.java.sampler.JavaSampler}
 * test types.
 *
 * @author Middleware Services
 */
public abstract class AbstractSampler extends AbstractJavaSamplerClient
{

  /** Logger instance */
  static final Logger LOGGER = LoggerFactory.getLogger(AbstractSampler.class);



  /**
   * Default constructor.
   *
   * The Java Sampler uses the default constructor to instantiate an instance
   * of the client class.
   */
  AbstractSampler()
  {
    LOGGER.debug(whoAmI() + "\tConstruct");
  }

  /**
   * Do an initial check to ensure the global test configuration was successful in the
   * {@link org.apache.jmeter.threads.SetupThreadGroup}. This is executed on the first iteration and will break out
   * of all tests in a Thread Group if the ThreadGroup has been configured to stop tests on failure, which it should
   * be.
   */
  @Override
  public SampleResult runTest(final JavaSamplerContext context)
  {
    if (context.getJMeterContext().getVariables().getIteration() == 1) {
      final SampleResult sampleResult = new SampleResult();
      sampleResult.sampleStart();
      // Return sampler with failed status so we can break out of tests
      if (!TestPlanConfig.isSetupSuccess()) {
        setupFailedResult(sampleResult);
        return sampleResult;
      }
    }
    return null;
  }

  /**
   * Update current SampleResult with failed response when test setup has failed.
   *
   * @param sampleResult SampleResult to update
   */
  private void setupFailedResult(final SampleResult sampleResult)
  {
    failedResult(sampleResult, new LdapException("Setup failed. See log for more details"));
  }

  /**
   * Update current SampleResult with success response.
   *
   * @param sampleResult SampleResult to update
   * @param message The response body to add for a successful request.
   */
  void successResult(final SampleResult sampleResult, final String message)
  {
    sampleResult.sampleEnd();
    sampleResult.setResponseCodeOK();
    sampleResult.setResponseMessageOK();
    sampleResult.setSuccessful(true);
    sampleResult.setResponseData(message, "UTF-8");
  }


  /**
   * Update current SampleResult with success response and LdapEntry in body.
   *
   * @param sampleResult SampleResult to update
   * @param entry The LdapEntry expected for a successful request.
   */
  void successResult(final SampleResult sampleResult, final LdapEntry entry)
  {
    successResult(sampleResult, entry.toString());
  }


  /**
   * Default failed SampleResult
   *
   * @param sampleResult SampleResult to update
   */
  private void failedResult(final SampleResult sampleResult)
  {
    sampleResult.sampleEnd();
    sampleResult.setResponseCode("5000");
    sampleResult.setResponseMessage("Failed");
    sampleResult.setSuccessful(false);
  }


  /**
   * Update current SampleResult with failed response and exception message.
   *
   * @param sampleResult SampleResult to update
   * @param ex The exception that occurred to cause the failure.
   */
  void failedResult(final SampleResult sampleResult, final LdapException ex)
  {
    failedResult(sampleResult);
    sampleResult.setResponseData(ex.getMessage(), "UTF-8");
  }


  /**
   * Update current SampleResult with failed response and provided message.
   *
   * @param sampleResult SampleResult to update
   * @param message response message to include with failed result.
   */
  void failedResult(final SampleResult sampleResult, final String message)
  {
    failedResult(sampleResult);
    sampleResult.setResponseData(message, "UTF-8");
  }


  /**
   * Generate a String identifier of this test for debugging purposes.
   *
   * @return a String identifier for this test instance
   */
  public static String whoAmI()
  {
    return Thread.currentThread().toString();
  }


  /**
   * {@link ConnectionFactory} to be used for this thread group
   *
   * @return fully initialized factory that can be used for making requests to ldap.
   */
  public abstract ConnectionFactory connectionFactory();
}
