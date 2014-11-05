/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;
import org.ldaptive.sasl.SaslConfig;

/**
 * Contains the data required to perform an ldap bind operation.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class BindRequest extends AbstractRequest
{

  /** DN to bind as before performing operations. */
  private String bindDn;

  /** Credential for the bind DN. */
  private Credential bindCredential;

  /** Configuration for SASL authentication. */
  private SaslConfig saslConfig;


  /** Default constructor. */
  public BindRequest() {}


  /**
   * Creates a new bind request.
   *
   * @param  dn  to bind as
   * @param  credential  to bind with
   */
  public BindRequest(final String dn, final Credential credential)
  {
    setDn(dn);
    setCredential(credential);
  }


  /**
   * Creates a new bind request.
   *
   * @param  dn  to bind as
   * @param  credential  to bind with
   * @param  config  sasl configuration
   */
  public BindRequest(
    final String dn,
    final Credential credential,
    final SaslConfig config)
  {
    setDn(dn);
    setCredential(credential);
    setSaslConfig(config);
  }


  /**
   * Creates a new bind request.
   *
   * @param  config  sasl configuration
   */
  public BindRequest(final SaslConfig config)
  {
    setSaslConfig(config);
  }


  /**
   * Returns the bind DN.
   *
   * @return  DN to bind as
   */
  public String getDn()
  {
    return bindDn;
  }


  /**
   * Sets the bind DN to authenticate as before performing operations.
   *
   * @param  dn  to bind as
   */
  public void setDn(final String dn)
  {
    bindDn = dn;
  }


  /**
   * Returns the credential used with the bind DN.
   *
   * @return  bind DN credential
   */
  public Credential getCredential()
  {
    return bindCredential;
  }


  /**
   * Sets the credential of the bind DN.
   *
   * @param  credential  to use with bind DN
   */
  public void setCredential(final Credential credential)
  {
    bindCredential = credential;
  }


  /**
   * Returns the sasl config.
   *
   * @return  sasl config
   */
  public SaslConfig getSaslConfig()
  {
    return saslConfig;
  }


  /**
   * Sets the sasl config.
   *
   * @param  config  sasl config
   */
  public void setSaslConfig(final SaslConfig config)
  {
    saslConfig = config;
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::bindDn=%s, saslConfig=%s, controls=%s]",
        getClass().getName(),
        hashCode(),
        bindDn,
        saslConfig,
        Arrays.toString(getControls()));
  }
}
