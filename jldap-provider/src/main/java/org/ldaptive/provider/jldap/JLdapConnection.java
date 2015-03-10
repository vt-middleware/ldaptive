/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.jldap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPConstraints;
import com.novell.ldap.LDAPControl;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPExtendedOperation;
import com.novell.ldap.LDAPExtendedResponse;
import com.novell.ldap.LDAPIntermediateResponse;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPMessageQueue;
import com.novell.ldap.LDAPResponse;
import com.novell.ldap.LDAPResponseQueue;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchQueue;
import com.novell.ldap.LDAPSearchResult;
import com.novell.ldap.LDAPSearchResultReference;
import com.novell.ldap.LDAPUnsolicitedNotificationListener;
import com.novell.security.sasl.RealmCallback;
import com.novell.security.sasl.RealmChoiceCallback;
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
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
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
 * JLDAP provider implementation of ldap operations.
 *
 * @author  Middleware Services
 */
public class JLdapConnection implements ProviderConnection
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Ldap connection. */
  private LDAPConnection connection;

  /** Provider configuration. */
  private final JLdapProviderConfig config;

  /** Receives unsolicited notifications. */
  private final AggregateUnsolicitedNotificationListener notificationListener =
    new AggregateUnsolicitedNotificationListener();


  /**
   * Creates a new jldap connection.
   *
   * @param  conn  ldap connection
   * @param  pc  provider configuration
   */
  public JLdapConnection(final LDAPConnection conn, final JLdapProviderConfig pc)
  {
    connection = conn;
    config = pc;
    connection.addUnsolicitedNotificationListener(notificationListener);
  }


  /**
   * Returns the underlying ldap connection.
   *
   * @return  ldap connection
   */
  public LDAPConnection getLDAPConnection()
  {
    return connection;
  }


  @Override
  public void close(final RequestControl[] controls)
    throws LdapException
  {
    try {
      if (connection != null) {
        connection.disconnect(getLDAPConstraints(controls));
      }
    } catch (LDAPException e) {
      throw new LdapException(e, ResultCode.valueOf(e.getResultCode()));
    } finally {
      connection = null;
    }
  }


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
      final LDAPResponseQueue queue = connection.bind(
        LDAPConnection.LDAP_V3,
        null,
        null,
        null,
        getLDAPConstraints(request));
      final LDAPResponse lr = (LDAPResponse) queue.getResponse();
      response = createResponse(request, null, lr);
    } catch (LDAPException e) {
      processLDAPException(e);
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
      final LDAPResponseQueue queue = connection.bind(
        LDAPConnection.LDAP_V3,
        request.getDn(),
        request.getCredential().getBytes(),
        null,
        getLDAPConstraints(request));
      final LDAPResponse lr = (LDAPResponse) queue.getResponse();
      response = createResponse(request, null, lr);
    } catch (LDAPException e) {
      processLDAPException(e);
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
    try {
      final SaslConfig sc = request.getSaslConfig();
      switch (sc.getMechanism()) {

      case EXTERNAL:
        throw new UnsupportedOperationException("SASL External not supported");
        /* current implementation appears to be broken
         * see http://tinyurl.com/7ojdzlz
         * connection.bind(
         * (String) null,
         * sc.getAuthorizationId(),
         * new String[] {"EXTERNAL"},
         * null,
         * (Object) null);
         * break;
         */

      case DIGEST_MD5:
        connection.bind(
          null,
          request.getDn(),
          new String[] {"DIGEST-MD5"},
          null,
          new SaslCallbackHandler(null, request.getCredential() != null ? request.getCredential().getString() : null));
        break;

      case CRAM_MD5:
        throw new UnsupportedOperationException("CRAM-MD5 not supported");

      case GSSAPI:
        throw new UnsupportedOperationException("GSSAPI not supported");

      default:
        throw new IllegalArgumentException("Unknown SASL authentication mechanism: " + sc.getMechanism());
      }
    } catch (LDAPException e) {
      processLDAPException(e);
    }
    return new Response<>(null, ResultCode.SUCCESS);
  }


  @Override
  public Response<Void> add(final AddRequest request)
    throws LdapException
  {
    Response<Void> response = null;
    try {
      final JLdapUtils bu = new JLdapUtils();
      final LDAPResponseQueue queue = connection.add(
        new LDAPEntry(request.getDn(), bu.fromLdapAttributes(request.getLdapAttributes())),
        null,
        getLDAPConstraints(request));
      final LDAPResponse lr = (LDAPResponse) queue.getResponse();
      response = createResponse(request, null, lr);
    } catch (LDAPException e) {
      processLDAPException(e);
    }
    return response;
  }


  @Override
  public Response<Boolean> compare(final CompareRequest request)
    throws LdapException
  {
    Response<Boolean> response = null;
    try {
      final JLdapUtils bu = new JLdapUtils();
      final LDAPResponseQueue queue = connection.compare(
        request.getDn(),
        bu.fromLdapAttribute(request.getAttribute()),
        null,
        getLDAPConstraints(request));
      final LDAPResponse lr = (LDAPResponse) queue.getResponse();
      response = createResponse(request, lr.getResultCode() == ResultCode.COMPARE_TRUE.value(), lr);
    } catch (LDAPException e) {
      processLDAPException(e);
    }
    return response;
  }


  @Override
  public Response<Void> delete(final DeleteRequest request)
    throws LdapException
  {
    Response<Void> response = null;
    try {
      final LDAPResponseQueue queue = connection.delete(request.getDn(), null, getLDAPConstraints(request));
      final LDAPResponse lr = (LDAPResponse) queue.getResponse();
      response = createResponse(request, null, lr);
    } catch (LDAPException e) {
      processLDAPException(e);
    }
    return response;
  }


  @Override
  public Response<Void> modify(final ModifyRequest request)
    throws LdapException
  {
    Response<Void> response = null;
    try {
      final JLdapUtils bu = new JLdapUtils();
      final LDAPResponseQueue queue = connection.modify(
        request.getDn(),
        bu.fromAttributeModification(request.getAttributeModifications()),
        null,
        getLDAPConstraints(request));
      final LDAPResponse lr = (LDAPResponse) queue.getResponse();
      response = createResponse(request, null, lr);
    } catch (LDAPException e) {
      processLDAPException(e);
    }
    return response;
  }


  @Override
  public Response<Void> modifyDn(final ModifyDnRequest request)
    throws LdapException
  {
    Response<Void> response = null;
    try {
      final String[] dn = request.getNewDn().split(",", 2);
      final LDAPResponseQueue queue = connection.rename(
        request.getDn(),
        dn[0],
        dn[1],
        request.getDeleteOldRDn(),
        null,
        getLDAPConstraints(request));
      final LDAPResponse lr = (LDAPResponse) queue.getResponse();
      response = createResponse(request, null, lr);
    } catch (LDAPException e) {
      processLDAPException(e);
    }
    return response;
  }


  @Override
  public SearchIterator search(final SearchRequest request)
    throws LdapException
  {
    final JLdapSearchIterator i = new JLdapSearchIterator(request);
    i.initialize();
    return i;
  }


  @Override
  public void searchAsync(final SearchRequest request, final SearchListener listener)
    throws LdapException
  {
    final JLdapAsyncSearchListener l = new JLdapAsyncSearchListener(request, listener);
    l.initialize();
  }


  @Override
  public void abandon(final int messageId, final RequestControl[] controls)
    throws LdapException
  {
    try {
      connection.abandon(messageId, getLDAPConstraints(controls));
    } catch (LDAPException e) {
      processLDAPException(e);
    }
  }


  @Override
  public Response<?> extendedOperation(final ExtendedRequest request)
    throws LdapException
  {
    Response<?> response = null;
    try {
      final LDAPExtendedResponse ldapExtRes = connection.extendedOperation(
        new LDAPExtendedOperation(request.getOID(), request.encode()),
        getLDAPConstraints(request));
      final ExtendedResponse<?> extRes = ExtendedResponseFactory.createExtendedResponse(
        request.getOID(),
        ldapExtRes.getID(),
        ldapExtRes.getValue());
      response = createResponse(request, extRes.getValue(), ldapExtRes);
    } catch (LDAPException e) {
      processLDAPException(e);
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
   * Returns an ldap constraints object configured with the supplied request.
   *
   * @param  request  request containing configuration to create constraints
   *
   * @return  ldap constraints
   */
  protected LDAPConstraints getLDAPConstraints(final Request request)
  {
    LDAPConstraints constraints = connection.getConstraints();
    if (constraints == null) {
      constraints = new LDAPConstraints();
    }
    if (request.getControls() != null) {
      constraints.setControls(config.getControlProcessor().processRequestControls(request.getControls()));
    }
    return constraints;
  }


  /**
   * Returns an ldap constraints object configured with the supplied controls.
   *
   * @param  controls  to sets in the constraints
   *
   * @return  ldap constraints
   */
  protected LDAPConstraints getLDAPConstraints(final RequestControl[] controls)
  {
    LDAPConstraints constraints = connection.getConstraints();
    if (constraints == null) {
      constraints = new LDAPConstraints();
    }
    if (controls != null) {
      constraints.setControls(config.getControlProcessor().processRequestControls(controls));
    }
    return constraints;
  }


  /**
   * Determines if the supplied response should result in an operation retry.
   *
   * @param  request  that produced the exception
   * @param  ldapResponse  provider response
   *
   * @throws  LdapException  wrapping the ldap exception
   */
  protected void throwOperationException(final Request request, final LDAPResponse ldapResponse)
    throws LdapException
  {
    ProviderUtils.throwOperationException(
      config.getOperationExceptionResultCodes(),
      String.format("Ldap returned result code: %s", ldapResponse.getResultCode()),
      ldapResponse.getResultCode(),
      ldapResponse.getMatchedDN(),
      config.getControlProcessor().processResponseControls(ldapResponse.getControls()),
      ldapResponse.getReferrals(),
      false);
  }


  /**
   * Creates an operation response with the supplied response data.
   *
   * @param  <T>  type of response
   * @param  request  containing controls
   * @param  result  of the operation
   * @param  ldapResponse  provider response
   *
   * @return  operation response
   */
  protected <T> Response<T> createResponse(final Request request, final T result, final LDAPResponse ldapResponse)
  {
    return
      new Response<>(
        result,
        ResultCode.valueOf(ldapResponse.getResultCode()),
        ldapResponse.getErrorMessage(),
        ldapResponse.getMatchedDN(),
        config.getControlProcessor().processResponseControls(ldapResponse.getControls()),
        ldapResponse.getReferrals(),
        ldapResponse.getMessageID());
  }


  /**
   * Determines if the supplied ldap exception should result in an operation retry.
   *
   * @param  e  that was produced
   *
   * @throws  LdapException  wrapping the ldap exception
   */
  protected void processLDAPException(final LDAPException e)
    throws LdapException
  {
    ProviderUtils.throwOperationException(
      config.getOperationExceptionResultCodes(),
      e,
      e.getResultCode(),
      e.getMatchedDN(),
      null,
      null,
      true);
  }


  /** Callback handler used by SASL mechanisms. */
  private static class SaslCallbackHandler implements CallbackHandler
  {

    /** user name. */
    private final String user;

    /** password. */
    private final char[] pass;


    /**
     * Creates a new bind callback handler.
     *
     * @param  u  username to bind with
     * @param  p  password to bind with
     */
    public SaslCallbackHandler(final String u, final String p)
    {
      user = u;
      if (p != null) {
        pass = p.toCharArray();
      } else {
        pass = null;
      }
    }


    @Override
    public void handle(final Callback[] callbacks)
      throws IOException, UnsupportedCallbackException
    {
      for (Callback cb : callbacks) {
        if (cb instanceof NameCallback) {
          // if user is null, the authzId will be used as it's the default name
          ((NameCallback) cb).setName(user != null ? user : ((NameCallback) cb).getDefaultName());
        } else if (cb instanceof PasswordCallback) {
          ((PasswordCallback) cb).setPassword(pass);
        } else if (cb instanceof RealmCallback) {
          ((RealmCallback) cb).setText(((RealmCallback) cb).getDefaultText());
        } else if (cb instanceof RealmChoiceCallback) {
          ((RealmChoiceCallback) cb).setSelectedIndex(0);
        }
      }
    }
  }


  /** Search iterator for JLdap search results. */
  protected class JLdapSearchIterator extends AbstractJLdapSearch implements SearchIterator
  {

    /** Response data. */
    private Response<Void> response;

    /** Ldap search result iterator. */
    private SearchResultIterator resultIterator;


    /**
     * Creates a new jldap search iterator.
     *
     * @param  sr  search request
     */
    public JLdapSearchIterator(final SearchRequest sr)
    {
      super(sr);
    }


    /**
     * Initializes this jldap search iterator.
     *
     * @throws  LdapException  if an error occurs
     */
    public void initialize()
      throws LdapException
    {
      try {
        resultIterator = new SearchResultIterator(search(connection, request));
      } catch (LDAPException e) {
        processLDAPException(e);
      }
    }


    @Override
    public boolean hasNext()
      throws LdapException
    {
      if (resultIterator == null || response != null) {
        return false;
      }

      boolean more = false;
      try {
        more = resultIterator.hasNext();
        if (!more) {
          final LDAPResponse res = resultIterator.getResponse();
          logger.trace("reading search response: {}", res);
          throwOperationException(request, res);
          response = createResponse(request, null, res);
        }
      } catch (LDAPException e) {
        final ResultCode rc = ignoreSearchException(config.getSearchIgnoreResultCodes(), e);
        if (rc == null) {
          processLDAPException(e);
        }
        response = new Response<>(null, rc, e.getLDAPErrorMessage(), null, null, null, -1);
      }
      return more;
    }


    @Override
    public SearchItem next()
      throws LdapException
    {
      SearchItem si;
      final LDAPMessage message = resultIterator.next();
      if (message instanceof LDAPSearchResult) {
        si = processLDAPSearchResult((LDAPSearchResult) message);
      } else if (message instanceof LDAPSearchResultReference) {
        si = processLDAPSearchResultReference((LDAPSearchResultReference) message);
      } else if (message instanceof LDAPIntermediateResponse) {
        si = processLDAPIntermediateResponse((LDAPIntermediateResponse) message);
      } else {
        throw new IllegalStateException("Unknown message: " + message);
      }
      return si;
    }


    @Override
    public Response<Void> getResponse()
    {
      return response;
    }


    @Override
    public void close()
      throws LdapException {}
  }


  /** Async search listener for JLdap search results. */
  protected class JLdapAsyncSearchListener extends AbstractJLdapSearch
  {

    /** Search result listener. */
    private final SearchListener listener;


    /**
     * Creates a new jldap async search listener.
     *
     * @param  sr  search request
     * @param  sl  search listener
     */
    public JLdapAsyncSearchListener(final SearchRequest sr, final SearchListener sl)
    {
      super(sr);
      listener = sl;
    }


    /**
     * Initializes this jldap search listener.
     *
     * @throws  LdapException  if an error occurs
     */
    public void initialize()
      throws LdapException
    {
      try {
        search(connection, request);
      } catch (LDAPException e) {
        processLDAPException(e);
      }
    }


    /**
     * Executes an ldap search.
     *
     * @param  conn  to search with
     * @param  sr  to read properties from
     *
     * @return  ldap search queue
     *
     * @throws  LDAPException  if an error occurs
     */
    protected LDAPSearchQueue search(final LDAPConnection conn, final SearchRequest sr)
      throws LDAPException
    {
      final SearchResultIterator i = new SearchResultIterator(super.search(conn, sr));
      listener.asyncRequestReceived(new JLdapAsyncRequest(i.getLDAPSearchQueue()));
      while (i.hasNext()) {
        final LDAPMessage message = i.next();
        if (message instanceof LDAPSearchResult) {
          listener.searchItemReceived(processLDAPSearchResult((LDAPSearchResult) message));
        } else if (message instanceof LDAPSearchResultReference) {
          listener.searchItemReceived(processLDAPSearchResultReference((LDAPSearchResultReference) message));
        } else if (message instanceof LDAPIntermediateResponse) {
          listener.searchItemReceived(processLDAPIntermediateResponse((LDAPIntermediateResponse) message));
        } else {
          throw new IllegalStateException("Unknown message: " + message);
        }
      }

      final Response<Void> response = createResponse(request, null, i.getResponse());
      listener.responseReceived(response);
      return null;
    }
  }


  /** Common search functionality for jldap iterators and listeners. */
  protected abstract class AbstractJLdapSearch
  {

    /** Search request. */
    protected final SearchRequest request;

    /** Utility class. */
    protected final JLdapUtils util;


    /**
     * Creates a new abstract jldap search.
     *
     * @param  sr  search request
     */
    public AbstractJLdapSearch(final SearchRequest sr)
    {
      request = sr;
      util = new JLdapUtils(request.getSortBehavior());
      util.setBinaryAttributes(request.getBinaryAttributes());
    }


    /**
     * Executes an ldap search.
     *
     * @param  conn  to search with
     * @param  sr  to read properties from
     *
     * @return  ldap search queue
     *
     * @throws  LDAPException  if an error occurs
     */
    protected LDAPSearchQueue search(final LDAPConnection conn, final SearchRequest sr)
      throws LDAPException
    {
      final LDAPSearchConstraints constraints = getLDAPSearchConstraints(sr);
      final LDAPControl[] lc = config.getControlProcessor().processRequestControls(sr.getControls());
      if (lc != null) {
        constraints.setControls(lc);
      }
      return
        conn.search(
          sr.getBaseDn(),
          getSearchScope(sr.getSearchScope()),
          sr.getSearchFilter() != null ? sr.getSearchFilter().format() : null,
          sr.getReturnAttributes(),
          sr.getTypesOnly(),
          null,
          constraints);
    }


    /**
     * Returns the jldap integer constant for the supplied search scope.
     *
     * @param  ss  search scope
     *
     * @return  integer constant
     */
    protected int getSearchScope(final SearchScope ss)
    {
      int scope = -1;
      if (ss == SearchScope.OBJECT) {
        scope = LDAPConnection.SCOPE_BASE;
      } else if (ss == SearchScope.ONELEVEL) {
        scope = LDAPConnection.SCOPE_ONE;
      } else if (ss == SearchScope.SUBTREE) {
        scope = LDAPConnection.SCOPE_SUB;
      }
      return scope;
    }


    /**
     * Returns an ldap search constraints object configured with the supplied search request.
     *
     * @param  sr  search request containing configuration to create search constraints
     *
     * @return  ldap search constraints
     */
    protected LDAPSearchConstraints getLDAPSearchConstraints(final SearchRequest sr)
    {
      LDAPSearchConstraints constraints = connection.getSearchConstraints();
      if (constraints == null) {
        constraints = new LDAPSearchConstraints();
      }
      constraints.setServerTimeLimit(Long.valueOf(sr.getTimeLimit()).intValue());
      constraints.setMaxResults(Long.valueOf(sr.getSizeLimit()).intValue());
      if (sr.getDerefAliases() != null) {
        if (sr.getDerefAliases() == DerefAliases.ALWAYS) {
          constraints.setDereference(LDAPSearchConstraints.DEREF_ALWAYS);
        } else if (sr.getDerefAliases() == DerefAliases.FINDING) {
          constraints.setDereference(LDAPSearchConstraints.DEREF_FINDING);
        } else if (sr.getDerefAliases() == DerefAliases.NEVER) {
          constraints.setDereference(LDAPSearchConstraints.DEREF_NEVER);
        } else if (sr.getDerefAliases() == DerefAliases.SEARCHING) {
          constraints.setDereference(LDAPSearchConstraints.DEREF_SEARCHING);
        }
      }
      return constraints;
    }


    /**
     * Determines whether the supplied ldap exception should be ignored.
     *
     * @param  ignoreResultCodes  to match against the exception
     * @param  e  ldap exception to match
     *
     * @return  result code that should be ignored or null
     */
    protected ResultCode ignoreSearchException(final ResultCode[] ignoreResultCodes, final LDAPException e)
    {
      ResultCode ignore = null;
      if (ignoreResultCodes != null && ignoreResultCodes.length > 0) {
        for (ResultCode rc : ignoreResultCodes) {
          if (e.getResultCode() == rc.value()) {
            logger.debug("Ignoring ldap exception", e);
            ignore = rc;
            break;
          }
        }
      }
      return ignore;
    }


    /**
     * Processes the response controls on the supplied result and returns a corresponding search item.
     *
     * @param  res  to process
     *
     * @return  search item
     */
    protected SearchItem processLDAPSearchResult(final LDAPSearchResult res)
    {
      logger.trace("reading search result: {}", res);

      ResponseControl[] respControls = null;
      if (res.getControls() != null && res.getControls().length > 0) {
        respControls = config.getControlProcessor().processResponseControls(res.getControls());
      }

      final SearchEntry se = util.toSearchEntry(res.getEntry(), respControls, res.getMessageID());
      return new SearchItem(se);
    }


    /**
     * Processes the response controls on the supplied reference and returns a corresponding search item.
     *
     * @param  ref  to process
     *
     * @return  search item
     */
    protected SearchItem processLDAPSearchResultReference(final LDAPSearchResultReference ref)
    {
      logger.trace("reading search reference: {}", ref);

      ResponseControl[] respControls = null;
      if (ref.getControls() != null && ref.getControls().length > 0) {
        respControls = config.getControlProcessor().processResponseControls(ref.getControls());
      }

      final SearchReference sr = new SearchReference(ref.getMessageID(), respControls, ref.getReferrals());
      return new SearchItem(sr);
    }


    /**
     * Processes the response controls on the supplied response and returns a corresponding search item.
     *
     * @param  res  to process
     *
     * @return  search item
     */
    protected SearchItem processLDAPIntermediateResponse(final LDAPIntermediateResponse res)
    {
      logger.trace("reading intermediate response: {}", res);

      ResponseControl[] respControls = null;
      if (res.getControls() != null && res.getControls().length > 0) {
        respControls = config.getControlProcessor().processResponseControls(res.getControls());
      }

      final org.ldaptive.intermediate.IntermediateResponse ir = IntermediateResponseFactory.createIntermediateResponse(
        res.getID(),
        res.getValue(),
        respControls,
        res.getMessageID());
      return new SearchItem(ir);
    }
  }


  /** Iterates over an ldap search queue. */
  protected static class SearchResultIterator
  {

    /** Queue to iterate over. */
    private final LDAPSearchQueue queue;

    /** Last response message received from the queue. */
    private LDAPMessage message;

    /** Response available after all messages have been received. */
    private LDAPResponse response;


    /**
     * Create a new ldap result iterator.
     *
     * @param  q  ldap search queue
     */
    public SearchResultIterator(final LDAPSearchQueue q)
    {
      queue = q;
    }


    /**
     * Returns the search queue used by this iterator.
     *
     * @return  ldap search queue
     */
    public LDAPSearchQueue getLDAPSearchQueue()
    {
      return queue;
    }


    /**
     * Returns whether the queue has another message to read.
     *
     * @return  whether the queue has another message to read
     *
     * @throws  LDAPException  if an error occurs reading the response
     */
    public boolean hasNext()
      throws LDAPException
    {
      if (response != null) {
        return false;
      }

      boolean more = false;
      message = queue.getResponse();
      if (message != null) {
        if (message instanceof LDAPSearchResult) {
          more = true;
        } else if (message instanceof LDAPSearchResultReference) {
          more = true;
        } else if (message instanceof LDAPIntermediateResponse) {
          more = true;
        } else {
          response = (LDAPResponse) message;
        }
      }
      return more;
    }


    /**
     * Returns the next message in the queue.
     *
     * @return  ldap message
     */
    public LDAPMessage next()
    {
      return message;
    }


    /**
     * Returns the search response. Available after all messages have been read from the queue.
     *
     * @return  ldap search response
     */
    public LDAPResponse getResponse()
    {
      return response;
    }
  }


  /** Async request to invoke abandons. */
  protected class JLdapAsyncRequest implements AsyncRequest
  {

    /** Message queue. */
    private final LDAPMessageQueue messageQueue;


    /**
     * Creates a new JLDAP async request.
     *
     * @param  queue  from an async operation
     */
    public JLdapAsyncRequest(final LDAPMessageQueue queue)
    {
      messageQueue = queue;
    }


    @Override
    public int getMessageId()
    {
      final int[] ids = messageQueue.getMessageIDs();
      if (ids == null || ids.length == 0) {
        return -1;
      }
      return ids[ids.length - 1];
    }


    @Override
    public void abandon()
      throws LdapException
    {
      try {
        LDAPConstraints constraints = connection.getConstraints();
        if (constraints == null) {
          constraints = new LDAPConstraints();
        }
        connection.abandon(messageQueue, constraints);
      } catch (LDAPException e) {
        processLDAPException(e);
      }
    }


    @Override
    public void abandon(final RequestControl[] controls)
      throws LdapException
    {
      try {
        LDAPConstraints constraints = connection.getConstraints();
        if (constraints == null) {
          constraints = new LDAPConstraints();
        }
        if (controls != null) {
          constraints.setControls(config.getControlProcessor().processRequestControls(controls));
        }
        connection.abandon(messageQueue, constraints);
      } catch (LDAPException e) {
        processLDAPException(e);
      }
    }
  }


  /** Allows the use of multiple unsolicited notification handlers per connection. */
  protected class AggregateUnsolicitedNotificationListener implements LDAPUnsolicitedNotificationListener
  {

    /** Listeners to receive unsolicited notifications. */
    private final List<UnsolicitedNotificationListener> listeners = new ArrayList<>();


    /**
     * Adds an unsolicited notification listener to this handler.
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
     * Removes an unsolicited notification listener from this handler.
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
    public void messageReceived(final LDAPExtendedResponse extendedResponse)
    {
      logger.debug("Unsolicited notification received: {}", extendedResponse);
      synchronized (listeners) {
        final Response<Void> response = createResponse(null, null, extendedResponse);
        for (UnsolicitedNotificationListener listener : listeners) {
          listener.notificationReceived(extendedResponse.getID(), response);
        }
      }
    }
  }
}
