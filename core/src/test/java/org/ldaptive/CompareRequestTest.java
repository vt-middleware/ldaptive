/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link CompareRequest}.
 *
 * @author  Middleware Services
 */
public class CompareRequestTest
{


  /**
   * Compare test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          CompareRequest.builder()
            .dn("uid=jdoe,ou=People,dc=example,dc=com")
            .name("employeeType")
            .value("salaried").build(),
          new byte[] {
            // preamble
            0x30, 0x45, 0x02, 0x01, 0x02,
            // compare op
            0x6e, 0x40,
            // entry DN
            0x04, 0x24, 0x75, 0x69, 0x64, 0x3d, 0x6a, 0x64, 0x6f, 0x65, 0x2c, 0x6f, 0x75, 0x3d, 0x50, 0x65, 0x6f, 0x70,
            0x6c, 0x65, 0x2c, 0x64, 0x63, 0x3d, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2c, 0x64, 0x63, 0x3d, 0x63,
            0x6f, 0x6d,
            // attribute value assertion
            0x30, 0x18,
            0x04, 0x0c, 0x65, 0x6d, 0x70, 0x6c, 0x6f, 0x79, 0x65, 0x65, 0x54, 0x79, 0x70, 0x65,
            0x04, 0x08, 0x73, 0x61, 0x6c, 0x61, 0x72, 0x69, 0x65, 0x64},
        },
      };
  }


  /**
   * @param  request  compare request to encode.
   * @param  berValue  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "request")
  public void encode(final CompareRequest request, final byte[] berValue)
    throws Exception
  {
    Assert.assertEquals(request.encode(2), berValue);
  }


  /** Test {@link CompareRequest#toString()}. */
  @Test
  public void testToString()
  {
    // CheckStyle:Indentation OFF
    Assert.assertEquals(
      CompareRequest.builder().build().toString().split("::")[1],
      "controls=null, responseTimeout=null, dn=null, attributeDesc=null, assertionValue=null");
    Assert.assertEquals(
      new CompareRequest(null, null, null).toString().split("::")[1],
      "controls=null, responseTimeout=null, dn=null, attributeDesc=null, assertionValue=null");
    Assert.assertEquals(
      new CompareRequest("uid=1", null, null).toString().split("::")[1],
      "controls=null, responseTimeout=null, dn=uid=1, attributeDesc=null, assertionValue=null");
    Assert.assertEquals(
      new CompareRequest(null, "name", null).toString().split("::")[1],
      "controls=null, responseTimeout=null, dn=null, attributeDesc=name, assertionValue=null");
    Assert.assertEquals(
      new CompareRequest(null, null, "value").toString().split("::")[1],
      "controls=null, responseTimeout=null, dn=null, attributeDesc=null, assertionValue=value");
    Assert.assertEquals(
      new CompareRequest(null, "name", "value").toString().split("::")[1],
      "controls=null, responseTimeout=null, dn=null, attributeDesc=name, assertionValue=value");
    Assert.assertEquals(
      new CompareRequest(null, "userPassword", "password").toString().split("::")[1],
      "controls=null, responseTimeout=null, dn=null, attributeDesc=userPassword, assertionValue=<suppressed>");
    Assert.assertEquals(
      new CompareRequest("uid=1", "userPassword", "password").toString().split("::")[1],
      "controls=null, responseTimeout=null, dn=uid=1, attributeDesc=userPassword, assertionValue=<suppressed>");
    Assert.assertEquals(
      new CompareRequest("uid=1", "name", "value").toString().split("::")[1],
      "controls=null, responseTimeout=null, dn=uid=1, attributeDesc=name, assertionValue=value");
    // CheckStyle:Indentation ON
  }
}
