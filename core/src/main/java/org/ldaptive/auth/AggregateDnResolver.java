/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Looks up a user's DN using multiple DN resolvers. Each DN resolver is invoked on a separate thread. If multiple DNs
 * are allowed then the first one retrieved is returned.
 *
 * @author  Middleware Services
 */
public class AggregateDnResolver implements DnResolver
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** To submit operations to. */
  private final ExecutorService service;

  /** Labeled DN resolvers. */
  private Map<String, DnResolver> dnResolvers;

  /** Whether to throw an exception if multiple DNs are found. */
  private boolean allowMultipleDns;


  /** Default constructor. */
  public AggregateDnResolver()
  {
    service = Executors.newCachedThreadPool();
  }


  /**
   * Creates a new aggregate dn resolver.
   *
   * @param  resolvers  dn resolvers
   */
  public AggregateDnResolver(final Map<String, DnResolver> resolvers)
  {
    this(resolvers, Executors.newCachedThreadPool());
  }


  /**
   * Creates a new aggregate dn resolver.
   *
   * @param  resolvers  dn resolvers
   * @param  es  executor service for invoking DN resolvers
   */
  public AggregateDnResolver(final Map<String, DnResolver> resolvers, final ExecutorService es)
  {
    setDnResolvers(resolvers);
    service = es;
  }


  /**
   * Returns the DN resolvers to aggregate over.
   *
   * @return  map of label to dn resolver
   */
  public Map<String, DnResolver> getDnResolvers()
  {
    return Collections.unmodifiableMap(dnResolvers);
  }


  /**
   * Sets the DN resolvers to aggregate over.
   *
   * @param  resolvers  to set
   */
  public void setDnResolvers(final Map<String, DnResolver> resolvers)
  {
    logger.trace("setting dnResolvers: {}", resolvers);
    dnResolvers = resolvers;
  }


  /**
   * Returns whether DN resolution should fail if multiple DNs are found.
   *
   * @return  whether an exception will be thrown if multiple DNs are found
   */
  public boolean getAllowMultipleDns()
  {
    return allowMultipleDns;
  }


  /**
   * Sets whether DN resolution should fail if multiple DNs are found If false an exception will be thrown if {@link
   * #resolve(User)} finds that more than one DN resolver returns a DN. Otherwise the first DN found is returned.
   *
   * @param  b  whether multiple DNs are allowed
   */
  public void setAllowMultipleDns(final boolean b)
  {
    logger.trace("setting allowMultipleDns: {}", b);
    allowMultipleDns = b;
  }


  /**
   * Creates an aggregate entry resolver using the labels from the DN resolver and the supplied entry resolver.
   *
   * @param  resolver  used for every label
   *
   * @return  aggregate entry resolver
   */
  public EntryResolver createEntryResolver(final org.ldaptive.auth.EntryResolver resolver)
  {
    final Map<String, org.ldaptive.auth.EntryResolver> resolvers = new HashMap(dnResolvers.size());
    for (String label : dnResolvers.keySet()) {
      resolvers.put(label, resolver);
    }
    return new EntryResolver(resolvers);
  }


  @Override
  public String resolve(final User user)
    throws LdapException
  {
    final CompletionService<String> cs = new ExecutorCompletionService<>(service);
    final List<String> results = new ArrayList<>(dnResolvers.size());
    for (final Map.Entry<String, DnResolver> entry : dnResolvers.entrySet()) {
      cs.submit(
        () -> {
          final String dn = entry.getValue().resolve(user);
          if (dn != null && !dn.isEmpty()) {
            return String.format("%s:%s", entry.getKey(), dn);
          }
          return null;
        });
      logger.debug("submitted DN resolver {}", entry.getValue());
    }
    for (DnResolver resolver : dnResolvers.values()) {
      try {
        logger.debug("waiting on DN resolver {}", resolver);

        final String dn = cs.take().get();
        logger.debug("DN resolver {} resolved dn {}", resolver, dn);
        if (dn != null) {
          results.add(dn);
        }
      } catch (ExecutionException e) {
        if (e.getCause() instanceof LdapException) {
          throw (LdapException) e.getCause();
        } else if (e.getCause() instanceof RuntimeException) {
          throw (RuntimeException) e.getCause();
        } else {
          logger.warn("ExecutionException thrown, ignoring", e);
        }
      } catch (InterruptedException e) {
        logger.warn("InterruptedException thrown, ignoring", e);
      }
    }
    if (results.size() > 1 && !allowMultipleDns) {
      throw new LdapException("Found more than (1) DN for: " + user);
    }
    return results.isEmpty() ? null : results.get(0);
  }


  /** Invokes {@link ExecutorService#shutdown()} on the underlying executor service. */
  public void shutdown()
  {
    service.shutdown();
  }


  @Override
  protected void finalize()
    throws Throwable
  {
    try {
      shutdown();
    } finally {
      super.finalize();
    }
  }


  /**
   * Used in conjunction with an {@link AggregateDnResolver} to authenticate the resolved DN. In particular, the
   * resolved DN is expected to be of the form: label:DN where the label indicates the authentication handler to use.
   * This class only invokes one authentication handler that matches the label found on the DN.
   */
  public static class AuthenticationHandler implements org.ldaptive.auth.AuthenticationHandler
  {

    /** Logger for this class. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** Labeled authentication handlers. */
    private Map<String, org.ldaptive.auth.AuthenticationHandler> authenticationHandlers;


    /** Default constructor. */
    public AuthenticationHandler() {}


    /**
     * Creates a new aggregate authentication handler.
     *
     * @param  handlers  authentication handlers
     */
    public AuthenticationHandler(final Map<String, org.ldaptive.auth.AuthenticationHandler> handlers)
    {
      setAuthenticationHandlers(handlers);
    }


    /**
     * Returns the authentication handlers to aggregate over.
     *
     * @return  map of label to authentication handler
     */
    public Map<String, org.ldaptive.auth.AuthenticationHandler> getAuthenticationHandlers()
    {
      return Collections.unmodifiableMap(authenticationHandlers);
    }


    /**
     * Sets the authentication handlers to aggregate over.
     *
     * @param  handlers  to set
     */
    public void setAuthenticationHandlers(final Map<String, org.ldaptive.auth.AuthenticationHandler> handlers)
    {
      logger.trace("setting authenticationHandlers: {}", handlers);
      authenticationHandlers = handlers;
    }


    @Override
    public AuthenticationHandlerResponse authenticate(final AuthenticationCriteria criteria)
      throws LdapException
    {
      final String[] labeledDn = criteria.getDn().split(":", 2);
      final org.ldaptive.auth.AuthenticationHandler ah = authenticationHandlers.get(labeledDn[0]);
      if (ah == null) {
        throw new LdapException("Could not find authentication handler for label: " + labeledDn[0]);
      }
      return ah.authenticate(new AuthenticationCriteria(labeledDn[1], criteria.getAuthenticationRequest()));
    }
  }


  /**
   * Used in conjunction with an {@link AggregateDnResolver} to resolve an entry. In particular, the resolved DN is
   * expected to be of the form: label:DN where the label indicates the entry resolver to use. This class only invokes
   * one entry resolver that matches the label found on the DN.
   */
  public static class EntryResolver implements org.ldaptive.auth.EntryResolver
  {

    /** Logger for this class. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** Labeled entry resolvers. */
    private Map<String, org.ldaptive.auth.EntryResolver> entryResolvers;


    /** Default constructor. */
    public EntryResolver() {}


    /**
     * Creates a new aggregate entry resolver.
     *
     * @param  resolvers  entry resolvers
     */
    public EntryResolver(final Map<String, org.ldaptive.auth.EntryResolver> resolvers)
    {
      setEntryResolvers(resolvers);
    }


    /**
     * Returns the entry resolvers to aggregate over.
     *
     * @return  map of label to entry resolver
     */
    public Map<String, org.ldaptive.auth.EntryResolver> getEntryResolvers()
    {
      return Collections.unmodifiableMap(entryResolvers);
    }


    /**
     * Sets the entry resolvers to aggregate over.
     *
     * @param  resolvers  to set
     */
    public void setEntryResolvers(final Map<String, org.ldaptive.auth.EntryResolver> resolvers)
    {
      logger.trace("setting entryResolvers: {}", resolvers);
      entryResolvers = resolvers;
    }


    @Override
    public LdapEntry resolve(final AuthenticationCriteria criteria, final AuthenticationHandlerResponse response)
      throws LdapException
    {
      final String[] labeledDn = criteria.getDn().split(":", 2);
      final org.ldaptive.auth.EntryResolver er = entryResolvers.get(labeledDn[0]);
      if (er == null) {
        throw new LdapException("Could not find entry resolver for label: " + labeledDn[0]);
      }
      return er.resolve(new AuthenticationCriteria(labeledDn[1], criteria.getAuthenticationRequest()), response);
    }
  }


  /**
   * Used in conjunction with an {@link AggregateDnResolver} to execute a list of response handlers. In particular, the
   * resolved DN is expected to be of the form: label:DN where the label indicates the response handler to use. This
   * class only invokes the response handlers that matches the label found on the DN.
   */
  public static class AuthenticationResponseHandler implements org.ldaptive.auth.AuthenticationResponseHandler
  {

    /** Logger for this class. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** Labeled entry resolvers. */
    private Map<String, org.ldaptive.auth.AuthenticationResponseHandler[]> responseHandlers;


    /** Default constructor. */
    public AuthenticationResponseHandler() {}


    /**
     * Creates a new aggregate authentication response handler.
     *
     * @param  handlers  authentication response handlers
     */
    public AuthenticationResponseHandler(final Map<String, org.ldaptive.auth.AuthenticationResponseHandler[]> handlers)
    {
      setAuthenticationResponseHandlers(handlers);
    }


    /**
     * Returns the response handlers to aggregate over.
     *
     * @return  map of label to response handlers
     */
    public Map<String, org.ldaptive.auth.AuthenticationResponseHandler[]> getAuthenticationResponseHandlers()
    {
      return Collections.unmodifiableMap(responseHandlers);
    }


    /**
     * Sets the response handlers to aggregate over.
     *
     * @param  handlers  to set
     */
    public void setAuthenticationResponseHandlers(
      final Map<String, org.ldaptive.auth.AuthenticationResponseHandler[]> handlers)
    {
      logger.trace("setting authenticationResponseHandlers: {}", handlers);
      responseHandlers = handlers;
    }


    @Override
    public void handle(final AuthenticationResponse response) throws LdapException
    {
      final String[] labeledDn = response.getResolvedDn().split(":", 2);
      final org.ldaptive.auth.AuthenticationResponseHandler[] handlers = responseHandlers.get(labeledDn[0]);
      if (handlers == null) {
        throw new LdapException("Could not find response handlers for label: " + labeledDn[0]);
      }
      if (handlers.length > 0) {
        for (org.ldaptive.auth.AuthenticationResponseHandler ah : handlers) {
          ah.handle(response);
        }
      }
    }
  }
}
