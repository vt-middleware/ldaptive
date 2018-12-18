/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.jndi;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.ReferralException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.LdapReferralException;
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
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchReference;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.control.RequestControl;
import org.ldaptive.control.ResponseControl;
import org.ldaptive.extended.ExtendedRequest;
import org.ldaptive.extended.ExtendedResponse;
import org.ldaptive.extended.ExtendedResponseFactory;
import org.ldaptive.extended.UnsolicitedNotificationListener;
import org.ldaptive.provider.ControlProcessor;
import org.ldaptive.provider.ProviderConnection;
import org.ldaptive.provider.ProviderUtils;
import org.ldaptive.provider.SearchItem;
import org.ldaptive.provider.SearchIterator;
import org.ldaptive.provider.SearchListener;
import org.ldaptive.sasl.DigestMd5Config;
import org.ldaptive.sasl.SaslConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JNDI provider implementation of ldap operations.
 *
 * @author  Middleware Services
 */
public class JndiConnection implements ProviderConnection
{

  /**
   * The value of this property is a string that specifies the authentication mechanism(s) for the provider to use. The
   * value of this constant is {@value}.
   */
  public static final String AUTHENTICATION = "java.naming.security.authentication";

  /**
   * The value of this property is an object that specifies the credentials of the principal to be authenticated. The
   * value of this constant is {@value}.
   */
  public static final String CREDENTIALS = "java.naming.security.credentials";

  /**
   * The value of this property is a string that specifies the identity of the principal to be authenticated. The value
   * of this constant is {@value}.
   */
  public static final String PRINCIPAL = "java.naming.security.principal";

  /**
   * The value of this property is a string that specifies the sasl authorization id. The value of this constant is
   * {@value}.
   */
  public static final String SASL_AUTHZ_ID = "java.naming.security.sasl.authorizationId";

  /**
   * The value of this property is a string that specifies the sasl quality of protection. The value of this constant is
   * {@value}.
   */
  public static final String SASL_QOP = "javax.security.sasl.qop";

  /**
   * The value of this property is a string that specifies the sasl security strength. The value of this constant is
   * {@value}.
   */
  public static final String SASL_STRENGTH = "javax.security.sasl.strength";

  /**
   * The value of this property is a string that specifies the sasl mutual authentication flag. The value of this
   * constant is {@value}.
   */
  public static final String SASL_MUTUAL_AUTH = "javax.security.sasl.server.authentication";

  /** The value of this property is a string that specifies the sasl realm. The value of this constant is {@value}. */
  public static final String SASL_REALM = "java.naming.security.sasl.realm";

  /**
   * The value of this property is a string that specifies whether the RDN attribute should be deleted for a modify dn
   * operation. The value of this constant is {@value}.
   */
  public static final String DELETE_RDN = "java.naming.ldap.deleteRDN";

  /**
   * The value of this property is a string that specifies additional binary attributes. The value of this constant is
   * {@value}.
   */
  public static final String BINARY_ATTRIBUTES = "java.naming.ldap.attributes.binary";

  /**
   * The value of this property is a string that specifies how aliases shall be handled by the provider. The value of
   * this constant is {@value}.
   */
  public static final String DEREF_ALIASES = "java.naming.ldap.derefAliases";

  /**
   * The value of this property is a string that specifies how referrals shall be handled by the provider. The value of
   * this constant is {@value}.
   */
  public static final String REFERRAL = "java.naming.referral";

  /**
   * The value of this property is a string that specifies to only return attribute type names, no values. The value of
   * this constant is {@value}.
   */
  public static final String TYPES_ONLY = "java.naming.ldap.typesOnly";

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Ldap context. */
  private LdapContext context;

  /** Provider configuration. */
  private final JndiProviderConfig config;


  /**
   * Creates a new jndi connection.
   *
   * @param  lc  ldap context
   * @param  pc  provider configuration
   */
  public JndiConnection(final LdapContext lc, final JndiProviderConfig pc)
  {
    context = lc;
    config = pc;
  }


