/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Trust manager that delegates to multiple trust managers.
 *
 * @author  Middleware Services
 */
public class AggregateTrustManager extends X509ExtendedTrustManager
{

  /** Enum to define how trust managers should be processed. */
  public enum Strategy {

    /** all trust managers must succeed. */
    ALL,

    /** any trust manager must succeed. */
    ANY
  }

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Trust managers to invoke. */
  private final X509ExtendedTrustManager[] trustManagers;

  /** Whether to require all trust managers succeed. */
  private final Strategy trustStrategy;


  /**
   * Creates a new aggregate trust manager with the ALL {@link Strategy}.
   *
   * @param  managers  to aggregate
   */
  public AggregateTrustManager(final X509TrustManager... managers)
  {
    this(Strategy.ALL, managers);
  }


  /**
   * Creates a new aggregate trust manager.
   *
   * @param  strategy  for processing trust managers
   * @param  managers  to aggregate
   */
  public AggregateTrustManager(final Strategy strategy, final X509TrustManager... managers)
  {
    if (strategy == null) {
      throw new NullPointerException("Strategy cannot be null");
    }
    trustStrategy = strategy;
    if (managers == null || managers.length == 0) {
      throw new NullPointerException("Trust managers cannot be empty or null");
    }
    trustManagers = Stream.of(managers)
      .map(tm -> {
        if (tm instanceof X509ExtendedTrustManager) {
          return (X509ExtendedTrustManager) tm;
        } else {
          return new X509ExtendedTrustManagerWrapper(tm, new DefaultHostnameVerifier());
        }
      })
      .toArray(X509ExtendedTrustManager[]::new);
  }


  /**
   * Returns the trust managers that are aggregated.
   *
   * @return  trust managers
   */
  public X509TrustManager[] getTrustManagers()
  {
    return trustManagers;
  }


  /**
   * Returns the trust strategy.
   *
   * @return  trust strategy
   */
  public Strategy getTrustStrategy()
  {
    return trustStrategy;
  }


  @Override
  public void checkClientTrusted(final X509Certificate[] chain, final String authType, final Socket socket)
    throws CertificateException
  {
    trustManagerCheck(tm -> tm.checkClientTrusted(chain, authType, socket));
  }


  @Override
  public void checkClientTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine)
    throws CertificateException
  {
    trustManagerCheck(tm -> tm.checkClientTrusted(chain, authType, engine));
  }


  @Override
  public void checkClientTrusted(final X509Certificate[] chain, final String authType)
    throws CertificateException
  {
    trustManagerCheck(tm -> tm.checkClientTrusted(chain, authType));
  }


  @Override
  public void checkServerTrusted(final X509Certificate[] chain, final String authType, final Socket socket)
    throws CertificateException
  {
    trustManagerCheck(tm -> tm.checkServerTrusted(chain, authType, socket));
  }


  @Override
  public void checkServerTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine)
    throws CertificateException
  {
    trustManagerCheck(tm -> tm.checkServerTrusted(chain, authType, engine));
  }


  @Override
  public void checkServerTrusted(final X509Certificate[] chain, final String authType)
    throws CertificateException
  {
    trustManagerCheck(tm -> tm.checkServerTrusted(chain, authType));
  }


  @Override
  public X509Certificate[] getAcceptedIssuers()
  {
    final List<X509Certificate> issuers = new ArrayList<>();
    for (X509ExtendedTrustManager tm : trustManagers) {
      Collections.addAll(issuers, tm.getAcceptedIssuers());
    }
    return issuers.toArray(new X509Certificate[0]);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "trustManagers=" + Arrays.toString(trustManagers) + ", " +
      "trustStrategy=" + trustStrategy + "]";
  }


  /**
   * Invoke the supplied consumer for each trust manager.
   *
   * @param  consumer  to invoke
   *
   * @throws  CertificateException  if trust check fails. For multiple failures the first exception is thrown
   */
  private void trustManagerCheck(final TrustManagerConsumer consumer)
    throws CertificateException
  {
    CertificateException certEx = null;
    for (X509ExtendedTrustManager tm : trustManagers) {
      try {
        consumer.checkTrusted(tm);
        logger.trace("checkServerTrusted for {} succeeded", tm);
        if (trustStrategy == Strategy.ANY) {
          return;
        }
      } catch (CertificateException e) {
        logger.trace("checkServerTrusted for {} failed", tm);
        if (trustStrategy == Strategy.ALL) {
          throw e;
        }
        if (certEx == null) {
          certEx = e;
        }
      }
    }
    if (certEx != null) {
      throw certEx;
    }
  }


  /**
   * Interface for consuming a trust manager.
   */
  private interface TrustManagerConsumer
  {


    /**
     * Invoke the trust check for the supplied trust manager.
     *
     * @param  tm  trust manager
     *
     * @throws  CertificateException  if trust check fails
     */
    void checkTrusted(X509ExtendedTrustManager tm) throws CertificateException;
  }
}
