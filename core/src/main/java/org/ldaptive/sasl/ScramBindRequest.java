/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

import org.ldaptive.LdapUtils;
import org.ldaptive.transport.ScramSaslClient;

/**
 * LDAP SCRAM (Salted Challenge Response Authentication Mechanism) bind request.
 *
 * @author  Middleware Services
 */
public class ScramBindRequest implements SaslClientRequest
{

  /** Mechanism. */
  private final Mechanism scramMechanism;

  /** Username. */
  private final String username;

  /** Password. */
  private final String password;

  /** Scram nonce. */
  private final byte[] scramNonce;


  /**
   * Creates a new scram bind request.
   *
   * @param  mech  SCRAM SASL mechanism
   * @param  user  to bind as
   * @param  pass  to bind with
   */
  public ScramBindRequest(final Mechanism mech, final String user, final String pass)
  {
    this(mech, user, pass, null);
  }


  /**
   * Creates a new scram bind request.
   *
   * @param  mech  SCRAM SASL mechanism
   * @param  user  to bind as
   * @param  pass  to bind with
   * @param  nonce  to use with the SCRAM protocol
   */
  public ScramBindRequest(final Mechanism mech, final String user, final String pass, final byte[] nonce)
  {
    if (mech != Mechanism.SCRAM_SHA_1 && mech != Mechanism.SCRAM_SHA_256 && mech != Mechanism.SCRAM_SHA_512) {
      throw new IllegalArgumentException("Invalid SCRAM mechanism: " + mech);
    }
    scramMechanism = mech;
    username = user;
    password = pass;
    scramNonce = LdapUtils.copyArray(nonce);
  }


  public Mechanism getMechanism()
  {
    return scramMechanism;
  }


  public String getUsername()
  {
    return username;
  }


  public String getPassword()
  {
    return password;
  }


  public byte[] getNonce()
  {
    return LdapUtils.copyArray(scramNonce);
  }


  @Override
  public ScramSaslClient getSaslClient()
  {
    return new ScramSaslClient();
  }
}