  /**
   * Returns the underlying ldap context.
   *
   * @return  ldap context
   */
  public LdapContext getLdapContext()
  {
    return context;
  }


  @Override
  public void close(final RequestControl[] controls)
    throws LdapException
  {
    if (controls != null) {
      throw new UnsupportedOperationException("Provider does not support unbind with controls");
    }
    try {
      if (context != null) {
        context.close();
      }
    } catch (NamingException e) {
      ResultCode rc = NamingExceptionUtils.getResultCode(e.getClass());
      if (rc == null) {
        rc = NamingExceptionUtils.getResultCode(e.getMessage());
      }
      throw new LdapException(e, rc);
    } finally {
      context = null;
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
      context.addToEnvironment(AUTHENTICATION, "none");
      context.removeFromEnvironment(PRINCIPAL);
      context.removeFromEnvironment(CREDENTIALS);
      context.reconnect(config.getControlProcessor().processRequestControls(request.getControls()));
      response = createResponse(request, null, ResultCode.SUCCESS, null, context);
    } catch (ReferralException e) {
      final String[] refUrls = e.getReferralInfo() != null ? new String[] {(String) e.getReferralInfo()} : null;
      response = createResponse(request, null, ResultCode.REFERRAL, refUrls, context);
    } catch (NamingException e) {
      processNamingException(request, e, null, context);
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
      context.addToEnvironment(AUTHENTICATION, "simple");
      context.addToEnvironment(PRINCIPAL, request.getDn());
      context.addToEnvironment(CREDENTIALS, request.getCredential().getBytes());
      context.reconnect(config.getControlProcessor().processRequestControls(request.getControls()));
      response = createResponse(request, null, ResultCode.SUCCESS, null, context);
    } catch (ReferralException e) {
      final String[] refUrls = e.getReferralInfo() != null ? new String[] {(String) e.getReferralInfo()} : null;
      response = createResponse(request, null, ResultCode.REFERRAL, refUrls, context);
    } catch (NamingException e) {
      processNamingException(request, e, null, context);
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
      final String authenticationType = JndiUtils.getAuthenticationType(request.getSaslConfig().getMechanism());
      for (Map.Entry<String, Object> entry : getSaslProperties(request.getSaslConfig()).entrySet()) {
        context.addToEnvironment(entry.getKey(), entry.getValue());
      }
      context.addToEnvironment(AUTHENTICATION, authenticationType);
      if (request.getDn() != null) {
        context.addToEnvironment(PRINCIPAL, request.getDn());
        if (request.getCredential() != null) {
          context.addToEnvironment(CREDENTIALS, request.getCredential().getBytes());
        }
      }
      context.reconnect(config.getControlProcessor().processRequestControls(request.getControls()));
      response = createResponse(request, null, ResultCode.SUCCESS, null, context);
    } catch (ReferralException e) {
      final String[] refUrls = e.getReferralInfo() != null ? new String[] {(String) e.getReferralInfo()} : null;
      response = createResponse(request, null, ResultCode.REFERRAL, refUrls, context);
    } catch (NamingException e) {
      processNamingException(request, e, null, context);
    }
    return response;
  }


  @Override
  public Response<Void> add(final AddRequest request)
    throws LdapException
  {
    Response<Void> response = null;
    LdapContext ctx = null;
    try {
      try {
        ctx = initializeContext(request);

        final JndiUtils bu = new JndiUtils();
        ctx.createSubcontext(new LdapName(request.getDn()), bu.fromLdapAttributes(request.getLdapAttributes())).close();
        response = createResponse(request, null, ResultCode.SUCCESS, null, ctx);
      } finally {
        if (ctx != null) {
          ctx.close();
        }
      }
    } catch (ReferralException e) {
      final String[] refUrls = e.getReferralInfo() != null ? new String[] {(String) e.getReferralInfo()} : null;
      response = createResponse(request, null, ResultCode.REFERRAL, refUrls, ctx);
    } catch (NamingException e) {
      processNamingException(request, e, null, ctx);
    }
    return response;
  }


