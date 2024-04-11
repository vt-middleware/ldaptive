/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.ldaptive.ConnectionFactoryManager;
import org.ldaptive.Credential;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.ReturnAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides functionality to authenticate users against an ldap directory.
 *
 * @author  Middleware Services
 */
public class Authenticator
{

  /** NoOp entry resolver. */
  private static final EntryResolver NO_OP_RESOLVER = new NoOpEntryResolver();

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** For finding user DNs. */
  private DnResolver dnResolver;

  /** Handler to handle authentication. */
  private AuthenticationHandler authenticationHandler;

  /** For finding user entries. */
  private EntryResolver entryResolver;

  /** User attributes to return. Concatenated to {@link AuthenticationRequest#getReturnAttributes()}. */
  private String[] returnAttributes;

  /** Handlers to handle authentication requests. */
  private AuthenticationRequestHandler[] requestHandlers;

  /** Handlers to handle authentication responses. */
  private AuthenticationResponseHandler[] responseHandlers;

  /** Whether to execute the entry resolver on authentication failure. */
  private boolean resolveEntryOnFailure;


  /** Default constructor. */
  public Authenticator() {}


  /**
   * Creates a new authenticator.
   *
   * @param  resolver  dn resolver
   * @param  handler  authentication handler
   */
  public Authenticator(final DnResolver resolver, final AuthenticationHandler handler)
  {
    setDnResolver(resolver);
    setAuthenticationHandler(handler);
  }


  /**
   * Returns the DN resolver.
   *
   * @return  DN resolver
   */
  public DnResolver getDnResolver()
  {
    return dnResolver;
  }


  /**
   * Sets the DN resolver.
   *
   * @param  resolver  for finding DNs
   */
  public void setDnResolver(final DnResolver resolver)
  {
    dnResolver = resolver;
  }


  /**
   * Returns the authentication handler.
   *
   * @return  authentication handler
   */
  public AuthenticationHandler getAuthenticationHandler()
  {
    return authenticationHandler;
  }


  /**
   * Sets the authentication handler.
   *
   * @param  handler  for performing authentication
   */
  public void setAuthenticationHandler(final AuthenticationHandler handler)
  {
    authenticationHandler = handler;
  }


  /**
   * Returns the entry resolver.
   *
   * @return  entry resolver
   */
  public EntryResolver getEntryResolver()
  {
    return entryResolver;
  }


  /**
   * Sets the entry resolver.
   *
   * @param  resolver  for finding entries
   */
  public void setEntryResolver(final EntryResolver resolver)
  {
    entryResolver = resolver;
  }


