/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;

/**
 * Provides utility methods for using a {@link KeyStore}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public final class KeyStoreUtils
{

  /** Default keystore type. */
  private static final String DEFAULT_TYPE = KeyStore.getDefaultType();


  /** Default constructor. */
  private KeyStoreUtils() {}


  /**
   * Creates a new {@link KeyStore} with the default keystore type and
   * initializes it.
   *
   * @return  initialized keystore
   *
   * @throws  GeneralSecurityException  if the keystore cannot be initialized
   */
  public static KeyStore newInstance()
    throws GeneralSecurityException
  {
    return newInstance(DEFAULT_TYPE);
  }


  /**
   * Creates a new {@link KeyStore} with the default keystore type and
   * initializes it.
   *
   * @param  password  to protect the keystore
   *
   * @return  initialized keystore
   *
   * @throws  GeneralSecurityException  if the keystore cannot be initialized
   */
  public static KeyStore newInstance(final char[] password)
    throws GeneralSecurityException
  {
    return newInstance(DEFAULT_TYPE, password);
  }


  /**
   * Creates a new {@link KeyStore} and initializes it.
   *
   * @param  type  of keystore instance
   *
   * @return  initialized keystore
   *
   * @throws  GeneralSecurityException  if the keystore cannot be initialized
   */
  public static KeyStore newInstance(final String type)
    throws GeneralSecurityException
  {
    return newInstance(type, null);
  }


  /**
   * Creates a new {@link KeyStore} and initializes it.
   *
   * @param  type  of keystore instance
   * @param  password  to protect the keystore
   *
   * @return  initialized keystore
   *
   * @throws  GeneralSecurityException  if the keystore cannot be initialized
   */
  public static KeyStore newInstance(final String type, final char[] password)
    throws GeneralSecurityException
  {
    final KeyStore.Builder builder = KeyStore.Builder.newInstance(
      type,
      null,
      new KeyStore.PasswordProtection(password));
    return builder.getKeyStore();
  }


  /**
   * Returns a keystore entry from the supplied keystore.
   *
   * @param  alias  of the entry to return
   * @param  keystore  to read the entry from
   * @param  password  to access the keystore
   *
   * @return  keystore entry
   *
   * @throws  GeneralSecurityException  if the keystore cannot be read
   * @throws  IllegalArgumentException  if the alias does not exist
   */
  public static KeyStore.Entry getEntry(
    final String alias,
    final KeyStore keystore,
    final char[] password)
    throws GeneralSecurityException
  {
    if (!keystore.containsAlias(alias)) {
      throw new IllegalArgumentException(
        "KeyStore does not contain alias " + alias);
    }
    return
      keystore.getEntry(
        alias,
        password != null ? new KeyStore.PasswordProtection(password) : null);
  }


  /**
   * Sets a keystore entry on the supplied keystore.
   *
   * @param  alias  of the supplied entry
   * @param  entry  to set
   * @param  keystore  to set the entry on
   * @param  password  to protect the entry
   *
   * @throws  GeneralSecurityException  if the keystore cannot be modified
   */
  public static void setEntry(
    final String alias,
    final KeyStore.Entry entry,
    final KeyStore keystore,
    final char[] password)
    throws GeneralSecurityException
  {
    keystore.setEntry(
      alias,
      entry,
      password != null ? new KeyStore.PasswordProtection(password) : null);
  }


  /**
   * Sets a key entry on the supplied keystore.
   *
   * @param  alias  of the supplied key
   * @param  keystore  to set the key on
   * @param  password  to protect the key
   * @param  key  to set
   * @param  certs  associated with the key
   *
   * @throws  GeneralSecurityException  if the keystore cannot be modified
   */
  public static void setKeyEntry(
    final String alias,
    final KeyStore keystore,
    final char[] password,
    final Key key,
    final Certificate... certs)
    throws GeneralSecurityException
  {
    keystore.setKeyEntry(alias, key, password, certs);
  }


  /**
   * Sets certificate entries on the supplied keystore. For certificate arrays
   * of size greater than 1, the alias is appended with an index.
   *
   * @param  alias  of the supplied certificate(s)
   * @param  keystore  to set the cert(s) on
   * @param  certs  to set
   *
   * @throws  GeneralSecurityException  if the keystore cannot be modified
   */
  public static void setCertificateEntry(
    final String alias,
    final KeyStore keystore,
    final Certificate... certs)
    throws GeneralSecurityException
  {
    if (certs != null && certs.length > 0) {
      if (certs.length == 1) {
        keystore.setCertificateEntry(alias, certs[0]);
      } else {
        for (int i = 0; i < certs.length; i++) {
          keystore.setCertificateEntry(
            String.format("%s%s", alias, i),
            certs[i]);
        }
      }
    }
  }
}