  @Override
  public Response<Boolean> compare(final CompareRequest request)
    throws LdapException
  {
    Response<Boolean> response = null;
    LdapContext ctx = null;
    try {
      NamingEnumeration<SearchResult> en = null;
      try {
        ctx = initializeContext(request);
        en = ctx.search(
          new LdapName(request.getDn()),
          String.format("(%s={0})", request.getAttribute().getName()),
          request.getAttribute().isBinary() ? new Object[] {request.getAttribute().getBinaryValue()} : new Object[] {
            request.getAttribute().getStringValue(),
          },
          getCompareSearchControls());

        final boolean success = en.hasMore();
        response = createResponse(
          request,
          success,
          success ? ResultCode.COMPARE_TRUE : ResultCode.COMPARE_FALSE,
          null,
          ctx);
      } finally {
        if (en != null) {
          en.close();
        }
        if (ctx != null) {
          ctx.close();
        }
      }
    } catch (ReferralException e) {
      final String[] refUrls = e.getReferralInfo() != null ? new String[] {(String) e.getReferralInfo()} : null;
      response = createResponse(request, null, ResultCode.REFERRAL, refUrls, ctx);
    } catch (NamingException e) {
      processNamingException(request, e, null, ctx);
    }
    return response;
  }


  @Override
  public Response<Void> delete(final DeleteRequest request)
    throws LdapException
  {
    Response<Void> response = null;
    LdapContext ctx = null;
    try {
      try {
        ctx = initializeContext(request);
        ctx.destroySubcontext(new LdapName(request.getDn()));
        response = createResponse(request, null, ResultCode.SUCCESS, null, ctx);
      } finally {
        if (ctx != null) {
          ctx.close();
        }
      }
    } catch (ReferralException e) {
      final String[] refUrls = e.getReferralInfo() != null ? new String[] {(String) e.getReferralInfo()} : null;
      response = createResponse(request, null, ResultCode.REFERRAL, refUrls, ctx);
    } catch (NamingException e) {
      processNamingException(request, e, null, ctx);
    }
    return response;
  }


  @Override
  public Response<Void> modify(final ModifyRequest request)
    throws LdapException
  {
    Response<Void> response = null;
    LdapContext ctx = null;
    try {
      try {
        ctx = initializeContext(request);

        final JndiUtils bu = new JndiUtils();
        ctx.modifyAttributes(
          new LdapName(request.getDn()),
          bu.fromAttributeModification(request.getAttributeModifications()));
        response = createResponse(request, null, ResultCode.SUCCESS, null, ctx);
      } finally {
        if (ctx != null) {
          ctx.close();
        }
      }
    } catch (ReferralException e) {
      final String[] refUrls = e.getReferralInfo() != null ? new String[] {(String) e.getReferralInfo()} : null;
      response = createResponse(request, null, ResultCode.REFERRAL, refUrls, ctx);
    } catch (NamingException e) {
      processNamingException(request, e, null, ctx);
    }
    return response;
  }


  @Override
  public Response<Void> modifyDn(final ModifyDnRequest request)
    throws LdapException
  {
    Response<Void> response = null;
    LdapContext ctx = null;
    try {
      try {
        ctx = initializeContext(request);
        ctx.addToEnvironment("java.naming.ldap.deleteRDN", Boolean.valueOf(request.getDeleteOldRDn()).toString());
        ctx.rename(new LdapName(request.getDn()), new LdapName(request.getNewDn()));
        response = createResponse(request, null, ResultCode.SUCCESS, null, ctx);
      } finally {
        if (ctx != null) {
          ctx.close();
        }
      }
    } catch (ReferralException e) {
      final String[] refUrls = e.getReferralInfo() != null ? new String[] {(String) e.getReferralInfo()} : null;
      response = createResponse(request, null, ResultCode.REFERRAL, refUrls, ctx);
    } catch (NamingException e) {
      processNamingException(request, e, null, ctx);
    }
    return response;
  }


  @Override
  public SearchIterator search(final SearchRequest request)
    throws LdapException
  {
    final JndiSearchIterator i = new JndiSearchIterator(request);
    i.initialize();
    return i;
  }


