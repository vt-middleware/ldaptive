/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring.parser;

import org.ldaptive.sasl.CramMd5Config;
import org.ldaptive.sasl.DigestMd5Config;
import org.ldaptive.sasl.ExternalConfig;
import org.ldaptive.sasl.GssApiConfig;
import org.ldaptive.sasl.Mechanism;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser for <pre>sasl-bind-search-authenticator</pre> elements.
 *
 * @author Middleware Services
 */
public class SaslBindSearchAuthenticatorBeanDefinitionParser extends AbstractSearchAuthenticatorBeanDefinitionParser
{


  @Override
  protected String resolveId(
    final Element element,
    // CheckStyle:IllegalTypeCheck OFF
    final AbstractBeanDefinition definition,
    // CheckStyle:IllegalTypeCheck ON
    final ParserContext parserContext) throws BeanDefinitionStoreException
  {
    final String idAttrValue = element.getAttribute("id");
    return StringUtils.hasText(idAttrValue) ? idAttrValue : "sasl-bind-search-authenticator";
  }


  /**
   * Creates a bind connection initializer.
   *
   * @param  builder  bean definition builder to set properties on, may be null
   * @param  element  containing configuration
   *
   * @return  bind connection initializer bean definition
   */
  @Override
  protected BeanDefinitionBuilder parseConnectionInitializer(final BeanDefinitionBuilder builder, final Element element)
  {
    final BeanDefinitionBuilder initializer = super.parseConnectionInitializer(builder, element);
    initializer.addPropertyValue("bindSaslConfig", parseSaslConfig(element).getBeanDefinition());
    return initializer;
  }


  /**
   * Creates a sasl config.
   *
   * @param  element  containing configuration
   *
   * @return  sasl config bean definition builder
   */
  protected BeanDefinitionBuilder parseSaslConfig(final Element element)
  {
    final Mechanism mechanism = Mechanism.valueOf(element.getAttribute("mechanism"));
    BeanDefinitionBuilder saslConfig;
    switch (mechanism) {

    case DIGEST_MD5:
      saslConfig = BeanDefinitionBuilder.genericBeanDefinition(DigestMd5Config.class);
      setIfPresent(element, "realm", saslConfig);
      break;

    case CRAM_MD5:
      saslConfig = BeanDefinitionBuilder.genericBeanDefinition(CramMd5Config.class);
      break;

    case EXTERNAL:
      saslConfig = BeanDefinitionBuilder.genericBeanDefinition(ExternalConfig.class);
      break;

    case GSSAPI:
      saslConfig = BeanDefinitionBuilder.genericBeanDefinition(GssApiConfig.class);
      setIfPresent(element, "realm", saslConfig);
      break;

    default:
      throw new IllegalArgumentException("Unknown SASL mechanism " + mechanism);
    }

    setIfPresent(element, "authorizationId", saslConfig);
    setIfPresent(element, "mutualAuthentication", saslConfig);
    setIfPresent(element, "qualityOfProtection", saslConfig);
    setIfPresent(element, "securityStrength", saslConfig);
    return saslConfig;
  }
}
