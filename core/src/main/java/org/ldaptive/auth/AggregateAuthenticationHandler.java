/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.ldaptive.AbstractFreezable;
import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used in conjunction with an {@link AggregateDnResolver} to authenticate the resolved DN. In particular, the
 * resolved DN is expected to be of the form: label:DN where the label indicates the authentication handler to use.
 * This class only invokes one authentication handler that matches the label found on the DN.
 *
 * @author  Middleware Services
 */
public final class AggregateAuthenticationHandler extends AbstractFreezable implements AuthenticationHandler
{

  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /** Labeled authentication handlers. */
  private final Map<String, AuthenticationHandler> authenticationHandlers = new HashMap<>();


  /** Default constructor. */
  public AggregateAuthenticationHandler() {}


  /**
   * Creates a new aggregate authentication handler.
   *
   * @param  handlers  authentication handlers
   */
  public AggregateAuthenticationHandler(final Map<String, AuthenticationHandler> handlers)
  {
    setAuthenticationHandlers(handlers);
  }


  @Override
  public void freeze()
  {
    super.freeze();
    makeImmutable(authenticationHandlers);
  }


  /**
   * Returns the authentication handlers to aggregate over.
   *
   * @return  map of label to authentication handler
   */
  public Map<String, AuthenticationHandler> getAuthenticationHandlers()
  {
    return Collections.unmodifiableMap(authenticationHandlers);
  }


  /**
   * Sets the authentication handlers to aggregate over.
   *
   * @param  handlers  to set
   */
  public void setAuthenticationHandlers(final Map<String, AuthenticationHandler> handlers)
  {
    assertMutable();
    logger.trace("setting authenticationHandlers: {}", handlers);
    authenticationHandlers.putAll(handlers);
  }


  /**
   * Adds an authentication handler with the supplied label.
   *
   * @param  label  of the resolver
   * @param  handler  authentication handler
   */
  public void addAuthenticationHandler(final String label, final AuthenticationHandler handler)
  {
    assertMutable();
    logger.trace("adding authenticationHandler: {}:{}", label, handler);
    authenticationHandlers.put(label, handler);
  }


  @Override
  public AuthenticationHandlerResponse authenticate(final AuthenticationCriteria criteria)
    throws LdapException
  {
    final String[] labeledDn = criteria.getDn().split(":", 2);
    final AuthenticationHandler ah = authenticationHandlers.get(labeledDn[0]);
    if (ah == null) {
      throw new LdapException(
        ResultCode.PARAM_ERROR,
        "Could not find authentication handler for label: " + labeledDn[0]);
    }
    return ah.authenticate(new AuthenticationCriteria(labeledDn[1], criteria.getAuthenticationRequest()));
  }


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static Builder builder()
  {
    return new Builder();
  }


  // CheckStyle:OFF
  public static final class Builder
  {


    private final AggregateAuthenticationHandler object = new AggregateAuthenticationHandler();


    private Builder() {}


    public Builder makeImmutable()
    {
      object.freeze();
      return this;
    }


    public Builder handler(final String label, final AuthenticationHandler handler)
    {
      object.addAuthenticationHandler(label, handler);
      return this;
    }


    public AggregateAuthenticationHandler build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
