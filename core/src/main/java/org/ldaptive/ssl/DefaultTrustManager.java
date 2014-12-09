/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads the trust managers from the default {@link TrustManagerFactory} and
 * delegates to those.
 *
 * @author  Middleware Services
 */
public class DefaultTrustManager implements X509TrustManager
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Default trust managers. */
  private final X509TrustManager[] trustManagers;


  /** Creates a new default trust manager. */
  public DefaultTrustManager()
  {
    try {
      final TrustManagerFactory tmf = TrustManagerFactory.getInstance(
        TrustManagerFactory.getDefaultAlgorithm());
      tmf.init((KeyStore) null);

      final TrustManager[] tm = tmf.getTrustManagers();
      trustManagers = new X509TrustManager[tm.length];
      for (int i = 0; i < tm.length; i++) {
        trustManagers[i] = (X509TrustManager) tm[i];
      }
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }


  @Override
  public void checkClientTrusted(
    final X509Certificate[] chain,
    final String authType)
    throws CertificateException
  {
    for (X509TrustManager tm : trustManagers) {
      logger.trace("invoking checkClientTrusted for {}", tm);
      tm.checkClientTrusted(chain, authType);
    }
  }


  @Override
  public void checkServerTrusted(
    final X509Certificate[] chain,
    final String authType)
    throws CertificateException
  {
    for (X509TrustManager tm : trustManagers) {
      logger.trace("invoking checkServerTrusted for {}", tm);
      tm.checkServerTrusted(chain, authType);
    }
  }


  @Override
  public X509Certificate[] getAcceptedIssuers()
  {
    final List<X509Certificate> issuers = new ArrayList<>();
    if (trustManagers != null) {
      for (X509TrustManager tm : trustManagers) {
        logger.trace("invoking getAcceptedIssuers for {}", tm);
        Collections.addAll(issuers, tm.getAcceptedIssuers());
      }
    }
    return issuers.toArray(new X509Certificate[issuers.size()]);
  }
}
