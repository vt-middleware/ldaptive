/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link DnParser}.
 *
 * @author  Middleware Services
 */
@SuppressWarnings("deprecation")
public class DnParserTest
{


  /**
   * DN test data.
   *
   * @return  test data
   */
  @DataProvider(name = "DNs")
  public Object[][] createDNs()
  {
    return
      new Object[][] {
        /* single value DN */
        new Object[] {
          "UID=jsmith",
          "uid",
          "jsmith",
        },
        /* multi value DN */
        new Object[] {
          "UID=jsmith,DC=ldaptive,DC=org",
          "uid",
          "jsmith",
        },
        /* whitespace */
        new Object[] {
          "  UID = jsmith   ,DC=ldaptive,DC=org",
          "Uid",
          "jsmith",
        },
        /* case sensitivity */
        new Object[] {
          "CN=Jim Smith,UID=jsmith,DC=ldaptive,DC=org",
          "UId",
          "jsmith",
        },
        /* multi value RDN */
        new Object[] {
          "OU=Sales+CN=J.  Smith,DC=ldaptive,DC=org",
          "ou",
          "Sales",
        },
        new Object[] {
          "OU=Sales+CN=J.  Smith,DC=ldaptive,DC=org",
          "cn",
          "J.  Smith",
        },
        /* escaped data */
        new Object[] {
          "CN=James \"Jim\" Smith\\, III,DC=ldaptive,DC=org",
          "CN",
          "James \"Jim\" Smith, III",
        },
        new Object[] {
          "OU=Sales\\; Data\\+Algorithms,DC=ldaptive,DC=org",
          "OU",
          "Sales; Data+Algorithms",
        },
        /* encoded data */
        new Object[] {
          "CN=\\23John Smith\\20,DC=ldaptive,DC=org",
          "CN",
          "#John Smith ",
        },
        new Object[] {
          "CN=Lu\\C4\\8Di\\C4\\87",
          "cn",
          "Lučić",
        },
        new Object[] {
          "CN=Lu\\C4\\8D\\C4\\8Di\\C4\\87",
          "cn",
          "Luččić",
        },
        new Object[] {
          "CN=Lu\\C4\\8D\\C4\\8Di\\C4\\87o",
          "cn",
          "Luččićo",
        },
        new Object[] {
          "CN=\\C4\\87Lu\\C4\\8D\\C4\\8Di\\C4\\87o",
          "cn",
          "ćLuččićo",
        },
        new Object[] {
          "1.3.6.1.4.1.1466.0=#04024869,DC=ldaptive,DC=org",
          "1.3.6.1.4.1.1466.0",
          "Hi",
        },
        new Object[] {
          "0.9.2342.19200300.100.1.1=#04066A736D697468,DC=ldaptive,DC=org",
          "0.9.2342.19200300.100.1.1",
          "jsmith",
        },
        /* invalid DNs, but can be parsed */
        new Object[] {
          "UID=jsmith,,DC=ldaptive,DC=org",
          ",dc",
          "ldaptive",
        },
        new Object[] {
          "UID=john\\?smith",
          "uid",
          "john?smith",
        },
        new Object[] {
          "UID=john\\GGsmith",
          "uid",
          "johnGGsmith",
        },
        new Object[] {
          "UID=john\\",
          "uid",
          "john",
        },
      };
  }


  /**
   * DN test data.
   *
   * @return  test data
   */
  @DataProvider(name = "invalidDNs")
  public Object[][] createInvalidDNs()
  {
    return
      new Object[][] {
        /* invalid hex */
        new Object[] {"1.1.1=#GG", },
        new Object[] {"1.1.1=#000", },
        new Object[] {"1.1.1=#F", },
        new Object[] {"1.1.1=#", },
        /* unescaped characters*/
        new Object[] {"UID=john,smith", },
        new Object[] {"UID=john+smith", },
        new Object[] {"UID=john\\Fsmith", },
        /* missing equals */
        new Object[] {"UID", },
        new Object[] {"UID:jsmith", },
        /* missing value */
        new Object[] {"UID=", },
        new Object[] {"UID =  ", },
      };
  }


  /**
   * @param  dn  to parse
   * @param  name  of an attribute to test
   * @param  value  of an attribute to test
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "dnParser", dataProvider = "DNs")
  public void testParsing(final String dn, final String name, final String value)
    throws Exception
  {
    Assert.assertEquals(DnParser.getValue(dn, name), value);
  }


  /**
   * @param  dn  to parse
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "dnParser", dataProvider = "invalidDNs")
  public void testInvalidParsing(final String dn)
    throws Exception
  {
    try {
      DnParser.getValue(dn, "");
      Assert.fail("Should have thrown IllegalArgumentException");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IllegalArgumentException.class);
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "dnParser")
  public void testSubstring()
    throws Exception
  {
    final String dn = "CN=Jim Smith,UID=jsmith,DC=ldaptive,DC=org";
    try {
      DnParser.substring(dn, -1);
      Assert.fail("Should have thrown IndexOutOfBoundsException");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IndexOutOfBoundsException.class);
    }
    Assert.assertEquals(DnParser.substring(dn, 0), "CN=Jim Smith,UID=jsmith,DC=ldaptive,DC=org");
    Assert.assertEquals(DnParser.substring(dn, 1), "UID=jsmith,DC=ldaptive,DC=org");
    Assert.assertEquals(DnParser.substring(dn, 2), "DC=ldaptive,DC=org");
    Assert.assertEquals(DnParser.substring(dn, 3), "DC=org");
    try {
      DnParser.substring(dn, 4);
      Assert.fail("Should have thrown IndexOutOfBoundsException");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IndexOutOfBoundsException.class);
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "dnParser")
  public void testSubstringEndIndex()
    throws Exception
  {
    final String dn = "CN=Jim Smith,UID=jsmith,DC=ldaptive,DC=org";
    try {
      DnParser.substring(dn, -1, 0);
      Assert.fail("Should have thrown IndexOutOfBoundsException");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IndexOutOfBoundsException.class);
    }
    try {
      DnParser.substring(dn, 2, 1);
      Assert.fail("Should have thrown IndexOutOfBoundsException");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IndexOutOfBoundsException.class);
    }
    Assert.assertEquals(DnParser.substring(dn, 0, 4), "CN=Jim Smith,UID=jsmith,DC=ldaptive,DC=org");
    Assert.assertEquals(DnParser.substring(dn, 1, 4), "UID=jsmith,DC=ldaptive,DC=org");
    Assert.assertEquals(DnParser.substring(dn, 2, 4), "DC=ldaptive,DC=org");
    Assert.assertEquals(DnParser.substring(dn, 3, 4), "DC=org");

    Assert.assertEquals(DnParser.substring(dn, 0, 1), "CN=Jim Smith");
    Assert.assertEquals(DnParser.substring(dn, 1, 2), "UID=jsmith");
    Assert.assertEquals(DnParser.substring(dn, 2, 3), "DC=ldaptive");
    Assert.assertEquals(DnParser.substring(dn, 3, 4), "DC=org");

    Assert.assertEquals(DnParser.substring(dn, 0, 2), "CN=Jim Smith,UID=jsmith");
    Assert.assertEquals(DnParser.substring(dn, 2, 4), "DC=ldaptive,DC=org");
    try {
      DnParser.substring(dn, 1, 5);
      Assert.fail("Should have thrown IndexOutOfBoundsException");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IndexOutOfBoundsException.class);
    }
  }
}
