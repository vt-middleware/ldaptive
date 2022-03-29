/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.testng.Assert;
import org.testng.annotations.Test;

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
      Assert.fail("Should have thrown IllegalStateException");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
  }


  @Test(groups = "auth")
  public void noUser()
    throws Exception
  {
    final FormatDnResolver resolver = new FormatDnResolver("uid=%s,ou=people,dc=vt,dc=edu");
    Assert.assertNull(resolver.resolve(null));
    Assert.assertNull(resolver.resolve(new User(null)));
    Assert.assertNull(resolver.resolve(new User("")));
  }


  @Test(groups = "auth")
  public void resolve()
    throws Exception
  {
    final FormatDnResolver resolver = new FormatDnResolver("uid=%s,ou=people,dc=vt,dc=edu");
    Assert.assertEquals(resolver.resolve(new User("bob")), "uid=bob,ou=people,dc=vt,dc=edu");
  }


  @Test(groups = "auth")
  public void resolveWithArgs()
    throws Exception
  {
    final FormatDnResolver resolver = new FormatDnResolver(
      "uid=%1$s,ou=%2$s,dc=%3$s,dc=%4$s", new Object[] {"people", "vt", "edu"});
    Assert.assertEquals(resolver.resolve(new User("bob")), "uid=bob,ou=people,dc=vt,dc=edu");
  }


  @Test(groups = "auth")
  public void resolveWithEscape()
    throws Exception
  {
    final FormatDnResolver resolver = new FormatDnResolver("uid=%s,ou=people,dc=vt,dc=edu");
    Assert.assertEquals(resolver.resolve(new User("bob+")), "uid=bob\\+,ou=people,dc=vt,dc=edu");
    Assert.assertEquals(resolver.resolve(new User("<ali#e>")), "uid=\\<ali\\#e\\>,ou=people,dc=vt,dc=edu");
  }


  @Test(groups = "auth")
  public void resolveWithoutEscape()
    throws Exception
  {
    final FormatDnResolver resolver = new FormatDnResolver("uid=%s,ou=people,dc=vt,dc=edu");
    resolver.setEscapeUser(false);
    Assert.assertEquals(resolver.resolve(new User("bob+")), "uid=bob+,ou=people,dc=vt,dc=edu");
    Assert.assertEquals(resolver.resolve(new User("<ali#e>")), "uid=<ali#e>,ou=people,dc=vt,dc=edu");
  }
}
