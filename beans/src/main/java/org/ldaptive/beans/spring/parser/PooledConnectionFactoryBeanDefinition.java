package org.ldaptive.beans.spring.parser;

import org.ldaptive.pool.PooledConnectionFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser for <pre>pooled-connection-factory</pre> elements.
 */
public class PooledConnectionFactoryBeanDefinition extends AbstractPooledConnectionFactoryBeanDefinition
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
        return StringUtils.hasText(idAttrValue) ? idAttrValue : "pooled-connection-factory";
    }


    @Override
    protected Class<?> getBeanClass(final Element element)
    {
        return PooledConnectionFactory.class;
    }


    @Override
    protected void doParse(
            final Element element,
            final ParserContext context,
            final BeanDefinitionBuilder builder)
    {
        builder.addPropertyValue("connectionPool", parseConnectionPool("connection-pool", element));
    }
}
