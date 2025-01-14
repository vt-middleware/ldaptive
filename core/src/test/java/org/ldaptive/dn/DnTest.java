/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

// CheckStyle:AvoidStaticImport OFF
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;
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
    assertThat(dn).isEqualTo(match);
    for (RDnNormalizer normalizer : normalizers) {
      assertThat(dn.format(normalizer)).withFailMessage("Normalizer: %s", normalizer).isEqualTo(normalized);
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
      fail("Should have thrown IllegalArgumentException for " + parse);
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
  }

  @Test
  public void testSubDn()
  {
    Dn dn = new Dn("uid=alice,ou=people,dc=ldaptive,dc=org");
    dn = dn.subDn(1);
    assertThat(dn.format()).isEqualTo("ou=people,dc=ldaptive,dc=org");
    dn = dn.subDn(1);
    assertThat(dn.format()).isEqualTo("dc=ldaptive,dc=org");
    dn = dn.subDn(1);
    assertThat(dn.format()).isEqualTo("dc=org");
    dn = dn.subDn(1);
    assertThat(dn.format()).isEqualTo("");
    assertThat(dn.isEmpty()).isTrue();
    dn = dn.subDn(1);
    assertThat(dn).isNull();
  }


  @Test
  public void testGetParent()
  {
    final Dn dn = new Dn("uid=alice,ou=people,dc=ldaptive,dc=org");
    assertThat(dn.getParent().format()).isEqualTo("ou=people,dc=ldaptive,dc=org");
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

    assertThat(dn1.isSame(dn1, null)).isTrue();
    assertThat(dn1.isSame(dn1Norm, null)).isFalse();
    assertThat(dn1.isSame(dn2, null)).isFalse();
    assertThat(dn1.isSame(dn2Norm, null)).isFalse();
    assertThat(dn1.isSame(dn3, null)).isFalse();
    assertThat(dn1.isSame(dn3Norm, null)).isFalse();
    assertThat(dn1.isSame(dn4, null)).isFalse();
    assertThat(dn1.isSame(dn4Norm, null)).isFalse();
    assertThat(dn1.isSame(dn5, null)).isFalse();
    assertThat(dn1.isSame(dn5Norm, null)).isFalse();
    assertThat(dn1.isSame(nullDn, null)).isFalse();

    assertThat(dn2.isSame(dn1, null)).isFalse();
    assertThat(dn2.isSame(dn1Norm, null)).isFalse();
    assertThat(dn2.isSame(dn2, null)).isTrue();
    assertThat(dn2.isSame(dn2Norm, null)).isFalse();
    assertThat(dn2.isSame(dn3, null)).isFalse();
    assertThat(dn2.isSame(dn3Norm, null)).isFalse();
    assertThat(dn2.isSame(dn4, null)).isFalse();
    assertThat(dn2.isSame(dn4Norm, null)).isFalse();
    assertThat(dn2.isSame(dn5, null)).isFalse();
    assertThat(dn2.isSame(dn5Norm, null)).isFalse();
    assertThat(dn2.isSame(nullDn, null)).isFalse();

    assertThat(dn3.isSame(dn1, null)).isFalse();
    assertThat(dn3.isSame(dn1Norm, null)).isFalse();
    assertThat(dn3.isSame(dn2, null)).isFalse();
    assertThat(dn3.isSame(dn2Norm, null)).isFalse();
    assertThat(dn3.isSame(dn3, null)).isTrue();
    assertThat(dn3.isSame(dn3Norm, null)).isFalse();
    assertThat(dn3.isSame(dn4, null)).isFalse();
    assertThat(dn3.isSame(dn4Norm, null)).isFalse();
    assertThat(dn3.isSame(dn5, null)).isFalse();
    assertThat(dn3.isSame(dn5Norm, null)).isFalse();
    assertThat(dn3.isSame(nullDn, null)).isFalse();

    assertThat(dn4.isSame(dn1, null)).isFalse();
    assertThat(dn4.isSame(dn1Norm, null)).isFalse();
    assertThat(dn4.isSame(dn2, null)).isFalse();
    assertThat(dn4.isSame(dn2Norm, null)).isFalse();
    assertThat(dn4.isSame(dn3, null)).isFalse();
    assertThat(dn4.isSame(dn3Norm, null)).isFalse();
    assertThat(dn4.isSame(dn4, null)).isTrue();
    assertThat(dn4.isSame(dn4Norm, null)).isFalse();
    assertThat(dn4.isSame(dn5, null)).isFalse();
    assertThat(dn4.isSame(dn5Norm, null)).isFalse();
    assertThat(dn5.isSame(nullDn, null)).isFalse();

    assertThat(dn5.isSame(dn1, null)).isFalse();
    assertThat(dn5.isSame(dn1Norm, null)).isFalse();
    assertThat(dn5.isSame(dn2, null)).isFalse();
    assertThat(dn5.isSame(dn2Norm, null)).isFalse();
    assertThat(dn5.isSame(dn3, null)).isFalse();
    assertThat(dn5.isSame(dn3Norm, null)).isFalse();
    assertThat(dn5.isSame(dn4, null)).isFalse();
    assertThat(dn5.isSame(dn4Norm, null)).isFalse();
    assertThat(dn5.isSame(dn5, null)).isTrue();
    assertThat(dn5.isSame(dn5Norm, null)).isFalse();
    assertThat(dn5.isSame(nullDn, null)).isFalse();

    assertThat(nullDn.isSame(dn1, null)).isFalse();
    assertThat(nullDn.isSame(dn1Norm, null)).isFalse();
    assertThat(nullDn.isSame(dn2, null)).isFalse();
    assertThat(nullDn.isSame(dn2Norm, null)).isFalse();
    assertThat(nullDn.isSame(dn3, null)).isFalse();
    assertThat(nullDn.isSame(dn3Norm, null)).isFalse();
    assertThat(nullDn.isSame(dn4, null)).isFalse();
    assertThat(nullDn.isSame(dn4Norm, null)).isFalse();
    assertThat(nullDn.isSame(dn5, null)).isFalse();
    assertThat(nullDn.isSame(dn5Norm, null)).isFalse();
    assertThat(nullDn.isSame(nullDn, null)).isTrue();
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

    assertThat(dn1.isSame(dn1, normalizer)).isTrue();
    assertThat(dn1.isSame(dn1Norm, normalizer)).isTrue();
    assertThat(dn1.isSame(dn2, normalizer)).isFalse();
    assertThat(dn1.isSame(dn2Norm, normalizer)).isFalse();
    assertThat(dn1.isSame(dn3, normalizer)).isFalse();
    assertThat(dn1.isSame(dn3Norm, normalizer)).isFalse();
    assertThat(dn1.isSame(dn4, normalizer)).isFalse();
    assertThat(dn1.isSame(dn4Norm, normalizer)).isFalse();
    assertThat(dn1.isSame(dn5, normalizer)).isFalse();
    assertThat(dn1.isSame(dn5Norm, normalizer)).isFalse();
    assertThat(dn1.isSame(nullDn, normalizer)).isFalse();

    assertThat(dn2.isSame(dn1, normalizer)).isFalse();
    assertThat(dn2.isSame(dn1Norm, normalizer)).isFalse();
    assertThat(dn2.isSame(dn2, normalizer)).isTrue();
    assertThat(dn2.isSame(dn2Norm, normalizer)).isTrue();
    assertThat(dn2.isSame(dn3, normalizer)).isFalse();
    assertThat(dn2.isSame(dn3Norm, normalizer)).isFalse();
    assertThat(dn2.isSame(dn4, normalizer)).isFalse();
    assertThat(dn2.isSame(dn4Norm, normalizer)).isFalse();
    assertThat(dn2.isSame(dn5, normalizer)).isFalse();
    assertThat(dn2.isSame(dn5Norm, normalizer)).isFalse();
    assertThat(dn2.isSame(nullDn, normalizer)).isFalse();

    assertThat(dn3.isSame(dn1, normalizer)).isFalse();
    assertThat(dn3.isSame(dn1Norm, normalizer)).isFalse();
    assertThat(dn3.isSame(dn2, normalizer)).isFalse();
    assertThat(dn3.isSame(dn2Norm, normalizer)).isFalse();
    assertThat(dn3.isSame(dn3, normalizer)).isTrue();
    assertThat(dn3.isSame(dn3Norm, normalizer)).isTrue();
    assertThat(dn3.isSame(dn4, normalizer)).isFalse();
    assertThat(dn3.isSame(dn4Norm, normalizer)).isFalse();
    assertThat(dn3.isSame(dn5, normalizer)).isFalse();
    assertThat(dn3.isSame(dn5Norm, normalizer)).isFalse();
    assertThat(dn3.isSame(nullDn, normalizer)).isFalse();

    assertThat(dn4.isSame(dn1, normalizer)).isFalse();
    assertThat(dn4.isSame(dn1Norm, normalizer)).isFalse();
    assertThat(dn4.isSame(dn2, normalizer)).isFalse();
    assertThat(dn4.isSame(dn2Norm, normalizer)).isFalse();
    assertThat(dn4.isSame(dn3, normalizer)).isFalse();
    assertThat(dn4.isSame(dn3Norm, normalizer)).isFalse();
    assertThat(dn4.isSame(dn4, normalizer)).isTrue();
    assertThat(dn4.isSame(dn4Norm, normalizer)).isTrue();
    assertThat(dn4.isSame(dn5, normalizer)).isFalse();
    assertThat(dn4.isSame(dn5Norm, normalizer)).isFalse();
    assertThat(dn5.isSame(nullDn, normalizer)).isFalse();

    assertThat(dn5.isSame(dn1, normalizer)).isFalse();
    assertThat(dn5.isSame(dn1Norm, normalizer)).isFalse();
    assertThat(dn5.isSame(dn2, normalizer)).isFalse();
    assertThat(dn5.isSame(dn2Norm, normalizer)).isFalse();
    assertThat(dn5.isSame(dn3, normalizer)).isFalse();
    assertThat(dn5.isSame(dn3Norm, normalizer)).isFalse();
    assertThat(dn5.isSame(dn4, normalizer)).isFalse();
    assertThat(dn5.isSame(dn4Norm, normalizer)).isFalse();
    assertThat(dn5.isSame(dn5, normalizer)).isTrue();
    assertThat(dn5.isSame(dn5Norm, normalizer)).isTrue();
    assertThat(dn5.isSame(nullDn, normalizer)).isFalse();

    assertThat(nullDn.isSame(dn1, normalizer)).isFalse();
    assertThat(nullDn.isSame(dn1Norm, normalizer)).isFalse();
    assertThat(nullDn.isSame(dn2, normalizer)).isFalse();
    assertThat(nullDn.isSame(dn2Norm, normalizer)).isFalse();
    assertThat(nullDn.isSame(dn3, normalizer)).isFalse();
    assertThat(nullDn.isSame(dn3Norm, normalizer)).isFalse();
    assertThat(nullDn.isSame(dn4, normalizer)).isFalse();
    assertThat(nullDn.isSame(dn4Norm, normalizer)).isFalse();
    assertThat(nullDn.isSame(dn5, normalizer)).isFalse();
    assertThat(nullDn.isSame(dn5Norm, normalizer)).isFalse();
    assertThat(nullDn.isSame(nullDn, normalizer)).isTrue();
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

    assertThat(dn1.isDescendant(dn1, null)).isFalse();
    assertThat(dn1.isDescendant(dn1Norm, null)).isFalse();
    assertThat(dn1.isDescendant(dn2, null)).isFalse();
    assertThat(dn1.isDescendant(dn2Norm, null)).isFalse();
    assertThat(dn1.isDescendant(dn3, null)).isTrue();
    assertThat(dn1.isDescendant(dn3Norm, null)).isFalse();
    assertThat(dn1.isDescendant(dn4, null)).isTrue();
    assertThat(dn1.isDescendant(dn4Norm, null)).isFalse();
    assertThat(dn1.isDescendant(dn5, null)).isTrue();
    assertThat(dn1.isDescendant(dn5Norm, null)).isFalse();
    assertThat(dn1.isDescendant(nullDn, null)).isTrue();

    assertThat(dn2.isDescendant(dn1, null)).isFalse();
    assertThat(dn2.isDescendant(dn1Norm, null)).isFalse();
    assertThat(dn2.isDescendant(dn2, null)).isFalse();
    assertThat(dn2.isDescendant(dn2Norm, null)).isFalse();
    assertThat(dn2.isDescendant(dn3, null)).isTrue();
    assertThat(dn2.isDescendant(dn3Norm, null)).isFalse();
    assertThat(dn2.isDescendant(dn4, null)).isTrue();
    assertThat(dn2.isDescendant(dn4Norm, null)).isFalse();
    assertThat(dn2.isDescendant(dn5, null)).isTrue();
    assertThat(dn2.isDescendant(dn5Norm, null)).isFalse();
    assertThat(dn2.isDescendant(nullDn, null)).isTrue();

    assertThat(dn3.isDescendant(dn1, null)).isFalse();
    assertThat(dn3.isDescendant(dn1Norm, null)).isFalse();
    assertThat(dn3.isDescendant(dn2, null)).isFalse();
    assertThat(dn3.isDescendant(dn2Norm, null)).isFalse();
    assertThat(dn3.isDescendant(dn3, null)).isFalse();
    assertThat(dn3.isDescendant(dn3Norm, null)).isFalse();
    assertThat(dn3.isDescendant(dn4, null)).isTrue();
    assertThat(dn3.isDescendant(dn4Norm, null)).isFalse();
    assertThat(dn3.isDescendant(dn5, null)).isTrue();
    assertThat(dn3.isDescendant(dn5Norm, null)).isFalse();
    assertThat(dn3.isDescendant(nullDn, null)).isTrue();

    assertThat(dn4.isDescendant(dn1, null)).isFalse();
    assertThat(dn4.isDescendant(dn1Norm, null)).isFalse();
    assertThat(dn4.isDescendant(dn2, null)).isFalse();
    assertThat(dn4.isDescendant(dn2Norm, null)).isFalse();
    assertThat(dn4.isDescendant(dn3, null)).isFalse();
    assertThat(dn4.isDescendant(dn3Norm, null)).isFalse();
    assertThat(dn4.isDescendant(dn4, null)).isFalse();
    assertThat(dn4.isDescendant(dn4Norm, null)).isFalse();
    assertThat(dn4.isDescendant(dn5, null)).isTrue();
    assertThat(dn4.isDescendant(dn5Norm, null)).isFalse();
    assertThat(dn5.isDescendant(nullDn, null)).isTrue();

    assertThat(dn5.isDescendant(dn1, null)).isFalse();
    assertThat(dn5.isDescendant(dn1Norm, null)).isFalse();
    assertThat(dn5.isDescendant(dn2, null)).isFalse();
    assertThat(dn5.isDescendant(dn2Norm, null)).isFalse();
    assertThat(dn5.isDescendant(dn3, null)).isFalse();
    assertThat(dn5.isDescendant(dn3Norm, null)).isFalse();
    assertThat(dn5.isDescendant(dn4, null)).isFalse();
    assertThat(dn5.isDescendant(dn4Norm, null)).isFalse();
    assertThat(dn5.isDescendant(dn5, null)).isFalse();
    assertThat(dn5.isDescendant(dn5Norm, null)).isFalse();
    assertThat(dn5.isDescendant(nullDn, null)).isTrue();

    assertThat(nullDn.isDescendant(dn1, null)).isFalse();
    assertThat(nullDn.isDescendant(dn1Norm, null)).isFalse();
    assertThat(nullDn.isDescendant(dn2, null)).isFalse();
    assertThat(nullDn.isDescendant(dn2Norm, null)).isFalse();
    assertThat(nullDn.isDescendant(dn3, null)).isFalse();
    assertThat(nullDn.isDescendant(dn3Norm, null)).isFalse();
    assertThat(nullDn.isDescendant(dn4, null)).isFalse();
    assertThat(nullDn.isDescendant(dn4Norm, null)).isFalse();
    assertThat(nullDn.isDescendant(dn5, null)).isFalse();
    assertThat(nullDn.isDescendant(dn5Norm, null)).isFalse();
    assertThat(nullDn.isDescendant(nullDn, null)).isFalse();
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

    assertThat(dn1.isDescendant(dn1, normalizer)).isFalse();
    assertThat(dn1.isDescendant(dn1Norm, normalizer)).isFalse();
    assertThat(dn1.isDescendant(dn2, normalizer)).isFalse();
    assertThat(dn1.isDescendant(dn2Norm, normalizer)).isFalse();
    assertThat(dn1.isDescendant(dn3, normalizer)).isTrue();
    assertThat(dn1.isDescendant(dn3Norm, normalizer)).isTrue();
    assertThat(dn1.isDescendant(dn4, normalizer)).isTrue();
    assertThat(dn1.isDescendant(dn4Norm, normalizer)).isTrue();
    assertThat(dn1.isDescendant(dn5, normalizer)).isTrue();
    assertThat(dn1.isDescendant(dn5Norm, normalizer)).isTrue();
    assertThat(dn1.isDescendant(nullDn, normalizer)).isTrue();

    assertThat(dn2.isDescendant(dn1, normalizer)).isFalse();
    assertThat(dn2.isDescendant(dn1Norm, normalizer)).isFalse();
    assertThat(dn2.isDescendant(dn2, normalizer)).isFalse();
    assertThat(dn2.isDescendant(dn2Norm, normalizer)).isFalse();
    assertThat(dn2.isDescendant(dn3, normalizer)).isTrue();
    assertThat(dn2.isDescendant(dn3Norm, normalizer)).isTrue();
    assertThat(dn2.isDescendant(dn4, normalizer)).isTrue();
    assertThat(dn2.isDescendant(dn4Norm, normalizer)).isTrue();
    assertThat(dn2.isDescendant(dn5, normalizer)).isTrue();
    assertThat(dn2.isDescendant(dn5Norm, normalizer)).isTrue();
    assertThat(dn2.isDescendant(nullDn, normalizer)).isTrue();

    assertThat(dn3.isDescendant(dn1, normalizer)).isFalse();
    assertThat(dn3.isDescendant(dn1Norm, normalizer)).isFalse();
    assertThat(dn3.isDescendant(dn2, normalizer)).isFalse();
    assertThat(dn3.isDescendant(dn2Norm, normalizer)).isFalse();
    assertThat(dn3.isDescendant(dn3, normalizer)).isFalse();
    assertThat(dn3.isDescendant(dn3Norm, normalizer)).isFalse();
    assertThat(dn3.isDescendant(dn4, normalizer)).isTrue();
    assertThat(dn3.isDescendant(dn4Norm, normalizer)).isTrue();
    assertThat(dn3.isDescendant(dn5, normalizer)).isTrue();
    assertThat(dn3.isDescendant(dn5Norm, normalizer)).isTrue();
    assertThat(dn3.isDescendant(nullDn, normalizer)).isTrue();

    assertThat(dn4.isDescendant(dn1, normalizer)).isFalse();
    assertThat(dn4.isDescendant(dn1Norm, normalizer)).isFalse();
    assertThat(dn4.isDescendant(dn2, normalizer)).isFalse();
    assertThat(dn4.isDescendant(dn2Norm, normalizer)).isFalse();
    assertThat(dn4.isDescendant(dn3, normalizer)).isFalse();
    assertThat(dn4.isDescendant(dn3Norm, normalizer)).isFalse();
    assertThat(dn4.isDescendant(dn4, normalizer)).isFalse();
    assertThat(dn4.isDescendant(dn4Norm, normalizer)).isFalse();
    assertThat(dn4.isDescendant(dn5, normalizer)).isTrue();
    assertThat(dn4.isDescendant(dn5Norm, normalizer)).isTrue();
    assertThat(dn5.isDescendant(nullDn, normalizer)).isTrue();

    assertThat(dn5.isDescendant(dn1, normalizer)).isFalse();
    assertThat(dn5.isDescendant(dn1Norm, normalizer)).isFalse();
    assertThat(dn5.isDescendant(dn2, normalizer)).isFalse();
    assertThat(dn5.isDescendant(dn2Norm, normalizer)).isFalse();
    assertThat(dn5.isDescendant(dn3, normalizer)).isFalse();
    assertThat(dn5.isDescendant(dn3Norm, normalizer)).isFalse();
    assertThat(dn5.isDescendant(dn4, normalizer)).isFalse();
    assertThat(dn5.isDescendant(dn4Norm, normalizer)).isFalse();
    assertThat(dn5.isDescendant(dn5, normalizer)).isFalse();
    assertThat(dn5.isDescendant(dn5Norm, normalizer)).isFalse();
    assertThat(dn5.isDescendant(nullDn, normalizer)).isTrue();

    assertThat(nullDn.isDescendant(dn1, normalizer)).isFalse();
    assertThat(nullDn.isDescendant(dn1Norm, normalizer)).isFalse();
    assertThat(nullDn.isDescendant(dn2, normalizer)).isFalse();
    assertThat(nullDn.isDescendant(dn2Norm, normalizer)).isFalse();
    assertThat(nullDn.isDescendant(dn3, normalizer)).isFalse();
    assertThat(nullDn.isDescendant(dn3Norm, normalizer)).isFalse();
    assertThat(nullDn.isDescendant(dn4, normalizer)).isFalse();
    assertThat(nullDn.isDescendant(dn4Norm, normalizer)).isFalse();
    assertThat(nullDn.isDescendant(dn5, normalizer)).isFalse();
    assertThat(nullDn.isDescendant(dn5Norm, normalizer)).isFalse();
    assertThat(nullDn.isDescendant(nullDn, normalizer)).isFalse();
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

    assertThat(dn1.isAncestor(dn1, null)).isFalse();
    assertThat(dn1.isAncestor(dn1Norm, null)).isFalse();
    assertThat(dn1.isAncestor(dn2, null)).isFalse();
    assertThat(dn1.isAncestor(dn2Norm, null)).isFalse();
    assertThat(dn1.isAncestor(dn3, null)).isFalse();
    assertThat(dn1.isAncestor(dn3Norm, null)).isFalse();
    assertThat(dn1.isAncestor(dn4, null)).isFalse();
    assertThat(dn1.isAncestor(dn4Norm, null)).isFalse();
    assertThat(dn1.isAncestor(dn5, null)).isFalse();
    assertThat(dn1.isAncestor(dn5Norm, null)).isFalse();
    assertThat(dn1.isAncestor(nullDn, null)).isFalse();

    assertThat(dn2.isAncestor(dn1, null)).isFalse();
    assertThat(dn2.isAncestor(dn1Norm, null)).isFalse();
    assertThat(dn2.isAncestor(dn2, null)).isFalse();
    assertThat(dn2.isAncestor(dn2Norm, null)).isFalse();
    assertThat(dn2.isAncestor(dn3, null)).isFalse();
    assertThat(dn2.isAncestor(dn3Norm, null)).isFalse();
    assertThat(dn2.isAncestor(dn4, null)).isFalse();
    assertThat(dn2.isAncestor(dn4Norm, null)).isFalse();
    assertThat(dn2.isAncestor(dn5, null)).isFalse();
    assertThat(dn2.isAncestor(dn5Norm, null)).isFalse();
    assertThat(dn2.isAncestor(nullDn, null)).isFalse();

    assertThat(dn3.isAncestor(dn1, null)).isTrue();
    assertThat(dn3.isAncestor(dn1Norm, null)).isFalse();
    assertThat(dn3.isAncestor(dn2, null)).isTrue();
    assertThat(dn3.isAncestor(dn2Norm, null)).isFalse();
    assertThat(dn3.isAncestor(dn3, null)).isFalse();
    assertThat(dn3.isAncestor(dn3Norm, null)).isFalse();
    assertThat(dn3.isAncestor(dn4, null)).isFalse();
    assertThat(dn3.isAncestor(dn4Norm, null)).isFalse();
    assertThat(dn3.isAncestor(dn5, null)).isFalse();
    assertThat(dn3.isAncestor(dn5Norm, null)).isFalse();
    assertThat(dn3.isAncestor(nullDn, null)).isFalse();

    assertThat(dn4.isAncestor(dn1, null)).isTrue();
    assertThat(dn4.isAncestor(dn1Norm, null)).isFalse();
    assertThat(dn4.isAncestor(dn2, null)).isTrue();
    assertThat(dn4.isAncestor(dn2Norm, null)).isFalse();
    assertThat(dn4.isAncestor(dn3, null)).isTrue();
    assertThat(dn4.isAncestor(dn3Norm, null)).isFalse();
    assertThat(dn4.isAncestor(dn4, null)).isFalse();
    assertThat(dn4.isAncestor(dn4Norm, null)).isFalse();
    assertThat(dn4.isAncestor(dn5, null)).isFalse();
    assertThat(dn4.isAncestor(dn5Norm, null)).isFalse();
    assertThat(dn5.isAncestor(nullDn, null)).isFalse();

    assertThat(dn5.isAncestor(dn1, null)).isTrue();
    assertThat(dn5.isAncestor(dn1Norm, null)).isFalse();
    assertThat(dn5.isAncestor(dn2, null)).isTrue();
    assertThat(dn5.isAncestor(dn2Norm, null)).isFalse();
    assertThat(dn5.isAncestor(dn3, null)).isTrue();
    assertThat(dn5.isAncestor(dn3Norm, null)).isFalse();
    assertThat(dn5.isAncestor(dn4, null)).isTrue();
    assertThat(dn5.isAncestor(dn4Norm, null)).isFalse();
    assertThat(dn5.isAncestor(dn5, null)).isFalse();
    assertThat(dn5.isAncestor(dn5Norm, null)).isFalse();
    assertThat(dn5.isAncestor(nullDn, null)).isFalse();

    assertThat(nullDn.isAncestor(dn1, null)).isTrue();
    assertThat(nullDn.isAncestor(dn1Norm, null)).isTrue();
    assertThat(nullDn.isAncestor(dn2, null)).isTrue();
    assertThat(nullDn.isAncestor(dn2Norm, null)).isTrue();
    assertThat(nullDn.isAncestor(dn3, null)).isTrue();
    assertThat(nullDn.isAncestor(dn3Norm, null)).isTrue();
    assertThat(nullDn.isAncestor(dn4, null)).isTrue();
    assertThat(nullDn.isAncestor(dn4Norm, null)).isTrue();
    assertThat(nullDn.isAncestor(dn5, null)).isTrue();
    assertThat(nullDn.isAncestor(dn5Norm, null)).isTrue();
    assertThat(nullDn.isAncestor(nullDn, null)).isFalse();
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

    assertThat(dn1.isAncestor(dn1, normalizer)).isFalse();
    assertThat(dn1.isAncestor(dn1Norm, normalizer)).isFalse();
    assertThat(dn1.isAncestor(dn2, normalizer)).isFalse();
    assertThat(dn1.isAncestor(dn2Norm, normalizer)).isFalse();
    assertThat(dn1.isAncestor(dn3, normalizer)).isFalse();
    assertThat(dn1.isAncestor(dn3Norm, normalizer)).isFalse();
    assertThat(dn1.isAncestor(dn4, normalizer)).isFalse();
    assertThat(dn1.isAncestor(dn4Norm, normalizer)).isFalse();
    assertThat(dn1.isAncestor(dn5, normalizer)).isFalse();
    assertThat(dn1.isAncestor(dn5Norm, normalizer)).isFalse();
    assertThat(dn1.isAncestor(nullDn, normalizer)).isFalse();

    assertThat(dn2.isAncestor(dn1, normalizer)).isFalse();
    assertThat(dn2.isAncestor(dn1Norm, normalizer)).isFalse();
    assertThat(dn2.isAncestor(dn2, normalizer)).isFalse();
    assertThat(dn2.isAncestor(dn2Norm, normalizer)).isFalse();
    assertThat(dn2.isAncestor(dn3, normalizer)).isFalse();
    assertThat(dn2.isAncestor(dn3Norm, normalizer)).isFalse();
    assertThat(dn2.isAncestor(dn4, normalizer)).isFalse();
    assertThat(dn2.isAncestor(dn4Norm, normalizer)).isFalse();
    assertThat(dn2.isAncestor(dn5, normalizer)).isFalse();
    assertThat(dn2.isAncestor(dn5Norm, normalizer)).isFalse();
    assertThat(dn2.isAncestor(nullDn, normalizer)).isFalse();

    assertThat(dn3.isAncestor(dn1, normalizer)).isTrue();
    assertThat(dn3.isAncestor(dn1Norm, normalizer)).isTrue();
    assertThat(dn3.isAncestor(dn2, normalizer)).isTrue();
    assertThat(dn3.isAncestor(dn2Norm, normalizer)).isTrue();
    assertThat(dn3.isAncestor(dn3, normalizer)).isFalse();
    assertThat(dn3.isAncestor(dn3Norm, normalizer)).isFalse();
    assertThat(dn3.isAncestor(dn4, normalizer)).isFalse();
    assertThat(dn3.isAncestor(dn4Norm, normalizer)).isFalse();
    assertThat(dn3.isAncestor(dn5, normalizer)).isFalse();
    assertThat(dn3.isAncestor(dn5Norm, normalizer)).isFalse();
    assertThat(dn3.isAncestor(nullDn, normalizer)).isFalse();

    assertThat(dn4.isAncestor(dn1, normalizer)).isTrue();
    assertThat(dn4.isAncestor(dn1Norm, normalizer)).isTrue();
    assertThat(dn4.isAncestor(dn2, normalizer)).isTrue();
    assertThat(dn4.isAncestor(dn2Norm, normalizer)).isTrue();
    assertThat(dn4.isAncestor(dn3, normalizer)).isTrue();
    assertThat(dn4.isAncestor(dn3Norm, normalizer)).isTrue();
    assertThat(dn4.isAncestor(dn4, normalizer)).isFalse();
    assertThat(dn4.isAncestor(dn4Norm, normalizer)).isFalse();
    assertThat(dn4.isAncestor(dn5, normalizer)).isFalse();
    assertThat(dn4.isAncestor(dn5Norm, normalizer)).isFalse();
    assertThat(dn5.isAncestor(nullDn, normalizer)).isFalse();

    assertThat(dn5.isAncestor(dn1, normalizer)).isTrue();
    assertThat(dn5.isAncestor(dn1Norm, normalizer)).isTrue();
    assertThat(dn5.isAncestor(dn2, normalizer)).isTrue();
    assertThat(dn5.isAncestor(dn2Norm, normalizer)).isTrue();
    assertThat(dn5.isAncestor(dn3, normalizer)).isTrue();
    assertThat(dn5.isAncestor(dn3Norm, normalizer)).isTrue();
    assertThat(dn5.isAncestor(dn4, normalizer)).isTrue();
    assertThat(dn5.isAncestor(dn4Norm, normalizer)).isTrue();
    assertThat(dn5.isAncestor(dn5, normalizer)).isFalse();
    assertThat(dn5.isAncestor(dn5Norm, normalizer)).isFalse();
    assertThat(dn5.isAncestor(nullDn, normalizer)).isFalse();

    assertThat(nullDn.isAncestor(dn1, normalizer)).isTrue();
    assertThat(nullDn.isAncestor(dn1Norm, normalizer)).isTrue();
    assertThat(nullDn.isAncestor(dn2, normalizer)).isTrue();
    assertThat(nullDn.isAncestor(dn2Norm, normalizer)).isTrue();
    assertThat(nullDn.isAncestor(dn3, normalizer)).isTrue();
    assertThat(nullDn.isAncestor(dn3Norm, normalizer)).isTrue();
    assertThat(nullDn.isAncestor(dn4, normalizer)).isTrue();
    assertThat(nullDn.isAncestor(dn4Norm, normalizer)).isTrue();
    assertThat(nullDn.isAncestor(dn5, normalizer)).isTrue();
    assertThat(nullDn.isAncestor(dn5Norm, normalizer)).isTrue();
    assertThat(nullDn.isAncestor(nullDn, normalizer)).isFalse();
  }


  /** Test for copy method. */
  @Test
  public void copy()
  {
    final Dn dn1 = new Dn("uid=1,ou=people,dc=ldaptive,dc=org");
    final Dn cp1 = Dn.copy(dn1);
    assertThat(dn1).isEqualTo(cp1);
    assertThat(dn1.isFrozen()).isFalse();
    assertThat(cp1.isFrozen()).isFalse();

    final Dn dn2 = Dn.builder()
      .add("uid=1,ou=people,dc=ldaptive,dc=org")
      .freeze()
      .build();
    final Dn cp2 = Dn.copy(dn2);
    assertThat(dn2).isEqualTo(cp2);
    assertThat(dn2.isFrozen()).isTrue();
    assertThat(cp2.isFrozen()).isFalse();
  }
}