  @Override
  public void searchAsync(final SearchRequest request, final SearchListener listener)
    throws LdapException
  {
    throw new UnsupportedOperationException("Asynchronous searches not supported");
  }


  @Override
  public void abandon(final int messageId, final RequestControl[] controls)
    throws LdapException
  {
    throw new UnsupportedOperationException("Abandons not supported");
  }


  @Override
  public Response<?> extendedOperation(final ExtendedRequest request)
    throws LdapException
  {
    Response<?> response = null;
    LdapContext ctx = null;
    try {
      try {
        ctx = initializeContext(request);

        final JndiExtendedResponse jndiExtRes = (JndiExtendedResponse) ctx.extendedOperation(
          new JndiExtendedRequest(request.getOID(), request.encode()));
        final ExtendedResponse<?> extRes = ExtendedResponseFactory.createExtendedResponse(
          request.getOID(),
          jndiExtRes.getID(),
          jndiExtRes.getEncodedValue());
        response = createResponse(request, extRes.getValue(), ResultCode.SUCCESS, null, ctx);
      } finally {
        if (ctx != null) {
          ctx.close();
        }
      }
    } catch (ReferralException e) {
      final String[] refUrls = e.getReferralInfo() != null ? new String[] {(String) e.getReferralInfo()} : null;
      response = createResponse(request, null, ResultCode.REFERRAL, refUrls, ctx);
    } catch (NamingException e) {
      processNamingException(request, e, null, ctx);
    }
    return response;
  }


  @Override
  public void addUnsolicitedNotificationListener(final UnsolicitedNotificationListener listener)
  {
    throw new UnsupportedOperationException("Unsolicited notifications not supported");
  }


  @Override
  public void removeUnsolicitedNotificationListener(final UnsolicitedNotificationListener listener)
  {
    throw new UnsupportedOperationException("Unsolicited notifications not supported");
  }


  /**
   * Returns a search controls object configured to perform an LDAP compare operation.
   *
   * @return  search controls
   */
  public static SearchControls getCompareSearchControls()
  {
    final SearchControls ctls = new SearchControls();
    ctls.setReturningAttributes(new String[0]);
    ctls.setSearchScope(SearchScope.OBJECT.ordinal());
    return ctls;
  }


  /**
   * Creates a new ldap context using {@link LdapContext#newInstance(Control[])}. Adds any additional environment
   * properties found in the supplied request to the context.
   *
   * @param  request  to read properties from
   *
   * @return  ldap context
   *
   * @throws  NamingException  if a property cannot be added to the context
   */
  protected LdapContext initializeContext(final Request request)
    throws NamingException
  {
    final LdapContext ctx = context.newInstance(
      config.getControlProcessor().processRequestControls(request.getControls()));

    // by default set referral behavior to throw, otherwise jndi will send the
    // ManageDsaIT control
    ctx.addToEnvironment(REFERRAL, "throw");
    return ctx;
  }


  /**
   * Creates an operation response with the supplied response data.
   *
   * @param  <T>  type of response
   * @param  request  containing controls
   * @param  result  of the operation
   * @param  code  operation result code
   * @param  urls  referral urls
   * @param  ctx  ldap context
   *
   * @return  operation response
   */
  protected <T> Response<T> createResponse(
    final Request request,
    final T result,
    final ResultCode code,
    final String[] urls,
    final LdapContext ctx)
  {
    return
      new Response<>(
        result,
        code,
        null,
        null,
        processResponseControls(config.getControlProcessor(), request.getControls(), ctx),
        urls,
        -1);
  }


  /**
   * Determines if the supplied naming exception should result in an operation retry.
   *
   * @param  request  that produced the exception
   * @param  e  that was produced
   * @param  urls  referral urls
   * @param  ctx  that the exception occurred on
   *
   * @throws  LdapException  wrapping the naming exception
   */
  protected void processNamingException(
    final Request request,
    final NamingException e,
    final String[] urls,
    final LdapContext ctx)
    throws LdapException
  {
    ResultCode rc = NamingExceptionUtils.getResultCode(e.getClass());
    if (rc == null) {
      rc = NamingExceptionUtils.getResultCode(e.getMessage());
    }
    ProviderUtils.throwOperationException(
      config.getOperationExceptionResultCodes(),
      e,
      rc != null ? rc.value() : -1,
      null,
      processResponseControls(config.getControlProcessor(), request.getControls(), ctx),
      urls,
      true);
  }


