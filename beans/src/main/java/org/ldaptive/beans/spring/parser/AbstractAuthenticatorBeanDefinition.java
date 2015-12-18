package org.ldaptive.beans.spring.parser;

import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.PooledBindAuthenticationHandler;
import org.ldaptive.auth.ext.PasswordExpirationAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordPolicyAuthenticationResponseHandler;
import org.ldaptive.control.PasswordPolicyControl;
import org.ldaptive.control.RequestControl;
import org.ldaptive.pool.PooledConnectionFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

/**
 * Common implementation for all authenticators.
 */
public abstract class AbstractAuthenticatorBeanDefinition
        extends AbstractPooledConnectionFactoryBeanDefinition
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
     * @return  pooled bind authentication handler bean definition
     */
    protected BeanDefinitionBuilder parseAuthHandler(final Element element)
    {
        final BeanDefinitionBuilder authHandler = BeanDefinitionBuilder.genericBeanDefinition(
                PooledBindAuthenticationHandler.class);
        final BeanDefinitionBuilder connectionFactory = BeanDefinitionBuilder.genericBeanDefinition(
                PooledConnectionFactory.class);
        connectionFactory.addPropertyValue("connectionPool", parseConnectionPool("bind-pool", element));
        authHandler.addPropertyValue("connectionFactory", connectionFactory.getBeanDefinition());
        if (element.hasAttribute("usePasswordPolicy")) {
            final BeanDefinitionBuilder control =  BeanDefinitionBuilder.rootBeanDefinition(
                    AbstractAuthenticatorBeanDefinition.class,
                    "parsePasswordPolicyControl");
            control.addConstructorArgValue(element.getAttribute("usePasswordPolicy"));
            authHandler.addPropertyValue("authenticationControls", control.getBeanDefinition());
        }

        return authHandler;
    }


    /**
     * Returns a {@link PasswordPolicyAuthenticationResponseHandler} if the supplied value is true.
     *
     * @param  value  of the usePasswordPolicy attribute
     *
     * @return  {@link PasswordPolicyAuthenticationResponseHandler} or null
     */
    protected static AuthenticationResponseHandler[] parsePasswordPolicyAuthenticationResponseHandler(
            final String value)
    {
        return Boolean.valueOf(value) ?
                new AuthenticationResponseHandler[] {
                        new PasswordPolicyAuthenticationResponseHandler(), new PasswordExpirationAuthenticationResponseHandler(), } :
                null;
    }


    /**
     * Returns a {@link PasswordPolicyControl} if the supplied value is true.
     *
     * @param  value  of the usePasswordPolicy attribute
     *
     * @return  {@link PasswordPolicyControl} or null
     */
    protected static RequestControl[] parsePasswordPolicyControl(final String value)
    {
        return Boolean.valueOf(value) ? new RequestControl[] {new PasswordPolicyControl()} : null;
    }
}
