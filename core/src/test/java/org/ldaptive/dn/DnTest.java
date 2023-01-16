/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link Dn}.
 *
 * @author  Middleware Services
 */
public class DnTest
{


  /**
   * DN test data.
   *
   * @return  test data
   */
  @DataProvider(name = "validDNs")
  public Object[][] createDNs()
  {
    final List<String> parseList = new ArrayList<>();
    final List<Dn> matchList = new ArrayList<>();
    final List<String> normList = new ArrayList<>();

    for (Object[] testData : new RDnTest().createRDNs()) {
      final String parse = (String) testData[0];
      final RDn match = (RDn) testData[1];
      final String norm = (String) testData[2];
      parseList.add(parse.concat(",DC=ldaptive,DC=org"));
      matchList.add(
        Dn.builder()
          .add(match)
          .add(new RDn("DC", "ldaptive"))
          .add(new RDn("DC", "org")).build());
      normList.add(norm.concat(",dc=ldaptive,dc=org"));
      parseList.add("CN=user,".concat(parse));
      matchList.add(
        Dn.builder()
          .add(new RDn("CN", "user"))
          .add(match).build());
      normList.add("cn=user,".concat(norm));
      parseList.add("CN=user,".concat(parse.concat(",DC=ldaptive,DC=org")));
      matchList.add(
        Dn.builder()
          .add(new RDn("CN", "user"))
          .add(match)
          .add(new RDn("DC", "ldaptive"))
          .add(new RDn("DC", "org")).build());
      normList.add("cn=user,".concat(norm.concat(",dc=ldaptive,dc=org")));
    }

    parseList.add("");
    matchList.add(Dn.builder().build());
    normList.add("");
    parseList.add(" ");
    matchList.add(Dn.builder().build());
    normList.add("");
    parseList.add("    ");
    matchList.add(Dn.builder().build());
    normList.add("");

    parseList.add("dc=org");
    matchList.add(Dn.builder().add(new RDn("dc", "org")).build());
    normList.add("dc=org");

    parseList.add("DC=ORG");
    matchList.add(Dn.builder().add(new RDn("DC", "ORG")).build());
    normList.add("dc=ORG");

    parseList.add("o=company inc.+c=US");
    matchList.add(Dn.builder().add(new RDn(new NameValue("o", "company inc."), new NameValue("c", "US"))).build());
    normList.add("c=US+o=company inc.");

    parseList.add("dc=ldaptive,dc=org");
    matchList.add(Dn.builder().add(new RDn("dc", "ldaptive")).add(new RDn("dc", "org")).build());
    normList.add("dc=ldaptive,dc=org");

    parseList.add(" dc = ldaptive , dc = org ");
    matchList.add(Dn.builder().add(new RDn("dc", "ldaptive")).add(new RDn("dc", "org")).build());
    normList.add("dc=ldaptive,dc=org");

    parseList.add("  dc  =  ldaptive  ,  dc  =  org  ");
    matchList.add(Dn.builder().add(new RDn("dc", "ldaptive")).add(new RDn("dc", "org")).build());
    normList.add("dc=ldaptive,dc=org");

    parseList.add("o=Company Inc.+dc=ldaptive,dc=org");
    matchList.add(
      Dn.builder()
        .add(new RDn(new NameValue("o", "Company Inc."), new NameValue("dc", "ldaptive")))
        .add(new RDn("dc", "org")).build());
    normList.add("dc=ldaptive+o=Company Inc.,dc=org");

    parseList.add("dc=ldaptive+o=Company Inc.+description=some text+cn=,dc=org");
    matchList.add(
      Dn.builder()
        .add(
          new RDn(
            new NameValue("dc", "ldaptive"),
            new NameValue("o", "Company Inc."),
            new NameValue("description", "some text"),
            new NameValue("cn", "")))
        .add(new RDn("dc", "org")).build());
    normList.add("cn=+dc=ldaptive+description=some text+o=Company Inc.,dc=org");

    parseList.add("1.2.3.4=foo,5.6.7.8=bar");
    matchList.add(Dn.builder().add(new RDn("1.2.3.4", "foo")).add(new RDn("5.6.7.8", "bar")).build());
    normList.add("1.2.3.4=foo,5.6.7.8=bar");

    final Object[][] returnData = new Object[parseList.size()][3];
    for (int i = 0; i < returnData.length; i++) {
      returnData[i][0] = parseList.get(i);
      returnData[i][1] = matchList.get(i);
      returnData[i][2] = normList.get(i);
    }
    return returnData;
  }


  /**
   * DN test data.
   *
   * @return  test data
   */
  @DataProvider(name = "invalidDNs")
  public Object[][] createInvalidDNs()
  {
    final List<String> parseList = new ArrayList<>();

    for (Object[] testData : new RDnTest().createInvalidRDNs()) {
      final String parse = (String) testData[0];
      if (!parse.isEmpty()) {
        parseList.add(parse);
        parseList.add(parse.concat(",DC=ldaptive,DC=org"));
        parseList.add(parse.concat(","));
      }
      parseList.add("CN=user,".concat(parse));
      parseList.add("CN=user,".concat(parse.concat(",DC=ldaptive,DC=org")));
      parseList.add(",".concat(parse));
    }

    parseList.add(",");
    parseList.add("+");
    parseList.add("dc=ldaptive,dc=org,");
    parseList.add("dc=ldaptive,,dc=org,");
    parseList.add("dc=ldaptive,dc=org, ");
    parseList.add(",dc=ldaptive,dc=org");
    parseList.add("dc=ldaptive,dc=org+");
    parseList.add("dc=ldaptive,dc=org+ ");
    parseList.add("dc=ldaptive+o=company inc.+,dc=org");

    final Object[][] returnData = new Object[parseList.size()][1];
    for (int i = 0; i < returnData.length; i++) {
      returnData[i][0] = parseList.get(i);
    }
    return returnData;
  }


  /**
   * @param  parse  DN to parse
   * @param  match  to match against parsed DN
   * @param  normalized  string to match
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "validDNs")
  public void testParsing(final String parse, final Dn match, final String normalized)
  {
    final Dn dn = new Dn(parse);
    Assert.assertEquals(dn, match);
    Assert.assertEquals(dn.format(new DefaultRDnNormalizer()), normalized);
  }


  /**
   * @param  parse  DN to parse
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "invalidDNs")
  public void testInvalidParsing(final String parse)
  {
    try {
      new Dn(parse);
      Assert.fail("Should have thrown IllegalArgumentException for " + parse);
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IllegalArgumentException.class);
    }
  }
}
