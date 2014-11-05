/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Trust manager that delegates to multiple trust managers.
 *
 * @author  Middleware Services
 */
public class AggregateTrustManager implements X509TrustManager
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
  private final X509TrustManager[] trustManagers;

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
  public AggregateTrustManager(
    final Strategy strategy,
    final X509TrustManager... managers)
  {
    if (strategy == null) {
      throw new NullPointerException("Strategy cannot be null");
    }
    trustStrategy = strategy;
    trustManagers = managers;
  }


  /** {@inheritDoc} */
  @Override
  public void checkClientTrusted(
    final X509Certificate[] chain,
    final String authType)
    throws CertificateException
  {
    if (trustManagers != null) {
      CertificateException certEx = null;
      for (X509TrustManager tm : trustManagers) {
        try {
          tm.checkClientTrusted(chain, authType);
          logger.debug("checkClientTrusted for {} succeeded", tm);
          if (trustStrategy == Strategy.ANY) {
            return;
          }
        } catch (CertificateException e) {
          logger.debug("checkClientTrusted for {} failed", tm);
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
  }


  /** {@inheritDoc} */
  @Override
  public void checkServerTrusted(
    final X509Certificate[] chain,
    final String authType)
    throws CertificateException
  {
    if (trustManagers != null) {
      CertificateException certEx = null;
      for (X509TrustManager tm : trustManagers) {
        try {
          tm.checkServerTrusted(chain, authType);
          logger.debug("checkServerTrusted for {} succeeded", tm);
          if (trustStrategy == Strategy.ANY) {
            return;
          }
        } catch (CertificateException e) {
          logger.debug("checkServerTrusted for {} failed", tm);
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
  }


  /** {@inheritDoc} */
  @Override
  public X509Certificate[] getAcceptedIssuers()
  {
    final List<X509Certificate> issuers = new ArrayList<>();
    if (trustManagers != null) {
      for (X509TrustManager tm : trustManagers) {
        logger.debug("invoking getAcceptedIssuers invoked for {}", tm);
        Collections.addAll(issuers, tm.getAcceptedIssuers());
      }
    }
    return issuers.toArray(new X509Certificate[issuers.size()]);
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::trustManagers=%s, trustStrategy=%s]",
        getClass().getName(),
        hashCode(),
        Arrays.toString(trustManagers),
        trustStrategy);
  }
}
