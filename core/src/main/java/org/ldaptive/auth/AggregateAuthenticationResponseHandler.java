/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.ldaptive.LdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used in conjunction with an {@link AggregateDnResolver} to execute a list of response handlers. In particular, the
 * resolved DN is expected to be of the form: label:DN where the label indicates the response handler to use. This
 * class only invokes the response handlers that matches the label found on the DN.
 *
 * @author  Middleware Services
 */
public class AggregateAuthenticationResponseHandler implements AuthenticationResponseHandler
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Labeled entry resolvers. */
  private Map<String, AuthenticationResponseHandler[]> responseHandlers = new HashMap<>();


  /** Default constructor. */
  public AggregateAuthenticationResponseHandler() {}


  /**
   * Creates a new aggregate authentication response handler.
   *
   * @param  handlers  authentication response handlers
   */
  public AggregateAuthenticationResponseHandler(final Map<String, AuthenticationResponseHandler[]> handlers)
  {
    setAuthenticationResponseHandlers(handlers);
  }


  /**
   * Returns the response handlers to aggregate over.
   *
   * @return  map of label to response handlers
   */
  public Map<String, AuthenticationResponseHandler[]> getAuthenticationResponseHandlers()
  {
    return Collections.unmodifiableMap(responseHandlers);
  }


  /**
   * Sets the response handlers to aggregate over.
   *
   * @param  handlers  to set
   */
  public void setAuthenticationResponseHandlers(final Map<String, AuthenticationResponseHandler[]> handlers)
  {
    logger.trace("setting authenticationResponseHandlers: {}", handlers);
    responseHandlers = handlers;
  }


  /**
   * Adds an authentication response handler with the supplied label.
   *
   * @param  label  of the resolver
   * @param  handlers  authentication response handler
   */
  public void addAuthenticationResponseHandlers(final String label, final AuthenticationResponseHandler... handlers)
  {
    logger.trace("adding authenticationResponseHandlers: {}:{}", label, Arrays.toString(handlers));
    responseHandlers.put(label, handlers);
  }


  @Override
  public void handle(final AuthenticationResponse response) throws LdapException
  {
    final String[] labeledDn = response.getResolvedDn().split(":", 2);
    final AuthenticationResponseHandler[] handlers = responseHandlers.get(labeledDn[0]);
    if (handlers == null) {
      throw new LdapException("Could not find response handlers for label: " + labeledDn[0]);
    }
    if (handlers.length > 0) {
      for (AuthenticationResponseHandler ah : handlers) {
        ah.handle(response);
      }
    }
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
  public static class Builder
  {


    private final AggregateAuthenticationResponseHandler object = new AggregateAuthenticationResponseHandler();


    protected Builder() {}


    public Builder handler(final String label, final AuthenticationResponseHandler... handlers)
    {
      object.addAuthenticationResponseHandlers(label, handlers);
      return this;
    }


    public AggregateAuthenticationResponseHandler build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
