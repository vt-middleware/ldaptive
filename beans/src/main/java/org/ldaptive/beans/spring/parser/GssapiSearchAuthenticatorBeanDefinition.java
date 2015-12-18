package org.ldaptive.beans.spring.parser;

import org.ldaptive.sasl.GssApiConfig;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

/**
 * @author tduehr
 */
public class GssapiSearchAuthenticatorBeanDefinition extends SaslSearchAuthenticatorBeanDefinition {
    @Override
    protected BeanDefinitionBuilder parseSaslConfig(final Element element){
        BeanDefinitionBuilder saslConfig = BeanDefinitionBuilder.genericBeanDefinition(GssApiConfig.class);
        setIfPresent(element, null, super.ATTRIBUTES, saslConfig);
        saslConfig.addPropertyValue("realm", element.getAttribute("saslRealm"));
        return saslConfig;
    }
}
