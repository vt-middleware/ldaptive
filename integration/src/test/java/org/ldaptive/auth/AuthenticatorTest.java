/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.HashMap;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.ldaptive.AbstractTest;
import org.ldaptive.AttributeModification;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.ConnectionFactoryManager;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.ResultCode;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.TestControl;
import org.ldaptive.TestUtils;
import org.ldaptive.auth.ext.ActiveDirectoryAccountState;
import org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordPolicyAuthenticationRequestHandler;
import org.ldaptive.auth.ext.PasswordPolicyAuthenticationResponseHandler;
import org.ldaptive.control.AuthorizationIdentityRequestControl;
import org.ldaptive.control.PasswordPolicyControl;
import org.ldaptive.control.RequestControl;
import org.ldaptive.control.SessionTrackingControl;
import org.ldaptive.extended.ExtendedOperation;
import org.ldaptive.extended.PasswordModifyRequest;
import org.ldaptive.handler.DnAttributeEntryHandler;
import org.ldaptive.velocity.TemplateSearchDnResolver;
import org.ldaptive.velocity.UserContext;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link Authenticator}.
 *
 * @author  Middleware Services
 */
public class AuthenticatorTest extends AbstractTest
{

  /** Invalid password test data. */
  public static final String INVALID_PASSWD = "not-a-password";

  /** Invalid filter test data. */
  public static final String INVALID_FILTER = "(departmentNumber=1111)";

  /** Entry created for auth tests. */
  private static LdapEntry testLdapEntry;

  /** Entry created for auth tests. */
  private static LdapEntry specialCharsLdapEntry2;

  /** Entry created for auth tests. */
  private static LdapEntry specialCharsLdapEntry3;

  /** Authenticator instance for concurrency testing. */
  private final Authenticator singleTLSAuth;

  /** Authenticator instance for concurrency testing. */
  private final Authenticator singleSSLAuth;

  /** Authenticator instance for concurrency testing. */
  private final Authenticator singleTLSDnAuth;

  /** Authenticator instance for concurrency testing. */
  private final Authenticator singleSSLDnAuth;

  /** Authenticator instance for concurrency testing. */
  private final Authenticator pooledTLSAuth;

