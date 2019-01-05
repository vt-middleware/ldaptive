/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.opendj;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.forgerock.opendj.ldap.ByteString;
import org.forgerock.opendj.ldap.Connection;
import org.forgerock.opendj.ldap.ConnectionEventListener;
import org.forgerock.opendj.ldap.DereferenceAliasesPolicy;
import org.forgerock.opendj.ldap.ErrorResultException;
import org.forgerock.opendj.ldap.FutureResult;
import org.forgerock.opendj.ldap.IntermediateResponseHandler;
import org.forgerock.opendj.ldap.Modification;
import org.forgerock.opendj.ldap.ReferralException;
import org.forgerock.opendj.ldap.SearchResultHandler;
import org.forgerock.opendj.ldap.SearchScope;
import org.forgerock.opendj.ldap.controls.Control;
import org.forgerock.opendj.ldap.requests.DigestMD5SASLBindRequest;
import org.forgerock.opendj.ldap.requests.ExternalSASLBindRequest;
import org.forgerock.opendj.ldap.requests.GSSAPISASLBindRequest;
import org.forgerock.opendj.ldap.requests.GenericExtendedRequest;
import org.forgerock.opendj.ldap.requests.Requests;
import org.forgerock.opendj.ldap.requests.SASLBindRequest;
import org.forgerock.opendj.ldap.requests.SearchRequest;
import org.forgerock.opendj.ldap.requests.SimpleBindRequest;
import org.forgerock.opendj.ldap.responses.BindResult;
import org.forgerock.opendj.ldap.responses.CompareResult;
import org.forgerock.opendj.ldap.responses.ExtendedResult;
import org.forgerock.opendj.ldap.responses.GenericExtendedResult;
import org.forgerock.opendj.ldap.responses.IntermediateResponse;
import org.forgerock.opendj.ldap.responses.Result;
import org.forgerock.opendj.ldap.responses.SearchResultEntry;
import org.forgerock.opendj.ldap.responses.SearchResultReference;
import org.ldaptive.AddRequest;
import org.ldaptive.BindRequest;
import org.ldaptive.CompareRequest;
import org.ldaptive.DeleteRequest;
import org.ldaptive.DerefAliases;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ModifyDnRequest;
import org.ldaptive.ModifyRequest;
import org.ldaptive.Request;
import org.ldaptive.Response;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchEntry;
import org.ldaptive.SearchReference;
import org.ldaptive.asn1.DefaultDERBuffer;
import org.ldaptive.async.AsyncRequest;
import org.ldaptive.control.RequestControl;
import org.ldaptive.control.ResponseControl;
import org.ldaptive.extended.ExtendedRequest;
import org.ldaptive.extended.ExtendedResponse;
import org.ldaptive.extended.ExtendedResponseFactory;
import org.ldaptive.extended.UnsolicitedNotificationListener;
import org.ldaptive.intermediate.IntermediateResponseFactory;
import org.ldaptive.provider.ProviderUtils;
import org.ldaptive.provider.SearchItem;
import org.ldaptive.provider.SearchIterator;
import org.ldaptive.provider.SearchListener;
import org.ldaptive.sasl.DigestMd5Config;
import org.ldaptive.sasl.QualityOfProtection;
import org.ldaptive.sasl.SaslConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenDJ provider implementation of ldap operations.
 *
 * @author  Middleware Services
 */
