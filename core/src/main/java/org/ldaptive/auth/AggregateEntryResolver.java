/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.ldaptive.AbstractFreezable;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used in conjunction with an {@link AggregateDnResolver} to resolve an entry. In particular, the resolved DN is
 * expected to be of the form: label:DN where the label indicates the entry resolver to use. This class only invokes
 * one entry resolver that matches the label found on the DN.
 *
 * @author  Middleware Services
 */
public final class AggregateEntryResolver extends AbstractFreezable implements EntryResolver
{

  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /** Labeled entry resolvers. */
  private final Map<String, EntryResolver> entryResolvers = new HashMap<>();


  /** Default constructor. */
  public AggregateEntryResolver() {}


  /**
   * Creates a new aggregate entry resolver.
   *
   * @param  resolvers  entry resolvers
   */
  public AggregateEntryResolver(final Map<String, EntryResolver> resolvers)
  {
    setEntryResolvers(resolvers);
  }


  @Override
  public void freeze()
  {
    super.freeze();
    for (EntryResolver resolver : entryResolvers.values()) {
      makeImmutable(resolver);
    }
  }


  /**
   * Returns the entry resolvers to aggregate over.
   *
   * @return  map of label to entry resolver
   */
  public Map<String, EntryResolver> getEntryResolvers()
  {
    return Collections.unmodifiableMap(entryResolvers);
  }


  /**
   * Sets the entry resolvers to aggregate over.
   *
   * @param  resolvers  to set
   */
  public void setEntryResolvers(final Map<String, EntryResolver> resolvers)
  {
    assertMutable();
    logger.trace("setting entryResolvers: {}", resolvers);
    entryResolvers.putAll(resolvers);
  }


  /**
   * Adds an entry resolver with the supplied label.
   *
   * @param  label  of the resolver
   * @param  resolver  entry resolver
   */
  public void addEntryResolver(final String label, final EntryResolver resolver)
  {
    assertMutable();
    logger.trace("adding dnResolver: {}:{}", label, resolver);
    entryResolvers.put(label, resolver);
  }


  @Override
  public LdapEntry resolve(final AuthenticationCriteria criteria, final AuthenticationHandlerResponse response)
    throws LdapException
  {
    final String[] labeledDn = criteria.getDn().split(":", 2);
    final EntryResolver er = entryResolvers.get(labeledDn[0]);
    if (er == null) {
      throw new LdapException(ResultCode.PARAM_ERROR, "Could not find entry resolver for label: " + labeledDn[0]);
    }
    return er.resolve(new AuthenticationCriteria(labeledDn[1], criteria.getAuthenticationRequest()), response);
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


    private final AggregateEntryResolver object = new AggregateEntryResolver();


    private Builder() {}


    public Builder makeImmutable()
    {
      object.freeze();
      return this;
    }


    public Builder resolver(final String label, final EntryResolver resolver)
    {
      object.addEntryResolver(label, resolver);
      return this;
    }


    public AggregateEntryResolver build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
