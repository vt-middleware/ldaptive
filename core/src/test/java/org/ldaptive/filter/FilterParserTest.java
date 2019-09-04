/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import java.util.Random;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link FilterParser}.
 *
 * @author  Middleware Services
 */
public class FilterParserTest
{

  /**
   * Random number generator.
   */
  private static Random rand = new Random();
  /**
   * Search filter test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "filter")
  public Object[][] createFilter()
  {
    return
      new Object[][] {
        new Object[] {
          "",
          null,
          true,
        },
        new Object[] {
          "givenName:=",
          new ExtensibleFilter(null, "givenName", new byte[0]),
          false,
        },
        new Object[] {
          "uupid:caseExactMatch:=john",
          new ExtensibleFilter("caseExactMatch", "uupid", "john"),
          false,
        },
        new Object[] {
          ":2.5.13.5:=john",
          new ExtensibleFilter("2.5.13.5", null, "john"),
          false,
        },
        new Object[] {
          "uid:dn:caseExactMatch:=1152120",
          new ExtensibleFilter("caseExactMatch", "uid", "1152120", true),
          false,
        },
        new Object[] {
          "uid:dn:2.5.13.5:=1152120",
          new ExtensibleFilter("2.5.13.5", "uid", "1152120", true),
          false,
        },
        new Object[] {
          "uid:dn:=1152120",
          new ExtensibleFilter(null, "uid", "1152120", true),
          false,
        },
        new Object[] {
          ":=",
          null,
          true,
        },
        new Object[] {
          "givenName:=John",
          new ExtensibleFilter(null, "givenName", "John"),
          false,
        },
        new Object[] {
          "(givenName:=)",
          new ExtensibleFilter(null, "givenName", new byte[0]),
          false,
        },
        new Object[] {
          "(=John)",
          null,
          true,
        },
        new Object[] {
          "(givenName:=John",
          null,
          true,
        },
        new Object[] {
          "givenName:=John)",
          null,
          true,
        },
        new Object[] {
          "uupid=jo\\00hn",
          new EqualityFilter("uupid", "jo\0hn"),
          false,
        },
        new Object[] {
          "uupid=\\5cdhawes\\5c",
          new EqualityFilter("uupid", "\\dhawes\\"),
          false,
        },
        new Object[] {
          "(|(cn=|&~!<>:=.dhawes))",
          new OrFilter(
            new EqualityFilter("cn", "|&~!<>:=.dhawes")),
          false,
        },
        new Object[] {
          "(givenName=*)",
          new PresenceFilter("givenName"),
          false,
        },
        new Object[] {
          "(givenName~=John)",
          new ApproximateFilter("givenName", "John"),
          false,
        },
        new Object[] {
          "(givenName>=John)",
          new GreaterOrEqualFilter("givenName", "John"),
          false,
        },
        new Object[] {
          "(givenName<=John)",
          new LessOrEqualFilter("givenName", "John"),
          false,
        },
        new Object[] {
          "(givenName=John)",
          new EqualityFilter("givenName", "John"),
          false,
        },
        new Object[] {
          "(givenName=John*)",
          new SubstringFilter("givenName", "John", null, (String[]) null),
          false,
        },
        new Object[] {
          "(givenName=*John)",
          new SubstringFilter("givenName", null, "John", (String[]) null),
          false,
        },
        new Object[] {
          "(givenName=*John*)",
          new SubstringFilter("givenName", null, null, "John"),
          false,
        },
        new Object[] {
          "(givenName=Mr*John*Doe*Jr)",
          new SubstringFilter("givenName", "Mr", "Jr", "John", "Doe"),
          false,
        },
        new Object[] {
          "(givenName:=John)",
          new ExtensibleFilter(null, "givenName", "John"),
          false,
        },
        new Object[] {
          "(&(givenName=John))",
          new AndFilter(new EqualityFilter("givenName", "John")),
          false,
        },
        new Object[] {
          "(&(givenName=John)(sn=Doe))",
          new AndFilter(new EqualityFilter("givenName", "John"), new EqualityFilter("sn", "Doe")),
          false,
        },
        new Object[] {
          "(&(givenName=John)(|(sn=Doe)(sn=Deere)))",
          new AndFilter(
            new EqualityFilter("givenName", "John"),
            new OrFilter(new EqualityFilter("sn", "Doe"), new EqualityFilter("sn", "Deere"))),
          false,
        },
        new Object[] {
          "(cn=Babs Jensen)",
          new EqualityFilter("cn", "Babs Jensen"),
          false,
        },
        new Object[] {
          "(!(cn=Tim Howes))",
          new NotFilter(new EqualityFilter("cn", "Tim Howes")),
          false,
        },
        new Object[] {
          "(&(objectClass=Person)(|(sn=Jensen)(cn=Babs J*)))",
          new AndFilter(
            new EqualityFilter("objectClass", "Person"),
            new OrFilter(
              new EqualityFilter("sn", "Jensen"),
              new SubstringFilter("cn", "Babs J", null, (String[]) null))),
          false,
        },
        new Object[] {
          "(o=univ*of*mich*)",
          new SubstringFilter("o", "univ", null, "of", "mich"),
          false,
        },
        new Object[] {
          "(seeAlso=)",
          new EqualityFilter("seeAlso", ""),
          false,
        },
        new Object[]{
          "(&(attr1=a)(attr2=b)(attr3=c)(attr4=d))",
          new AndFilter(
            new EqualityFilter("attr1", "a"),
            new EqualityFilter("attr2", "b"),
            new EqualityFilter("attr3", "c"),
            new EqualityFilter("attr4", "d")),
          false,
        },
        new Object[] {
          "(&(attr1=a)(&(attr2=b)(&(attr3=c)(attr4=d))))",
          new AndFilter(
            new EqualityFilter("attr1", "a"),
            new AndFilter(
              new EqualityFilter("attr2", "b"),
              new AndFilter(
                new EqualityFilter("attr3", "c"),
                new EqualityFilter("attr4", "d")))),
          false,
        },
        new Object[] {
          "(&(employmentType=*)" +
            "(!(employmentType=Hired))(!(employmentType=NEW))(!(employmentType=POS))(!(employmentType=REH)))",
          new AndFilter(
            new PresenceFilter("employmentType"),
            new NotFilter(new EqualityFilter("employmentType", "Hired")),
            new NotFilter(new EqualityFilter("employmentType", "NEW")),
            new NotFilter(new EqualityFilter("employmentType", "POS")),
            new NotFilter(new EqualityFilter("employmentType", "REH"))),
          false,
        },
        new Object[] {
          "(systemFlags:1.2.840.113556.1.4.803:=-2147483648)",
          new ExtensibleFilter("1.2.840.113556.1.4.803", "systemFlags", "-2147483648"),
          false,
        },
        new Object[] {
          "(o=Parens R Us \\28for all your parenthetical needs\\29)",
          new EqualityFilter("o", "Parens R Us (for all your parenthetical needs)"),
          false,
        },
        new Object[] {
          "(cn=*\\2A*)",
          new SubstringFilter("cn", null, null, "*"),
          false,
        },
        new Object[] {
          "(sn=Lu\\c4\\8di\\c4\\87)",
          new EqualityFilter("sn", "Lučić"),
          false,
        },
        new Object[] {
          "((objectClass=*)&(uid=*))",
          null,
          true,
        },
        new Object[] {
          "&(objectClass=*)(uid=*)",
          null,
          true,
        },
        new Object[] {
          "((objectCategory=person)(objectClass=user)(!(cn=user1*)))",
          null,
          true,
        },
        new Object[] {
          "((&(objectClass=user)(cn=andy*)(cn=steve*)(cn=margaret*)))",
          null,
          true,
        },
      };
  }

  /**
   * Search filter test data generated randomly.
   *
   * @return  request test data
   */
  @DataProvider(name = "generateRandomFilter")
  public Object[][] createRandomFilter()
  {
    return
      new Object[][] {
        new Object[] {
          generateFilter(rand.nextInt(1000)+1, false),
          false,
        },
        new Object[] {
          generateFilter(rand.nextInt(1000)+1, true),
          true,
        },
      };
  }

