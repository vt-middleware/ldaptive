/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.util.Arrays;
import org.ldaptive.AbstractConfig;

/**
 * Contains all the configuration data for hostname verification.
 *
 * @author  Middleware Services
 */
public class HostnameVerifierConfig extends AbstractConfig
{

  /** Certificate hostname verifier. */
  private CertificateHostnameVerifier certificateHostnameVerifier;

  /** Hostnames to verify. */
  private String[] hostnames;


  /** Default constructor. */
  public HostnameVerifierConfig() {}


  /**
   * Creates a new hostname verifier config.
   *
   * @param  verifier  certificate hostname verifier
   * @param  names  hostnames
   */
  public HostnameVerifierConfig(final CertificateHostnameVerifier verifier, final String... names)
  {
    certificateHostnameVerifier = verifier;
    hostnames = names;
  }


  /**
   * Returns the certificate hostname verifier.
   *
   * @return  certificate hostname verifier
   */
  public CertificateHostnameVerifier getCertificateHostnameVerifier()
  {
    return certificateHostnameVerifier;
  }


  /**
   * Sets the certificate hostname verifier.
   *
   * @param  verifier  certificate hostname verifier
   */
  public void setCertificateHostnameVerifier(final CertificateHostnameVerifier verifier)
  {
    checkImmutable();
    logger.trace("setting certificateHostnameVerifier: {}", verifier);
    certificateHostnameVerifier = verifier;
  }


  /**
   * Returns the hostnames to verify.
   *
   * @return  hostnames
   */
  public String[] getHostnames()
  {
    return hostnames;
  }


  /**
   * Sets the hostnames to verify.
   *
   * @param  names  hostnames
   */
  public void setHostnames(final String... names)
  {
    checkImmutable();
    logger.trace("setting hostnames: {}", Arrays.toString(names));
    hostnames = names;
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::certificateHostnameVerifier=%s, hostnames=%s]",
        getClass().getName(),
        hashCode(),
        certificateHostnameVerifier,
        Arrays.toString(hostnames));
  }
}
