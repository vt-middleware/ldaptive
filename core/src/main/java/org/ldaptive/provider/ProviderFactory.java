/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import java.lang.reflect.Constructor;
import org.ldaptive.provider.netty.SharedNettyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating connections.
 *
 * @author  Middleware Services
 */
public final class ProviderFactory
{

  /** Ldap provider system property. */
  public static final String PROVIDER = "org.ldaptive.provider";

  /** Logger for this class. */
  private static  final Logger LOGGER = LoggerFactory.getLogger(ProviderFactory.class);

  /** Custom provider constructor. */
  private static Constructor<?> providerConstructor;

  /** Initialize a custom provider if a system property is found. */
  static {
    final String providerClass = System.getProperty(PROVIDER);
    if (providerClass != null) {
      try {
        LOGGER.info("Setting ldap provider to {}", providerClass);
        providerConstructor = Class.forName(providerClass).getDeclaredConstructor();
      } catch (Exception e) {
        LOGGER.error("Error instantiating {}", providerClass, e);
        throw new IllegalStateException(e);
      }
    }
  }


  /** Default constructor. */
  private ProviderFactory() {}


  /**
   * The {@link #PROVIDER} property is checked and that class is loaded if provided. Otherwise the Netty provider is
   * returned.
   *
   * @return  ldaptive provider
   */
  public static Provider getProvider()
  {
    if (providerConstructor != null) {
      try {
        return (Provider) providerConstructor.newInstance();
      } catch (Exception e) {
        LOGGER.error("Error creating new provider instance with {}", providerConstructor, e);
        throw new IllegalStateException(e);
      }
    }
    return new SharedNettyProvider();
  }
}
