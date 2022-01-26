/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.ldaptive.LdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides common implementation for SSL context initializer.
 *
 * @author  Middleware Services
 */
public abstract class AbstractSSLContextInitializer implements SSLContextInitializer
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Trust managers. */
  protected TrustManager[] trustManagers;


  @Override
  public TrustManager[] getTrustManagers()
    throws GeneralSecurityException
  {
    final TrustManager[] tm = createTrustManagers();
    TrustManager[] aggregate = null;
    if (tm == null) {
      if (trustManagers != null) {
        aggregate = aggregateTrustManagers(trustManagers);
      }
    } else {
      aggregate = aggregateTrustManagers(LdapUtils.concatArrays(tm, trustManagers));
    }
    return aggregate;
  }


  @Override
  public void setTrustManagers(final TrustManager... managers)
  {
    trustManagers = managers;
  }


  /**
   * Creates any trust managers specific to this context initializer.
   *
   * @return  trust managers
   *
   * @throws  GeneralSecurityException  if an errors occurs while loading the TrustManagers
   */
  protected abstract TrustManager[] createTrustManagers()
    throws GeneralSecurityException;


  @Override
  public SSLContext initSSLContext(final String protocol)
    throws GeneralSecurityException
  {
    final KeyManager[] km = getKeyManagers();
    final TrustManager[] tm = getTrustManagers();
    logger.trace(
      "initialize SSLContext with keyManagers={} and trustManagers={}", Arrays.toString(km), Arrays.toString(tm));
    final SSLContext ctx = SSLContext.getInstance(protocol);
    ctx.init(km, tm, null);
    return ctx;
  }


  /**
   * Creates an {@link AggregateTrustManager} containing the supplied trust managers.
   *
   * @param  managers  to aggregate
   *
   * @return  array containing a single aggregate trust manager
   */
  protected TrustManager[] aggregateTrustManagers(final TrustManager... managers)
  {
    X509TrustManager[] x509Managers = null;
    if (managers != null) {
      x509Managers = new X509TrustManager[managers.length];
      for (int i = 0; i < managers.length; i++) {
        x509Managers[i] = (X509TrustManager) managers[i];
      }
    }
    return new TrustManager[] {new AggregateTrustManager(x509Managers)};
  }
}