  /**
   * Retrieves the response controls from the supplied context and processes them with the supplied control processor.
   * Logs a warning if controls cannot be retrieved.
   *
   * @param  processor  control processor
   * @param  requestControls  that produced this response
   * @param  ctx  to get controls from
   *
   * @return  response controls
   */
  protected ResponseControl[] processResponseControls(
    final ControlProcessor<Control> processor,
    final RequestControl[] requestControls,
    final LdapContext ctx)
  {
    ResponseControl[] ctls = null;
    if (ctx != null) {
      try {
        ctls = processor.processResponseControls(ctx.getResponseControls());
      } catch (NamingException e) {
        final Logger l = LoggerFactory.getLogger(JndiUtils.class);
        l.warn("Error retrieving response controls.", e);
      }
    }
    return ctls;
  }


  /**
   * Returns the JNDI properties for the supplied sasl configuration.
   *
   * @param  config  sasl configuration
   *
   * @return  JNDI properties for use in a context environment
   */
  protected static Map<String, Object> getSaslProperties(final SaslConfig config)
  {
    final Map<String, Object> env = new HashMap<>();
    if (config.getAuthorizationId() != null && !"".equals(config.getAuthorizationId())) {
      env.put(SASL_AUTHZ_ID, config.getAuthorizationId());
    }
    if (config.getQualityOfProtection() != null) {
      env.put(SASL_QOP, JndiUtils.getQualityOfProtection(config.getQualityOfProtection()));
    }
    if (config.getSecurityStrength() != null) {
      env.put(SASL_STRENGTH, JndiUtils.getSecurityStrength(config.getSecurityStrength()));
    }
    if (config.getMutualAuthentication() != null) {
      env.put(SASL_MUTUAL_AUTH, config.getMutualAuthentication().toString());
    }
    if (config instanceof DigestMd5Config) {
      if (((DigestMd5Config) config).getRealm() != null) {
        env.put(SASL_REALM, ((DigestMd5Config) config).getRealm());
      }
    }
    return env;
  }


  /** Search iterator for JNDI naming enumeration. */
  protected class JndiSearchIterator implements SearchIterator
  {

    /** Search request. */
    private final SearchRequest request;

    /** Response data. */
    private Response<Void> response;

    /** Response result code. */
    private ResultCode responseResultCode;

    /** Search reference URLs. */
    private List<String> searchReferences;

    /** Ldap context to search with. */
    private LdapContext searchContext;

    /** Results read from the search operation. */
    private NamingEnumeration<SearchResult> results;


    /**
     * Creates a new jndi search iterator.
     *
     * @param  sr  search request
     */
    public JndiSearchIterator(final SearchRequest sr)
    {
      request = sr;
    }


    /**
     * Initializes this jndi search iterator.
     *
     * @throws  LdapException  if an error occurs
     */
    public void initialize()
      throws LdapException
    {
      boolean closeContext = false;
      try {
        searchContext = context.newInstance(config.getControlProcessor().processRequestControls(request.getControls()));
        initializeSearchContext(searchContext, request);
        results = search(searchContext, request);
      } catch (LdapReferralException e) {
        closeContext = true;
        response = createResponse(request, null, ResultCode.REFERRAL, readReferralUrls(e), searchContext);
      } catch (NamingException e) {
        closeContext = true;
        processNamingException(request, e, null, searchContext);
      } finally {
        if (closeContext) {
          try {
            if (searchContext != null) {
              searchContext.close();
            }
          } catch (NamingException e) {
            logger.debug("Problem closing context", e);
          }
        }
      }
    }


