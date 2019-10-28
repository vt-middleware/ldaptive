/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ldaptive.auth.AggregateAuthenticationHandler;
import org.ldaptive.auth.AggregateAuthenticationResponseHandler;
import org.ldaptive.auth.AggregateDnResolver;
import org.ldaptive.auth.AggregateEntryResolver;
import org.ldaptive.auth.AuthenticationHandler;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.DnResolver;
import org.ldaptive.auth.EntryResolver;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Parser for <pre>aggregate-authenticator</pre> elements.
 *
 * @author Middleware Services
 */
public class AggregateAuthenticatorBeanDefinitionParser
  extends org.springframework.beans.factory.xml.AbstractBeanDefinitionParser
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
    return StringUtils.hasText(idAttrValue) ? idAttrValue : "aggregate-authenticator";
  }


  @Override
  protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext context)
  {
    final BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(
      AggregateAuthenticatorFactoryBean.class);
    final ManagedList<BeanDefinition> authenticators = new ManagedList<>();
    for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
      BeanDefinitionParser parser = null;
      if (child instanceof Element) {
        switch (child.getLocalName()) {

        case "anonymous-search-authenticator":
          parser = new AnonSearchAuthenticatorBeanDefinitionParser();
          break;

        case "bind-search-authenticator":
          parser = new BindSearchAuthenticatorBeanDefinitionParser();
          break;

        case "sasl-bind-search-authenticator":
          parser = new SaslBindSearchAuthenticatorBeanDefinitionParser();
          break;

        case "direct-authenticator":
          parser = new DirectAuthenticatorBeanDefinitionParser();
          break;

        case "ad-authenticator":
          parser = new ADAuthenticatorBeanDefinitionParser();
          break;

        default:
          throw new IllegalArgumentException("Unknown authenticator type: " + child.getLocalName());
        }
      }
      if (parser != null) {
        authenticators.add(parser.parse((Element) child, context));
      }
    }

    factory.addPropertyValue("authenticators", authenticators);
    factory.addPropertyValue("allowMultipleDns", element.getAttribute("allowMultipleDns"));
    if (element.hasAttribute("returnAttributes")) {
      factory.addPropertyValue("returnAttributes", element.getAttribute("returnAttributes"));
    }
    factory.addPropertyValue("resolveEntryOnFailure", element.getAttribute("resolveEntryOnFailure"));
    return factory.getBeanDefinition();
  }


  /**
   * Factory bean that creates an authenticator with an {@link AggregateDnResolver}.
   */
  protected static class AggregateAuthenticatorFactoryBean implements FactoryBean<Authenticator>
  {

    /** Authenticators to aggregate. */
    private List<Authenticator> authenticators;

    /** Value for {@link AggregateDnResolver#getAllowMultipleDns()}. */
    private boolean allowMultipleDns;

    /** Value for {@link Authenticator#getReturnAttributes()}. */
    private String[] returnAttributes;

    /** Value of {@link Authenticator#getResolveEntryOnFailure()}. */
    private boolean resolveEntryOnFailure;


    /**
     * Sets the authenticators to aggregate.
     *
     * @param  auths  authenticators to aggregate
     */
    public void setAuthenticators(final List<Authenticator> auths)
    {
      authenticators = auths;
    }


    /**
     * Sets whether the aggrgate authenticator will allow multiple DNs.
     *
     * @param  b  whether multiple DNs are allowed
     */
    public void setAllowMultipleDns(final boolean b)
    {
      allowMultipleDns = b;
    }


    /**
     * Sets the return attributes.
     *
     * @param  attrs  return attributes
     */
    public void setReturnAttributes(final String... attrs)
    {
      returnAttributes = attrs;
    }


    /**
     * Sets whether to execute the entry resolver on authentication failure.
     *
     * @param  b  whether to execute the entry resolver
     */
    public void setResolveEntryOnFailure(final boolean b)
    {
      resolveEntryOnFailure = b;
    }


    @Override
    public Authenticator getObject() throws Exception
    {
      final Authenticator aggregateAuth = new Authenticator();
      final Map<String, DnResolver> dnResolvers = new HashMap<>();
      final Map<String, AuthenticationHandler> authHandlers = new HashMap<>();
      final Map<String, EntryResolver> entryResolvers = new HashMap<>();
      final Map<String, AuthenticationResponseHandler[]> responseHandlers = new HashMap<>();

      int count = 0;
      for (Authenticator auth : authenticators) {
        final String id = String.format("%s-%s", auth.hashCode(), String.valueOf(count++));
        dnResolvers.put(id, auth.getDnResolver());
        authHandlers.put(id, auth.getAuthenticationHandler());
        if (auth.getEntryResolver() != null) {
          entryResolvers.put(id, auth.getEntryResolver());
        }
        if (auth.getResponseHandlers() != null) {
          responseHandlers.put(id, auth.getResponseHandlers());
        }
      }

      final AggregateDnResolver dnResolver = new AggregateDnResolver();
      dnResolver.setAllowMultipleDns(allowMultipleDns);
      dnResolver.setDnResolvers(dnResolvers);
      aggregateAuth.setDnResolver(dnResolver);

      final AggregateAuthenticationHandler authHandler = new AggregateAuthenticationHandler();
      authHandler.setAuthenticationHandlers(authHandlers);
      aggregateAuth.setAuthenticationHandler(authHandler);

      if (!entryResolvers.isEmpty()) {
        final AggregateEntryResolver entryResolver = new AggregateEntryResolver();
        entryResolver.setEntryResolvers(entryResolvers);
        aggregateAuth.setEntryResolver(entryResolver);
      }

      if (!responseHandlers.isEmpty()) {
        final AggregateAuthenticationResponseHandler responseHandler = new AggregateAuthenticationResponseHandler();
        responseHandler.setAuthenticationResponseHandlers(responseHandlers);
        aggregateAuth.setResponseHandlers(responseHandler);
      }

      aggregateAuth.setReturnAttributes(returnAttributes);
      aggregateAuth.setResolveEntryOnFailure(resolveEntryOnFailure);
      return aggregateAuth;
    }


    @Override
    public Class<Authenticator> getObjectType()
    {
      return Authenticator.class;
    }


    @Override
    public boolean isSingleton()
    {
      return true;
    }
  }
}
