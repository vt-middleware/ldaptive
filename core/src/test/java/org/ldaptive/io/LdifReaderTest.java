/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.io.Reader;
import java.io.StringReader;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchResponse;
import org.ldaptive.SearchResultReference;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link LdifReader}.
 *
 * @author  Middleware Services
 */
public class LdifReaderTest
{


  /**
   * LDIF test data.
   *
   * @return  hex test data
   */
  // CheckStyle:MethodLength OFF
  @DataProvider(name = "ldif")
  public Object[][] createEncodeDecodeData()
  {
    return
      new Object[][] {
        new Object[] {
          new StringReader(
            "version: 1\n" +
              "dn: cn=Barbara Jensen, ou=Product Development, dc=airius, dc=com\n" +
              "objectclass: top\n" +
              "objectclass: person\n" +
              "objectclass: organizationalPerson\n" +
              "cn: Barbara Jensen\n" +
              "cn: Barbara J Jensen\n" +
              "cn: Babs Jensen\n" +
              "sn: Jensen\n" +
              "uid: bjensen\n" +
              "telephonenumber: +1 408 555 1212\n" +
              "description: A big sailing fan.\n" +
              "\n" +
              "dn: cn=Bjorn Jensen, ou=Accounting, dc=airius, dc=com\n" +
              "objectclass: top\n" +
              "objectclass: person\n" +
              "objectclass: organizationalPerson\n" +
              "cn: Bjorn Jensen\n" +
              "sn: Jensen\n" +
              "telephonenumber: +1 408 555 1212"),
          SearchResponse.builder()
            .entry(
              LdapEntry.builder()
                .dn("cn=Barbara Jensen, ou=Product Development, dc=airius, dc=com")
                .attributes(
                  LdapAttribute.builder().name("objectclass").values("top", "person", "organizationalPerson").build(),
                  LdapAttribute.builder()
                    .name("cn")
                    .values("Barbara Jensen", "Barbara J Jensen", "Babs Jensen")
                    .build(),
                  LdapAttribute.builder().name("sn").values("Jensen").build(),
                  LdapAttribute.builder().name("uid").values("bjensen").build(),
                  LdapAttribute.builder().name("telephonenumber").values("+1 408 555 1212").build(),
                  LdapAttribute.builder().name("description").values("A big sailing fan.").build())
                .build(),
              LdapEntry.builder()
                .dn("cn=Bjorn Jensen, ou=Accounting, dc=airius, dc=com")
                .attributes(
                  LdapAttribute.builder().name("objectclass").values("top", "person", "organizationalPerson").build(),
                  LdapAttribute.builder().name("cn").values("Bjorn Jensen").build(),
                  LdapAttribute.builder().name("sn").values("Jensen").build(),
                  LdapAttribute.builder().name("telephonenumber").values("+1 408 555 1212").build())
                .build())
            .build(),
        },
        new Object[] {
          new StringReader(
            "version: 1\n" +
              "dn:cn=Barbara Jensen, ou=Product Development, dc=airius, dc=com\n" +
              "objectclass:top\n" +
              "objectclass:person\n" +
              "objectclass:organizationalPerson\n" +
              "cn:Barbara Jensen\n" +
              "cn:Barbara J Jensen\n" +
              "cn:Babs Jensen\n" +
              "sn:Jensen\n" +
              "uid:bjensen\n" +
              "telephonenumber:+1 408 555 1212\n" +
              "description:Babs is a big sailing fan, and travels extensively in sea\n" +
              " rch of perfect sailing conditions.\n" +
              "title:Product Manager, Rod and Reel Division"),
          SearchResponse.builder()
            .entry(
              LdapEntry.builder()
                .dn("cn=Barbara Jensen, ou=Product Development, dc=airius, dc=com")
                .attributes(
                  LdapAttribute.builder().name("objectclass").values("top", "person", "organizationalPerson").build(),
                  LdapAttribute.builder()
                    .name("cn")
                    .values("Barbara Jensen", "Barbara J Jensen", "Babs Jensen")
                    .build(),
                  LdapAttribute.builder().name("sn").values("Jensen").build(),
                  LdapAttribute.builder().name("uid").values("bjensen").build(),
                  LdapAttribute.builder().name("telephonenumber").values("+1 408 555 1212").build(),
                  LdapAttribute.builder()
                    .name("description")
                    .values(
                      "Babs is a big sailing fan, and travels extensively in search of perfect sailing conditions.")
                    .build(),
                  LdapAttribute.builder().name("title").values("Product Manager, Rod and Reel Division").build())
                .build())
            .build(),
        },
        new Object[] {
          new StringReader(
            "version: 1\n" +
              "dn: cn=Gern Jensen, ou=Product Testing, dc=airius, dc=com\n" +
              "objectclass: top\n" +
              "objectclass: person\n" +
              "objectclass: organizationalPerson\n" +
              "cn: Gern Jensen\n" +
              "cn: Gern O Jensen\n" +
              "sn: Jensen\n" +
              "uid: gernj\n" +
              "telephonenumber: +1 408 555 1212\n" +
              "description:: V2hhdCBhIGNhcmVmdWwgcmVhZGVyIHlvdSBhcmUhICBUaGlzIHZhbHVl\n" +
              " IGlzIGJhc2UtNjQtZW5jb2RlZCBiZWNhdXNlIGl0IGhhcyBhIGNvbnRyb2wgY2hhcmFjdG\n" +
              " VyIGluIGl0IChhIENSKS4NICBCeSB0aGUgd2F5LCB5b3Ugc2hvdWxkIHJlYWxseSBnZXQg\n" +
              " b3V0IG1vcmUu"),
          SearchResponse.builder()
            .entry(
              LdapEntry.builder()
                .dn("cn=Gern Jensen, ou=Product Testing, dc=airius, dc=com")
                .attributes(
                  LdapAttribute.builder().name("objectclass").values("top", "person", "organizationalPerson").build(),
                  LdapAttribute.builder()
                    .name("cn")
                    .values("Gern Jensen", "Gern O Jensen")
                    .build(),
                  LdapAttribute.builder().name("sn").values("Jensen").build(),
                  LdapAttribute.builder().name("uid").values("gernj").build(),
                  LdapAttribute.builder().name("telephonenumber").values("+1 408 555 1212").build(),
                  LdapAttribute.builder()
                    .name("description")
                    .values(
                      "What a careful reader you are!  This value is base-64-encoded because it has a control " +
                        "character in it (a CR)." + (char) 0x0D + "  By the way, you should really get out more.")
                    .build())
                .build())
            .build(),
        },
        new Object[] {
          new StringReader(
            "version: 1\n" +
              "dn:: b3U95Za25qWt6YOoLG89QWlyaXVz\n" +
              "# dn:: ou=<JapaneseOU>,o=Airius\n" +
              "objectclass: top\n" +
              "objectclass: organizationalUnit\n" +
              "ou:: 5Za25qWt6YOo\n" +
              "# ou:: <JapaneseOU>\n" +
              "ou;lang-ja:: 5Za25qWt6YOo\n" +
              "# ou;lang-ja:: <JapaneseOU>\n" +
              "ou;lang-ja;phonetic:: 44GI44GE44GO44KH44GG44G2\n" +
              "# ou;lang-ja:: <JapaneseOU_in_phonetic_representation>\n" +
              "ou;lang-en: Sales\n" +
              "description: Japanese office\n" +
              "\n" +
              "dn:: dWlkPXJvZ2FzYXdhcmEsb3U95Za25qWt6YOoLG89QWlyaXVz\n" +
              "# dn:: uid=<uid>,ou=<JapaneseOU>,o=Airius\n" +
              "userpassword: {SHA}O3HSv1MusyL4kTjP+HKI5uxuNoM=\n" +
              "objectclass: top\n" +
              "objectclass: person\n" +
              "objectclass: organizationalPerson\n" +
              "objectclass: inetOrgPerson\n" +
              "uid: rogasawara\n" +
              "mail: rogasawara@airius.co.jp\n" +
              "givenname;lang-ja:: 44Ot44OJ44OL44O8\n" +
              "# givenname;lang-ja:: <JapaneseGivenname>\n" +
              "sn;lang-ja:: 5bCP56yg5Y6f\n" +
              "# sn;lang-ja:: <JapaneseSn>\n" +
              "cn;lang-ja:: 5bCP56yg5Y6fIOODreODieODi+ODvA==\n" +
              "# cn;lang-ja:: <JapaneseCn>\n" +
              "title;lang-ja:: 5Za25qWt6YOoIOmDqOmVtw==\n" +
              "# title;lang-ja:: <JapaneseTitle>\n" +
              "preferredlanguage: ja\n" +
              "givenname:: 44Ot44OJ44OL44O8\n" +
              "# givenname:: <JapaneseGivenname>\n" +
              "sn:: 5bCP56yg5Y6f\n" +
              "# sn:: <JapaneseSn>\n" +
              "cn:: 5bCP56yg5Y6fIOODreODieODi+ODvA==\n" +
              "# cn:: <JapaneseCn>\n" +
              "title:: 5Za25qWt6YOoIOmDqOmVtw==\n" +
              "# title:: <JapaneseTitle>\n" +
              "givenname;lang-ja;phonetic:: 44KN44Gp44Gr44O8\n" +
              "# givenname;lang-ja;phonetic::\n" +
              " <JapaneseGivenname_in_phonetic_representation_kana>\n" +
              "sn;lang-ja;phonetic:: 44GK44GM44GV44KP44KJ\n" +
              "# sn;lang-ja;phonetic:: <JapaneseSn_in_phonetic_representation_kana>\n" +
              "cn;lang-ja;phonetic:: 44GK44GM44GV44KP44KJIOOCjeOBqeOBq+ODvA==\n" +
              "# cn;lang-ja;phonetic:: <JapaneseCn_in_phonetic_representation_kana>\n" +
              "title;lang-ja;phonetic:: 44GI44GE44GO44KH44GG44G2IOOBtuOBoeOCh+OBhg==\n" +
              "# title;lang-ja;phonetic::\n" +
              "# <JapaneseTitle_in_phonetic_representation_kana>\n" +
              "givenname;lang-en: Rodney\n" +
              "sn;lang-en: Ogasawara\n" +
              "cn;lang-en: Rodney Ogasawara\n" +
              "title;lang-en: Sales, Director"),
          SearchResponse.builder()
            .entry(
              LdapEntry.builder()
                .dn("ou=営業部,o=Airius")
                .attributes(
                  LdapAttribute.builder().name("objectclass").values("top", "organizationalUnit").build(),
                  LdapAttribute.builder().name("ou").values("営業部").build(),
                  LdapAttribute.builder().name("ou;lang-ja").values("営業部").build(),
                  LdapAttribute.builder()
                    .name("ou;lang-ja;phonetic")
                    .values("えいぎょうぶ")
                    .build(),
                  LdapAttribute.builder().name("ou;lang-en").values("Sales").build(),
                  LdapAttribute.builder().name("description").values("Japanese office").build())
                .build(),
              LdapEntry.builder()
                .dn("uid=rogasawara,ou=営業部,o=Airius")
                .attributes(
                  LdapAttribute.builder().name("userpassword").values("{SHA}O3HSv1MusyL4kTjP+HKI5uxuNoM=").build(),
                  LdapAttribute.builder()
                    .name("objectclass")
                    .values("top", "person", "organizationalPerson", "inetOrgPerson")
                    .build(),
                  LdapAttribute.builder().name("uid").values("rogasawara").build(),
                  LdapAttribute.builder().name("mail").values("rogasawara@airius.co.jp").build(),
                  LdapAttribute.builder().name("givenname;lang-ja").values("ロドニー").build(),
                  LdapAttribute.builder().name("sn;lang-ja").values("小笠原").build(),
                  LdapAttribute.builder().name("cn;lang-ja").values("小笠原 ロドニー").build(),
                  LdapAttribute.builder().name("title;lang-ja").values("営業部 部長").build(),
                  LdapAttribute.builder().name("preferredlanguage").values("ja").build(),
                  LdapAttribute.builder().name("givenname").values("ロドニー").build(),
                  LdapAttribute.builder().name("sn").values("小笠原").build(),
                  LdapAttribute.builder().name("cn").values("小笠原 ロドニー").build(),
                  LdapAttribute.builder().name("title").values("営業部 部長").build(),
                  LdapAttribute.builder()
                    .name("givenname;lang-ja;phonetic")
                    .values("ろどにー")
                    .build(),
                  LdapAttribute.builder()
                    .name("sn;lang-ja;phonetic")
                    .values("おがさわら")
                    .build(),
                  LdapAttribute.builder()
                    .name("cn;lang-ja;phonetic")
                    .values("おがさわら ろどにー")
                    .build(),
                  LdapAttribute.builder()
                    .name("title;lang-ja;phonetic")
                    .values("えいぎょうぶ ぶちょう")
                    .build(),
                  LdapAttribute.builder().name("givenname;lang-en").values("Rodney").build(),
                  LdapAttribute.builder().name("sn;lang-en").values("Ogasawara").build(),
                  LdapAttribute.builder().name("cn;lang-en").values("Rodney Ogasawara").build(),
                  LdapAttribute.builder().name("title;lang-en").values("Sales, Director").build())
                .build())
            .build(),
        },
        new Object[] {
          new StringReader(
            "dn:\n" +
              "cn: Barbara J Jensen\n" +
              "cn: Babs Jensen"),
          SearchResponse.builder()
            .entry(
              LdapEntry.builder()
                .dn("")
                .attributes(
                  LdapAttribute.builder().name("cn").values("Barbara J Jensen", "Babs Jensen").build())
                .build())
            .build(),
        },
        new Object[] {
          new StringReader(
            "dn: cn=Barbara J Jensen,dc=example,dc=com\n" +
              "cn: Barbara J Jensen\n" +
              "cn: Babs Jensen\n" +
              "objectclass: person\n" +
              "description:< classpath:/org/ldaptive/io/description.txt\n" +
              "sn: Jensen"),
          SearchResponse.builder()
            .entry(
              LdapEntry.builder()
                .dn("cn=Barbara J Jensen,dc=example,dc=com")
                .attributes(
                  LdapAttribute.builder().name("cn").values("Barbara J Jensen", "Babs Jensen").build(),
                  LdapAttribute.builder().name("objectclass").values("person").build(),
                  LdapAttribute.builder().name("description").values("An excellent person").build(),
                  LdapAttribute.builder().name("sn").values("Jensen").build())
                .build())
            .build(),
        },
        new Object[] {
          new StringReader(
            "dn: cn=Barbara J Jensen,dc=example,dc=com\n" +
              "cn: Barbara J Jensen\n" +
              "cn: Babs Jensen\n" +
              "objectclass: person\n" +
              "description:< classpath:/org/ldaptive/io/description.txt\n" +
              "sn: Jensen\n"),
          SearchResponse.builder()
            .entry(
              LdapEntry.builder()
                .dn("cn=Barbara J Jensen,dc=example,dc=com")
                .attributes(
                  LdapAttribute.builder().name("cn").values("Barbara J Jensen", "Babs Jensen").build(),
                  LdapAttribute.builder().name("objectclass").values("person").build(),
                  LdapAttribute.builder().name("description").values("An excellent person").build(),
                  LdapAttribute.builder().name("sn").values("Jensen").build())
                .build())
            .build(),
        },
        new Object[] {
          new StringReader(
            "dn: cn=Barbara J Jensen,dc=example,dc=com\n" +
            "cn: Barbara J Jensen\n" +
            "cn: Babs Jensen\n" +
            "objectclass: person\n" +
            "description:< classpath:/org/ldaptive/io/description.txt\n" +
            "sn: Jensen\n" +
            "\n" +
            "dn: cn=Bjorn J Jensen,dc=example,dc=com\n" +
            "cn: Bjorn J Jensen\n" +
            "cn: Bjorn Jensen\n" +
            "objectclass: person\n" +
            "sn: Jensen\n" +
            "\n" +
            "dn: cn=Jennifer J Jensen,dc=example,dc=com\n" +
            "cn: Jennifer J Jensen\n" +
            "cn: Jennifer Jensen\n" +
            "objectclass: person\n" +
            "sn: Jensen\n" +
            "jpegPhoto:: /9j/4AAQSkZJRgABAAAAAQABAAD/2wBDABALD\n" +
            " A4MChAODQ4SERATGCgaGBYWGDEjJR0oOjM9PDkzODdASFxOQ\n" +
            " ERXRTc4UG1RV19iZ2hnPk1xeXBkeFxlZ2P/2wBDARESEhgV\n" +
            "\n" +
            "ref: ldap://localhost:389/dc=vt,dc=edu??sub\n" +
            "\n" +
            "ref: ldap://ldap-test-2.middleware.vt.edu:10389/dc=vt,dc=edu??sub\n" +
            "\n" +
            "ref: ldap://localhost:389/dc=vt,dc=edu??sub\n" +
            "ref: ldap://ldap-test-10.middleware.vt.edu:10389/dc=vt,dc=edu??sub"),
          SearchResponse.builder()
            .entry(
              LdapEntry.builder()
                .dn("cn=Barbara J Jensen,dc=example,dc=com")
                .attributes(
                  LdapAttribute.builder().name("cn").values("Barbara J Jensen", "Babs Jensen").build(),
                  LdapAttribute.builder().name("objectclass").values("person").build(),
                  LdapAttribute.builder().name("description").values("An excellent person").build(),
                  LdapAttribute.builder().name("sn").values("Jensen").build())
                .build(),
              LdapEntry.builder()
                .dn("cn=Bjorn J Jensen,dc=example,dc=com")
                .attributes(
                  LdapAttribute.builder().name("cn").values("Bjorn J Jensen", "Bjorn Jensen").build(),
                  LdapAttribute.builder().name("objectclass").values("person").build(),
                  LdapAttribute.builder().name("sn").values("Jensen").build())
                .build(),
              LdapEntry.builder()
                .dn("cn=Jennifer J Jensen,dc=example,dc=com")
                .attributes(
                  LdapAttribute.builder().name("cn").values("Jennifer J Jensen", "Jennifer Jensen").build(),
                  LdapAttribute.builder().name("objectclass").values("person").build(),
                  LdapAttribute.builder().name("sn").values("Jensen").build(),
                  LdapAttribute.builder()
                    .name("jpegPhoto")
                    .binary(true)
                    .values("/9j/4AAQSkZJRgABAAAAAQABAAD/2wBDABALDA4MChAODQ4SERATGCgaGBYWGDEjJR0oOjM9PDkzODdASFxOQERX" +
                            "RTc4UG1RV19iZ2hnPk1xeXBkeFxlZ2P/2wBDARESEhgV").build())
                .build())
            .reference(
              SearchResultReference.builder()
                .uris("ldap://localhost:389/dc=vt,dc=edu??sub")
                .build(),
              SearchResultReference.builder()
                .uris("ldap://ldap-test-2.middleware.vt.edu:10389/dc=vt,dc=edu??sub")
                .build(),
              SearchResultReference.builder()
                .uris(
                  "ldap://localhost:389/dc=vt,dc=edu??sub",
                  "ldap://ldap-test-10.middleware.vt.edu:10389/dc=vt,dc=edu??sub")
                .build())
            .build(),
        },
      };
  }
  // CheckStyle:MethodLength ON

  /**
   * @param  actual  reader containing LDIF
   * @param  expected  search response that should be produced
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "io", dataProvider = "ldif")
  public void read(final Reader actual, final SearchResponse expected)
    throws Exception
  {
    final LdifReader reader = new LdifReader(actual);
    assertThat(reader.read()).isEqualTo(expected);
  }
}
