package org.ldaptive.beans.spring.parser;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser for <pre>bind-search-authenticator</pre> elements.
 */
public class BindSearchAuthenticatorBeanDefinition extends AbstractSearchAuthenticatorBeanDefinition
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
        return StringUtils.hasText(idAttrValue) ? idAttrValue : "bind-search-authenticator";
    }
}
