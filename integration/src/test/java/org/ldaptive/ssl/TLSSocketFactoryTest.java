/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.net.Socket;
import java.util.Arrays;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapURL;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.provider.Provider;
import org.testng.AssertJUnit;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link TLSSocketFactory}.
 *
 * @author  Middleware Services
 */
public class TLSSocketFactoryTest
{

  /** List of ciphers. */
  public static final String[] CIPHERS = new String[] {
    "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
    "TLS_RSA_WITH_AES_128_CBC_SHA",
    "TLS_RSA_WITH_AES_256_CBC_SHA",
    "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
    "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
    "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
    /* GCM ciphers only supported in Java 8
    "TLS_RSA_WITH_AES_128_GCM_SHA256",
    "TLS_RSA_WITH_AES_256_GCM_SHA384",
    */
    "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
    /* GCM ciphers only supported in Java 8
    "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
    */
    "TLS_RSA_WITH_AES_128_CBC_SHA256",
    "TLS_RSA_WITH_AES_256_CBC_SHA256",
    "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
    /* GCM ciphers only supported in Java 8
    "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
    */
  };

  /** List of ciphers. */
  public static final String[] UNKNOWN_CIPHERS = new String[] {
    /* one valid cipher, three invalid */
    "TLS_DH_anon_WITH_AES_128_CBC_SHA",
    "TLS_DH_anon_WITH_3DES_256_CBC_SHA",
    "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA",
    "SSL_DH_anon_WITH_RC4_128_MD5",
  };

  /** List of protocols. */
  public static final String[] ALL_PROTOCOLS = new String[] {
    "SSLv2Hello",
    "SSLv3",
    "TLSv1",
    "TLSv1.1",
    "TLSv1.2",
  };

  /** List of protocols. */
  public static final String[] PROTOCOLS = new String[] {"TLSv1", "TLSv1.1", "TLSv1.2"};

  /** List of protocols. */
  public static final String[] FAIL_PROTOCOLS = new String[] {"SSLv2Hello", };

