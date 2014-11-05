/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.DN;
import org.ldaptive.asn1.RDN;
import org.ldaptive.io.StringValueTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hostname verifier that provides an implementation similar to what occurs with
 * JNDI startTLS. Verification occurs in the following order:
 *
 * <ul>
 *   <li>if hostname is IP, then cert must have exact match IP subjAltName</li>
 *   <li>hostname must match any DNS subjAltName if any exist</li>
 *   <li>hostname must match the first CN</li>
 *   <li>if cert begins with a wildcard, domains are used for matching</li>
 * </ul>
 *
 * @author  Middleware Services
 */
public class DefaultHostnameVerifier
  implements HostnameVerifier, CertificateHostnameVerifier
{

  /** Enum for subject alt name types. */
  private enum SubjectAltNameType {

    /** other name (0). */
    OTHER_NAME,

    /** ref822 name (1). */
    RFC822_NAME,

    /** dns name (2). */
    DNS_NAME,

    /** x400 address (3). */
    X400_ADDRESS,

    /** directory name (4). */
    DIRECTORY_NAME,

    /** edi party name (5). */
    EDI_PARTY_NAME,

    /** uniform resource identifier (6). */
    UNIFORM_RESOURCE_IDENTIFIER,

    /** ip address (7). */
    IP_ADDRESS,

    /** registered id (8). */
    REGISTERED_ID
  }

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());


  /** {@inheritDoc} */
  @Override
  public boolean verify(final String hostname, final SSLSession session)
  {
    boolean b = false;
    try {
      String name = null;
      if (hostname != null) {
        // if IPv6 strip off the "[]"
        if (hostname.startsWith("[") && hostname.endsWith("]")) {
          name = hostname.substring(1, hostname.length() - 1).trim();
        } else {
          name = hostname.trim();
        }
      }
      b = verify(name, (X509Certificate) session.getPeerCertificates()[0]);
    } catch (SSLPeerUnverifiedException e) {
      logger.warn("Could not get certificate from the SSL session", e);
    }
    return b;
  }


  /**
   * Verify if the hostname is an IP address using {@link
   * LdapUtils#isIPAddress(String)}. Delegates to {@link #verifyIP(String,
   * X509Certificate)} and {@link #verifyDNS(String, X509Certificate)}
   * accordingly.
   *
   * @param  hostname  to verify
   * @param  cert  to verify hostname against
   *
   * @return  whether hostname is valid for the supplied certificate
   */
  @Override
  public boolean verify(final String hostname, final X509Certificate cert)
  {
    logger.debug(
      "verifying hostname={} against cert={}",
      hostname,
      cert.getSubjectX500Principal().toString());

    boolean b;
    if (LdapUtils.isIPAddress(hostname)) {
      b = verifyIP(hostname, cert);
    } else {
      b = verifyDNS(hostname, cert);
    }
    return b;
  }


  /**
   * Verify the certificate allows use of the supplied IP address.
   *
   * <p>From RFC2818: In some cases, the URI is specified as an IP address
   * rather than a hostname. In this case, the iPAddress subjectAltName must be
   * present in the certificate and must exactly match the IP in the URI.</p>
   *
   * @param  ip  address to match in the certificate
   * @param  cert  to inspect for the IP address
   *
   * @return  whether the ip matched a subject alt name
   */
  protected boolean verifyIP(final String ip, final X509Certificate cert)
  {
    final String[] subjAltNames = getSubjectAltNames(
      cert,
      SubjectAltNameType.IP_ADDRESS);
    logger.debug(
      "verifyIP using subjectAltNames={}",
      Arrays.toString(subjAltNames));
    for (String name : subjAltNames) {
      if (ip.equalsIgnoreCase(name)) {
        logger.debug("verifyIP found hostname match: {}", name);
        return true;
      }
    }
    return false;
  }


  /**
   * Verify the certificate allows use of the supplied DNS name. Note that only
   * the first CN is used.
   *
   * <p>From RFC2818: If a subjectAltName extension of type dNSName is present,
   * that MUST be used as the identity. Otherwise, the (most specific) Common
   * Name field in the Subject field of the certificate MUST be used. Although
   * the use of the Common Name is existing practice, it is deprecated and
   * Certification Authorities are encouraged to use the dNSName instead.</p>
   *
   * <p>Matching is performed using the matching rules specified by [RFC2459].
   * If more than one identity of a given type is present in the certificate
   * (e.g., more than one dNSName name, a match in any one of the set is
   * considered acceptable.)</p>
   *
   * @param  hostname  to match in the certificate
   * @param  cert  to inspect for the hostname
   *
   * @return  whether the hostname matched a subject alt name or CN
   */
  protected boolean verifyDNS(final String hostname, final X509Certificate cert)
  {
    boolean verified = false;
    final String[] subjAltNames = getSubjectAltNames(
      cert,
      SubjectAltNameType.DNS_NAME);
    logger.debug(
      "verifyDNS using subjectAltNames={}",
      Arrays.toString(subjAltNames));
    if (subjAltNames.length > 0) {
      // if subject alt names exist, one must match
      for (String name : subjAltNames) {
        if (isMatch(hostname, name)) {
          logger.debug("verifyDNS found hostname match: {}", name);
          verified = true;
          break;
        }
      }
    } else {
      final String[] cns = getCNs(cert);
      logger.debug("verifyDNS using CN={}", Arrays.toString(cns));
      if (cns.length > 0) {
        // the most specific CN refers to the last CN
        if (isMatch(hostname, cns[cns.length - 1])) {
          logger.debug(
            "verifyDNS found hostname match: {}",
            cns[cns.length - 1]);
          verified = true;
        }
      }
    }
    return verified;
  }


  /**
   * Returns the subject alternative names matching the supplied name type from
   * the supplied certificate.
   *
   * @param  cert  to get subject alt names from
   * @param  type  subject alt name type
   *
   * @return  subject alt names
   */
  private String[] getSubjectAltNames(
    final X509Certificate cert,
    final SubjectAltNameType type)
  {
    final List<String> names = new ArrayList<>();
    try {
      final Collection<List<?>> subjAltNames =
        cert.getSubjectAlternativeNames();
      if (subjAltNames != null) {
        for (List<?> generalName : subjAltNames) {
          final Integer nameType = (Integer) generalName.get(0);
          if (nameType == type.ordinal()) {
            names.add((String) generalName.get(1));
          }
        }
      }
    } catch (CertificateParsingException e) {
      logger.warn("Error reading subject alt names from certificate", e);
    }
    return names.toArray(new String[names.size()]);
  }


  /**
   * Returns the CNs from the supplied certificate.
   *
   * @param  cert  to get CNs from
   *
   * @return  CNs
   */
  private String[] getCNs(final X509Certificate cert)
  {
    final List<String> names = new ArrayList<>();
    final byte[] encodedDn = cert.getSubjectX500Principal().getEncoded();
    if (encodedDn != null && encodedDn.length > 0) {
      final DN dn = DN.decode(ByteBuffer.wrap(encodedDn));
      for (RDN rdn : dn.getRDNs()) {
        // for multi value RDNs the first value is used
        final String value = rdn.getAttributeValue(
          "2.5.4.3",
          new StringValueTranscoder());
        if (value != null) {
          names.add(value);
        }
      }
    }
    return names.toArray(new String[names.size()]);
  }


  /**
   * Determines if the supplied hostname matches a name derived from the
   * certificate. If the certificate name starts with '*', the domain components
   * after the first '.' in each name are compared.
   *
   * @param  hostname  to match
   * @param  certName  to match
   *
   * @return  whether the hostname matched the cert name
   */
  private boolean isMatch(final String hostname, final String certName)
  {
    // must start with '*' and contain two domain components
    final boolean isWildcard = certName.startsWith("*.") &&
      certName.indexOf('.') < certName.lastIndexOf('.');
    logger.trace(
      "matching for hostname={}, certName={}, isWildcard={}",
      new Object[] {hostname, certName, isWildcard});

    boolean match;
    if (isWildcard) {
      final String certNameDomain = certName.substring(certName.indexOf("."));

      final int hostnameIdx = hostname.contains(".") ? hostname.indexOf(".")
                                                     : hostname.length();
      final String hostnameDomain = hostname.substring(hostnameIdx);

      match = certNameDomain.equalsIgnoreCase(hostnameDomain);
      logger.trace(
        "match={} for {} == {}",
        new Object[] {match, certNameDomain, hostnameDomain});
    } else {
      match = certName.equalsIgnoreCase(hostname);
      logger.trace(
        "match={} for {} == {}",
        new Object[] {match, certName, hostname});
    }
    return match;
  }


  /** Socket factory that uses {@link DefaultHostnameVerifier}. */
  public static class SSLSocketFactory extends TLSSocketFactory
  {


    /** Creates a new socket factory that uses this hostname verifier. */
    public SSLSocketFactory()
    {
      setHostnameVerifier(new DefaultHostnameVerifier());
    }


    /**
     * Returns the default SSL socket factory.
     *
     * @return  socket factory
     */
    public static SocketFactory getDefault()
    {
      final SSLSocketFactory sf = new SSLSocketFactory();
      try {
        sf.initialize();
      } catch (GeneralSecurityException e) {
        LoggerFactory.getLogger(SSLSocketFactory.class).error(
          "Error initializing socket factory",
          e);
      }
      return sf;
    }
  }
}
