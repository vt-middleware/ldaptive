/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.testng.annotations.Test;

/**
 * Unit test for objects that override equals.
 *
 * @author  Middleware Services
 */
public class EqualsTest
{


  @Test
  public void ldapCredential()
  {
    EqualsVerifier.forClass(LdapCredential.class)
      .suppress(Warning.STRICT_INHERITANCE)
      .suppress(Warning.NONFINAL_FIELDS)
      .verify();
  }


  @Test
  public void ldapGroup()
  {
    EqualsVerifier.forClass(LdapGroup.class)
      .suppress(Warning.STRICT_INHERITANCE)
      .suppress(Warning.NONFINAL_FIELDS)
      .verify();
  }


  @Test
  public void ldapRole()
  {
    EqualsVerifier.forClass(LdapRole.class)
      .suppress(Warning.STRICT_INHERITANCE)
      .suppress(Warning.NONFINAL_FIELDS)
      .verify();
  }


  @Test
  public void ldapPrincipal()
  {
    EqualsVerifier.forClass(LdapPrincipal.class)
      .suppress(Warning.STRICT_INHERITANCE)
      .suppress(Warning.NONFINAL_FIELDS)
      .withIgnoredFields("ldapEntry")
      .verify();
  }


  @Test
  public void ldapDnPrincipal()
  {
    EqualsVerifier.forClass(LdapDnPrincipal.class)
      .suppress(Warning.STRICT_INHERITANCE)
      .suppress(Warning.NONFINAL_FIELDS)
      .withIgnoredFields("ldapEntry")
      .verify();
  }
}
