/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import java.util.Random;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for filter functions.
 *
 * @author  Middleware Services
 */
public class FilterFunctionTest
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
  // CheckStyle:MethodLength OFF
  @DataProvider(name = "filter")
  public Object[][] createFilter()
  {
    return
      new Object[][] {
        new Object[] {"", null, true, },
        new Object[] {" ", null, true, },
        new Object[] {"  ", null, true, },
        new Object[] {"(", null, true, },
        new Object[] {" (", null, true, },
        new Object[] {"  (", null, true, },
        new Object[] {")", null, true, },
        new Object[] {" )", null, true, },
        new Object[] {"  )", null, true, },
        new Object[] {"()", null, true, },
        new Object[] {"( )", null, true, },
        new Object[] {"(  )", null, true, },
        new Object[] {" () ", null, true, },
        new Object[] {"  ()  ", null, true, },
        new Object[] {"(foo)", null, true, },
        new Object[] {"&foo=bar", null, true, },
        new Object[] {"(=bar)", null, true, },
        new Object[] {"(>=bar)", null, true, },
        new Object[] {"(<=bar)", null, true, },
        new Object[] {"(~=bar)", null, true, },
        new Object[] {"(:=bar)", null, true, },
        new Object[] {"(:dn:=bar)", null, true, },
        new Object[] {"(foo=bar", null, true, },
        new Object[] {"((foo=bar))", null, true, },
        new Object[] {"((a=b)c", null, true, },
        new Object[] {"(a=b)(c=d)", null, true, },
        new Object[] {"(::=bar)", null, true, },
        new Object[] {"(:dn::=bar)", null, true, },
        new Object[] {"(:error:foo:=bar)", null, true, },
        new Object[] {"(:dn:a:b:=bar)", null, true, },
        new Object[] {"(:dn:foo:a=bar)", null, true, },
        new Object[] {"(:foo:a=bar)", null, true, },
        new Object[] {"(foo=ba(r)", null, true, },
        new Object[] {"(foo=ba)r)", null, true, },
        new Object[] {"(foo:=ba(r)", null, true, },
        new Object[] {"(foo:=ba)r)", null, true, },
        new Object[] {"(foo:dn:=ba(r)", null, true, },
        new Object[] {"(foo:dn:=ba)r)", null, true, },
        new Object[] {"(:dn:foo:=ba(r)", null, true, },
        new Object[] {"(:dn:foo:=ba)r)", null, true, },
        new Object[] {"(foo=**bar)", null, true, },
        new Object[] {"(foo=bar**)", null, true, },
        new Object[] {"(foo=bar**baz)", null, true, },
        new Object[] {"(foo=*bar**baz**qux*)", null, true, },
        new Object[] {"(foo>)", null, true, },
        new Object[] {"(foo<)", null, true, },
        new Object[] {"(foo~)", null, true, },
        new Object[] {"(foo:)", null, true, },
        new Object[] {"(foo>bar)", null, true, },
        new Object[] {"(foo<bar)", null, true, },
        new Object[] {"(foo~bar)", null, true, },
        new Object[] {"(foo:bar)", null, true, },
        new Object[] {"(foo:dn=bar)", null, true, },
        new Object[] {"(:dn:bar=baz)", null, true, },
        new Object[] {"(foo:dn:bar=baz)", null, true, },
        new Object[] {"(foo::=bar)", null, true, },
        new Object[] {"(:dn::=bar)", null, true, },
        new Object[] {"(foo:dn::=bar)", null, true, },
        new Object[] {"(foo=\\zz)", null, true, },
        new Object[] {"(foo=\\az)", null, true, },
        new Object[] {"(foo=\\a)", null, true, },
        new Object[] {"(foo=a\\z)", null, true, },
        new Object[] {"(foo=\\)", null, true, },
        new Object[] {"(foo>=\\zz)", null, true, },
        new Object[] {"(foo>=\\az)", null, true, },
        new Object[] {"(foo>=\\a)", null, true, },
        new Object[] {"(foo>=a\\z)", null, true, },
        new Object[] {"(foo>=\\)", null, true, },
        new Object[] {"(foo<=\\zz)", null, true, },
        new Object[] {"(foo<=\\az)", null, true, },
        new Object[] {"(foo<=\\a)", null, true, },
        new Object[] {"(foo<=a\\z)", null, true, },
        new Object[] {"(foo<=\\)", null, true, },
        new Object[] {"(foo=))", null, true, },
        new Object[] {"(foo=()", null, true, },
        new Object[] {"(foo>=*)", null, true, },
        new Object[] {"(foo<=*)", null, true, },
        new Object[] {"(foo~=*)", null, true, },
        new Object[] {"(foo:=*)", null, true, },
        new Object[] {"(foo>=*)", null, true, },
        new Object[] {"(foo>=()", null, true, },
        new Object[] {"(foo<=()", null, true, },
        new Object[] {"(foo~=()", null, true, },
        new Object[] {"(foo:=()", null, true, },
        new Object[] {"(foo>=()", null, true, },
        new Object[] {"(foo>=))", null, true, },
        new Object[] {"(foo<=))", null, true, },
        new Object[] {"(foo~=))", null, true, },
        new Object[] {"(foo:=))", null, true, },
        new Object[] {"(foo>=))", null, true, },
        new Object[] {"(foo>=bar*baz)", null, true, },
        new Object[] {"(foo<=bar*baz)", null, true, },
        new Object[] {"(foo~=bar*baz)", null, true, },
        new Object[] {"(&(foo=bar)(baz=qux)", null, true, },
        new Object[] {"(&(foo=bar)((baz=qux))", null, true, },
        new Object[] {"(&(foo=bar))(baz=qux))", null, true, },
        new Object[] {"(cn=test(", null, true, },
        new Object[] {"(cn=aaaaa", null, true, },
        new Object[] {"(&(cn=abc)", null, true, },
        new Object[] {"(&(cn=abc)(sn=xyz)", null, true, },
        new Object[] {"(&(cn=abc)(givenName=def)(sn=xyz)", null, true, },
        new Object[] {"(=John)", null, true, },
        new Object[] {"(?authId=)", null, true, },
        new Object[] {"(givenName:=John", null, true, },
        new Object[] {"givenName:=John)", null, true, },
        new Object[] {":=", null, true, },
        new Object[] {"(:=dummyAssertion)", null, true, },
        new Object[] {"((objectClass=*)&(uid=*))", null, true, },
        new Object[] {"&(objectClass=*)(uid=*)", null, true, },
        new Object[] {"(cn=**john)", null, true, },
        new Object[] {"(givenName=John**)", null, true, },
        new Object[] {"(uid=123**456)", null, true, },
        new Object[] {"(uid=*123**456**789*)", null, true, },
        new Object[] {"((objectCategory=person)(objectClass=user)(!(cn=user1*)))", null, true, },
        new Object[] {"((&(objectClass=user)(cn=andy*)(cn=steve*)(cn=margaret*)))", null, true, },
        new Object[] {"(cn>=*)", null, true, },
        new Object[] {"(cn>=123*)", null, true, },
        new Object[] {"(cn>=*123)", null, true, },
        new Object[] {"(cn<=*)", null, true, },
        new Object[] {"(cn<=123*)", null, true, },
        new Object[] {"(cn<=*123)", null, true, },
        new Object[] {"(cn~=*)", null, true, },
        new Object[] {"(cn~=123*)", null, true, },
        new Object[] {"(cn~=*123)", null, true, },
        new Object[] {"(::=alice)", null, true, },
        new Object[] {"(:dn::=alice)", null, true, },
        new Object[] {"(uid:dn:caseExactMatch=123)", null, true, },
        new Object[] {"(uid::=123)", null, true, },
        new Object[] {"uid:caseExactMatch:=*", null, true, },
        new Object[] {"uid:caseExactMatch:=123*", null, true, },
        new Object[] {"uid:caseExactMatch:=*123", null, true, },
        new Object[] {"uid:dn:caseExactMatch:=*", null, true, },
        new Object[] {"uid:dn:caseExactMatch:=123*", null, true, },
        new Object[] {"uid:dn:caseExactMatch:=*123", null, true, },
        new Object[] {
          "(memberOf=1.2.840.113556.1.4.1301=$#@&*()==,2.5.4.11=local,2.5.4.11=users,2.5.4.11=readimanager)",
          null,
          true,
        },
        new Object[] {
          "(member:1.2.840.113556.1.4.1941:=(CN=John Smith,DC=MyDomain,DC=NET))",
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
          "givenName:=",
          new ExtensibleFilter(null, "givenName", new byte[0]),
          false,
        },
        new Object[] {
          "givenName:dn:=",
          new ExtensibleFilter(null, "givenName", new byte[0], true),
          false,
        },
        new Object[] {
          ":caseExactMatch:=",
          new ExtensibleFilter("caseExactMatch", null, new byte[0]),
          false,
        },
        new Object[] {
          "(:caseExactMatch:=John)",
          new ExtensibleFilter("caseExactMatch", null, "John"),
          false,
        },
        new Object[] {
          ":dn:caseExactMatch:=",
          new ExtensibleFilter("caseExactMatch", null, new byte[0], true),
          false,
        },
        new Object[] {
          ":dn:caseExactMatch:=John",
          new ExtensibleFilter("caseExactMatch", null, "John", true),
          false,
        },
        new Object[] {
          "givenName:=John",
          new ExtensibleFilter(null, "givenName", "John"),
          false,
        },
        new Object[] {
          "givenName:dn:=John",
          new ExtensibleFilter(null, "givenName", "John", true),
          false,
        },
        new Object[] {
          "(givenName:=)",
          new ExtensibleFilter(null, "givenName", new byte[0]),
          false,
        },
        new Object[] {
          "(ou:dn:someMatch:=dummyAssertion\\23\\c4\\8d)",
          new ExtensibleFilter("someMatch", "ou", "dummyAssertion#\u010D", true),
          false,
        },
        new Object[] {
          "(ou:someMatch:=dummyAssertion\\23\\c4\\8d)",
          new ExtensibleFilter("someMatch", "ou", "dummyAssertion#\u010D"),
          false,
        },
        new Object[] {
          "(1.2.3.4:dn:1.3434.23.2:=dummyAssertion\\23\\c4\\8d)",
          new ExtensibleFilter("1.3434.23.2", "1.2.3.4", "dummyAssertion#\u010D", true),
          false,
        },
        new Object[] {
          "(1.2.3.4:1.3434.23.2:=dummyAssertion\\23\\c4\\8d)",
          new ExtensibleFilter("1.3434.23.2", "1.2.3.4", "dummyAssertion#\u010D"),
          false,
        },
        new Object[] {
          "(:1.3434.23.2:=dummyAssertion\\23\\c4\\8d)",
          new ExtensibleFilter("1.3434.23.2", null, "dummyAssertion#\u010D"),
          false,
        },
        new Object[] {
          "(ou:=dummyAssertion\\23\\c4\\8d)",
          new ExtensibleFilter(null, "ou", "dummyAssertion#\u010D"),
          false,
        },
        new Object[] {
          "(:dn:someMatch:=dummyAssertion\\23\\c4\\8d)",
          new ExtensibleFilter("someMatch", null, "dummyAssertion#\u010D", true),
          false,
        },
        new Object[] {
          "(:someMatch:=dummyAssertion\\23\\c4\\8d)",
          new ExtensibleFilter("someMatch", null, "dummyAssertion#\u010D"),
          false,
        },
        new Object[] {
          "(:dn:1.3434.23.2:=dummyAssertion\\23\\c4\\8d)",
          new ExtensibleFilter("1.3434.23.2", null, "dummyAssertion#\u010D", true),
          false,
        },
        new Object[] {
          "(:1.3434.23.2:=dummyAssertion\\23\\c4\\8d)",
          new ExtensibleFilter("1.3434.23.2", null, "dummyAssertion#\u010D"),
          false,
        },
        new Object[] {
          "(cn:1.2.3.4.5:=Fred Flintstone)",
          new ExtensibleFilter("1.2.3.4.5", "cn", "Fred Flintstone"),
          false,
        },
        new Object[] {
          "(sn:dn:2.4.6.8.10:=Barney Rubble)",
          new ExtensibleFilter("2.4.6.8.10", "sn", "Barney Rubble", true),
          false,
        },
        new Object[] {
          "(o:dn:=Ace Industry)",
          new ExtensibleFilter(null, "o", "Ace Industry", true),
          false,
        },
        new Object[] {
          "(:dn:2.4.6.8.10:=Dino)",
          new ExtensibleFilter("2.4.6.8.10", null, "Dino", true),
          false,
        },
        new Object[] {
          "foo=",
          new EqualityFilter("foo", new byte[0]),
          false,
        },
        new Object[] {
          "(foo=)",
          new EqualityFilter("foo", new byte[0]),
          false,
        },
        new Object[] {
          "(foo>=)",
          new GreaterOrEqualFilter("foo", new byte[0]),
          false,
        },
        new Object[] {
          "(foo<=)",
          new LessOrEqualFilter("foo", new byte[0]),
          false,
        },
        new Object[] {
          "(foo~=)",
          new ApproximateFilter("foo", new byte[0]),
          false,
        },
        new Object[] {
          "(foo:=)",
          new ExtensibleFilter(null, "foo", new byte[0], false),
          false,
        },
        new Object[] {
          "(foo:dn:=)",
          new ExtensibleFilter(null, "foo", new byte[0], true),
          false,
        },
        new Object[] {
          "(:foo:=)",
          new ExtensibleFilter("foo", null, new byte[0], false),
          false,
        },
        new Object[] {
          "(:dn:foo:=)",
          new ExtensibleFilter("foo", null, new byte[0], true),
          false,
        },
        new Object[] {
          "foo=*",
          new PresenceFilter("foo"),
          false,
        },
        new Object[] {
          "(foo=*)",
          new PresenceFilter("foo"),
          false,
        },
        new Object[] {
          "foo=a",
          new EqualityFilter("foo", "a"),
          false,
        },
        new Object[] {
          "(foo=a)",
          new EqualityFilter("foo", "a"),
          false,
        },
        new Object[] {
          "(foo>=a)",
          new GreaterOrEqualFilter("foo", "a"),
          false,
        },
        new Object[] {
          "(foo<=a)",
          new LessOrEqualFilter("foo", "a"),
          false,
        },
        new Object[] {
          "(foo~=a)",
          new ApproximateFilter("foo", "a"),
          false,
        },
        new Object[] {
          "(foo:=a)",
          new ExtensibleFilter(null, "foo", "a", false),
          false,
        },
        new Object[] {
          "(foo:dn:=a)",
          new ExtensibleFilter(null, "foo", "a", true),
          false,
        },
        new Object[] {
          "(:foo:=a)",
          new ExtensibleFilter("foo", null, "a", false),
          false,
        },
        new Object[] {
          "(:dn:foo:=a)",
          new ExtensibleFilter("foo", null, "a", true),
          false,
        },
        new Object[] {
          "foo=bar",
          new EqualityFilter("foo", "bar"),
          false,
        },
        new Object[] {
          "(foo=bar)",
          new EqualityFilter("foo", "bar"),
          false,
        },
        new Object[] {
          "foo>=bar",
          new GreaterOrEqualFilter("foo", "bar"),
          false,
        },
        new Object[] {
          "(foo>=bar)",
          new GreaterOrEqualFilter("foo", "bar"),
          false,
        },
        new Object[] {
          "foo<=bar",
          new LessOrEqualFilter("foo", "bar"),
          false,
        },
        new Object[] {
          "(foo<=bar)",
          new LessOrEqualFilter("foo", "bar"),
          false,
        },
        new Object[] {
          "foo~=bar",
          new ApproximateFilter("foo", "bar"),
          false,
        },
        new Object[] {
          "(foo~=bar)",
          new ApproximateFilter("foo", "bar"),
          false,
        },
        new Object[] {
          "(foo=bar*)",
          new SubstringFilter("foo", "bar", null, (String[]) null),
          false,
        },
        new Object[] {
          "(foo=*bar*)",
          new SubstringFilter("foo", null, null, "bar"),
          false,
        },
        new Object[] {
          "(foo=*bar)",
          new SubstringFilter("foo", null, "bar", (String[]) null),
          false,
        },
        new Object[] {
          "(foo=bar*baz*qux)",
          new SubstringFilter("foo", "bar", "qux", "baz"),
          false,
        },
        new Object[] {
          "foo:=bar",
          new ExtensibleFilter(null, "foo", "bar", false),
          false,
        },
        new Object[] {
          "(foo:=bar)",
          new ExtensibleFilter(null, "foo", "bar", false),
          false,
        },
        new Object[] {
          "foo:dn:=bar",
          new ExtensibleFilter(null, "foo", "bar", true),
          false,
        },
        new Object[] {
          "(foo:dn:=bar)",
          new ExtensibleFilter(null, "foo", "bar", true),
          false,
        },
        new Object[] {
          ":dn:foo:=bar",
          new ExtensibleFilter("foo", null, "bar", true),
          false,
        },
        new Object[] {
          "(:dn:foo:=bar)",
          new ExtensibleFilter("foo", null, "bar", true),
          false,
        },
        new Object[] {
          "foo:dn:bar:=baz",
          new ExtensibleFilter("bar", "foo", "baz", true),
          false,
        },
        new Object[] {
          "(foo:dn:bar:=baz)",
          new ExtensibleFilter("bar", "foo", "baz", true),
          false,
        },
        new Object[] {
          "(:foo:=bar)",
          new ExtensibleFilter("foo", null, "bar", false),
          false,
        },
        new Object[] {
          "(&(a=b))",
          new AndFilter(new EqualityFilter("a", "b")),
          false,
        },
        new Object[] {
          "(&(a=b)(c=d))",
          new AndFilter(new EqualityFilter("a", "b"), new EqualityFilter("c", "d")),
          false,
        },
        new Object[] {
          "(|(a=b))",
          new OrFilter(new EqualityFilter("a", "b")),
          false,
        },
        new Object[] {
          "(|(a=b)(c=d))",
          new OrFilter(new EqualityFilter("a", "b"), new EqualityFilter("c", "d")),
          false,
        },
        new Object[] {
          "(!(foo=bar))",
          new NotFilter(new EqualityFilter("foo", "bar")),
          false,
        },
        new Object[] {
          "cn=",
          new EqualityFilter("cn", new byte[0]),
          false,
        },
        new Object[] {
          "(cn=)",
          new EqualityFilter("cn", new byte[0]),
          false,
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
          "(cn='~%\\28'$'\\5C)",
          new EqualityFilter("cn", "'~%('$'\\"),
          false,
        },
        new Object[] {
          "(cn='~%\\28'$'\\5Cac)",
          new EqualityFilter("cn", "'~%('$'\\ac"),
          false,
        },
        new Object[] {
          "(givenName=*)",
          new PresenceFilter("givenName"),
          false,
        },
        new Object[] {
          "(ou=*)",
          new PresenceFilter("ou"),
          false,
        },
        new Object[] {
          "(1.2.3.4=*)",
          new PresenceFilter("1.2.3.4"),
          false,
        },
        new Object[] {
          "(givenName~=)",
          new ApproximateFilter("givenName", new byte[0]),
          false,
        },
        new Object[] {
          "(givenName~=John)",
          new ApproximateFilter("givenName", "John"),
          false,
        },
        new Object[] {
          "(ou~=people)",
          new ApproximateFilter("ou", "people"),
          false,
        },
        new Object[] {
          "(givenName>=)",
          new GreaterOrEqualFilter("givenName", new byte[0]),
          false,
        },
        new Object[] {
          "(givenName>=John)",
          new GreaterOrEqualFilter("givenName", "John"),
          false,
        },
        new Object[] {
          "(givenName<=)",
          new LessOrEqualFilter("givenName", new byte[0]),
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
          "(ou=foo*)",
          new SubstringFilter("ou", "foo", null, (String[]) null),
          false,
        },
        new Object[] {
          "(ou=*foo*)",
          new SubstringFilter("ou", null, null, "foo"),
          false,
        },
        new Object[] {
          "(ou=foo*bar)",
          new SubstringFilter("ou", "foo", "bar", (String[]) null),
          false,
        },
        new Object[] {
          "(ou=foo*bar*)",
          new SubstringFilter("ou", "foo", null, "bar"),
          false,
        },
        new Object[] {
          "(ou=*bar)",
          new SubstringFilter("ou", null, "bar", (String[]) null),
          false,
        },
        new Object[] {
          "(ou=foo*baz*bar)",
          new SubstringFilter("ou", "foo", "bar", "baz"),
          false,
        },
        new Object[] {
          "(ou=a*b*c*d*e*f)",
          new SubstringFilter("ou", "a", "f", "b", "c", "d", "e"),
          false,
        },
        new Object[] {
          "(ou=*b*c*d*e*f)",
          new SubstringFilter("ou", null, "f", "b", "c", "d", "e"),
          false,
        },
        new Object[] {
          "(ou=a*b*c*d*e*)",
          new SubstringFilter("ou", "a", null, "b", "c", "d", "e"),
          false,
        },
        new Object[] {
          "(ou=*b*c*d*e*)",
          new SubstringFilter("ou", null, null, "b", "c", "d", "e"),
          false,
        },
        new Object[] {
          "(ou=foo* *bar)",
          new SubstringFilter("ou", "foo", "bar", " "),
          false,
        },
        new Object[] {
          "(ou=foo* a *bar)",
          new SubstringFilter("ou", "foo", "bar", " a "),
          false,
        },
        new Object[] {
          "(ou=*foo*)",
          new SubstringFilter("ou", null, null, "foo"),
          false,
        },
        new Object[] {
          "(ou=\\5C*\\00*\\3D*\\2Abb)",
          new SubstringFilter("ou", "\\", "*bb", "\0", "="),
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
          "(&(objectClass=person)(objectClass=organizationalUnit))",
          new AndFilter(
            new EqualityFilter("objectClass", "person"),
            new EqualityFilter("objectClass", "organizationalUnit")),
          false,
        },
        new Object[] {
          "(&(ou~=people)(age>=30))",
          new AndFilter(new ApproximateFilter("ou", "people"), new GreaterOrEqualFilter("age", "30")),
          false,
        },
        new Object[] {
          "(&(ou~=people))",
          new AndFilter(new ApproximateFilter("ou", "people")),
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
          "(&(objectClass=nisNetgroup)(|(nisNetGroupTriple=a*a)(nisNetGroupTriple=\\28*,acc1,*\\29)))",
          new AndFilter(
            new EqualityFilter("objectClass", "nisNetgroup"),
            new OrFilter(
              new SubstringFilter("nisNetGroupTriple", "a", "a", (String[]) null),
              new SubstringFilter("nisNetGroupTriple", "(", ")", ",acc1,"))),
          false,
        },
        new Object[] {
          "(|(ou~=people)(age>=30))",
          new OrFilter(new ApproximateFilter("ou", "people"), new GreaterOrEqualFilter("age", "30")),
          false,
        },
        new Object[] {
          "(|(age>=30))",
          new OrFilter(new GreaterOrEqualFilter("age", "30")),
          false,
        },
        new Object[] {
          "(cn=Babs Jensen)",
          new EqualityFilter("cn", "Babs Jensen"),
          false,
        },
        new Object[] {
          "(ou=people)",
          new EqualityFilter("ou", "people"),
          false,
        },
        new Object[] {
          "(ou=people/in/my/company)",
          new EqualityFilter("ou", "people/in/my/company"),
          false,
        },
        new Object[] {
          "(!(cn=Tim Howes))",
          new NotFilter(new EqualityFilter("cn", "Tim Howes")),
          false,
        },
        new Object[] {
          "(!(&(ou~=people)(age>=30)))",
          new NotFilter(new AndFilter(new ApproximateFilter("ou", "people"), new GreaterOrEqualFilter("age", "30"))),
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
          "(ou;lang-de>=\\23\\42asdl fkajsd)",
          new GreaterOrEqualFilter("ou;lang-de", "#Basdl fkajsd"),
          false,
        },
        new Object[] {
          "(ou;lang-de;version-124>=\\23\\42asdl fkajsd)",
          new GreaterOrEqualFilter("ou;lang-de;version-124", "#Basdl fkajsd"),
          false,
        },
        new Object[] {
          "(1.3.4.2;lang-de;version-124>=\\23\\42afdl fkajsd)",
          new GreaterOrEqualFilter("1.3.4.2;lang-de;version-124", "#Bafdl fkajsd"),
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
          "(cn=\\C2\\A2)",
          new EqualityFilter("cn", "\u00A2"),
          false,
        },
        new Object[] {
          "(cn=\\E2\\89\\A0)",
          new EqualityFilter("cn", "\u2260"),
          false,
        },
        new Object[] {
          "(uid=#f1)",
          new EqualityFilter("uid", "#f1"),
          false,
        },
        new Object[] {
          "(sn=Lu\\c4\\8di\\c4\\87)",
          new EqualityFilter("sn", "Lučić"),
          false,
        },
        new Object[] {
          "(&(givenName=Bill\\2A)(sn=Wa\\28ll\\29ace))",
          new AndFilter(new EqualityFilter("givenName", "Bill*"), new EqualityFilter("sn", "Wa(ll)ace")),
          false,
        },
        new Object[] {
          "(&(givenName=\\42\\69\\6C\\6C)(sn=\\57\\61\\6C\\6C\\61\\63\\65))",
          new AndFilter(new EqualityFilter("givenName", "Bill"), new EqualityFilter("sn", "Wallace")),
          false,
        },
        new Object[] {
          "(&(givenName=B\\C3\\ACll))",
          new AndFilter(new EqualityFilter("givenName", "B\u00ECll")),
          false,
        },
        new Object[] {
          "(&(givenName=B\\F0\\9F\\9C\\81ll))",
          new AndFilter(new EqualityFilter("givenName", "B\uD83D\uDF01ll")),
          false,
        },
        new Object[] {
          "(&(equal1=1)(objectClass1=oc1)(presence1=*)(objectClass2=*)(sub1=1*)(sub2=*2*)" +
            "(sub3=*3)(greater1>=1)(less1<=1)(approx1~=1)(ext1:=1)(&(equal2=2)" +
            "(objectClass3=oc3)(objectClass4=oc4)(presence2=*)(sub4=4*4*4))" +
            "(!(equal3=3))(!(&(equal4=4)(equal5=5)))(!(|(objectClass5=oc5)(equal6=6)))" +
            "(|(equal7=7)(objectClass6=oc6)(objectClass7=oc7)(presence3=*)" +
            "(presence4=*)(objectClass8=*)(sub5=5*5*5)))",
          new AndFilter(
            new EqualityFilter("equal1", "1"),
            new EqualityFilter("objectClass1", "oc1"),
            new PresenceFilter("presence1"),
            new PresenceFilter("objectClass2"),
            new SubstringFilter("sub1", "1", null, (String[]) null),
            new SubstringFilter("sub2", null, null, "2"),
            new SubstringFilter("sub3", null, "3", (String[]) null),
            new GreaterOrEqualFilter("greater1", "1"),
            new LessOrEqualFilter("less1", "1"),
            new ApproximateFilter("approx1", "1"),
            new ExtensibleFilter(null, "ext1", "1", false),
            new AndFilter(
              new EqualityFilter("equal2", "2"),
              new EqualityFilter("objectClass3", "oc3"),
              new EqualityFilter("objectClass4", "oc4"),
              new PresenceFilter("presence2"),
              new SubstringFilter("sub4", "4", "4", "4")),
            new NotFilter(
              new EqualityFilter("equal3", "3")),
            new NotFilter(
              new AndFilter(
                new EqualityFilter("equal4", "4"),
                new EqualityFilter("equal5", "5"))),
            new NotFilter(
              new OrFilter(
                new EqualityFilter("objectClass5", "oc5"),
                new EqualityFilter("equal6", "6"))),
            new OrFilter(
              new EqualityFilter("equal7", "7"),
              new EqualityFilter("objectClass6", "oc6"),
              new EqualityFilter("objectClass7", "oc7"),
              new PresenceFilter("presence3"),
              new PresenceFilter("presence4"),
              new PresenceFilter("objectClass8"),
              new SubstringFilter("sub5", "5", "5", "5"))),
          false,
        },
      };
  }
  // CheckStyle:MethodLength ON

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
  @Test(dataProvider = "filter")
  public void parseAndCompareDefault(final String value, final Filter filter, final boolean throwsException)
    throws Exception
  {
    final FilterFunction func = new DefaultFilterFunction();
    try {
      Assert.assertEquals(func.parse(value), filter);
      if (throwsException) {
        Assert.fail("Should have thrown exception");
      }
    } catch (FilterParseException e) {
      if (!throwsException) {
        Assert.fail("Should not have thrown exception");
      }
    }
  }


  /**
   * @param  value  to parse.
   * @param  filter  expected value.
   * @param  throwsException  whether an exception is expected
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "filter")
  public void parseAndCompareRegex(final String value, final Filter filter, final boolean throwsException)
    throws Exception
  {
    final FilterFunction func = new RegexFilterFunction();
    try {
      Assert.assertEquals(func.parse(value), filter);
      if (throwsException) {
        Assert.fail("Should have thrown exception");
      }
    } catch (FilterParseException e) {
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
  @Test(dataProvider = "generateRandomFilter", invocationCount = 1000)
  public void parseDefault(final String value, final boolean throwsException)
    throws Exception
  {
    final FilterFunction func = new DefaultFilterFunction();
    try {
      func.parse(value);
      if (throwsException) {
        Assert.fail("Should have thrown exception for filter " + value);
      }
    } catch (FilterParseException e) {
      if (!throwsException) {
        Assert.fail("Should not have thrown exception for filter " + value, e);
      }
    }
  }


  /**
   * @param  value  to parse.
   * @param  throwsException  whether an exception is expected
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "generateRandomFilter", invocationCount = 1000)
  public void parseRegex(final String value, final boolean throwsException)
    throws Exception
  {
    final FilterFunction func = new RegexFilterFunction();
    try {
      func.parse(value);
      if (throwsException) {
        Assert.fail("Should have thrown exception for filter " + value);
      }
    } catch (FilterParseException e) {
      if (!throwsException) {
        Assert.fail("Should not have thrown exception for filter " + value, e);
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
    final String[] attr = new String[] {"a", "b", "c", "d", "e", "f", "g", "h", "i"};
    final String[] values = new String[] {"j", "k", "l", "m", "n", "o", "p", "q", "r", "*s", "*", "}", "!", "|"};
    final String[] operators = new String[] {"&", "|"};
    final String[] filterTypes = new String[] {"~=", "=", ">=", "<=", ":="};
    final int count = rand.nextInt(seed) + 1;
    String filter = "";
    for (int i = 0; i < count; i++) {
      final String o = operators[rand.nextInt(operators.length)];
      final String a = attr[rand.nextInt(attr.length)];
      final String f = filterTypes[rand.nextInt(filterTypes.length)];
      String v = values[rand.nextInt(values.length)];
      if (v.contains("*") && !"=".equals(f)) {
        v = v.replace('*', 't');
      }
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
