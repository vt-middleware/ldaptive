/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.ssl.AllowAnyTrustManager;
import org.ldaptive.ssl.DefaultHostnameVerifier;
import org.ldaptive.ssl.NoHostnameVerifier;
import org.ldaptive.ssl.SslConfig;
import org.ldaptive.ssl.X509CredentialConfig;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link Connection}.
 *
 * @author  Middleware Services
 */
public class ConnectionTest
{

  /** Entry created for ldap tests. */
  private static LdapEntry testLdapEntry;


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createEntry15")
  @BeforeClass(groups = "conn")
  public void add(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();

    final AddOperation add = new AddOperation(TestUtils.createConnectionFactory());
    final AddResponse response = add.execute(new AddRequest(testLdapEntry.getDn(), testLdapEntry.getAttributes()));
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "conn")
  public void compare()
    throws Exception
  {
    final CompareOperation compare = new CompareOperation(TestUtils.createConnectionFactory());
    AssertJUnit.assertTrue(
      compare.execute(
        new CompareRequest(
          testLdapEntry.getDn(), "mail", testLdapEntry.getAttribute("mail").getStringValue())).isTrue());
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "conn")
  public void delete()
    throws Exception
  {
    final DeleteOperation delete = new DeleteOperation(TestUtils.createConnectionFactory());
    final DeleteResponse response = delete.execute(new DeleteRequest(testLdapEntry.getDn()));
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "conn")
  public void modify()
    throws Exception
  {
    final ModifyOperation modify = new ModifyOperation(TestUtils.createConnectionFactory());
    final ModifyResponse response = modify.execute(
      new ModifyRequest(
        testLdapEntry.getDn(),
        new AttributeModification(AttributeModification.Type.ADD, new LdapAttribute("title", "President"))));
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "conn")
  public void modifyDn()
    throws Exception
  {
    final ModifyDnOperation modifyDn = new ModifyDnOperation(TestUtils.createConnectionFactory());
    ModifyDnResponse response = modifyDn.execute(
      new ModifyDnRequest(
        testLdapEntry.getDn(),
        "cn=James Buchanan Jr.",
        true));
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    response = modifyDn.execute(
      new ModifyDnRequest(
        "cn=James Buchanan Jr.," + DnParser.substring(testLdapEntry.getDn(), 1),
        "cn=James Buchanan",
        true));
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "conn")
  public void search()
    throws Exception
  {
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());
    final SearchResponse lr = search.execute(
      new SearchRequest(DnParser.substring(testLdapEntry.getDn(), 1), new SearchFilter("(uid=15)")));
    AssertJUnit.assertEquals(testLdapEntry.getDn().toLowerCase(), lr.getEntry().getDn().toLowerCase());
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "conn")
  public void strategyConnect()
    throws Exception
  {
    ConnectionConfig cc = TestUtils.readConnectionConfig("classpath:/org/ldaptive/ldap.conn.properties");
    cc.setConnectionStrategy(new RoundRobinConnectionStrategy());
    DefaultConnectionFactory connFactory = new DefaultConnectionFactory(cc);

    Connection conn = connFactory.getConnection();

    try {
      conn.open();
    } finally {
      conn.close();
    }
    try {
      conn.open();
    } finally {
      conn.close();
    }
    try {
      conn.open();
    } finally {
      conn.close();
    }

    cc = TestUtils.readConnectionConfig("classpath:/org/ldaptive/ldap.conn.properties");
    cc.setConnectionStrategy(new ActivePassiveConnectionStrategy());
    connFactory = new DefaultConnectionFactory(cc);
    conn = connFactory.getConnection();
    try {
      conn.open();
    } finally {
      conn.close();
    }
    try {
      conn.open();
    } finally {
      conn.close();
    }
    try {
      conn.open();
    } finally {
      conn.close();
    }

    cc = TestUtils.readConnectionConfig("classpath:/org/ldaptive/ldap.conn.properties");
    cc.setConnectionStrategy(new RandomConnectionStrategy());
    connFactory = new DefaultConnectionFactory(cc);
    conn = connFactory.getConnection();
    try {
      conn.open();
    } finally {
      conn.close();
    }
    try {
      conn.open();
    } finally {
      conn.close();
    }
    try {
      conn.open();
    } finally {
      conn.close();
    }
  }


  /**
   * @return  ssl config
   *
   * @throws  Exception  On configuration error.
   */
  public SslConfig createSslConfig()
    throws Exception
  {
    final X509CredentialConfig config = new X509CredentialConfig();
    config.setTrustCertificates("file:target/test-classes/ldaptive.trust.crt");
    return new SslConfig(config);
  }


  /**
   * @param  url  to connect to
   *
   * @return  connection configuration
   *
   * @throws  Exception  On connection failure.
   */
  public ConnectionConfig createTLSConnectionConfig(final String url)
    throws Exception
  {
    final ConnectionConfig cc = new ConnectionConfig(url);
    cc.setUseStartTLS(true);
    cc.setSslConfig(createSslConfig());
    return cc;
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapTestHost")
  @Test(groups = "conn")
  public void startTLS(final String url)
    throws Exception
  {
    // no trusted certificates
    ConnectionConfig cc = createTLSConnectionConfig(url);
    cc.setSslConfig(null);
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    // no trusted certificates
    cc = createTLSConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    // no trusted certificates with hostname verification
    cc = createTLSConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setHostnameVerifier(new DefaultHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    // trust any
    cc = createTLSConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
    }

    cc = createTLSConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    cc.getSslConfig().setHostnameVerifier(new NoHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    // trust any with hostname verification
    cc = createTLSConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    cc.getSslConfig().setHostnameVerifier(new DefaultHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapSslTestHost")
  @Test(groups = "ssl")
  public void ldaps(final String url)
    throws Exception
  {
    final String ldapsUrl = url.replace("ldap://", "ldaps://");
    // no trusted certificates
    ConnectionConfig cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(null);
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    // no trusted certificates
    cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(new SslConfig());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    // no trusted certificates with hostname verification
    cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setHostnameVerifier(new DefaultHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    // trust any
    cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
    }

    // trust any with hostname verification failure
    cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    cc.getSslConfig().setHostnameVerifier(new NoHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    // trust any with hostname verification
    cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    cc.getSslConfig().setHostnameVerifier(new DefaultHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
    }
  }


  /**
   * Sleeps at the end of all tests and checks open connections.
   *
   * @param  host  to check for connections with.
   * @param  sleepTime  time to sleep for.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "ldapTestHost", "sleepTime" })
  @AfterSuite(groups = "conn")
  public void sleep(final String host, final int sleepTime)
    throws Exception
  {
    System.out.println("--BEGIN CONNECTION COUNT TEST--");
    Thread.sleep(sleepTime);

    final LdapURL ldapUrl = new LdapURL(host);
    final String hostPrefix = ldapUrl.getHostname().contains(".") ?
      ldapUrl.getHostname().substring(0, ldapUrl.getHostname().indexOf(".")) :
      ldapUrl.getHostname();
    final int openConns = TestUtils.countOpenConnections(hostPrefix);
    AssertJUnit.assertEquals(0, openConns);
  }
}
