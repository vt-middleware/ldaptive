/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Arrays;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Provides an SSL context initializer which can use java KeyStores to create key and trust managers.
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
   * Returns the keystore to use for creating the trust managers.
   *
   * @return  keystore
   */
  public KeyStore getTrustKeystore()
  {
    return trustKeystore;
  }


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
   * Returns the aliases of the entries to use in the trust keystore
   *
   * @return  trust aliases
   */
  public String[] getTrustAliases()
  {
    return trustAliases;
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
   * Returns the keystore to use for creating the key managers.
   *
   * @return  keystore
   */
  public KeyStore getAuthenticationKeystore()
  {
    return authenticationKeystore;
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
   * Returns the aliases of the entries to use in the authentication keystore
   *
   * @return  authentication aliases
   */
  public String[] getAuthenticationAliases()
  {
    return authenticationAliases;
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
   * Returns the password used for accessing the authentication keystore.
   *
   * @return  authentication password
   */
  public char[] getAuthenticationPassword()
  {
    return authenticationPassword;
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
      final TrustManagerFactory tmf = getTrustManagerFactory(trustKeystore, trustAliases);
      tm = tmf.getTrustManagers();
    }
    return tm;
  }


  /**
   * Creates a new trust manager factory.
   *
   * @param  keystore  to initialize the trust manager factory
   * @param  aliases  to include from the supplied keystore or null to include all entries
   *
   * @return  trust manager factory
   *
   * @throws  GeneralSecurityException  if the trust manager factory cannot be initialized
   */
  protected TrustManagerFactory getTrustManagerFactory(final KeyStore keystore, final String... aliases)
    throws GeneralSecurityException
  {
    final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    if (aliases != null && aliases.length > 0) {
      final KeyStore ks = KeyStoreUtils.newInstance();
      for (String alias : aliases) {
        final KeyStore.Entry entry = KeyStoreUtils.getEntry(alias, keystore, null);
        KeyStoreUtils.setEntry(alias, entry, ks, null);
      }
      tmf.init(ks);
    } else {
      tmf.init(keystore);
    }
    return tmf;
  }


  @Override
  public KeyManager[] getKeyManagers()
    throws GeneralSecurityException
  {
    KeyManager[] km = null;
    if (authenticationKeystore != null && authenticationPassword != null) {
      final KeyManagerFactory kmf = getKeyManagerFactory(
        authenticationKeystore,
        authenticationPassword,
        authenticationAliases);
      km = kmf.getKeyManagers();
    }
    return km;
  }


  /**
   * Creates a new key manager factory.
   *
   * @param  keystore  to initialize the key manager factory
   * @param  password  to unlock the supplied keystore
   * @param  aliases  to include from the supplied keystore or null to include all entries
   *
   * @return  key manager factory
   *
   * @throws  GeneralSecurityException  if the key manager factory cannot be initialized
   */
  protected KeyManagerFactory getKeyManagerFactory(
    final KeyStore keystore,
    final char[] password,
    final String... aliases)
    throws GeneralSecurityException
  {
    final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    if (aliases != null && aliases.length > 0) {
      final KeyStore ks = KeyStoreUtils.newInstance(password);
      for (String alias : aliases) {
        final KeyStore.Entry entry = KeyStoreUtils.getEntry(alias, keystore, password);
        KeyStoreUtils.setEntry(alias, entry, ks, password);
      }
      kmf.init(ks, password);
    } else {
      kmf.init(keystore, password);
    }
    return kmf;
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("trustManagers=").append(Arrays.toString(trustManagers)).append(", ")
      .append("trustKeystore=").append(trustKeystore).append(", ")
      .append("trustAliases=").append(Arrays.toString(trustAliases)).append(", ")
      .append("authenticationKeystore=").append(authenticationKeystore).append(", ")
      .append("authenticationAliases=").append(Arrays.toString(authenticationAliases)).append("]").toString();
  }
}
