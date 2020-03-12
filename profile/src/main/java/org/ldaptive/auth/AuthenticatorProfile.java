/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

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

/**
 * Class for profiling {@link Authenticator}.
 *
 * @author  Middleware Services
 */
public class AuthenticatorProfile extends AbstractProfile
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
  protected void initialize(final String host, final int port)
  {
    resolverConnectionFactory = newConnectionFactory(host, port, POOL_SIZE);
    bindConnectionFactory = newConnectionFactory(host, port, POOL_SIZE);
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


  /**
   * Creates a new pooled connection factory.
   *
   * @param  host  to connect to
   * @param  port  to connect to
   * @param  size  of the pool
   *
   * @return  pooled connection factory
   */
  private PooledConnectionFactory newConnectionFactory(final String host, final int port, final int size)
  {
    final PooledConnectionFactory connectionFactory = PooledConnectionFactory.builder()
      .config(ConnectionConfig.builder()
        .url(new LdapURL(host, port).getUrl())
        .connectionInitializers(
          BindConnectionInitializer.builder()
            .dn(bindDn)
            .credential(bindCredential)
            .build())
        .build())
      .min(size)
      .max(size)
      .build();
    connectionFactory.initialize();
    return connectionFactory;
  }


  @Override
  protected void shutdown()
  {
    resolverConnectionFactory.close();
    bindConnectionFactory.close();
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
  protected void doOperation(final Consumer<Object> consumer, final int uid)
  {
    try {
      final AuthenticationResponse result = authenticator.authenticate(
        new AuthenticationRequest(String.valueOf(uid), new Credential("password" + uid)));
      if (!result.isSuccess()) {
        System.out.println("AUTHENTICATION FAILURE: " + result);
      }
      consumer.accept(result);
    } catch (LdapException e) {
      System.out.println("CAUGHT EXCEPTION:: " + e.getMessage());
    }
  }


  @Override
  protected void createEntries(final int count)
  {
    createEntries(bindConnectionFactory, UID_START, count);
  }


  @Override
  public String toString()
  {
    return authenticator != null ? authenticator.toString() : "[null authenticator]";
  }
}
