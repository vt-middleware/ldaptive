package org.ldaptive.beans.spring.parser;

import org.ldaptive.sasl.ExternalConfig;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

/**
 * @author tduehr
 */
public class ExternalSearchAuthenticatorBeanDefinition extends SaslSearchAuthenticatorBeanDefinition {
    @Override
    protected BeanDefinitionBuilder parseSaslConfig(final Element element){
      BeanDefinitionBuilder saslConfig = BeanDefinitionBuilder.genericBeanDefinition(ExternalConfig.class);
      setIfPresent(element, null, super.ATTRIBUTES, saslConfig);
      return saslConfig;
    }
}
