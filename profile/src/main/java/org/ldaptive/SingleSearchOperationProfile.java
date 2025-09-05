/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;

/**
 * Class for profiling {@link SingleConnectionFactory}.
 *
 * @author  Middleware Services
 */
public final class SingleSearchOperationProfile extends AbstractSearchOperationProfile
{


  @Override
  // CheckStyle:MagicNumber OFF
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
      .failFastInitialize(false)
      .build();
    ((SingleConnectionFactory) connectionFactory).setValidator(
      SearchConnectionValidator.builder()
        .period(Duration.ofSeconds(5))
        .onFailure(((SingleConnectionFactory) connectionFactory).new ReinitializeConnectionConsumer())
        .build());
    try {
      ((SingleConnectionFactory) connectionFactory).initialize();
    } catch (LdapException e) {
      throw new IllegalStateException("Could not initialize connection factory", e);
    }
  }
  // CheckStyle:MagicNumber ON
}