  /** List of protocols. */
  public static final String[] UNKNOWN_PROTOCOLS = new String[] {
    /* one invalid protocol, two valid */
    "SSLv3Hello",
    "SSLv2Hello",
    "TLSv1",
  };


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
   * @return  connection configuration
   *
   * @throws  Exception  On connection failure.
   */
  public ConnectionConfig createSSLConnectionConfig(final String url)
    throws Exception
  {
    final ConnectionConfig cc = new ConnectionConfig(url);
    cc.setUseSSL(true);
    cc.setSslConfig(createSslConfig());
    return cc;
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapTestHost")
  @Test(groups = {"ssl"})
  public void connectTLS(final String url)
    throws Exception
  {
    final Provider<?> p = DefaultConnectionFactory.getDefaultProvider();

    // no trusted certificates
    ConnectionConfig cc = createTLSConnectionConfig(url);
    cc.setSslConfig(null);
    try (Connection conn = DefaultConnectionFactory.getConnection(cc)) {
      conn.open();
      final SearchOperation op = new SearchOperation(conn);
      op.execute(SearchRequest.newObjectScopeSearchRequest(""));
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    // no trusted certificates
    cc = createTLSConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    try (Connection conn = DefaultConnectionFactory.getConnection(cc)) {
      conn.open();
      final SearchOperation op = new SearchOperation(conn);
      op.execute(SearchRequest.newObjectScopeSearchRequest(""));
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    // no trusted certificates with hostname verification
    cc = createTLSConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setHostnameVerifier(new DefaultHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.getConnection(cc)) {
      conn.open();
      final SearchOperation op = new SearchOperation(conn);
      op.execute(SearchRequest.newObjectScopeSearchRequest(""));
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    // trust any
    cc = createTLSConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    try (Connection conn = DefaultConnectionFactory.getConnection(cc)) {
      conn.open();
      final SearchOperation op = new SearchOperation(conn);
      op.execute(SearchRequest.newObjectScopeSearchRequest(""));
    }

    // trust any with hostname verification failure
    if ("org.ldaptive.provider.jndi.JndiProvider".equals(p.getClass().getName())) {
      // JNDI startTLS always executes default hostname verification
      AssertJUnit.assertTrue(true);
    } else {
      cc = createTLSConnectionConfig(url);
      cc.setSslConfig(new SslConfig());
      cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
      cc.getSslConfig().setHostnameVerifier(new NoHostnameVerifier());
      try (Connection conn = DefaultConnectionFactory.getConnection(cc)) {
        conn.open();
        final SearchOperation op = new SearchOperation(conn);
        op.execute(SearchRequest.newObjectScopeSearchRequest(""));
        AssertJUnit.fail("Should have thrown Exception, no exception thrown");
      } catch (Exception e) {
        AssertJUnit.assertNotNull(e);
      }
    }

    // trust any with hostname verification
    cc = createTLSConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    // note that this verifier does not run with JNDI startTLS
    cc.getSslConfig().setHostnameVerifier(new DefaultHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.getConnection(cc)) {
      conn.open();
      final SearchOperation op = new SearchOperation(conn);
      op.execute(SearchRequest.newObjectScopeSearchRequest(""));
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapSslTestHost")
  @Test(groups = {"ssl"})
  public void connectSSL(final String url)
    throws Exception
  {
    // no trusted certificates
    ConnectionConfig cc = createSSLConnectionConfig(url);
    cc.setSslConfig(null);
    try (Connection conn = DefaultConnectionFactory.getConnection(cc)) {
      conn.open();
      final SearchOperation op = new SearchOperation(conn);
      op.execute(SearchRequest.newObjectScopeSearchRequest(""));
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    // no trusted certificates
    cc = createSSLConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    try (Connection conn = DefaultConnectionFactory.getConnection(cc)) {
      conn.open();
      final SearchOperation op = new SearchOperation(conn);
      op.execute(SearchRequest.newObjectScopeSearchRequest(""));
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    // no trusted certificates with hostname verification
    cc = createSSLConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setHostnameVerifier(new DefaultHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.getConnection(cc)) {
      conn.open();
      final SearchOperation op = new SearchOperation(conn);
      op.execute(SearchRequest.newObjectScopeSearchRequest(""));
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    // trust any
    cc = createSSLConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    try (Connection conn = DefaultConnectionFactory.getConnection(cc)) {
      conn.open();
      final SearchOperation op = new SearchOperation(conn);
      op.execute(SearchRequest.newObjectScopeSearchRequest(""));
    }

    // trust any with hostname verification failure
    cc = createSSLConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    cc.getSslConfig().setHostnameVerifier(new NoHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.getConnection(cc)) {
      conn.open();
      final SearchOperation op = new SearchOperation(conn);
      op.execute(SearchRequest.newObjectScopeSearchRequest(""));
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    // trust any with hostname verification
    cc = createSSLConnectionConfig(url);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    cc.getSslConfig().setHostnameVerifier(new DefaultHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.getConnection(cc)) {
      conn.open();
      final SearchOperation op = new SearchOperation(conn);
      op.execute(SearchRequest.newObjectScopeSearchRequest(""));
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapSslTestHost")
  @Test(groups = {"ssl"})
  public void connectLDAPS(final String url)
    throws Exception
  {
    final String ldapsUrl = url.replace("ldap://", "ldaps://");
    // no trusted certificates
    ConnectionConfig cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(null);
    try (Connection conn = DefaultConnectionFactory.getConnection(cc)) {
      conn.open();
      final SearchOperation op = new SearchOperation(conn);
      op.execute(SearchRequest.newObjectScopeSearchRequest(""));
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    // no trusted certificates
    cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(new SslConfig());
    try (Connection conn = DefaultConnectionFactory.getConnection(cc)) {
      conn.open();
      final SearchOperation op = new SearchOperation(conn);
      op.execute(SearchRequest.newObjectScopeSearchRequest(""));
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    // no trusted certificates with hostname verification
    cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setHostnameVerifier(new DefaultHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.getConnection(cc)) {
      conn.open();
      final SearchOperation op = new SearchOperation(conn);
      op.execute(SearchRequest.newObjectScopeSearchRequest(""));
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    // trust any
    cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    try (Connection conn = DefaultConnectionFactory.getConnection(cc)) {
      conn.open();
      final SearchOperation op = new SearchOperation(conn);
      op.execute(SearchRequest.newObjectScopeSearchRequest(""));
    }

    // trust any with hostname verification failure
    cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    cc.getSslConfig().setHostnameVerifier(new NoHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.getConnection(cc)) {
      conn.open();
      final SearchOperation op = new SearchOperation(conn);
      op.execute(SearchRequest.newObjectScopeSearchRequest(""));
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    // trust any with hostname verification
    cc = new ConnectionConfig(ldapsUrl);
    cc.setSslConfig(new SslConfig());
    cc.getSslConfig().setTrustManagers(new AllowAnyTrustManager());
    cc.getSslConfig().setHostnameVerifier(new DefaultHostnameVerifier());
    try (Connection conn = DefaultConnectionFactory.getConnection(cc)) {
      conn.open();
      final SearchOperation op = new SearchOperation(conn);
      op.execute(SearchRequest.newObjectScopeSearchRequest(""));
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapTestHost")
  @Test(groups = {"ssl"})
  public void setEnabledCipherSuites(final String url)
    throws Exception
  {
    final ConnectionConfig cc = createTLSConnectionConfig(url);
    final TLSSocketFactory sf = new TLSSocketFactory();
    sf.setSslConfig(cc.getSslConfig());
    sf.initialize();

    AssertJUnit.assertEquals(
      Arrays.asList(((SSLSocket) sf.createSocket()).getEnabledCipherSuites()),
      Arrays.asList(sf.getDefaultCipherSuites()));
    AssertJUnit.assertNotSame(Arrays.asList(sf.getDefaultCipherSuites()), Arrays.asList(CIPHERS));

    cc.getSslConfig().setEnabledCipherSuites(UNKNOWN_CIPHERS);

    final Connection conn = DefaultConnectionFactory.getConnection(cc);
    try {
      conn.open();
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    sf.getSslConfig().setEnabledCipherSuites(CIPHERS);
    sf.initialize();
    AssertJUnit.assertEquals(
      Arrays.asList(((SSLSocket) sf.createSocket()).getEnabledCipherSuites()),
      Arrays.asList(CIPHERS));
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapTestHost")
  @Test(groups = {"ssl"})
  public void setEnabledProtocols(final String url)
    throws Exception
  {
    final Provider<?> p = DefaultConnectionFactory.getDefaultProvider();

    final ConnectionConfig cc = createTLSConnectionConfig(url);
    final TLSSocketFactory sf = new TLSSocketFactory();
    sf.setSslConfig(cc.getSslConfig());
    sf.initialize();

    AssertJUnit.assertNotSame(
      Arrays.asList(((SSLSocket) sf.createSocket()).getEnabledProtocols()),
      Arrays.asList(PROTOCOLS));

    cc.getSslConfig().setEnabledProtocols(FAIL_PROTOCOLS);

    Connection conn = DefaultConnectionFactory.getConnection(cc);
    try {
      conn.open();
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    }

    if ("org.ldaptive.provider.opendj.OpenDJProvider".equals(p.getClass().getName())) {
      // grizzly ignores unknown protocols
      AssertJUnit.assertTrue(true);
    } else {
      cc.getSslConfig().setEnabledProtocols(UNKNOWN_PROTOCOLS);
      conn = DefaultConnectionFactory.getConnection(cc);
      try {
        conn.open();
        AssertJUnit.fail("Should have thrown Exception, no exception thrown");
      } catch (Exception e) {
        AssertJUnit.assertNotNull(e);
      }
    }

    sf.getSslConfig().setEnabledProtocols(PROTOCOLS);
    sf.initialize();
    AssertJUnit.assertEquals(
      Arrays.asList(((SSLSocket) sf.createSocket()).getEnabledProtocols()),
      Arrays.asList(PROTOCOLS));
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapSslTestHost")
  @Test(groups = {"ssl"})
  public void setSocketConfig(final String url)
    throws Exception
  {
    final SocketConfig sc = new SocketConfig();
    sc.setKeepAlive(true);
    sc.setReuseAddress(true);
    sc.setTcpNoDelay(true);
    sc.setReceiveBufferSize(256);
    sc.setSendBufferSize(256);
    sc.setSoLinger(100);
    sc.setSoTimeout(500);
    sc.setTrafficClass(0x10);

    final LdapURL ldapUrl = new LdapURL(url);
    final TLSSocketFactory sf = new TLSSocketFactory();
    sf.setSocketConfig(sc);
    sf.initialize();

    try (Socket s = sf.createSocket(ldapUrl.getEntry().getHostname(), ldapUrl.getEntry().getPort())) {
      AssertJUnit.assertNotNull(s);
    }
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"ssl"})
  public void createSSLContextInitializer()
    throws Exception
  {
    // no ssl config
    TLSSocketFactory factory = new TLSSocketFactory();
    SSLContextInitializer init = factory.createSSLContextInitializer();
    AssertJUnit.assertEquals(1, init.getTrustManagers().length);
    AssertJUnit.assertTrue(init.getTrustManagers()[0] instanceof AggregateTrustManager);
    TrustManager[] tm = ((AggregateTrustManager) init.getTrustManagers()[0]).getTrustManagers();
    AssertJUnit.assertEquals(1, tm.length);
    AssertJUnit.assertEquals("sun.security.ssl.X509TrustManagerImpl", tm[0].getClass().getName());

    // empty ssl config
    factory = new TLSSocketFactory();
    factory.setSslConfig(new SslConfig());
    init = factory.createSSLContextInitializer();
    AssertJUnit.assertEquals(1, init.getTrustManagers().length);
    AssertJUnit.assertTrue(init.getTrustManagers()[0] instanceof AggregateTrustManager);
    tm = ((AggregateTrustManager) init.getTrustManagers()[0]).getTrustManagers();
    AssertJUnit.assertEquals(1, tm.length);
    AssertJUnit.assertEquals("sun.security.ssl.X509TrustManagerImpl", tm[0].getClass().getName());

    // trust managers
    factory = new TLSSocketFactory();
    factory.setSslConfig(new SslConfig(new AllowAnyTrustManager()));
    init = factory.createSSLContextInitializer();
    AssertJUnit.assertEquals(1, init.getTrustManagers().length);
    AssertJUnit.assertTrue(init.getTrustManagers()[0] instanceof AggregateTrustManager);
    tm = ((AggregateTrustManager) init.getTrustManagers()[0]).getTrustManagers();
    AssertJUnit.assertEquals(1, tm.length);
    AssertJUnit.assertEquals("org.ldaptive.ssl.AllowAnyTrustManager", tm[0].getClass().getName());

    // hostname verifier
    factory = new TLSSocketFactory();
    SslConfig sslConfig = new SslConfig();
    sslConfig.setHostnameVerifierConfig(new HostnameVerifierConfig(new DefaultHostnameVerifier()));
    factory.setSslConfig(sslConfig);
    init = factory.createSSLContextInitializer();
    AssertJUnit.assertEquals(1, init.getTrustManagers().length);
    AssertJUnit.assertTrue(init.getTrustManagers()[0] instanceof AggregateTrustManager);
    tm = ((AggregateTrustManager) init.getTrustManagers()[0]).getTrustManagers();
    AssertJUnit.assertEquals(2, tm.length);
    AssertJUnit.assertEquals("sun.security.ssl.X509TrustManagerImpl", tm[0].getClass().getName());
    AssertJUnit.assertEquals("org.ldaptive.ssl.HostnameVerifyingTrustManager", tm[1].getClass().getName());

    // trust managers and hostname verifier
    factory = new TLSSocketFactory();
    sslConfig = new SslConfig();
    sslConfig.setTrustManagers(new AllowAnyTrustManager());
    sslConfig.setHostnameVerifierConfig(new HostnameVerifierConfig(new DefaultHostnameVerifier()));
    factory.setSslConfig(sslConfig);
    init = factory.createSSLContextInitializer();
    AssertJUnit.assertEquals(1, init.getTrustManagers().length);
    AssertJUnit.assertTrue(init.getTrustManagers()[0] instanceof AggregateTrustManager);
    tm = ((AggregateTrustManager) init.getTrustManagers()[0]).getTrustManagers();
    AssertJUnit.assertEquals(2, tm.length);
    AssertJUnit.assertEquals("org.ldaptive.ssl.AllowAnyTrustManager", tm[0].getClass().getName());
    AssertJUnit.assertEquals("org.ldaptive.ssl.HostnameVerifyingTrustManager", tm[1].getClass().getName());

    // empty credential config
    factory = new TLSSocketFactory();
    factory.setSslConfig(new SslConfig(new X509CredentialConfig()));
    init = factory.createSSLContextInitializer();
    AssertJUnit.assertNull(init.getTrustManagers());

    // empty credential config with trust
    factory = new TLSSocketFactory();
    factory.setSslConfig(new SslConfig(new X509CredentialConfig(), new AllowAnyTrustManager()));
    init = factory.createSSLContextInitializer();
    AssertJUnit.assertEquals(1, init.getTrustManagers().length);
    AssertJUnit.assertTrue(init.getTrustManagers()[0] instanceof AggregateTrustManager);
    tm = ((AggregateTrustManager) init.getTrustManagers()[0]).getTrustManagers();
    AssertJUnit.assertEquals(1, tm.length);
    AssertJUnit.assertEquals("org.ldaptive.ssl.AllowAnyTrustManager", tm[0].getClass().getName());

    // empty credential config with hostname verifier
    factory = new TLSSocketFactory();
    sslConfig = new SslConfig();
    sslConfig.setHostnameVerifierConfig(new HostnameVerifierConfig(new DefaultHostnameVerifier()));
    factory.setSslConfig(sslConfig);
    init = factory.createSSLContextInitializer();
    AssertJUnit.assertEquals(1, init.getTrustManagers().length);
    AssertJUnit.assertTrue(init.getTrustManagers()[0] instanceof AggregateTrustManager);
    tm = ((AggregateTrustManager) init.getTrustManagers()[0]).getTrustManagers();
    AssertJUnit.assertEquals(2, tm.length);
    AssertJUnit.assertEquals("sun.security.ssl.X509TrustManagerImpl", tm[0].getClass().getName());
    AssertJUnit.assertEquals("org.ldaptive.ssl.HostnameVerifyingTrustManager", tm[1].getClass().getName());

    // empty credential config with trust and hostname verifier
    factory = new TLSSocketFactory();
    sslConfig = new SslConfig();
    sslConfig.setTrustManagers(new AllowAnyTrustManager());
    sslConfig.setHostnameVerifierConfig(new HostnameVerifierConfig(new DefaultHostnameVerifier()));
    factory.setSslConfig(sslConfig);
    init = factory.createSSLContextInitializer();
    AssertJUnit.assertEquals(1, init.getTrustManagers().length);
    AssertJUnit.assertTrue(init.getTrustManagers()[0] instanceof AggregateTrustManager);
    tm = ((AggregateTrustManager) init.getTrustManagers()[0]).getTrustManagers();
    AssertJUnit.assertEquals(2, tm.length);
    AssertJUnit.assertEquals("org.ldaptive.ssl.AllowAnyTrustManager", tm[0].getClass().getName());
    AssertJUnit.assertEquals("org.ldaptive.ssl.HostnameVerifyingTrustManager", tm[1].getClass().getName());

    // credential config
    factory = new TLSSocketFactory();
    factory.setSslConfig(createSslConfig());
    init = factory.createSSLContextInitializer();
    AssertJUnit.assertEquals(1, init.getTrustManagers().length);
    AssertJUnit.assertTrue(init.getTrustManagers()[0] instanceof AggregateTrustManager);
    tm = ((AggregateTrustManager) init.getTrustManagers()[0]).getTrustManagers();
    AssertJUnit.assertEquals(1, tm.length);
    AssertJUnit.assertEquals("sun.security.ssl.X509TrustManagerImpl", tm[0].getClass().getName());

    // credential config with trust
    factory = new TLSSocketFactory();
    sslConfig = createSslConfig();
    sslConfig.setTrustManagers(new AllowAnyTrustManager());
    factory.setSslConfig(sslConfig);
    init = factory.createSSLContextInitializer();
    AssertJUnit.assertEquals(1, init.getTrustManagers().length);
    AssertJUnit.assertTrue(init.getTrustManagers()[0] instanceof AggregateTrustManager);
    tm = ((AggregateTrustManager) init.getTrustManagers()[0]).getTrustManagers();
    AssertJUnit.assertEquals(2, tm.length);
    AssertJUnit.assertEquals("sun.security.ssl.X509TrustManagerImpl", tm[0].getClass().getName());
    AssertJUnit.assertEquals("org.ldaptive.ssl.AllowAnyTrustManager", tm[1].getClass().getName());

    // credential config with hostname verifier
    factory = new TLSSocketFactory();
    sslConfig = createSslConfig();
    sslConfig.setHostnameVerifierConfig(new HostnameVerifierConfig(new DefaultHostnameVerifier()));
    factory.setSslConfig(sslConfig);
    init = factory.createSSLContextInitializer();
    AssertJUnit.assertEquals(init.getTrustManagers().length, 1);
    AssertJUnit.assertTrue(init.getTrustManagers()[0] instanceof AggregateTrustManager);
    tm = ((AggregateTrustManager) init.getTrustManagers()[0]).getTrustManagers();
    AssertJUnit.assertEquals(tm.length, 2);
    AssertJUnit.assertEquals("sun.security.ssl.X509TrustManagerImpl", tm[0].getClass().getName());
    AssertJUnit.assertEquals("org.ldaptive.ssl.HostnameVerifyingTrustManager", tm[1].getClass().getName());

    // credential config with trust and hostname verifier
    factory = new TLSSocketFactory();
    sslConfig = createSslConfig();
    sslConfig.setTrustManagers(new AllowAnyTrustManager());
    sslConfig.setHostnameVerifierConfig(new HostnameVerifierConfig(new DefaultHostnameVerifier()));
    factory.setSslConfig(sslConfig);
    init = factory.createSSLContextInitializer();
    AssertJUnit.assertEquals(1, init.getTrustManagers().length);
    AssertJUnit.assertTrue(init.getTrustManagers()[0] instanceof AggregateTrustManager);
    tm = ((AggregateTrustManager) init.getTrustManagers()[0]).getTrustManagers();
    AssertJUnit.assertEquals(3, tm.length);
    AssertJUnit.assertEquals("sun.security.ssl.X509TrustManagerImpl", tm[0].getClass().getName());
    AssertJUnit.assertEquals("org.ldaptive.ssl.AllowAnyTrustManager", tm[1].getClass().getName());
    AssertJUnit.assertEquals("org.ldaptive.ssl.HostnameVerifyingTrustManager", tm[2].getClass().getName());
  }
}
