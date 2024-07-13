/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.ldaptive.AbstractTest;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactoryManager;
import org.ldaptive.ConnectionInitializer;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchConnectionValidator;
import org.ldaptive.SearchRequest;
import org.ldaptive.TestControl;
import org.ldaptive.ad.extended.FastBindConnectionInitializer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;
import static org.ldaptive.TestUtils.*;

/**
 * Load test for {@link Authenticator}.
 *
 * @author  Middleware Services
 */
public class AuthenticatorLoadTest extends AbstractTest
{

  /** Entries for auth tests. */
  private static final Map<String, LdapEntry[]> ENTRIES = new HashMap<>();

  static {
    // Initialize the map of entries
    for (int i = 2; i <= 10; i++) {
      ENTRIES.put(String.valueOf(i), new LdapEntry[2]);
    }
  }

  /** Authenticator instance for concurrency testing. */
  private final Authenticator singleTLSAuth;

  /** Authenticator instance for concurrency testing. */
  private final Authenticator pooledTLSAuth;

  /** Authenticator instance for concurrency testing. */
  private final Authenticator singleADFastBind;

  /** Authenticator instance for concurrency testing. */
  private final Authenticator pooledADFastBind;


  /**
   * Default constructor.
   *
   * @throws  Exception  if ldap cannot be constructed
   */
  public AuthenticatorLoadTest()
    throws Exception
  {
    singleTLSAuth = readAuthenticator("classpath:/org/ldaptive/ldap.tls.load.properties");
    pooledTLSAuth = readAuthenticator("classpath:/org/ldaptive/ldap.tls.load.properties");
    singleADFastBind = readAuthenticator("classpath:/org/ldaptive/ldap.tls.load.properties");
    pooledADFastBind = readAuthenticator("classpath:/org/ldaptive/ldap.tls.load.properties");
  }


