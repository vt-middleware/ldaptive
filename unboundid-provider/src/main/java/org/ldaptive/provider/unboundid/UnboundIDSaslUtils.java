/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.unboundid;

import com.unboundid.ldap.sdk.DIGESTMD5BindRequestProperties;
import com.unboundid.ldap.sdk.GSSAPIBindRequestProperties;
import com.unboundid.ldap.sdk.SASLQualityOfProtection;
import org.ldaptive.Credential;
import org.ldaptive.sasl.DigestMd5Config;
import org.ldaptive.sasl.GssApiConfig;
import org.ldaptive.sasl.QualityOfProtection;
import org.ldaptive.sasl.SaslConfig;

/**
 * Support for SASL authentication.
 *
 * @author  Middleware Services
 */
public final class UnboundIDSaslUtils
{


  /** Default constructor. */
  private UnboundIDSaslUtils() {}


  /**
   * Creates a new digest md5 request properties.
   *
   * @param  username  to bind as
   * @param  credential  to bind with
   * @param  config  to set sasl parameters
   *
   * @return  digest md5 request properties
   */
  protected static DIGESTMD5BindRequestProperties createDigestMd5Properties(
    final String username,
    final Credential credential,
    final SaslConfig config)
  {
    final DIGESTMD5BindRequestProperties props =
      new DIGESTMD5BindRequestProperties(
        username,
        credential != null ? credential.getBytes() : null);
    if (config.getAuthorizationId() != null &&
        !"".equals(config.getAuthorizationId())) {
      props.setAuthorizationID(config.getAuthorizationId());
    }
    if (config.getQualityOfProtection() != null) {
      props.setAllowedQoP(
        getQualityOfProtection(config.getQualityOfProtection()));
    }
    if (config instanceof DigestMd5Config) {
      final DigestMd5Config c = (DigestMd5Config) config;
      if (c.getRealm() != null) {
        props.setRealm(c.getRealm());
      }
    }
    return props;
  }


  /**
   * Creates a new gssapi request properties.
   *
   * @param  username  to bind as
   * @param  credential  to bind with
   * @param  config  to set sasl parameters
   *
   * @return  gssapi request properties
   */
  protected static GSSAPIBindRequestProperties createGssApiProperties(
    final String username,
    final Credential credential,
    final SaslConfig config)
  {
    final GSSAPIBindRequestProperties props = new GSSAPIBindRequestProperties(
      username,
      credential != null ? credential.getBytes() : null);
    if (config.getAuthorizationId() != null) {
      props.setAuthorizationID(config.getAuthorizationId());
    }
    if (config.getQualityOfProtection() != null) {
      props.setAllowedQoP(
        getQualityOfProtection(config.getQualityOfProtection()));
    }
    if (config instanceof GssApiConfig) {
      final GssApiConfig c = (GssApiConfig) config;
      if (c.getRealm() != null) {
        props.setRealm(c.getRealm());
      }
    }
    return props;
  }


  /**
   * Returns the SASL quality of protection string for the supplied enum.
   *
   * @param  qop  quality of protection enum
   *
   * @return  SASL quality of protection
   */
  protected static SASLQualityOfProtection getQualityOfProtection(
    final QualityOfProtection qop)
  {
    SASLQualityOfProtection e;
    switch (qop) {

    case AUTH:
      e = SASLQualityOfProtection.AUTH;
      break;

    case AUTH_INT:
      e = SASLQualityOfProtection.AUTH_INT;
      break;

    case AUTH_CONF:
      e = SASLQualityOfProtection.AUTH_CONF;
      break;

    default:
      throw new IllegalArgumentException(
        "Unknown SASL quality of protection: " + qop);
    }
    return e;
  }
}
