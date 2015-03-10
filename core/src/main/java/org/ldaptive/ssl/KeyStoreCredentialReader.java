/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Arrays;

/**
 * Reads keystore credentials from a classpath, filepath, or stream resource.
 *
 * @author  Middleware Services
 */
public class KeyStoreCredentialReader extends AbstractCredentialReader<KeyStore>
{


  /**
   * Reads a keystore from an input stream.
   *
   * @param  is  Input stream from which to read keystore.
   * @param  params  Two optional parameters are supported:
   *
   *                 <ul>
   *                   <li>keystore password</li>
   *                   <li>keystore type; defaults to JVM default keystore format if omitted</li>
   *                 </ul>
   *
   *                 <p>If only a single parameter is supplied, it is assumed to be the password.</p>
   *
   * @return  keystore read from data in stream.
   *
   * @throws  IOException  On IO errors.
   * @throws  GeneralSecurityException  On errors with the credential data.
   */
  @Override
  public KeyStore read(final InputStream is, final String... params)
    throws IOException, GeneralSecurityException
  {
    char[] password = null;
    if (params.length > 0 && params[0] != null) {
      password = params[0].toCharArray();
    }

    String type = KeyStore.getDefaultType();
    if (params.length > 1 && params[1] != null) {
      type = params[1];
    }

    final KeyStore keystore = KeyStore.getInstance(type);
    if (is != null) {
      keystore.load(getBufferedInputStream(is), password);
      if (password != null) {
        Arrays.fill(password, '0');
      }
    }
    return keystore;
  }
}
