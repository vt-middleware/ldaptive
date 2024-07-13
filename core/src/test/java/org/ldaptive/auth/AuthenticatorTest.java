/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import java.util.Collections;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.Freezable;
import org.ldaptive.MockConnectionFactory;
import org.ldaptive.TestUtils;
import org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.EDirectoryAuthenticationResponseHandler;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link Authenticator}.
 *
 * @author  Middleware Services
 */
public class AuthenticatorTest
{


  /**
   * Unit test for {@link Authenticator#close()}.
   */
  @Test(groups = "auth")
  public void close()
  {
    final Authenticator auth = new Authenticator();
    auth.setDnResolver(new SearchDnResolver(new MockConnectionFactory(new ConnectionConfig())));
    auth.setAuthenticationHandler(
      new SimpleBindAuthenticationHandler(new MockConnectionFactory(new ConnectionConfig())));
    auth.setEntryResolver(new SearchEntryResolver(new MockConnectionFactory(new ConnectionConfig())));
    auth.freeze();
    auth.close();

    assertThat(
      ((MockConnectionFactory)
        ((SearchDnResolver) auth.getDnResolver()).getConnectionFactory()).isOpen()).isFalse();
    assertThat(
      ((MockConnectionFactory)
        ((SimpleBindAuthenticationHandler) auth.getAuthenticationHandler()).getConnectionFactory()).isOpen()).isFalse();
    assertThat(
      ((MockConnectionFactory)
        ((SearchEntryResolver) auth.getEntryResolver()).getConnectionFactory()).isOpen()).isFalse();
  }


  /**
   * Unit test for {@link Authenticator#close()} wired with aggregate components.
   */
  @Test(groups = "auth")
  public void closeAggregate()
  {
    final Authenticator auth = new Authenticator();
    auth.setDnResolver(
      new AggregateDnResolver(
        Collections.singletonMap("1", new SearchDnResolver(new MockConnectionFactory(new ConnectionConfig())))));
    auth.setAuthenticationHandler(
      new AggregateAuthenticationHandler(
        Collections.singletonMap(
          "1", new SimpleBindAuthenticationHandler(new MockConnectionFactory(new ConnectionConfig())))));
    auth.setEntryResolver(
      new AggregateEntryResolver(
        Collections.singletonMap("1", new SearchEntryResolver(new MockConnectionFactory(new ConnectionConfig())))));
    auth.freeze();
    auth.close();

    assertThat(
      ((MockConnectionFactory)
        ((SearchDnResolver)
          ((AggregateDnResolver)
            auth.getDnResolver()).getDnResolvers()
              .values().iterator().next()).getConnectionFactory()).isOpen()).isFalse();
    assertThat(
      ((MockConnectionFactory)
        ((SimpleBindAuthenticationHandler)
          ((AggregateAuthenticationHandler)
            auth.getAuthenticationHandler()).getAuthenticationHandlers()
              .values().iterator().next()).getConnectionFactory()).isOpen()).isFalse();
    assertThat(
      ((MockConnectionFactory)
        ((SearchEntryResolver)
          ((AggregateEntryResolver) auth.getEntryResolver()).getEntryResolvers()
              .values().iterator().next()).getConnectionFactory()).isOpen()).isFalse();
  }


  /**
   * Unit test for {@link Authenticator#freeze()}.
   */
  @Test(groups = "auth")
  public void immutable()
  {
    final Authenticator auth = new Authenticator();
    auth.setDnResolver(new SearchDnResolver());
    auth.setAuthenticationHandler(new SimpleBindAuthenticationHandler());
    auth.setEntryResolver(new SearchEntryResolver());
    auth.setResponseHandlers(
      new ActiveDirectoryAuthenticationResponseHandler(), new EDirectoryAuthenticationResponseHandler());

    auth.assertMutable();
    ((Freezable) auth.getDnResolver()).assertMutable();
    ((Freezable) auth.getAuthenticationHandler()).assertMutable();
    ((Freezable) auth.getEntryResolver()).assertMutable();
    Arrays.stream(auth.getResponseHandlers()).forEach(ah -> ((Freezable) ah).assertMutable());

    auth.freeze();
    TestUtils.testImmutable(auth);
    TestUtils.testImmutable((Freezable) auth.getDnResolver());
    TestUtils.testImmutable((Freezable) auth.getAuthenticationHandler());
    TestUtils.testImmutable((Freezable) auth.getEntryResolver());
    Arrays.stream(auth.getResponseHandlers()).forEach(ah -> TestUtils.testImmutable((Freezable) ah));
  }
}
