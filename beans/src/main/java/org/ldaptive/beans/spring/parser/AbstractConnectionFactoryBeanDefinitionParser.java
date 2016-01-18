/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring.parser;

import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ssl.KeyStoreCredentialConfig;
import org.ldaptive.ssl.SslConfig;
import org.ldaptive.ssl.X509CredentialConfig;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

/**
 * Common implementation for all connection factories.
 *
 * @author Middleware Services
 */
public abstract class AbstractConnectionFactoryBeanDefinitionParser extends AbstractBeanDefinitionParser
{


  /**
   * Creates a connection config. If a bindDn and bindCredential are present, a {@link BindConnectionInitializer} is
   * inserted into the connection config.
   *
   * @param  element  containing configuration
   * @param  includeConnectionInitializer  whether to include a connection initializer
   *
   * @return  connection config bean definition
   */
  protected BeanDefinitionBuilder parseConnectionConfig(
    final Element element,
    final boolean includeConnectionInitializer)
  {
    final BeanDefinitionBuilder connectionConfig = BeanDefinitionBuilder.genericBeanDefinition(ConnectionConfig.class);
    connectionConfig.addPropertyValue("ldapUrl", element.getAttribute("ldapUrl"));
    connectionConfig.addPropertyValue("useStartTLS", element.getAttribute("useStartTLS"));
    connectionConfig.addPropertyValue("useSSL", element.getAttribute("useSSL"));
    connectionConfig.addPropertyValue("connectTimeout", element.getAttribute("connectTimeout"));

    if (element.hasAttribute("trustCertificates")) {
      final BeanDefinitionBuilder credentialConfig = BeanDefinitionBuilder.genericBeanDefinition(
        X509CredentialConfig.class);
      credentialConfig.addPropertyValue("trustCertificates", element.getAttribute("trustCertificates"));
      final BeanDefinitionBuilder sslConfig = BeanDefinitionBuilder.genericBeanDefinition(SslConfig.class);
      sslConfig.addPropertyValue("credentialConfig", credentialConfig.getBeanDefinition());
      connectionConfig.addPropertyValue("sslConfig", sslConfig.getBeanDefinition());
    } else if (element.hasAttribute("trustStore")) {
      final BeanDefinitionBuilder credentialConfig = BeanDefinitionBuilder.genericBeanDefinition(
        KeyStoreCredentialConfig.class);
      credentialConfig.addPropertyValue("trustStore", element.getAttribute("trustStore"));
      credentialConfig.addPropertyValue("trustStorePassword", element.getAttribute("trustStorePassword"));
      credentialConfig.addPropertyValue("trustStoreType", element.getAttribute("trustStoreType"));
      final BeanDefinitionBuilder sslConfig = BeanDefinitionBuilder.genericBeanDefinition(SslConfig.class);
      sslConfig.addPropertyValue("credentialConfig", credentialConfig.getBeanDefinition());
      connectionConfig.addPropertyValue("sslConfig", sslConfig.getBeanDefinition());
    }

    if (includeConnectionInitializer && element.hasAttribute("bindDn")) {
      connectionConfig.addPropertyValue(
        "connectionInitializer",
        parseConnectionInitializer(element).getBeanDefinition());
    }
    return connectionConfig;
  }


  /**
   * Creates a bind connection initializer.
   *
   * @param  element  containing configuration
   *
   * @return  bind connection initializer bean definition
   */
  protected BeanDefinitionBuilder parseConnectionInitializer(final Element element)
  {
    final BeanDefinitionBuilder initializer = BeanDefinitionBuilder.genericBeanDefinition(
      BindConnectionInitializer.class);
    initializer.addPropertyValue("bindDn", element.getAttribute("bindDn"));
    setIfPresent(element, "bindCredential", initializer);
    return initializer;
  }


  /**
   * Creates a provider.
   *
   * @param  element  containing configuration
   * @return  provider bean definition
   */
  protected BeanDefinitionBuilder parseProvider(final Element element)
  {
    final BeanDefinitionBuilder provider = BeanDefinitionBuilder.genericBeanDefinition(
      element.getAttribute("provider"));
    return provider;
  }
}
