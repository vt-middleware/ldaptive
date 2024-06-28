/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

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
  public void keyStoreCredentialConfig()
  {
    EqualsVerifier.forClass(KeyStoreCredentialConfig.class)
      .suppress(Warning.STRICT_INHERITANCE)
      .suppress(Warning.NONFINAL_FIELDS)
      .withIgnoredFields("keyStoreReader", "immutable")
      .verify();
  }


  @Test
  public void x509CredentialConfig()
  {
    EqualsVerifier.forClass(X509CredentialConfig.class)
      .suppress(Warning.STRICT_INHERITANCE)
      .suppress(Warning.NONFINAL_FIELDS)
      .withIgnoredFields("certsReader", "certReader", "keyReader", "immutable")
      .verify();
  }
}