    /**
     * Adds any additional environment properties found in the supplied request to the supplied context.
     *
     * @param  ctx  to initialize for searching
     * @param  sr  to read properties from
     *
     * @throws  NamingException  if a property cannot be added to the context
     */
    protected void initializeSearchContext(final LdapContext ctx, final SearchRequest sr)
      throws NamingException
    {
      // by default set referral behavior to throw, otherwise jndi will send the
      // ManageDsaIT control
      ctx.addToEnvironment(REFERRAL, "throw");
      // by default set dereferencing aliases to never, jndi default is always
      if (sr.getDerefAliases() != null) {
        ctx.addToEnvironment(DEREF_ALIASES, sr.getDerefAliases().name().toLowerCase());
      } else {
        ctx.addToEnvironment(DEREF_ALIASES, DerefAliases.NEVER.name().toLowerCase());
      }
      if (sr.getBinaryAttributes() != null) {
        final String[] a = sr.getBinaryAttributes();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
          sb.append(a[i]);
          if (i < a.length - 1) {
            sb.append(" ");
          }
        }
        ctx.addToEnvironment(BINARY_ATTRIBUTES, sb.toString());
      }
      if (sr.getTypesOnly()) {
        ctx.addToEnvironment(TYPES_ONLY, Boolean.valueOf(sr.getTypesOnly()).toString());
      }
    }


    /**
     * Executes {@link LdapContext#search( javax.naming.Name, String, Object[], SearchControls)}.
     *
     * @param  ctx  to search
     * @param  sr  to read properties from
     *
     * @return  naming enumeration of search results
     *
     * @throws  NamingException  if an error occurs
     */
    protected NamingEnumeration<SearchResult> search(final LdapContext ctx, final SearchRequest sr)
      throws NamingException
    {
      return
        ctx.search(
          new LdapName(sr.getBaseDn()),
          sr.getSearchFilter() != null ? request.getSearchFilter().format() : null,
          getSearchControls(sr));
    }


    /**
     * Returns a search controls object configured with the supplied search request.
     *
     * @param  sr  search request containing configuration to create search controls
     *
     * @return  search controls
     */
    protected SearchControls getSearchControls(final SearchRequest sr)
    {
      final SearchControls ctls = new SearchControls();
      if (ReturnAttributes.DEFAULT.equalsAttributes(sr.getReturnAttributes())) {
        ctls.setReturningAttributes(null);
      } else {
        ctls.setReturningAttributes(sr.getReturnAttributes());
      }

      final int searchScope = getSearchScope(sr.getSearchScope());
      if (searchScope != -1) {
        ctls.setSearchScope(searchScope);
      }
      ctls.setTimeLimit((int) sr.getTimeLimit().toMillis());
      ctls.setCountLimit(sr.getSizeLimit());
      ctls.setDerefLinkFlag(false);
      // note that if returning obj flag is set to true, object contexts on the
      // SearchResult must the explicitly closed:
      // ctx = (Context) SearchResult#getObject(); ctx.close();
      ctls.setReturningObjFlag(false);
      return ctls;
    }


    /**
     * Returns the jndi integer constant for the supplied search scope.
     *
     * @param  ss  search scope
     *
     * @return  integer constant
     */
    protected int getSearchScope(final SearchScope ss)
    {
      int scope = -1;
      if (ss == SearchScope.OBJECT) {
        scope = SearchControls.OBJECT_SCOPE;
      } else if (ss == SearchScope.ONELEVEL) {
        scope = SearchControls.ONELEVEL_SCOPE;
      } else if (ss == SearchScope.SUBTREE) {
        scope = SearchControls.SUBTREE_SCOPE;
      }
      return scope;
    }


    @Override
    public boolean hasNext()
      throws LdapException
    {
      if (results == null || response != null) {
        return false;
      }

      boolean more = false;
      if (searchReferences != null) {
        more = true;
      } else {
        try {
          more = results.hasMore();
          if (!more) {
            response = createResponse(
              request,
              null,
              responseResultCode != null ? responseResultCode : ResultCode.SUCCESS,
              null,
              searchContext);
          }
        } catch (LdapReferralException e) {
          searchReferences = new ArrayList<>(Arrays.asList(readReferralUrls(e)));
          more = true;
        } catch (NamingException e) {
          final ResultCode ignoreRc = ignoreSearchException(config.getSearchIgnoreResultCodes(), e);
          if (ignoreRc == null) {
            processNamingException(request, e, null, searchContext);
          }
          response = createResponse(request, null, ignoreRc, null, searchContext);
        }
      }
      return more;
    }


