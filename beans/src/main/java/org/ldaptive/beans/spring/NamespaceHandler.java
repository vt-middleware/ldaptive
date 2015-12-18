/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring;

import org.ldaptive.beans.spring.parser.ADAuthenticatorBeanDefinition;
import org.ldaptive.beans.spring.parser.ActiveDirectoryAuthenticationResponseHandlerBeanDefinition;
import org.ldaptive.beans.spring.parser.AnonSearchAuthenticatorBeanDefinition;
import org.ldaptive.beans.spring.parser.BindSearchAuthenticatorBeanDefinition;
import org.ldaptive.beans.spring.parser.ConnectionFactoryBeanDefinition;
import org.ldaptive.beans.spring.parser.Crammd5SearchAuthenticatorBeanDefinition;
import org.ldaptive.beans.spring.parser.Digestmd5SearchAuthenticatorBeanDefinition;
import org.ldaptive.beans.spring.parser.DirectAuthenticatorBeanDefinition;
import org.ldaptive.beans.spring.parser.EDirectoryAuthenticationResponseHandlerBeanDefinition;
import org.ldaptive.beans.spring.parser.ExternalSearchAuthenticatorBeanDefinition;
import org.ldaptive.beans.spring.parser.FreeIPAAuthenticationResponseHandlerBeanDefinition;
import org.ldaptive.beans.spring.parser.GssapiSearchAuthenticatorBeanDefinition;
import org.ldaptive.beans.spring.parser.PasswordExpirationAuthenticationResponseHandlerBeanDefinition;
import org.ldaptive.beans.spring.parser.PasswordPolicyAuthenticationResponseHandlerBeanDefinition;
import org.ldaptive.beans.spring.parser.PooledConnectionFactoryBeanDefinition;
import org.ldaptive.beans.spring.parser.SaslSearchAuthenticatorBeanDefinition;
import org.ldaptive.beans.spring.parser.SearchExecutorBeanDefinition;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Spring namespace handler for ldaptive.
 *
 * @author Middleware Services
 */
public class NamespaceHandler extends NamespaceHandlerSupport
{


  @Override
  public void init()
  {

    registerBeanDefinitionParser("anonymous-search-authenticator", new AnonSearchAuthenticatorBeanDefinition());
    registerBeanDefinitionParser("bind-search-authenticator", new BindSearchAuthenticatorBeanDefinition());
    registerBeanDefinitionParser("direct-authenticator", new DirectAuthenticatorBeanDefinition());
    registerBeanDefinitionParser("ad-authenticator", new ADAuthenticatorBeanDefinition());
    registerBeanDefinitionParser("pooled-connection-factory", new PooledConnectionFactoryBeanDefinition());
    registerBeanDefinitionParser("connection-factory", new ConnectionFactoryBeanDefinition());
    registerBeanDefinitionParser("search-executor", new SearchExecutorBeanDefinition());
    registerBeanDefinitionParser("sasl-search-authenticator", new SaslSearchAuthenticatorBeanDefinition());
    registerBeanDefinitionParser("gssapi-search-authenticator", new GssapiSearchAuthenticatorBeanDefinition());
    registerBeanDefinitionParser("crammd5-search-authenticator", new Crammd5SearchAuthenticatorBeanDefinition());
    registerBeanDefinitionParser("digestmd5-search-authenticator", new Digestmd5SearchAuthenticatorBeanDefinition());
    registerBeanDefinitionParser("external-search-authenticator", new ExternalSearchAuthenticatorBeanDefinition());
    registerBeanDefinitionParser("active-directory-handler", new ActiveDirectoryAuthenticationResponseHandlerBeanDefinition());
    registerBeanDefinitionParser("e-directory-handler", new EDirectoryAuthenticationResponseHandlerBeanDefinition());
    registerBeanDefinitionParser("free-ipa-handler", new FreeIPAAuthenticationResponseHandlerBeanDefinition());
    registerBeanDefinitionParser("password-policy-handler", new PasswordPolicyAuthenticationResponseHandlerBeanDefinition());
    registerBeanDefinitionParser("password-expiration-handler", new PasswordExpirationAuthenticationResponseHandlerBeanDefinition());
  }
}
