package org.ldaptive.beans.spring.parser;

import org.ldaptive.sasl.CramMd5Config;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

/**
 * @author tduehr
 */
public class Crammd5SearchAuthenticatorBeanDefinition extends SaslSearchAuthenticatorBeanDefinition {
    @Override
    protected BeanDefinitionBuilder parseSaslConfig(final Element element){
        BeanDefinitionBuilder saslConfig = BeanDefinitionBuilder.genericBeanDefinition(CramMd5Config.class);
        setIfPresent(element, null, super.ATTRIBUTES, saslConfig);
        return saslConfig;
    }
}
