/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import java.util.Iterator;
import org.ldaptive.Connection;
import org.ldaptive.DerefAliases;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.SearchScope;

/**
 * Base implementation for search dn resolvers.
 *
 * @author  Middleware Services
 */
public abstract class AbstractSearchDnResolver
  extends AbstractSearchOperationFactory implements DnResolver
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
  private DerefAliases derefAliases;

  /** Whether to follow referrals. */
  private boolean followReferrals;


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
    logger.trace(
      "setting userFilterParameters: {}",
      Arrays.toString(filterParams));
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
   * Sets whether DN resolution should fail if multiple DNs are found. If false
   * an exception will be thrown if {@link #resolve(String)} finds more than one
   * DN matching it's filter. Otherwise the first DN found is returned.
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
   * Sets whether subtree searching will be used. If true, the DN used for
   * authenticating will be searched for over the entire {@link #getBaseDn()}.
   * Otherwise the DN will be searched for in the {@link #getBaseDn()} context.
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
   * Returns whether to follow referrals.
   *
   * @return  whether to follow referrals
   */
  public boolean getFollowReferrals()
  {
    return followReferrals;
  }


  /**
   * Sets whether to follow referrals.
   *
   * @param  b  whether to follow referrals
   */
  public void setFollowReferrals(final boolean b)
  {
    logger.trace("setting followReferrals: {}", b);
    followReferrals = b;
  }


  /**
   * Attempts to find the DN for the supplied user. {@link #getUserFilter()} is
   * used to look up the DN. The user is provided as the 'user' variable filter
   * parameter. If more than one entry matches the search, the result is
   * controlled by {@link #setAllowMultipleDns(boolean)}.
   *
   * @param  user  to find DN for
   *
   * @return  user DN
   *
   * @throws  LdapException  if the entry resolution fails
   */
  @Override
  public String resolve(final String user)
    throws LdapException
  {
    logger.debug("resolve user={}", user);

    String dn = null;
    if (user != null && !"".equals(user)) {
      // create the search filter
      final SearchFilter filter = createSearchFilter(user);

      if (filter.getFilter() != null) {
        final SearchResult result = performLdapSearch(filter);
        final Iterator<LdapEntry> answer = result.getEntries().iterator();

        // return first match, otherwise user doesn't exist
        if (answer != null && answer.hasNext()) {
          dn = resolveDn(answer.next());
          if (answer.hasNext()) {
            logger.debug(
              "multiple results found for user={} using filter={}",
              user,
              filter);
            if (!allowMultipleDns) {
              throw new LdapException("Found more than (1) DN for: " + user);
            }
          }
        } else {
          logger.info(
            "search for user={} failed using filter={}",
            user,
            filter);
        }
      } else {
        logger.error("DN search filter not found, no search performed");
      }
    } else {
      logger.warn("DN resolution cannot occur, user input was empty or null");
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
   * Returns a search filter using {@link #userFilter} and {@link
   * #userFilterParameters}. The user parameter is injected as a named parameter
   * of 'user'.
   *
   * @param  user  identifier
   *
   * @return  search filter
   */
  protected SearchFilter createSearchFilter(final String user)
  {
    final SearchFilter filter = new SearchFilter();
    if (userFilter != null) {
      logger.debug("searching for DN using userFilter");
      filter.setFilter(userFilter);
      if (userFilterParameters != null) {
        filter.setParameters(userFilterParameters);
      }
      // assign user as a named parameter
      filter.setParameter("user", user);
    } else {
      logger.error("Invalid userFilter, cannot be null or empty.");
    }
    return filter;
  }


  /**
   * Returns a search request for searching for a single entry in an LDAP,
   * returning no attributes.
   *
   * @param  filter  to execute
   *
   * @return  search request
   */
  protected SearchRequest createSearchRequest(final SearchFilter filter)
  {
    final SearchRequest request = new SearchRequest();
    request.setBaseDn(baseDn);
    request.setSearchFilter(filter);
    request.setReturnAttributes(ReturnAttributes.NONE.value());
    if (subtreeSearch) {
      request.setSearchScope(SearchScope.SUBTREE);
    } else {
      request.setSearchScope(SearchScope.ONELEVEL);
    }
    request.setDerefAliases(derefAliases);
    request.setFollowReferrals(followReferrals);
    return request;
  }


  /**
   * Executes the ldap search operation with the supplied filter.
   *
   * @param  filter  to execute
   *
   * @return  ldap search result
   *
   * @throws  LdapException  if an error occurs
   */
  protected SearchResult performLdapSearch(final SearchFilter filter)
    throws LdapException
  {
    final SearchRequest request = createSearchRequest(filter);
    try (Connection conn = getConnection()) {
      final SearchOperation op = createSearchOperation(conn);
      return op.execute(request).getResult();
    }
  }


  /**
   * Retrieve a connection that is ready for use.
   *
   * @return  connection
   *
   * @throws  LdapException  if an error occurs opening the connection
   */
  protected abstract Connection getConnection()
    throws LdapException;
}
