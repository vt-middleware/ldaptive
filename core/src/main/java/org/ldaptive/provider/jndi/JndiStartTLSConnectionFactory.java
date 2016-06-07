/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.jndi;

import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import org.ldaptive.ConnectionStrategy;
import org.ldaptive.LdapException;
import org.ldaptive.provider.AbstractProviderConnectionFactory;
import org.ldaptive.provider.ConnectionException;

/**
 * Creates connections using the JNDI {@link InitialLdapContext} class with the startTLS extended operation.
 *
 * @author  Middleware Services
 */
public class JndiStartTLSConnectionFactory extends AbstractProviderConnectionFactory<JndiProviderConfig>
{

  /** Environment properties. */
  private final Map<String, Object> environment;

  /** Context class loader to use when instantiating {@link InitialLdapContext}. */
  private final ClassLoader classLoader;

  /** SSL socket factory to use for startTLS negotiation. */
  private final SSLSocketFactory sslSocketFactory;

  /** Hostname verifier to use for startTLS negotiation. */
  private final HostnameVerifier hostnameVerifier;


  /**
   * Creates a new jndi startTLS connection factory.
   *
   * @param  url  of the ldap to connect to
   * @param  strategy  connection strategy
   * @param  config  provider configuration
   * @param  env  jndi context environment
   * @param  cl  class loader
   * @param  factory  SSL socket factory
   * @param  verifier  hostname verifier
   */
  public JndiStartTLSConnectionFactory(
    final String url,
    final ConnectionStrategy strategy,
    final JndiProviderConfig config,
    final Map<String, Object> env,
    final ClassLoader cl,
    final SSLSocketFactory factory,
    final HostnameVerifier verifier)
  {
    super(url, strategy, config);
    environment = Collections.unmodifiableMap(env);
    classLoader = cl;
    sslSocketFactory = factory;
    hostnameVerifier = verifier;
  }


  @Override
  protected JndiStartTLSConnection createInternal(final String url)
    throws LdapException
  {
    // CheckStyle:IllegalType OFF
    // the JNDI API requires the Hashtable type
    final Hashtable<String, Object> env = new Hashtable<>(environment);
    // CheckStyle:IllegalType ON
    env.put(JndiProvider.PROVIDER_URL, url);

    JndiStartTLSConnection conn = null;
    boolean closeConn = false;
    try {
      if (classLoader != null) {
        final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
          Thread.currentThread().setContextClassLoader(classLoader);
          conn = new JndiStartTLSConnection(new InitialLdapContext(env, null), getProviderConfig());
        } finally {
          Thread.currentThread().setContextClassLoader(currentClassLoader);
        }
      } else {
        conn = new JndiStartTLSConnection(new InitialLdapContext(env, null), getProviderConfig());
      }
      conn.setStartTlsResponse(startTLS(conn.getLdapContext()));
    } catch (NamingException e) {
      closeConn = true;
      throw new ConnectionException(e, NamingExceptionUtils.getResultCode(e.getClass()));
    } catch (IOException e) {
      closeConn = true;
      throw new ConnectionException(e);
    } catch (RuntimeException e) {
      closeConn = true;
      throw e;
    } finally {
      if (closeConn) {
        try {
          if (conn != null) {
            conn.close(null);
          }
        } catch (LdapException e) {
          logger.debug("Problem tearing down connection", e);
        }
      }
    }
    return conn;
  }


  /**
   * This will attempt the startTLS extended operation on the supplied ldap context.
   *
   * @param  ctx  ldap context
   *
   * @return  start tls response
   *
   * @throws  NamingException  if an error occurs while requesting an extended operation
   * @throws  IOException  if an error occurs while negotiating TLS
   */
  protected StartTlsResponse startTLS(final LdapContext ctx)
    throws NamingException, IOException
  {
    final StartTlsResponse tls = (StartTlsResponse) ctx.extendedOperation(new StartTlsRequest());
    if (hostnameVerifier != null) {
      logger.trace("startTLS hostnameVerifier = {}", hostnameVerifier);
      tls.setHostnameVerifier(hostnameVerifier);
    }
    if (sslSocketFactory != null) {
      logger.trace("startTLS sslSocketFactory = {}", sslSocketFactory);
      tls.negotiate(sslSocketFactory);
    } else {
      tls.negotiate();
    }
    return tls;
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::metadata=%s, environment=%s, classLoader=%s, providerConfig=%s, sslSocketFactory=%s, " +
        "hostnameVerifier=%s]",
        getClass().getName(),
        hashCode(),
        getMetadata(),
        environment,
        classLoader,
        getProviderConfig(),
        sslSocketFactory,
        hostnameVerifier);
  }
}
