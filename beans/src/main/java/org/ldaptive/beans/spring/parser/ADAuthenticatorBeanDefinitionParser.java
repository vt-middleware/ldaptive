/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring.parser;

import org.ldaptive.ad.handler.ObjectGuidHandler;
import org.ldaptive.ad.handler.ObjectSidHandler;
import org.ldaptive.auth.PooledSearchEntryResolver;
import org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler;
import org.ldaptive.handler.SearchEntryHandler;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser for <pre>ad-authenticator</pre> elements.
 *
 * @author Middleware Services
 */
public class ADAuthenticatorBeanDefinitionParser extends AbstractSearchAuthenticatorBeanDefinitionParser
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
    return StringUtils.hasText(idAttrValue) ? idAttrValue : "ad-authenticator";
  }


  @Override
  protected void doParse(
    final Element element,
    final ParserContext context,
    final BeanDefinitionBuilder builder)
  {
    super.doParse(element, context, builder);
    builder.addPropertyValue("authenticationResponseHandlers", new ActiveDirectoryAuthenticationResponseHandler());
  }


  @Override
  protected BeanDefinitionBuilder parseEntryResolver(
    final Element element,
    final BeanDefinitionBuilder connectionFactory)
  {
    BeanDefinitionBuilder entryResolver;
    if (element.hasAttribute("resolveEntryWithBindCredentials") &&
        Boolean.valueOf(element.getAttribute("resolveEntryWithBindCredentials"))) {
      entryResolver = BeanDefinitionBuilder.genericBeanDefinition(PooledSearchEntryResolver.class);
      entryResolver.addPropertyValue("connectionFactory", connectionFactory.getBeanDefinition());
    } else {
      entryResolver = super.parseEntryResolver(element, connectionFactory);
    }
    entryResolver.addPropertyValue(
      "searchEntryHandlers",
      new SearchEntryHandler[]{new ObjectGuidHandler(), new ObjectSidHandler()});
    return entryResolver;
  }
}
