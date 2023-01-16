/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import java.util.Iterator;
import org.ldaptive.AbstractSearchOperationFactory;
import org.ldaptive.DerefAliases;
import org.ldaptive.FilterTemplate;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.SearchScope;

/**
 * Base implementation for search entry resolvers. Uses an object level search on the {@link
 * AuthenticationCriteria#getDn()} if no {@link #userFilter} is configured. If a {@link #userFilter} is configured, then
 * a search is executed using that filter.
 *
 * @author  Middleware Services
 */
public abstract class AbstractSearchEntryResolver extends AbstractSearchOperationFactory implements EntryResolver
{

  /** DN to search. */
  private String baseDn = "";

  /** Filter for searching for the user. */
  private String userFilter;

  /** Filter parameters for searching for the user. */
  private Object[] userFilterParameters;

  /** Whether to throw an exception if multiple entries are found. */
  private boolean allowMultipleEntries;

  /** Whether to use a subtree search when resolving DNs. */
  private boolean subtreeSearch;

  /** How to handle aliases. */
  private DerefAliases derefAliases = DerefAliases.NEVER;

  /** Binary attribute names. */
  private String[] binaryAttributes;


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
   * @param  filter  for searching
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
   * Returns whether entry resolution should fail if multiple entries are found.
   *
   * @return  whether an exception will be thrown if multiple entries are found
   */
  public boolean getAllowMultipleEntries()
  {
    return allowMultipleEntries;
  }


  /**
   * Sets whether entry resolution should fail if multiple entries are found. If false an exception will be thrown if
   * {@link #resolve(AuthenticationCriteria, AuthenticationHandlerResponse)} finds more than one entry matching its
   * filter. Otherwise, the first entry found is returned.
   *
   * @param  b  whether multiple entries are allowed
   */
  public void setAllowMultipleEntries(final boolean b)
  {
    logger.trace("setting allowMultipleEntries: {}", b);
    allowMultipleEntries = b;
  }


  /**
   * Returns whether subtree searching will be used.
   *
   * @return  whether the entry will be searched for over the entire base
   */
  public boolean getSubtreeSearch()
  {
    return subtreeSearch;
  }


  /**
   * Sets whether subtree searching will be used. If true, the entry will be searched for over the entire {@link
   * #getBaseDn()}. Otherwise the entry will be searched for in the {@link #getBaseDn()} context.
   *
   * @param  b  whether the entry will be searched for over the entire base
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
   * Returns names of binary attributes.
   *
   * @return  binary attribute names
   */
  public String[] getBinaryAttributes()
  {
    return binaryAttributes;
  }


  /**
   * Sets names of binary attributes.
   *
   * @param  attrs  binary attribute names
   */
  public void setBinaryAttributes(final String... attrs)
  {
    logger.trace("setting binaryAttributes: {}", Arrays.toString(attrs));
    binaryAttributes = attrs;
  }


  /**
   * Executes an ldap search with the supplied authentication criteria.
   *
   * @param  criteria  authentication criteria associated with the user
   * @param  response  response from the authentication event
   *
   * @return  search result
   *
   * @throws  LdapException  if an error occurs attempting the search
   */
  protected abstract SearchResponse performLdapSearch(
    AuthenticationCriteria criteria,
    AuthenticationHandlerResponse response)
    throws LdapException;


  /**
   * Returns a filter template using {@link #userFilter} and {@link #userFilterParameters}. {@link User#getIdentifier()}
   * is injected with a named parameter of 'user', {@link User#getContext()} is injected with a named parameter of
   * 'context', and {@link AuthenticationCriteria#getDn()} is injected with a named parameter of 'dn'.
   *
   * @param  ac  authentication criteria
   *
   * @return  filter template
   */
  protected FilterTemplate createFilterTemplate(final AuthenticationCriteria ac)
  {
    final FilterTemplate filter = new FilterTemplate();
    if (userFilter != null) {
      logger.debug("Searching for entry using userFilter");
      filter.setFilter(userFilter);
      if (userFilterParameters != null) {
        filter.setParameters(userFilterParameters);
      }
      // assign named parameters
      filter.setParameter("user", ac.getAuthenticationRequest().getUser().getIdentifier());
      filter.setParameter("context", ac.getAuthenticationRequest().getUser().getContext());
      filter.setParameter("dn", ac.getDn());
    } else {
      logger.error("Invalid userFilter, cannot be null.");
    }
    return filter;
  }


  /**
   * Returns a search request for the supplied authentication criteria. If no {@link #userFilter} is defined then an
   * object level search on the authentication criteria DN is returned. Otherwise the {@link #userFilter}, {@link
   * #baseDn} and {@link #subtreeSearch} are used to create the search request.
   *
   * @param  ac  authentication criteria containing a DN
   *
   * @return  search request
   */
  protected SearchRequest createSearchRequest(final AuthenticationCriteria ac)
  {
    final SearchRequest request;
    if (userFilter != null) {
      request = SearchRequest.builder()
        .dn(baseDn)
        .filter(createFilterTemplate(ac))
        .returnAttributes(ac.getAuthenticationRequest().getReturnAttributes())
        .scope(subtreeSearch ? SearchScope.SUBTREE : SearchScope.ONELEVEL)
        .build();
    } else {
      request = SearchRequest.objectScopeSearchRequest(
        ac.getDn(),
        ac.getAuthenticationRequest().getReturnAttributes());
    }
    request.setDerefAliases(derefAliases);
    request.setBinaryAttributes(binaryAttributes);
    return request;
  }


  @Override
  public LdapEntry resolve(final AuthenticationCriteria criteria, final AuthenticationHandlerResponse response)
    throws LdapException
  {
    logger.trace("resolve criteria={}", criteria);

    final SearchResponse result = performLdapSearch(criteria, response);
    logger.debug("Resolved result={} for criteria={}", result, criteria);

    if (!result.isSuccess()) {
      throw new LdapException(
        "Error resolving entry for " + criteria.getDn() + ". Unsuccessful search response: " + result);
    }

    LdapEntry entry = null;
    final Iterator<LdapEntry> answer = result.getEntries().iterator();
    if (answer != null && answer.hasNext()) {
      entry = answer.next();
      if (answer.hasNext()) {
        logger.debug("Multiple results found for user={}", criteria.getDn());
        if (!allowMultipleEntries) {
          throw new LdapException("Found more than (1) entry for: " + criteria.getDn());
        }
      }
    }
    return entry;
  }
}