  /**
   * Returns whether to execute the entry resolver on authentication failure.
   *
   * @return  whether to execute the entry resolver on authentication failure
   */
  public boolean getResolveEntryOnFailure()
  {
    return resolveEntryOnFailure;
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


  /**
   * Returns the return attributes.
   *
   * @return  attributes to return
   */
  public String[] getReturnAttributes()
  {
    return returnAttributes;
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
   * Returns the authentication request handlers.
   *
   * @return  authentication request handlers
   */
  public AuthenticationRequestHandler[] getRequestHandlers()
  {
    return requestHandlers;
  }


  /**
   * Sets the authentication request handlers.
   *
   * @param  handlers  authentication request handlers
   */
  public void setRequestHandlers(final AuthenticationRequestHandler... handlers)
  {
    requestHandlers = handlers;
  }


  /**
   * Returns the authentication response handlers.
   *
   * @return  authentication response handlers
   */
  public AuthenticationResponseHandler[] getResponseHandlers()
  {
    return responseHandlers;
  }


  /**
   * Sets the authentication response handlers.
   *
   * @param  handlers  authentication response handlers
   */
  public void setResponseHandlers(final AuthenticationResponseHandler... handlers)
  {
    responseHandlers = handlers;
  }


  /**
   * This will attempt to find the DN for the supplied user. {@link DnResolver#resolve(User)} is invoked to perform this
   * operation.
   *
   * @param  user  to find DN for
   *
   * @return  user DN
   *
   * @throws  LdapException  if an LDAP error occurs during resolution
   */
  public String resolveDn(final User user)
    throws LdapException
  {
    return dnResolver.resolve(user);
  }


  /**
   * Authenticate the user in the supplied request.
   *
   * @param  request  authentication request
   *
   * @return  response containing the ldap entry of the user authenticated
   *
   * @throws  LdapException  if an LDAP error occurs
   */
  public AuthenticationResponse authenticate(final AuthenticationRequest request)
    throws LdapException
  {
    return authenticate(resolveDn(request.getUser()), request);
  }


  /**
   * Attempts to close any connection factories associated with this authenticator. Inspects the {@link #dnResolver},
   * {@link #authenticationHandler} and {@link #entryResolver} for type {@link ConnectionFactoryManager}. If found,
   * those underlying connection factories are closed. {@link AggregateDnResolver}, {@link
   * AggregateAuthenticationHandler} and {@link AggregateEntryResolver} are handled as well.
   *
   * Note that custom components that contain connection factories but do not implement {@link ConnectionFactoryManager}
   * <b>will not</b> be closed by this method.
   */
  public void close()
  {
    final Set<ConnectionFactoryManager> managers = new HashSet<>();
    if (dnResolver instanceof ConnectionFactoryManager) {
      managers.add((ConnectionFactoryManager) dnResolver);
    } else if (dnResolver instanceof AggregateDnResolver) {
      final Map<String, DnResolver> resolvers = ((AggregateDnResolver) dnResolver).getDnResolvers();
      if (resolvers != null) {
        resolvers.values().stream()
          .filter(ConnectionFactoryManager.class::isInstance)
          .map(ConnectionFactoryManager.class::cast)
          .forEach(managers::add);
      }
    }
    if (authenticationHandler instanceof ConnectionFactoryManager) {
      managers.add((ConnectionFactoryManager) authenticationHandler);
    } else if (authenticationHandler instanceof AggregateAuthenticationHandler) {
      final Map<String, AuthenticationHandler> handlers =
        ((AggregateAuthenticationHandler) authenticationHandler).getAuthenticationHandlers();
      if (handlers != null) {
        handlers.values().stream()
          .filter(ConnectionFactoryManager.class::isInstance)
          .map(ConnectionFactoryManager.class::cast)
          .forEach(managers::add);
      }
    }
    if (entryResolver instanceof ConnectionFactoryManager) {
      managers.add((ConnectionFactoryManager) entryResolver);
    } else if (entryResolver instanceof AggregateEntryResolver) {
      final Map<String, EntryResolver> resolvers = ((AggregateEntryResolver) entryResolver).getEntryResolvers();
      if (resolvers != null) {
        resolvers.values().stream()
          .filter(ConnectionFactoryManager.class::isInstance)
          .map(ConnectionFactoryManager.class::cast)
          .forEach(managers::add);
      }
    }

    if (!managers.isEmpty()) {
      closeConnectionFactoryManagers(managers);
    }
  }


  /**
   * Attempts to close all the connection factories in the supplied collection.
   *
   * @param  managers  to close connection factories for
   */
  private void closeConnectionFactoryManagers(final Set<ConnectionFactoryManager> managers)
  {
    if (managers != null) {
      managers.stream()
        .filter(Objects::nonNull)
        .map(ConnectionFactoryManager::getConnectionFactory)
        .filter(Objects::nonNull)
        .distinct()
        .forEach(cf -> {
          try {
            cf.close();
          } catch (Exception e) {
            logger.warn("Error closing connection factory {}", cf, e);
          }
        });
    }
  }


  /**
   * Validates input and performs authentication using an {@link AuthenticationHandler}. Executes any configured {@link
   * AuthenticationResponseHandler}.
   *
   * @param  dn  to authenticate as
   * @param  request  containing authentication parameters
   *
   * @return  ldap entry for the supplied DN
   *
   * @throws  LdapException  if an LDAP error occurs
   */
  protected AuthenticationResponse authenticate(final String dn, final AuthenticationRequest request)
    throws LdapException
  {
    logger.trace("authenticate dn={} with request={}", dn, request);

    final AuthenticationResponse invalidInput = validateInput(dn, request);
    if (invalidInput != null) {
      return invalidInput;
    }

    final LdapEntry entry;
    final AuthenticationRequest processedRequest = processRequest(dn, request);
    AuthenticationHandlerResponse response = null;
    try {
      final AuthenticationCriteria ac = new AuthenticationCriteria(dn, processedRequest);

      // attempt to authenticate as this dn
      response = getAuthenticationHandler().authenticate(ac);
      // resolve the entry
      entry = resolveEntry(ac, response);
    } finally {
      if (response != null && response.getConnection() != null) {
        response.getConnection().close();
      }
    }

    logger.info("Authentication {} for dn: {}", response.isSuccess() ? "succeeded" : "failed", dn);

    final AuthenticationResponse authResponse = new AuthenticationResponse(response, dn, entry);
    // execute authentication response handlers
    if (getResponseHandlers() != null && getResponseHandlers().length > 0) {
      for (AuthenticationResponseHandler ah : getResponseHandlers()) {
        ah.handle(authResponse);
      }
    }

    logger.debug("Authenticate response={} for dn={} with request={}", response, dn, processedRequest);
    return authResponse;
  }


  /**
   * Validates the authentication request and resolved DN. Returns an authentication response if validation failed.
   *
   * @param  dn  to validate
   * @param  request  to validate
   *
   * @return  authentication response if validation failed, otherwise null
   */
  protected AuthenticationResponse validateInput(final String dn, final AuthenticationRequest request)
  {
    AuthenticationResponse response = null;
    final Credential credential = request.getCredential();
    if (credential == null || credential.getBytes() == null) {
      response = AuthenticationResponse.builder()
        .response(
          AuthenticationHandlerResponse.builder()
            .diagnosticMessage("Credential cannot be null")
            .resultCode(AuthenticationResultCode.INVALID_CREDENTIAL).build())
        .dn(dn)
        .build();
    } else if (credential.getBytes().length == 0) {
      response = AuthenticationResponse.builder()
        .response(
          AuthenticationHandlerResponse.builder()
            .diagnosticMessage("Credential cannot be empty")
            .resultCode(AuthenticationResultCode.INVALID_CREDENTIAL).build())
        .dn(dn)
        .build();
    } else if (dn == null) {
      response = AuthenticationResponse.builder()
        .response(
          AuthenticationHandlerResponse.builder()
            .diagnosticMessage("DN cannot be null")
            .resultCode(AuthenticationResultCode.DN_RESOLUTION_FAILURE).build())
        .dn(null)
        .build();
    } else if (dn.isEmpty()) {
      response = AuthenticationResponse.builder()
        .response(
          AuthenticationHandlerResponse.builder()
            .diagnosticMessage("DN cannot be empty")
            .resultCode(AuthenticationResultCode.DN_RESOLUTION_FAILURE).build())
        .dn(dn)
        .build();
    }
    return response;
  }


  /**
   * Creates a new authentication request applying any applicable configuration on this authenticator. Returns the
   * supplied request if no configuration is applied.
   *
   * @param  dn  to process
   * @param  request  to process
   *
   * @return  authentication request
   *
   * @throws  LdapException  if an error occurs with a request handler
   */
  protected AuthenticationRequest processRequest(final String dn, final AuthenticationRequest request)
    throws LdapException
  {
    if (returnAttributes == null && (getRequestHandlers() == null || getRequestHandlers().length == 0)) {
      return request;
    }

    final AuthenticationRequest newRequest = AuthenticationRequest.copy(request);
    if (returnAttributes != null) {
      if (newRequest.getReturnAttributes() == null ||
        ReturnAttributes.NONE.equalsAttributes(newRequest.getReturnAttributes())) {
        newRequest.setReturnAttributes(returnAttributes);
      } else {
        newRequest.setReturnAttributes(LdapUtils.concatArrays(newRequest.getReturnAttributes(), returnAttributes));
      }
    }

    // execute authentication request handlers
    if (getRequestHandlers() != null && getRequestHandlers().length > 0) {
      for (AuthenticationRequestHandler ah : getRequestHandlers()) {
        ah.handle(dn, newRequest);
      }
    }
    return newRequest;
  }


  /**
   * Attempts to find the ldap entry for the supplied DN. If an entry resolver has been configured it is used. A {@link
   * SearchEntryResolver} is used if return attributes have been requested. If none of these criteria is met, a {@link
   * NoOpDnResolver} is used.
   *
   * @param  criteria  needed by the entry resolver
   * @param  response  from the authentication handler
   *
   * @return  ldap entry
   *
   * @throws  LdapException  if an error occurs resolving the entry
   */
  protected LdapEntry resolveEntry(
    final AuthenticationCriteria criteria,
    final AuthenticationHandlerResponse response)
    throws LdapException
  {
    LdapEntry entry = null;
    final EntryResolver er;
    if (resolveEntryOnFailure || response.isSuccess()) {
      if (entryResolver != null) {
        er = entryResolver;
      } else if (!ReturnAttributes.NONE.equalsAttributes(criteria.getAuthenticationRequest().getReturnAttributes())) {
        if (dnResolver instanceof AggregateDnResolver) {
          er = ((AggregateDnResolver) dnResolver).createEntryResolver(new SearchEntryResolver());
        } else {
          er = new SearchEntryResolver();
        }
      } else {
        er = NO_OP_RESOLVER;
      }
      try {
        entry = er.resolve(criteria, response);
        logger.trace("resolved entry={} with resolver={}", entry, er);
      } catch (LdapException e) {
        logger.warn("Entry resolution failed for resolver={}", er, e);
      }
    }
    if (entry == null) {
      entry = NO_OP_RESOLVER.resolve(criteria, response);
      logger.trace("resolved entry={} with resolver={}", entry, NO_OP_RESOLVER);
    }
    return entry;
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "dnResolver=" + dnResolver + ", " +
      "authenticationHandler=" + authenticationHandler + ", " +
      "entryResolver=" + entryResolver + ", " +
      "returnAttributes=" + Arrays.toString(returnAttributes) + ", " +
      "requestHandlers=" + Arrays.toString(requestHandlers) + ", " +
      "responseHandlers=" + Arrays.toString(responseHandlers) + "]";
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


  /** Authenticator builder. */
  public static class Builder
  {

    /** Authenticator to build. */
    private final Authenticator object = new Authenticator();


    /**
     * Default constructor.
     */
    protected Builder() {}


    /**
     * Sets the DN resolver.
     *
     * @param  resolver  DN resolver
     *
     * @return  this builder
     */
    public Builder dnResolver(final DnResolver resolver)
    {
      object.setDnResolver(resolver);
      return this;
    }


    /**
     * Sets the authentication handler.
     *
     * @param  handler  authentication handler
     *
     * @return  this builder
     */
    public Builder authenticationHandler(final AuthenticationHandler handler)
    {
      object.setAuthenticationHandler(handler);
      return this;
    }


    /**
     * Sets the entry resolver.
     *
     * @param  resolver  entry resolver
     *
     * @return  this builder
     */
    public Builder entryResolver(final EntryResolver resolver)
    {
      object.setEntryResolver(resolver);
      return this;
    }


    /**
     * Sets the authentication request handlers.
     *
     * @param  handlers  request handlers
     *
     * @return  this builder
     */
    public Builder requestHandlers(final AuthenticationRequestHandler... handlers)
    {
      object.setRequestHandlers(handlers);
      return this;
    }


    /**
     * Sets the authentication response handlers.
     *
     * @param  handlers  response handlers
     *
     * @return  this builder
     */
    public Builder responseHandlers(final AuthenticationResponseHandler... handlers)
    {
      object.setResponseHandlers(handlers);
      return this;
    }


    /**
     * Sets the return attributes.
     *
     * @param  attributes  return attributes
     *
     * @return  this builder
     */
    public Builder returnAttributes(final String... attributes)
    {
      object.setReturnAttributes(attributes);
      return this;
    }


    /**
     * Returns the authenticator.
     *
     * @return  authenticator
     */
    public Authenticator build()
    {
      return object;
    }
  }
}
