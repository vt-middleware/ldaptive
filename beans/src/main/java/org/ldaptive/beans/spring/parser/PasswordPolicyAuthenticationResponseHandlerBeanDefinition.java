package org.ldaptive.beans.spring.parser;

import org.ldaptive.auth.ext.PasswordPolicyAuthenticationResponseHandler;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * @author tduehr
 */
public class PasswordPolicyAuthenticationResponseHandlerBeanDefinition extends AbstractSimpleBeanDefinitionParser {
    protected Class getBeanClass(Element element) {
        return PasswordPolicyAuthenticationResponseHandler.class;
    }
}
