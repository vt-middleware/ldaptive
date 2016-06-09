/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.Response;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.TestControl;
import org.testng.AssertJUnit;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for class loader issues related to custom SSL socket factories.
 *
 * @author  Middleware Services
 */
public class ClassLoaderTest
{


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
   * @param  host  to connect to
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapSslTestHost")
  @Test(groups = {"ssl"})
  public void connectSSL(final String host)
    throws Exception
  {
    if (!TestControl.isJndiProvider()) {
      return;
    }

    // remove ldaptive classes from the classloader
    final TestClassLoader cl = new TestClassLoader(Thread.currentThread().getContextClassLoader());
    Thread.currentThread().setContextClassLoader(cl);

    final ConnectionConfig cc = new ConnectionConfig(host);
    cc.setUseSSL(true);
    cc.setSslConfig(createSslConfig());
    final Connection conn = DefaultConnectionFactory.getConnection(cc);
    try {
      conn.open();
      final SearchOperation op = new SearchOperation(conn);
      final Response<SearchResult> response = op.execute(SearchRequest.newObjectScopeSearchRequest(""));
      AssertJUnit.assertFalse(response.getResult().getEntries().isEmpty());
    } finally {
      conn.close();
    }
  }


  /**
   * Class loader that cannot find ldaptive classes.
   */
  public class TestClassLoader extends ClassLoader
  {


    /**
     * Creates a new test class loader.
     *
     * @param  cl  class loader delegate
     */
    public TestClassLoader(final ClassLoader cl)
    {
      super(cl);
    }


    @Override
    public Class<?> loadClass(final String name)
      throws ClassNotFoundException
    {
      if (name.startsWith("org.ldaptive")) {
        throw new ClassNotFoundException("Use a different class loader for ldaptive");
      }
      return loadClass(name, false);
    }
  }
}
