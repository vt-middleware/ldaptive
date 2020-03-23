/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Class for profiling multiple instances of {@link PooledConnectionFactory}.
 *
 * @author  Middleware Services
 */
public final class MultiPooledSearchOperationProfile extends AbstractSearchOperationProfile
{

  /** Default pool size. */
  private static final int POOL_SIZE = 10;

  /** Connection factory. */
  protected ConnectionFactory connectionFactory2;

  /** Connection factory. */
  protected ConnectionFactory connectionFactory3;


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
      .validator(SearchConnectionValidator.builder()
        .period(Duration.ofMinutes(1))
        .build())
      .build();
    ((PooledConnectionFactory) connectionFactory).initialize();

    connectionFactory2 = PooledConnectionFactory.builder()
      .config(ConnectionConfig.builder()
        .url(new LdapURL(host + "-2", port).getHostnameWithSchemeAndPort())
        .connectionInitializers(
          BindConnectionInitializer.builder()
            .dn(bindDn)
            .credential(bindCredential)
            .build())
        .build())
      .min(POOL_SIZE)
      .max(POOL_SIZE)
      .validator(SearchConnectionValidator.builder()
        .period(Duration.ofMinutes(1))
        .build())
      .build();
    ((PooledConnectionFactory) connectionFactory2).initialize();

    connectionFactory3 = PooledConnectionFactory.builder()
      .config(ConnectionConfig.builder()
        .url(new LdapURL(host + "-3", port).getHostnameWithSchemeAndPort())
        .connectionInitializers(
          BindConnectionInitializer.builder()
            .dn(bindDn)
            .credential(bindCredential)
            .build())
        .build())
      .min(POOL_SIZE)
      .max(POOL_SIZE)
      .validator(SearchConnectionValidator.builder()
        .period(Duration.ofMinutes(1))
        .build())
      .build();
    ((PooledConnectionFactory) connectionFactory3).initialize();
  }


  @Override
  protected void shutdown()
  {
    connectionFactory.close();
    connectionFactory2.close();
    connectionFactory3.close();
  }


  @Override
  protected void doOperation(final Consumer<Object> consumer, final int uid)
  {
    doOperation(connectionFactory, consumer, uid);
    doOperation(connectionFactory2, consumer, uid);
    doOperation(connectionFactory3, consumer, uid);
  }


  @Override
  protected void createEntries(final int count)
  {
    createEntries(connectionFactory, UID_START, count);
    createEntries(connectionFactory2, UID_START, count);
    createEntries(connectionFactory3, UID_START, count);
  }


  @Override
  public String toString()
  {
    final StringBuilder sb = new StringBuilder();
    sb.append(connectionFactory != null ? connectionFactory.toString() : "[null connection factory]").append(" -- ");
    sb.append(connectionFactory2 != null ? connectionFactory2.toString() : "[null connection factory2]").append(" -- ");
    sb.append(connectionFactory3 != null ? connectionFactory3.toString() : "[null connection factory3]");
    return sb.toString();
  }
}
