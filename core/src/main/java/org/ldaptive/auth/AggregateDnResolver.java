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
  private Map<String, DnResolver> dnResolvers = new HashMap<>();

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
   * Adds a DN resolver with the supplied label.
   *
   * @param  label  of the resolver
   * @param  resolver  DN resolver
   */
  public void addDnResolver(final String label, final DnResolver resolver)
  {
    logger.trace("adding dnResolver: {}:{}", label, resolver);
    dnResolvers.put(label, resolver);
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
   * #resolve(User)} finds that more than one DN resolver returns a DN. Otherwise, the first DN found is returned.
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
  public EntryResolver createEntryResolver(final EntryResolver resolver)
  {
    final Map<String, EntryResolver> resolvers = new HashMap<>(dnResolvers.size());
    for (String label : dnResolvers.keySet()) {
      resolvers.put(label, resolver);
    }
    return new AggregateEntryResolver(resolvers);
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
          logger.debug("DN resolver {} resolved dn {} for user {}", entry.getValue(), dn, user);
          if (dn != null && !dn.isEmpty()) {
            return String.format("%s:%s", entry.getKey(), dn);
          }
          return null;
        });
      logger.debug("Submitted DN resolver {}", entry.getValue());
    }
    for (int i = 1; i <= dnResolvers.size(); i++) {
      try {
        logger.trace("waiting on DN resolver {} of {}", i, dnResolvers.size());
        final String dn = cs.take().get();
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
    logger.debug("Resolved aggregate DN {}", results);
    return results.isEmpty() ? null : results.get(0);
  }


  /** Invokes {@link ExecutorService#shutdown()} on the underlying executor service. */
  public void shutdown()
  {
    service.shutdown();
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


    private final AggregateDnResolver object = new AggregateDnResolver();


    protected Builder() {}


    public Builder resolver(final String label, final DnResolver resolver)
    {
      object.addDnResolver(label, resolver);
      return this;
    }


    public AggregateDnResolver build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
