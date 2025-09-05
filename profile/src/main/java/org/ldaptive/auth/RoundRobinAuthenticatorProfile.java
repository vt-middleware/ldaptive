/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.time.Duration;
import java.util.function.Consumer;
import org.ldaptive.AbstractProfile;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.Credential;
import org.ldaptive.LdapException;
import org.ldaptive.LdapURL;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.RoundRobinConnectionStrategy;

/**
 * Class for profiling {@link Authenticator}.
 *
 * @author  Middleware Services
 */
public class RoundRobinAuthenticatorProfile extends AbstractProfile
{

  /** Default pool size. */
  private static final int POOL_SIZE = 10;

  /** Authenticator. */
  protected Authenticator authenticator;

  /** DN resolver connection factory. */
  protected ConnectionFactory resolverConnectionFactory;

  /** Bind connection factory. */
  protected ConnectionFactory bindConnectionFactory;

  /** Base DN. */
  protected String baseDn;

  /** Bind DN. */
  protected String bindDn;

  /** Bind credential. */
  protected String bindCredential;


  @Override
  // CheckStyle:MagicNumber OFF
  protected void initialize(final String host, final int port)
  {
    resolverConnectionFactory = PooledConnectionFactory.builder()
      .config(ConnectionConfig.builder()
        .url(
          String.format(
            "%s %s %s",
            new LdapURL(host, port).getHostnameWithSchemeAndPort(),
            new LdapURL(host + "-2", port).getHostnameWithSchemeAndPort(),
            new LdapURL(host + "-3", port).getHostnameWithSchemeAndPort()))
        .connectTimeout(Duration.ofSeconds(5))
        .connectionStrategy(new RoundRobinConnectionStrategy())
        .connectionInitializers(
          BindConnectionInitializer.builder()
            .dn(bindDn)
            .credential(bindCredential)
            .build())
        .build())
      .blockWaitTime(Duration.ofSeconds(5))
      .failFastInitialize(false)
      .min(POOL_SIZE)
      .max(POOL_SIZE)
      .build();
    ((PooledConnectionFactory) resolverConnectionFactory).initialize();

    bindConnectionFactory = PooledConnectionFactory.builder()
      .config(ConnectionConfig.builder()
        .url(
          String.format(
            "%s %s %s",
            new LdapURL(host, port).getHostnameWithSchemeAndPort(),
            new LdapURL(host + "-2", port).getHostnameWithSchemeAndPort(),
            new LdapURL(host + "-3", port).getHostnameWithSchemeAndPort()))
        .connectTimeout(Duration.ofSeconds(5))
        .connectionStrategy(new RoundRobinConnectionStrategy())
        .build())
      .blockWaitTime(Duration.ofSeconds(5))
      .failFastInitialize(false)
      .min(POOL_SIZE)
      .max(POOL_SIZE)
      .build();
    ((PooledConnectionFactory) bindConnectionFactory).initialize();

    authenticator = Authenticator.builder()
      .dnResolver(SearchDnResolver.builder()
        .dn(baseDn)
        .filter("(uid={user})")
        .subtreeSearch(true)
        .factory(resolverConnectionFactory)
        .build())
      .authenticationHandler(new SimpleBindAuthenticationHandler(bindConnectionFactory))
      .returnAttributes(ReturnAttributes.ALL_USER.value())
      .build();
  }
  // CheckStyle:MagicNumber ON


  @Override
  protected void shutdown()
  {
    authenticator.close();
  }


  @Override
  protected void setBaseDn(final String dn)
  {
    baseDn = dn;
  }


  @Override
  protected void setBindDn(final String dn)
  {
    bindDn = dn;
  }


  @Override
  protected void setBindCredential(final String pass)
  {
    bindCredential = pass;
  }


  @Override
  protected int doOperation(final Consumer<Object> consumer, final int uid)
  {
    try {
      final AuthenticationResponse result = authenticator.authenticate(
        new AuthenticationRequest(String.valueOf(uid), new Credential("password" + uid)));
      if (!result.isSuccess()) {
        consumer.accept(new RuntimeException("Authentication failure for " + result));
      }
      consumer.accept(new Result(result));
    } catch (LdapException e) {
      consumer.accept(e);
    }
    return 1;
  }


  @Override
  public String toString()
  {
    return authenticator != null ? authenticator.toString() : "[null authenticator]";
  }
}
