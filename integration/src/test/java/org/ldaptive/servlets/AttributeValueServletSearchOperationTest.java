/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.servlets;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import org.ldaptive.AbstractTest;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;
import org.ldaptive.TestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link AttributeValueServletSearchOperation}.
 *
 * @author  Middleware Services
 */
public class AttributeValueServletSearchOperationTest extends AbstractTest
{

  /** Entry created for tests. */
  private static LdapEntry testLdapEntry;

  /** To test servlets with. */
  private ServletRunner servletRunner;


  /**
   * @param  ldifFile  to create.
   * @param  webXml  web.xml for queries
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "createEntry12", "webXml" })
  @BeforeClass(groups = "servlet")
  public void createLdapEntry(final String ldifFile, final String webXml)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);

    servletRunner = new ServletRunner(new File(webXml));
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "servlet")
  public void deleteLdapEntry()
    throws Exception
  {
    super.deleteLdapEntry(testLdapEntry.getDn());
    servletRunner.shutDown();
  }


  /**
   * @param  query  to execute for.
   * @param  attr  attribute to return from execute
   * @param  attributeValue  to compare
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "attributeServletQuery",
      "attributeServletAttr",
      "attributeServletValue"
    })
  @Test(groups = "servlet")
  public void attributeServlet(final String query, final String attr, final String attributeValue)
    throws Exception
  {
    final ServletUnitClient sc = servletRunner.newClient();
    final WebRequest request = new PostMethodWebRequest("http://servlets.ldaptive.org/AttributeSearch");
    request.setParameter("query", query);
    request.setParameter("attrs", attr);

    final WebResponse response = sc.getResponse(request);

    Assert.assertNotNull(response);
    Assert.assertEquals(response.getContentType(), "application/octet-stream");
    Assert.assertEquals(
      response.getHeaderField("Content-Disposition"),
      "attachment; filename=\"" + attr + ".bin\"");

    final InputStream input = response.getInputStream();
    final ByteArrayOutputStream data = new ByteArrayOutputStream();
    if (input != null) {
      try {
        final byte[] buffer = new byte[128];
        int length;
        while ((length = input.read(buffer)) != -1) {
          data.write(buffer, 0, length);
        }
      } finally {
        data.close();
      }
    }
    Assert.assertEquals(LdapUtils.base64Encode(data.toByteArray()), attributeValue);
  }
}
