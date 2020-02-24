/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import java.util.Iterator;
import org.ldaptive.AbstractSearchOperationFactory;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DerefAliases;
import org.ldaptive.FilterTemplate;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.SearchScope;

/**
 * Base implementation for search dn resolvers.
 *
 * @author  Middleware Services
 */
public class SearchDnResolver extends AbstractSearchOperationFactory implements DnResolver
{

  /** DN to search. */
  private String baseDn = "";

  /** Filter for searching for the user. */
  private String userFilter;

  /** Filter parameters for searching for the user. */
  private Object[] userFilterParameters;

  /** Whether to throw an exception if multiple DNs are found. */
  private boolean allowMultipleDns;

  /** Whether to use a subtree search when resolving DNs. */
  private boolean subtreeSearch;

  /** How to handle aliases. */
  private DerefAliases derefAliases = DerefAliases.NEVER;


  /** Default constructor. */
  public SearchDnResolver() {}


  /**
   * Creates a new search dn resolver.
   *
   * @param  cf  connection factory
   */
  public SearchDnResolver(final ConnectionFactory cf)
  {
    setConnectionFactory(cf);
  }


  /**
   * Returns the base DN.
   *
   * @return  base DN
   */
  public String getBaseDn()
  {
    return baseDn;
  }


  /**
   * Sets the base DN.
   *
   * @param  dn  base DN
   */
  public void setBaseDn(final String dn)
  {
    logger.trace("setting baseDn: {}", dn);
    baseDn = dn;
  }


  /**
   * Returns the filter used to search for the user.
   *
   * @return  filter for searching
   */
  public String getUserFilter()
  {
    return userFilter;
  }


  /**
   * Sets the filter used to search for the user.
   *
   * @param  filter  user filter
   */
  public void setUserFilter(final String filter)
  {
    logger.trace("setting userFilter: {}", filter);
    userFilter = filter;
  }


  /**
   * Returns the filter parameters used to search for the user.
   *
   * @return  filter parameters
   */
  public Object[] getUserFilterParameters()
  {
    return userFilterParameters;
  }


