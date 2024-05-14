/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import java.util.Collections;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.Immutable;
import org.ldaptive.MockConnectionFactory;
import org.ldaptive.TestUtils;
import org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.EDirectoryAuthenticationResponseHandler;
import org.testng.Assert;
import org.testng.annotations.Test;

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
    auth.makeImmutable();
    auth.close();

    Assert.assertFalse(
      ((MockConnectionFactory)
        ((SearchDnResolver) auth.getDnResolver()).getConnectionFactory()).isOpen());
    Assert.assertFalse(
      ((MockConnectionFactory)
        ((SimpleBindAuthenticationHandler) auth.getAuthenticationHandler()).getConnectionFactory()).isOpen());
    Assert.assertFalse(
      ((MockConnectionFactory)
        ((SearchEntryResolver) auth.getEntryResolver()).getConnectionFactory()).isOpen());
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
    auth.makeImmutable();
    auth.close();

    Assert.assertFalse(
      ((MockConnectionFactory)
        ((SearchDnResolver)
          ((AggregateDnResolver)
            auth.getDnResolver()).getDnResolvers()
              .values().iterator().next()).getConnectionFactory()).isOpen());
    Assert.assertFalse(
      ((MockConnectionFactory)
        ((SimpleBindAuthenticationHandler)
          ((AggregateAuthenticationHandler)
            auth.getAuthenticationHandler()).getAuthenticationHandlers()
              .values().iterator().next()).getConnectionFactory()).isOpen());
    Assert.assertFalse(
      ((MockConnectionFactory)
        ((SearchEntryResolver)
          ((AggregateEntryResolver) auth.getEntryResolver()).getEntryResolvers()
              .values().iterator().next()).getConnectionFactory()).isOpen());
  }


  /**
   * Unit test for {@link Authenticator#makeImmutable()}.
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

    auth.checkImmutable();
    ((Immutable) auth.getDnResolver()).checkImmutable();
    ((Immutable) auth.getAuthenticationHandler()).checkImmutable();
    ((Immutable) auth.getEntryResolver()).checkImmutable();
    Arrays.stream(auth.getResponseHandlers()).forEach(ah -> ((Immutable) ah).checkImmutable());

    auth.makeImmutable();
    TestUtils.testImmutable(auth);
    TestUtils.testImmutable((Immutable) auth.getDnResolver());
    TestUtils.testImmutable((Immutable) auth.getAuthenticationHandler());
    TestUtils.testImmutable((Immutable) auth.getEntryResolver());
    Arrays.stream(auth.getResponseHandlers()).forEach(ah -> TestUtils.testImmutable((Immutable) ah));
  }
}
