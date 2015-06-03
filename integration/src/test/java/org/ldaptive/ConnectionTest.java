/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

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
  @BeforeClass(groups = {"conn"})
  public void add(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();

    final Connection conn = TestUtils.createConnection();
    try {
      conn.open();

      final AddOperation add = new AddOperation(conn);
      final Response<Void> response = add.execute(new AddRequest(testLdapEntry.getDn(), testLdapEntry.getAttributes()));
      AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    } finally {
      conn.close();
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"conn"})
  public void compare()
    throws Exception
  {
    final Connection conn = TestUtils.createConnection();
    try {
      conn.open();

      final CompareOperation compare = new CompareOperation(conn);
      AssertJUnit.assertTrue(
        compare.execute(new CompareRequest(testLdapEntry.getDn(), testLdapEntry.getAttribute("mail"))).getResult());
    } finally {
      conn.close();
    }
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"conn"})
  public void delete()
    throws Exception
  {
    final Connection conn = TestUtils.createConnection();
    try {
      conn.open();

      final DeleteOperation delete = new DeleteOperation(conn);
      final Response<Void> response = delete.execute(new DeleteRequest(testLdapEntry.getDn()));
      AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    } finally {
      conn.close();
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"conn"})
  public void modify()
    throws Exception
  {
    final Connection conn = TestUtils.createConnection();
    try {
      conn.open();

      final ModifyOperation modify = new ModifyOperation(conn);
      final Response<Void> response = modify.execute(
        new ModifyRequest(
          testLdapEntry.getDn(),
          new AttributeModification(AttributeModificationType.ADD, new LdapAttribute("title", "President"))));
      AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    } finally {
      conn.close();
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"conn"})
  public void modifyDn()
    throws Exception
  {
    final Connection conn = TestUtils.createConnection();
    try {
      conn.open();

      final ModifyDnOperation modifyDn = new ModifyDnOperation(conn);
      Response<Void> response = modifyDn.execute(
        new ModifyDnRequest(
          testLdapEntry.getDn(),
          "cn=James Buchanan Jr.," + DnParser.substring(testLdapEntry.getDn(), 1)));
      AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
      response = modifyDn.execute(
        new ModifyDnRequest(
          "cn=James Buchanan Jr.," + DnParser.substring(testLdapEntry.getDn(), 1),
          testLdapEntry.getDn()));
      AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    } finally {
      conn.close();
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"conn"})
  public void search()
    throws Exception
  {
    final Connection conn = TestUtils.createConnection();
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      final SearchResult lr = search.execute(
        new SearchRequest(DnParser.substring(testLdapEntry.getDn(), 1), new SearchFilter("(uid=15)"))).getResult();
      AssertJUnit.assertEquals(testLdapEntry.getDn().toLowerCase(), lr.getEntry().getDn().toLowerCase());
    } finally {
      conn.close();
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"conn"})
  public void strategyConnect()
    throws Exception
  {
    ConnectionConfig cc = TestUtils.readConnectionConfig("classpath:/org/ldaptive/ldap.conn.properties");
    cc.setConnectionStrategy(ConnectionStrategy.ROUND_ROBIN);
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
    cc.setConnectionStrategy(ConnectionStrategy.DEFAULT);
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
    cc.setConnectionStrategy(ConnectionStrategy.ACTIVE_PASSIVE);
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
    cc.setConnectionStrategy(ConnectionStrategy.RANDOM);
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
   * Sleeps at the end of all tests and checks open connections.
   *
   * @param  host  to check for connections with.
   * @param  sleepTime  time to sleep for.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "ldapTestHost", "sleepTime" })
  @AfterSuite(groups = {"conn"})
  public void sleep(final String host, final int sleepTime)
    throws Exception
  {
    Thread.sleep(sleepTime);

    /*
     * -- expected open connections --
     * SearchOperationTest: 1
     */
    final LdapURL ldapUrl = new LdapURL(host);
    final int openConns = TestUtils.countOpenConnections(
      ldapUrl.getEntry().getHostname().substring(0, ldapUrl.getEntry().getHostname().indexOf(".")));
    AssertJUnit.assertEquals(1, openConns);
  }
}
