/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.Permission;
import org.ldaptive.AbstractTest;
import org.ldaptive.TestUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for ldap operation cli classes.
 *
 * @author  Middleware Services
 */
public class OperationCliTest extends AbstractTest
{

  /** System security manager. */
  private final SecurityManager securityManager = System.getSecurityManager();

  /**
   * @param  args  list of delimited arguments to pass to the CLI
   *
   * @throws  Exception  On test failure
   */
  @Parameters("cliAddArgs")
  @BeforeClass(groups = {"cli"})
  public void createLdapEntry(final String args)
    throws Exception
  {
    // don't allow System#exit
    System.setSecurityManager(
      new SecurityManager() {
        @Override
        public void checkPermission(final Permission permission)
        {
          if (permission.getName().startsWith("exitVM")) {
            throw new SecurityException("System.exit blocked.");
          }
        }
      });

    final PrintStream oldStdOut = System.out;
    try {
      final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      System.setOut(new PrintStream(outStream));

      AddOperationCli.main(args.split("\\|"));
    } catch (SecurityException e) {
      AssertJUnit.assertNotNull(e);
    } finally {
      // Restore STDOUT
      System.setOut(oldStdOut);
    }
  }


  /**
   * @param  args  list of delimited arguments to pass to the CLI
   *
   * @throws  Exception  On test failure
   */
  @Parameters("cliDeleteArgs")
  @AfterClass(groups = {"cli"})
  public void deleteLdapEntry(final String args)
    throws Exception
  {
    final PrintStream oldStdOut = System.out;
    try {
      final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      System.setOut(new PrintStream(outStream));

      DeleteOperationCli.main(args.split("\\|"));
    } catch (SecurityException e) {
      AssertJUnit.assertNotNull(e);
    } finally {
      // Restore STDOUT
      System.setOut(oldStdOut);
    }

    System.setSecurityManager(securityManager);
  }


  /**
   * @param  args  list of delimited arguments to pass to the CLI
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure
   */
  @Parameters({ "cliSearchArgs", "cliSearchResults" })
  @Test(groups = {"cli"})
  public void search(final String args, final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    final PrintStream oldStdOut = System.out;
    try {
      final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      System.setOut(new PrintStream(outStream));

      try {
        SearchOperationCli.main(args.split("\\|"));
      } catch (SecurityException e) {
        AssertJUnit.assertNotNull(e);
      }
      AssertJUnit.assertEquals(
        TestUtils.convertLdifToResult(ldif),
        TestUtils.convertLdifToResult(outStream.toString()));
    } finally {
      // Restore STDOUT
      System.setOut(oldStdOut);
    }
  }


  /**
   * @param  args  list of delimited arguments to pass to the CLI
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("cliCompareArgs")
  @Test(groups = {"cli"})
  public void compare(final String args)
    throws Exception
  {
    final PrintStream oldStdOut = System.out;
    try {
      final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      System.setOut(new PrintStream(outStream));

      try {
        CompareOperationCli.main(args.split("\\|"));
      } catch (SecurityException e) {
        AssertJUnit.assertNotNull(e);
      }
      AssertJUnit.assertEquals("true", outStream.toString().trim());
    } finally {
      // Restore STDOUT
      System.setOut(oldStdOut);
    }
  }
}
