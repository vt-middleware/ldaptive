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
 * Common implementation for parsers that extend connection-config.
 *
 * @author Middleware Services
 */
public abstract class AbstractConnectionConfigBeanDefinitionParser extends AbstractBeanDefinitionParser
{


  /**
   * Creates a connection config. If a bindDn and bindCredential are present, a {@link BindConnectionInitializer} is
   * inserted into the connection config.
   *
   * @param  builder  bean definition builder to set properties on, may be null
   * @param  element  containing configuration
   * @param  includeConnectionInitializer  whether to include a connection initializer
   *
   * @return  connection config bean definition
   */
  protected BeanDefinitionBuilder parseConnectionConfig(
    final BeanDefinitionBuilder builder,
    final Element element,
    final boolean includeConnectionInitializer)
  {
    BeanDefinitionBuilder connectionConfig = builder;
    if (connectionConfig == null) {
      connectionConfig = BeanDefinitionBuilder.genericBeanDefinition(ConnectionConfig.class);
    }
    setIfPresent(element, "ldapUrl", connectionConfig);
    connectionConfig.addPropertyValue("useStartTLS", element.getAttribute("useStartTLS"));
    connectionConfig.addPropertyValue("useSSL", element.getAttribute("useSSL"));
    final BeanDefinitionBuilder connectTimeout =  BeanDefinitionBuilder.rootBeanDefinition(
      AbstractAuthenticatorBeanDefinitionParser.class,
      "parseDuration");
    connectTimeout.addConstructorArgValue(element.getAttribute("connectTimeout"));
    connectionConfig.addPropertyValue("connectTimeout", connectTimeout.getBeanDefinition());

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
        parseConnectionInitializer(null, element).getBeanDefinition());
    }
    return connectionConfig;
  }


  /**
   * Creates a bind connection initializer.
   *
   * @param  builder  bean definition builder to set properties on, may be null
   * @param  element  containing configuration
   *
   * @return  bind connection initializer bean definition
   */
  protected BeanDefinitionBuilder parseConnectionInitializer(final BeanDefinitionBuilder builder, final Element element)
  {
    BeanDefinitionBuilder initializer = builder;
    if (initializer == null) {
      initializer = BeanDefinitionBuilder.genericBeanDefinition(BindConnectionInitializer.class);
    }
    initializer.addPropertyValue("bindDn", element.getAttribute("bindDn"));
    setIfPresent(element, "bindCredential", initializer);
    return initializer;
  }
}
