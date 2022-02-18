/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.net.InetSocketAddress;
import java.time.Duration;
import org.ldaptive.dn.Dn;
import org.ldaptive.ssl.AllowAnyHostnameVerifier;
import org.ldaptive.ssl.AllowAnyTrustManager;
import org.ldaptive.ssl.CustomTrustManager;
import org.ldaptive.ssl.DefaultHostnameVerifier;
import org.ldaptive.ssl.DefaultTrustManager;
import org.ldaptive.ssl.NoHostnameVerifier;
import org.ldaptive.ssl.SslConfig;
import org.ldaptive.ssl.X509CredentialConfig;
import org.testng.Assert;
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
    Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "conn")
  public void compare()
    throws Exception
  {
    final CompareOperation compare = new CompareOperation(TestUtils.createConnectionFactory());
    Assert.assertTrue(
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
    Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
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
    Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
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
    Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
    response = modifyDn.execute(
      new ModifyDnRequest(
        Dn.builder().add("cn=James Buchanan Jr.").add(new Dn(testLdapEntry.getDn()).subDN(1)).build().format(),
        "cn=James Buchanan",
        true));
    Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "conn")
  public void search()
    throws Exception
  {
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());
    final SearchResponse lr = search.execute(
      new SearchRequest(new Dn(testLdapEntry.getDn()).subDN(1).format(), "(uid=15)"));
    Assert.assertEquals(lr.getEntry().getDn().toLowerCase(), testLdapEntry.getDn().toLowerCase());
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "conn")
  public void getLdapURL()
    throws Exception
  {
    final ConnectionConfig cc = TestUtils.readConnectionConfig("classpath:/org/ldaptive/ldap.conn.properties");
    cc.setLdapUrl(cc.getLdapUrl().concat("/dc=ldaptive,dc=org?cn,sn?one?(uid=dfisher)"));
    final DefaultConnectionFactory connFactory = new DefaultConnectionFactory(cc);

    final Connection conn = connFactory.getConnection();
    try {
      conn.open();
      Assert.assertTrue(conn.getLdapURL().getUrl().endsWith("/dc=ldaptive,dc=org?cn,sn?one?(uid=dfisher)"));
    } finally {
      conn.close();
    }
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
    conn = connFactory.getConnection();
    try {
      conn.open();
    } finally {
      conn.close();
    }
    conn = connFactory.getConnection();
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
    conn = connFactory.getConnection();
    try {
      conn.open();
    } finally {
      conn.close();
    }
    conn = connFactory.getConnection();
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
    conn = connFactory.getConnection();
    try {
      conn.open();
    } finally {
      conn.close();
    }
    conn = connFactory.getConnection();
    try {
      conn.open();
    } finally {
      conn.close();
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "conn")
  public void validateConnectionDefaultSearch()
    throws Exception
  {
    final ConnectionConfig cc = TestUtils.readConnectionConfig("classpath:/org/ldaptive/ldap.conn.properties");
    cc.setConnectionValidator(SearchConnectionValidator.builder()
      .timeout(Duration.ofSeconds(3))
      .period(Duration.ofSeconds(5))
      .build());
    final DefaultConnectionFactory connFactory = new DefaultConnectionFactory(cc);

    final Connection conn = connFactory.getConnection();
    try {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Thread.sleep(Duration.ofSeconds(15).toMillis());
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
    } finally {
      conn.close();
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "conn")
  public void validateConnectionSingleSearch()
    throws Exception
  {
    final ConnectionConfig cc = TestUtils.readConnectionConfig("classpath:/org/ldaptive/ldap.conn.properties");
    cc.setConnectionValidator(SearchConnectionValidator.builder()
      .timeout(Duration.ofSeconds(3))
      .period(Duration.ofSeconds(5))
      .build());
    final SingleConnectionFactory connFactory = new SingleConnectionFactory(cc);
    connFactory.initialize();

    final Connection conn = connFactory.getConnection();
    try {
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Thread.sleep(Duration.ofSeconds(15).toMillis());
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
    } finally {
      connFactory.close();
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "conn")
  public void validateConnectionPooledSearch()
    throws Exception
  {
    final ConnectionConfig cc = TestUtils.readConnectionConfig("classpath:/org/ldaptive/ldap.conn.properties");
    cc.setConnectionValidator(SearchConnectionValidator.builder()
      .timeout(Duration.ofSeconds(3))
      .period(Duration.ofSeconds(5))
      .build());
    final PooledConnectionFactory connFactory = new PooledConnectionFactory(cc);
    connFactory.initialize();

    final Connection conn = connFactory.getConnection();
    try {
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Thread.sleep(Duration.ofSeconds(15).toMillis());
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
    } finally {
      connFactory.close();
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "conn")
  public void validateConnectionDefaultCompare()
    throws Exception
  {
    final ConnectionConfig cc = TestUtils.readConnectionConfig("classpath:/org/ldaptive/ldap.conn.properties");
    cc.setConnectionValidator(CompareConnectionValidator.builder()
      .timeout(Duration.ofSeconds(3))
      .period(Duration.ofSeconds(5))
      .build());
    final DefaultConnectionFactory connFactory = new DefaultConnectionFactory(cc);

    final Connection conn = connFactory.getConnection();
    try {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Thread.sleep(Duration.ofSeconds(15).toMillis());
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
    } finally {
      conn.close();
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "conn")
  public void validateConnectionSingleCompare()
    throws Exception
  {
    final ConnectionConfig cc = TestUtils.readConnectionConfig("classpath:/org/ldaptive/ldap.conn.properties");
    cc.setConnectionValidator(CompareConnectionValidator.builder()
      .timeout(Duration.ofSeconds(3))
      .period(Duration.ofSeconds(5))
      .build());
    final SingleConnectionFactory connFactory = new SingleConnectionFactory(cc);
    connFactory.initialize();

    final Connection conn = connFactory.getConnection();
    try {
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Thread.sleep(Duration.ofSeconds(15).toMillis());
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
    } finally {
      connFactory.close();
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "conn")
  public void validateConnectionPooledCompare()
    throws Exception
  {
    final ConnectionConfig cc = TestUtils.readConnectionConfig("classpath:/org/ldaptive/ldap.conn.properties");
    cc.setConnectionValidator(CompareConnectionValidator.builder()
      .timeout(Duration.ofSeconds(3))
      .period(Duration.ofSeconds(5))
      .build());
    final PooledConnectionFactory connFactory = new PooledConnectionFactory(cc);
    connFactory.initialize();

    final Connection conn = connFactory.getConnection();
    try {
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Thread.sleep(Duration.ofSeconds(15).toMillis());
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
    } finally {
      connFactory.close();
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
    final ConnectionConfig cc = createTLSConnectionConfig(url);
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
  @Parameters("ldapTestHost")
  @Test(groups = "conn")
  public void startTLSNullSslConfig(final String url)
    throws Exception
  {
    final ConnectionConfig cc = createTLSConnectionConfig(url);
    cc.setSslConfig(null);
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Assert.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapTestHost")
  @Test(groups = "conn")
  public void startTLSEmptySslConfig(final String url)
    throws Exception
  {
    final ConnectionConfig cc = createTLSConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Assert.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapTestHost")
  @Test(groups = "conn")
  public void startTLSNoTrustedCertsDefaultHostnameVerifier(final String url)
    throws Exception
  {
    final ConnectionConfig cc = createTLSConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setHostnameVerifier(new DefaultHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Assert.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapTestHost")
  @Test(groups = "conn")
  public void startTLSAllowAnyTrust(final String url)
    throws Exception
  {
    final ConnectionConfig cc = createTLSConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
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
  @Parameters("ldapTestHost")
  @Test(groups = "conn")
  public void startTLSAllowAnyTrustNoHostnameVerifier(final String url)
    throws Exception
  {
    final ConnectionConfig cc = createTLSConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    cc.getSslConfig().setHostnameVerifier(new NoHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Assert.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapTestHost")
  @Test(groups = "conn")
  public void startTLSAllowAnyTrustDefaultHostnameVerifier(final String url)
    throws Exception
  {
    final ConnectionConfig cc = createTLSConnectionConfig(url);
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
  @Parameters("ldapTestHost")
  @Test(groups = "conn")
  public void startTLSAllowAnyTrustAllowAnyHostnameVerifier(final String url)
    throws Exception
  {
    final ConnectionConfig cc = createTLSConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    cc.getSslConfig().setHostnameVerifier(new AllowAnyHostnameVerifier());
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
  @Parameters("ldapTestHost")
  @Test(groups = "conn")
  public void startTLSCustomTrust(final String url)
    throws Exception
  {
    final ConnectionConfig cc = createTLSConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new CustomTrustManager());
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
  @Parameters("ldapTestHost")
  @Test(groups = "conn")
  public void startTLSCustomTrustDefaultHostnameVerifier(final String url)
    throws Exception
  {
    final ConnectionConfig cc = createTLSConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new CustomTrustManager());
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
  @Parameters("ldapTestHost")
  @Test(groups = "conn")
  public void startTLSCustomTrustNoHostnameVerifier(final String url)
    throws Exception
  {
    final ConnectionConfig cc = createTLSConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new CustomTrustManager());
    cc.getSslConfig().setHostnameVerifier(new NoHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Assert.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapTestHost")
  @Test(groups = "conn")
  public void startTLSIPAddress(final String url)
    throws Exception
  {
    final InetSocketAddress address = new InetSocketAddress(new LdapURL(url).getHostname(), new LdapURL(url).getPort());
    final ConnectionConfig cc = createTLSConnectionConfig(
      "ldap://" + address.getAddress().getHostAddress() + ":" + address.getPort());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Assert.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapTestHost")
  @Test(groups = "conn")
  public void startTLSEmptySslConfigIPAddress(final String url)
    throws Exception
  {
    final InetSocketAddress address = new InetSocketAddress(new LdapURL(url).getHostname(), new LdapURL(url).getPort());
    final ConnectionConfig cc = createTLSConnectionConfig(
      "ldap://" + address.getAddress().getHostAddress() + ":" + address.getPort());
    cc.setSslConfig(new SslConfig());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Assert.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapTestHost")
  @Test(groups = "conn")
  public void startTLSDefaultTrustIPAddress(final String url)
    throws Exception
  {
    final InetSocketAddress address = new InetSocketAddress(new LdapURL(url).getHostname(), new LdapURL(url).getPort());
    final ConnectionConfig cc = createTLSConnectionConfig(
      "ldap://" + address.getAddress().getHostAddress() + ":" + address.getPort());
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new DefaultTrustManager());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Assert.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapTestHost")
  @Test(groups = "conn")
  public void startTLSAllowAnyTrustIPAddress(final String url)
    throws Exception
  {
    final InetSocketAddress address = new InetSocketAddress(new LdapURL(url).getHostname(), new LdapURL(url).getPort());
    final ConnectionConfig cc = createTLSConnectionConfig(
      "ldap://" + address.getAddress().getHostAddress() + ":" + address.getPort());
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
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
  @Parameters("ldapTestHost")
  @Test(groups = "conn")
  public void startTLSAllowAnyTrustDefaultHostnameVerifierIPAddress(final String url)
    throws Exception
  {
    final InetSocketAddress address = new InetSocketAddress(new LdapURL(url).getHostname(), new LdapURL(url).getPort());
    final ConnectionConfig cc = createTLSConnectionConfig(
      "ldap://" + address.getAddress().getHostAddress() + ":" + address.getPort());
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    cc.getSslConfig().setHostnameVerifier(new DefaultHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Assert.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapTestHost")
  @Test(groups = "conn")
  public void startTLSAllowAnyTrustAllowAnyHostnameVerifierIPAddress(final String url)
    throws Exception
  {
    final InetSocketAddress address = new InetSocketAddress(new LdapURL(url).getHostname(), new LdapURL(url).getPort());
    final ConnectionConfig cc = createTLSConnectionConfig(
      "ldap://" + address.getAddress().getHostAddress() + ":" + address.getPort());
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    cc.getSslConfig().setHostnameVerifier(new AllowAnyHostnameVerifier());
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
  @Parameters("ldapTestHost")
  @Test(groups = "conn")
  public void startTLSCustomTrustIPAddress(final String url)
    throws Exception
  {
    final InetSocketAddress address = new InetSocketAddress(new LdapURL(url).getHostname(), new LdapURL(url).getPort());
    final ConnectionConfig cc = createTLSConnectionConfig(
      "ldap://" + address.getAddress().getHostAddress() + ":" + address.getPort());
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new CustomTrustManager());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Assert.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapSslTestHost")
  @Test(groups = "conn")
  public void ldaps(final String url)
    throws Exception
  {
    final String ldapsUrl = url.replace("ldap://", "ldaps://");
    final ConnectionConfig cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(createSslConfig());
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
  @Test(groups = "conn")
  public void ldapsNullSslConfig(final String url)
    throws Exception
  {
    final String ldapsUrl = url.replace("ldap://", "ldaps://");
    final ConnectionConfig cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(null);
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Assert.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapSslTestHost")
  @Test(groups = "conn")
  public void ldapsEmptySslConfig(final String url)
    throws Exception
  {
    final String ldapsUrl = url.replace("ldap://", "ldaps://");
    final ConnectionConfig cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(new SslConfig());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Assert.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapSslTestHost")
  @Test(groups = "conn")
  public void ldapsNoTrustedCertsDefaultHostnameVerifier(final String url)
    throws Exception
  {
    final String ldapsUrl = url.replace("ldap://", "ldaps://");
    final ConnectionConfig cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setHostnameVerifier(new DefaultHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Assert.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapSslTestHost")
  @Test(groups = "conn")
  public void ldapsAllowAnyTrust(final String url)
    throws Exception
  {
    final String ldapsUrl = url.replace("ldap://", "ldaps://");
    final ConnectionConfig cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
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
  @Test(groups = "conn")
  public void ldapsAllowAnyTrustNoHostnameVerifier(final String url)
    throws Exception
  {
    final String ldapsUrl = url.replace("ldap://", "ldaps://");
    final ConnectionConfig cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    cc.getSslConfig().setHostnameVerifier(new NoHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Assert.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapSslTestHost")
  @Test(groups = "conn")
  public void ldapsAllowAnyTrustDefaultHostnameVerifier(final String url)
    throws Exception
  {
    final String ldapsUrl = url.replace("ldap://", "ldaps://");
    final ConnectionConfig cc = new ConnectionConfig(ldapsUrl);
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
  @Test(groups = "conn")
  public void ldapsAllowAnyTrustAllowAnyHostnameVerifier(final String url)
    throws Exception
  {
    final String ldapsUrl = url.replace("ldap://", "ldaps://");
    final ConnectionConfig cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    cc.getSslConfig().setHostnameVerifier(new AllowAnyHostnameVerifier());
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
  @Test(groups = "conn")
  public void ldapsCustomTrust(final String url)
    throws Exception
  {
    final String ldapsUrl = url.replace("ldap://", "ldaps://");
    final ConnectionConfig cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new CustomTrustManager());
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
  @Test(groups = "conn")
  public void ldapsCustomTrustDefaultHostnameVerifier(final String url)
    throws Exception
  {
    final String ldapsUrl = url.replace("ldap://", "ldaps://");
    final ConnectionConfig cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new CustomTrustManager());
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
  @Test(groups = "conn")
  public void ldapsCustomTrustNoHostnameVerifier(final String url)
    throws Exception
  {
    final String ldapsUrl = url.replace("ldap://", "ldaps://");
    final ConnectionConfig cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new CustomTrustManager());
    cc.getSslConfig().setHostnameVerifier(new NoHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Assert.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapSslTestHost")
  @Test(groups = "conn")
  public void ldapsIPAddress(final String url)
    throws Exception
  {
    final InetSocketAddress address = new InetSocketAddress(new LdapURL(url).getHostname(), new LdapURL(url).getPort());
    final ConnectionConfig cc = new ConnectionConfig(
      "ldaps://" + address.getAddress().getHostAddress() + ":" + address.getPort());
    cc.setSslConfig(createSslConfig());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Assert.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapSslTestHost")
  @Test(groups = "conn")
  public void ldapsEmptySslConfigIPAddress(final String url)
    throws Exception
  {
    final InetSocketAddress address = new InetSocketAddress(new LdapURL(url).getHostname(), new LdapURL(url).getPort());
    final ConnectionConfig cc = new ConnectionConfig(
      "ldaps://" + address.getAddress().getHostAddress() + ":" + address.getPort());
    cc.setSslConfig(new SslConfig());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Assert.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapSslTestHost")
  @Test(groups = "conn")
  public void ldapsDefaultTrustManagerIPAddress(final String url)
    throws Exception
  {
    final InetSocketAddress address = new InetSocketAddress(new LdapURL(url).getHostname(), new LdapURL(url).getPort());
    final ConnectionConfig cc = new ConnectionConfig(
      "ldaps://" + address.getAddress().getHostAddress() + ":" + address.getPort());
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new DefaultTrustManager());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Assert.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapSslTestHost")
  @Test(groups = "conn")
  public void ldapsAllowAnyTrustManagerIPAddress(final String url)
    throws Exception
  {
    final InetSocketAddress address = new InetSocketAddress(new LdapURL(url).getHostname(), new LdapURL(url).getPort());
    final ConnectionConfig cc = new ConnectionConfig(
      "ldaps://" + address.getAddress().getHostAddress() + ":" + address.getPort());
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
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
  @Test(groups = "conn")
  public void ldapsAllowAnyTrustManagerDefaultHostnameVerifierIPAddress(final String url)
    throws Exception
  {
    final InetSocketAddress address = new InetSocketAddress(new LdapURL(url).getHostname(), new LdapURL(url).getPort());
    final ConnectionConfig cc = new ConnectionConfig(
      "ldaps://" + address.getAddress().getHostAddress() + ":" + address.getPort());
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    cc.getSslConfig().setHostnameVerifier(new DefaultHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Assert.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapSslTestHost")
  @Test(groups = "conn")
  public void ldapsAllowAnyTrustAllowAnyHostnameVerifierIPAddress(final String url)
    throws Exception
  {
    final InetSocketAddress address = new InetSocketAddress(new LdapURL(url).getHostname(), new LdapURL(url).getPort());
    final ConnectionConfig cc = new ConnectionConfig(
      "ldaps://" + address.getAddress().getHostAddress() + ":" + address.getPort());
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    cc.getSslConfig().setHostnameVerifier(new AllowAnyHostnameVerifier());
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
  @Test(groups = "conn")
  public void ldapsCustomTrustIPAddress(final String url)
    throws Exception
  {
    final InetSocketAddress address = new InetSocketAddress(new LdapURL(url).getHostname(), new LdapURL(url).getPort());
    final ConnectionConfig cc = new ConnectionConfig(
      "ldaps://" + address.getAddress().getHostAddress() + ":" + address.getPort());
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new CustomTrustManager());
    try (Connection conn = DefaultConnectionFactory.builder().config(cc).build().getConnection()) {
      conn.open();
      conn.operation(SearchRequest.objectScopeSearchRequest("")).execute();
      Assert.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      Assert.assertNotNull(e);
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
    Assert.assertEquals(openConns, 0);
  }
}
