/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import org.ldaptive.LdapUtils;

/**
 * Reads private key credentials from classpath, filepath, or stream resource.
 * Supported private key formats include: PKCS8.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class PrivateKeyCredentialReader
  extends AbstractCredentialReader<PrivateKey>
{


  /**
   * Reads a private key from an input stream.
   *
   * @param  is  Input stream from which to read private key.
   * @param  params  A single optional parameter, algorithm, may be specified.
   * The default is RSA.
   *
   * @return  Private key read from data in stream.
   *
   * @throws  IOException  On IO errors.
   * @throws  GeneralSecurityException  On errors with the credential data.
   */
  @Override
  public PrivateKey read(final InputStream is, final String... params)
    throws IOException, GeneralSecurityException
  {
    String algorithm = "RSA";
    if (params.length > 0 && params[0] != null) {
      algorithm = params[0];
    }

    final KeyFactory kf = KeyFactory.getInstance(algorithm);
    final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(
      LdapUtils.readInputStream(getBufferedInputStream(is)));
    return kf.generatePrivate(spec);
  }
}
