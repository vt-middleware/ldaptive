/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link FormatDnResolver}.
 *
 * @author  Middleware Services
 */
public class FormatDnResolverTest
{


  @Test(groups = "auth")
  public void noFormat()
  {
    try {
      new FormatDnResolver().resolve(new User("bob"));
      fail("Should have thrown IllegalStateException");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
    }
  }


  @Test(groups = "auth")
  public void noUser()
    throws Exception
  {
    final FormatDnResolver resolver = new FormatDnResolver("uid=%s,ou=people,dc=vt,dc=edu");
    assertThat(resolver.resolve(null)).isNull();
    assertThat(resolver.resolve(new User(null))).isNull();
    assertThat(resolver.resolve(new User(""))).isNull();
  }


  @Test(groups = "auth")
  public void resolve()
    throws Exception
  {
    final FormatDnResolver resolver = new FormatDnResolver("uid=%s,ou=people,dc=vt,dc=edu");
    assertThat(resolver.resolve(new User("bob"))).isEqualTo("uid=bob,ou=people,dc=vt,dc=edu");
  }


  @Test(groups = "auth")
  public void resolveWithArgs()
    throws Exception
  {
    final FormatDnResolver resolver = new FormatDnResolver(
      "uid=%1$s,ou=%2$s,dc=%3$s,dc=%4$s", new Object[] {"people", "vt", "edu"});
    assertThat(resolver.resolve(new User("bob"))).isEqualTo("uid=bob,ou=people,dc=vt,dc=edu");
  }


  @Test(groups = "auth")
  public void resolveWithEscape()
    throws Exception
  {
    final FormatDnResolver resolver = new FormatDnResolver("uid=%s,ou=people,dc=vt,dc=edu");
    assertThat(resolver.resolve(new User("bob+"))).isEqualTo("uid=bob\\+,ou=people,dc=vt,dc=edu");
    assertThat(resolver.resolve(new User("<ali#e>"))).isEqualTo("uid=\\<ali\\#e\\>,ou=people,dc=vt,dc=edu");
  }


  @Test(groups = "auth")
  public void resolveWithoutEscape()
    throws Exception
  {
    final FormatDnResolver resolver = new FormatDnResolver("uid=%s,ou=people,dc=vt,dc=edu");
    resolver.setEscapeUser(false);
    assertThat(resolver.resolve(new User("bob+"))).isEqualTo("uid=bob+,ou=people,dc=vt,dc=edu");
    assertThat(resolver.resolve(new User("<ali#e>"))).isEqualTo("uid=<ali#e>,ou=people,dc=vt,dc=edu");
  }
}
