/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
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
  private static final EntryResolver NOOP_RESOLVER = new NoOpEntryResolver();

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

  /** Handlers to handle authentication responses. */
  private AuthenticationResponseHandler[] authenticationResponseHandlers;

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
   * Returns the authentication response handlers.
   *
   * @return  authentication response handlers
   */
  public AuthenticationResponseHandler[] getAuthenticationResponseHandlers()
  {
    return authenticationResponseHandlers;
  }


  /**
   * Sets the authentication response handlers.
   *
   * @param  handlers  authentication response handlers
   */
  public void setAuthenticationResponseHandlers(final AuthenticationResponseHandler... handlers)
  {
    authenticationResponseHandlers = handlers;
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
    logger.debug("authenticate dn={} with request={}", dn, request);

    final AuthenticationResponse invalidInput = validateInput(dn, request);
    if (invalidInput != null) {
      return invalidInput;
    }

    LdapEntry entry = null;

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

    logger.info("Authentication {} for dn: {}", response.getResult() ? "succeeded" : "failed", dn);

    final AuthenticationResponse authResponse = new AuthenticationResponse(
      response.getResult() ? AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS
                           : AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE,
      response.getResultCode(),
      dn,
      entry,
      response.getMessage(),
      response.getControls(),
      response.getMessageId());

    // execute authentication response handlers
    if (getAuthenticationResponseHandlers() != null && getAuthenticationResponseHandlers().length > 0) {
      for (AuthenticationResponseHandler ah : getAuthenticationResponseHandlers()) {
        ah.handle(authResponse);
      }
    }

    logger.debug("authenticate response={} for dn={} with request={}", response, dn, processedRequest);
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
      response = new AuthenticationResponse(
        AuthenticationResultCode.INVALID_CREDENTIAL,
        null,
        dn,
        null,
        "Credential cannot be null",
        null,
        -1);
    } else if (credential.getBytes().length == 0) {
      response = new AuthenticationResponse(
        AuthenticationResultCode.INVALID_CREDENTIAL,
        null,
        dn,
        null,
        "Credential cannot be empty",
        null,
        -1);
    } else if (dn == null) {
      response = new AuthenticationResponse(
        AuthenticationResultCode.DN_RESOLUTION_FAILURE,
        null,
        dn,
        null,
        "DN cannot be null",
        null,
        -1);
    } else if (dn.isEmpty()) {
      response = new AuthenticationResponse(
        AuthenticationResultCode.DN_RESOLUTION_FAILURE,
        null,
        dn,
        null,
        "DN cannot be empty",
        null,
        -1);
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
   */
  protected AuthenticationRequest processRequest(final String dn, final AuthenticationRequest request)
  {
    if (returnAttributes == null) {
      return request;
    }
    final AuthenticationRequest newRequest = AuthenticationRequest.newAuthenticationRequest(request);
    newRequest.setReturnAttributes(LdapUtils.concatArrays(newRequest.getReturnAttributes(), returnAttributes));
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
  protected LdapEntry resolveEntry(final AuthenticationCriteria criteria, final AuthenticationHandlerResponse response)
    throws LdapException
  {
    LdapEntry entry = null;
    EntryResolver er;
    if (resolveEntryOnFailure || response.getResult()) {
      if (entryResolver != null) {
        er = entryResolver;
      } else if (!ReturnAttributes.NONE.equalsAttributes(criteria.getAuthenticationRequest().getReturnAttributes())) {
        er = new SearchEntryResolver();
      } else {
        er = NOOP_RESOLVER;
      }
      try {
        entry = er.resolve(criteria, response);
        logger.trace("resolved entry={} with resolver={}", entry, er);
      } catch (LdapException e) {
        logger.debug("entry resolution failed for resolver={}", er, e);
      }
    }
    if (entry == null) {
      entry = NOOP_RESOLVER.resolve(criteria, response);
      logger.trace("resolved entry={} with resolver={}", entry, NOOP_RESOLVER);
    }
    return entry;
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::dnResolver=%s, authenticationHandler=%s, entryResolver=%s, returnAttributes=%s, " +
        "authenticationResponseHandlers=%s]",
        getClass().getName(),
        hashCode(),
        getDnResolver(),
        getAuthenticationHandler(),
        getEntryResolver(),
        Arrays.toString(getReturnAttributes()),
        Arrays.toString(getAuthenticationResponseHandlers()));
  }
}
