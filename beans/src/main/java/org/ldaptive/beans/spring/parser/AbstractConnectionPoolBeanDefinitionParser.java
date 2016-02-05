/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring.parser;

import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.pool.SearchValidator;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

/**
 * Common implementation for all pooled connection factories.
 *
 * @author Middleware Services
 */
public abstract class AbstractConnectionPoolBeanDefinitionParser extends AbstractConnectionFactoryBeanDefinitionParser
{


  /**
   * Creates a blocking connection pool.
   *
   * @param  builder  bean definition builder to set properties on, may be null
   * @param  name  of the connection pool
   * @param  element  containing configuration
   * @param  includeConnectionInitializer  whether to include a connection initializer
   *
   * @return  blocking connection pool bean definition builder
   */
  protected BeanDefinitionBuilder parseConnectionPool(
    final BeanDefinitionBuilder builder,
    final String name,
    final Element element,
    final boolean includeConnectionInitializer)
  {
    BeanDefinitionBuilder pool = builder;
    if (pool == null) {
      pool = BeanDefinitionBuilder.genericBeanDefinition(BlockingConnectionPool.class);
    }
    pool.addPropertyValue("name", name);
    final BeanDefinitionBuilder factory = BeanDefinitionBuilder.genericBeanDefinition(DefaultConnectionFactory.class);
    factory.addPropertyValue(
      "connectionConfig",
      parseConnectionConfig(null, element, includeConnectionInitializer).getBeanDefinition());
    if (element.hasAttribute("provider")) {
      factory.addPropertyValue("provider", parseProvider(element).getBeanDefinition());
    }
    pool.addPropertyValue("connectionFactory", factory.getBeanDefinition());
    pool.addPropertyValue("poolConfig", parsePoolConfig(null, element).getBeanDefinition());
    pool.addPropertyValue("blockWaitTime", element.getAttribute("blockWaitTime"));
    pool.addPropertyValue("failFastInitialize", element.getAttribute("failFastInitialize"));
    final BeanDefinitionBuilder pruneStrategy = BeanDefinitionBuilder.genericBeanDefinition(IdlePruneStrategy.class);
    pruneStrategy.addConstructorArgValue(element.getAttribute("prunePeriod"));
    pruneStrategy.addConstructorArgValue(element.getAttribute("idleTime"));
    pool.addPropertyValue("pruneStrategy", pruneStrategy.getBeanDefinition());
    pool.addPropertyValue("validator", new SearchValidator());
    if (element.hasAttribute("ldapUrl")) {
      pool.setInitMethodName("initialize");
    } else {
      logger.info("No ldapUrl attribute found for element {}, pool not initialized.", name);
    }
    return pool;
  }


  /**
   * Creates a pool config.
   *
   * @param  builder  bean definition builder to set properties on, may be null
   * @param  element  containing configuration
   *
   * @return  pool config bean definition
   */
  protected BeanDefinitionBuilder parsePoolConfig(final BeanDefinitionBuilder builder, final Element element)
  {
    BeanDefinitionBuilder poolConfig = builder;
    if (poolConfig == null) {
      poolConfig = BeanDefinitionBuilder.genericBeanDefinition(PoolConfig.class);
    }
    poolConfig.addPropertyValue("minPoolSize", element.getAttribute("minPoolSize"));
    poolConfig.addPropertyValue("maxPoolSize", element.getAttribute("maxPoolSize"));
    poolConfig.addPropertyValue("validateOnCheckOut", element.getAttribute("validateOnCheckOut"));
    poolConfig.addPropertyValue("validatePeriodically", element.getAttribute("validatePeriodically"));
    poolConfig.addPropertyValue("validatePeriod", element.getAttribute("validatePeriod"));
    return poolConfig;
  }
}
