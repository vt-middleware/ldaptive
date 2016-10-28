/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import org.ldaptive.LdapUtils;

/**
 * Provides the properties necessary for creating an SSL context initializer with a keystore credential reader.
 *
 * @author  Middleware Services
 */
public class KeyStoreCredentialConfig implements CredentialConfig
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1013;

  /** Handles loading keystores. */
  private final KeyStoreCredentialReader keyStoreReader = new KeyStoreCredentialReader();

  /** Name of the truststore to use for the SSL connection. */
  private String trustStore;

  /** Password needed to open the truststore. */
  private String trustStorePassword;

  /** Truststore type. */
  private String trustStoreType;

  /** Truststore aliases to use. */
  private String[] trustStoreAliases;

  /** Name of the keystore to use for the SSL connection. */
  private String keyStore;

  /** Password needed to open the keystore. */
  private String keyStorePassword;

  /** Keystore type. */
  private String keyStoreType;

  /** Keystore aliases to use. */
  private String[] keyStoreAliases;


  /**
   * Returns the name of the truststore to use.
   *
   * @return  truststore name
   */
  public String getTrustStore()
  {
    return trustStore;
  }


  /**
   * Sets the name of the truststore to use.
   *
   * @param  name  truststore name
   */
  public void setTrustStore(final String name)
  {
    trustStore = name;
  }


  /**
   * Returns the password for the truststore.
   *
   * @return  truststore password
   */
  public String getTrustStorePassword()
  {
    return trustStorePassword;
  }


  /**
   * Sets the password for the truststore.
   *
   * @param  password  truststore password
   */
  public void setTrustStorePassword(final String password)
  {
    trustStorePassword = password;
  }


  /**
   * Returns the type of the truststore.
   *
   * @return  truststore type
   */
  public String getTrustStoreType()
  {
    return trustStoreType;
  }


  /**
   * Sets the type of the truststore.
   *
   * @param  type  truststore type
   */
  public void setTrustStoreType(final String type)
  {
    trustStoreType = type;
  }


  /**
   * Returns the aliases of the truststore to use.
   *
   * @return  truststore aliases
   */
  public String[] getTrustStoreAliases()
  {
    return trustStoreAliases;
  }


  /**
   * Sets the aliases of the truststore to use.
   *
   * @param  aliases  truststore aliases
   */
  public void setTrustStoreAliases(final String... aliases)
  {
    trustStoreAliases = aliases;
  }


  /**
   * Returns the name of the keystore to use.
   *
   * @return  keystore name
   */
  public String getKeyStore()
  {
    return keyStore;
  }


  /**
   * Sets the name of the keystore to use.
   *
   * @param  name  keystore name
   */
  public void setKeyStore(final String name)
  {
    keyStore = name;
  }


  /**
   * Returns the password for the keystore.
   *
   * @return  keystore password
   */
  public String getKeyStorePassword()
  {
    return keyStorePassword;
  }


  /**
   * Sets the password for the keystore.
   *
   * @param  password  keystore password
   */
  public void setKeyStorePassword(final String password)
  {
    keyStorePassword = password;
  }


  /**
   * Returns the type of the keystore.
   *
   * @return  keystore type
   */
  public String getKeyStoreType()
  {
    return keyStoreType;
  }


  /**
   * Sets the type of the keystore.
   *
   * @param  type  keystore type
   */
  public void setKeyStoreType(final String type)
  {
    keyStoreType = type;
  }


  /**
   * Returns the aliases of the keystore to use.
   *
   * @return  keystore aliases
   */
  public String[] getKeyStoreAliases()
  {
    return keyStoreAliases;
  }


  /**
   * Sets the aliases of the keystore to use.
   *
   * @param  aliases  keystore aliases
   */
  public void setKeyStoreAliases(final String... aliases)
  {
    keyStoreAliases = aliases;
  }


  @Override
  public SSLContextInitializer createSSLContextInitializer()
    throws GeneralSecurityException
  {
    final KeyStoreSSLContextInitializer sslInit = new KeyStoreSSLContextInitializer();
    try {
      if (trustStore != null) {
        sslInit.setTrustKeystore(keyStoreReader.read(trustStore, trustStorePassword, trustStoreType));
        sslInit.setTrustAliases(trustStoreAliases);
      }
      if (keyStore != null) {
        sslInit.setAuthenticationKeystore(keyStoreReader.read(keyStore, keyStorePassword, keyStoreType));
        sslInit.setAuthenticationPassword(keyStorePassword != null ? keyStorePassword.toCharArray() : null);
        sslInit.setAuthenticationAliases(keyStoreAliases);
      }
    } catch (IOException e) {
      throw new GeneralSecurityException(e);
    }
    return sslInit;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof KeyStoreCredentialConfig) {
      final KeyStoreCredentialConfig v = (KeyStoreCredentialConfig) o;
      return LdapUtils.areEqual(trustStore, v.trustStore) &&
             LdapUtils.areEqual(trustStoreType, v.trustStoreType) &&
             LdapUtils.areEqual(trustStorePassword, v.trustStorePassword) &&
             LdapUtils.areEqual(trustStoreAliases, v.trustStoreAliases) &&
             LdapUtils.areEqual(keyStore, v.keyStore) &&
             LdapUtils.areEqual(keyStoreType, v.keyStoreType) &&
             LdapUtils.areEqual(keyStorePassword, v.keyStorePassword) &&
             LdapUtils.areEqual(keyStoreAliases, v.keyStoreAliases);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        trustStore,
        trustStoreType,
        trustStorePassword,
        trustStoreAliases,
        keyStore,
        keyStoreType,
        keyStorePassword,
        keyStoreAliases);
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::trustStore=%s, trustStoreType=%s, trustStoreAliases=%s, " +
        "keyStore=%s, keyStoreType=%s, keyStoreAliases=%s]",
        getClass().getName(),
        hashCode(),
        trustStore,
        trustStoreType,
        Arrays.toString(trustStoreAliases),
        keyStore,
        keyStoreType,
        Arrays.toString(keyStoreAliases));
  }
}
