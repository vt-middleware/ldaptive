/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Class for profiling {@link DefaultConnectionFactory}.
 *
 * @author  Middleware Services
 */
public final class DefaultSearchOperationProfile extends AbstractSearchOperationProfile
{


  @Override
  protected void initialize(final String host, final int port)
  {
    connectionFactory = DefaultConnectionFactory.builder()
      .config(ConnectionConfig.builder()
        .url(new LdapURL(host, port).getUrl())
        .connectionInitializers(
          BindConnectionInitializer.builder()
            .dn(bindDn)
            .credential(bindCredential)
            .build())
        .build())
      .build();
  }
}