    @Override
    public SearchItem next()
      throws LdapException
    {
      SearchItem item = null;
      if (searchReferences != null) {
        if (!searchReferences.isEmpty()) {
          item = new SearchItem(new SearchReference(-1, null, searchReferences.remove(0)));
        }
        if (searchReferences.isEmpty()) {
          response = createResponse(request, null, ResultCode.SUCCESS, null, searchContext);
        }
      } else {
        final JndiUtils bu = new JndiUtils(request.getSortBehavior());
        try {
          final SearchResult result = results.next();
          logger.trace("reading search result: {}", result);
          result.setName(formatDn(result, getSearchDn(searchContext, request)));
          item = new SearchItem(bu.toSearchEntry(result));
        } catch (LdapReferralException e) {
          item = new SearchItem(new SearchReference(-1, null, readReferralUrls(e)));
        } catch (NamingException e) {
          final ResultCode ignoreRc = ignoreSearchException(config.getSearchIgnoreResultCodes(), e);
          if (ignoreRc == null) {
            processNamingException(request, e, null, searchContext);
          }
          responseResultCode = ignoreRc;
        }
      }
      return item;
    }


    /**
     * Determines whether the supplied naming exception should be ignored.
     *
     * @param  ignoreResultCodes  to match against the exception
     * @param  e  naming exception to match
     *
     * @return  result code that should be ignored or null
     */
    protected ResultCode ignoreSearchException(final ResultCode[] ignoreResultCodes, final NamingException e)
    {
      ResultCode ignore = null;
      if (ignoreResultCodes != null && ignoreResultCodes.length > 0) {
        for (ResultCode rc : ignoreResultCodes) {
          if (NamingExceptionUtils.matches(e.getClass(), rc)) {
            logger.debug("Ignoring naming exception", e);
            ignore = rc;
            break;
          }
        }
      }
      return ignore;
    }


    /**
     * Reads all referral URLs associated with this exception by invoking the search operation on the referral context
     * until all referrals have been read. JNDI does not distinguish the URLs contained in specific references. So each
     * URL must be treated as a separate search reference.
     *
     * @param  refEx  to read URLs from
     *
     * @return  referral urls
     */
    protected String[] readReferralUrls(final LdapReferralException refEx)
    {
      final List<String> urls = new ArrayList<>();
      LdapReferralException loopEx = refEx;
      urls.add((String) loopEx.getReferralInfo());
      while (loopEx.skipReferral()) {
        try {
          final LdapContext ctx = (LdapContext) loopEx.getReferralContext(
            searchContext.getEnvironment(),
            config.getControlProcessor().processRequestControls(request.getControls()));
          search(ctx, request);
        } catch (LdapReferralException e) {
          if (e.getReferralInfo() != null && e.getReferralInfo() instanceof String) {
            urls.add((String) e.getReferralInfo());
          }
          loopEx = e;
        } catch (NamingException namingEx) {
          logger.warn("Error reading search references", namingEx);
          break;
        }
      }
      logger.trace("read search references: {}", urls);
      return urls.toArray(new String[urls.size()]);
    }


    @Override
    public Response<Void> getResponse()
    {
      return response;
    }


    /**
     * Determines the DN of the supplied search request. Returns {@link LdapContext#getNameInNamespace()} if it is
     * available, otherwise returns {@link SearchRequest#getBaseDn()}.
     *
     * @param  ctx  ldap context the search was performed on
     * @param  sr  search request
     *
     * @return  DN
     *
     * @throws  NamingException  if an error occurs
     */
    protected String getSearchDn(final LdapContext ctx, final SearchRequest sr)
      throws NamingException
    {
      if (ctx != null && !"".equals(ctx.getNameInNamespace())) {
        return ctx.getNameInNamespace();
      } else {
        return sr.getBaseDn();
      }
    }


