package org.ldaptive.beans.spring.parser;

import org.ldaptive.auth.PooledSearchDnResolver;
import org.ldaptive.pool.PooledConnectionFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Common implementation for search based authenticators.
 */
public abstract class AbstractSearchAuthenticatorBeanDefinition extends AbstractAuthenticatorBeanDefinition {
  @Override
  protected void doParse(final Element element, final ParserContext context, final BeanDefinitionBuilder builder) {
    final BeanDefinition pool = parseConnectionPool("search-pool", element);
    final BeanDefinitionBuilder connectionFactory = BeanDefinitionBuilder.genericBeanDefinition(PooledConnectionFactory.class);
    connectionFactory.addPropertyValue("connectionPool", pool);

    final BeanDefinitionBuilder resolver = BeanDefinitionBuilder.genericBeanDefinition(PooledSearchDnResolver.class);
    resolver.addPropertyValue("baseDn", element.getAttribute("baseDn"));
    resolver.addPropertyValue("subtreeSearch", element.getAttribute("subtreeSearch"));
    resolver.addPropertyValue("userFilter", element.getAttribute("userFilter"));
    resolver.addPropertyValue("allowMultipleDns", element.getAttribute("allowMultipleDns"));
    resolver.addPropertyValue("connectionFactory", connectionFactory.getBeanDefinition());

    final BeanDefinitionBuilder authHandler = parseAuthHandler(element);
    if (element.hasAttribute("usePasswordPolicy")) {
      final BeanDefinitionBuilder responseHandler = BeanDefinitionBuilder.rootBeanDefinition(
        AbstractAuthenticatorBeanDefinition.class,
        "parsePasswordPolicyAuthenticationResponseHandler");
      responseHandler.addConstructorArgValue(element.getAttribute("usePasswordPolicy"));
      builder.addPropertyValue("authenticationResponseHandlers", responseHandler.getBeanDefinition());
    } else if (element.hasAttribute("authenticationResponseHandlers")) {
      builder.addPropertyReference("authenticationResponseHandlers", element.getAttribute("authenticationResponseHandlers"));
    }
    builder.addConstructorArgValue(resolver.getBeanDefinition());
    builder.addConstructorArgValue(authHandler.getBeanDefinition());
  }
}
