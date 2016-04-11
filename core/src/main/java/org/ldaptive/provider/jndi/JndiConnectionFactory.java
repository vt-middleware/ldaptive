/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.jndi;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import org.ldaptive.LdapException;
import org.ldaptive.provider.AbstractProviderConnectionFactory;
import org.ldaptive.provider.ConnectionException;
import org.ldaptive.ssl.SslConfig;
import org.ldaptive.ssl.ThreadLocalTLSSocketFactory;

/**
 * Creates connections using the JNDI {@link InitialLdapContext} class.
 *
 * @author  Middleware Services
 */
public class JndiConnectionFactory extends AbstractProviderConnectionFactory<JndiProviderConfig>
{

  /** Environment properties. */
  private final Map<String, Object> environment;

  /** Thread local SslConfig, if one exists. */
  private SslConfig threadLocalSslConfig;


  /**
   * Creates a new jndi connection factory.
   *
   * @param  url  of the ldap to connect to
   * @param  config  provider configuration
   * @param  env  jndi context environment
   */
  public JndiConnectionFactory(final String url, final JndiProviderConfig config, final Map<String, Object> env)
  {
    super(url, config);
    environment = Collections.unmodifiableMap(env);
    if (ThreadLocalTLSSocketFactory.class.getName().equals(environment.get(JndiProvider.SOCKET_FACTORY))) {
      threadLocalSslConfig = new ThreadLocalTLSSocketFactory().getSslConfig();
    }
  }


  /**
   * Returns the JNDI environment for this connection factory. This map cannot be modified.
   *
   * @return  jndi environment
   */
  protected Map<String, Object> getEnvironment()
  {
    return environment;
  }


  @Override
  protected JndiConnection createInternal(final String url)
    throws LdapException
  {
    if (
      threadLocalSslConfig != null &&
        ThreadLocalTLSSocketFactory.class.getName().equals(environment.get(JndiProvider.SOCKET_FACTORY))) {
      final ThreadLocalTLSSocketFactory sf = new ThreadLocalTLSSocketFactory();
      sf.setSslConfig(threadLocalSslConfig);
    }

    // CheckStyle:IllegalType OFF
    // the JNDI API requires the Hashtable type
    final Hashtable<String, Object> env = new Hashtable<>(getEnvironment());
    // CheckStyle:IllegalType ON
    env.put(JndiProvider.PROVIDER_URL, url);

    JndiConnection conn;
    try {
      conn = new JndiConnection(new InitialLdapContext(env, null), getProviderConfig());
    } catch (NamingException e) {
      throw new ConnectionException(e, NamingExceptionUtils.getResultCode(e.getClass()));
    }
    return conn;
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::metadata=%s, environment=%s, providerConfig=%s]",
        getClass().getName(),
        hashCode(),
        getMetadata(),
        environment,
        getProviderConfig());
  }
}
