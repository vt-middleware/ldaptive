/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring.parser;

import org.ldaptive.pool.BlockingConnectionPool;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser for <pre>connection-pool</pre> elements.
 *
 * @author Middleware Services
 */
public class ConnectionPoolBeanDefinitionParser extends AbstractConnectionPoolBeanDefinitionParser
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
    return StringUtils.hasText(idAttrValue) ? idAttrValue : "connection-pool";
  }


  @Override
  protected Class<?> getBeanClass(final Element element)
  {
    return BlockingConnectionPool.class;
  }


  @Override
  protected void doParse(
    final Element element, final ParserContext context, final BeanDefinitionBuilder builder)
  {
    String name = "connection-pool";
    if (element.hasAttribute("id")) {
      name = element.getAttribute("id") + "-connection-pool";
    }
    parseConnectionPool(builder, name, element, true).getBeanDefinition();
  }
}
