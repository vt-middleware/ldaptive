package org.ldaptive.beans.spring.parser;

import org.ldaptive.auth.ext.FreeIPAAuthenticationResponseHandler;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * @author tduehr
 */
public class FreeIPAAuthenticationResponseHandlerBeanDefinition extends AbstractSimpleBeanDefinitionParser {
    protected Class getBeanClass(Element element) {
        return FreeIPAAuthenticationResponseHandler.class;
    }
}
