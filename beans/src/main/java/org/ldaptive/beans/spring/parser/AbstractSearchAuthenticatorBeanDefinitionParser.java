/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring.parser;

import org.ldaptive.auth.SearchDnResolver;
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
    final BeanDefinitionBuilder dnResolver;
    if (Boolean.valueOf(element.getAttribute("disablePooling"))) {
      final BeanDefinitionBuilder connectionFactory = parseDefaultConnectionFactory(null, element, true);
      dnResolver = parseDnResolver(null, element, connectionFactory);
    } else {
      final BeanDefinitionBuilder connectionFactory = parsePooledConnectionFactory(
        null,
        element.hasAttribute("id") ? element.getAttribute("id") + "-dn-resolver-pool" : "dn-resolver-pool",
        element,
        true);
      dnResolver = parseDnResolver(null, element, connectionFactory);
    }

    BeanDefinitionBuilder entryResolver = null;
    if (Boolean.valueOf(element.getAttribute("resolveEntryWithBindCredentials"))) {
      if (Boolean.valueOf(element.getAttribute("disablePooling"))) {
        final BeanDefinitionBuilder connectionFactory = parseDefaultConnectionFactory(null, element, true);
        entryResolver = parseEntryResolver(element, connectionFactory);
      } else {
        final BeanDefinitionBuilder connectionFactory = parsePooledConnectionFactory(
          null,
          element.hasAttribute("id") ? element.getAttribute("id") + "-entry-resolver-pool" : "entry-resolver-pool",
          element,
          true);
        entryResolver = parseEntryResolver(element, connectionFactory);
      }
    } else if (element.hasAttribute("binaryAttributes")) {
      // only wire a search entry resolver if binaryAttributes are configured
      entryResolver = parseEntryResolver(element, null);
    }

    final BeanDefinitionBuilder authHandler = parseAuthHandler(element);
    final BeanDefinitionBuilder authResponseHandler = parseAuthResponseHandler(builder, authHandler, element);
    if (authResponseHandler != null) {
      builder.addPropertyValue("responseHandlers", authResponseHandler.getBeanDefinition());
    }

    builder.addConstructorArgValue(dnResolver.getBeanDefinition());
    builder.addConstructorArgValue(authHandler.getBeanDefinition());
    if (entryResolver != null) {
      builder.addPropertyValue("entryResolver", entryResolver.getBeanDefinition());
    }

    setIfPresent(element, "returnAttributes", builder);
    setIfPresent(element, "resolveEntryOnFailure", builder);
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
}
