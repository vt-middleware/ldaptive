package org.ldaptive.beans.spring.parser;

import org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * @author tduehr
 */
public class ActiveDirectoryAuthenticationResponseHandlerBeanDefinition extends AbstractSimpleBeanDefinitionParser {
  protected Class getBeanClass(Element element) {
    return ActiveDirectoryAuthenticationResponseHandler.class;
  }
}
