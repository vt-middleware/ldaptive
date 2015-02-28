/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.LdapURL;
import org.ldaptive.LdapUtils;
import org.ldaptive.Operation;
import org.ldaptive.Response;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchReference;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.handler.HandlerResult;

/**
 * Provides handling of an ldap referral for search operations. Injects a
 * {@link org.ldaptive.handler.SearchReferenceHandler} so that both referrals
 * and search references will be followed.
 *
 * @author  Middleware Services
 */
public class SearchReferralHandler
  extends AbstractReferralHandler<SearchRequest, SearchResult>
{


  /**
   * Creates a new search referral handler.
   */
  public SearchReferralHandler()
  {
    this(DEFAULT_REFERRAL_LIMIT, 0 , DEFAULT_CONNECTION_FACTORY);
  }


  /**
   * Creates a new search referral handler.
   *
   * @param  factory  referral connection factory
   */
  public SearchReferralHandler(final ReferralConnectionFactory factory)
  {
    this(DEFAULT_REFERRAL_LIMIT, 0, factory);
  }


  /**
   * Creates a new search referral handler.
   *
   * @param  limit  number of referrals to follow
   */
  public SearchReferralHandler(final int limit)
  {
    this(limit, 0, DEFAULT_CONNECTION_FACTORY);
  }


  /**
   * Creates a new search referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  factory  referral connection factory
   */
  public SearchReferralHandler(
    final int limit,
    final ReferralConnectionFactory factory)
  {
    this(limit, 0, factory);
  }


  /**
   * Creates a new search referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  depth  number of referrals followed
   * @param  factory  referral connection factory
   */
  private SearchReferralHandler(
    final int limit,
    final int depth,
    final ReferralConnectionFactory factory)
  {
    super(limit, depth, factory);
  }


  @Override
  protected SearchRequest createReferralRequest(
    final SearchRequest request,
    final LdapURL url)
  {
    final SearchRequest referralRequest = new SearchRequest();
    referralRequest.setControls(request.getControls());
    referralRequest.setIntermediateResponseHandlers(
      request.getIntermediateResponseHandlers());
    referralRequest.setReferralHandler(
      new SearchReferralHandler(
        getReferralLimit(),
        getReferralDepth() + 1,
        getReferralConnectionFactory()));

    if (!url.getEntry().isDefaultBaseDn()) {
      referralRequest.setBaseDn(url.getEntry().getBaseDn());
    } else {
      referralRequest.setBaseDn(request.getBaseDn());
    }
    referralRequest.setBinaryAttributes(request.getBinaryAttributes());
    referralRequest.setDerefAliases(request.getDerefAliases());
    referralRequest.setSearchEntryHandlers(request.getSearchEntryHandlers());
    final org.ldaptive.handler.SearchReferenceHandler[]
      searchReferenceHandlers = request.getSearchReferenceHandlers();
    if (searchReferenceHandlers != null) {
      for (int i = 0; i < searchReferenceHandlers.length; i++) {
        if (searchReferenceHandlers[i] instanceof SearchReferenceHandler) {
          final SearchReferenceHandler handler =
            (SearchReferenceHandler) searchReferenceHandlers[i];
          searchReferenceHandlers[i] = new SearchReferenceHandler(
            handler.getReferralLimit(),
            handler.getReferralDepth() + 1,
            handler.getReferralConnectionFactory());
        }
      }
    }
    referralRequest.setReturnAttributes(request.getReturnAttributes());
    if (!url.getEntry().isDefaultFilter()) {
      referralRequest.setSearchFilter(url.getEntry().getFilter());
    } else {
      referralRequest.setSearchFilter(request.getSearchFilter());
    }
    if (!url.getEntry().isDefaultScope()) {
      referralRequest.setSearchScope(url.getEntry().getScope());
    } else {
      referralRequest.setSearchScope(request.getSearchScope());
    }
    referralRequest.setSizeLimit(request.getSizeLimit());
    referralRequest.setSortBehavior(request.getSortBehavior());
    referralRequest.setTimeLimit(request.getTimeLimit());
    referralRequest.setTypesOnly(request.getTypesOnly());

    return referralRequest;
  }


  @Override
  protected Operation<SearchRequest, SearchResult> createReferralOperation(
    final Connection conn)
  {
    return new SearchOperation(conn);
  }


  @Override
  public void initializeRequest(final SearchRequest request)
  {
    if (request.getSearchReferenceHandlers() != null) {
      request.setSearchReferenceHandlers(
        LdapUtils.concatArrays(
          request.getSearchReferenceHandlers(),
          new org.ldaptive.handler.SearchReferenceHandler[] {
            new SearchReferenceHandler(),
          }));
    } else {
      request.setSearchReferenceHandlers(new SearchReferenceHandler());
    }
  }


  /**
   * Implementation of {@link org.ldaptive.handler.SearchReferenceHandler} that
   * delegates to {@link SearchReferralHandler}.
   */
  public static class SearchReferenceHandler
    implements org.ldaptive.handler.SearchReferenceHandler
  {

    /** Referral limit. */
    private final int referralLimit;

    /** Referral depth. */
    private final int referralDepth;

    /** Referral connection factory. */
    private final ReferralConnectionFactory connectionFactory;


    /**
     * Creates a new search reference handler.
     */
    public SearchReferenceHandler()
    {
      this(DEFAULT_REFERRAL_LIMIT, 0, DEFAULT_CONNECTION_FACTORY);
    }


    /**
     * Creates a new search reference handler.
     *
     * @param  factory  referral connection factory
     */
    public SearchReferenceHandler(final ReferralConnectionFactory factory)
    {
      this(DEFAULT_REFERRAL_LIMIT, 0, factory);
    }


    /**
     * Creates a new search reference handler.
     *
     * @param  limit  number of referrals to follow
     */
    public SearchReferenceHandler(final int limit)
    {
      this(limit, 0, DEFAULT_CONNECTION_FACTORY);
    }


    /**
     * Creates a new search reference handler.
     *
     * @param  limit  number of referrals to follow
     * @param  factory  referral connection factory
     */
    public SearchReferenceHandler(
      final int limit,
      final ReferralConnectionFactory factory)
    {
      this(limit, 0, factory);
    }


    /**
     * Creates a new search reference handler.
     *
     * @param  limit  number of referrals to follow
     * @param  depth  number of referrals followed
     * @param  factory  referral connection factory
     */
    private SearchReferenceHandler(
      final int limit,
      final int depth,
      final ReferralConnectionFactory factory)
    {
      referralLimit = limit;
      referralDepth = depth;
      connectionFactory = factory;
    }


    /**
     * Returns the maximum number of referrals to follow.
     *
     * @return  referral limit
     */
    public int getReferralLimit()
    {
      return referralLimit;
    }


    /**
     * Returns the referral depth of this handler.
     *
     * @return  referral depth
     */
    public int getReferralDepth()
    {
      return referralDepth;
    }


    /**
     * Returns the referral connection factory.
     *
     * @return  referral connection factory
     */
    public ReferralConnectionFactory getReferralConnectionFactory()
    {
      return connectionFactory;
    }


    @Override
    public HandlerResult<SearchReference> handle(
      final Connection conn,
      final SearchRequest request,
      final SearchReference reference)
      throws LdapException
    {
      final SearchReferralHandler handler = new SearchReferralHandler(
        referralLimit,
        referralDepth,
        connectionFactory);
      final HandlerResult<Response<SearchResult>> result = handler.handle(
        conn,
        request,
        reference.getReferralUrls());
      final Response<SearchResult> response = result.getResult();
      if (response != null) {
        reference.setReferenceResponse(response);
      }
      return new HandlerResult<>(reference);
    }


    @Override
    public void initializeRequest(final SearchRequest request) {}
  }
}