  /**
   * @param  ldifFile2  to create.
   * @param  ldifFile3  to create.
   * @param  ldifFile4  to create.
   * @param  ldifFile5  to create.
   * @param  ldifFile6  to create.
   * @param  ldifFile7  to create.
   * @param  ldifFile8  to create.
   * @param  ldifFile9  to create.
   * @param  ldifFile10  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "createEntry2",
    "createEntry3",
    "createEntry4",
    "createEntry5",
    "createEntry6",
    "createEntry7",
    "createEntry8",
    "createEntry9",
    "createEntry10"
  })
  @BeforeClass(groups = "authload")
  // CheckStyle:ParameterNumber OFF
  public void createAuthEntry(
    final String ldifFile2,
    final String ldifFile3,
    final String ldifFile4,
    final String ldifFile5,
    final String ldifFile6,
    final String ldifFile7,
    final String ldifFile8,
    final String ldifFile9,
    final String ldifFile10)
    throws Exception
  {
    // initialize the pooled authenticator
    DefaultConnectionFactory drcf = (DefaultConnectionFactory)
      ((ConnectionFactoryManager) pooledTLSAuth.getDnResolver()).getConnectionFactory();
    PooledConnectionFactory drFactory = new PooledConnectionFactory();
    drFactory.setValidatePeriodically(true);
    drFactory.setValidator(
      new SearchConnectionValidator(
        Duration.ofSeconds(5),
        Duration.ofSeconds(5),
        SearchRequest.objectScopeSearchRequest("", ReturnAttributes.NONE.value())));
    drFactory.setDefaultConnectionFactory(drcf);
    drFactory.initialize();
    SearchDnResolver dr = new SearchDnResolver(drFactory);
    dr.setBaseDn(((SearchDnResolver) pooledTLSAuth.getDnResolver()).getBaseDn());
    dr.setUserFilter(((SearchDnResolver) pooledTLSAuth.getDnResolver()).getUserFilter());
    pooledTLSAuth.setDnResolver(dr);

    DefaultConnectionFactory ahcf = (DefaultConnectionFactory)
      ((ConnectionFactoryManager) pooledTLSAuth.getAuthenticationHandler()).getConnectionFactory();
    ConnectionConfig ahcc = ConnectionConfig.copy(ahcf.getConnectionConfig());
    ahcc.setConnectionInitializers((ConnectionInitializer[]) null);
    ahcf.setConnectionConfig(ahcc);

    PooledConnectionFactory ahFactory = new PooledConnectionFactory();
    ahFactory.setValidatePeriodically(true);
    ahFactory.setValidator(
      new SearchConnectionValidator(
        Duration.ofSeconds(5),
        Duration.ofSeconds(5),
        SearchRequest.objectScopeSearchRequest("", ReturnAttributes.NONE.value())));
    ahFactory.setDefaultConnectionFactory(ahcf);
    ahFactory.initialize();
    pooledTLSAuth.setAuthenticationHandler(new SimpleBindAuthenticationHandler(ahFactory));

    // initialize the ad authenticator
    if (TestControl.isActiveDirectory()) {
      ahcf = (DefaultConnectionFactory)
        ((ConnectionFactoryManager) singleADFastBind.getAuthenticationHandler()).getConnectionFactory();
      ahcc = ConnectionConfig.copy(ahcf.getConnectionConfig());
      ahcc.setConnectionInitializers(new FastBindConnectionInitializer());
      ((SimpleBindAuthenticationHandler) singleADFastBind.getAuthenticationHandler()).setConnectionFactory(
        new DefaultConnectionFactory(ahcc));
      // initialize the pooled ad authenticator
      drcf = (DefaultConnectionFactory) ((SearchDnResolver) pooledADFastBind.getDnResolver()).getConnectionFactory();
      drFactory = new PooledConnectionFactory();
      drFactory.setValidatePeriodically(true);
      drFactory.setValidator(
        new SearchConnectionValidator(
          Duration.ofSeconds(5),
          Duration.ofSeconds(5),
          SearchRequest.objectScopeSearchRequest("", ReturnAttributes.NONE.value())));
      drFactory.setDefaultConnectionFactory(drcf);
      drFactory.initialize();
      dr = new SearchDnResolver(drFactory);
      dr.setBaseDn(((SearchDnResolver) pooledADFastBind.getDnResolver()).getBaseDn());
      dr.setUserFilter(((SearchDnResolver) pooledADFastBind.getDnResolver()).getUserFilter());
      pooledADFastBind.setDnResolver(dr);

      ahcf = (DefaultConnectionFactory)
        ((ConnectionFactoryManager) pooledADFastBind.getAuthenticationHandler()).getConnectionFactory();
      ahcc = ConnectionConfig.copy(ahcf.getConnectionConfig());
      ahcc.setConnectionInitializers(new FastBindConnectionInitializer());
      ahFactory = new PooledConnectionFactory();
      ahFactory.setValidatePeriodically(true);
      ahFactory.setValidator(
        new SearchConnectionValidator(
          Duration.ofSeconds(5),
          Duration.ofSeconds(5),
          SearchRequest.objectScopeSearchRequest("", ReturnAttributes.NONE.value())));
      ahFactory.setDefaultConnectionFactory(ahcf);
      ahFactory.initialize();
      pooledADFastBind.setAuthenticationHandler(new SimpleBindAuthenticationHandler(ahFactory));
    }

    // CheckStyle:Indentation OFF
    ENTRIES.get("2")[0] = convertLdifToEntry(readFileIntoString(ldifFile2));
    ENTRIES.get("3")[0] = convertLdifToEntry(readFileIntoString(ldifFile3));
    ENTRIES.get("4")[0] = convertLdifToEntry(readFileIntoString(ldifFile4));
    ENTRIES.get("5")[0] = convertLdifToEntry(readFileIntoString(ldifFile5));
    ENTRIES.get("6")[0] = convertLdifToEntry(readFileIntoString(ldifFile6));
    ENTRIES.get("7")[0] = convertLdifToEntry(readFileIntoString(ldifFile7));
    ENTRIES.get("8")[0] = convertLdifToEntry(readFileIntoString(ldifFile8));
    ENTRIES.get("9")[0] = convertLdifToEntry(readFileIntoString(ldifFile9));
    ENTRIES.get("10")[0] = convertLdifToEntry(readFileIntoString(ldifFile10));
    // CheckStyle:Indentation ON

    for (Map.Entry<String, LdapEntry[]> e : ENTRIES.entrySet()) {
      super.createLdapEntry(e.getValue()[0]);
    }
  }
  // CheckStyle:ParameterNumber ON


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "authload")
  public void deleteAuthEntry()
    throws Exception
  {
    super.deleteLdapEntry(ENTRIES.get("2")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("3")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("4")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("5")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("6")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("7")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("8")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("9")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("10")[0].getDn());

    pooledTLSAuth.close();
  }


  /**
   * Sample authentication data.
   *
   * @return  user authentication data
   */
  @DataProvider(name = "auth-data")
  public Object[][] createAuthData()
  {
    return
      new Object[][] {
        {
          "jadams@ldaptive.org",
          "password2",
          "cn",
          "cn=John Adams",
        },
        {
          "jadams@ldaptive.org",
          "wrongpass",
          null,
          null,
        },
        {
          "tjefferson@ldaptive.org",
          "password3",
          "givenName|sn",
          "givenName=Thomas|sn=Jefferson",
        },
        {
          "tjefferson@ldaptive.org",
          "wrongpass",
          null,
          null,
        },
        {
          "jmadison@ldaptive.org",
          "password4",
          "givenName|sn",
          "givenName=James|sn=Madison",
        },
        {
          "jmadison@ldaptive.org",
          "wrongpass",
          null,
          null,
        },
        {
          "jmonroe@ldaptive.org",
          "password5",
          "givenName|sn",
          "givenName=James|sn=Monroe",
        },
        {
          "jmonroe@ldaptive.org",
          "wrongpass",
          null,
          null,
        },
        {
          "jqadams@ldaptive.org",
          "password6",
          "cn",
          "cn=John Quincy Adams",
        },
        {
          "jqadams@ldaptive.org",
          "wrongpass",
          null,
          null,
        },
        {
          "ajackson@ldaptive.org",
          "password7",
          "givenName|sn",
          "givenName=Andrew|sn=Jackson",
        },
        {
          "ajackson@ldaptive.org",
          "wrongpass",
          null,
          null,
        },
        {
          "mvburen@ldaptive.org",
          "password8",
          "givenName|sn",
          "givenName=Martin|sn=Buren",
        },
        {
          "mvburen@ldaptive.org",
          "wrongpass",
          null,
          null,
        },
        {
          "whharrison@ldaptive.org",
          "password9",
          "givenName|sn",
          "givenName=William|sn=Harrison",
        },
        {
          "whharrison@ldaptive.org",
          "wrongpass",
          null,
          null,
        },
        {
          "jtyler@ldaptive.org",
          "password10",
          "givenName|sn",
          "givenName=John|sn=Tyler",
        },
        {
          "jtyler@ldaptive.org",
          "wrongpass",
          null,
          null,
        },
      };
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  expectedAttrs  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "authload", dataProvider = "auth-data", threadPoolSize = 10, invocationCount = 100, timeOut = 60000)
  public void authenticate(
    final String user,
    final String credential,
    final String returnAttrs,
    final String expectedAttrs)
    throws Exception
  {
    if (returnAttrs == null) {
      final AuthenticationResponse response = singleTLSAuth.authenticate(
        new AuthenticationRequest(user, new Credential(credential)));
      assertThat(response.isSuccess()).isFalse();
      return;
    }
    // test auth with return attributes
    final LdapEntry expected = convertStringToEntry(null, expectedAttrs);
    final AuthenticationResponse response = singleTLSAuth.authenticate(
      new AuthenticationRequest(user, new Credential(credential), returnAttrs.split("\\|")));
    assertThat(response.isSuccess()).isTrue();
    expected.setDn(response.getLdapEntry().getDn());
    // TODO this will need some work
    assertThat(response.getLdapEntry()).isEqualTo(expected);
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  expectedAttrs  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "authload", dataProvider = "auth-data", threadPoolSize = 10, invocationCount = 100, timeOut = 60000)
  public void authenticatePooled(
    final String user,
    final String credential,
    final String returnAttrs,
    final String expectedAttrs)
    throws Exception
  {
    if (returnAttrs == null) {
      final AuthenticationResponse response = pooledTLSAuth.authenticate(
        new AuthenticationRequest(user, new Credential(credential)));
      assertThat(response.isSuccess()).isFalse();
      return;
    }
    // test auth with return attributes
    final LdapEntry expected = convertStringToEntry(null, expectedAttrs);
    final AuthenticationResponse response = pooledTLSAuth.authenticate(
      new AuthenticationRequest(user, new Credential(credential), returnAttrs.split("\\|")));
    assertThat(response.isSuccess()).isTrue();
    expected.setDn(response.getLdapEntry().getDn());
    // TODO this will need some work
    assertThat(response.getLdapEntry()).isEqualTo(expected);
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  expectedAttrs  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "authload", dataProvider = "auth-data", threadPoolSize = 5, invocationCount = 50, timeOut = 60000)
  public void authenticateADFastBind(
    final String user,
    final String credential,
    final String returnAttrs,
    final String expectedAttrs)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
      return;
    }

    if (returnAttrs == null) {
      final AuthenticationResponse response = singleADFastBind.authenticate(
        new AuthenticationRequest(user, new Credential(credential)));
      assertThat(response.isSuccess()).isFalse();
      return;
    }
    // test auth with fast bind
    final AuthenticationResponse response = singleADFastBind.authenticate(
      new AuthenticationRequest(user, new Credential(credential)));
    assertThat(response.isSuccess()).isTrue();
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  expectedAttrs  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "authload", dataProvider = "auth-data", threadPoolSize = 5, invocationCount = 50, timeOut = 60000)
  public void authenticatePooledADFastBind(
    final String user,
    final String credential,
    final String returnAttrs,
    final String expectedAttrs)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
      return;
    }

    if (returnAttrs == null) {
      final AuthenticationResponse response = pooledADFastBind.authenticate(
        new AuthenticationRequest(user, new Credential(credential)));
      assertThat(response.isSuccess()).isFalse();
      return;
    }
    // test auth with return attributes
    final LdapEntry expected = convertStringToEntry(null, expectedAttrs);
    final AuthenticationResponse response = pooledADFastBind.authenticate(
      new AuthenticationRequest(user, new Credential(credential), returnAttrs.split("\\|")));
    assertThat(response.isSuccess()).isTrue();
  }
}