public class OpenDJConnection implements org.ldaptive.provider.ProviderConnection
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Ldap connection. */
  private Connection connection;

  /** Provider configuration. */
  private final OpenDJProviderConfig config;

  /** Connection event listener for unsolicited notifications. */
  private final AggregateUnsolicitedNotificationListener notificationListener =
    new AggregateUnsolicitedNotificationListener();


  /**
   * Creates a new opendj ldap connection.
   *
   * @param  c  ldap connection
   * @param  pc  provider configuration
   */
  public OpenDJConnection(final Connection c, final OpenDJProviderConfig pc)
  {
    connection = c;
    config = pc;
    connection.addConnectionEventListener(notificationListener);
  }


  /**
   * Returns the underlying ldap connection.
   *
   * @return  ldap connection
   */
  public Connection getLdapConnection()
  {
    return connection;
  }


  @Override
  public void close(final RequestControl[] controls)
    throws LdapException
  {
    if (connection != null) {
      final org.forgerock.opendj.ldap.requests.UnbindRequest ur = Requests.newUnbindRequest();
      if (controls != null) {
        for (Control c : config.getControlProcessor().processRequestControls(controls)) {
          ur.addControl(c);
        }
      }

      try {
        connection.close(ur, "Close requested by client");
      } finally {
        connection = null;
      }
    }
  }


  @Override
  public Response<Void> bind(final BindRequest request)
    throws LdapException
  {
    final Response<Void> response;
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
      final SimpleBindRequest sbr = Requests.newSimpleBindRequest();
      if (request.getControls() != null) {
        for (Control c : config.getControlProcessor().processRequestControls(request.getControls())) {
          sbr.addControl(c);
        }
      }

      final BindResult result = connection.bind(sbr);
      response = createResponse(request, null, result);
    } catch (ReferralException e) {
      response = createResponse(request, null, e.getResult());
    } catch (ErrorResultException e) {
      processErrorResultException(request, e);
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
      final SimpleBindRequest sbr = Requests.newSimpleBindRequest(request.getDn(), request.getCredential().getChars());
      if (request.getControls() != null) {
        for (Control c : config.getControlProcessor().processRequestControls(request.getControls())) {
          sbr.addControl(c);
        }
      }

      final BindResult result = connection.bind(sbr);
      response = createResponse(request, null, result);
    } catch (ReferralException e) {
      response = createResponse(request, null, e.getResult());
    } catch (ErrorResultException e) {
      processErrorResultException(request, e);
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
    final SASLBindRequest sbr;
    final SaslConfig sc = request.getSaslConfig();
    switch (sc.getMechanism()) {

    case EXTERNAL:
      sbr = Requests.newExternalSASLBindRequest();
      ((ExternalSASLBindRequest) sbr).setAuthorizationID(sc.getAuthorizationId());
      break;

    case DIGEST_MD5:
      sbr = Requests.newDigestMD5SASLBindRequest(
        request.getDn() != null ?
          request.getDn() : "", request.getCredential() != null ? request.getCredential().getBytes() : null);
      String digestMd5Realm = sc instanceof DigestMd5Config ? ((DigestMd5Config) sc).getRealm() : null;
      if (digestMd5Realm == null && request.getDn().contains("@")) {
        digestMd5Realm = request.getDn().substring(request.getDn().indexOf("@") + 1);
      }
      if (digestMd5Realm != null) {
        ((DigestMD5SASLBindRequest) sbr).setRealm(digestMd5Realm);
      }
      break;

    case CRAM_MD5:
      throw new UnsupportedOperationException("CRAM-MD5 not supported");
      // LDAP reports: error: SASL bind in progress (tag=99)
      /*
       * sbr = Requests.newCRAMMD5SASLBindRequest(
       * request.getDn() != null ? request.getDn() : "",
       * request.getCredential() != null ?
       *  request.getCredential().getBytes() : null);
       * break;
       */

    case GSSAPI:
      throw new UnsupportedOperationException("GSSAPI not supported");
      /*
       * sbr = Requests.newGSSAPISASLBindRequest(
       * request.getDn() != null ? request.getDn() : "",
       * request.getCredential() != null ?
       *  request.getCredential().getBytes() : new byte[0]);
       * ((GSSAPISASLBindRequest) sbr).setAuthorizationID(
       * sc.getAuthorizationId());
       * final String gssApiRealm = sc instanceof GssApiConfig
       * ? ((GssApiConfig) sc).getRealm() : null;
       * if (gssApiRealm != null) {
       * ((GSSAPISASLBindRequest) sbr).setRealm(gssApiRealm);
       * }
       * if (sc.getQualityOfProtection() != null) {
       * ((GSSAPISASLBindRequest) sbr).addQOP(
       *  getQualityOfProtection(sc.getQualityOfProtection()));
       * }
       * break;
       */

    default:
      throw new IllegalArgumentException("Unknown SASL authentication mechanism: " + sc.getMechanism());
    }

    if (request.getControls() != null) {
      for (Control c : config.getControlProcessor().processRequestControls(request.getControls())) {
        sbr.addControl(c);
      }
    }

    try {
      final BindResult result = connection.bind(sbr);
      response = createResponse(request, null, result);
    } catch (ReferralException e) {
      response = createResponse(request, null, e.getResult());
    } catch (ErrorResultException e) {
      processErrorResultException(request, e);
    }
    return response;
  }


  /**
   * Returns the SASL quality of protection string for the supplied enum.
   *
   * @param  qop  quality of protection enum
   *
   * @return  SASL quality of protection string
   */
  protected static String getQualityOfProtection(final QualityOfProtection qop)
  {
    final String name;
    switch (qop) {

    case AUTH:
      name = GSSAPISASLBindRequest.QOP_AUTH;
      break;

    case AUTH_INT:
      name = GSSAPISASLBindRequest.QOP_AUTH_INT;
      break;

    case AUTH_CONF:
      name = GSSAPISASLBindRequest.QOP_AUTH_CONF;
      break;

    default:
      throw new IllegalArgumentException("Unknown SASL quality of protection: " + qop);
    }
    return name;
  }


  @Override
  public Response<Void> add(final AddRequest request)
    throws LdapException
  {
    Response<Void> response = null;
    try {
      final OpenDJUtils util = new OpenDJUtils();
      final org.forgerock.opendj.ldap.requests.AddRequest ar = Requests.newAddRequest(
        util.fromLdapEntry(new LdapEntry(request.getDn(), request.getLdapAttributes())));
      if (request.getControls() != null) {
        for (Control c : config.getControlProcessor().processRequestControls(request.getControls())) {
          ar.addControl(c);
        }
      }

      final Result result = connection.add(ar);
      response = createResponse(request, null, result);
    } catch (ReferralException e) {
      response = createResponse(request, null, e.getResult());
    } catch (ErrorResultException e) {
      processErrorResultException(request, e);
    }
    return response;
  }


  @Override
  public Response<Boolean> compare(final CompareRequest request)
    throws LdapException
  {
    Response<Boolean> response = null;
    try {
      final OpenDJUtils util = new OpenDJUtils();
      final org.forgerock.opendj.ldap.requests.CompareRequest cr;
      if (request.getAttribute().isBinary()) {
        cr = Requests.newCompareRequest(
          request.getDn(),
          request.getAttribute().getName(),
          util.fromBinaryValues(request.getAttribute().getBinaryValues())[0]);
      } else {
        cr = Requests.newCompareRequest(
          request.getDn(),
          request.getAttribute().getName(),
          util.fromStringValues(request.getAttribute().getStringValues())[0]);
      }
      if (request.getControls() != null) {
        for (Control c : config.getControlProcessor().processRequestControls(request.getControls())) {
          cr.addControl(c);
        }
      }

      final CompareResult result = connection.compare(cr);
      response = createResponse(request, result.matched(), result);
    } catch (ReferralException e) {
      response = createResponse(request, null, e.getResult());
    } catch (ErrorResultException e) {
      processErrorResultException(request, e);
    }
    return response;
  }


  @Override
  public Response<Void> delete(final DeleteRequest request)
    throws LdapException
  {
    Response<Void> response = null;
    try {
      final org.forgerock.opendj.ldap.requests.DeleteRequest dr = Requests.newDeleteRequest(request.getDn());
      if (request.getControls() != null) {
        for (Control c : config.getControlProcessor().processRequestControls(request.getControls())) {
          dr.addControl(c);
        }
      }

      final Result result = connection.delete(dr);
      response = createResponse(request, null, result);
    } catch (ReferralException e) {
      response = createResponse(request, null, e.getResult());
    } catch (ErrorResultException e) {
      processErrorResultException(request, e);
    }
    return response;
  }


  @Override
  public Response<Void> modify(final ModifyRequest request)
    throws LdapException
  {
    Response<Void> response = null;
    try {
      final OpenDJUtils util = new OpenDJUtils();
      final org.forgerock.opendj.ldap.requests.ModifyRequest mr = Requests.newModifyRequest(request.getDn());
      for (Modification m : util.fromAttributeModification(request.getAttributeModifications())) {
        mr.addModification(m);
      }
      if (request.getControls() != null) {
        for (Control c : config.getControlProcessor().processRequestControls(request.getControls())) {
          mr.addControl(c);
        }
      }

      final Result result = connection.modify(mr);
      response = createResponse(request, null, result);
    } catch (ReferralException e) {
      response = createResponse(request, null, e.getResult());
    } catch (ErrorResultException e) {
      processErrorResultException(request, e);
    }
    return response;
  }


  @Override
  public Response<Void> modifyDn(final ModifyDnRequest request)
    throws LdapException
  {
    Response<Void> response = null;
    try {
      final org.forgerock.opendj.ldap.requests.ModifyDNRequest mdr = Requests.newModifyDNRequest(
        request.getDn(),
        request.getNewDn());
      mdr.setDeleteOldRDN(request.getDeleteOldRDn());
      if (request.getControls() != null) {
        for (Control c : config.getControlProcessor().processRequestControls(request.getControls())) {
          mdr.addControl(c);
        }
      }

      final Result result = connection.modifyDN(mdr);
      response = createResponse(request, null, result);
    } catch (ReferralException e) {
      response = createResponse(request, null, e.getResult());
    } catch (ErrorResultException e) {
      processErrorResultException(request, e);
    }
    return response;
  }


  @Override
  public SearchIterator search(final org.ldaptive.SearchRequest request)
    throws LdapException
  {
    final OpenDJSearchIterator i = new OpenDJSearchIterator(request);
    i.initialize();
    return i;
  }


  @Override
  public void searchAsync(final org.ldaptive.SearchRequest request, final SearchListener listener)
    throws LdapException
  {
    final OpenDJAsyncSearchListener l = new OpenDJAsyncSearchListener(request, listener);
    l.initialize();
  }


  @Override
  public void abandon(final int messageId, final RequestControl[] controls)
    throws LdapException
  {
    final org.forgerock.opendj.ldap.requests.AbandonRequest ar = Requests.newAbandonRequest(messageId);
    if (controls != null) {
      for (Control c : config.getControlProcessor().processRequestControls(controls)) {
        ar.addControl(c);
      }
    }

    connection.abandonAsync(ar);
  }


  @Override
  public Response<?> extendedOperation(final ExtendedRequest request)
    throws LdapException
  {
    Response<?> response = null;
    try {
      final GenericExtendedRequest er;
      final byte[] requestBerValue = request.encode();
      if (requestBerValue == null) {
        er = Requests.newGenericExtendedRequest(request.getOID());
      } else {
        er = Requests.newGenericExtendedRequest(request.getOID(), ByteString.wrap(requestBerValue));
      }
      if (request.getControls() != null) {
        for (Control c : config.getControlProcessor().processRequestControls(request.getControls())) {
          er.addControl(c);
        }
      }

      final GenericExtendedResult result = connection.extendedRequest(er);
      final ExtendedResponse<?> extRes = ExtendedResponseFactory.createExtendedResponse(
        request.getOID(),
        result.getOID(),
        result.getValue() != null ? new DefaultDERBuffer(result.getValue().toByteArray()) : null);
      response = createResponse(request, extRes.getValue(), result);
    } catch (ErrorResultException e) {
      processErrorResultException(request, e);
    }
    return response;
  }


  @Override
  public void addUnsolicitedNotificationListener(final UnsolicitedNotificationListener listener)
  {
    notificationListener.addUnsolicitedNotificationListener(listener);
  }


  @Override
  public void removeUnsolicitedNotificationListener(final UnsolicitedNotificationListener listener)
  {
    notificationListener.removeUnsolicitedNotificationListener(listener);
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
  protected <T> Response<T> createResponse(final Request request, final T result, final Result ldapResult)
  {
    final List<Control> ctls = ldapResult.getControls();
    final List<String> urls = ldapResult.getReferralURIs();
    return
      new Response<>(
        result,
        ResultCode.valueOf(ldapResult.getResultCode().intValue()),
        ldapResult.getDiagnosticMessage(),
        ldapResult.getMatchedDN(),
        config.getControlProcessor().processResponseControls(ctls.toArray(new Control[ctls.size()])),
        urls.toArray(new String[urls.size()]),
        -1);
  }


  /**
   * Determines if the supplied error result exception should result in an operation retry.
   *
   * @param  request  that produced the exception
   * @param  e  that was produced
   *
   * @throws  LdapException  wrapping the error result exception
   */
  protected void processErrorResultException(final Request request, final ErrorResultException e)
    throws LdapException
  {
    final List<Control> ctls = e.getResult().getControls();
    final List<String> urls = e.getResult().getReferralURIs();
    ProviderUtils.throwOperationException(
      config.getOperationExceptionResultCodes(),
      e,
      e.getResult().getResultCode().intValue(),
      e.getResult().getMatchedDN(),
      config.getControlProcessor().processResponseControls(ctls.toArray(new Control[ctls.size()])),
      urls.toArray(new String[urls.size()]),
      true);
  }


  /** Search iterator for opendj search results. */
  protected class OpenDJSearchIterator extends AbstractOpenDJSearch implements SearchIterator
  {

    /** Response data. */
    private org.ldaptive.Response<Void> response;

    /** Search result iterator. */
    private SearchResultIterator resultIterator;


    /**
     * Creates a new opendj search iterator.
     *
     * @param  sr  search request
     */
    public OpenDJSearchIterator(final org.ldaptive.SearchRequest sr)
    {
      super(sr);
    }


    /**
     * Initializes this opendj search iterator.
     *
     * @throws  LdapException  if an error occurs
     */
    public void initialize()
      throws LdapException
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
    protected SearchResultIterator search(final Connection conn, final org.ldaptive.SearchRequest sr)
      throws LdapException
    {
      final SearchRequest opendjSr = getSearchRequest(sr);
      if (sr.getControls() != null) {
        for (Control c : config.getControlProcessor().processRequestControls(sr.getControls())) {
          opendjSr.addControl(c);
        }
      }

      final SearchResultIterator i = new SearchResultIterator();
      try {
        conn.search(opendjSr, i);
      } catch (ErrorResultException e) {
        final ResultCode rc = ignoreSearchException(config.getSearchIgnoreResultCodes(), e);
        if (rc == null) {
          processErrorResultException(request, e);
        }
      }
      return i;
    }


    @Override
    public boolean hasNext()
      throws LdapException
    {
      if (resultIterator == null || response != null) {
        return false;
      }

      final boolean more = resultIterator.hasNext();
      if (!more) {
        final Result result = resultIterator.getResult();
        logger.trace("reading search result: {}", result);
        response = createResponse(request, null, result);
      }
      return more;
    }


    @Override
    public SearchItem next()
      throws LdapException
    {
      return resultIterator.next();
    }


    @Override
    public org.ldaptive.Response<Void> getResponse()
    {
      return response;
    }


    @Override
    public void close()
      throws LdapException {}


    /** Search results handler for storing entries returned by a search operation. */
    protected class SearchResultIterator implements SearchResultHandler
    {

      /** Search items. */
      protected final Queue<SearchItem> queue = new ArrayDeque<>();

      /** Search result. */
      private Result result;


      /**
       * Returns the next search item from the queue.
       *
       * @return  search item
       */
      public SearchItem next()
      {
        return queue.poll();
      }


      /**
       * Returns the result of the search.
       *
       * @return  search result
       */
      public Result getResult()
      {
        return result;
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


      @Override
      public void handleErrorResult(final ErrorResultException e)
      {
        logger.trace("reading error result: {}", e);
        result = e.getResult();
      }


      @Override
      public void handleResult(final Result r)
      {
        logger.trace("reading result: {}", r);
        result = r;
      }


      @Override
      public boolean handleEntry(final SearchResultEntry entry)
      {
        return queue.add(processSearchResultEntry(entry));
      }


      @Override
      public boolean handleReference(final SearchResultReference ref)
      {
        return queue.add(processSearchResultReference(ref));
      }
    }
  }


  /** Search listener for opendj id async search results. */
  protected class OpenDJAsyncSearchListener extends AbstractOpenDJSearch
    implements SearchResultHandler, IntermediateResponseHandler
  {

    /** Search result listener. */
    private final SearchListener listener;


    /**
     * Creates a new opendj async search listener.
     *
     * @param  sr  search request
     * @param  sl  search listener
     */
    public OpenDJAsyncSearchListener(final org.ldaptive.SearchRequest sr, final SearchListener sl)
    {
      super(sr);
      listener = sl;
    }


    /**
     * Initializes this opendj async search listener.
     *
     * @throws  LdapException  if an error occurs
     */
    public void initialize()
      throws LdapException
    {
      search(connection, request);
    }


    /**
     * Executes an asynchronous ldap search.
     *
     * @param  conn  to search with
     * @param  sr  to read properties from
     *
     * @throws  LdapException  if an error occurs
     */
    protected void search(final Connection conn, final org.ldaptive.SearchRequest sr)
      throws LdapException
    {
      final SearchRequest opendjSr = getSearchRequest(sr);
      if (sr.getControls() != null) {
        for (Control c : config.getControlProcessor().processRequestControls(sr.getControls())) {
          opendjSr.addControl(c);
        }
      }

      final FutureResult<Result> result = conn.searchAsync(opendjSr, this, this);
      listener.asyncRequestReceived(new OpenDJAsyncRequest(result));
    }


    @Override
    public void handleErrorResult(final ErrorResultException e)
    {
      logger.trace("reading error result: {}", e);

      final List<Control> ctls = e.getResult().getControls();
      final List<String> urls = e.getResult().getReferralURIs();
      listener.exceptionReceived(
        new LdapException(
          e.getMessage(),
          new Exception(e.getCause()),
          ResultCode.valueOf(e.getResult().getResultCode().intValue()),
          e.getResult().getMatchedDN(),
          config.getControlProcessor().processResponseControls(ctls.toArray(new Control[ctls.size()])),
          urls.toArray(new String[urls.size()])));
    }


    @Override
    public void handleResult(final Result r)
    {
      logger.trace("reading result: {}", r);

      final org.ldaptive.Response<Void> response = createResponse(request, null, r);
      listener.responseReceived(response);
    }


    @Override
    public boolean handleEntry(final SearchResultEntry entry)
    {
      listener.searchItemReceived(processSearchResultEntry(entry));
      return true;
    }


    @Override
    public boolean handleReference(final SearchResultReference ref)
    {
      listener.searchItemReceived(processSearchResultReference(ref));
      return true;
    }


    @Override
    public boolean handleIntermediateResponse(final IntermediateResponse res)
    {
      listener.searchItemReceived(processIntermediateResponse(res));
      return true;
    }
  }


  /** Common search functionality for opendj iterators and listeners. */
  protected abstract class AbstractOpenDJSearch
  {

    /** Search request. */
    protected final org.ldaptive.SearchRequest request;

    /** Utility class. */
    protected final OpenDJUtils util;


    /**
     * Creates a new abstract opendj search.
     *
     * @param  sr  search request
     */
    public AbstractOpenDJSearch(final org.ldaptive.SearchRequest sr)
    {
      request = sr;
      util = new OpenDJUtils(request.getSortBehavior());
      util.setBinaryAttributes(request.getBinaryAttributes());
    }


    /**
     * Returns an opendj search request object configured with the supplied search request.
     *
     * @param  sr  search request containing configuration to create opendj search request
     *
     * @return  search request
     */
    protected SearchRequest getSearchRequest(final org.ldaptive.SearchRequest sr)
    {
      final SearchRequest opendjSr = Requests.newSearchRequest(
        sr.getBaseDn(),
        getSearchScope(sr.getSearchScope()),
        sr.getSearchFilter() != null ? sr.getSearchFilter().format() : null,
        sr.getReturnAttributes());
      opendjSr.setDereferenceAliasesPolicy(getDereferencePolicy(sr.getDerefAliases()));
      opendjSr.setSizeLimit((int) sr.getSizeLimit());
      opendjSr.setTimeLimit((int) sr.getTimeLimit().getSeconds());
      opendjSr.setTypesOnly(sr.getTypesOnly());
      return opendjSr;
    }


    /**
     * Returns the opendj search scope for the supplied search scope.
     *
     * @param  ss  search scope
     *
     * @return  opendj search scope
     */
    protected SearchScope getSearchScope(final org.ldaptive.SearchScope ss)
    {
      SearchScope scope = null;
      if (ss == org.ldaptive.SearchScope.OBJECT) {
        scope = SearchScope.BASE_OBJECT;
      } else if (ss == org.ldaptive.SearchScope.ONELEVEL) {
        scope = SearchScope.SINGLE_LEVEL;
      } else if (ss == org.ldaptive.SearchScope.SUBTREE) {
        scope = SearchScope.WHOLE_SUBTREE;
      }
      return scope;
    }


    /**
     * Returns the opendj deference policy for the supplied deref aliases.
     *
     * @param  deref  deref aliases
     *
     * @return  dereference policy
     */
    protected DereferenceAliasesPolicy getDereferencePolicy(final DerefAliases deref)
    {
      DereferenceAliasesPolicy policy = DereferenceAliasesPolicy.NEVER;
      if (deref == DerefAliases.ALWAYS) {
        policy = DereferenceAliasesPolicy.ALWAYS;
      } else if (deref == DerefAliases.FINDING) {
        policy = DereferenceAliasesPolicy.FINDING_BASE;
      } else if (deref == DerefAliases.NEVER) {
        policy = DereferenceAliasesPolicy.NEVER;
      } else if (deref == DerefAliases.SEARCHING) {
        policy = DereferenceAliasesPolicy.IN_SEARCHING;
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
    protected ResultCode ignoreSearchException(final ResultCode[] ignoreResultCodes, final ErrorResultException e)
    {
      ResultCode ignore = null;
      if (ignoreResultCodes != null && ignoreResultCodes.length > 0) {
        for (ResultCode rc : ignoreResultCodes) {
          if (e.getResult().getResultCode().intValue() == rc.value()) {
            logger.debug("Ignoring ldap exception", e);
            ignore = rc;
            break;
          }
        }
      }
      return ignore;
    }


    /**
     * Processes the response controls on the supplied entry and returns a corresponding search item.
     *
     * @param  entry  to process
     *
     * @return  search item
     */
    protected SearchItem processSearchResultEntry(final SearchResultEntry entry)
    {
      logger.trace("reading search entry: {}", entry);

      ResponseControl[] respControls = null;
      if (entry.getControls() != null && entry.getControls().size() > 0) {
        final List<Control> ctls = entry.getControls();
        respControls = config.getControlProcessor().processResponseControls(ctls.toArray(new Control[ctls.size()]));
      }

      final SearchEntry se = util.toSearchEntry(entry, respControls, -1);
      return new SearchItem(se);
    }


    /**
     * Processes the response controls on the supplied reference and returns a corresponding search item.
     *
     * @param  ref  to process
     *
     * @return  search item
     */
    protected SearchItem processSearchResultReference(final SearchResultReference ref)
    {
      logger.trace("reading search reference: {}", ref);

      ResponseControl[] respControls = null;
      if (ref.getControls() != null && ref.getControls().size() > 0) {
        final List<Control> ctls = ref.getControls();
        respControls = config.getControlProcessor().processResponseControls(ctls.toArray(new Control[ctls.size()]));
      }

      final SearchReference sr = new SearchReference(-1, respControls, ref.getURIs());
      return new SearchItem(sr);
    }


    /**
     * Processes the response controls on the supplied response and returns a corresponding search item.
     *
     * @param  res  to process
     *
     * @return  search item
     */
    protected SearchItem processIntermediateResponse(final IntermediateResponse res)
    {
      logger.trace("reading intermediate response: {}", res);

      ResponseControl[] respControls = null;
      if (res.getControls() != null && res.getControls().size() > 0) {
        final List<Control> ctls = res.getControls();
        respControls = config.getControlProcessor().processResponseControls(ctls.toArray(new Control[ctls.size()]));
      }

      final org.ldaptive.intermediate.IntermediateResponse ir = IntermediateResponseFactory.createIntermediateResponse(
        res.getOID(),
        res.getValue() != null ? new DefaultDERBuffer(res.getValue().toByteArray()) : null,
        respControls,
        -1);
      return new SearchItem(ir);
    }
  }


  /** Async request to invoke abandons. */
  protected class OpenDJAsyncRequest implements AsyncRequest
  {

    /** Future result. */
    private final FutureResult<Result> result;


    /**
     * Creates a new OpenDJ async request.
     *
     * @param  r  future result from an async operation
     */
    public OpenDJAsyncRequest(final FutureResult<Result> r)
    {
      result = r;
    }


    @Override
    public int getMessageId()
    {
      return result.getRequestID();
    }


    @Override
    public void abandon()
      throws LdapException
    {
      final org.forgerock.opendj.ldap.requests.AbandonRequest ar = Requests.newAbandonRequest(result.getRequestID());
      connection.abandonAsync(ar);
    }


    @Override
    public void abandon(final RequestControl[] controls)
      throws LdapException
    {
      final org.forgerock.opendj.ldap.requests.AbandonRequest ar = Requests.newAbandonRequest(result.getRequestID());
      if (controls != null) {
        for (Control c : config.getControlProcessor().processRequestControls(controls)) {
          ar.addControl(c);
        }
      }
      connection.abandonAsync(ar);
    }
  }


  /** Allows the use of multiple unsolicited notification listeners per connection. */
  protected class AggregateUnsolicitedNotificationListener implements ConnectionEventListener
  {

    /** Listeners to receive unsolicited notifications. */
    private final List<UnsolicitedNotificationListener> listeners = new ArrayList<>();


    /**
     * Adds an unsolicited notification listener to this listener.
     *
     * @param  listener  to receive unsolicited notifications
     */
    public void addUnsolicitedNotificationListener(final UnsolicitedNotificationListener listener)
    {
      synchronized (listeners) {
        listeners.add(listener);
      }
    }


    /**
     * Removes an unsolicited notification listener from this listener.
     *
     * @param  listener  to stop receiving unsolicited notifications
     */
    public void removeUnsolicitedNotificationListener(final UnsolicitedNotificationListener listener)
    {
      synchronized (listeners) {
        listeners.remove(listener);
      }
    }


    @Override
    public void handleConnectionClosed() {}


    @Override
    public void handleConnectionError(final boolean b, final ErrorResultException e) {}


    @Override
    public void handleUnsolicitedNotification(final ExtendedResult extendedResult)
    {
      logger.debug("Unsolicited notification received: {}", extendedResult);
      synchronized (listeners) {
        final Response<Void> response = createResponse(null, null, extendedResult);
        for (UnsolicitedNotificationListener listener : listeners) {
          listener.notificationReceived(extendedResult.getOID(), response);
        }
      }
    }
  }
}
