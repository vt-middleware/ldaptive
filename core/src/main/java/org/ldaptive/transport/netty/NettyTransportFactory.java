/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapUtils;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.SingleConnectionFactory;
import org.ldaptive.transport.DelegateTransportFactory;
import org.ldaptive.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty specific implementation for creating {@link Transport}.
 *
 * @author  Middleware Services
 */
public class NettyTransportFactory implements DelegateTransportFactory
{

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(NettyTransportFactory.class);

  /** Transport factory system property. */
  private static final String POOLED_TRANSPORT_FACTORY_PROPERTY =
    "org.ldaptive.transport.netty.pooledConnectionFactory";

  /** Transport factory system property. */
  private static final String SINGLE_TRANSPORT_FACTORY_PROPERTY =
    "org.ldaptive.transport.netty.singleConnectionFactory";

  /** Transport factory system property. */
  private static final String DEFAULT_TRANSPORT_FACTORY_PROPERTY =
    "org.ldaptive.transport.netty.defaultConnectionFactory";

  /** Map of connection factory class to transport constructor. */
  private static final Map<Class<? extends ConnectionFactory>, Constructor<Transport>> TRANSPORT_OVERRIDE;

  // initialize TRANSPORT_OVERRIDE
  static {
    final Map<Class<? extends ConnectionFactory>, Constructor<Transport>> constructors = new HashMap<>(2);
    final Constructor<Transport> pooledTransport =
      LdapUtils.createConstructorFromProperty(POOLED_TRANSPORT_FACTORY_PROPERTY);
    if (pooledTransport != null) {
      constructors.put(PooledConnectionFactory.class, pooledTransport);
    }
    final Constructor<Transport> singleTransport =
      LdapUtils.createConstructorFromProperty(SINGLE_TRANSPORT_FACTORY_PROPERTY);
    if (singleTransport != null) {
      constructors.put(SingleConnectionFactory.class, singleTransport);
    }
    final Constructor<Transport> defaultTransport =
      LdapUtils.createConstructorFromProperty(DEFAULT_TRANSPORT_FACTORY_PROPERTY);
    if (defaultTransport != null) {
      constructors.put(DefaultConnectionFactory.class, defaultTransport);
    }
    TRANSPORT_OVERRIDE = Collections.unmodifiableMap(constructors);
    if (!TRANSPORT_OVERRIDE.isEmpty()) {
      LOGGER.info("Netty transport override set to {}", TRANSPORT_OVERRIDE);
    }
  }

  @Override
  public Transport createTransport(final Class<? extends ConnectionFactory> clazz)
  {
    if (!TRANSPORT_OVERRIDE.isEmpty() && TRANSPORT_OVERRIDE.containsKey(clazz)) {
      try {
        return TRANSPORT_OVERRIDE.get(clazz).newInstance();
      } catch (Exception e) {
        LOGGER.error("Error creating new transport instance with {}", TRANSPORT_OVERRIDE.get(clazz), e);
        throw new IllegalStateException(e);
      }
    }
    final Transport transport;
    if (PooledConnectionFactory.class.isAssignableFrom(clazz)) {
      transport = new ConnectionFactoryTransport();
    } else if (SingleConnectionFactory.class.isAssignableFrom(clazz)) {
      transport = new ConnectionTransport.SingleThread();
    } else if (DefaultConnectionFactory.class.isAssignableFrom(clazz)) {
      transport = new ConnectionTransport.SingleThread();
    } else {
      // be conservative for unknown connection factory types
      transport = new ConnectionTransport.SingleThread();
    }
    return transport;
  }
}
