/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring.parser;

import org.ldaptive.auth.SearchDnResolver;
import org.ldaptive.auth.SearchEntryResolver;
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

    final BeanDefinitionBuilder connectionFactory;
    final BeanDefinitionBuilder dnResolver;
    if (element.getAttribute("disablePooling") != null && Boolean.valueOf(element.getAttribute("disablePooling"))) {
      connectionFactory = parseDefaultConnectionFactory(null, element, true);
      dnResolver = parseDnResolver(
        BeanDefinitionBuilder.genericBeanDefinition(SearchDnResolver.class),
        element,
        connectionFactory);
    } else {
      connectionFactory = parsePooledConnectionFactory(null, name, element, true);
      dnResolver = parseDnResolver(null, element, connectionFactory);
    }

    final BeanDefinitionBuilder authHandler = parseAuthHandler(element);
    final BeanDefinitionBuilder authResponseHandler = parseAuthResponseHandler(builder, authHandler, element);
    if (authResponseHandler != null) {
      builder.addPropertyValue("responseHandlers", authResponseHandler.getBeanDefinition());
    }

    builder.addConstructorArgValue(dnResolver.getBeanDefinition());
    builder.addConstructorArgValue(authHandler.getBeanDefinition());

    final BeanDefinitionBuilder entryResolver = parseEntryResolver(element, connectionFactory);
    builder.addPropertyValue("entryResolver", entryResolver.getBeanDefinition());

    setIfPresent(element, "returnAttributes", builder);
    builder.addPropertyValue("resolveEntryOnFailure", element.getAttribute("resolveEntryOnFailure"));
  }


  /**
   * Creates a DN resolver.
   *
   * @param  builder  bean definition builder to set properties on, may be null
   * @param  element  containing configuration
   * @param  connectionFactory  to use for DN resolution
   *
   * @return  pooled search dn resolver bean definition builder
   */
  protected BeanDefinitionBuilder parseDnResolver(
    final BeanDefinitionBuilder builder,
    final Element element,
    final BeanDefinitionBuilder connectionFactory)
  {
    BeanDefinitionBuilder dnResolver = builder;
    if (dnResolver == null) {
      dnResolver = BeanDefinitionBuilder.genericBeanDefinition(SearchDnResolver.class);
    }
    dnResolver.addPropertyValue("baseDn", element.getAttribute("baseDn"));
    dnResolver.addPropertyValue("subtreeSearch", element.getAttribute("subtreeSearch"));
    dnResolver.addPropertyValue("userFilter", element.getAttribute("userFilter"));
    dnResolver.addPropertyValue("allowMultipleDns", element.getAttribute("allowMultipleDns"));
    dnResolver.addPropertyValue("connectionFactory", connectionFactory.getBeanDefinition());
    return dnResolver;
  }


  /**
   * Creates an entry resolver.
   *
   * @param  element  containing configuration
   * @param  connectionFactory  that was used for DN resolution
   *
   * @return  search entry resolver bean definition builder
   */
  protected BeanDefinitionBuilder parseEntryResolver(
    final Element element,
    final BeanDefinitionBuilder connectionFactory)
  {
    final BeanDefinitionBuilder entryResolver = BeanDefinitionBuilder.genericBeanDefinition(SearchEntryResolver.class);
    entryResolver.addPropertyValue("connectionFactory", connectionFactory.getBeanDefinition());
    return entryResolver;
  }
}
