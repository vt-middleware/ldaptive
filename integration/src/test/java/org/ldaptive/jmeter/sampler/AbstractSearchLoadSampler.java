/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jmeter.sampler;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchResponse;
import org.ldaptive.jmeter.setup_teardown.TestPlanConfig;

/**
 * Abstract of {@link AbstractSampler} that provides the implementation of
 * {@link org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient#runTest(JavaSamplerContext)}
 * specific to LDAP Search tests.
 *
 * @author Middleware Services
 */
public abstract class AbstractSearchLoadSampler extends AbstractSampler
{


  @Override
  public SampleResult runTest(final JavaSamplerContext context)
  {
    super.runTest(context);
    final String baseDn = TestPlanConfig.getProperties().baseDn();
    final String searchFilter = TestPlanConfig.getProperties().searchFilter();
    final SearchOperation searchOperation = new SearchOperation(connectionFactory(), baseDn);
    final SampleResult sampleResult = new SampleResult();
    sampleResult.sampleStart();
    try {
      final SearchResponse searchResponse = searchOperation.execute(searchFilter);
      final LdapEntry entry = searchResponse.getEntry();
      if (entry != null) {
        successResult(sampleResult, entry);
      } else {
        successResult(sampleResult, "Search request was successful");
      }
    } catch (LdapException e) {
      LOGGER.error("Unexpected exception occurred while performing search", e);
      failedResult(sampleResult, e);
    }
    return sampleResult;
  }
}