  /**
   * Sets the filter parameters used to search for the user.
   *
   * @param  filterParams  filter parameters
   */
  public void setUserFilterParameters(final Object[] filterParams)
  {
    logger.trace("setting userFilterParameters: {}", Arrays.toString(filterParams));
    userFilterParameters = filterParams;
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
   * Sets whether DN resolution should fail if multiple DNs are found. If false an exception will be thrown if {@link
   * #resolve(User)} finds more than one DN matching it's filter. Otherwise the first DN found is returned.
   *
   * @param  b  whether multiple DNs are allowed
   */
  public void setAllowMultipleDns(final boolean b)
  {
    logger.trace("setting allowMultipleDns: {}", b);
    allowMultipleDns = b;
  }


  /**
   * Returns whether subtree searching will be used.
   *
   * @return  whether the DN will be searched for over the entire base
   */
  public boolean getSubtreeSearch()
  {
    return subtreeSearch;
  }


  /**
   * Sets whether subtree searching will be used. If true, the DN used for authenticating will be searched for over the
   * entire {@link #getBaseDn()}. Otherwise the DN will be searched for in the {@link #getBaseDn()} context.
   *
   * @param  b  whether the DN will be searched for over the entire base
   */
  public void setSubtreeSearch(final boolean b)
  {
    logger.trace("setting subtreeSearch: {}", b);
    subtreeSearch = b;
  }


  /**
   * Returns how to dereference aliases.
   *
   * @return  how to dereference aliases
   */
  public DerefAliases getDerefAliases()
  {
    return derefAliases;
  }


  /**
   * Sets how to dereference aliases.
   *
   * @param  da  how to dereference aliases
   */
  public void setDerefAliases(final DerefAliases da)
  {
    logger.trace("setting derefAliases: {}", da);
    derefAliases = da;
  }


  /**
   * Attempts to find the DN for the supplied user. {@link #createFilterTemplate(User)} ()} is used to create the search
   * filter. If more than one entry matches the search, the result is controlled by {@link
   * #setAllowMultipleDns(boolean)}.
   *
   * @param  user  to find DN for
   *
   * @return  user DN
   *
   * @throws  LdapException  if the entry resolution fails
   */
  @Override
  public String resolve(final User user)
    throws LdapException
  {
    logger.debug("resolve user={}", user);

    String dn = null;
    if (user != null) {
      // create the filter template
      final FilterTemplate filter = createFilterTemplate(user);

      if (filter != null && filter.getFilter() != null) {
        final SearchResponse result = performLdapSearch(filter);
        if (!result.isSuccess()) {
          throw new LdapException(
            "Error resolving DN for user " + user + " with filter " + filter +
              ". Unsuccessful search response: " + result);
        }

        final Iterator<LdapEntry> answer = result.getEntries().iterator();

        // return first match, otherwise user doesn't exist
        if (answer != null && answer.hasNext()) {
          dn = resolveDn(answer.next());
          if (answer.hasNext()) {
            logger.debug("multiple results found for user={} using filter={}", user, filter);
            if (!allowMultipleDns) {
              throw new LdapException(
                "Found " + result.entrySize() + " DNs for " + user + " : " + result.getEntryDns());
            }
          }
        } else {
          logger.info("search for user={} failed using filter={}", user, filter);
        }
      } else {
        logger.error("DN filter template not found, no search performed");
      }
    } else {
      logger.warn("DN resolution cannot occur, user is null");
    }
    logger.debug("resolved dn={} for user={}", dn, user);
    return dn;
  }


  /**
   * Returns the DN for the supplied ldap entry.
   *
   * @param  entry  to retrieve the DN from
   *
   * @return  dn
   */
  protected String resolveDn(final LdapEntry entry)
  {
    return entry.getDn();
  }


  /**
   * Returns a filter template using {@link #userFilter} and {@link #userFilterParameters}. The user parameter is
   * injected as a named parameter of 'user'.
   *
   * @param  user  to resolve DN
   *
   * @return  filter template
   */
  protected FilterTemplate createFilterTemplate(final User user)
  {
    final FilterTemplate filter = new FilterTemplate();
    if (user != null && user.getIdentifier() != null && !"".equals(user.getIdentifier())) {
      if (userFilter != null) {
        logger.debug("searching for DN using userFilter");
        filter.setFilter(userFilter);
        if (userFilterParameters != null) {
          filter.setParameters(userFilterParameters);
        }
        // assign user as a named parameter
        filter.setParameter("user", user.getIdentifier());
        // assign context as a named parameter
        filter.setParameter("context", user.getContext());
      } else {
        logger.error("Invalid userFilter, cannot be null or empty.");
      }
    } else {
      logger.warn("Filter template cannot be created, user input was empty or null");
    }
    return filter;
  }


  /**
   * Returns a search request for searching for a single entry in an LDAP, returning no attributes.
   *
   * @param  template  to execute
   *
   * @return  search request
   */
  protected SearchRequest createSearchRequest(final FilterTemplate template)
  {
    return SearchRequest.builder()
      .dn(baseDn)
      .filter(template)
      .returnAttributes(ReturnAttributes.NONE.value())
      .scope(subtreeSearch ? SearchScope.SUBTREE : SearchScope.ONELEVEL)
      .aliases(derefAliases)
      .build();
  }


  /**
   * Executes the ldap search operation with the supplied filter.
   *
   * @param  template  to execute
   *
   * @return  ldap search result
   *
   * @throws  LdapException  if an error occurs
   */
  protected SearchResponse performLdapSearch(final FilterTemplate template)
    throws LdapException
  {
    final SearchRequest request = createSearchRequest(template);
    final SearchOperation op = createSearchOperation();
    return op.execute(request);
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("factory=").append(getConnectionFactory()).append(", ")
      .append("baseDn=").append(baseDn).append(", ")
      .append("userFilter=").append(userFilter).append(", ")
      .append("userFilterParameters=").append(Arrays.toString(userFilterParameters)).append(", ")
      .append("allowMultipleDns=").append(allowMultipleDns).append(", ")
      .append("subtreeSearch=").append(subtreeSearch).append(", ")
      .append("derefAliases=").append(derefAliases).append("]").toString();
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


  /** Search DN resolver builder. */
  public static class Builder
  {

    /** DN resolver to build. */
    private final SearchDnResolver object = new SearchDnResolver();


    /**
     * Default constructor.
     */
    protected Builder() {}


    /**
     * Sets the connection factory.
     *
     * @param  factory  connection factory
     *
     * @return  this builder
     */
    public Builder factory(final ConnectionFactory factory)
    {
      object.setConnectionFactory(factory);
      return this;
    }


    /**
     * Sets the base DN.
     *
     * @param  dn  base DN
     *
     * @return  this builder
     */
    public Builder dn(final String dn)
    {
      object.setBaseDn(dn);
      return this;
    }


    /**
     * Sets the user filter.
     *
     * @param  filter  suer filter
     *
     * @return  this builder
     */
    public Builder filter(final String filter)
    {
      object.setUserFilter(filter);
      return this;
    }


    /**
     * Sets the user filter parameters.
     *
     * @param  params  filter parameters
     *
     * @return  this builder
     */
    public Builder filterParameters(final Object... params)
    {
      object.setUserFilterParameters(params);
      return this;
    }


    /**
     * Sets whether to allow multiple DNs.
     *
     * @param  multipleDns  whether to allow multiple DNs
     *
     * @return  this builder
     */
    public Builder allowMultipleDns(final boolean multipleDns)
    {
      object.setAllowMultipleDns(multipleDns);
      return this;
    }


    /**
     * Sets whether to perform a subtree search or a onelevel search.
     *
     * @param  b  whether to perform a subtree search or a onelevel search
     *
     * @return  this builder
     */
    public Builder subtreeSearch(final boolean b)
    {
      object.setSubtreeSearch(b);
      return this;
    }


    /**
     * Sets the deref aliases flag.
     *
     * @param  aliases  deref aliases
     *
     * @return  this builder
     */
    public Builder aliases(final DerefAliases aliases)
    {
      object.setDerefAliases(aliases);
      return this;
    }


    /**
     * Returns the search DN resolver.
     *
     * @return  search DN resolver
     */
    public SearchDnResolver build()
    {
      return object;
    }
  }
}
