/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import java.lang.reflect.Constructor;
import org.ldaptive.transport.netty.NettyTransport;

/**
 * Factory for creating connections.
 *
 * @author  Middleware Services
 */
public final class TransportFactory
{

  /** Ldap transport system property. */
  private static final String TRANSPORT_PROPERTY = "org.ldaptive.transport";

  /** Custom transport constructor. */
  private static final Constructor<?> TRANSPORT_CONSTRUCTOR;

  static {
    // Initialize a custom transport if a system property is found
    final String transportClass = System.getProperty(TRANSPORT_PROPERTY);
    if (transportClass != null) {
      try {
        TRANSPORT_CONSTRUCTOR = Class.forName(transportClass).getDeclaredConstructor();
      } catch (Exception e) {
        throw new IllegalStateException("Error instantiating " + transportClass, e);
      }
    } else {
      TRANSPORT_CONSTRUCTOR = null;
    }
  }


  /** Default constructor. */
  private TransportFactory() {}


  /**
   * The {@link #TRANSPORT_PROPERTY} property is checked and that class is loaded if provided. Otherwise a Netty
   * transport is returned.
   *
   * @return  transport
   */
  public static Transport getTransport()
  {
    if (TRANSPORT_CONSTRUCTOR != null) {
      try {
        return (Transport) TRANSPORT_CONSTRUCTOR.newInstance();
      } catch (Exception e) {
        throw new IllegalStateException("Error creating new transport instance with " + TRANSPORT_CONSTRUCTOR, e);
      }
    }
    return new NettyTransport();
  }
}