  /**
   * Default constructor.
   *
   * @throws  Exception  if ldap cannot be constructed
   */
  public AuthenticatorTest()
    throws Exception
  {
    singleTLSAuth = TestUtils.createTLSAuthenticator();
    singleSSLAuth = TestUtils.createSSLAuthenticator();
    singleTLSDnAuth = TestUtils.createTLSDnAuthenticator();
    singleSSLDnAuth = TestUtils.createSSLDnAuthenticator();
    pooledTLSAuth = TestUtils.createTLSAuthenticator();
  }


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createEntry6")
  @BeforeClass(groups = "auth")
  public void createAuthEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);

    final AuthenticationHandler ah = pooledTLSAuth.getAuthenticationHandler();
    final DefaultConnectionFactory cf =
      (DefaultConnectionFactory) ((ConnectionFactoryManager) ah).getConnectionFactory();
    final PooledConnectionFactory pcf = new PooledConnectionFactory();
    pcf.setDefaultConnectionFactory(cf);
    pcf.initialize();
    pooledTLSAuth.setAuthenticationHandler(new SimpleBindAuthenticationHandler(pcf));
  }


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createSpecialCharsEntry2")
  @BeforeClass(groups = "auth")
  public void createSpecialCharsEntry2(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    specialCharsLdapEntry2 = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(specialCharsLdapEntry2);
  }


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createSpecialCharsEntry3")
  @BeforeClass(groups = "auth")
  public void createSpecialCharsEntry3(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    specialCharsLdapEntry3 = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(specialCharsLdapEntry3);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "auth", dependsOnGroups = "authAccountState")
  public void deleteAuthEntry()
    throws Exception
  {
    super.deleteLdapEntry(testLdapEntry.getDn());
    super.deleteLdapEntry(specialCharsLdapEntry2.getDn());
    super.deleteLdapEntry(specialCharsLdapEntry3.getDn());

    final AuthenticationHandler ah = pooledTLSAuth.getAuthenticationHandler();
    ((SimpleBindAuthenticationHandler) ah).getConnectionFactory().close();
  }


  /**
   * @param  createNew  whether to construct a new authenticator.
   *
   * @return  authenticator
   *
   * @throws  Exception  On authenticator construction failure.
   */
  public Authenticator createTLSAuthenticator(final boolean createNew)
    throws Exception
  {
    if (createNew) {
      return TestUtils.createTLSAuthenticator();
    }
    return singleTLSAuth;
  }


  /**
   * @param  createNew  whether to construct a new authenticator.
   *
   * @return  authenticator
   *
   * @throws  Exception  On authenticator construction failure.
   */
  public Authenticator createTLSDnAuthenticator(final boolean createNew)
    throws Exception
  {
    if (createNew) {
      return TestUtils.createTLSDnAuthenticator();
    }
    return singleTLSDnAuth;
  }


  /**
   * @param  createNew  whether to construct a new authenticator.
   *
   * @return  authenticator
   *
   * @throws  Exception  On authenticator construction failure.
   */
  public Authenticator createSSLAuthenticator(final boolean createNew)
    throws Exception
  {
    if (createNew) {
      return TestUtils.createSSLAuthenticator();
    }
    return singleSSLAuth;
  }


  /**
   * @param  createNew  whether to construct a new authenticator.
   *
   * @return  authenticator
   *
   * @throws  Exception  On authenticator construction failure.
   */
  public Authenticator createSSLDnAuthenticator(final boolean createNew)
    throws Exception
  {
    if (createNew) {
      return TestUtils.createSSLDnAuthenticator();
    }
    return singleSSLDnAuth;
  }


  /**
   * @param  ldapUrl  to check
   * @param  baseDn  to check
   */
  @Parameters({ "loadPropertiesUrl", "loadPropertiesBaseDn" })
  @Test(groups = "auth")
  public void loadProperties(final String ldapUrl, final String baseDn)
  {
    final Authenticator auth = TestUtils.readAuthenticator("classpath:/org/ldaptive/ldap.tls.properties");
    final SearchDnResolver dnResolver = (SearchDnResolver) auth.getDnResolver();
    final DefaultConnectionFactory resolverCf = (DefaultConnectionFactory) dnResolver.getConnectionFactory();
    Assert.assertEquals(resolverCf.getConnectionConfig().getLdapUrl(), ldapUrl);
    Assert.assertEquals(((SearchDnResolver) auth.getDnResolver()).getBaseDn(), baseDn);
  }


  /**
   * @param  cn  to get dn for.
   * @param  user  to get dn for.
   * @param  duplicateFilter  for user lookups
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "getDnCn", "getDnUser", "getDnDuplicateFilter" })
  @Test(groups = "auth")
  public void resolveDn(final String cn, final String user, final String duplicateFilter)
    throws Exception
  {
    final Authenticator auth = createTLSAuthenticator(true);

    // test input
    Assert.assertNull(auth.resolveDn(null));
    Assert.assertNull(auth.resolveDn(new User("")));

    final SearchDnResolver resolver = (SearchDnResolver) auth.getDnResolver();

    // test one level searching
    Assert.assertEquals(auth.resolveDn(new User(user)).toLowerCase(), testLdapEntry.getDn().toLowerCase());

    // test duplicate DNs
    final String filter = resolver.getUserFilter();
    resolver.setUserFilter(duplicateFilter);
    try {
      auth.resolveDn(new User(user));
      Assert.fail("Should have thrown LdapException");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), LdapException.class);
    }

    resolver.setAllowMultipleDns(true);
    auth.resolveDn(new User(user));
    resolver.setUserFilter(filter);
    resolver.setAllowMultipleDns(false);

    // test subtree searching
    resolver.setSubtreeSearch(true);

    final String baseDn = resolver.getBaseDn();
    resolver.setBaseDn(baseDn.substring(baseDn.indexOf(",") + 1));
    Assert.assertEquals(auth.resolveDn(new User(user)).toLowerCase(), testLdapEntry.getDn().toLowerCase());
  }


  /**
   * @param  cn  to get dn for.
   * @param  user  to get dn for.
   * @param  duplicateFilter  for user lookups
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "getDnCn", "getDnUser", "getDnDuplicateFilter" })
  @Test(groups = "auth")
  public void resolveDnFormat(final String cn, final String user, final String duplicateFilter)
    throws Exception
  {
    final Authenticator auth = createTLSAuthenticator(true);
    final SearchDnResolver resolver = (SearchDnResolver) auth.getDnResolver();

    auth.setDnResolver(new FormatDnResolver("cn=%s,%s", new Object[] {resolver.getBaseDn()}));
    Assert.assertEquals(auth.resolveDn(new User(cn)), testLdapEntry.getDn());
  }


  /**
   * @param  cn  to get dn for.
   * @param  user  to get dn for.
   * @param  duplicateFilter  for user lookups
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "getDnCn", "getDnUser", "getDnDuplicateFilter" })
  @Test(groups = "auth")
  public void resolveDnVelocity(final String cn, final String user, final String duplicateFilter)
    throws Exception
  {
    final Authenticator auth = createTLSAuthenticator(true);
    final SearchDnResolver resolver = (SearchDnResolver) auth.getDnResolver();

    final VelocityEngine engine = new VelocityEngine();
    engine.addProperty("string.resource.loader.class",
      "org.apache.velocity.runtime.resource.loader.StringResourceLoader");
    engine.addProperty("resource.loader", "string");
    engine.addProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
    engine.init();

    final VelocityContext context = new VelocityContext();
    context.put("context", new UserContext(user));
    final TemplateSearchDnResolver velocityResolver = new TemplateSearchDnResolver(
      resolver.getConnectionFactory(),
      engine,
      "(|(uid=$context.principal)(mail=$context.principal))");
    velocityResolver.setBaseDn(resolver.getBaseDn());
    auth.setDnResolver(velocityResolver);
    Assert.assertEquals(auth.resolveDn(new User(null, context)).toLowerCase(), testLdapEntry.getDn().toLowerCase());
  }


  /**
   * @param  user  to get dn for.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("getDnUser")
  @Test(groups = "auth")
  public void resolveDnAggregate(final String user)
    throws Exception
  {
    final Authenticator auth = createTLSAuthenticator(true);
    final SearchDnResolver sr1 = (SearchDnResolver) auth.getDnResolver();
    final SearchDnResolver sr2 = new SearchDnResolver();
    sr2.setAllowMultipleDns(sr1.getAllowMultipleDns());
    sr2.setConnectionFactory(sr1.getConnectionFactory());
    sr2.setBaseDn(sr1.getBaseDn());
    sr2.setSubtreeSearch(sr1.getSubtreeSearch());
    sr2.setUserFilter(sr1.getUserFilter());
    sr2.setUserFilterParameters(sr1.getUserFilterParameters());

    final Map<String, DnResolver> resolvers = new HashMap<>();
    resolvers.put("resolver1", sr1);
    resolvers.put("resolver2", sr2);

    final AggregateDnResolver resolver = new AggregateDnResolver(resolvers);
    auth.setDnResolver(resolver);

    // test input
    Assert.assertNull(auth.resolveDn(null));
    Assert.assertNull(auth.resolveDn(new User("")));

    // test duplicate DNs
    resolver.setAllowMultipleDns(false);
    try {
      auth.resolveDn(new User(user));
      Assert.fail("Should have thrown LdapException");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), LdapException.class);
    }

    resolver.setAllowMultipleDns(true);
    Assert.assertEquals(
      auth.resolveDn(new User(user)).toLowerCase().split(":")[1],
      testLdapEntry.getDn().toLowerCase());
  }


  /**
   * @param  user  to get dn for.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("getDnUser")
  @Test(groups = "auth")
  public void resolveDnFromAttribute(final String user)
    throws Exception
  {
    final Authenticator auth = createTLSAuthenticator(true);
    final SearchDnResolver resolver = (SearchDnResolver) auth.getDnResolver();
    resolver.setResolveFromAttribute("entryDN");
    resolver.setEntryHandlers(new DnAttributeEntryHandler());
    Assert.assertEquals(auth.resolveDn(new User(user)).toLowerCase(), testLdapEntry.getDn().toLowerCase());
  }


  /**
   * @param  dn  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  ldifFile  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "authenticateDn",
    "authenticateDnCredential",
    "authenticateDnReturnAttrs",
    "authenticateDnResults"
  })
  @Test(
    groups = "auth", threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT)
  public void authenticateDn(final String dn, final String credential, final String returnAttrs, final String ldifFile)
    throws Exception
  {
    // test plain auth
    final Authenticator auth = createTLSDnAuthenticator(false);
    AuthenticationResponse response = auth.authenticate(new AuthenticationRequest(dn, new Credential(INVALID_PASSWD)));
    Assert.assertFalse(response.isSuccess());

    response = auth.authenticate(new AuthenticationRequest(dn, new Credential(credential)));
    Assert.assertTrue(response.isSuccess());

    // test auth with return attributes
    final String expected = TestUtils.readFileIntoString(ldifFile);
    response = auth.authenticate(new AuthenticationRequest(dn, new Credential(credential), returnAttrs.split("\\|")));
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected).getEntry(), response.getLdapEntry());
  }


  /**
   * @param  dn  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  ldifFile  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "authenticateDn",
    "authenticateDnCredential",
    "authenticateDnReturnAttrs",
    "authenticateDnResults"
  })
  @Test(
    groups = "auth", threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT)
  public void authenticateDnSsl(
    final String dn,
    final String credential,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    // test plain auth
    final Authenticator auth = createSSLDnAuthenticator(false);
    AuthenticationResponse response = auth.authenticate(new AuthenticationRequest(dn, new Credential(INVALID_PASSWD)));
    Assert.assertFalse(response.isSuccess());

    response = auth.authenticate(new AuthenticationRequest(dn, new Credential(credential)));
    Assert.assertTrue(response.isSuccess());

    // test auth with return attributes
    final String expected = TestUtils.readFileIntoString(ldifFile);
    response = auth.authenticate(new AuthenticationRequest(dn, new Credential(credential), returnAttrs.split("\\|")));
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected).getEntry(), response.getLdapEntry());
  }


  /**
   * @param  dn  to authenticate.
   * @param  credential  to authenticate with.
   * @param  filter  to authorize with.
   * @param  filterParameters  to authorize with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "authenticateDn",
    "authenticateDnCredential",
    "authenticateDnFilter",
    "authenticateDnFilterParameters"
  })
  @Test(groups = "auth")
  public void authenticateDnHandler(
    final String dn,
    final String credential,
    final String filter,
    final String filterParameters)
    throws Exception
  {
    final Authenticator auth = createTLSDnAuthenticator(true);

    final TestAuthenticationResponseHandler authHandler = new TestAuthenticationResponseHandler();
    auth.setResponseHandlers(authHandler);

    AuthenticationResponse response = auth.authenticate(new AuthenticationRequest(dn, new Credential(INVALID_PASSWD)));
    Assert.assertFalse(response.isSuccess());
    Assert.assertTrue(!authHandler.getResults().isEmpty());
    Assert.assertFalse(authHandler.getResults().get(dn));

    response = auth.authenticate(new AuthenticationRequest(dn, new Credential(credential)));
    Assert.assertTrue(response.isSuccess());
    Assert.assertTrue(authHandler.getResults().get(dn));

    authHandler.getResults().clear();

    response = auth.authenticate(new AuthenticationRequest(dn, new Credential(credential)));
    Assert.assertTrue(response.isSuccess());
    Assert.assertTrue(authHandler.getResults().get(dn));
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  ldifFile  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "authenticateUser",
    "authenticateCredential",
    "authenticateReturnAttrs",
    "authenticateResults"
  })
  @Test(
    groups = "auth", threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT)
  public void authenticate(final String user, final String credential, final String returnAttrs, final String ldifFile)
    throws Exception
  {
    final Authenticator auth = createTLSAuthenticator(false);

    // test invalid user
    AuthenticationResponse response = auth.authenticate(
      new AuthenticationRequest("i-do-not-exist", new Credential(credential)));
    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals(response.getAuthenticationResultCode(), AuthenticationResultCode.DN_RESOLUTION_FAILURE);
    Assert.assertNull(response.getResultCode());
    Assert.assertNotNull(response.getDiagnosticMessage());

    // test failed auth with return attributes
    response = auth.authenticate(
      new AuthenticationRequest(user, new Credential(INVALID_PASSWD), returnAttrs.split("\\|")));
    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals(
      response.getAuthenticationResultCode(),
      AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE);
    Assert.assertEquals(response.getResultCode(), ResultCode.INVALID_CREDENTIALS);

    response = auth.authenticate(new AuthenticationRequest(user, new Credential(credential)));
    Assert.assertTrue(response.isSuccess());
    Assert.assertEquals(
      response.getAuthenticationResultCode(),
      AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS);
    Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);

    // test auth with return attributes
    final String expected = TestUtils.readFileIntoString(ldifFile);
    response = auth.authenticate(new AuthenticationRequest(user, new Credential(credential), returnAttrs.split("\\|")));
    Assert.assertTrue(response.isSuccess());
    Assert.assertEquals(
      response.getAuthenticationResultCode(),
      AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS);
    Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected).getEntry(), response.getLdapEntry());
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  ldifFile  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "authenticateUser",
    "authenticateCredential",
    "authenticateReturnAttrs",
    "authenticateResults"
  })
  @Test(
    groups = "auth", threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT)
  public void authenticateSsl(
    final String user,
    final String credential,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final Authenticator auth = createSSLAuthenticator(false);

    // test plain auth
    AuthenticationResponse response = auth.authenticate(
      new AuthenticationRequest(user, new Credential(INVALID_PASSWD)));
    Assert.assertFalse(response.isSuccess());

    response = auth.authenticate(new AuthenticationRequest(user, new Credential(credential)));
    Assert.assertTrue(response.isSuccess());

    // test auth with return attributes
    final String expected = TestUtils.readFileIntoString(ldifFile);
    response = auth.authenticate(new AuthenticationRequest(user, new Credential(credential), returnAttrs.split("\\|")));
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected).getEntry(), response.getLdapEntry());
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  filter  to authorize with.
   * @param  returnAttrs  to search for.
   * @param  ldifFile  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "authenticateUser",
    "authenticateCredential",
    "authenticateFilter",
    "authenticateReturnAttrs",
    "authenticateResults"
  })
  @Test(
    groups = "auth", threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT)
  public void authenticatePooled(
    final String user,
    final String credential,
    final String filter,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    // test plain auth
    AuthenticationResponse response = pooledTLSAuth.authenticate(
      new AuthenticationRequest(user, new Credential(INVALID_PASSWD)));
    Assert.assertFalse(response.isSuccess());

    response = pooledTLSAuth.authenticate(new AuthenticationRequest(user, new Credential(credential)));
    Assert.assertTrue(response.isSuccess());

    // test auth with return attributes
    final String expected = TestUtils.readFileIntoString(ldifFile);
    response = pooledTLSAuth.authenticate(
      new AuthenticationRequest(user, new Credential(credential), returnAttrs.split("\\|")));
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected).getEntry(), response.getLdapEntry());
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  filter  to authorize with.
   * @param  returnAttrs  to search for.
   * @param  ldifFile  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "authenticateUser",
    "authenticateCredential",
    "authenticateFilter",
    "authenticateReturnAttrs",
    "authenticateResults"
  })
  @Test(groups = "auth")
  public void authenticateVelocity(
      final String user,
      final String credential,
      final String filter,
      final String returnAttrs,
      final String ldifFile)
    throws Exception
  {
    final Authenticator auth = createTLSAuthenticator(true);
    final SearchDnResolver resolver = (SearchDnResolver) auth.getDnResolver();

    final VelocityEngine engine = new VelocityEngine();
    engine.addProperty("string.resource.loader.class",
      "org.apache.velocity.runtime.resource.loader.StringResourceLoader");
    engine.addProperty("resource.loader", "string");
    engine.addProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
    engine.init();

    final VelocityContext context = new VelocityContext();
    context.put("context", new UserContext(user));
    final TemplateSearchDnResolver velocityResolver = new TemplateSearchDnResolver(
      resolver.getConnectionFactory(),
      engine,
      "(|(uid=$context.principal)(mail=$context.principal))");
    velocityResolver.setBaseDn(resolver.getBaseDn());
    auth.setDnResolver(velocityResolver);

    // test plain auth
    AuthenticationResponse response = auth.authenticate(
      new AuthenticationRequest(new User(null, context), new Credential(INVALID_PASSWD)));
    Assert.assertFalse(response.isSuccess());

    response = auth.authenticate(new AuthenticationRequest(new User(null, context), new Credential(credential)));
    Assert.assertTrue(response.isSuccess());

    // test auth with return attributes
    final String expected = TestUtils.readFileIntoString(ldifFile);
    response = auth.authenticate(
      new AuthenticationRequest(new User(null, context), new Credential(credential), returnAttrs.split("\\|")));
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected).getEntry(), response.getLdapEntry());
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  ldifFile  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "authenticateUser",
    "authenticateCredential",
    "authenticateReturnAttrs",
    "authenticateResults"
  })
  @Test(groups = "auth")
  public void authenticateAggregate(
    final String user,
    final String credential,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final Authenticator auth = createTLSAuthenticator(true);
    final SearchDnResolver sr1 = (SearchDnResolver) auth.getDnResolver();
    final SearchDnResolver sr2 = new SearchDnResolver();
    sr2.setAllowMultipleDns(sr1.getAllowMultipleDns());
    sr2.setConnectionFactory(sr1.getConnectionFactory());
    sr2.setBaseDn(sr1.getBaseDn());
    sr2.setSubtreeSearch(sr1.getSubtreeSearch());
    sr2.setUserFilter(sr1.getUserFilter());
    sr2.setUserFilterParameters(sr1.getUserFilterParameters());

    final Map<String, DnResolver> dnResolvers = new HashMap<>();
    dnResolvers.put("system1", sr1);
    dnResolvers.put("system2", sr2);

    final AggregateDnResolver dnResolver = new AggregateDnResolver(dnResolvers);
    auth.setDnResolver(dnResolver);

    final Map<String, AuthenticationHandler> authHandlers = new HashMap<>();
    authHandlers.put("system1", auth.getAuthenticationHandler());
    authHandlers.put("system2", auth.getAuthenticationHandler());

    final AggregateAuthenticationHandler authHandler = new AggregateAuthenticationHandler();
    authHandler.setAuthenticationHandlers(authHandlers);
    auth.setAuthenticationHandler(authHandler);

    // test invalid user
    AuthenticationResponse response = auth.authenticate(
      new AuthenticationRequest("i-do-not-exist", new Credential(credential)));
    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals(response.getAuthenticationResultCode(), AuthenticationResultCode.DN_RESOLUTION_FAILURE);
    Assert.assertNull(response.getResultCode());
    Assert.assertNotNull(response.getDiagnosticMessage());

    // test multiple DNs
    try {
      auth.authenticate(new AuthenticationRequest(user, new Credential(INVALID_PASSWD)));
      Assert.fail("Should have thrown LdapException");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), LdapException.class);
    }
    dnResolver.setAllowMultipleDns(true);

    // test failed auth with return attributes
    response = auth.authenticate(
      new AuthenticationRequest(user, new Credential(INVALID_PASSWD), returnAttrs.split("\\|")));
    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals(
      response.getAuthenticationResultCode(),
      AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE);
    Assert.assertEquals(response.getResultCode(), ResultCode.INVALID_CREDENTIALS);

    response = auth.authenticate(new AuthenticationRequest(user, new Credential(credential)));
    Assert.assertTrue(response.isSuccess());
    Assert.assertEquals(
      response.getAuthenticationResultCode(),
      AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS);
    Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);

    // test auth with return attributes
    final String expected = TestUtils.readFileIntoString(ldifFile);
    response = auth.authenticate(new AuthenticationRequest(user, new Credential(credential), returnAttrs.split("\\|")));
    Assert.assertTrue(response.isSuccess());
    Assert.assertEquals(
      response.getAuthenticationResultCode(),
      AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS);
    Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected).getEntry(), response.getLdapEntry());
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "authenticateUser",
    "authenticateCredential",
    "authenticateReturnAttrs"
  })
  @Test(groups = "auth")
  public void authenticateInvalidInput(final String user, final String credential, final String returnAttrs)
    throws Exception
  {
    final Authenticator auth = createTLSAuthenticator(true);

    AuthenticationResponse response = auth.authenticate(
      new AuthenticationRequest(user, null, returnAttrs.split("\\|")));
    Assert.assertEquals(response.getAuthenticationResultCode(), AuthenticationResultCode.INVALID_CREDENTIAL);
    Assert.assertNull(response.getResultCode());
    Assert.assertNotNull(response.getDiagnosticMessage());

    response = auth.authenticate(
      new AuthenticationRequest(user, new Credential(new byte[0]), returnAttrs.split("\\|")));
    Assert.assertEquals(response.getAuthenticationResultCode(), AuthenticationResultCode.INVALID_CREDENTIAL);
    Assert.assertNull(response.getResultCode());
    Assert.assertNotNull(response.getDiagnosticMessage());

    response = auth.authenticate(new AuthenticationRequest(user, new Credential(""), returnAttrs.split("\\|")));
    Assert.assertEquals(response.getAuthenticationResultCode(), AuthenticationResultCode.INVALID_CREDENTIAL);
    Assert.assertNull(response.getResultCode());
    Assert.assertNotNull(response.getDiagnosticMessage());

    response = auth.authenticate(
      new AuthenticationRequest((String) null, new Credential(credential), returnAttrs.split("\\|")));
    Assert.assertEquals(response.getAuthenticationResultCode(), AuthenticationResultCode.DN_RESOLUTION_FAILURE);
    Assert.assertNull(response.getResultCode());
    Assert.assertNotNull(response.getDiagnosticMessage());

    response = auth.authenticate(new AuthenticationRequest("", new Credential(credential), returnAttrs.split("\\|")));
    Assert.assertEquals(response.getAuthenticationResultCode(), AuthenticationResultCode.DN_RESOLUTION_FAILURE);
    Assert.assertNull(response.getResultCode());
    Assert.assertNotNull(response.getDiagnosticMessage());
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  ldifFile  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "authenticateUser",
    "authenticateCredential",
    "authenticateReturnAttrs",
    "authenticateResults"
  })
  @Test(groups = "auth")
  public void authenticateReturnAttributes(
    final String user,
    final String credential,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final String expected = TestUtils.readFileIntoString(ldifFile);
    final Authenticator auth = createTLSAuthenticator(true);

    // no attributes
    AuthenticationResponse response = auth.authenticate(new AuthenticationRequest(user, new Credential(credential)));
    Assert.assertEquals(
      response.getAuthenticationResultCode(),
      AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS);
    Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
    Assert.assertEquals(response.getLdapEntry().getAttributes().size(), 0);

    // attributes on the request
    response = auth.authenticate(
      new AuthenticationRequest(user, new Credential(credential), returnAttrs.split("\\|")));
    Assert.assertEquals(
      response.getAuthenticationResultCode(),
      AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS);
    Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected).getEntry(), response.getLdapEntry());

    // attributes on the authenticator
    auth.setReturnAttributes(returnAttrs.split("\\|"));
    response = auth.authenticate(new AuthenticationRequest(user, new Credential(credential)));
    Assert.assertEquals(
      response.getAuthenticationResultCode(),
      AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS);
    Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected).getEntry(), response.getLdapEntry());
    auth.setReturnAttributes((String) null);

    // NONE attributes on the authenticator
    auth.setReturnAttributes(ReturnAttributes.NONE.value());
    response = auth.authenticate(new AuthenticationRequest(user, new Credential(credential), returnAttrs.split("\\|")));
    Assert.assertEquals(
      response.getAuthenticationResultCode(),
      AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS);
    Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected).getEntry(), response.getLdapEntry());
    auth.setReturnAttributes((String) null);

    // NONE attributes on the authenticator and request
    auth.setReturnAttributes(ReturnAttributes.NONE.value());
    response = auth.authenticate(new AuthenticationRequest(user, new Credential(credential)));
    Assert.assertEquals(
      response.getAuthenticationResultCode(),
      AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS);
    Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
    Assert.assertEquals(response.getLdapEntry().getAttributes().size(), 0);
    auth.setReturnAttributes((String) null);
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "authenticateSpecialCharsUser2",
    "authenticateSpecialCharsCredential2"
  })
  @Test(groups = "auth")
  public void authenticateSpecialChars2(final String user, final String credential)
    throws Exception
  {
    final Authenticator auth = createTLSAuthenticator(true);

    // test without rewrite
    AuthenticationResponse response = auth.authenticate(
      new AuthenticationRequest(user, new Credential(INVALID_PASSWD)));
    Assert.assertFalse(response.isSuccess());

    response = auth.authenticate(new AuthenticationRequest(user, new Credential(credential)));
    Assert.assertTrue(response.isSuccess());

    // test with rewrite

    // TODO ignore active directory until it's configured
    if (TestControl.isActiveDirectory()) {
      return;
    }

    ((SearchDnResolver) auth.getDnResolver()).setBaseDn("dc=blah");
    ((SearchDnResolver) auth.getDnResolver()).setSubtreeSearch(true);
    response = auth.authenticate(new AuthenticationRequest(user, new Credential(INVALID_PASSWD)));
    Assert.assertFalse(response.isSuccess());

    response = auth.authenticate(new AuthenticationRequest(user, new Credential(credential)));
    Assert.assertTrue(response.isSuccess());
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "authenticateSpecialCharsUser3",
    "authenticateSpecialCharsCredential3"
  })
  @Test(groups = "auth")
  public void authenticateSpecialChars3(final String user, final String credential)
    throws Exception
  {
    final Authenticator auth = createTLSAuthenticator(true);

    AuthenticationResponse response = auth.authenticate(
      new AuthenticationRequest(user, new Credential(INVALID_PASSWD)));
    Assert.assertFalse(response.isSuccess());

    response = auth.authenticate(new AuthenticationRequest(user, new Credential(credential)));
    Assert.assertTrue(response.isSuccess());
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  ldifFile  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "authenticateUser",
    "authenticateCredential",
    "authenticateReturnAttrs",
    "authenticateResults"
  })
  @Test(
    groups = "auth", threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT)
  public void authenticateSearchEntry(
    final String user,
    final String credential,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final Authenticator auth = createTLSAuthenticator(false);
    final SearchDnResolver dnResolver = (SearchDnResolver) auth.getDnResolver();
    final SearchEntryResolver entryResolver = new SearchEntryResolver();
    entryResolver.setUserFilter(dnResolver.getUserFilter());
    entryResolver.setBaseDn(dnResolver.getBaseDn());
    entryResolver.setSubtreeSearch(dnResolver.getSubtreeSearch());
    auth.setEntryResolver(entryResolver);

    final String expected = TestUtils.readFileIntoString(ldifFile);
    final AuthenticationResponse response = auth.authenticate(
      new AuthenticationRequest(user, new Credential(credential), returnAttrs.split("\\|")));
    Assert.assertTrue(response.isSuccess());
    Assert.assertEquals(
      response.getAuthenticationResultCode(),
      AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS);
    Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected).getEntry(), response.getLdapEntry());
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  ldifFile  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "authenticateUser",
    "authenticateCredential",
    "authenticateReturnAttrs",
    "authenticateResults"
  })
  @Test(groups = "auth")
  public void authenticateWhoAmI(
    final String user,
    final String credential,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    if (TestControl.isActiveDirectory()) {
      return;
    }

    final Authenticator auth = createTLSAuthenticator(true);
    auth.setEntryResolver(new WhoAmIEntryResolver());

    final String expected = TestUtils.readFileIntoString(ldifFile);
    final AuthenticationResponse response = auth.authenticate(
      new AuthenticationRequest(user, new Credential(credential), returnAttrs.split("\\|")));
    Assert.assertTrue(response.isSuccess());
    Assert.assertEquals(
      response.getAuthenticationResultCode(),
      AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS);
    Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected).getEntry(), response.getLdapEntry());
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  ldifFile  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "authenticateUser",
    "authenticateCredential",
    "authenticateReturnAttrs",
    "authenticateResults"
  })
  @Test(groups = "auth")
  public void authenticateAuthorizationIdentity(
    final String user,
    final String credential,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final Authenticator auth = createTLSAuthenticator(true);
    final SimpleBindAuthenticationHandler ah = (SimpleBindAuthenticationHandler) auth.getAuthenticationHandler();
    ah.setAuthenticationControls(new AuthorizationIdentityRequestControl());
    auth.setEntryResolver(new AuthorizationIdentityEntryResolver());

    final String expected = TestUtils.readFileIntoString(ldifFile);
    try {
      final AuthenticationResponse response = auth.authenticate(
        new AuthenticationRequest(user, new Credential(credential), returnAttrs.split("\\|")));
      Assert.assertTrue(response.isSuccess());
      Assert.assertEquals(
        response.getAuthenticationResultCode(),
        AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS);
      Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
      TestUtils.assertEquals(TestUtils.convertLdifToResult(expected).getEntry(), response.getLdapEntry());
    } catch (IllegalStateException e) {
      throw new UnsupportedOperationException("LDAP server does not support this control");
    }
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  ldifFile  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "authenticateUser",
    "authenticateCredential",
    "authenticateReturnAttrs",
    "authenticateResults"
  })
  @Test(groups = "auth")
  public void authenticateSessionTracking(
    final String user,
    final String credential,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final Authenticator auth = createTLSAuthenticator(true);
    auth.setRequestHandlers(
      new AddControlAuthenticationRequestHandler(
        (dn, arUser) -> new RequestControl[] {
          new SessionTrackingControl("151.101.32.133", "", SessionTrackingControl.USERNAME_ACCT_OID, ""), }));
    final String expected = TestUtils.readFileIntoString(ldifFile);
    try {
      final AuthenticationRequest request = new AuthenticationRequest(
        user,
        new Credential(credential),
        returnAttrs.split("\\|"));
      final AuthenticationResponse response = auth.authenticate(request);
      Assert.assertTrue(response.isSuccess());
      Assert.assertEquals(
        response.getAuthenticationResultCode(),
        AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS);
      Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
      TestUtils.assertEquals(TestUtils.convertLdifToResult(expected).getEntry(), response.getLdapEntry());
    } catch (IllegalStateException e) {
      throw new UnsupportedOperationException("LDAP server does not support this control");
    }
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "authenticateUser",
    "authenticateCredential"
  })
  @AfterClass(groups = {"auth", "authAccountState"})
  public void authenticatePasswordPolicy(final String user, final String credential)
    throws Exception
  {
    if (TestControl.isActiveDirectory()) {
      return;
    }

    final ConnectionFactory cf = TestUtils.createSetupConnectionFactory();
    AuthenticationResponse response;
    PasswordPolicyControl ppcResponse;
    final Authenticator auth = createTLSAuthenticator(true);
    auth.setRequestHandlers(new PasswordPolicyAuthenticationRequestHandler());
    auth.setResponseHandlers(new PasswordPolicyAuthenticationResponseHandler());

    // get the entry DN
    final String entryDn = auth.resolveDn(new User(user));

    // test bind sending ppolicy control
    response = auth.authenticate(new AuthenticationRequest(user, new Credential(credential)));
    Assert.assertTrue(response.isSuccess());
    ppcResponse = (PasswordPolicyControl) response.getControl(PasswordPolicyControl.OID);
    Assert.assertNotNull(ppcResponse);

    Assert.assertNull(ppcResponse.getError());
    Assert.assertNull(response.getAccountState());
    Assert.assertEquals(ppcResponse.getGraceAuthNsRemaining(), -1);
    Assert.assertEquals(ppcResponse.getTimeBeforeExpiration(), -1);

    final ModifyOperation modify = new ModifyOperation(cf);
    modify.execute(
      new ModifyRequest(
        entryDn,
        new AttributeModification(
          AttributeModification.Type.ADD,
          new LdapAttribute("pwdPolicySubentry", "cn=default,ou=policies,dc=vt,dc=edu"))));
    Thread.sleep(2000);

    // test bind with zero expiration time
    response = auth.authenticate(new AuthenticationRequest(user, new Credential(credential)));
    Assert.assertTrue(response.isSuccess());
    ppcResponse = (PasswordPolicyControl) response.getControl(PasswordPolicyControl.OID);
    Assert.assertEquals(ppcResponse.getTimeBeforeExpiration(), 0);
    Assert.assertEquals(ppcResponse.getGraceAuthNsRemaining(), -1);
    Assert.assertNotNull(response.getAccountState().getWarning().getExpiration());
    Assert.assertNull(response.getAccountState().getError());

    // test bind with expiration time
    final String newCredential = credential + "-new";
    final ExtendedOperation passwordModify = new ExtendedOperation(cf);
    passwordModify.execute(
      new PasswordModifyRequest(entryDn, credential, newCredential));
    Thread.sleep(2000);

    response = auth.authenticate(new AuthenticationRequest(user, new Credential(newCredential)));
    Assert.assertTrue(response.isSuccess());
    ppcResponse = (PasswordPolicyControl) response.getControl(PasswordPolicyControl.OID);
    Assert.assertTrue(ppcResponse.getTimeBeforeExpiration() > 0);
    Assert.assertEquals(ppcResponse.getGraceAuthNsRemaining(), -1);
    Assert.assertNotNull(response.getAccountState().getWarning().getExpiration());
    Assert.assertNull(response.getAccountState().getError());

    // test bind on locked account
    modify.execute(
      new ModifyRequest(
        entryDn,
        new AttributeModification(
          AttributeModification.Type.ADD,
          new LdapAttribute("pwdAccountLockedTime", "000001010000Z"))));

    response = auth.authenticate(new AuthenticationRequest(user, new Credential(newCredential)));
    Assert.assertFalse(response.isSuccess());
    ppcResponse = (PasswordPolicyControl) response.getControl(PasswordPolicyControl.OID);
    Assert.assertEquals(ppcResponse.getError(), PasswordPolicyControl.Error.ACCOUNT_LOCKED);
    Assert.assertEquals(
      response.getAccountState().getError().getCode(),
      PasswordPolicyControl.Error.ACCOUNT_LOCKED.getCode());
    Assert.assertEquals(ppcResponse.getGraceAuthNsRemaining(), -1);
    Assert.assertEquals(ppcResponse.getTimeBeforeExpiration(), -1);

    modify.execute(
      new ModifyRequest(
        entryDn,
        new AttributeModification(AttributeModification.Type.DELETE, new LdapAttribute("pwdAccountLockedTime"))));

    // test bind with grace login
    modify.execute(
      new ModifyRequest(
        entryDn,
        new AttributeModification(
          AttributeModification.Type.REPLACE,
          new LdapAttribute("pwdPolicySubentry", "cn=1s-expire,ou=policies,dc=vt,dc=edu"))));
    Thread.sleep(2000);

    // note that OpenLDAP never sends back 0 grace logins
    int graceLogins = 2;
    response = auth.authenticate(new AuthenticationRequest(user, new Credential(newCredential)));
    do {
      ppcResponse = (PasswordPolicyControl) response.getControl(PasswordPolicyControl.OID);
      Assert.assertTrue(response.isSuccess());
      Assert.assertEquals(ppcResponse.getTimeBeforeExpiration(), -1);
      Assert.assertEquals(ppcResponse.getGraceAuthNsRemaining(), graceLogins);
      Assert.assertEquals(response.getAccountState().getWarning().getLoginsRemaining(), graceLogins);
      Assert.assertNull(response.getAccountState().getWarning().getExpiration());
      Assert.assertNull(response.getAccountState().getError());
      graceLogins--;
      Thread.sleep(2000);
      response = auth.authenticate(new AuthenticationRequest(user, new Credential(newCredential)));
    } while (response.isSuccess());

    // password expired
    ppcResponse = (PasswordPolicyControl) response.getControl(PasswordPolicyControl.OID);
    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals(ppcResponse.getError(), PasswordPolicyControl.Error.PASSWORD_EXPIRED);
    Assert.assertEquals(
      response.getAccountState().getError().getCode(),
      PasswordPolicyControl.Error.PASSWORD_EXPIRED.getCode());
    Assert.assertEquals(ppcResponse.getGraceAuthNsRemaining(), -1);
    Assert.assertEquals(ppcResponse.getTimeBeforeExpiration(), -1);

    modify.execute(
      new ModifyRequest(
        entryDn,
        new AttributeModification(
          AttributeModification.Type.DELETE, new LdapAttribute("pwdPolicySubentry"))));
    modify.execute(
      new ModifyRequest(
        entryDn,
        new AttributeModification(
          AttributeModification.Type.REPLACE, new LdapAttribute("userPassword", credential))));
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "authenticateUser",
    "authenticateCredential"
  })
  @AfterClass(groups = {"auth", "authAccountState"})
  public void authenticateActiveDirectory(final String user, final String credential)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
      return;
    }

    final Authenticator auth = createTLSAuthenticator(true);
    auth.setResponseHandlers(new ActiveDirectoryAuthenticationResponseHandler());
    auth.setResolveEntryOnFailure(true);

    // success, store the entry for modify operations
    // setting return attributes uses the search entry resolver
    AuthenticationResponse response = auth.authenticate(
      new AuthenticationRequest(user, new Credential(credential), ReturnAttributes.ALL_USER.value()));
    Assert.assertTrue(response.isSuccess());
    Assert.assertNull(response.getAccountState());

    LdapEntry entry = response.getLdapEntry();
    Assert.assertNotNull(entry.getAttribute("pwdLastSet"));
    Assert.assertNotNull(entry.getAttribute("userAccountControl"));

    // bad password
    // setting return attributes uses the search entry resolver
    response = auth.authenticate(
      new AuthenticationRequest(user, new Credential(INVALID_PASSWD), ReturnAttributes.ALL_USER.value()));
    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals(response.getAccountState().getError(), ActiveDirectoryAccountState.Error.LOGON_FAILURE);
    entry = response.getLdapEntry();
    Assert.assertNull(entry.getAttribute("pwdLastSet"));
    Assert.assertNull(entry.getAttribute("userAccountControl"));

    // bad password, no return attributes
    response = auth.authenticate(new AuthenticationRequest(user, new Credential(INVALID_PASSWD)));
    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals(response.getAccountState().getError(), ActiveDirectoryAccountState.Error.LOGON_FAILURE);
    entry = response.getLdapEntry();
    Assert.assertNull(entry.getAttribute("pwdLastSet"));
    Assert.assertNull(entry.getAttribute("userAccountControl"));

    // bad password, leverage an existing connection factory for entry
    // resolution on a failed bind
    final SimpleBindAuthenticationHandler ah =
      (SimpleBindAuthenticationHandler) singleTLSAuth.getAuthenticationHandler();
    auth.setEntryResolver(new SearchEntryResolver(ah.getConnectionFactory()));
    response = auth.authenticate(
      new AuthenticationRequest(user, new Credential(INVALID_PASSWD), ReturnAttributes.ALL_USER.value()));
    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals(response.getAccountState().getError(), ActiveDirectoryAccountState.Error.LOGON_FAILURE);
    entry = response.getLdapEntry();
    Assert.assertNotNull(entry.getAttribute("pwdLastSet"));
    Assert.assertNotNull(entry.getAttribute("userAccountControl"));
    auth.setEntryResolver(null);

    final ModifyOperation modify = new ModifyOperation(TestUtils.createSetupConnectionFactory());

    // account disabled
    final String userAccountControl = entry.getAttribute("userAccountControl").getStringValue();
    modify.execute(
      new ModifyRequest(
        entry.getDn(),
        new AttributeModification(
          AttributeModification.Type.REPLACE,
          new LdapAttribute("userAccountControl", "514"))));

    response = auth.authenticate(new AuthenticationRequest(user, new Credential(credential)));
    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals(response.getAccountState().getError(), ActiveDirectoryAccountState.Error.ACCOUNT_DISABLED);

    modify.execute(
      new ModifyRequest(
        entry.getDn(),
        new AttributeModification(
          AttributeModification.Type.REPLACE,
          new LdapAttribute("userAccountControl", userAccountControl))));

    // account must change password
    modify.execute(
      new ModifyRequest(
        entry.getDn(),
        new AttributeModification(AttributeModification.Type.REPLACE, new LdapAttribute("pwdLastSet", "0"))));

    response = auth.authenticate(new AuthenticationRequest(user, new Credential(credential)));
    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals(response.getAccountState().getError(), ActiveDirectoryAccountState.Error.PASSWORD_MUST_CHANGE);
  }
}
