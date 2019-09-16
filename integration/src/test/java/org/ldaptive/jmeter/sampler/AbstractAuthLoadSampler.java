/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jmeter.sampler;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.ldaptive.Credential;
import org.ldaptive.LdapException;
import org.ldaptive.auth.AuthenticationRequest;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.SearchDnResolver;
import org.ldaptive.auth.SimpleBindAuthenticationHandler;
import org.ldaptive.jmeter.setup_teardown.TestPlanConfig;

/**
 * Base class for Authentication based load sampling. Implementations only need to provide the
 * {@link org.ldaptive.ConnectionFactory} to be used.
 *
 * @author Middleware Services
 */
public abstract class AbstractAuthLoadSampler extends AbstractSampler
{


  @Override
  public SampleResult runTest(final JavaSamplerContext context)
  {
    super.runTest(context);
    final String authBaseDn = TestPlanConfig.getProperties().baseDn();
    final String bindFilter = TestPlanConfig.getProperties().bindFilter();
    final String authUserId = TestPlanConfig.getProperties().authRequestId();
    final String authUserCredential = TestPlanConfig.getProperties().bindCredential();
    final SearchDnResolver dnResolver = SearchDnResolver.builder()
      .factory(connectionFactory())
      .dn(authBaseDn)
      .filter(bindFilter)
      .build();
    // Create and start the Sampler
    final SampleResult sampleResult = new SampleResult();
    sampleResult.sampleStart();
    final SimpleBindAuthenticationHandler authHandler = new SimpleBindAuthenticationHandler(connectionFactory());
    final Authenticator auth = new Authenticator(dnResolver, authHandler);
    try {
      final AuthenticationResponse authResponse =
        auth.authenticate(new AuthenticationRequest(authUserId, new Credential(authUserCredential)));
      if (authResponse.isSuccess()) {
        successResult(sampleResult, authResponse.toString());
      } else {
        failedResult(sampleResult, "Authenticated failed");
      }
    } catch (LdapException e) {
      LOGGER.error("Unexpected exception occurred", e);
      failedResult(sampleResult, e);
    }
    return sampleResult;
  }
}
