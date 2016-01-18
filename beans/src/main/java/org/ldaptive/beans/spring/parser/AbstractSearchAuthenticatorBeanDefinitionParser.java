/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring.parser;

import org.ldaptive.auth.PooledSearchDnResolver;
import org.ldaptive.pool.PooledConnectionFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Common implementation for search based authenticators.
 *
 * @author Middleware Services
 */
public abstract class AbstractSearchAuthenticatorBeanDefinitionParser extends AbstractAuthenticatorBeanDefinitionParser
{


  @Override
  protected void doParse(final Element element, final ParserContext context, final BeanDefinitionBuilder builder)
  {
    String name = "search-pool";
    if (element.hasAttribute("id")) {
      name = element.getAttribute("id") + "-search-pool";
    }
    final BeanDefinitionBuilder connectionFactory = BeanDefinitionBuilder.genericBeanDefinition(
      PooledConnectionFactory.class);
    connectionFactory.addPropertyValue(
      "connectionPool",
      parseConnectionPool(name, element, true).getBeanDefinition());

    final BeanDefinitionBuilder resolver = BeanDefinitionBuilder.genericBeanDefinition(PooledSearchDnResolver.class);
    resolver.addPropertyValue("baseDn", element.getAttribute("baseDn"));
    resolver.addPropertyValue("subtreeSearch", element.getAttribute("subtreeSearch"));
    resolver.addPropertyValue("userFilter", element.getAttribute("userFilter"));
    resolver.addPropertyValue("allowMultipleDns", element.getAttribute("allowMultipleDns"));
    resolver.addPropertyValue("connectionFactory", connectionFactory.getBeanDefinition());

    final BeanDefinitionBuilder authHandler = parseAuthHandler(element);
    final BeanDefinitionBuilder authResponseHandler = parseAuthResponseHandler(authHandler, element);
    if (authResponseHandler != null) {
      builder.addPropertyValue("authenticationResponseHandlers", authResponseHandler.getBeanDefinition());
    }

    builder.addConstructorArgValue(resolver.getBeanDefinition());
    builder.addConstructorArgValue(authHandler.getBeanDefinition());
  }
}
