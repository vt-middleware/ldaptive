/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import org.ldaptive.AbstractFreezable;
import org.ldaptive.LdapException;
import org.ldaptive.concurrent.CallableWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Looks up a user's DN using multiple DN resolvers. Each DN resolver is invoked on a separate thread. If multiple DNs
 * are allowed then the first one retrieved is returned.
 *
 * @author  Middleware Services
 */
public final class AggregateDnResolver extends AbstractFreezable implements DnResolver
{

  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /** To submit operations to. */
  private final CallableWorker<String> callableWorker;

  /** Labeled DN resolvers. */
  private final Map<String, DnResolver> dnResolvers = new HashMap<>();

  /** Whether to throw an exception if multiple DNs are found. */
  private boolean allowMultipleDns;


  /** Default constructor. */
  public AggregateDnResolver()
  {
    callableWorker = new CallableWorker<>("ldaptive-aggregate-dn-resolver");
  }


  /**
   * Creates a new aggregate dn resolver.
   *
   * @param  resolvers  dn resolvers
   */
  public AggregateDnResolver(final Map<String, DnResolver> resolvers)
  {
    setDnResolvers(resolvers);
    callableWorker = new CallableWorker<>("ldaptive-aggregate-dn-resolver");
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
    callableWorker = new CallableWorker<>(es);
  }


  @Override
  public void freeze()
  {
    super.freeze();
    for (DnResolver resolver : dnResolvers.values()) {
      freeze(resolver);
    }
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
    assertMutable();
    logger.trace("setting dnResolvers: {}", resolvers);
    dnResolvers.putAll(resolvers);
  }


  /**
   * Adds a DN resolver with the supplied label.
   *
   * @param  label  of the resolver
   * @param  resolver  DN resolver
   */
  public void addDnResolver(final String label, final DnResolver resolver)
  {
    assertMutable();
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
    assertMutable();
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
    final List<Callable<String>> callables = new ArrayList<>();
    for (final Map.Entry<String, DnResolver> entry : dnResolvers.entrySet()) {
      callables.add(
        () -> {
          logger.debug("Submitted DN resolver {}", entry.getValue());
          final String dn = entry.getValue().resolve(user);
          logger.debug("DN resolver {} resolved dn {} for user {}", entry.getValue(), dn, user);
          if (dn != null && !dn.isEmpty()) {
            return String.format("%s:%s", entry.getKey(), dn);
          }
          return null;
        });
    }

    final List<String> results = new ArrayList<>(dnResolvers.size());
    final List<ExecutionException> exceptions = callableWorker.execute(
      callables,
      s -> {
        if (s != null) {
          results.add(s);
        }
      });
    for (ExecutionException e : exceptions) {
      if (e.getCause() instanceof LdapException) {
        throw (LdapException) e.getCause();
      } else if (e.getCause() instanceof RuntimeException) {
        throw (RuntimeException) e.getCause();
      } else {
        logger.warn("ExecutionException thrown, ignoring", e);
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
    callableWorker.shutdown();
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


    private final AggregateDnResolver object = new AggregateDnResolver();


    private Builder() {}


    public Builder freeze()
    {
      object.freeze();
      return this;
    }


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
