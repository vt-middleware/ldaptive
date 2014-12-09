/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Provides an SSL context initializer which can use java KeyStores to create
 * key and trust managers.
 *
 * @author  Middleware Services
 */
public class KeyStoreSSLContextInitializer extends AbstractSSLContextInitializer
{

  /** KeyStore used to create trust managers. */
  private KeyStore trustKeystore;

  /** Aliases of trust entries to use. */
  private String[] trustAliases;

  /** KeyStore used to create key managers. */
  private KeyStore authenticationKeystore;

  /** Aliases of key entries to use. */
  private String[] authenticationAliases;

  /** Password used to access the authentication keystore. */
  private char[] authenticationPassword;


  /**
   * Sets the keystore to use for creating the trust managers.
   *
   * @param  keystore  to set
   */
  public void setTrustKeystore(final KeyStore keystore)
  {
    trustKeystore = keystore;
  }


  /**
   * Sets the aliases of the entries to use in the trust keystore.
   *
   * @param  aliases  to use
   */
  public void setTrustAliases(final String... aliases)
  {
    trustAliases = aliases;
  }


  /**
   * Sets the keystore to use for creating the key managers.
   *
   * @param  keystore  to set
   */
  public void setAuthenticationKeystore(final KeyStore keystore)
  {
    authenticationKeystore = keystore;
  }


  /**
   * Sets the aliases of the entries to use in the authentication keystore.
   *
   * @param  aliases  to use
   */
  public void setAuthenticationAliases(final String... aliases)
  {
    authenticationAliases = aliases;
  }


  /**
   * Sets the password used for accessing the authentication keystore.
   *
   * @param  password  to use for authentication
   */
  public void setAuthenticationPassword(final char[] password)
  {
    authenticationPassword = password;
  }


  @Override
  protected TrustManager[] createTrustManagers()
    throws GeneralSecurityException
  {
    TrustManager[] tm = null;
    if (trustKeystore != null) {
      final TrustManagerFactory tmf = TrustManagerFactory.getInstance(
        TrustManagerFactory.getDefaultAlgorithm());
      if (trustAliases != null) {
        final KeyStore ks = KeyStoreUtils.newInstance();
        for (String alias : trustAliases) {
          final KeyStore.Entry entry = KeyStoreUtils.getEntry(
            alias,
            trustKeystore,
            null);
          KeyStoreUtils.setEntry(alias, entry, ks, null);
        }
        tmf.init(ks);
      } else {
        tmf.init(trustKeystore);
      }
      tm = tmf.getTrustManagers();
    }
    return tm;
  }


  @Override
  public KeyManager[] getKeyManagers()
    throws GeneralSecurityException
  {
    KeyManager[] km = null;
    if (authenticationKeystore != null && authenticationPassword != null) {
      final KeyManagerFactory kmf = KeyManagerFactory.getInstance(
        KeyManagerFactory.getDefaultAlgorithm());
      if (authenticationAliases != null) {
        final KeyStore ks = KeyStoreUtils.newInstance(authenticationPassword);
        for (String alias : authenticationAliases) {
          final KeyStore.Entry entry = KeyStoreUtils.getEntry(
            alias,
            authenticationKeystore,
            authenticationPassword);
          KeyStoreUtils.setEntry(alias, entry, ks, authenticationPassword);
        }
        kmf.init(ks, authenticationPassword);
      } else {
        kmf.init(authenticationKeystore, authenticationPassword);
      }
      km = kmf.getKeyManagers();
    }
    return km;
  }
}