  /**
   * @param  value  to parse.
   * @param  filter  expected value.
   * @param  throwsException  whether an exception is expected
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "provider", dataProvider = "filter")
  public void parseAndCompare(final String value, final Filter filter, final boolean throwsException)
    throws Exception
  {
    try {
      Assert.assertEquals(FilterParser.parse(value), filter);
      if (throwsException) {
        Assert.fail("Should have thrown exception");
      }
    } catch (Exception e) {
      if (!throwsException) {
        Assert.fail("Should not have thrown exception");
      }
    }
  }

  /**
   * @param  value  to parse.
   * @param  throwsException  whether an exception is expected
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "provider", dataProvider = "generateRandomFilter", invocationCount = 1000)
  public void parse(final String value, final boolean throwsException)
    throws Exception
  {
    try {
      FilterParser.parse(value);
      if (throwsException) {
        Assert.fail("Should have thrown exception");
      }
    } catch (Exception e) {
      if (!throwsException) {
        Assert.fail("Should not have thrown exception");
      }
    }
  }

  /**
   * @param  seed  to rand function.
   * @param generateBadData boolean which decides if the filter is a bad one.
   *
   * @return generated filter string.
   */
  private static String generateFilter(final int seed, final boolean generateBadData)
  {
    final String[] attr = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i"};
    final String[] values = new String[]{"j", "k", "l", "m", "n", "o", "p", "q", "r", "*s", "*", "}", "!", "|"};
    final String[] operators = new String[]{"&", "|"};
    final String[] filterTypes = new String[]{"~=", "=", ">=", "<=", ":="};
    final int count = rand.nextInt(seed) + 1;
    String filter = "";
    for (int i = 0; i < count; i++) {
      final String o = operators[rand.nextInt(operators.length)];
      final String a = attr[rand.nextInt(attr.length)];
      final String v = values[rand.nextInt(values.length)];
      final String f = filterTypes[rand.nextInt(filterTypes.length)];
      String extraFilter = "";
      if ("|".equals(o) || "&".equals(o)) {
        for (int j = 0; j < rand.nextInt(100)+1; j++) {
          if (j % 2 == 0) {
            extraFilter = "(!(" + a + "=" + v +"))";
          } else {
            extraFilter = "(|(" + a + "=" + v +"))";
          }
        }
      }
      if (generateBadData) {
        extraFilter = extraFilter + f;
      }
      filter = "(" + o + ("".equals(extraFilter) ? "" : extraFilter)+ "(" + a + f + v + ")" +
        ("".equals(filter) ? "" : filter) + ")";
    }
    return filter;
  }
}
