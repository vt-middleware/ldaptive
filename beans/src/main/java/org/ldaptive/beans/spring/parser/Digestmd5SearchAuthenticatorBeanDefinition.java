package org.ldaptive.beans.spring.parser;

import org.ldaptive.sasl.DigestMd5Config;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

/**
 * @author tduehr
 */
public class Digestmd5SearchAuthenticatorBeanDefinition extends SaslSearchAuthenticatorBeanDefinition {
    @Override
    protected BeanDefinitionBuilder parseSaslConfig(final Element element){
        BeanDefinitionBuilder saslConfig = BeanDefinitionBuilder.genericBeanDefinition(DigestMd5Config.class);
        saslConfig.addConstructorArgValue(super.parseSaslConfig(element).getBeanDefinition());
        saslConfig.addPropertyValue("realm", element.getAttribute("saslRealm"));
        return saslConfig;
    }
}
