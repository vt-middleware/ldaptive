/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring;

import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.FormatDnResolver;
import org.ldaptive.auth.PooledBindAuthenticationHandler;
import org.ldaptive.auth.PooledSearchDnResolver;
import org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordPolicyAuthenticationResponseHandler;
import org.ldaptive.control.PasswordPolicyControl;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.SearchValidator;
import org.ldaptive.ssl.SslConfig;
import org.ldaptive.ssl.X509CredentialConfig;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Spring namespace handler for ldaptive.
 *
 * @author  Middleware Services
 */
public class NamespaceHandler extends NamespaceHandlerSupport
{


  @Override
  public void init()
  {
    registerBeanDefinitionParser("anonymous-search-authenticator", new AnonSearchAuthenticatorBeanDefinitionParser());
    registerBeanDefinitionParser("bind-search-authenticator", new BindSearchAuthenticatorBeanDefinitionParser());
    registerBeanDefinitionParser("direct-authenticator", new DirectAuthenticatorBeanDefinitionParser());
    registerBeanDefinitionParser("ad-authenticator", new ADAuthenticatorBeanDefinitionParser());
  }


  /**
   * Parser for <pre>anonymous-search-authenticator</pre> elements.
   */
  private static class AnonSearchAuthenticatorBeanDefinitionParser
    extends AbstractSearchAuthenticatorBeanDefinitionParser
  {


    @Override
    protected String resolveId(
      final Element element,
      // CheckStyle:IllegalTypeCheck OFF
      final AbstractBeanDefinition definition,
      // CheckStyle:IllegalTypeCheck ON
      final ParserContext parserContext)
      throws BeanDefinitionStoreException
    {
      final String idAttrValue = element.getAttribute("id");
      return StringUtils.hasText(idAttrValue) ? idAttrValue : "anonymous-search-authenticator";
    }
  }


  /**
   * Parser for <pre>bind-search-authenticator</pre> elements.
   */
  private static class BindSearchAuthenticatorBeanDefinitionParser
    extends AbstractSearchAuthenticatorBeanDefinitionParser
  {


    @Override
    protected String resolveId(
      final Element element,
      // CheckStyle:IllegalTypeCheck OFF
      final AbstractBeanDefinition definition,
      // CheckStyle:IllegalTypeCheck ON
      final ParserContext parserContext)
      throws BeanDefinitionStoreException
    {
      final String idAttrValue = element.getAttribute("id");
      return StringUtils.hasText(idAttrValue) ? idAttrValue : "bind-search-authenticator";
    }
  }


  /**
   * Parser for <pre>direct-authenticator</pre> elements.
   */
  private static class DirectAuthenticatorBeanDefinitionParser extends AbstractAuthenticatorBeanDefinitionParser
  {


    @Override
    protected String resolveId(
      final Element element,
      // CheckStyle:IllegalTypeCheck OFF
      final AbstractBeanDefinition definition,
      // CheckStyle:IllegalTypeCheck ON
      final ParserContext parserContext)
      throws BeanDefinitionStoreException
    {
      final String idAttrValue = element.getAttribute("id");
      return StringUtils.hasText(idAttrValue) ? idAttrValue : "direct-authenticator";
    }


    @Override
    protected void doParse(
      final Element element, final ParserContext context, final BeanDefinitionBuilder builder)
    {
      builder.addConstructorArgValue(new FormatDnResolver(element.getAttribute("format")));
      builder.addConstructorArgValue(parseAuthHandler(element));
      if (element.hasAttribute("usePpolicy") && Boolean.valueOf(element.getAttribute("usePpolicy"))) {
        builder.addPropertyValue("authenticationResponseHandlers", new PasswordPolicyAuthenticationResponseHandler());
      }
    }
  }


  /**
   * Parser for <pre>ad-authenticator</pre> elements.
   */
  private static class ADAuthenticatorBeanDefinitionParser extends AbstractAuthenticatorBeanDefinitionParser
  {


    @Override
    protected String resolveId(
      final Element element,
      // CheckStyle:IllegalTypeCheck OFF
      final AbstractBeanDefinition definition,
      // CheckStyle:IllegalTypeCheck ON
      final ParserContext parserContext)
      throws BeanDefinitionStoreException
    {
      final String idAttrValue = element.getAttribute("id");
      return StringUtils.hasText(idAttrValue) ? idAttrValue : "ad-authenticator";
    }


    @Override
    protected void doParse(
      final Element element, final ParserContext context, final BeanDefinitionBuilder builder)
    {
      builder.addConstructorArgValue(new FormatDnResolver("%s@domain.com"));
      builder.addConstructorArgValue(parseAuthHandler(element));
      builder.addPropertyValue("authenticationResponseHandlers", new ActiveDirectoryAuthenticationResponseHandler());
    }
  }


