package org.ldaptive.beans.spring.parser;

import org.ldaptive.auth.ext.EDirectoryAuthenticationResponseHandler;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * @author tduehr
 */
public class EDirectoryAuthenticationResponseHandlerBeanDefinition extends AbstractSimpleBeanDefinitionParser {
  protected Class getBeanClass(Element element) {
    return EDirectoryAuthenticationResponseHandler.class;
  }
}
