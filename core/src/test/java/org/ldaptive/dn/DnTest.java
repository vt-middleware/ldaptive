/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

// CheckStyle:AvoidStaticImport OFF
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.ldaptive.dn.DefaultRDnNormalizer.COMPRESS;
import static org.ldaptive.dn.DefaultRDnNormalizer.LOWERCASE_COMPRESS;
import static org.ldaptive.dn.DefaultRDnNormalizer.LOWERCASE;
// CheckStyle:AvoidStaticImport ON

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
    final AttributeValueEscaper escaper = new DefaultAttributeValueEscaper();
    final List<String> parseList = new ArrayList<>();
    final List<Dn> matchList = new ArrayList<>();
    final List<RDnNormalizer[]> normalizerList = new ArrayList<>();
    final List<String> normalizedList = new ArrayList<>();

    for (Object[] testData : new RDnTest().createRDNs()) {
      final String parse = (String) testData[0];
      final RDn match = (RDn) testData[1];
      final RDnNormalizer[] normalizers = (RDnNormalizer[]) testData[2];
      final String norm = (String) testData[3];
      parseList.add(parse.concat(",DC=ldaptive,DC=org"));
      matchList.add(
        Dn.builder()
          .add(match)
          .add(new RDn("DC", "ldaptive"))
          .add(new RDn("DC", "org")).build());
      normalizerList.add(normalizers);
      normalizedList.add(norm.concat(",dc=ldaptive,dc=org"));
      parseList.add("CN=user,".concat(parse));
      matchList.add(
        Dn.builder()
          .add(new RDn("CN", "user"))
          .add(match).build());
      normalizerList.add(normalizers);
      normalizedList.add("cn=user,".concat(norm));
      parseList.add("CN=user,".concat(parse.concat(",DC=ldaptive,DC=org")));
      matchList.add(
        Dn.builder()
          .add(new RDn("CN", "user"))
          .add(match)
          .add(new RDn("DC", "ldaptive"))
          .add(new RDn("DC", "org")).build());
      normalizerList.add(normalizers);
      normalizedList.add("cn=user,".concat(norm.concat(",dc=ldaptive,dc=org")));
    }

    parseList.add("");
    matchList.add(Dn.builder().build());
    normalizerList.add(
      new RDnNormalizer[] {
        new DefaultRDnNormalizer(escaper, LOWERCASE, s -> s),
        new DefaultRDnNormalizer(escaper, LOWERCASE, COMPRESS),
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE),
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE_COMPRESS),
      });
    normalizedList.add("");
    parseList.add(" ");
    matchList.add(Dn.builder().build());
    normalizerList.add(
      new RDnNormalizer[] {
        new DefaultRDnNormalizer(escaper, LOWERCASE, s -> s),
        new DefaultRDnNormalizer(escaper, LOWERCASE, COMPRESS),
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE),
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE_COMPRESS),
      });
    normalizedList.add("");
    parseList.add("    ");
    matchList.add(Dn.builder().build());
    normalizerList.add(
      new RDnNormalizer[] {
        new DefaultRDnNormalizer(escaper, LOWERCASE, s -> s),
        new DefaultRDnNormalizer(escaper, LOWERCASE, COMPRESS),
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE),
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE_COMPRESS),
      });
    normalizedList.add("");

    parseList.add("dc=org");
    matchList.add(Dn.builder().add(new RDn("dc", "org")).build());
    normalizerList.add(
      new RDnNormalizer[] {
        new DefaultRDnNormalizer(escaper, LOWERCASE, s -> s),
        new DefaultRDnNormalizer(escaper, LOWERCASE, COMPRESS),
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE),
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE_COMPRESS),
      });
    normalizedList.add("dc=org");

    parseList.add("DC=ORG");
    matchList.add(Dn.builder().add(new RDn("DC", "ORG")).build());
    normalizerList.add(
      new RDnNormalizer[] {
        new DefaultRDnNormalizer(escaper, LOWERCASE, s -> s),
        new DefaultRDnNormalizer(escaper, LOWERCASE, COMPRESS),
      });
    normalizedList.add("dc=ORG");

    parseList.add("DC=ORG");
    matchList.add(Dn.builder().add(new RDn("DC", "ORG")).build());
    normalizerList.add(
      new RDnNormalizer[] {
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE),
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE_COMPRESS),
      });
    normalizedList.add("dc=org");

    parseList.add("o=company inc.+c=US");
    matchList.add(Dn.builder().add(new RDn(new NameValue("o", "company inc."), new NameValue("c", "US"))).build());
    normalizerList.add(
      new RDnNormalizer[] {
        new DefaultRDnNormalizer(escaper, LOWERCASE, s -> s),
        new DefaultRDnNormalizer(escaper, LOWERCASE, COMPRESS),
      });
    normalizedList.add("c=US+o=company inc.");

    parseList.add("o=company inc.+c=US");
    matchList.add(Dn.builder().add(new RDn(new NameValue("o", "company inc."), new NameValue("c", "US"))).build());
    normalizerList.add(
      new RDnNormalizer[] {
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE),
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE_COMPRESS),
      });
    normalizedList.add("c=us+o=company inc.");

    parseList.add("dc=ldaptive,dc=org");
    matchList.add(Dn.builder().add(new RDn("dc", "ldaptive")).add(new RDn("dc", "org")).build());
    normalizerList.add(
      new RDnNormalizer[] {
        new DefaultRDnNormalizer(escaper, LOWERCASE, s -> s),
        new DefaultRDnNormalizer(escaper, LOWERCASE, COMPRESS),
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE),
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE_COMPRESS),
      });
    normalizedList.add("dc=ldaptive,dc=org");

    parseList.add(" dc = ldaptive , dc = org ");
    matchList.add(Dn.builder().add(new RDn("dc", "ldaptive")).add(new RDn("dc", "org")).build());
    normalizerList.add(
      new RDnNormalizer[] {
        new DefaultRDnNormalizer(escaper, LOWERCASE, s -> s),
        new DefaultRDnNormalizer(escaper, LOWERCASE, COMPRESS),
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE),
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE_COMPRESS),
      });
    normalizedList.add("dc=ldaptive,dc=org");

    parseList.add("  dc  =  ldaptive  ,  dc  =  org  ");
    matchList.add(Dn.builder().add(new RDn("dc", "ldaptive")).add(new RDn("dc", "org")).build());
    normalizerList.add(
      new RDnNormalizer[] {
        new DefaultRDnNormalizer(escaper, LOWERCASE, s -> s),
        new DefaultRDnNormalizer(escaper, LOWERCASE, COMPRESS),
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE),
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE_COMPRESS),
      });
    normalizedList.add("dc=ldaptive,dc=org");

    parseList.add("o=Company Inc.+dc=ldaptive,dc=org");
    matchList.add(
      Dn.builder()
        .add(new RDn(new NameValue("o", "Company Inc."), new NameValue("dc", "ldaptive")))
        .add(new RDn("dc", "org")).build());
    normalizerList.add(
      new RDnNormalizer[] {
        new DefaultRDnNormalizer(escaper, LOWERCASE, s -> s),
        new DefaultRDnNormalizer(escaper, LOWERCASE, COMPRESS),
      });
    normalizedList.add("dc=ldaptive+o=Company Inc.,dc=org");

    parseList.add("o=Company Inc.+dc=ldaptive,dc=org");
    matchList.add(
      Dn.builder()
        .add(new RDn(new NameValue("o", "Company Inc."), new NameValue("dc", "ldaptive")))
        .add(new RDn("dc", "org")).build());
    normalizerList.add(
      new RDnNormalizer[] {
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE),
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE_COMPRESS),
      });
    normalizedList.add("dc=ldaptive+o=company inc.,dc=org");

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
    normalizerList.add(
      new RDnNormalizer[] {
        new DefaultRDnNormalizer(escaper, LOWERCASE, s -> s),
        new DefaultRDnNormalizer(escaper, LOWERCASE, COMPRESS),
      });
    normalizedList.add("cn=+dc=ldaptive+description=some text+o=Company Inc.,dc=org");

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
    normalizerList.add(
      new RDnNormalizer[] {
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE),
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE_COMPRESS),
      });
    normalizedList.add("cn=+dc=ldaptive+description=some text+o=company inc.,dc=org");

    parseList.add("1.2.3.4=foo,5.6.7.8=bar");
    matchList.add(Dn.builder().add(new RDn("1.2.3.4", "foo")).add(new RDn("5.6.7.8", "bar")).build());
    normalizerList.add(
      new RDnNormalizer[] {
        new DefaultRDnNormalizer(escaper, LOWERCASE, s -> s),
        new DefaultRDnNormalizer(escaper, LOWERCASE, COMPRESS),
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE),
        new DefaultRDnNormalizer(escaper, LOWERCASE, LOWERCASE_COMPRESS),
      });
    normalizedList.add("1.2.3.4=foo,5.6.7.8=bar");

    parseList.add("uid=username,ou=Лаборатория");
    matchList.add(Dn.builder().add(new RDn("uid", "username")).add(new RDn("ou", "Лаборатория")).build());
    normalizerList.add(
      new RDnNormalizer[] {
        new DefaultRDnNormalizer(escaper, LOWERCASE, s -> s),
        new DefaultRDnNormalizer(escaper, LOWERCASE, COMPRESS),
      });
    normalizedList.add(
      "uid=username,ou=\\D0\\9B\\D0\\B0\\D0\\B1\\D0\\BE\\D1\\80\\D0\\B0\\D1\\82\\D0\\BE\\D1\\80\\D0\\B8\\D1\\8F");

    final Object[][] returnData = new Object[parseList.size()][4];
    for (int i = 0; i < returnData.length; i++) {
      returnData[i][0] = parseList.get(i);
      returnData[i][1] = matchList.get(i);
      returnData[i][2] = normalizerList.get(i);
      returnData[i][3] = normalizedList.get(i);
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
   * @param  normalizers  to normalize DN
   * @param  normalized  string to match
   */
  @Test(dataProvider = "validDNs")
  public void testParsing(
    final String parse,
    final Dn match,
    final RDnNormalizer[] normalizers,
    final String normalized)
  {
    final Dn dn = new Dn(parse);
    Assert.assertEquals(dn, match);
    for (RDnNormalizer normalizer : normalizers) {
      Assert.assertEquals(dn.format(normalizer), normalized, "Normalizer: " + normalizer);
    }
  }


  /**
   * @param  parse  DN to parse
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

  @Test
  public void testSubDn()
  {
    Dn dn = new Dn("uid=alice,ou=people,dc=ldaptive,dc=org");
    dn = dn.subDn(1);
    Assert.assertEquals(dn.format(), "ou=people,dc=ldaptive,dc=org");
    dn = dn.subDn(1);
    Assert.assertEquals(dn.format(), "dc=ldaptive,dc=org");
    dn = dn.subDn(1);
    Assert.assertEquals(dn.format(), "dc=org");
    dn = dn.subDn(1);
    Assert.assertEquals(dn.format(), "");
    Assert.assertTrue(dn.isEmpty());
    dn = dn.subDn(1);
    Assert.assertNull(dn);
  }


  @Test
  public void testGetParent()
  {
    final Dn dn = new Dn("uid=alice,ou=people,dc=ldaptive,dc=org");
    Assert.assertEquals(dn.getParent().format(), "ou=people,dc=ldaptive,dc=org");
  }


  @Test
  public void testIsSame()
  {
    final Dn dn1 = new Dn("UID=bar+CN=foo,OU = people,DC=ldaptive,DC=org");
    final Dn dn1Norm = new Dn("cn=foo+uid=bar,ou=people,dc=ldaptive,dc=org");
    final Dn dn2 = new Dn("UID=bar,OU = people,DC=ldaptive,DC=org");
    final Dn dn2Norm = new Dn("uid=bar,ou=people,dc=ldaptive,dc=org");
    final Dn dn3 = new Dn("OU = people,DC=ldaptive,DC=org");
    final Dn dn3Norm = new Dn("ou=people,dc=ldaptive,dc=org");
    final Dn dn4 = new Dn("DC=ldaptive,DC=org");
    final Dn dn4Norm = new Dn("dc=ldaptive,dc=org");
    final Dn dn5 = new Dn("DC=org");
    final Dn dn5Norm = new Dn("dc=org");
    final Dn nullDn = new Dn();

    Assert.assertTrue(dn1.isSame(dn1, null));
    Assert.assertFalse(dn1.isSame(dn1Norm, null));
    Assert.assertFalse(dn1.isSame(dn2, null));
    Assert.assertFalse(dn1.isSame(dn2Norm, null));
    Assert.assertFalse(dn1.isSame(dn3, null));
    Assert.assertFalse(dn1.isSame(dn3Norm, null));
    Assert.assertFalse(dn1.isSame(dn4, null));
    Assert.assertFalse(dn1.isSame(dn4Norm, null));
    Assert.assertFalse(dn1.isSame(dn5, null));
    Assert.assertFalse(dn1.isSame(dn5Norm, null));
    Assert.assertFalse(dn1.isSame(nullDn, null));

    Assert.assertFalse(dn2.isSame(dn1, null));
    Assert.assertFalse(dn2.isSame(dn1Norm, null));
    Assert.assertTrue(dn2.isSame(dn2, null));
    Assert.assertFalse(dn2.isSame(dn2Norm, null));
    Assert.assertFalse(dn2.isSame(dn3, null));
    Assert.assertFalse(dn2.isSame(dn3Norm, null));
    Assert.assertFalse(dn2.isSame(dn4, null));
    Assert.assertFalse(dn2.isSame(dn4Norm, null));
    Assert.assertFalse(dn2.isSame(dn5, null));
    Assert.assertFalse(dn2.isSame(dn5Norm, null));
    Assert.assertFalse(dn2.isSame(nullDn, null));

    Assert.assertFalse(dn3.isSame(dn1, null));
    Assert.assertFalse(dn3.isSame(dn1Norm, null));
    Assert.assertFalse(dn3.isSame(dn2, null));
    Assert.assertFalse(dn3.isSame(dn2Norm, null));
    Assert.assertTrue(dn3.isSame(dn3, null));
    Assert.assertFalse(dn3.isSame(dn3Norm, null));
    Assert.assertFalse(dn3.isSame(dn4, null));
    Assert.assertFalse(dn3.isSame(dn4Norm, null));
    Assert.assertFalse(dn3.isSame(dn5, null));
    Assert.assertFalse(dn3.isSame(dn5Norm, null));
    Assert.assertFalse(dn3.isSame(nullDn, null));

    Assert.assertFalse(dn4.isSame(dn1, null));
    Assert.assertFalse(dn4.isSame(dn1Norm, null));
    Assert.assertFalse(dn4.isSame(dn2, null));
    Assert.assertFalse(dn4.isSame(dn2Norm, null));
    Assert.assertFalse(dn4.isSame(dn3, null));
    Assert.assertFalse(dn4.isSame(dn3Norm, null));
    Assert.assertTrue(dn4.isSame(dn4, null));
    Assert.assertFalse(dn4.isSame(dn4Norm, null));
    Assert.assertFalse(dn4.isSame(dn5, null));
    Assert.assertFalse(dn4.isSame(dn5Norm, null));
    Assert.assertFalse(dn5.isSame(nullDn, null));

    Assert.assertFalse(dn5.isSame(dn1, null));
    Assert.assertFalse(dn5.isSame(dn1Norm, null));
    Assert.assertFalse(dn5.isSame(dn2, null));
    Assert.assertFalse(dn5.isSame(dn2Norm, null));
    Assert.assertFalse(dn5.isSame(dn3, null));
    Assert.assertFalse(dn5.isSame(dn3Norm, null));
    Assert.assertFalse(dn5.isSame(dn4, null));
    Assert.assertFalse(dn5.isSame(dn4Norm, null));
    Assert.assertTrue(dn5.isSame(dn5, null));
    Assert.assertFalse(dn5.isSame(dn5Norm, null));
    Assert.assertFalse(dn5.isSame(nullDn, null));

    Assert.assertFalse(nullDn.isSame(dn1, null));
    Assert.assertFalse(nullDn.isSame(dn1Norm, null));
    Assert.assertFalse(nullDn.isSame(dn2, null));
    Assert.assertFalse(nullDn.isSame(dn2Norm, null));
    Assert.assertFalse(nullDn.isSame(dn3, null));
    Assert.assertFalse(nullDn.isSame(dn3Norm, null));
    Assert.assertFalse(nullDn.isSame(dn4, null));
    Assert.assertFalse(nullDn.isSame(dn4Norm, null));
    Assert.assertFalse(nullDn.isSame(dn5, null));
    Assert.assertFalse(nullDn.isSame(dn5Norm, null));
    Assert.assertTrue(nullDn.isSame(nullDn, null));
  }


  @Test
  public void testIsSameWithNormalizer()
  {
    final DefaultRDnNormalizer normalizer = new DefaultRDnNormalizer();
    final Dn dn1 = new Dn("UID=bar+CN=foo,OU = people,DC=ldaptive,DC=org");
    final Dn dn1Norm = new Dn("cn=foo+uid=bar,ou=people,dc=ldaptive,dc=org");
    final Dn dn2 = new Dn("UID=bar,OU = people,DC=ldaptive,DC=org");
    final Dn dn2Norm = new Dn("uid=bar,ou=people,dc=ldaptive,dc=org");
    final Dn dn3 = new Dn("OU = people,DC=ldaptive,DC=org");
    final Dn dn3Norm = new Dn("ou=people,dc=ldaptive,dc=org");
    final Dn dn4 = new Dn("DC=ldaptive,DC=org");
    final Dn dn4Norm = new Dn("dc=ldaptive,dc=org");
    final Dn dn5 = new Dn("DC=org");
    final Dn dn5Norm = new Dn("dc=org");
    final Dn nullDn = new Dn();

    Assert.assertTrue(dn1.isSame(dn1, normalizer));
    Assert.assertTrue(dn1.isSame(dn1Norm, normalizer));
    Assert.assertFalse(dn1.isSame(dn2, normalizer));
    Assert.assertFalse(dn1.isSame(dn2Norm, normalizer));
    Assert.assertFalse(dn1.isSame(dn3, normalizer));
    Assert.assertFalse(dn1.isSame(dn3Norm, normalizer));
    Assert.assertFalse(dn1.isSame(dn4, normalizer));
    Assert.assertFalse(dn1.isSame(dn4Norm, normalizer));
    Assert.assertFalse(dn1.isSame(dn5, normalizer));
    Assert.assertFalse(dn1.isSame(dn5Norm, normalizer));
    Assert.assertFalse(dn1.isSame(nullDn, normalizer));

    Assert.assertFalse(dn2.isSame(dn1, normalizer));
    Assert.assertFalse(dn2.isSame(dn1Norm, normalizer));
    Assert.assertTrue(dn2.isSame(dn2, normalizer));
    Assert.assertTrue(dn2.isSame(dn2Norm, normalizer));
    Assert.assertFalse(dn2.isSame(dn3, normalizer));
    Assert.assertFalse(dn2.isSame(dn3Norm, normalizer));
    Assert.assertFalse(dn2.isSame(dn4, normalizer));
    Assert.assertFalse(dn2.isSame(dn4Norm, normalizer));
    Assert.assertFalse(dn2.isSame(dn5, normalizer));
    Assert.assertFalse(dn2.isSame(dn5Norm, normalizer));
    Assert.assertFalse(dn2.isSame(nullDn, normalizer));

    Assert.assertFalse(dn3.isSame(dn1, normalizer));
    Assert.assertFalse(dn3.isSame(dn1Norm, normalizer));
    Assert.assertFalse(dn3.isSame(dn2, normalizer));
    Assert.assertFalse(dn3.isSame(dn2Norm, normalizer));
    Assert.assertTrue(dn3.isSame(dn3, normalizer));
    Assert.assertTrue(dn3.isSame(dn3Norm, normalizer));
    Assert.assertFalse(dn3.isSame(dn4, normalizer));
    Assert.assertFalse(dn3.isSame(dn4Norm, normalizer));
    Assert.assertFalse(dn3.isSame(dn5, normalizer));
    Assert.assertFalse(dn3.isSame(dn5Norm, normalizer));
    Assert.assertFalse(dn3.isSame(nullDn, normalizer));

    Assert.assertFalse(dn4.isSame(dn1, normalizer));
    Assert.assertFalse(dn4.isSame(dn1Norm, normalizer));
    Assert.assertFalse(dn4.isSame(dn2, normalizer));
    Assert.assertFalse(dn4.isSame(dn2Norm, normalizer));
    Assert.assertFalse(dn4.isSame(dn3, normalizer));
    Assert.assertFalse(dn4.isSame(dn3Norm, normalizer));
    Assert.assertTrue(dn4.isSame(dn4, normalizer));
    Assert.assertTrue(dn4.isSame(dn4Norm, normalizer));
    Assert.assertFalse(dn4.isSame(dn5, normalizer));
    Assert.assertFalse(dn4.isSame(dn5Norm, normalizer));
    Assert.assertFalse(dn5.isSame(nullDn, normalizer));

    Assert.assertFalse(dn5.isSame(dn1, normalizer));
    Assert.assertFalse(dn5.isSame(dn1Norm, normalizer));
    Assert.assertFalse(dn5.isSame(dn2, normalizer));
    Assert.assertFalse(dn5.isSame(dn2Norm, normalizer));
    Assert.assertFalse(dn5.isSame(dn3, normalizer));
    Assert.assertFalse(dn5.isSame(dn3Norm, normalizer));
    Assert.assertFalse(dn5.isSame(dn4, normalizer));
    Assert.assertFalse(dn5.isSame(dn4Norm, normalizer));
    Assert.assertTrue(dn5.isSame(dn5, normalizer));
    Assert.assertTrue(dn5.isSame(dn5Norm, normalizer));
    Assert.assertFalse(dn5.isSame(nullDn, normalizer));

    Assert.assertFalse(nullDn.isSame(dn1, normalizer));
    Assert.assertFalse(nullDn.isSame(dn1Norm, normalizer));
    Assert.assertFalse(nullDn.isSame(dn2, normalizer));
    Assert.assertFalse(nullDn.isSame(dn2Norm, normalizer));
    Assert.assertFalse(nullDn.isSame(dn3, normalizer));
    Assert.assertFalse(nullDn.isSame(dn3Norm, normalizer));
    Assert.assertFalse(nullDn.isSame(dn4, normalizer));
    Assert.assertFalse(nullDn.isSame(dn4Norm, normalizer));
    Assert.assertFalse(nullDn.isSame(dn5, normalizer));
    Assert.assertFalse(nullDn.isSame(dn5Norm, normalizer));
    Assert.assertTrue(nullDn.isSame(nullDn, normalizer));
  }


  @Test
  public void testIsDescendant()
  {
    final Dn dn1 = new Dn("UID=bar+CN=foo,OU = people,DC=ldaptive,DC=org");
    final Dn dn1Norm = new Dn("cn=foo+uid=bar,ou=people,dc=ldaptive,dc=org");
    final Dn dn2 = new Dn("UID=bar,OU = people,DC=ldaptive,DC=org");
    final Dn dn2Norm = new Dn("uid=bar,ou=people,dc=ldaptive,dc=org");
    final Dn dn3 = new Dn("OU = people,DC=ldaptive,DC=org");
    final Dn dn3Norm = new Dn("ou=people,dc=ldaptive,dc=org");
    final Dn dn4 = new Dn("DC=ldaptive,DC=org");
    final Dn dn4Norm = new Dn("dc=ldaptive,dc=org");
    final Dn dn5 = new Dn("DC=org");
    final Dn dn5Norm = new Dn("dc=org");
    final Dn nullDn = new Dn();

    Assert.assertFalse(dn1.isDescendant(dn1, null));
    Assert.assertFalse(dn1.isDescendant(dn1Norm, null));
    Assert.assertFalse(dn1.isDescendant(dn2, null));
    Assert.assertFalse(dn1.isDescendant(dn2Norm, null));
    Assert.assertTrue(dn1.isDescendant(dn3, null));
    Assert.assertFalse(dn1.isDescendant(dn3Norm, null));
    Assert.assertTrue(dn1.isDescendant(dn4, null));
    Assert.assertFalse(dn1.isDescendant(dn4Norm, null));
    Assert.assertTrue(dn1.isDescendant(dn5, null));
    Assert.assertFalse(dn1.isDescendant(dn5Norm, null));
    Assert.assertTrue(dn1.isDescendant(nullDn, null));

    Assert.assertFalse(dn2.isDescendant(dn1, null));
    Assert.assertFalse(dn2.isDescendant(dn1Norm, null));
    Assert.assertFalse(dn2.isDescendant(dn2, null));
    Assert.assertFalse(dn2.isDescendant(dn2Norm, null));
    Assert.assertTrue(dn2.isDescendant(dn3, null));
    Assert.assertFalse(dn2.isDescendant(dn3Norm, null));
    Assert.assertTrue(dn2.isDescendant(dn4, null));
    Assert.assertFalse(dn2.isDescendant(dn4Norm, null));
    Assert.assertTrue(dn2.isDescendant(dn5, null));
    Assert.assertFalse(dn2.isDescendant(dn5Norm, null));
    Assert.assertTrue(dn2.isDescendant(nullDn, null));

    Assert.assertFalse(dn3.isDescendant(dn1, null));
    Assert.assertFalse(dn3.isDescendant(dn1Norm, null));
    Assert.assertFalse(dn3.isDescendant(dn2, null));
    Assert.assertFalse(dn3.isDescendant(dn2Norm, null));
    Assert.assertFalse(dn3.isDescendant(dn3, null));
    Assert.assertFalse(dn3.isDescendant(dn3Norm, null));
    Assert.assertTrue(dn3.isDescendant(dn4, null));
    Assert.assertFalse(dn3.isDescendant(dn4Norm, null));
    Assert.assertTrue(dn3.isDescendant(dn5, null));
    Assert.assertFalse(dn3.isDescendant(dn5Norm, null));
    Assert.assertTrue(dn3.isDescendant(nullDn, null));

    Assert.assertFalse(dn4.isDescendant(dn1, null));
    Assert.assertFalse(dn4.isDescendant(dn1Norm, null));
    Assert.assertFalse(dn4.isDescendant(dn2, null));
    Assert.assertFalse(dn4.isDescendant(dn2Norm, null));
    Assert.assertFalse(dn4.isDescendant(dn3, null));
    Assert.assertFalse(dn4.isDescendant(dn3Norm, null));
    Assert.assertFalse(dn4.isDescendant(dn4, null));
    Assert.assertFalse(dn4.isDescendant(dn4Norm, null));
    Assert.assertTrue(dn4.isDescendant(dn5, null));
    Assert.assertFalse(dn4.isDescendant(dn5Norm, null));
    Assert.assertTrue(dn5.isDescendant(nullDn, null));

    Assert.assertFalse(dn5.isDescendant(dn1, null));
    Assert.assertFalse(dn5.isDescendant(dn1Norm, null));
    Assert.assertFalse(dn5.isDescendant(dn2, null));
    Assert.assertFalse(dn5.isDescendant(dn2Norm, null));
    Assert.assertFalse(dn5.isDescendant(dn3, null));
    Assert.assertFalse(dn5.isDescendant(dn3Norm, null));
    Assert.assertFalse(dn5.isDescendant(dn4, null));
    Assert.assertFalse(dn5.isDescendant(dn4Norm, null));
    Assert.assertFalse(dn5.isDescendant(dn5, null));
    Assert.assertFalse(dn5.isDescendant(dn5Norm, null));
    Assert.assertTrue(dn5.isDescendant(nullDn, null));

    Assert.assertFalse(nullDn.isDescendant(dn1, null));
    Assert.assertFalse(nullDn.isDescendant(dn1Norm, null));
    Assert.assertFalse(nullDn.isDescendant(dn2, null));
    Assert.assertFalse(nullDn.isDescendant(dn2Norm, null));
    Assert.assertFalse(nullDn.isDescendant(dn3, null));
    Assert.assertFalse(nullDn.isDescendant(dn3Norm, null));
    Assert.assertFalse(nullDn.isDescendant(dn4, null));
    Assert.assertFalse(nullDn.isDescendant(dn4Norm, null));
    Assert.assertFalse(nullDn.isDescendant(dn5, null));
    Assert.assertFalse(nullDn.isDescendant(dn5Norm, null));
    Assert.assertFalse(nullDn.isDescendant(nullDn, null));
  }


  @Test
  public void testIsDescendantWithNormalizer()
  {
    final DefaultRDnNormalizer normalizer = new DefaultRDnNormalizer();
    final Dn dn1 = new Dn("UID=bar+CN=foo,OU = people,DC=ldaptive,DC=org");
    final Dn dn1Norm = new Dn("cn=foo+uid=bar,ou=people,dc=ldaptive,dc=org");
    final Dn dn2 = new Dn("UID=bar,OU = people,DC=ldaptive,DC=org");
    final Dn dn2Norm = new Dn("uid=bar,ou=people,dc=ldaptive,dc=org");
    final Dn dn3 = new Dn("OU = people,DC=ldaptive,DC=org");
    final Dn dn3Norm = new Dn("ou=people,dc=ldaptive,dc=org");
    final Dn dn4 = new Dn("DC=ldaptive,DC=org");
    final Dn dn4Norm = new Dn("dc=ldaptive,dc=org");
    final Dn dn5 = new Dn("DC=org");
    final Dn dn5Norm = new Dn("dc=org");
    final Dn nullDn = new Dn();

    Assert.assertFalse(dn1.isDescendant(dn1, normalizer));
    Assert.assertFalse(dn1.isDescendant(dn1Norm, normalizer));
    Assert.assertFalse(dn1.isDescendant(dn2, normalizer));
    Assert.assertFalse(dn1.isDescendant(dn2Norm, normalizer));
    Assert.assertTrue(dn1.isDescendant(dn3, normalizer));
    Assert.assertTrue(dn1.isDescendant(dn3Norm, normalizer));
    Assert.assertTrue(dn1.isDescendant(dn4, normalizer));
    Assert.assertTrue(dn1.isDescendant(dn4Norm, normalizer));
    Assert.assertTrue(dn1.isDescendant(dn5, normalizer));
    Assert.assertTrue(dn1.isDescendant(dn5Norm, normalizer));
    Assert.assertTrue(dn1.isDescendant(nullDn, normalizer));

    Assert.assertFalse(dn2.isDescendant(dn1, normalizer));
    Assert.assertFalse(dn2.isDescendant(dn1Norm, normalizer));
    Assert.assertFalse(dn2.isDescendant(dn2, normalizer));
    Assert.assertFalse(dn2.isDescendant(dn2Norm, normalizer));
    Assert.assertTrue(dn2.isDescendant(dn3, normalizer));
    Assert.assertTrue(dn2.isDescendant(dn3Norm, normalizer));
    Assert.assertTrue(dn2.isDescendant(dn4, normalizer));
    Assert.assertTrue(dn2.isDescendant(dn4Norm, normalizer));
    Assert.assertTrue(dn2.isDescendant(dn5, normalizer));
    Assert.assertTrue(dn2.isDescendant(dn5Norm, normalizer));
    Assert.assertTrue(dn2.isDescendant(nullDn, normalizer));

    Assert.assertFalse(dn3.isDescendant(dn1, normalizer));
    Assert.assertFalse(dn3.isDescendant(dn1Norm, normalizer));
    Assert.assertFalse(dn3.isDescendant(dn2, normalizer));
    Assert.assertFalse(dn3.isDescendant(dn2Norm, normalizer));
    Assert.assertFalse(dn3.isDescendant(dn3, normalizer));
    Assert.assertFalse(dn3.isDescendant(dn3Norm, normalizer));
    Assert.assertTrue(dn3.isDescendant(dn4, normalizer));
    Assert.assertTrue(dn3.isDescendant(dn4Norm, normalizer));
    Assert.assertTrue(dn3.isDescendant(dn5, normalizer));
    Assert.assertTrue(dn3.isDescendant(dn5Norm, normalizer));
    Assert.assertTrue(dn3.isDescendant(nullDn, normalizer));

    Assert.assertFalse(dn4.isDescendant(dn1, normalizer));
    Assert.assertFalse(dn4.isDescendant(dn1Norm, normalizer));
    Assert.assertFalse(dn4.isDescendant(dn2, normalizer));
    Assert.assertFalse(dn4.isDescendant(dn2Norm, normalizer));
    Assert.assertFalse(dn4.isDescendant(dn3, normalizer));
    Assert.assertFalse(dn4.isDescendant(dn3Norm, normalizer));
    Assert.assertFalse(dn4.isDescendant(dn4, normalizer));
    Assert.assertFalse(dn4.isDescendant(dn4Norm, normalizer));
    Assert.assertTrue(dn4.isDescendant(dn5, normalizer));
    Assert.assertTrue(dn4.isDescendant(dn5Norm, normalizer));
    Assert.assertTrue(dn5.isDescendant(nullDn, normalizer));

    Assert.assertFalse(dn5.isDescendant(dn1, normalizer));
    Assert.assertFalse(dn5.isDescendant(dn1Norm, normalizer));
    Assert.assertFalse(dn5.isDescendant(dn2, normalizer));
    Assert.assertFalse(dn5.isDescendant(dn2Norm, normalizer));
    Assert.assertFalse(dn5.isDescendant(dn3, normalizer));
    Assert.assertFalse(dn5.isDescendant(dn3Norm, normalizer));
    Assert.assertFalse(dn5.isDescendant(dn4, normalizer));
    Assert.assertFalse(dn5.isDescendant(dn4Norm, normalizer));
    Assert.assertFalse(dn5.isDescendant(dn5, normalizer));
    Assert.assertFalse(dn5.isDescendant(dn5Norm, normalizer));
    Assert.assertTrue(dn5.isDescendant(nullDn, normalizer));

    Assert.assertFalse(nullDn.isDescendant(dn1, normalizer));
    Assert.assertFalse(nullDn.isDescendant(dn1Norm, normalizer));
    Assert.assertFalse(nullDn.isDescendant(dn2, normalizer));
    Assert.assertFalse(nullDn.isDescendant(dn2Norm, normalizer));
    Assert.assertFalse(nullDn.isDescendant(dn3, normalizer));
    Assert.assertFalse(nullDn.isDescendant(dn3Norm, normalizer));
    Assert.assertFalse(nullDn.isDescendant(dn4, normalizer));
    Assert.assertFalse(nullDn.isDescendant(dn4Norm, normalizer));
    Assert.assertFalse(nullDn.isDescendant(dn5, normalizer));
    Assert.assertFalse(nullDn.isDescendant(dn5Norm, normalizer));
    Assert.assertFalse(nullDn.isDescendant(nullDn, normalizer));
  }


  @Test
  public void testIsAncestor()
  {
    final Dn dn1 = new Dn("UID=bar+CN=foo,OU = people,DC=ldaptive,DC=org");
    final Dn dn1Norm = new Dn("cn=foo+uid=bar,ou=people,dc=ldaptive,dc=org");
    final Dn dn2 = new Dn("UID=bar,OU = people,DC=ldaptive,DC=org");
    final Dn dn2Norm = new Dn("uid=bar,ou=people,dc=ldaptive,dc=org");
    final Dn dn3 = new Dn("OU = people,DC=ldaptive,DC=org");
    final Dn dn3Norm = new Dn("ou=people,dc=ldaptive,dc=org");
    final Dn dn4 = new Dn("DC=ldaptive,DC=org");
    final Dn dn4Norm = new Dn("dc=ldaptive,dc=org");
    final Dn dn5 = new Dn("DC=org");
    final Dn dn5Norm = new Dn("dc=org");
    final Dn nullDn = new Dn();

    Assert.assertFalse(dn1.isAncestor(dn1, null));
    Assert.assertFalse(dn1.isAncestor(dn1Norm, null));
    Assert.assertFalse(dn1.isAncestor(dn2, null));
    Assert.assertFalse(dn1.isAncestor(dn2Norm, null));
    Assert.assertFalse(dn1.isAncestor(dn3, null));
    Assert.assertFalse(dn1.isAncestor(dn3Norm, null));
    Assert.assertFalse(dn1.isAncestor(dn4, null));
    Assert.assertFalse(dn1.isAncestor(dn4Norm, null));
    Assert.assertFalse(dn1.isAncestor(dn5, null));
    Assert.assertFalse(dn1.isAncestor(dn5Norm, null));
    Assert.assertFalse(dn1.isAncestor(nullDn, null));

    Assert.assertFalse(dn2.isAncestor(dn1, null));
    Assert.assertFalse(dn2.isAncestor(dn1Norm, null));
    Assert.assertFalse(dn2.isAncestor(dn2, null));
    Assert.assertFalse(dn2.isAncestor(dn2Norm, null));
    Assert.assertFalse(dn2.isAncestor(dn3, null));
    Assert.assertFalse(dn2.isAncestor(dn3Norm, null));
    Assert.assertFalse(dn2.isAncestor(dn4, null));
    Assert.assertFalse(dn2.isAncestor(dn4Norm, null));
    Assert.assertFalse(dn2.isAncestor(dn5, null));
    Assert.assertFalse(dn2.isAncestor(dn5Norm, null));
    Assert.assertFalse(dn2.isAncestor(nullDn, null));

    Assert.assertTrue(dn3.isAncestor(dn1, null));
    Assert.assertFalse(dn3.isAncestor(dn1Norm, null));
    Assert.assertTrue(dn3.isAncestor(dn2, null));
    Assert.assertFalse(dn3.isAncestor(dn2Norm, null));
    Assert.assertFalse(dn3.isAncestor(dn3, null));
    Assert.assertFalse(dn3.isAncestor(dn3Norm, null));
    Assert.assertFalse(dn3.isAncestor(dn4, null));
    Assert.assertFalse(dn3.isAncestor(dn4Norm, null));
    Assert.assertFalse(dn3.isAncestor(dn5, null));
    Assert.assertFalse(dn3.isAncestor(dn5Norm, null));
    Assert.assertFalse(dn3.isAncestor(nullDn, null));

    Assert.assertTrue(dn4.isAncestor(dn1, null));
    Assert.assertFalse(dn4.isAncestor(dn1Norm, null));
    Assert.assertTrue(dn4.isAncestor(dn2, null));
    Assert.assertFalse(dn4.isAncestor(dn2Norm, null));
    Assert.assertTrue(dn4.isAncestor(dn3, null));
    Assert.assertFalse(dn4.isAncestor(dn3Norm, null));
    Assert.assertFalse(dn4.isAncestor(dn4, null));
    Assert.assertFalse(dn4.isAncestor(dn4Norm, null));
    Assert.assertFalse(dn4.isAncestor(dn5, null));
    Assert.assertFalse(dn4.isAncestor(dn5Norm, null));
    Assert.assertFalse(dn5.isAncestor(nullDn, null));

    Assert.assertTrue(dn5.isAncestor(dn1, null));
    Assert.assertFalse(dn5.isAncestor(dn1Norm, null));
    Assert.assertTrue(dn5.isAncestor(dn2, null));
    Assert.assertFalse(dn5.isAncestor(dn2Norm, null));
    Assert.assertTrue(dn5.isAncestor(dn3, null));
    Assert.assertFalse(dn5.isAncestor(dn3Norm, null));
    Assert.assertTrue(dn5.isAncestor(dn4, null));
    Assert.assertFalse(dn5.isAncestor(dn4Norm, null));
    Assert.assertFalse(dn5.isAncestor(dn5, null));
    Assert.assertFalse(dn5.isAncestor(dn5Norm, null));
    Assert.assertFalse(dn5.isAncestor(nullDn, null));

    Assert.assertTrue(nullDn.isAncestor(dn1, null));
    Assert.assertTrue(nullDn.isAncestor(dn1Norm, null));
    Assert.assertTrue(nullDn.isAncestor(dn2, null));
    Assert.assertTrue(nullDn.isAncestor(dn2Norm, null));
    Assert.assertTrue(nullDn.isAncestor(dn3, null));
    Assert.assertTrue(nullDn.isAncestor(dn3Norm, null));
    Assert.assertTrue(nullDn.isAncestor(dn4, null));
    Assert.assertTrue(nullDn.isAncestor(dn4Norm, null));
    Assert.assertTrue(nullDn.isAncestor(dn5, null));
    Assert.assertTrue(nullDn.isAncestor(dn5Norm, null));
    Assert.assertFalse(nullDn.isAncestor(nullDn, null));
  }


  @Test
  public void testIsAncestorWithNormalizer()
  {
    final DefaultRDnNormalizer normalizer = new DefaultRDnNormalizer();
    final Dn dn1 = new Dn("UID=bar+CN=foo,OU = people,DC=ldaptive,DC=org");
    final Dn dn1Norm = new Dn("cn=foo+uid=bar,ou=people,dc=ldaptive,dc=org");
    final Dn dn2 = new Dn("UID=bar,OU = people,DC=ldaptive,DC=org");
    final Dn dn2Norm = new Dn("uid=bar,ou=people,dc=ldaptive,dc=org");
    final Dn dn3 = new Dn("OU = people,DC=ldaptive,DC=org");
    final Dn dn3Norm = new Dn("ou=people,dc=ldaptive,dc=org");
    final Dn dn4 = new Dn("DC=ldaptive,DC=org");
    final Dn dn4Norm = new Dn("dc=ldaptive,dc=org");
    final Dn dn5 = new Dn("DC=org");
    final Dn dn5Norm = new Dn("dc=org");
    final Dn nullDn = new Dn();

    Assert.assertFalse(dn1.isAncestor(dn1, normalizer));
    Assert.assertFalse(dn1.isAncestor(dn1Norm, normalizer));
    Assert.assertFalse(dn1.isAncestor(dn2, normalizer));
    Assert.assertFalse(dn1.isAncestor(dn2Norm, normalizer));
    Assert.assertFalse(dn1.isAncestor(dn3, normalizer));
    Assert.assertFalse(dn1.isAncestor(dn3Norm, normalizer));
    Assert.assertFalse(dn1.isAncestor(dn4, normalizer));
    Assert.assertFalse(dn1.isAncestor(dn4Norm, normalizer));
    Assert.assertFalse(dn1.isAncestor(dn5, normalizer));
    Assert.assertFalse(dn1.isAncestor(dn5Norm, normalizer));
    Assert.assertFalse(dn1.isAncestor(nullDn, normalizer));

    Assert.assertFalse(dn2.isAncestor(dn1, normalizer));
    Assert.assertFalse(dn2.isAncestor(dn1Norm, normalizer));
    Assert.assertFalse(dn2.isAncestor(dn2, normalizer));
    Assert.assertFalse(dn2.isAncestor(dn2Norm, normalizer));
    Assert.assertFalse(dn2.isAncestor(dn3, normalizer));
    Assert.assertFalse(dn2.isAncestor(dn3Norm, normalizer));
    Assert.assertFalse(dn2.isAncestor(dn4, normalizer));
    Assert.assertFalse(dn2.isAncestor(dn4Norm, normalizer));
    Assert.assertFalse(dn2.isAncestor(dn5, normalizer));
    Assert.assertFalse(dn2.isAncestor(dn5Norm, normalizer));
    Assert.assertFalse(dn2.isAncestor(nullDn, normalizer));

    Assert.assertTrue(dn3.isAncestor(dn1, normalizer));
    Assert.assertTrue(dn3.isAncestor(dn1Norm, normalizer));
    Assert.assertTrue(dn3.isAncestor(dn2, normalizer));
    Assert.assertTrue(dn3.isAncestor(dn2Norm, normalizer));
    Assert.assertFalse(dn3.isAncestor(dn3, normalizer));
    Assert.assertFalse(dn3.isAncestor(dn3Norm, normalizer));
    Assert.assertFalse(dn3.isAncestor(dn4, normalizer));
    Assert.assertFalse(dn3.isAncestor(dn4Norm, normalizer));
    Assert.assertFalse(dn3.isAncestor(dn5, normalizer));
    Assert.assertFalse(dn3.isAncestor(dn5Norm, normalizer));
    Assert.assertFalse(dn3.isAncestor(nullDn, normalizer));

    Assert.assertTrue(dn4.isAncestor(dn1, normalizer));
    Assert.assertTrue(dn4.isAncestor(dn1Norm, normalizer));
    Assert.assertTrue(dn4.isAncestor(dn2, normalizer));
    Assert.assertTrue(dn4.isAncestor(dn2Norm, normalizer));
    Assert.assertTrue(dn4.isAncestor(dn3, normalizer));
    Assert.assertTrue(dn4.isAncestor(dn3Norm, normalizer));
    Assert.assertFalse(dn4.isAncestor(dn4, normalizer));
    Assert.assertFalse(dn4.isAncestor(dn4Norm, normalizer));
    Assert.assertFalse(dn4.isAncestor(dn5, normalizer));
    Assert.assertFalse(dn4.isAncestor(dn5Norm, normalizer));
    Assert.assertFalse(dn5.isAncestor(nullDn, normalizer));

    Assert.assertTrue(dn5.isAncestor(dn1, normalizer));
    Assert.assertTrue(dn5.isAncestor(dn1Norm, normalizer));
    Assert.assertTrue(dn5.isAncestor(dn2, normalizer));
    Assert.assertTrue(dn5.isAncestor(dn2Norm, normalizer));
    Assert.assertTrue(dn5.isAncestor(dn3, normalizer));
    Assert.assertTrue(dn5.isAncestor(dn3Norm, normalizer));
    Assert.assertTrue(dn5.isAncestor(dn4, normalizer));
    Assert.assertTrue(dn5.isAncestor(dn4Norm, normalizer));
    Assert.assertFalse(dn5.isAncestor(dn5, normalizer));
    Assert.assertFalse(dn5.isAncestor(dn5Norm, normalizer));
    Assert.assertFalse(dn5.isAncestor(nullDn, normalizer));

    Assert.assertTrue(nullDn.isAncestor(dn1, normalizer));
    Assert.assertTrue(nullDn.isAncestor(dn1Norm, normalizer));
    Assert.assertTrue(nullDn.isAncestor(dn2, normalizer));
    Assert.assertTrue(nullDn.isAncestor(dn2Norm, normalizer));
    Assert.assertTrue(nullDn.isAncestor(dn3, normalizer));
    Assert.assertTrue(nullDn.isAncestor(dn3Norm, normalizer));
    Assert.assertTrue(nullDn.isAncestor(dn4, normalizer));
    Assert.assertTrue(nullDn.isAncestor(dn4Norm, normalizer));
    Assert.assertTrue(nullDn.isAncestor(dn5, normalizer));
    Assert.assertTrue(nullDn.isAncestor(dn5Norm, normalizer));
    Assert.assertFalse(nullDn.isAncestor(nullDn, normalizer));
  }


  /** Test for copy method. */
  @Test
  public void copy()
  {
    final Dn dn1 = new Dn("uid=1,ou=people,dc=ldaptive,dc=org");
    final Dn cp1 = Dn.copy(dn1);
    Assert.assertEquals(cp1, dn1);
    Assert.assertFalse(dn1.isFrozen());
    Assert.assertFalse(cp1.isFrozen());

    final Dn dn2 = Dn.builder()
      .add("uid=1,ou=people,dc=ldaptive,dc=org")
      .freeze()
      .build();
    final Dn cp2 = Dn.copy(dn2);
    Assert.assertEquals(cp2, dn2);
    Assert.assertTrue(dn2.isFrozen());
    Assert.assertFalse(cp2.isFrozen());
  }
}
