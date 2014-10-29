/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive.sasl;

/**
 * Contains all the configuration data for SASL Digest-MD5 authentication.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class DigestMd5Config extends SaslConfig
{

  /** sasl realm. */
  private String saslRealm;


  /** Default constructor. */
  public DigestMd5Config()
  {
    setMechanism(Mechanism.DIGEST_MD5);
  }


  /**
   * Returns the sasl realm.
   *
   * @return  realm
   */
  public String getRealm()
  {
    return saslRealm;
  }


  /**
   * Sets the sasl realm.
   *
   * @param  realm  to set
   */
  public void setRealm(final String realm)
  {
    checkImmutable();
    logger.trace("setting realm: {}", realm);
    saslRealm = realm;
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::mechanism=%s, authorizationId=%s, mutualAuthentication=%s, " +
        "qualityOfProtection=%s, securityStrength=%s, realm=%s]",
        getClass().getName(),
        hashCode(),
        getMechanism(),
        getAuthorizationId(),
        getMutualAuthentication(),
        getQualityOfProtection(),
        getSecurityStrength(),
        saslRealm);
  }
}
