/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.io.StringReader;
import java.io.StringWriter;
import org.ldaptive.AbstractTest;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.TestControl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;
import static org.ldaptive.TestUtils.*;

/**
 * Unit test for {@link LdifReader} and {@link LdifWriter}.
 *
 * @author  Middleware Services
 */
public class LdifTest extends AbstractTest
{

  /** Entry created for ldap tests. */
  private static LdapEntry testLdapEntry;


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createEntry14")
  @BeforeClass(groups = "ldif")
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = readFileIntoString(ldifFile);
    testLdapEntry = convertLdifToEntry(ldif);
    super.createLdapEntry(testLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "ldif")
  public void deleteLdapEntry()
    throws Exception
  {
    super.deleteLdapEntry(testLdapEntry.getDn());
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({"ldifSearchDn", "ldifSearchFilter"})
  @Test(groups = "ldif")
  public void searchAndCompareLdif(final String dn, final String filter)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(createConnectionFactory());

    final SearchRequest request = new SearchRequest(dn, filter);
    if (TestControl.isActiveDirectory()) {
      request.setBinaryAttributes("objectSid", "objectGUID", "jpegPhoto");
    } else {
      request.setBinaryAttributes("jpegPhoto");
    }

    final SearchResponse result1 = search.execute(request);

    final StringWriter writer = new StringWriter();
    final LdifWriter ldifWriter = new LdifWriter(writer);
    ldifWriter.write(result1);

    final StringReader reader = new StringReader(writer.toString());
    final LdifReader ldifReader = new LdifReader(reader);
    final SearchResponse result2 = ldifReader.read();
    SearchResponseAssert.assertThat(result1).isSame(result2);
  }


  /**
   * @param  ldifFileIn  to create with
   * @param  ldifFileOut  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({"multipleLdifResultsIn", "multipleLdifResultsOut"})
  @Test(groups = "ldif")
  public void readAndCompareMultipleLdif(final String ldifFileIn, final String ldifFileOut)
    throws Exception
  {
    final String ldifStringIn = readFileIntoString(ldifFileIn);
    LdifReader ldifReader = new LdifReader(new StringReader(ldifStringIn));
    final SearchResponse result1 = ldifReader.read();

    final String ldifStringOut = readFileIntoString(ldifFileOut);
    ldifReader = new LdifReader(new StringReader(ldifStringOut));

    final SearchResponse result2 = ldifReader.read();

    assertThat(result1).isEqualTo(result2);
  }
}
