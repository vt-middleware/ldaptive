/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.ldaptive.ad.UnicodePwdAttribute;

/**
 * Contains functions common to all tests.
 *
 * @author  Middleware Services
 */
public abstract class AbstractTest
{

  /** Number of threads for threaded tests. */
  public static final int TEST_THREAD_POOL_SIZE = 2;

  /** Invocation count for threaded tests. */
  public static final int TEST_INVOCATION_COUNT = 10;

  /** Timeout for threaded tests. */
  public static final int TEST_TIME_OUT = 60000;

  /**
   * Add the BC provider.
   */
  static {
    Security.addProvider(new BouncyCastleProvider());
  }


  /**
   * Creates the supplied ldap entry and confirms it exists in the ldap.
   *
   * @param  entry  to create.
   *
   * @throws  Exception  On failure.
   */
  public void createLdapEntry(final LdapEntry entry)
    throws Exception
  {
    try (Connection conn = TestUtils.createSetupConnection()) {
      conn.open();

      final AddOperation create = new AddOperation(conn);
      create.execute(new AddRequest(entry.getDn(), entry.getAttributes()));
      if (!entryExists(conn, entry)) {
        throw new IllegalStateException("Could not add entry to LDAP");
      }
      if (TestControl.isActiveDirectory() && entry.getAttribute("userPassword") != null) {
        final ModifyOperation modify = new ModifyOperation(conn);
        modify.execute(
          new ModifyRequest(
            entry.getDn(),
            new AttributeModification(
              AttributeModificationType.REPLACE,
              new UnicodePwdAttribute("password" + entry.getAttribute("uid").getStringValue())),
            new AttributeModification(
              AttributeModificationType.REPLACE,
              new LdapAttribute("userAccountControl", "512"))));
      }
    } catch (LdapException e) {
      // ignore entry already exists
      if (ResultCode.ENTRY_ALREADY_EXISTS != e.getResultCode()) {
        throw e;
      }
    }
  }


  /**
   * Deletes the supplied dn if it exists.
   *
   * @param  dn  to delete
   *
   * @throws  Exception  On failure.
   */
  public void deleteLdapEntry(final String dn)
    throws Exception
  {
    try (Connection conn = TestUtils.createSetupConnection()) {
      conn.open();
      if (entryExists(conn, new LdapEntry(dn))) {
        final DeleteOperation delete = new DeleteOperation(conn);
        delete.execute(new DeleteRequest(dn));
      }
    }
  }


  /**
   * Performs a compare on the supplied entry to determine if it exists in the LDAP.
   *
   * @param  conn  to perform compare with
   * @param  entry  to perform compare on
   *
   * @return  whether the supplied entry exists
   *
   * @throws  Exception  On failure.
   */
  protected boolean entryExists(final Connection conn, final LdapEntry entry)
    throws Exception
  {
    final CompareOperation compare = new CompareOperation(conn);
    final LdapAttribute la = new LdapAttribute();
    la.setName("CN");
    la.addStringValue(DnParser.getValue(entry.getDn(), "CN"));
    try {
      return compare.execute(new CompareRequest(entry.getDn(), la)).getResult();
    } catch (LdapException e) {
      if (ResultCode.NO_SUCH_OBJECT == e.getResultCode()) {
        return false;
      }
      throw e;
    }
  }
}
