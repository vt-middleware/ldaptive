/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import java.lang.reflect.Constructor;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapUtils;
import org.ldaptive.transport.netty.NettyTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating connection transports based on the type of {@link ConnectionFactory} being used.
 *
 * @author  Middleware Services
 */
public final class TransportFactory
{

  /** Transport factory system property. */
  private static final String TRANSPORT_FACTORY_PROPERTY = "org.ldaptive.transport.factory";

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(TransportFactory.class);

  /** Used to create transports. */
  private static final DelegateTransportFactory TRANSPORT_FACTORY;

  // initialize TRANSPORT_FACTORY
  static {
    final Constructor<DelegateTransportFactory> transportFactory = LdapUtils.createConstructorFromProperty(
      TRANSPORT_FACTORY_PROPERTY);
    if (transportFactory != null) {
      try {
        TRANSPORT_FACTORY = transportFactory.newInstance();
        LOGGER.info("Transport factory override set to {}", TRANSPORT_FACTORY);
      } catch (Exception e) {
        LOGGER.error("Error creating new transport factory instance with {}", transportFactory, e);
        throw new IllegalStateException(e);
      }
    } else {
      TRANSPORT_FACTORY = new NettyTransportFactory();
    }
  }


  /** Default constructor. */
  private TransportFactory() {}


  /**
   * Returns a transport for the supplied connection factory type.
   *
   * @param  clazz  to return transport for
   *
   * @return  transport
   */
  public static Transport getTransport(final Class<? extends ConnectionFactory> clazz)
  {
    return TRANSPORT_FACTORY.createTransport(clazz);
  }
}