    /**
     * Returns a fully-qualified DN for the supplied search result. If search result is relative, the DN is created with
     * {@link SearchResult#getNameInNamespace()}. Otherwise the behavior is controlled by {@link
     * JndiProviderConfig#getRemoveDnUrls()}.
     *
     * @param  sr  to determine DN for
     * @param  baseDn  that search was performed on
     *
     * @return  fully qualified DN
     *
     * @throws  NamingException  if search result name cannot be formatted as a DN
     */
    protected String formatDn(final SearchResult sr, final String baseDn)
      throws NamingException
    {
      final String fqName;
      if (sr.isRelative()) {
        logger.trace("formatting relative dn '{}'", sr.getNameInNamespace());
        final LdapName lname = new LdapName(sr.getNameInNamespace());
        fqName = lname.toString();
      } else {
        logger.trace("formatting non-relative dn '{}'", sr.getName());
        if (config.getRemoveDnUrls()) {
          fqName = readCompositeName(URI.create(sr.getName()).getPath().substring(1));
        } else {
          fqName = readCompositeName(sr.getName());
        }
      }
      logger.trace("formatted dn '{}'", fqName);
      return fqName;
    }


    /**
     * Uses a composite name to parse the supplied string.
     *
     * @param  s  composite name to read
     *
     * @return  ldap name
     *
     * @throws  InvalidNameException  if the supplied string is not a valid composite name
     */
    protected String readCompositeName(final String s)
      throws InvalidNameException
    {
      final StringBuilder name = new StringBuilder();
      final CompositeName cName = new CompositeName(s);
      for (int i = 0; i < cName.size(); i++) {
        name.append(cName.get(i));
        if (i + 1 < cName.size()) {
          name.append("/");
        }
      }
      return name.toString();
    }


    @Override
    public void close()
      throws LdapException
    {
      try {
        if (results != null) {
          results.close();
        }
      } catch (NamingException e) {
        logger.error("Error closing naming enumeration", e);
      }
      try {
        if (searchContext != null) {
          searchContext.close();
        }
      } catch (NamingException e) {
        logger.error("Error closing ldap context", e);
      }
    }
  }


  /** Class for exposing extended request properties. */
  protected static class JndiExtendedRequest implements javax.naming.ldap.ExtendedRequest
  {

    /** OID of the extended request. */
    private final String oid;

    /** BER encoded request data. */
    private final byte[] encoded;


    /**
     * Creates a new jndi extended request.
     *
     * @param  id  request oid
     * @param  berValue  BER encoded request
     */
    public JndiExtendedRequest(final String id, final byte[] berValue)
    {
      oid = id;
      encoded = berValue;
    }


    @Override
    public String getID()
    {
      return oid;
    }


    @Override
    public byte[] getEncodedValue()
    {
      return encoded;
    }


    @Override
    public javax.naming.ldap.ExtendedResponse createExtendedResponse(
      final String id,
      final byte[] berValue,
      final int offset,
      final int length)
      throws NamingException
    {
      byte[] b = null;
      if (berValue != null) {
        b = new byte[length];
        System.arraycopy(berValue, offset, b, 0, length);
      }
      return new JndiExtendedResponse(id, b);
    }
  }


  /** Class for exposing extended response properties. */
  protected static class JndiExtendedResponse implements javax.naming.ldap.ExtendedResponse
  {

    /** OID of the extended response. */
    private final String oid;

    /** BER encoded response data. */
    private final byte[] encoded;


    /**
     * Creates a new jndi extended response.
     *
     * @param  id  response oid
     * @param  berValue  BER encoded response
     */
    public JndiExtendedResponse(final String id, final byte[] berValue)
    {
      oid = id;
      encoded = berValue;
    }


    @Override
    public String getID()
    {
      return oid;
    }


    @Override
    public byte[] getEncodedValue()
    {
      return encoded;
    }
  }
}
