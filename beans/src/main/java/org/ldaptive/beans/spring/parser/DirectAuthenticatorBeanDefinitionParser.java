/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring.parser;

import org.ldaptive.auth.FormatDnResolver;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser for <pre>direct-authenticator</pre> elements.
 *
 * @author Middleware Services
 */
public class DirectAuthenticatorBeanDefinitionParser extends AbstractAuthenticatorBeanDefinitionParser
{


  @Override
  protected String resolveId(
    final Element element,
    // CheckStyle:IllegalTypeCheck OFF
    final AbstractBeanDefinition definition,
    // CheckStyle:IllegalTypeCheck ON
    final ParserContext parserContext)
    throws BeanDefinitionStoreException
  {
    final String idAttrValue = element.getAttribute("id");
    return StringUtils.hasText(idAttrValue) ? idAttrValue : "direct-authenticator";
  }


  @Override
  protected void doParse(
    final Element element, final ParserContext context, final BeanDefinitionBuilder builder)
  {
    final BeanDefinitionBuilder authHandler = parseAuthHandler(element);
    final BeanDefinitionBuilder authResponseHandler = parseAuthResponseHandler(builder, authHandler, element);
    if (authResponseHandler != null) {
      builder.addPropertyValue("responseHandlers", authResponseHandler.getBeanDefinition());
    }
    final BeanDefinitionBuilder dnResolver = BeanDefinitionBuilder.genericBeanDefinition(FormatDnResolver.class);
    dnResolver.addConstructorArgValue(element.getAttribute("format"));
    builder.addConstructorArgValue(dnResolver.getBeanDefinition());
    builder.addConstructorArgValue(authHandler.getBeanDefinition());
  }
}
