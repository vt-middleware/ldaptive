/*
  $Id: TLSSocketFactoryTest.java 2979 2014-05-05 19:45:13Z dfisher $

  Copyright (C) 2003-2010 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2979 $
  Updated: $Date: 2014-05-05 15:45:13 -0400 (Mon, 05 May 2014) $
*/
package org.ldaptive.ssl;

import java.net.Socket;
import java.util.Arrays;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import org.ldaptive.CompareOperation;
import org.ldaptive.CompareRequest;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapURL;
import org.ldaptive.provider.Provider;
import org.testng.AssertJUnit;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link TLSSocketFactory}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2979 $
 */
public class TLSSocketFactoryTest
{

  /** List of ciphers. */
  public static final String[] CIPHERS = new String[] {
    "TLS_DH_anon_WITH_AES_128_CBC_SHA",
    "TLS_DH_anon_WITH_AES_256_CBC_SHA",
    "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA",
    "SSL_DH_anon_WITH_RC4_128_MD5",
    "TLS_RSA_WITH_AES_128_CBC_SHA",
    "TLS_RSA_WITH_AES_256_CBC_SHA",
    "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
    "TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
    "TLS_DHE_DSS_WITH_AES_256_CBC_SHA",
    "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
    "TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
    "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
    "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
    "SSL_RSA_WITH_RC4_128_MD5",
    "SSL_RSA_WITH_RC4_128_SHA",
  };

  /** List of ciphers. */
  public static final String[] UNKNOWN_CIPHERS = new String[] {
    "TLS_DH_anon_WITH_AES_128_CBC_SHA",
    "TLS_DH_anon_WITH_3DES_256_CBC_SHA",
    "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA",
    "SSL_DH_anon_WITH_RC4_128_MD5",
  };

  /** List of protocols. */
  public static final String[] ALL_PROTOCOLS = new String[] {
    "SSLv3",
    "TLSv1",
  };

  /** List of protocols. */
  public static final String[] PROTOCOLS = new String[] {
    "TLSv1",
  };

  /** List of protocols. */
  public static final String[] FAIL_PROTOCOLS = new String[] {
    "SSLv2Hello",
  };

  /** List of protocols. */
  public static final String[] UNKNOWN_PROTOCOLS = new String[] {
    "SSLv3Hello",
    "SSLv2Hello",
    "TLSv1",
  };


  /**
   * @return  ssl config
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
  @Parameters({ "ldapTestHost" })
  @Test(groups = {"ssl"})
  public void connectTLS(final String url)
    throws Exception
  {
    // with no trusted certificates, connection should fail
    final ConnectionConfig cc = createTLSConnectionConfig(url);
    cc.setSslConfig(null);
    Connection conn = DefaultConnectionFactory.getConnection(cc);
    try {
      // some providers won't report errors until an operation is
      // executed
      conn.open();
      final CompareOperation op = new CompareOperation(conn);
      op.execute(
        new CompareRequest("", new LdapAttribute("objectClass", "top")));
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    } finally {
      conn.close();
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "ldapSslTestHost" })
  @Test(groups = {"ssl"})
  public void connectSSL(final String url)
    throws Exception
  {
    // with no trusted certificates, connection should fail
    final ConnectionConfig cc = createSSLConnectionConfig(url);
    cc.setSslConfig(null);
    Connection conn = DefaultConnectionFactory.getConnection(cc);
    try {
      conn.open();
      // some providers won't perform the handshake until an operation is
      // executed
      final CompareOperation op = new CompareOperation(conn);
      op.execute(
        new CompareRequest("", new LdapAttribute("objectClass", "top")));
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
    } finally {
      conn.close();
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "ldapTestHost" })
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
    AssertJUnit.assertNotSame(
      Arrays.asList(sf.getDefaultCipherSuites()), Arrays.asList(CIPHERS));

    cc.getSslConfig().setEnabledCipherSuites(UNKNOWN_CIPHERS);
    Connection conn = DefaultConnectionFactory.getConnection(cc);
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
  @Parameters({ "ldapTestHost" })
  @Test(groups = {"ssl"})
  public void setEnabledProtocols(final String url)
    throws Exception
  {
    final Provider<?> p = DefaultConnectionFactory.getDefaultProvider();
    if (p.getClass().getName().equals(
      "org.ldaptive.provider.opends.OpenDSProvider")) {
      throw new UnsupportedOperationException("Test causes hang");
    }
    if (p.getClass().getName().equals(
      "org.ldaptive.provider.opendj.OpenDJProvider")) {
      throw new UnsupportedOperationException("Test causes hang");
    }
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

    cc.getSslConfig().setEnabledProtocols(UNKNOWN_PROTOCOLS);
    conn = DefaultConnectionFactory.getConnection(cc);
    try {
      conn.open();
      AssertJUnit.fail("Should have thrown Exception, no exception thrown");
    } catch (Exception e) {
      AssertJUnit.assertNotNull(e);
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
  @Parameters({ "ldapSslTestHost" })
  @Test(groups = {"ssl"})
  public void setHostnameVerifier(final String url)
    throws Exception
  {
    final LdapURL ldapUrl = new LdapURL(url);
    final TLSSocketFactory sf = new TLSSocketFactory();
    sf.initialize();

    sf.setHostnameVerifier(new AnyHostnameVerifier());
    Socket s = null;
    try {
      s = sf.createSocket(
        ldapUrl.getEntry().getHostname(), ldapUrl.getEntry().getPort());
    } finally {
      s.close();
    }

    sf.setHostnameVerifier(new NoHostnameVerifier());
    try {
      s = sf.createSocket(
        ldapUrl.getEntry().getHostname(), ldapUrl.getEntry().getPort());
      AssertJUnit.fail("Should have thrown SSLPeerUnverifiedException");
    } catch (Exception e) {
      AssertJUnit.assertEquals(SSLPeerUnverifiedException.class, e.getClass());
    } finally {
      s.close();
    }
  }


  /**
   * @param  url  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "ldapSslTestHost" })
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

    Socket s = null;
    try {
      s = sf.createSocket(
        ldapUrl.getEntry().getHostname(), ldapUrl.getEntry().getPort());
    } finally {
      s.close();
    }
  }
}
