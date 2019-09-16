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
   * @return  connection config bean definition builder
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
    final BeanDefinitionBuilder connectTimeout =  BeanDefinitionBuilder.rootBeanDefinition(
      AbstractBeanDefinitionParser.class,
      "parseDuration");
    connectTimeout.addConstructorArgValue(element.getAttribute("connectTimeout"));
    connectionConfig.addPropertyValue("connectTimeout", connectTimeout.getBeanDefinition());
    final BeanDefinitionBuilder responseTimeout =  BeanDefinitionBuilder.rootBeanDefinition(
      AbstractBeanDefinitionParser.class,
      "parseDuration");
    responseTimeout.addConstructorArgValue(element.getAttribute("responseTimeout"));
    connectionConfig.addPropertyValue("responseTimeout", responseTimeout.getBeanDefinition());
    connectionConfig.addPropertyValue("useStartTLS", element.getAttribute("useStartTLS"));

    if (element.hasAttribute("trustCertificates") || element.hasAttribute("authenticationCertificate")) {
      final BeanDefinitionBuilder sslConfig = BeanDefinitionBuilder.genericBeanDefinition(SslConfig.class);
      sslConfig.addPropertyValue("credentialConfig", parseX509CredentialConfig(null, element).getBeanDefinition());
      connectionConfig.addPropertyValue("sslConfig", sslConfig.getBeanDefinition());
    } else if (element.hasAttribute("trustStore") || element.hasAttribute("keyStore")) {
      final BeanDefinitionBuilder sslConfig = BeanDefinitionBuilder.genericBeanDefinition(SslConfig.class);
      sslConfig.addPropertyValue("credentialConfig", parseKeyStoreCredentialConfig(null, element).getBeanDefinition());
      connectionConfig.addPropertyValue("sslConfig", sslConfig.getBeanDefinition());
    }

    if (includeConnectionInitializer && element.hasAttribute("bindDn")) {
      connectionConfig.addPropertyValue(
        "connectionInitializers",
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
   * @return  bind connection initializer bean definition builder
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


  /**
   * Creates a X509 credential config.
   *
   * @param  builder  bean definition builder to set properties on, may be null
   * @param  element  containing configuration
   *
   * @return  x509 credential config bean definition builder
   */
  protected BeanDefinitionBuilder parseX509CredentialConfig(final BeanDefinitionBuilder builder, final Element element)
  {
    BeanDefinitionBuilder credentialConfig = builder;
    if (credentialConfig == null) {
      credentialConfig = BeanDefinitionBuilder.genericBeanDefinition(X509CredentialConfig.class);
    }
    setIfPresent(element, "trustCertificates", credentialConfig);
    setIfPresent(element, "authenticationCertificate", credentialConfig);
    setIfPresent(element, "authenticationKey", credentialConfig);
    return credentialConfig;
  }


  /**
   * Creates a keystore credential config.
   *
   * @param  builder  bean definition builder to set properties on, may be null
   * @param  element  containing configuration
   *
   * @return  keystore credential config bean definition builder
   */
  protected BeanDefinitionBuilder parseKeyStoreCredentialConfig(
    final BeanDefinitionBuilder builder,
    final Element element)
  {
    BeanDefinitionBuilder credentialConfig = builder;
    if (credentialConfig == null) {
      credentialConfig = BeanDefinitionBuilder.genericBeanDefinition(KeyStoreCredentialConfig.class);
    }
    setIfPresent(element, "trustStore", credentialConfig);
    setIfPresent(element, "trustStorePassword", credentialConfig);
    setIfPresent(element, "trustStoreType", credentialConfig);
    setIfPresent(element, "trustStoreAliases", credentialConfig);
    setIfPresent(element, "keyStore", credentialConfig);
    setIfPresent(element, "keyStorePassword", credentialConfig);
    setIfPresent(element, "keyStoreType", credentialConfig);
    setIfPresent(element, "keyStoreAliases", credentialConfig);
    return credentialConfig;
  }
}
