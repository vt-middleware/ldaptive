package org.ldaptive.beans.spring.parser;

import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ssl.KeyStoreCredentialConfig;
import org.ldaptive.ssl.SslConfig;
import org.ldaptive.ssl.X509CredentialConfig;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * Common implementation for all connection factories.
 */
public abstract class AbstractConnectionFactoryBeanDefinition extends AbstractSingleBeanDefinitionParser {
  /**
   * Creates a connection config. If a bindDn and bindCredential are present, a {@link BindConnectionInitializer} is
   * inserted into the connection config.
   *
   * @param element containing configuration
   * @return connection config bean definition
   */
  protected BeanDefinition parseConnectionConfig(final Element element) {
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

    parseInitializer(element, connectionConfig);
    return connectionConfig.getBeanDefinition();
  }

  protected void parseInitializer(final Element element, final BeanDefinitionBuilder connectionConfig) {
    if (element.hasAttribute("bindDn") && element.hasAttribute("bindCredential")) {
      final BeanDefinitionBuilder initializer = BeanDefinitionBuilder.genericBeanDefinition(
        BindConnectionInitializer.class);
      initializer.addPropertyValue("bindDn", element.getAttribute("bindDn"));
      initializer.addPropertyValue("bindCredential", element.getAttribute("bindCredential"));
      connectionConfig.addPropertyValue("connectionInitializer", initializer.getBeanDefinition());
    }
  }

  /**
   * Creates a provider.
   *
   * @param element containing configuration
   * @return provider bean definition
   */
  protected BeanDefinition parseProvider(final Element element) {
    final BeanDefinitionBuilder provider = BeanDefinitionBuilder.genericBeanDefinition(
      element.getAttribute("provider"));
    return provider.getBeanDefinition();
  }

  /**
   * Sets a property if the given attribute is set.
   * @param element Element from which to obtain property
   * @param attribute Attribute value for obtaining property
   * @param property Property to set
   * @param builder Bean builder to receive property
   */
  protected void setIfPresent(final Element element, final String attribute, final String property, BeanDefinitionBuilder builder) {
    if (element.hasAttribute(attribute)) {
      builder.addPropertyValue(property, element.getAttribute(attribute));
    }
  }

  /**
   * Sets a property if the given attribute is set. Properties are conditionally set if their corresponding attributes are present.
   * @param element Element from which to obtain property
   * @param attribute Attribute value for obtaining property
   * @param attrs Map of attributes and properties to be set
   * @param builder Bean builder to receive property
   */
  protected void setIfPresent(final Element element, final String attribute, Map<String, String> attrs, BeanDefinitionBuilder builder) {
    if (attribute == null || element.hasAttribute(attribute)) {
      for (Map.Entry<String, String> entry : attrs.entrySet()) {
        setIfPresent(element, entry.getKey(), entry.getValue(), builder);
      }
    }
  }

  /**
   * Sets all provided properties. All attributes in +attrs+ are assumed to be present.
   * @param element Element from which to obtain property
   * @param attrs Map of attributes and properties to be set
   * @param builder Bean builder to receive property
   */
  protected void setAll(final Element element, final Map<String, String> attrs, final BeanDefinitionBuilder builder) {
    for (Map.Entry<String, String> entry : attrs.entrySet()) {
      builder.addPropertyValue(entry.getValue(), element.getAttribute(entry.getKey()));
    }
  }

  /**
   * Sets a property if the given attribute is set. All attributes in +attrs+ are assumed to be present.
   * @param element Element from which to obtain property
   * @param attribute Attribute value for obtaining property
   * @param attrs Map of attributes and properties to be set
   * @param builder Bean builder to receive property
   */
  protected void setAllIfPresent(final Element element, final String attribute, Map<String, String> attrs, BeanDefinitionBuilder builder) {
    if (attribute == null || element.hasAttribute(attribute)) {
      setAll(element, attrs, builder);
    }
  }
}
