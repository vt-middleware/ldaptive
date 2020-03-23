/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Class for profiling {@link PooledConnectionFactory}.
 *
 * @author  Middleware Services
 */
public final class PooledSearchOperationProfile extends AbstractSearchOperationProfile
{

  /** Default pool size. */
  private static final int POOL_SIZE = 10;


  @Override
  protected void initialize(final String host, final int port)
  {
    connectionFactory = PooledConnectionFactory.builder()
      .config(ConnectionConfig.builder()
        .url(new LdapURL(host, port).getHostnameWithSchemeAndPort())
        .connectionInitializers(
          BindConnectionInitializer.builder()
            .dn(bindDn)
            .credential(bindCredential)
            .build())
        .build())
      .min(POOL_SIZE)
      .max(POOL_SIZE)
      .build();
    ((PooledConnectionFactory) connectionFactory).initialize();
  }
}
