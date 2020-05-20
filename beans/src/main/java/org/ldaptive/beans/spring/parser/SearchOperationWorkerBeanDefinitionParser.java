/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring.parser;

import org.ldaptive.SearchOperation;
import org.ldaptive.concurrent.SearchOperationWorker;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser for <pre>search-operation-worker</pre> elements.
 *
 * @author Middleware Services
 */
public class SearchOperationWorkerBeanDefinitionParser extends SearchOperationBeanDefinitionParser
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
    return StringUtils.hasText(idAttrValue) ? idAttrValue : "search-operation-worker";
  }


  @Override
  protected Class<?> getBeanClass(final Element element)
  {
    return SearchOperationWorker.class;
  }


  @Override
  protected void doParse(
    final Element element,
    final ParserContext context,
    final BeanDefinitionBuilder builder)
  {
    final BeanDefinitionBuilder searchOperation = BeanDefinitionBuilder.genericBeanDefinition(SearchOperation.class);
    searchOperation.addPropertyValue("request", parseSearchRequest(null, element).getBeanDefinition());
    setObjectIfPresent(element, "exceptionHandler", searchOperation);
    setObjectIfPresent(element, "throwCondition", searchOperation);
    builder.addPropertyValue("operation", searchOperation.getBeanDefinition());
  }
}
