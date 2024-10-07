/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

  /** Maximum number of certificates to log. */
  private static final int DEFAULT_CHAIN_LOG_DEPTH = 3;

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
    Objects.requireNonNull(strategy, "Strategy cannot be null");
    trustStrategy = strategy;
    if (managers == null || managers.length == 0) {
      throw new IllegalArgumentException("Trust managers cannot be empty or null");
    }
    for (X509TrustManager tm : managers) {
      if (tm.getAcceptedIssuers() == null) {
        throw new IllegalArgumentException("Trust manager " + tm + " cannot return null accepted issuers");
      }
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
    try {
      trustManagerCheck(tm -> tm.checkClientTrusted(chain, authType, socket));
    } catch (CertificateException e) {
      throw new CertificateException(createCertificateExceptionMessage(chain), e);
    }
  }


  @Override
  public void checkClientTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine)
    throws CertificateException
  {
    try {
      trustManagerCheck(tm -> tm.checkClientTrusted(chain, authType, engine));
    } catch (CertificateException e) {
      throw new CertificateException(createCertificateExceptionMessage(chain), e);
    }
  }


  @Override
  public void checkClientTrusted(final X509Certificate[] chain, final String authType)
    throws CertificateException
  {
    try {
      trustManagerCheck(tm -> tm.checkClientTrusted(chain, authType));
    } catch (CertificateException e) {
      throw new CertificateException(createCertificateExceptionMessage(chain), e);
    }
  }


  @Override
  public void checkServerTrusted(final X509Certificate[] chain, final String authType, final Socket socket)
    throws CertificateException
  {
    try {
      trustManagerCheck(tm -> tm.checkServerTrusted(chain, authType, socket));
    } catch (CertificateException e) {
      throw new CertificateException(createCertificateExceptionMessage(chain), e);
    }
  }


  @Override
  public void checkServerTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine)
    throws CertificateException
  {
    try {
      trustManagerCheck(tm -> tm.checkServerTrusted(chain, authType, engine));
    } catch (CertificateException e) {
      throw new CertificateException(createCertificateExceptionMessage(chain), e);
    }
  }


  @Override
  public void checkServerTrusted(final X509Certificate[] chain, final String authType)
    throws CertificateException
  {
    try {
      trustManagerCheck(tm -> tm.checkServerTrusted(chain, authType));
    } catch (CertificateException e) {
      throw new CertificateException(createCertificateExceptionMessage(chain), e);
    }
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
   * Creates an exception message for the supplied certificate chain.
   *
   * @param  chain  to create message for
   *
   * @return  string representation of certificate chain
   */
  protected String createCertificateExceptionMessage(final X509Certificate[] chain)
  {
    final X509Certificate[] issuers = getAcceptedIssuers();
    if (chain == null) {
      return "Trust check failed with null chain";
    } else if (issuers == null) {
      return "Trust check failed with null trust anchors";
    } else {
      return "Trust check failed for chain [" +
        certsToString(chain, true) + "] using trust anchors [" + certsToString(issuers, false) + "]";
    }
  }


  /**
   * Returns a simple string representation of the supplied certificate chain.
   *
   * @param  chain  to log
   * @param  withIssuer  whether to include the certificate issuer
   *
   * @return  string representation of certificate chain
   */
  private String certsToString(final X509Certificate[] chain, final boolean withIssuer)
  {
    final String s = IntStream.range(0, Math.min(chain.length, DEFAULT_CHAIN_LOG_DEPTH))
      .mapToObj(i -> i + "=" + certToString(chain[i], withIssuer))
      .collect(Collectors.joining(", "));
    return chain.length > DEFAULT_CHAIN_LOG_DEPTH ? s + ", ..." : s;
  }


  /**
   * Returns a simple string representation of the supplied certificate.
   *
   * @param  cert  to convert to string format
   * @param  withIssuer  whether to include the certificate issuer
   *
   * @return  string representation of the certificate
   */
  private String certToString(final X509Certificate cert, final boolean withIssuer)
  {
    final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault(Locale.Category.FORMAT));
    final StringBuilder sb = new StringBuilder("{s:");
    if (cert.getSubjectX500Principal() != null) {
      sb.append(cert.getSubjectX500Principal().getName());
    } else {
      sb.append("null");
    }
    sb.append(", ");
    if (withIssuer) {
      sb.append("i:");
      if (cert.getIssuerX500Principal() != null) {
        sb.append(cert.getIssuerX500Principal().getName());
      } else {
        sb.append("null");
      }
      sb.append(", ");
    }
    sb.append("e:");
    if (cert.getNotAfter() != null) {
      sb.append(df.format(cert.getNotAfter()));
    } else {
      sb.append("null");
    }
    sb.append("}");
    return sb.toString();
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
