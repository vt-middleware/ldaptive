/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import java.lang.reflect.Constructor;
import org.ldaptive.transport.netty.ConnectionTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating connections.
 *
 * @author  Middleware Services
 */
public final class TransportFactory
{

  /** Ldap transport system property. */
  private static final String TRANSPORT_PROPERTY = "org.ldaptive.transport";

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(TransportFactory.class);

  /** Custom transport constructor. */
  private static final Constructor<?> TRANSPORT_CONSTRUCTOR;

  static {
    // Initialize a custom transport if a system property is found
    final String transportClass = System.getProperty(TRANSPORT_PROPERTY);
    if (transportClass != null) {
      try {
        // note that the number of IO threads can be controlled with the io.netty.eventLoopThreads system property
        LOGGER.info("Setting ldap transport to {}", transportClass);
        TRANSPORT_CONSTRUCTOR = Class.forName(transportClass).getDeclaredConstructor();
      } catch (Exception e) {
        LOGGER.error("Error instantiating {}", transportClass, e);
        throw new IllegalStateException(e);
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
   * @return  ldaptive transport
   */
  public static Transport getTransport()
  {
    if (TRANSPORT_CONSTRUCTOR != null) {
      try {
        return (Transport) TRANSPORT_CONSTRUCTOR.newInstance();
      } catch (Exception e) {
        LOGGER.error("Error creating new transport instance with {}", TRANSPORT_CONSTRUCTOR, e);
        throw new IllegalStateException(e);
      }
    }
    return new ConnectionTransport(1);
  }
}
