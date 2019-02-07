/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring.parser;

import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.pool.SearchValidator;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

/**
 * Common implementation for all connection factories.
 *
 * @author Middleware Services
 */
public abstract class AbstractConnectionFactoryBeanDefinitionParser extends AbstractConnectionConfigBeanDefinitionParser
{


  /**
   * Creates a default connection factory.
   *
   * @param  builder  bean definition builder to set properties on, may be null
   * @param  element  containing configuration
   * @param  includeConnectionInitializer  whether to include a connection initializer
   *
   * @return  default connection factory bean definition builder
   */
  protected BeanDefinitionBuilder parseDefaultConnectionFactory(
    final BeanDefinitionBuilder builder,
    final Element element,
    final boolean includeConnectionInitializer)
  {
    BeanDefinitionBuilder factory = builder;
    if (factory == null) {
      factory = BeanDefinitionBuilder.genericBeanDefinition(DefaultConnectionFactory.class);
    }
    factory.addPropertyValue(
      "connectionConfig",
      parseConnectionConfig(null, element, includeConnectionInitializer).getBeanDefinition());
    return factory;
  }


  /**
   * Creates a pooled connection factory.
   *
   * @param  builder  bean definition builder to set properties on, may be null
   * @param  name  of the connection pool
   * @param  element  containing configuration
   * @param  includeConnectionInitializer  whether to include a connection initializer
   *
   * @return  pooled connection factory bean definition builder
   */
  protected BeanDefinitionBuilder parsePooledConnectionFactory(
    final BeanDefinitionBuilder builder,
    final String name,
    final Element element,
    final boolean includeConnectionInitializer)
  {
    BeanDefinitionBuilder pool = builder;
    if (pool == null) {
      pool = BeanDefinitionBuilder.genericBeanDefinition(PooledConnectionFactory.class);
    }
    pool.addPropertyValue("name", name);
    pool.addPropertyValue(
      "defaultConnectionFactory",
      parseDefaultConnectionFactory(null, element, includeConnectionInitializer).getBeanDefinition());
    pool.addPropertyValue("poolConfig", parsePoolConfig(null, element).getBeanDefinition());

    final BeanDefinitionBuilder blockWaitTime =  BeanDefinitionBuilder.rootBeanDefinition(
      AbstractAuthenticatorBeanDefinitionParser.class,
      "parseDuration");
    blockWaitTime.addConstructorArgValue(element.getAttribute("blockWaitTime"));
    pool.addPropertyValue("blockWaitTime", blockWaitTime.getBeanDefinition());


    pool.addPropertyValue("failFastInitialize", element.getAttribute("failFastInitialize"));
    final BeanDefinitionBuilder pruneStrategy = BeanDefinitionBuilder.genericBeanDefinition(IdlePruneStrategy.class);
    final BeanDefinitionBuilder prunePeriod =  BeanDefinitionBuilder.rootBeanDefinition(
      AbstractAuthenticatorBeanDefinitionParser.class,
      "parseDuration");
    prunePeriod.addConstructorArgValue(element.getAttribute("prunePeriod"));
    final BeanDefinitionBuilder idleTime =  BeanDefinitionBuilder.rootBeanDefinition(
      AbstractAuthenticatorBeanDefinitionParser.class,
      "parseDuration");
    idleTime.addConstructorArgValue(element.getAttribute("idleTime"));
    pruneStrategy.addPropertyValue("prunePeriod", prunePeriod.getBeanDefinition());
    pruneStrategy.addPropertyValue("idleTime", idleTime.getBeanDefinition());
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
   * @return  pool config bean definition builder
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
    final BeanDefinitionBuilder validatePeriod =  BeanDefinitionBuilder.rootBeanDefinition(
      AbstractAuthenticatorBeanDefinitionParser.class,
      "parseDuration");
    validatePeriod.addConstructorArgValue(element.getAttribute("validatePeriod"));
    poolConfig.addPropertyValue("validatePeriod", validatePeriod.getBeanDefinition());
    return poolConfig;
  }
}