  /**
   * Common implementation for search based authenticators.
   */
  private abstract static class AbstractSearchAuthenticatorBeanDefinitionParser
    extends AbstractAuthenticatorBeanDefinitionParser
  {


    @Override
    protected void doParse(
      final Element element, final ParserContext context, final BeanDefinitionBuilder builder)
    {
      final BlockingConnectionPool pool = parseConnectionPool(element);
      pool.setName("search-pool");
      final PooledConnectionFactory connectionFactory = new PooledConnectionFactory();
      connectionFactory.setConnectionPool(pool);

      final PooledSearchDnResolver resolver = new PooledSearchDnResolver();
      resolver.setBaseDn(element.getAttribute("baseDn"));
      resolver.setSubtreeSearch(Boolean.valueOf(element.getAttribute("subtreeSearch")));
      resolver.setUserFilter(element.getAttribute("userFilter"));
      resolver.setConnectionFactory(connectionFactory);

      if (element.hasAttribute("usePpolicy") && Boolean.valueOf(element.getAttribute("usePpolicy"))) {
        builder.addPropertyValue("authenticationResponseHandlers", new PasswordPolicyAuthenticationResponseHandler());
      }
      builder.addConstructorArgValue(resolver);
      builder.addConstructorArgValue(parseAuthHandler(element));
      pool.initialize();
    }
  }


  /**
   * Common implementation for all authenticators.
   */
  private abstract static class AbstractAuthenticatorBeanDefinitionParser extends AbstractSingleBeanDefinitionParser
  {


    @Override
    protected Class<?> getBeanClass(final Element element)
    {
      return Authenticator.class;
    }


    /**
     * Creates a pooled authentication handler for performing binds.
     *
     * @param  element  containing configuration
     *
     * @return  pooled bind authentication handler
     */
    protected PooledBindAuthenticationHandler parseAuthHandler(final Element element)
    {
      final BlockingConnectionPool pool = parseConnectionPool(element);
      pool.setName("bind-pool");
      final PooledConnectionFactory connectionFactory = new PooledConnectionFactory();
      connectionFactory.setConnectionPool(pool);
      final PooledBindAuthenticationHandler authHandler = new PooledBindAuthenticationHandler();
      authHandler.setConnectionFactory(connectionFactory);
      if (element.hasAttribute("usePpolicy") && Boolean.valueOf(element.getAttribute("usePpolicy"))) {
        authHandler.setAuthenticationControls(new PasswordPolicyControl());
      }
      pool.initialize();
      return authHandler;
    }


    /**
     * Creates a blocking connection pool.
     *
     * @param  element  containing configuration
     *
     * @return  blocking connection pool
     */
    protected BlockingConnectionPool parseConnectionPool(final Element element)
    {
      final BlockingConnectionPool pool = new BlockingConnectionPool();
      pool.setConnectionFactory(new DefaultConnectionFactory(parseConnectionConfig(element)));
      pool.setPoolConfig(parsePoolConfig(element));
      pool.setBlockWaitTime(Long.valueOf(element.getAttribute("blockWaitTime")));
      pool.setFailFastInitialize(Boolean.valueOf(element.getAttribute("failFastInitialize")));
      pool.setPruneStrategy(
        new IdlePruneStrategy(
          Long.valueOf(element.getAttribute("prunePeriod")),
          Long.valueOf(element.getAttribute("idleTime"))));
      pool.setValidator(new SearchValidator());
      return pool;
    }


    /**
     * Creates a connection config. If a bindDn and bindCredential are present, a {@link BindConnectionInitializer} is
     * inserted into the connection config.
     *
     * @param  element  containing configuration
     *
     * @return  connection config
     */
    protected ConnectionConfig parseConnectionConfig(final Element element)
    {
      final ConnectionConfig connectionConfig = new ConnectionConfig();
      connectionConfig.setLdapUrl(element.getAttribute("ldapUrl"));
      connectionConfig.setUseStartTLS(Boolean.valueOf(element.getAttribute("useStartTLS")));
      connectionConfig.setUseSSL(Boolean.valueOf(element.getAttribute("useSSL")));
      connectionConfig.setConnectTimeout(Long.valueOf(element.getAttribute("connectTimeout")));

      if (element.hasAttribute("trustCertificates")) {
        final X509CredentialConfig credentialConfig = new X509CredentialConfig();
        credentialConfig.setTrustCertificates(element.getAttribute("trustCertificates"));
        final SslConfig sslConfig = new SslConfig();
        sslConfig.setCredentialConfig(credentialConfig);
        connectionConfig.setSslConfig(sslConfig);
      }

      if (element.hasAttribute("bindDn") && element.hasAttribute("bindCredential")) {
        final BindConnectionInitializer initializer = new BindConnectionInitializer();
        initializer.setBindDn(element.getAttribute("bindDn"));
        initializer.setBindCredential(new Credential(element.getAttribute("bindCredential")));
        connectionConfig.setConnectionInitializer(initializer);
      }
      return connectionConfig;
    }


    /**
     * Creates a pool config.
     *
     * @param  element  containing configuration
     *
     * @return  pool config
     */
    protected PoolConfig parsePoolConfig(final Element element)
    {
      final PoolConfig poolConfig = new PoolConfig();
      poolConfig.setMinPoolSize(Integer.valueOf(element.getAttribute("minPoolSize")));
      poolConfig.setMaxPoolSize(Integer.valueOf(element.getAttribute("maxPoolSize")));
      poolConfig.setValidateOnCheckOut(Boolean.valueOf(element.getAttribute("validateOnCheckOut")));
      poolConfig.setValidatePeriodically(Boolean.valueOf(element.getAttribute("validatePeriodically")));
      poolConfig.setValidatePeriod(Long.valueOf(element.getAttribute("validatePeriod")));
      return poolConfig;
    }
  }
}
