/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Class for profiling {@link SingleConnectionFactory}.
 *
 * @author  Middleware Services
 */
public final class SingleSearchOperationProfile extends AbstractSearchOperationProfile
{


  @Override
  protected void initialize(final String host, final int port)
  {
    connectionFactory = SingleConnectionFactory.builder()
      .config(ConnectionConfig.builder()
        .url(new LdapURL(host, port).getHostnameWithSchemeAndPort())
        .connectionInitializers(
          BindConnectionInitializer.builder()
            .dn(bindDn)
            .credential(bindCredential)
            .build())
        .build())
      .build();
    try {
      ((SingleConnectionFactory) connectionFactory).initialize();
    } catch (LdapException e) {
      throw new IllegalStateException("Could not initialize connection factory", e);
    }
  }
}
