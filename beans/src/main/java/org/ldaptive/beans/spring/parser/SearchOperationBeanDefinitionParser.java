/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring.parser;

import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.filter.Filter;
import org.ldaptive.filter.FilterParseException;
import org.ldaptive.filter.FilterParser;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser for <pre>search-operation</pre> elements.
 *
 * @author Middleware Services
 */
public class SearchOperationBeanDefinitionParser extends AbstractBeanDefinitionParser
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
    return StringUtils.hasText(idAttrValue) ? idAttrValue : "search-operation";
  }


  @Override
  protected Class<?> getBeanClass(final Element element)
  {
    return SearchOperation.class;
  }


  @Override
  protected void doParse(
    final Element element,
    final ParserContext context,
    final BeanDefinitionBuilder builder)
  {
    builder.addPropertyValue("request", parseSearchRequest(null, element).getBeanDefinition());
  }


  /**
   * Creates a search request.
   *
   * @param  builder  bean definition builder to set properties on, may be null
   * @param  element  containing configuration
   *
   * @return  search request bean definition builder
   */
  protected BeanDefinitionBuilder parseSearchRequest(final BeanDefinitionBuilder builder, final Element element)
  {
    BeanDefinitionBuilder searchRequest = builder;
    if (searchRequest == null) {
      searchRequest = BeanDefinitionBuilder.genericBeanDefinition(SearchRequest.class);
    }
    searchRequest.addPropertyValue("baseDn", element.getAttribute("baseDn"));
    searchRequest.addPropertyValue("searchScope", element.getAttribute("searchScope"));
    searchRequest.addPropertyValue("derefAliases", element.getAttribute("derefAliases"));
    setIfPresent(element, "sizeLimit", searchRequest);
    if (element.hasAttribute("timeLimit")) {
      final BeanDefinitionBuilder timeLimit =  BeanDefinitionBuilder.rootBeanDefinition(
        AbstractBeanDefinitionParser.class,
        "parseDuration");
      timeLimit.addConstructorArgValue(element.getAttribute("timeLimit"));
      searchRequest.addPropertyValue("timeLimit", timeLimit.getBeanDefinition());
    }
    setIfPresent(element, "typesOnly", searchRequest);
    if (element.hasAttribute("filter")) {
      final BeanDefinitionBuilder filter =  BeanDefinitionBuilder.rootBeanDefinition(
        SearchOperationBeanDefinitionParser.class,
        "parseFilter");
      filter.addConstructorArgValue(element.getAttribute("filter"));
      searchRequest.addPropertyValue("filter", filter.getBeanDefinition());
    }
    setIfPresent(element, "returnAttributes", searchRequest);
    setIfPresent(element, "binaryAttributes", searchRequest);
    return searchRequest;
  }


  /**
   * Returns a {@link Filter} for the supplied value.
   *
   * @param  value  to parse
   *
   * @return  period
   */
  protected static Filter parseFilter(final String value)
  {
    try {
      return FilterParser.parse(value);
    } catch (FilterParseException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
