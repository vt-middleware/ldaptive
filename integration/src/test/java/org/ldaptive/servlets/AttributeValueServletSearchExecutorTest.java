/*
  $Id: AttributeValueServletSearchExecutorTest.java 2541 2012-11-08 21:16:53Z dfisher $

  Copyright (C) 2003-2010 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2541 $
  Updated: $Date: 2012-11-08 16:16:53 -0500 (Thu, 08 Nov 2012) $
*/
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
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link AttributeValueServletSearchExecutor}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2541 $
 */
public class AttributeValueServletSearchExecutorTest extends AbstractTest
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
  @BeforeClass(groups = {"servlet"})
  public void createLdapEntry(final String ldifFile, final String webXml)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);

    servletRunner = new ServletRunner(new File(webXml));
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"servlet"})
  public void deleteLdapEntry()
    throws Exception
  {
    super.deleteLdapEntry(testLdapEntry.getDn());
    servletRunner.shutDown();
  }


  /**
   * @param  query  to search for.
   * @param  attr  attribute to return from search
   * @param  attributeValue  to compare
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "attributeServletQuery",
      "attributeServletAttr",
      "attributeServletValue"
    }
  )
  @Test(groups = {"servlet"})
  public void attributeServlet(
    final String query,
    final String attr,
    final String attributeValue)
    throws Exception
  {
    final ServletUnitClient sc = servletRunner.newClient();
    final WebRequest request = new PostMethodWebRequest(
      "http://servlets.ldaptive.org/AttributeSearch");
    request.setParameter("query", query);
    request.setParameter("attrs", attr);

    final WebResponse response = sc.getResponse(request);

    AssertJUnit.assertNotNull(response);
    AssertJUnit.assertEquals(
      "application/octet-stream",
      response.getContentType());
    AssertJUnit.assertEquals(
      "attachment; filename=\"" + attr + ".bin\"",
      response.getHeaderField("Content-Disposition"));

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
    AssertJUnit.assertEquals(
      attributeValue,
      LdapUtils.base64Encode(data.toByteArray()));
  }
}
