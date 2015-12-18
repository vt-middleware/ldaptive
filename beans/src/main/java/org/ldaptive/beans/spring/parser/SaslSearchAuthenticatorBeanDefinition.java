package org.ldaptive.beans.spring.parser;

import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.sasl.Mechanism;
import org.ldaptive.sasl.SaslConfig;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Parser for <pre>sasl-search-authenticator</pre> elements.
 * @author tduehr
 */
public class SaslSearchAuthenticatorBeanDefinition extends AbstractSearchAuthenticatorBeanDefinition {
    protected static final Map<String, String> ATTRIBUTES = new HashMap<>();
    protected static final String MECHANISM = "mechanism";

    static {
        ATTRIBUTES.put("authorizationId", "authorizationId");
        ATTRIBUTES.put("qualityOfProtection", "qualityOfProtection");
        ATTRIBUTES.put("mutualAuthentication", "mutualAuthentication");
        ATTRIBUTES.put("securityStrength", "securityStrength");
    }

    protected BeanDefinitionBuilder parseSaslConfig(final Element element){
        BeanDefinitionBuilder saslConfig = BeanDefinitionBuilder.genericBeanDefinition(SaslConfig.class);
        setIfPresent(element, null, ATTRIBUTES, saslConfig);
        if (element.hasAttribute(MECHANISM)) {
            String mechanism = element.getAttribute(MECHANISM);
            if ("gssapi".equalsIgnoreCase(mechanism)) {
                saslConfig.addPropertyValue(MECHANISM, Mechanism.GSSAPI);
            } else if ("external".equalsIgnoreCase(mechanism)) {
                saslConfig.addPropertyValue(MECHANISM, Mechanism.EXTERNAL);
            } else if ("cram_md5".equalsIgnoreCase(mechanism)) {
                saslConfig.addPropertyValue(MECHANISM, Mechanism.CRAM_MD5);
            } else if ("digest_md5".equalsIgnoreCase(mechanism)) {
                saslConfig.addPropertyValue(MECHANISM, Mechanism.DIGEST_MD5);
            }
        }
        return saslConfig;
    }

    @Override
    protected String resolveId(
        final Element element,
        // CheckStyle:IllegalTypeCheck OFF
        final AbstractBeanDefinition definition,
        // CheckStyle:IllegalTypeCheck ON
        final ParserContext parserContext) throws BeanDefinitionStoreException
    {
        final String idAttrValue = element.getAttribute("id");
        return StringUtils.hasText(idAttrValue) ? idAttrValue : "sasl-search-authenticator";
    }

    @Override
    protected void parseInitializer(final Element element, final BeanDefinitionBuilder connectionConfig){
        final BeanDefinitionBuilder initializer = BeanDefinitionBuilder.genericBeanDefinition(BindConnectionInitializer.class);
        if (element.hasAttribute("searchBindDn"))
        {
            initializer.addPropertyValue("bindDn", element.getAttribute("searchBindDn"));
        } else {
            initializer.addPropertyValue("bindDn", element.getAttribute("bindDn"));
        }
        initializer.addPropertyValue("bindSaslConfig", parseSaslConfig(element).getBeanDefinition());
        connectionConfig.addPropertyValue("connectionInitializer", initializer.getBeanDefinition());
    }
}
