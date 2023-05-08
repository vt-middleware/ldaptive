/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transcode;

import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import org.ldaptive.LdapUtils;

/**
 * Decodes and encodes a certificate for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class CertificateValueTranscoder implements ValueTranscoder<Certificate>
{

  /** PEM cert header. */
  private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----" + System.getProperty("line.separator");

  /** PEM cert footer. */
  private static final String END_CERT = System.getProperty("line.separator") + "-----END CERTIFICATE-----";


  @Override
  public Certificate decodeStringValue(final String value)
  {
    return decodeBinaryValue(LdapUtils.utf8Encode(value));
  }


  @Override
  public Certificate decodeBinaryValue(final byte[] value)
  {
    try {
      final CertificateFactory cf = CertificateFactory.getInstance("X.509");
      return cf.generateCertificate(new ByteArrayInputStream(value));
    } catch (CertificateException e) {
      throw new IllegalArgumentException("Attribute value could not be decoded as a certificate", e);
    }
  }


  @Override
  public String encodeStringValue(final Certificate value)
  {
    return BEGIN_CERT + LdapUtils.base64Encode(encodeBinaryValue(value)) + END_CERT;
  }


  @Override
  public byte[] encodeBinaryValue(final Certificate value)
  {
    try {
      return value.getEncoded();
    } catch (CertificateEncodingException e) {
      throw new IllegalArgumentException("Certificate could not be encoded", e);
    }
  }


  @Override
  public Class<Certificate> getType()
  {
    return Certificate.class;
  }
}
