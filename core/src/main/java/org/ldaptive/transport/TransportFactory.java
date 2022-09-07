/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapUtils;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.SingleConnectionFactory;
import org.ldaptive.transport.netty.ConnectionFactoryTransport;
import org.ldaptive.transport.netty.ConnectionTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating connection transports.
 *
 * @author  Middleware Services
 */
public final class TransportFactory
{

  /** Ldap transport system property. */
  private static final String POOLED_FACTORY_TRANSPORT_PROPERTY = "org.ldaptive.transport.pooledConnectionFactory";

  /** Ldap transport system property. */
  private static final String SINGLE_FACTORY_TRANSPORT_PROPERTY = "org.ldaptive.transport.singleConnectionFactory";

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(TransportFactory.class);

  /** Map of connection factory class to transport constructor. */
  private static final Map<Class<? extends ConnectionFactory>, Constructor<?>> TRANSPORT_OVERRIDE;

  // initialize TRANSPORT_OVERRIDE
  static {
    final Map<Class<? extends ConnectionFactory>, Constructor<?>> constructors = new HashMap<>(2);
    final Constructor<?> pooledTransport = LdapUtils.createConstructorFromProperty(POOLED_FACTORY_TRANSPORT_PROPERTY);
    if (pooledTransport != null) {
      constructors.put(PooledConnectionFactory.class, pooledTransport);
    }
    final Constructor<?> singleTransport = LdapUtils.createConstructorFromProperty(SINGLE_FACTORY_TRANSPORT_PROPERTY);
    if (singleTransport != null) {
      constructors.put(SingleConnectionFactory.class, singleTransport);
    }
    TRANSPORT_OVERRIDE = Collections.unmodifiableMap(constructors);
    if (!TRANSPORT_OVERRIDE.isEmpty()) {
      LOGGER.info("Transport override set to {}", TRANSPORT_OVERRIDE);
    }
  }


  /** Default constructor. */
  private TransportFactory() {}


  /**
   * The {@link #TRANSPORT_OVERRIDE} map is checked and that class is loaded if provided. Otherwise, the default
   * transport for the supplied class is provided.
   *
   * @param  clazz  to return transport for
   *
   * @return  transport
   */
  public static Transport getTransport(final Class<? extends ConnectionFactory> clazz)
  {
    if (TRANSPORT_OVERRIDE.containsKey(clazz)) {
      try {
        return (Transport) TRANSPORT_OVERRIDE.get(clazz).newInstance();
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
