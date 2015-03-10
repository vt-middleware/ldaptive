/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link DERPath} class.
 *
 * @author  Middleware Services
 */
public class DERPathTest
{

  /** @throws  Exception  On test failure. */
  @Test(groups = {"asn1"})
  public void testPushPop()
    throws Exception
  {
    final DERPath path = new DERPath("/SEQ");
    path.pushNode("OCTSTR");
    Assert.assertEquals(path.toString(), "/SEQ/OCTSTR");
    Assert.assertEquals(path.popNode(), "OCTSTR");
    path.pushNode("INT", 1);
    Assert.assertEquals(path.toString(), "/SEQ/INT[1]");
    Assert.assertEquals(path.popNode(), "INT[1]");
    Assert.assertEquals(path.popNode(), "SEQ");
    Assert.assertNull(path.popNode());
    Assert.assertNull(path.popNode());
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"asn1"})
  public void testEquals()
    throws Exception
  {
    Assert.assertTrue(new DERPath("/SEQ[0]/OCTSTR[1]").equals(new DERPath("/SEQ[0]/OCTSTR[1]")));
    Assert.assertFalse(new DERPath("/SEQ[0]/OCTSTR[1]").equals(new DERPath("/SEQ[0]/OCTSTR[2]")));
    Assert.assertFalse(new DERPath("/SEQ[0]/OCTSTR[1]").equals(new DERPath("/SEQ/OCTSTR[2]")));
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"asn1"})
  public void testHashcode()
    throws Exception
  {
    Assert.assertEquals(new DERPath("/SEQ[0]/OCTSTR[1]").hashCode(), new DERPath("/SEQ[0]/OCTSTR[1]").hashCode());
    Assert.assertFalse(new DERPath("/SEQ[0]/OCTSTR[1]").hashCode() == new DERPath("/SEQ[0]/OCTSTR[2]").hashCode());
    Assert.assertFalse(new DERPath("/SEQ[0]/OCTSTR[1]").hashCode() == new DERPath("/SEQ/OCTSTR[2]").hashCode());
  }

  /**
   * DER path test data.
   *
   * @return  der paths
   */
  @DataProvider(name = "paths")
  public Object[][] createTestParams()
  {
    return
      new Object[][] {
        new Object[] {
          "/SET/SEQ/INT[1]",
          "/SET/SEQ/INT[1]",
        },
        new Object[] {
          "/SET/CTX(0)/INT",
          "/SET/CTX(0)/INT",
        },
        new Object[] {
          "/SET/APP(0)[1]/CTX(1)",
          "/SET/APP(0)[1]/CTX(1)",
        },
      };
  }


  /**
   * @param  testPath  to test
   * @param  expected  to test
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"asn1"}, dataProvider = "paths")
  public void testToString(final String testPath, final String expected)
    throws Exception
  {
    Assert.assertEquals(new DERPath(testPath).toString(), expected);
  }


  /**
   * @param  testPath  to test
   * @param  expected  to test
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"asn1"}, dataProvider = "paths")
  public void testEquals(final String testPath, final String expected)
    throws Exception
  {
    Assert.assertTrue(new DERPath(testPath).equals(new DERPath(expected)));
  }
}
