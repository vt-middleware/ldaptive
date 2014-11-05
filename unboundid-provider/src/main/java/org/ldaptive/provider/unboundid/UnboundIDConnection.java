/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.unboundid;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.ldap.sdk.AsyncRequestID;
import com.unboundid.ldap.sdk.AsyncSearchResultListener;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.CRAMMD5BindRequest;
import com.unboundid.ldap.sdk.CompareResult;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.DIGESTMD5BindRequest;
import com.unboundid.ldap.sdk.DIGESTMD5BindRequestProperties;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.DereferencePolicy;
import com.unboundid.ldap.sdk.DisconnectHandler;
import com.unboundid.ldap.sdk.DisconnectType;
import com.unboundid.ldap.sdk.EXTERNALBindRequest;
import com.unboundid.ldap.sdk.ExtendedResult;
import com.unboundid.ldap.sdk.GSSAPIBindRequest;
import com.unboundid.ldap.sdk.GSSAPIBindRequestProperties;
import com.unboundid.ldap.sdk.IntermediateResponse;
import com.unboundid.ldap.sdk.IntermediateResponseListener;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.SASLBindRequest;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchResultListener;
import com.unboundid.ldap.sdk.SearchResultReference;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import com.unboundid.ldap.sdk.UnsolicitedNotificationHandler;
import org.ldaptive.AddRequest;
import org.ldaptive.BindRequest;
import org.ldaptive.CompareRequest;
import org.ldaptive.DeleteRequest;
import org.ldaptive.DerefAliases;
import org.ldaptive.LdapException;
import org.ldaptive.ModifyDnRequest;
import org.ldaptive.ModifyRequest;
import org.ldaptive.Request;
import org.ldaptive.Response;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchEntry;
import org.ldaptive.SearchReference;
import org.ldaptive.async.AsyncRequest;
import org.ldaptive.control.RequestControl;
import org.ldaptive.control.ResponseControl;
import org.ldaptive.extended.ExtendedRequest;
import org.ldaptive.extended.ExtendedResponse;
import org.ldaptive.extended.ExtendedResponseFactory;
import org.ldaptive.extended.UnsolicitedNotificationListener;
import org.ldaptive.intermediate.IntermediateResponseFactory;
import org.ldaptive.provider.ProviderConnection;
import org.ldaptive.provider.ProviderUtils;
import org.ldaptive.provider.SearchItem;
import org.ldaptive.provider.SearchIterator;
import org.ldaptive.provider.SearchListener;
import org.ldaptive.sasl.SaslConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unbound ID provider implementation of ldap operations.
 *
 * @author  Middleware Services
 * @version  $Revision: 2944 $ $Date: 2014-03-31 13:58:44 -0400 (Mon, 31 Mar 2014) $
 */
