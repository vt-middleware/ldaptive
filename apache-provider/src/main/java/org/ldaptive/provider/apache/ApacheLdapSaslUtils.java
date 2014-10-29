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
package org.ldaptive.provider.apache;

import javax.security.auth.login.Configuration;
import org.apache.directory.api.ldap.model.constants.SaslQoP;
import org.apache.directory.api.ldap.model.constants.SaslSecurityStrength;
import org.apache.directory.ldap.client.api.SaslCramMd5Request;
import org.apache.directory.ldap.client.api.SaslDigestMd5Request;
import org.apache.directory.ldap.client.api.SaslGssApiRequest;
import org.ldaptive.Credential;
import org.ldaptive.sasl.DigestMd5Config;
import org.ldaptive.sasl.GssApiConfig;
import org.ldaptive.sasl.QualityOfProtection;
import org.ldaptive.sasl.SaslConfig;
import org.ldaptive.sasl.SecurityStrength;

/**
 * Support for SASL authentication.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public final class ApacheLdapSaslUtils
{


  /** Default constructor. */
  private ApacheLdapSaslUtils() {}


  /**
   * Creates a new digest md5 request.
   *
   * @param  username  to bind as
   * @param  credential  to bind with
   * @param  config  to set sasl parameters
   *
   * @return  digest md5 request
   */
  protected static SaslDigestMd5Request createDigestMd5Request(
    final String username,
    final Credential credential,
    final SaslConfig config)
  {
    final SaslDigestMd5Request request = new SaslDigestMd5Request();
    if (username != null) {
      request.setUsername(username);
    }
    if (credential != null) {
      request.setCredentials(credential.getBytes());
    }
    if (config.getAuthorizationId() != null &&
        !"".equals(config.getAuthorizationId())) {
      request.setAuthorizationId(config.getAuthorizationId());
    }
    if (config.getMutualAuthentication() != null) {
      request.setMutualAuthentication(config.getMutualAuthentication());
    }
    if (config.getQualityOfProtection() != null) {
      request.setQualityOfProtection(
        getQualityOfProtection(config.getQualityOfProtection()));
    }
    if (config.getSecurityStrength() != null) {
      request.setSecurityStrength(
        getSecurityStrength(config.getSecurityStrength()));
    }
    if (config instanceof DigestMd5Config) {
      final DigestMd5Config c = (DigestMd5Config) config;
      if (c.getRealm() != null) {
        request.setRealmName(c.getRealm());
      }
    }
    return request;
  }


  /**
   * Creates a new cram md5 request.
   *
   * @param  username  to bind as
   * @param  credential  to bind with
   * @param  config  to set sasl parameters
   *
   * @return  cram md5 request
   */
  protected static SaslCramMd5Request createCramMd5Request(
    final String username,
    final Credential credential,
    final SaslConfig config)
  {
    final SaslCramMd5Request request = new SaslCramMd5Request();
    if (username != null) {
      request.setUsername(username);
    }
    if (credential != null) {
      request.setCredentials(credential.getBytes());
    }
    if (config.getAuthorizationId() != null &&
        !"".equals(config.getAuthorizationId())) {
      request.setAuthorizationId(config.getAuthorizationId());
    }
    if (config.getMutualAuthentication() != null) {
      request.setMutualAuthentication(config.getMutualAuthentication());
    }
    if (config.getQualityOfProtection() != null) {
      request.setQualityOfProtection(
        getQualityOfProtection(config.getQualityOfProtection()));
    }
    if (config.getSecurityStrength() != null) {
      request.setSecurityStrength(
        getSecurityStrength(config.getSecurityStrength()));
    }
    return request;
  }


  /**
   * Creates a new gssapi request.
   *
   * @param  username  to bind as
   * @param  credential  to bind with
   * @param  config  to set sasl parameters
   *
   * @return  gssapi request
   */
  protected static SaslGssApiRequest createGssApiRequest(
    final String username,
    final Credential credential,
    final SaslConfig config)
  {
    final SaslGssApiRequest request = new SaslGssApiRequest();
    if (username != null) {
      request.setUsername(username);
    }
    if (credential != null) {
      request.setCredentials(credential.getBytes());
    }
    if (config.getAuthorizationId() != null) {
      request.setAuthorizationId(config.getAuthorizationId());
    }
    if (config.getMutualAuthentication() != null) {
      request.setMutualAuthentication(config.getMutualAuthentication());
    }
    if (config.getQualityOfProtection() != null) {
      request.setQualityOfProtection(
        getQualityOfProtection(config.getQualityOfProtection()));
    }
    if (config.getSecurityStrength() != null) {
      request.setSecurityStrength(
        getSecurityStrength(config.getSecurityStrength()));
    }
    if (config instanceof GssApiConfig) {
      final GssApiConfig c = (GssApiConfig) config;
      if (c.getRealm() != null) {
        request.setRealmName(c.getRealm());
      }
    }

    final String realm = System.getProperty("java.security.krb5.realm");
    if (realm != null) {
      request.setRealmName(realm);
    }

    final String kdcHost = System.getProperty("java.security.krb5.kdc");
    if (kdcHost != null) {
      request.setKdcHost(kdcHost);
    }

    final String loginConfig = System.getProperty(
      "java.security.auth.login.config");
    if (loginConfig != null) {
      request.setLoginModuleConfiguration(Configuration.getConfiguration());
    }
    request.setLoginContextName("com.sun.security.jgss.initiate");
    return request;
  }


  /**
   * Returns the SASL quality of protection string for the supplied enum.
   *
   * @param  qop  quality of protection enum
   *
   * @return  SASL quality of protection
   */
  protected static SaslQoP getQualityOfProtection(final QualityOfProtection qop)
  {
    SaslQoP e;
    switch (qop) {

    case AUTH:
      e = SaslQoP.AUTH;
      break;

    case AUTH_INT:
      e = SaslQoP.AUTH_INT;
      break;

    case AUTH_CONF:
      e = SaslQoP.AUTH_CONF;
      break;

    default:
      throw new IllegalArgumentException(
        "Unknown SASL quality of protection: " + qop);
    }
    return e;
  }


  /**
   * Returns the SASL security strength string for the supplied enum.
   *
   * @param  ss  security strength enum
   *
   * @return  SASL security strength
   */
  protected static SaslSecurityStrength getSecurityStrength(
    final SecurityStrength ss)
  {
    SaslSecurityStrength e;
    switch (ss) {

    case HIGH:
      e = SaslSecurityStrength.HIGH;
      break;

    case MEDIUM:
      e = SaslSecurityStrength.MEDIUM;
      break;

    case LOW:
      e = SaslSecurityStrength.LOW;
      break;

    default:
      throw new IllegalArgumentException(
        "Unknown SASL security strength: " + ss);
    }
    return e;
  }
}
