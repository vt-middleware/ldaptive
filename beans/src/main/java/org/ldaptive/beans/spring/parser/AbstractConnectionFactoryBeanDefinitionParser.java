/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring.parser;

import org.ldaptive.DefaultConnectionFactory;
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
    if (element.hasAttribute("provider")) {
      factory.addPropertyValue("provider", parseProvider(element).getBeanDefinition());
    }
    return factory;
  }


  /**
   * Creates a provider.
   *
   * @param  element  containing configuration
   *
   * @return  provider bean definition builder
   */
  protected BeanDefinitionBuilder parseProvider(final Element element)
  {
    return BeanDefinitionBuilder.genericBeanDefinition(element.getAttribute("provider"));
  }
}
