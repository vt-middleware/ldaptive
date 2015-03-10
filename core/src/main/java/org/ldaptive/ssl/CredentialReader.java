/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

/**
 * Reads a credential from an IO source.
 *
 * @param  <T>  Type of credential read by this instance.
 *
 * @author  Middleware Services
 */
public interface CredentialReader<T>
{


  /**
   * Reads a credential object from a path.
   *
   * @param  path  from which to read credential.
   * @param  params  Arbitrary string parameters, e.g. password, needed to read the credential.
   *
   * @return  credential read from data at path.
   *
   * @throws  IOException  On IO errors.
   * @throws  GeneralSecurityException  On errors with the credential data.
   */
  T read(String path, String... params)
    throws IOException, GeneralSecurityException;


  /**
   * Reads a credential object from an input stream.
   *
   * @param  is  input stream from which to read credential.
   * @param  params  Arbitrary string parameters, e.g. password, needed to read the credential.
   *
   * @return  credential read from data in stream.
   *
   * @throws  IOException  On IO errors.
   * @throws  GeneralSecurityException  On errors with the credential data.
   */
  T read(InputStream is, String... params)
    throws IOException, GeneralSecurityException;
}