public class UnboundIDConnection implements ProviderConnection
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Ldap connection. */
  private LDAPConnection connection;

  /** Provider configuration. */
  private final UnboundIDProviderConfig config;

  /** Receives unsolicited notifications. */
  private final AggregateUnsolicitedNotificationHandler notificationHandler =
    new AggregateUnsolicitedNotificationHandler();

  /** Receives disconnect notifications. */
  private final AggregateDisconnectHandler disconnectHandler =
    new AggregateDisconnectHandler();


  /**
   * Creates a new unboundid ldap connection.
   *
   * @param  lc  ldap connection
   * @param  pc  provider configuration
   */
  public UnboundIDConnection(
    final LDAPConnection lc,
    final UnboundIDProviderConfig pc)
  {
    connection = lc;
    config = pc;

    final LDAPConnectionOptions options = connection.getConnectionOptions();
    if (options.getUnsolicitedNotificationHandler() == null) {
      options.setUnsolicitedNotificationHandler(notificationHandler);
    }
    if (options.getDisconnectHandler() == null) {
      options.setDisconnectHandler(disconnectHandler);
    }
  }


  /**
   * Returns the underlying ldap connection.
   *
   * @return  ldap connection
   */
  public LDAPConnection getLdapConnection()
  {
    return connection;
  }


  /** {@inheritDoc} */
  @Override
  public void close(final RequestControl[] controls)
    throws LdapException
  {
    if (connection != null) {
      try {
        connection.close(
          config.getControlProcessor().processRequestControls(controls));
      } finally {
        connection = null;
      }
    }
  }


  /** {@inheritDoc} */
  @Override
  public Response<Void> bind(final BindRequest request)
    throws LdapException
  {
    Response<Void> response;
    if (request.getSaslConfig() != null) {
      response = saslBind(request);
    } else if (request.getDn() == null && request.getCredential() == null) {
      response = anonymousBind(request);
    } else {
      response = simpleBind(request);
    }
    return response;
  }


  /**
   * Performs an anonymous bind.
   *
   * @param  request  to bind with
   *
   * @return  bind response
   *
   * @throws  LdapException  if an error occurs
   */
  protected Response<Void> anonymousBind(final BindRequest request)
    throws LdapException
  {
    Response<Void> response = null;
    try {
      SimpleBindRequest sbr;
      if (request.getControls() != null) {
        sbr = new SimpleBindRequest(
          "",
          new byte[0],
          config.getControlProcessor().processRequestControls(
            request.getControls()));
      } else {
        sbr = new SimpleBindRequest();
      }
      sbr.setFollowReferrals(request.getFollowReferrals());

      final BindResult result = connection.bind(sbr);
      response = createResponse(request, null, result);
    } catch (LDAPException e) {
      processLDAPException(request, e);
    }
    return response;
  }


  /**
   * Performs a simple bind.
   *
   * @param  request  to bind with
   *
   * @return  bind response
   *
   * @throws  LdapException  if an error occurs
   */
  protected Response<Void> simpleBind(final BindRequest request)
    throws LdapException
  {
    Response<Void> response = null;
    try {
      SimpleBindRequest sbr;
      if (request.getControls() != null) {
        sbr = new SimpleBindRequest(
          new DN(request.getDn()),
          request.getCredential().getBytes(),
          config.getControlProcessor().processRequestControls(
            request.getControls()));
      } else {
        sbr = new SimpleBindRequest(
          request.getDn(),
          request.getCredential().getBytes());
      }
      sbr.setFollowReferrals(request.getFollowReferrals());

      final BindResult result = connection.bind(sbr);
      response = createResponse(request, null, result);
    } catch (LDAPException e) {
      processLDAPException(request, e);
    }
    return response;
  }


  /**
   * Performs a sasl bind.
   *
   * @param  request  to bind with
   *
   * @return  bind response
   *
   * @throws  LdapException  if an error occurs
   */
  protected Response<Void> saslBind(final BindRequest request)
    throws LdapException
  {
    Response<Void> response = null;
    try {
      SASLBindRequest sbr;
      final SaslConfig sc = request.getSaslConfig();
      switch (sc.getMechanism()) {

      case EXTERNAL:
        sbr = new EXTERNALBindRequest(
          sc.getAuthorizationId(),
          config.getControlProcessor().processRequestControls(
            request.getControls()));
        break;

      case DIGEST_MD5:

        final DIGESTMD5BindRequestProperties digestMd5Props =
          UnboundIDSaslUtils.createDigestMd5Properties(request.getDn(),
                                                       request.getCredential(),
                                                       request.getSaslConfig());
        sbr = new DIGESTMD5BindRequest(
          digestMd5Props,
          config.getControlProcessor().processRequestControls(
            request.getControls()));
        break;

      case CRAM_MD5:
        sbr = new CRAMMD5BindRequest(
          request.getDn(),
          request.getCredential() != null ? request.getCredential().getBytes()
                                          : null,
          config.getControlProcessor().processRequestControls(
            request.getControls()));
        break;

      case GSSAPI:

        final GSSAPIBindRequestProperties props =
          UnboundIDSaslUtils.createGssApiProperties(request.getDn(),
                                                    request.getCredential(),
                                                    request.getSaslConfig());
        sbr = new GSSAPIBindRequest(
          props,
          config.getControlProcessor().processRequestControls(
            request.getControls()));
        break;

      default:
        throw new IllegalArgumentException(
          "Unknown SASL authentication mechanism: " + sc.getMechanism());
      }

      sbr.setFollowReferrals(request.getFollowReferrals());

      final BindResult result = connection.bind(sbr);
      response = createResponse(request, null, result);
    } catch (LDAPException e) {
      processLDAPException(request, e);
    }
    return response;
  }


  /** {@inheritDoc} */
  @Override
  public Response<Void> add(final AddRequest request)
    throws LdapException
  {
    Response<Void> response = null;
    try {
      final UnboundIDUtils util = new UnboundIDUtils();
      final com.unboundid.ldap.sdk.AddRequest ar =
        new com.unboundid.ldap.sdk.AddRequest(
          new DN(request.getDn()),
          util.fromLdapAttributes(request.getLdapAttributes()),
          config.getControlProcessor().processRequestControls(
            request.getControls()));
      ar.setFollowReferrals(request.getFollowReferrals());

      final LDAPResult result = connection.add(ar);
      response = createResponse(request, null, result);
    } catch (LDAPException e) {
      processLDAPException(request, e);
    }
    return response;
  }


  /** {@inheritDoc} */
  @Override
  public Response<Boolean> compare(final CompareRequest request)
    throws LdapException
  {
    Response<Boolean> response = null;
    try {
      com.unboundid.ldap.sdk.CompareRequest cr;
      if (request.getAttribute().isBinary()) {
        cr = new com.unboundid.ldap.sdk.CompareRequest(
          new DN(request.getDn()),
          request.getAttribute().getName(),
          request.getAttribute().getBinaryValue(),
          config.getControlProcessor().processRequestControls(
            request.getControls()));
      } else {
        cr = new com.unboundid.ldap.sdk.CompareRequest(
          new DN(request.getDn()),
          request.getAttribute().getName(),
          request.getAttribute().getStringValue(),
          config.getControlProcessor().processRequestControls(
            request.getControls()));
      }
      cr.setFollowReferrals(request.getFollowReferrals());

      final CompareResult result = connection.compare(cr);
      response = createResponse(request, result.compareMatched(), result);
    } catch (LDAPException e) {
      processLDAPException(request, e);
    }
    return response;
  }


  /** {@inheritDoc} */
  @Override
  public Response<Void> delete(final DeleteRequest request)
    throws LdapException
  {
    Response<Void> response = null;
    try {
      final com.unboundid.ldap.sdk.DeleteRequest dr =
        new com.unboundid.ldap.sdk.DeleteRequest(
          new DN(request.getDn()),
          config.getControlProcessor().processRequestControls(
            request.getControls()));
      dr.setFollowReferrals(request.getFollowReferrals());

      final LDAPResult result = connection.delete(dr);
      response = createResponse(request, null, result);
    } catch (LDAPException e) {
      processLDAPException(request, e);
    }
    return response;
  }


  /** {@inheritDoc} */
  @Override
  public Response<Void> modify(final ModifyRequest request)
    throws LdapException
  {
    Response<Void> response = null;
    try {
      final UnboundIDUtils bu = new UnboundIDUtils();
      final com.unboundid.ldap.sdk.ModifyRequest mr =
        new com.unboundid.ldap.sdk.ModifyRequest(
          new DN(request.getDn()),
          bu.fromAttributeModification(request.getAttributeModifications()),
          config.getControlProcessor().processRequestControls(
            request.getControls()));
      mr.setFollowReferrals(request.getFollowReferrals());

      final LDAPResult result = connection.modify(mr);
      response = createResponse(request, null, result);
    } catch (LDAPException e) {
      processLDAPException(request, e);
    }
    return response;
  }


  /** {@inheritDoc} */
  @Override
  public Response<Void> modifyDn(final ModifyDnRequest request)
    throws LdapException
  {
    Response<Void> response = null;
    try {
      final DN dn = new DN(request.getDn());
      final DN newDn = new DN(request.getNewDn());
      final com.unboundid.ldap.sdk.ModifyDNRequest mdr =
        new com.unboundid.ldap.sdk.ModifyDNRequest(
          dn,
          newDn.getRDN(),
          request.getDeleteOldRDn(),
          newDn.getParent(),
          config.getControlProcessor().processRequestControls(
            request.getControls()));
      mdr.setFollowReferrals(request.getFollowReferrals());

      final LDAPResult result = connection.modifyDN(mdr);
      response = createResponse(request, null, result);
    } catch (LDAPException e) {
      processLDAPException(request, e);
    }
    return response;
  }


  /** {@inheritDoc} */
  @Override
  public SearchIterator search(final org.ldaptive.SearchRequest request)
    throws LdapException
  {
    final UnboundIDSearchIterator i = new UnboundIDSearchIterator(request);
    i.initialize();
    return i;
  }


  /** {@inheritDoc} */
  @Override
  public void searchAsync(
    final org.ldaptive.SearchRequest request,
    final SearchListener listener)
    throws LdapException
  {
    final UnboundIDAsyncSearchListener l = new UnboundIDAsyncSearchListener(
      request,
      listener);
    l.initialize();
  }


  /** {@inheritDoc} */
  @Override
  public void abandon(final int messageId, final RequestControl[] controls)
    throws LdapException
  {
    throw new UnsupportedOperationException(
      "Abandons via messageId not supported, use AsyncRequest instead");
  }


  /** {@inheritDoc} */
  @Override
  public Response<?> extendedOperation(final ExtendedRequest request)
    throws LdapException
  {
    Response<?> response = null;
    try {
      com.unboundid.ldap.sdk.ExtendedRequest er;
      final byte[] requestBerValue = request.encode();
      if (requestBerValue == null) {
        er = new com.unboundid.ldap.sdk.ExtendedRequest(
          request.getOID(),
          config.getControlProcessor().processRequestControls(
            request.getControls()));
      } else {
        er = new com.unboundid.ldap.sdk.ExtendedRequest(
          request.getOID(),
          new ASN1OctetString(requestBerValue),
          config.getControlProcessor().processRequestControls(
            request.getControls()));
      }
      er.setFollowReferrals(request.getFollowReferrals());

      final ExtendedResult result = connection.processExtendedOperation(er);
      final byte[] responseBerValue = result.getValue() != null
        ? result.getValue().getValue() : null;
      final ExtendedResponse<?> extRes =
        ExtendedResponseFactory.createExtendedResponse(
          request.getOID(),
          result.getOID(),
          responseBerValue);
      response = createResponse(request, extRes.getValue(), result);
    } catch (LDAPException e) {
      processLDAPException(request, e);
    }
    return response;
  }


  /** {@inheritDoc} */
  @Override
  public void addUnsolicitedNotificationListener(
    final UnsolicitedNotificationListener listener)
  {
    notificationHandler.addUnsolicitedNotificationListener(listener);
  }


  /** {@inheritDoc} */
  @Override
  public void removeUnsolicitedNotificationListener(
    final UnsolicitedNotificationListener listener)
  {
    notificationHandler.removeUnsolicitedNotificationListener(listener);
  }


  /**
   * Creates an operation response with the supplied response data.
   *
   * @param  <T>  type of response
   * @param  request  containing controls
   * @param  result  of the operation
   * @param  ldapResult  provider result
   *
   * @return  operation response
   */
  protected <T> Response<T> createResponse(
    final Request request,
    final T result,
    final LDAPResult ldapResult)
  {
    return
      new Response<>(
        result,
        ResultCode.valueOf(ldapResult.getResultCode().intValue()),
        ldapResult.getDiagnosticMessage(),
        ldapResult.getMatchedDN(),
        config.getControlProcessor().processResponseControls(
          ldapResult.getResponseControls()),
        ldapResult.getReferralURLs(),
        ldapResult.getMessageID());
  }


  /**
   * Determines if the supplied ldap exception should result in an operation
   * retry.
   *
   * @param  request  that produced the exception
   * @param  e  that was produced
   *
   * @throws  LdapException  wrapping the ldap exception
   */
  protected void processLDAPException(
    final Request request,
    final LDAPException e)
    throws LdapException
  {
    ProviderUtils.throwOperationException(
      config.getOperationExceptionResultCodes(),
      e,
      e.getResultCode().intValue(),
      e.getMatchedDN(),
      config.getControlProcessor().processResponseControls(
        e.getResponseControls()),
      e.getReferralURLs(),
      true);
  }


  /** Search iterator for unbound id search results. */
  protected class UnboundIDSearchIterator extends AbstractUnboundIDSearch
    implements SearchIterator
  {

    /** Response data. */
    private org.ldaptive.Response<Void> response;

    /** Search result iterator. */
    private SearchResultIterator resultIterator;


    /**
     * Creates a new unbound id search iterator.
     *
     * @param  sr  search request
     */
    public UnboundIDSearchIterator(final org.ldaptive.SearchRequest sr)
    {
      super(sr);
    }


    /**
     * Initializes this unbound id search iterator.
     *
     * @throws  org.ldaptive.LdapException  if an error occurs
     */
    public void initialize()
      throws org.ldaptive.LdapException
    {
      resultIterator = search(connection, request);
    }


    /**
     * Executes an ldap search.
     *
     * @param  conn  to search with
     * @param  sr  to read properties from
     *
     * @return  ldap search results
     *
     * @throws  LdapException  if an error occurs
     */
    protected SearchResultIterator search(
      final LDAPConnection conn,
      final org.ldaptive.SearchRequest sr)
      throws LdapException
    {
      final SearchResultIterator i = new SearchResultIterator();
      try {
        final SearchRequest unboundIdSr = getSearchRequest(sr, i, i);
        final Control[] c = config.getControlProcessor().processRequestControls(
          sr.getControls());
        unboundIdSr.addControls(c);
        logger.debug("performing search: {}", unboundIdSr);

        final SearchResult result = conn.search(unboundIdSr);
        response = createResponse(request, null, result);
        logger.debug("created response: {}", response);
      } catch (LDAPSearchException e) {
        final ResultCode rc = ignoreSearchException(
          config.getSearchIgnoreResultCodes(),
          e);
        if (rc == null) {
          processLDAPException(sr, e);
        }
        response = createResponse(
          request,
          null,
          new SearchResult(
            -1,
            e.getResultCode(),
            e.getDiagnosticMessage(),
            e.getMatchedDN(),
            e.getReferralURLs(),
            e.getEntryCount(),
            e.getReferenceCount(),
            e.getResponseControls()));
        logger.debug("created response: {}", response);
      } catch (LDAPException e) {
        processLDAPException(sr, e);
      }
      return i;
    }


    /** {@inheritDoc} */
    @Override
    public boolean hasNext()
      throws org.ldaptive.LdapException
    {
      return resultIterator != null && resultIterator.hasNext();
    }


    /** {@inheritDoc} */
    @Override
    public SearchItem next()
      throws org.ldaptive.LdapException
    {
      return resultIterator.next();
    }


    /** {@inheritDoc} */
    @Override
    public org.ldaptive.Response<Void> getResponse()
    {
      return response;
    }


    /** {@inheritDoc} */
    @Override
    public void close()
      throws LdapException {}


    /**
     * Search results listener for storing entries returned by the search
     * operation.
     */
    protected class SearchResultIterator
      implements SearchResultListener, IntermediateResponseListener
    {

      /** Search items. */
      protected final Queue<SearchItem> queue = new ArrayDeque<>();


      /**
       * Returns the next search item from the queue.
       *
       * @return  search result entry
       */
      public SearchItem next()
      {
        return queue.poll();
      }


      /**
       * Whether the queue is empty.
       *
       * @return  whether the queue is empty
       */
      public boolean hasNext()
      {
        return !queue.isEmpty();
      }


      /** {@inheritDoc} */
      @Override
      public void searchEntryReturned(final SearchResultEntry entry)
      {
        queue.add(processSearchResultEntry(entry));
      }


      /** {@inheritDoc} */
      @Override
      public void searchReferenceReturned(final SearchResultReference ref)
      {
        queue.add(processSearchResultReference(ref));
      }


      /** {@inheritDoc} */
      @Override
      public void intermediateResponseReturned(final IntermediateResponse res)
      {
        queue.add(processIntermediateResponse(res));
      }
    }
  }


  /** Search listener for unbound id async search results. */
  protected class UnboundIDAsyncSearchListener extends AbstractUnboundIDSearch
    implements AsyncSearchResultListener, IntermediateResponseListener
  {

    /** Search result listener. */
    private final SearchListener listener;

    /** Request ID of the search operation. */
    private AsyncRequestID requestID;

    /** Receives disconnect notifications for this async operation. */
    private final DisconnectHandler handler;


    /**
     * Creates a new unbound id search listener.
     *
     * @param  sr  search request
     * @param  sl  search listener
     */
    public UnboundIDAsyncSearchListener(
      final org.ldaptive.SearchRequest sr,
      final SearchListener sl)
    {
      super(sr);
      listener = sl;
      handler = new DisconnectHandler() {
        @Override
        public void handleDisconnect(
          final LDAPConnection ldapConnection,
          final String host,
          final int port,
          final DisconnectType disconnectType,
          final String message,
          final Throwable throwable)
        {
          listener.exceptionReceived(new Exception(message, throwable));
          disconnectHandler.removeDisconnectHandler(this);
        }
      };
    }


    /**
     * Returns the request ID.
     *
     * @return  request ID
     */
    public AsyncRequestID getRequestID()
    {
      return requestID;
    }


    /**
     * Initializes this unbound id search listener.
     *
     * @throws  LdapException  if an error occurs
     */
    public void initialize()
      throws LdapException
    {
      search(connection, request);
    }


    /**
     * Executes an ldap search.
     *
     * @param  conn  to search with
     * @param  sr  to read properties from
     *
     * @throws  LdapException  if an error occurs
     */
    protected void search(
      final LDAPConnection conn,
      final org.ldaptive.SearchRequest sr)
      throws LdapException
    {
      try {
        final SearchRequest unboundIdSr = getSearchRequest(sr, this, this);
        final Control[] c = config.getControlProcessor().processRequestControls(
          sr.getControls());
        unboundIdSr.addControls(c);
        logger.debug("performing search: {}", unboundIdSr);
        requestID = conn.asyncSearch(unboundIdSr);
        disconnectHandler.addDisconnectHandler(handler);
        listener.asyncRequestReceived(new UnboundIDAsyncRequest(requestID, sr));
      } catch (LDAPSearchException e) {
        final ResultCode rc = ignoreSearchException(
          config.getSearchIgnoreResultCodes(),
          e);
        if (rc == null) {
          processLDAPException(sr, e);
        }
      } catch (LDAPException e) {
        processLDAPException(sr, e);
      }
    }


    /** {@inheritDoc} */
    @Override
    public void searchEntryReturned(final SearchResultEntry entry)
    {
      listener.searchItemReceived(processSearchResultEntry(entry));
    }


    /** {@inheritDoc} */
    @Override
    public void searchReferenceReturned(final SearchResultReference ref)
    {
      listener.searchItemReceived(processSearchResultReference(ref));
    }


    /** {@inheritDoc} */
    @Override
    public void intermediateResponseReturned(final IntermediateResponse res)
    {
      listener.searchItemReceived(processIntermediateResponse(res));
    }


    /** {@inheritDoc} */
    @Override
    public void searchResultReceived(
      final AsyncRequestID id,
      final SearchResult res)
    {
      logger.trace("reading result: {}", res);

      disconnectHandler.removeDisconnectHandler(handler);

      final org.ldaptive.Response<Void> response = createResponse(
        request,
        null,
        res);
      listener.responseReceived(response);
    }
  }


  /** Common search functionality for unbound id iterators and listeners. */
  protected abstract class AbstractUnboundIDSearch
  {

    /** Search request. */
    protected final org.ldaptive.SearchRequest request;

    /** Utility class. */
    protected final UnboundIDUtils util;


    /**
     * Creates a new abstract unbound id search.
     *
     * @param  sr  search request
     */
    public AbstractUnboundIDSearch(final org.ldaptive.SearchRequest sr)
    {
      request = sr;
      util = new UnboundIDUtils(request.getSortBehavior());
      util.setBinaryAttributes(request.getBinaryAttributes());
    }


    /**
     * Returns an unbound id search request object configured with the supplied
     * search request.
     *
     * @param  sr  search request containing configuration to create unbound id
     * search request
     * @param  srListener  search result listener
     * @param  irListener  intermediate response listener
     *
     * @return  search request
     *
     * @throws  LDAPSearchException  if the search request cannot be initialized
     */
    protected SearchRequest getSearchRequest(
      final org.ldaptive.SearchRequest sr,
      final SearchResultListener srListener,
      final IntermediateResponseListener irListener)
      throws LDAPSearchException
    {
      try {
        final SearchRequest req = new SearchRequest(
          srListener,
          sr.getBaseDn(),
          getSearchScope(sr.getSearchScope()),
          getDereferencePolicy(sr.getDerefAliases()),
          (int) sr.getSizeLimit(),
          (int) sr.getTimeLimit(),
          sr.getTypesOnly(),
          sr.getSearchFilter() != null ? sr.getSearchFilter().format() : null,
          sr.getReturnAttributes());
        req.setFollowReferrals(sr.getFollowReferrals());
        if (irListener != null) {
          req.setIntermediateResponseListener(irListener);
        }
        return req;
      } catch (LDAPException e) {
        // thrown if the filter cannot be parsed
        throw new LDAPSearchException(e);
      }
    }


    /**
     * Returns the unbound id search scope for the supplied search scope.
     *
     * @param  ss  search scope
     *
     * @return  unbound id search scope
     */
    protected SearchScope getSearchScope(final org.ldaptive.SearchScope ss)
    {
      SearchScope scope = null;
      if (ss == org.ldaptive.SearchScope.OBJECT) {
        scope = SearchScope.BASE;
      } else if (ss == org.ldaptive.SearchScope.ONELEVEL) {
        scope = SearchScope.ONE;
      } else if (ss == org.ldaptive.SearchScope.SUBTREE) {
        scope = SearchScope.SUB;
      }
      return scope;
    }


    /**
     * Returns the unbound id deference policy for the supplied deref aliases.
     *
     * @param  deref  deref aliases
     *
     * @return  dereference policy
     */
    protected DereferencePolicy getDereferencePolicy(final DerefAliases deref)
    {
      DereferencePolicy policy = DereferencePolicy.NEVER;
      if (deref == DerefAliases.ALWAYS) {
        policy = DereferencePolicy.ALWAYS;
      } else if (deref == DerefAliases.FINDING) {
        policy = DereferencePolicy.FINDING;
      } else if (deref == DerefAliases.NEVER) {
        policy = DereferencePolicy.NEVER;
      } else if (deref == DerefAliases.SEARCHING) {
        policy = DereferencePolicy.SEARCHING;
      }
      return policy;
    }


    /**
     * Determines whether the supplied ldap exception should be ignored.
     *
     * @param  ignoreResultCodes  to match against the exception
     * @param  e  ldap exception to match
     *
     * @return  result code that should be ignored or null
     */
    protected ResultCode ignoreSearchException(
      final ResultCode[] ignoreResultCodes,
      final LDAPException e)
    {
      ResultCode ignore = null;
      if (ignoreResultCodes != null && ignoreResultCodes.length > 0) {
        for (ResultCode rc : ignoreResultCodes) {
          if (e.getResultCode().intValue() == rc.value()) {
            logger.debug("Ignoring ldap exception", e);
            ignore = rc;
            break;
          }
        }
      }
      return ignore;
    }


    /**
     * Processes the response controls on the supplied entry and returns a
     * corresponding search item.
     *
     * @param  entry  to process
     *
     * @return  search item
     */
    protected SearchItem processSearchResultEntry(final SearchResultEntry entry)
    {
      logger.trace("reading search entry: {}", entry);

      ResponseControl[] respControls = null;
      if (entry.getControls() != null && entry.getControls().length > 0) {
        respControls = config.getControlProcessor().processResponseControls(
          entry.getControls());
      }

      final SearchEntry se = util.toSearchEntry(
        entry,
        respControls,
        entry.getMessageID());
      return new SearchItem(se);
    }


    /**
     * Processes the response controls on the supplied reference and returns a
     * corresponding search item.
     *
     * @param  ref  to process
     *
     * @return  search item
     */
    protected SearchItem processSearchResultReference(
      final SearchResultReference ref)
    {
      logger.trace("reading search reference: {}", ref);

      ResponseControl[] respControls = null;
      if (ref.getControls() != null && ref.getControls().length > 0) {
        respControls = config.getControlProcessor().processResponseControls(
          ref.getControls());
      }

      final SearchReference sr = new SearchReference(
        ref.getMessageID(),
        respControls,
        ref.getReferralURLs());
      return new SearchItem(sr);
    }


    /**
     * Processes the response controls on the supplied response and returns a
     * corresponding search item.
     *
     * @param  res  to process
     *
     * @return  search item
     */
    protected SearchItem processIntermediateResponse(
      final IntermediateResponse res)
    {
      logger.trace("reading intermediate response: {}", res);

      ResponseControl[] respControls = null;
      if (res.getControls() != null && res.getControls().length > 0) {
        respControls = config.getControlProcessor().processResponseControls(
          res.getControls());
      }

      final org.ldaptive.intermediate.IntermediateResponse ir =
        IntermediateResponseFactory.createIntermediateResponse(
          res.getOID(),
          res.getValue().getValue(),
          respControls,
          res.getMessageID());
      return new SearchItem(ir);
    }
  }


  /** Async request to invoke abandons. */
  protected class UnboundIDAsyncRequest implements AsyncRequest
  {

    /** Async request id. */
    private final AsyncRequestID requestID;

    /** Request that produced this async request. */
    private final Request request;


    /**
     * Creates a new unboundid async request.
     *
     * @param  id  async request id
     * @param  r  request
     */
    public UnboundIDAsyncRequest(final AsyncRequestID id, final Request r)
    {
      requestID = id;
      request = r;
    }


    /** {@inheritDoc} */
    @Override
    public int getMessageId()
    {
      return requestID.getMessageID();
    }


    /** {@inheritDoc} */
    @Override
    public void abandon()
      throws LdapException
    {
      try {
        connection.abandon(requestID);
      } catch (LDAPException e) {
        processLDAPException(request, e);
      }
    }


    /** {@inheritDoc} */
    @Override
    public void abandon(final RequestControl[] controls)
      throws LdapException
    {
      try {
        connection.abandon(
          requestID,
          config.getControlProcessor().processRequestControls(
            request.getControls()));
      } catch (LDAPException e) {
        processLDAPException(request, e);
      }
    }
  }


  /**
   * Allows the use of multiple unsolicited notification handlers per
   * connection.
   */
  protected class AggregateUnsolicitedNotificationHandler
    implements UnsolicitedNotificationHandler
  {

    /** Listeners to receive unsolicited notifications. */
    private final Queue<UnsolicitedNotificationListener> listeners =
      new ConcurrentLinkedQueue<>();


    /**
     * Adds an unsolicited notification listener to this handler.
     *
     * @param  listener  to receive unsolicited notifications
     */
    public void addUnsolicitedNotificationListener(
      final UnsolicitedNotificationListener listener)
    {
      listeners.add(listener);
    }


    /**
     * Removes an unsolicited notification listener from this handler.
     *
     * @param  listener  to stop receiving unsolicited notifications
     */
    public void removeUnsolicitedNotificationListener(
      final UnsolicitedNotificationListener listener)
    {
      listeners.remove(listener);
    }


    /** {@inheritDoc} */
    @Override
    public void handleUnsolicitedNotification(
      final LDAPConnection ldapConnection,
      final ExtendedResult extendedResult)
    {
      logger.debug("Unsolicited notification received: {}", extendedResult);

      final Response<Void> response = createResponse(
        null,
        null,
        extendedResult);
      for (UnsolicitedNotificationListener listener : listeners) {
        listener.notificationReceived(extendedResult.getOID(), response);
      }
    }
  }


  /** Allows the use of multiple disconnect handlers per connection. */
  protected class AggregateDisconnectHandler implements DisconnectHandler
  {

    /** Handlers to receive disconnect notifications. */
    private final Queue<DisconnectHandler> handlers =
      new ConcurrentLinkedQueue<>();


    /**
     * Adds an disconnect handler to this handler.
     *
     * @param  handler  to receive disconnect notifications
     */
    public void addDisconnectHandler(final DisconnectHandler handler)
    {
      handlers.add(handler);
    }


    /**
     * Removes a disconnect handler from this handler.
     *
     * @param  handler  to stop receiving disconnect notifications
     */
    public void removeDisconnectHandler(final DisconnectHandler handler)
    {
      handlers.remove(handler);
    }


    /** {@inheritDoc} */
    @Override
    public void handleDisconnect(
      final LDAPConnection ldapConnection,
      final String host,
      final int port,
      final DisconnectType disconnectType,
      final String message,
      final Throwable throwable)
    {
      logger.debug("Disconnection received: {}", disconnectType);
      for (DisconnectHandler handler : handlers) {
        handler.handleDisconnect(
          ldapConnection,
          host,
          port,
          disconnectType,
          message,
          throwable);
      }
    }
  }
}
