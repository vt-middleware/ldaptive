package org.ldaptive.beans.spring.parser;

import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.pool.SearchValidator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

/**
 * Common implementation for all pooled connection factories.
 */
public abstract class AbstractPooledConnectionFactoryBeanDefinition extends AbstractConnectionFactoryBeanDefinition
{


    /**
     * Creates a blocking connection pool.
     *
     * @param  name  of the connection pool
     * @param  element  containing configuration
     *
     * @return  blocking connection pool bean definition
     */
    protected BeanDefinition parseConnectionPool(final String name, final Element element)
    {
        final BeanDefinitionBuilder pool = BeanDefinitionBuilder.genericBeanDefinition(BlockingConnectionPool.class);
        pool.addPropertyValue("name", name);
        final BeanDefinitionBuilder factory = BeanDefinitionBuilder.genericBeanDefinition(DefaultConnectionFactory.class);
        factory.addPropertyValue("connectionConfig", parseConnectionConfig(element));
        if (element.hasAttribute("provider")) {
            factory.addPropertyValue("provider", parseProvider(element));
        }
        pool.addPropertyValue("connectionFactory", factory.getBeanDefinition());
        pool.addPropertyValue("poolConfig", parsePoolConfig(element));
        pool.addPropertyValue("blockWaitTime", element.getAttribute("blockWaitTime"));
        pool.addPropertyValue("failFastInitialize", element.getAttribute("failFastInitialize"));
        final BeanDefinitionBuilder pruneStrategy = BeanDefinitionBuilder.genericBeanDefinition(IdlePruneStrategy.class);
        pruneStrategy.addConstructorArgValue(element.getAttribute("prunePeriod"));
        pruneStrategy.addConstructorArgValue(element.getAttribute("idleTime"));
        pool.addPropertyValue("pruneStrategy", pruneStrategy.getBeanDefinition());
        pool.addPropertyValue("validator", new SearchValidator());
        pool.setInitMethodName("initialize");
        return pool.getBeanDefinition();
    }


    /**
     * Creates a pool config.
     *
     * @param  element  containing configuration
     *
     * @return  pool config bean definition
     */
    protected BeanDefinition parsePoolConfig(final Element element)
    {
        final BeanDefinitionBuilder poolConfig = BeanDefinitionBuilder.genericBeanDefinition(PoolConfig.class);
        poolConfig.addPropertyValue("minPoolSize", element.getAttribute("minPoolSize"));
        poolConfig.addPropertyValue("maxPoolSize", element.getAttribute("maxPoolSize"));
        poolConfig.addPropertyValue("validateOnCheckOut", element.getAttribute("validateOnCheckOut"));
        poolConfig.addPropertyValue("validatePeriodically", element.getAttribute("validatePeriodically"));
        poolConfig.addPropertyValue("validatePeriod", element.getAttribute("validatePeriod"));
        return poolConfig.getBeanDefinition();
    }
}
