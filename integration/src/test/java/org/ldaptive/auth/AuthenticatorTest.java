/*
  $Id: AuthenticatorTest.java 3089 2014-10-30 14:53:59Z dfisher $

  Copyright (C) 2003-2010 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3089 $
  Updated: $Date: 2014-10-30 10:53:59 -0400 (Thu, 30 Oct 2014) $
*/
package org.ldaptive.auth;

import java.util.HashMap;
import java.util.Map;
import org.ldaptive.AbstractTest;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModificationType;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactoryManager;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.ResultCode;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchResult;
import org.ldaptive.TestControl;
import org.ldaptive.TestUtils;
import org.ldaptive.auth.ext.ActiveDirectoryAccountState;
import org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordPolicyAuthenticationResponseHandler;
import org.ldaptive.control.PasswordPolicyControl;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.PooledConnectionFactoryManager;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link Authenticator}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3089 $
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
  private static LdapEntry specialCharsLdapEntry;

  /** Authenticator instance for concurrency testing. */
  private Authenticator singleTLSAuth;

  /** Authenticator instance for concurrency testing. */
  private Authenticator singleSSLAuth;

  /** Authenticator instance for concurrency testing. */
  private Authenticator singleTLSDnAuth;

  /** Authenticator instance for concurrency testing. */
  private Authenticator singleSSLDnAuth;

  /** Authenticator instance for concurrency testing. */
  private Authenticator pooledTLSAuth;

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
  @BeforeClass(groups = {"auth"})
  public void createAuthEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);

    final AuthenticationHandler ah = pooledTLSAuth.getAuthenticationHandler();
    final DefaultConnectionFactory cf =
      (DefaultConnectionFactory)
        ((ConnectionFactoryManager) ah).getConnectionFactory();
    final BlockingConnectionPool cp = new BlockingConnectionPool(cf);
    final PooledConnectionFactory pcf = new PooledConnectionFactory(cp);
    pooledTLSAuth.setAuthenticationHandler(
      new PooledBindAuthenticationHandler(pcf));
    try {
      cp.initialize();
    } catch (UnsupportedOperationException e) {
      // ignore if not supported
      AssertJUnit.assertNotNull(e);
    }
  }


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createSpecialCharsEntry2")
  @BeforeClass(groups = {"auth"})
  public void createSpecialCharsEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    specialCharsLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(specialCharsLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"auth"}, dependsOnGroups = {"authAccountState"})
  public void deleteAuthEntry()
    throws Exception
  {
    super.deleteLdapEntry(testLdapEntry.getDn());
    super.deleteLdapEntry(specialCharsLdapEntry.getDn());
    final AuthenticationHandler ah = pooledTLSAuth.getAuthenticationHandler();
    try {
      (((PooledConnectionFactoryManager)
        ah).getConnectionFactory().getConnectionPool()).close();
    } catch (IllegalStateException e) {}
  }


  /**
   * @param  createNew  whether to construct a new authenticator.
   *
   * @return  authenticator
   *
   * @throws  Exception  On authenticator construction failure.
   */
  public Authenticator createTLSAuthenticator(
    final boolean createNew)
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
  public Authenticator createTLSDnAuthenticator(
    final boolean createNew)
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
  public Authenticator createSSLAuthenticator(
    final boolean createNew)
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
  public Authenticator createSSLDnAuthenticator(
    final boolean createNew)
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
  @Test(groups = {"auth"})
  public void loadProperties(final String ldapUrl, final String baseDn)
  {
    final Authenticator auth = TestUtils.readAuthenticator(
      "classpath:/org/ldaptive/ldap.tls.properties");
    final SearchDnResolver dnResolver = (SearchDnResolver) auth.getDnResolver();
    final DefaultConnectionFactory resolverCf =
      (DefaultConnectionFactory) dnResolver.getConnectionFactory();
    AssertJUnit.assertEquals(
      ldapUrl,
      resolverCf.getConnectionConfig().getLdapUrl());
    AssertJUnit.assertEquals(
      baseDn, ((SearchDnResolver) auth.getDnResolver()).getBaseDn());
  }


  /**
   * @param  cn  to get dn for.
   * @param  user  to get dn for.
   * @param  duplicateFilter  for user lookups
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "getDnCn", "getDnUser", "getDnDuplicateFilter" })
  @Test(groups = {"auth"})
  public void resolveDn(
    final String cn,
    final String user,
    final String duplicateFilter)
    throws Exception
  {
    final Authenticator auth = createTLSAuthenticator(true);

    // test input
    AssertJUnit.assertNull(auth.resolveDn(null));
    AssertJUnit.assertNull(auth.resolveDn(""));

    final SearchDnResolver resolver = (SearchDnResolver) auth.getDnResolver();

    // test format dn
    auth.setDnResolver(
      new FormatDnResolver("cn=%s,%s", new Object[] {resolver.getBaseDn()}));
    AssertJUnit.assertEquals(testLdapEntry.getDn(), auth.resolveDn(cn));
    auth.setDnResolver(resolver);

    // test one level searching
    AssertJUnit.assertEquals(
      testLdapEntry.getDn().toLowerCase(), auth.resolveDn(user).toLowerCase());

    // test duplicate DNs
    final String filter = resolver.getUserFilter();
    resolver.setUserFilter(duplicateFilter);
    try {
      auth.resolveDn(user);
      AssertJUnit.fail("Should have thrown LdapException");
    } catch (Exception e) {
      AssertJUnit.assertEquals(LdapException.class, e.getClass());
    }

    resolver.setAllowMultipleDns(true);
    auth.resolveDn(user);
    resolver.setUserFilter(filter);
    resolver.setAllowMultipleDns(false);

    // test subtree searching
    resolver.setSubtreeSearch(true);
    final String baseDn = resolver.getBaseDn();
    resolver.setBaseDn(baseDn.substring(baseDn.indexOf(",") + 1));
    AssertJUnit.assertEquals(
      testLdapEntry.getDn().toLowerCase(), auth.resolveDn(user).toLowerCase());
  }


  /**
   * @param  user  to get dn for.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "getDnUser" })
  @Test(groups = {"auth"})
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

    final Map<String, DnResolver> resolvers = new HashMap<String, DnResolver>();
    resolvers.put("resover1", sr1);
    resolvers.put("resover2", sr2);
    final AggregateDnResolver resolver = new AggregateDnResolver(resolvers);
    auth.setDnResolver(resolver);

    // test input
    AssertJUnit.assertNull(auth.resolveDn(null));
    AssertJUnit.assertNull(auth.resolveDn(""));

    // test duplicate DNs
    resolver.setAllowMultipleDns(false);
    try {
      auth.resolveDn(user);
      AssertJUnit.fail("Should have thrown LdapException");
    } catch (Exception e) {
      AssertJUnit.assertEquals(LdapException.class, e.getClass());
    }

    resolver.setAllowMultipleDns(true);
    AssertJUnit.assertEquals(
      testLdapEntry.getDn().toLowerCase(),
      auth.resolveDn(user).toLowerCase().split(":")[1]);
  }


  /**
   * @param  dn  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  ldifFile  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateDn",
      "authenticateDnCredential",
      "authenticateDnReturnAttrs",
      "authenticateDnResults"
    }
  )
  @Test(
    groups = {"auth"},
    threadPoolSize = TEST_THREAD_POOL_SIZE,
    invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT
  )
  public void authenticateDn(
    final String dn,
    final String credential,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    // test plain auth
    final Authenticator auth = createTLSDnAuthenticator(false);
    AuthenticationResponse response = auth.authenticate(
      new AuthenticationRequest(dn, new Credential(INVALID_PASSWD)));
    AssertJUnit.assertFalse(response.getResult());

    response = auth.authenticate(
      new AuthenticationRequest(dn, new Credential(credential)));
    AssertJUnit.assertTrue(response.getResult());

    // test auth with return attributes
    final String expected = TestUtils.readFileIntoString(ldifFile);
    response = auth.authenticate(
      new AuthenticationRequest(
        dn, new Credential(credential), returnAttrs.split("\\|")));
    TestUtils.assertEquals(
      TestUtils.convertLdifToResult(expected),
      new SearchResult(response.getLdapEntry()));
  }


  /**
   * @param  dn  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  ldifFile  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateDn",
      "authenticateDnCredential",
      "authenticateDnReturnAttrs",
      "authenticateDnResults"
    }
  )
  @Test(
    groups = {"auth"},
    threadPoolSize = TEST_THREAD_POOL_SIZE,
    invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT
  )
  public void authenticateDnSsl(
    final String dn,
    final String credential,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    // test plain auth
    final Authenticator auth = createSSLDnAuthenticator(false);
    AuthenticationResponse response = auth.authenticate(
      new AuthenticationRequest(dn, new Credential(INVALID_PASSWD)));
    AssertJUnit.assertFalse(response.getResult());

    response = auth.authenticate(
      new AuthenticationRequest(dn, new Credential(credential)));
    AssertJUnit.assertTrue(response.getResult());

    // test auth with return attributes
    final String expected = TestUtils.readFileIntoString(ldifFile);
    response = auth.authenticate(
      new AuthenticationRequest(
        dn, new Credential(credential), returnAttrs.split("\\|")));
    TestUtils.assertEquals(
      TestUtils.convertLdifToResult(expected),
      new SearchResult(response.getLdapEntry()));
  }


  /**
   * @param  dn  to authenticate.
   * @param  credential  to authenticate with.
   * @param  filter  to authorize with.
   * @param  filterParameters  to authorize with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateDn",
      "authenticateDnCredential",
      "authenticateDnFilter",
      "authenticateDnFilterParameters"
    }
  )
  @Test(groups = {"auth"})
  public void authenticateDnHandler(
    final String dn,
    final String credential,
    final String filter,
    final String filterParameters)
    throws Exception
  {
    final Authenticator auth = createTLSDnAuthenticator(true);

    final TestAuthenticationResponseHandler authHandler =
      new TestAuthenticationResponseHandler();
    auth.setAuthenticationResponseHandlers(
      new AuthenticationResponseHandler[] {authHandler});

    AuthenticationResponse response = auth.authenticate(
      new AuthenticationRequest(dn, new Credential(INVALID_PASSWD)));
    AssertJUnit.assertFalse(response.getResult());
    AssertJUnit.assertTrue(!authHandler.getResults().isEmpty());
    AssertJUnit.assertFalse(authHandler.getResults().get(dn).booleanValue());

    response = auth.authenticate(
      new AuthenticationRequest(dn, new Credential(credential)));
    AssertJUnit.assertTrue(response.getResult());
    AssertJUnit.assertTrue(authHandler.getResults().get(dn).booleanValue());

    authHandler.getResults().clear();

    response = auth.authenticate(
      new AuthenticationRequest(dn, new Credential(credential)));
    AssertJUnit.assertTrue(response.getResult());
    AssertJUnit.assertTrue(authHandler.getResults().get(dn).booleanValue());
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "digestMd5User", "digestMd5Credential" })
  @Test(groups = {"auth"})
  public void authenticateDigestMd5(final String user, final String credential)
    throws Exception
  {
    // TODO ignore active directory until it's configured
    if (TestControl.isActiveDirectory()) {
      return;
    }

    final Authenticator auth = TestUtils.createDigestMD5Authenticator();

    AuthenticationResponse response = auth.authenticate(
      new AuthenticationRequest(
        user, new Credential(INVALID_PASSWD), ReturnAttributes.NONE.value()));
    AssertJUnit.assertFalse(response.getResult());

    response = auth.authenticate(
      new AuthenticationRequest(
        user, new Credential(credential), ReturnAttributes.NONE.value()));
    AssertJUnit.assertTrue(response.getResult());
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "cramMd5User", "cramMd5Credential" })
  @Test(groups = {"auth"})
  public void authenticateCramMd5(final String user, final String credential)
    throws Exception
  {
    // TODO ignore active directory until it's configured
    if (TestControl.isActiveDirectory()) {
      return;
    }

    try {
      final Authenticator auth = TestUtils.createCramMD5Authenticator();
      AuthenticationResponse response = auth.authenticate(
        new AuthenticationRequest(
          user, new Credential(INVALID_PASSWD), ReturnAttributes.NONE.value()));
      AssertJUnit.assertFalse(response.getResult());

      response = auth.authenticate(
        new AuthenticationRequest(
          user, new Credential(credential), ReturnAttributes.NONE.value()));
      AssertJUnit.assertTrue(response.getResult());
    } catch (LdapException e) {
      // ignore this test if not supported by the server
      AssertJUnit.assertEquals(
        ResultCode.AUTH_METHOD_NOT_SUPPORTED, e.getResultCode());
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported by the provider
      AssertJUnit.assertNotNull(e);
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
  @Parameters(
    {
      "authenticateUser",
      "authenticateCredential",
      "authenticateReturnAttrs",
      "authenticateResults"
    }
  )
  @Test(
    groups = {"auth"},
    threadPoolSize = TEST_THREAD_POOL_SIZE,
    invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT
  )
  public void authenticate(
    final String user,
    final String credential,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final Authenticator auth = createTLSAuthenticator(false);

    // test invalid user
    AuthenticationResponse response = auth.authenticate(
      new AuthenticationRequest("i-do-not-exist", new Credential(credential)));
    AssertJUnit.assertFalse(response.getResult());
    AssertJUnit.assertEquals(
      AuthenticationResultCode.DN_RESOLUTION_FAILURE,
      response.getAuthenticationResultCode());
    AssertJUnit.assertNull(response.getResultCode());
    AssertJUnit.assertNotNull(response.getMessage());

    // test failed auth with return attributes
    response = auth.authenticate(
      new AuthenticationRequest(
        user,
        new Credential(INVALID_PASSWD),
        returnAttrs.split("\\|")));
    AssertJUnit.assertFalse(response.getResult());
    AssertJUnit.assertEquals(
      AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE,
      response.getAuthenticationResultCode());
    AssertJUnit.assertEquals(
      ResultCode.INVALID_CREDENTIALS, response.getResultCode());

    response = auth.authenticate(
      new AuthenticationRequest(user, new Credential(credential)));
    AssertJUnit.assertTrue(response.getResult());
    AssertJUnit.assertEquals(
      AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
      response.getAuthenticationResultCode());
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());

    // test auth with return attributes
    final String expected = TestUtils.readFileIntoString(ldifFile);
    response = auth.authenticate(
      new AuthenticationRequest(
        user,
        new Credential(credential),
        returnAttrs.split("\\|")));
    AssertJUnit.assertTrue(response.getResult());
    AssertJUnit.assertEquals(
      AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
      response.getAuthenticationResultCode());
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    TestUtils.assertEquals(
      TestUtils.convertLdifToResult(expected),
      new SearchResult(response.getLdapEntry()));
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  ldifFile  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateUser",
      "authenticateCredential",
      "authenticateReturnAttrs",
      "authenticateResults"
    }
  )
  @Test(
    groups = {"auth"},
    threadPoolSize = TEST_THREAD_POOL_SIZE,
    invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT
  )
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
    AssertJUnit.assertFalse(response.getResult());

    response = auth.authenticate(
      new AuthenticationRequest(user, new Credential(credential)));
    AssertJUnit.assertTrue(response.getResult());

    // test auth with return attributes
    final String expected = TestUtils.readFileIntoString(ldifFile);
    response = auth.authenticate(
      new AuthenticationRequest(
        user,
        new Credential(credential),
        returnAttrs.split("\\|")));
    TestUtils.assertEquals(
      TestUtils.convertLdifToResult(expected),
      new SearchResult(response.getLdapEntry()));
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
  @Parameters(
    {
      "authenticateUser",
      "authenticateCredential",
      "authenticateFilter",
      "authenticateReturnAttrs",
      "authenticateResults"
    }
  )
  @Test(
    groups = {"auth"},
    threadPoolSize = TEST_THREAD_POOL_SIZE,
    invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT
  )
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
    AssertJUnit.assertFalse(response.getResult());

    response = pooledTLSAuth.authenticate(
      new AuthenticationRequest(user, new Credential(credential)));
    AssertJUnit.assertTrue(response.getResult());

    // test auth with return attributes
    final String expected = TestUtils.readFileIntoString(ldifFile);
    response = pooledTLSAuth.authenticate(
      new AuthenticationRequest(
        user,
        new Credential(credential),
        returnAttrs.split("\\|")));
    TestUtils.assertEquals(
      TestUtils.convertLdifToResult(expected),
      new SearchResult(response.getLdapEntry()));
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateUser",
      "authenticateCredential",
      "authenticateReturnAttrs"
    }
  )
  @Test(groups = {"auth"})
  public void authenticateInvalidInput(
    final String user,
    final String credential,
    final String returnAttrs)
    throws Exception
  {
    final Authenticator auth = createTLSAuthenticator(true);

    AuthenticationResponse response = auth.authenticate(
      new AuthenticationRequest(user, null, returnAttrs.split("\\|")));
    AssertJUnit.assertEquals(
      AuthenticationResultCode.INVALID_CREDENTIAL,
      response.getAuthenticationResultCode());
    AssertJUnit.assertNull(response.getResultCode());
    AssertJUnit.assertNotNull(response.getMessage());

    response = auth.authenticate(
      new AuthenticationRequest(
        user, new Credential(new byte[0]), returnAttrs.split("\\|")));
    AssertJUnit.assertEquals(
      AuthenticationResultCode.INVALID_CREDENTIAL,
      response.getAuthenticationResultCode());
    AssertJUnit.assertNull(response.getResultCode());
    AssertJUnit.assertNotNull(response.getMessage());

    response = auth.authenticate(
      new AuthenticationRequest(
        user, new Credential(""), returnAttrs.split("\\|")));
    AssertJUnit.assertEquals(
      AuthenticationResultCode.INVALID_CREDENTIAL,
      response.getAuthenticationResultCode());
    AssertJUnit.assertNull(response.getResultCode());
    AssertJUnit.assertNotNull(response.getMessage());

    response = auth.authenticate(
      new AuthenticationRequest(
        null, new Credential(credential), returnAttrs.split("\\|")));
    AssertJUnit.assertEquals(
      AuthenticationResultCode.DN_RESOLUTION_FAILURE,
      response.getAuthenticationResultCode());
    AssertJUnit.assertNull(response.getResultCode());
    AssertJUnit.assertNotNull(response.getMessage());

    response = auth.authenticate(
      new AuthenticationRequest(
        "", new Credential(credential), returnAttrs.split("\\|")));
    AssertJUnit.assertEquals(
      AuthenticationResultCode.DN_RESOLUTION_FAILURE,
      response.getAuthenticationResultCode());
    AssertJUnit.assertNull(response.getResultCode());
    AssertJUnit.assertNotNull(response.getMessage());
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateSpecialCharsUser",
      "authenticateSpecialCharsCredential"
    }
  )
  @Test(groups = {"auth"})
  public void authenticateSpecialChars(
    final String user, final String credential)
    throws Exception
  {
    // TODO ignore active directory until it's configured
    if (TestControl.isActiveDirectory()) {
      return;
    }

    final Authenticator auth = createTLSAuthenticator(true);

    // test without rewrite
    AuthenticationResponse response = auth.authenticate(
      new AuthenticationRequest(user, new Credential(INVALID_PASSWD)));
    AssertJUnit.assertFalse(response.getResult());

    response = auth.authenticate(
      new AuthenticationRequest(user, new Credential(credential)));
    AssertJUnit.assertTrue(response.getResult());

    // test with rewrite
    ((SearchDnResolver) auth.getDnResolver()).setBaseDn("dc=blah");
    ((SearchDnResolver) auth.getDnResolver()).setSubtreeSearch(true);
    response = auth.authenticate(
      new AuthenticationRequest(user, new Credential(INVALID_PASSWD)));
    AssertJUnit.assertFalse(response.getResult());

    response = auth.authenticate(
      new AuthenticationRequest(user, new Credential(credential)));
    AssertJUnit.assertTrue(response.getResult());
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  ldifFile  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateUser",
      "authenticateCredential",
      "authenticateReturnAttrs",
      "authenticateResults"
    }
  )
  @Test(
    groups = {"auth"},
    threadPoolSize = TEST_THREAD_POOL_SIZE,
    invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT
  )
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
      new AuthenticationRequest(
        user,
        new Credential(credential),
        returnAttrs.split("\\|")));
    AssertJUnit.assertTrue(response.getResult());
    AssertJUnit.assertEquals(
      AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
      response.getAuthenticationResultCode());
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    TestUtils.assertEquals(
      TestUtils.convertLdifToResult(expected),
      new SearchResult(response.getLdapEntry()));
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateUser",
      "authenticateCredential",
      "authenticateReturnAttrs",
      "authenticateResults"
    }
  )
  @Test(groups = {"auth"})
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
      new AuthenticationRequest(
        user,
        new Credential(credential),
        returnAttrs.split("\\|")));
    AssertJUnit.assertTrue(response.getResult());
    AssertJUnit.assertEquals(
      AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
      response.getAuthenticationResultCode());
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    TestUtils.assertEquals(
      TestUtils.convertLdifToResult(expected),
      new SearchResult(response.getLdapEntry()));
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateUser",
      "authenticateCredential"
    }
  )
  @AfterClass(groups = {"auth", "authAccountState"})
  public void authenticatePasswordPolicy(
    final String user, final String credential)
    throws Exception
  {
    if (TestControl.isActiveDirectory()) {
      return;
    }
    final PasswordPolicyControl ppc = new PasswordPolicyControl();
    final Connection conn = TestUtils.createSetupConnection();
    AuthenticationResponse response = null;
    PasswordPolicyControl ppcResponse = null;
    final Authenticator auth = createTLSAuthenticator(true);
    auth.setAuthenticationResponseHandlers(
      new AuthenticationResponseHandler[] {
        new PasswordPolicyAuthenticationResponseHandler(), });
    try {
      conn.open();

      final BindAuthenticationHandler ah =
        (BindAuthenticationHandler) auth.getAuthenticationHandler();
      ah.setAuthenticationControls(ppc);

      // test bind sending ppolicy control
      response = auth.authenticate(
        new AuthenticationRequest(user, new Credential(credential)));
      final LdapEntry entry = response.getLdapEntry();

      // test bind on locked account
      final ModifyOperation modify = new ModifyOperation(conn);
      modify.execute(
        new ModifyRequest(
          entry.getDn(),
          new AttributeModification[] {
            new AttributeModification(
              AttributeModificationType.ADD,
              new LdapAttribute("pwdAccountLockedTime", "000001010000Z")), }));

      response = auth.authenticate(
        new AuthenticationRequest(user, new Credential(credential)));
      AssertJUnit.assertFalse(response.getResult());
      ppcResponse = (PasswordPolicyControl) response.getControl(
        PasswordPolicyControl.OID);
      AssertJUnit.assertEquals(
        PasswordPolicyControl.Error.ACCOUNT_LOCKED, ppcResponse.getError());
      AssertJUnit.assertEquals(
        PasswordPolicyControl.Error.ACCOUNT_LOCKED.getCode(),
        response.getAccountState().getError().getCode());

      // test bind with expiration time
      modify.execute(
        new ModifyRequest(
          entry.getDn(),
          new AttributeModification[] {
            new AttributeModification(
              AttributeModificationType.REMOVE,
              new LdapAttribute("pwdAccountLockedTime")), }));

      response = auth.authenticate(
        new AuthenticationRequest(user, new Credential(credential)));
      ppcResponse = (PasswordPolicyControl) response.getControl(
        PasswordPolicyControl.OID);
      AssertJUnit.assertTrue(ppcResponse.getTimeBeforeExpiration() > 0);
      AssertJUnit.assertNotNull(
        response.getAccountState().getWarning().getExpiration());

    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported
      AssertJUnit.assertNotNull(e);
    } finally {
      conn.close();
    }
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateUser",
      "authenticateCredential"
    }
  )
  @AfterClass(groups = {"auth", "authAccountState"})
  public void authenticateActiveDirectory(
    final String user, final String credential)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
      return;
    }

    final Authenticator auth = createTLSAuthenticator(true);
    auth.setAuthenticationResponseHandlers(
      new ActiveDirectoryAuthenticationResponseHandler());
    auth.setResolveEntryOnFailure(true);

    // success, store the entry for modify operations
    // setting return attributes uses the search entry resolver
    AuthenticationResponse response = auth.authenticate(
      new AuthenticationRequest(
        user, new Credential(credential), ReturnAttributes.ALL_USER.value()));
    AssertJUnit.assertTrue(response.getResult());
    AssertJUnit.assertNull(response.getAccountState());
    LdapEntry entry = response.getLdapEntry();
    AssertJUnit.assertNotNull(entry.getAttribute("pwdLastSet"));
    AssertJUnit.assertNotNull(entry.getAttribute("userAccountControl"));

    // bad password
    // setting return attributes uses the search entry resolver
    response = auth.authenticate(
      new AuthenticationRequest(
        user,
        new Credential(INVALID_PASSWD),
        ReturnAttributes.ALL_USER.value()));
    AssertJUnit.assertFalse(response.getResult());
    AssertJUnit.assertEquals(
      ActiveDirectoryAccountState.Error.LOGON_FAILURE,
      response.getAccountState().getError());
    entry = response.getLdapEntry();
    AssertJUnit.assertNull(entry.getAttribute("pwdLastSet"));
    AssertJUnit.assertNull(entry.getAttribute("userAccountControl"));

    // bad password, no return attributes
    response = auth.authenticate(
      new AuthenticationRequest(user, new Credential(INVALID_PASSWD)));
    AssertJUnit.assertFalse(response.getResult());
    AssertJUnit.assertEquals(
      ActiveDirectoryAccountState.Error.LOGON_FAILURE,
      response.getAccountState().getError());
    entry = response.getLdapEntry();
    AssertJUnit.assertNull(entry.getAttribute("pwdLastSet"));
    AssertJUnit.assertNull(entry.getAttribute("userAccountControl"));

    // bad password, leverage an existing connection factory for entry
    // resolution on a failed bind
    BindAuthenticationHandler ah =
      (BindAuthenticationHandler) singleTLSAuth.getAuthenticationHandler();
    auth.setEntryResolver(
      new SearchEntryResolver(ah.getConnectionFactory()));
    response = auth.authenticate(
      new AuthenticationRequest(
        user,
        new Credential(INVALID_PASSWD),
        ReturnAttributes.ALL_USER.value()));
    AssertJUnit.assertFalse(response.getResult());
    AssertJUnit.assertEquals(
      ActiveDirectoryAccountState.Error.LOGON_FAILURE,
      response.getAccountState().getError());
    entry = response.getLdapEntry();
    AssertJUnit.assertNotNull(entry.getAttribute("pwdLastSet"));
    AssertJUnit.assertNotNull(entry.getAttribute("userAccountControl"));
    auth.setEntryResolver(null);

    final Connection conn = TestUtils.createSetupConnection();
    try {
      conn.open();
      final ModifyOperation modify = new ModifyOperation(conn);

      // account disabled
      final String userAccountControl =
        entry.getAttribute("userAccountControl").getStringValue();
      modify.execute(
        new ModifyRequest(
          entry.getDn(),
          new AttributeModification(
            AttributeModificationType.REPLACE,
            new LdapAttribute("userAccountControl", "514"))));

      response = auth.authenticate(
        new AuthenticationRequest(user, new Credential(credential)));
      AssertJUnit.assertFalse(response.getResult());
      AssertJUnit.assertEquals(
        ActiveDirectoryAccountState.Error.ACCOUNT_DISABLED,
        response.getAccountState().getError());

      modify.execute(
        new ModifyRequest(
          entry.getDn(),
          new AttributeModification(
            AttributeModificationType.REPLACE,
            new LdapAttribute("userAccountControl", userAccountControl))));

      // account must change password
      modify.execute(
        new ModifyRequest(
          entry.getDn(),
          new AttributeModification(
            AttributeModificationType.REPLACE,
            new LdapAttribute("pwdLastSet", "0"))));

      response = auth.authenticate(
        new AuthenticationRequest(user, new Credential(credential)));
      AssertJUnit.assertFalse(response.getResult());
      AssertJUnit.assertEquals(
        ActiveDirectoryAccountState.Error.PASSWORD_MUST_CHANGE,
        response.getAccountState().getError());
    } finally {
      conn.close();
    }
  }
}
