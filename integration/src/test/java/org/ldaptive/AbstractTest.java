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

  static {
    // Add the BC provider
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
    final ConnectionFactory cf = TestUtils.createSetupConnectionFactory();
    final AddOperation create = new AddOperation(cf);
    create.execute(new AddRequest(entry.getDn(), entry.getAttributes()));
    if (!entryExists(cf, entry)) {
      throw new IllegalStateException("Could not add entry to LDAP");
    }
    if (TestControl.isActiveDirectory() && entry.getAttribute("userPassword") != null) {
      final ModifyOperation modify = new ModifyOperation(cf);
      modify.execute(
        new ModifyRequest(
          entry.getDn(),
          new AttributeModification(
            AttributeModification.Type.REPLACE,
            new UnicodePwdAttribute("password" + entry.getAttribute("uid").getStringValue())),
          new AttributeModification(
            AttributeModification.Type.REPLACE,
            new LdapAttribute("userAccountControl", "512"))));
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
    final ConnectionFactory cf = TestUtils.createSetupConnectionFactory();
    if (entryExists(cf, LdapEntry.builder().dn(dn).build())) {
      final DeleteOperation delete = new DeleteOperation(cf);
      delete.execute(new DeleteRequest(dn));
    }
  }


  /**
   * Performs a compare on the supplied entry to determine if it exists in the LDAP.
   *
   * @param  cf  to perform compare with
   * @param  entry  to perform compare on
   *
   * @return  whether the supplied entry exists
   *
   * @throws  Exception  On failure.
   */
  protected boolean entryExists(final ConnectionFactory cf, final LdapEntry entry)
    throws Exception
  {
    final CompareOperation compare = new CompareOperation(cf);
    return compare.execute(new CompareRequest(entry.getDn(), "CN", DnParser.getValue(entry.getDn(), "CN"))).isTrue();
  }
}
