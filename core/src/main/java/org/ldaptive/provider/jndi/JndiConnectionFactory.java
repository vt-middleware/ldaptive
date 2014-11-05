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

/**
 * Creates connections using the JNDI {@link InitialLdapContext} class.
 *
 * @author  Middleware Services
 * @version  $Revision: 2974 $ $Date: 2014-04-21 15:29:45 -0400 (Mon, 21 Apr 2014) $
 */
public class JndiConnectionFactory
  extends AbstractProviderConnectionFactory<JndiProviderConfig>
{

  /** Environment properties. */
  private final Map<String, Object> environment;


  /**
   * Creates a new jndi connection factory.
   *
   * @param  url  of the ldap to connect to
   * @param  config  provider configuration
   * @param  env  jndi context environment
   */
  public JndiConnectionFactory(
    final String url,
    final JndiProviderConfig config,
    final Map<String, Object> env)
  {
    super(url, config);
    environment = Collections.unmodifiableMap(env);
  }


  /**
   * Returns the JNDI environment for this connection factory. This map cannot
   * be modified.
   *
   * @return  jndi environment
   */
  protected Map<String, Object> getEnvironment()
  {
    return environment;
  }


  /** {@inheritDoc} */
  @Override
  protected JndiConnection createInternal(final String url)
    throws LdapException
  {
    // CheckStyle:IllegalType OFF
    // the JNDI API requires the Hashtable type
    final Hashtable<String, Object> env = new Hashtable<>(
      getEnvironment());
    // CheckStyle:IllegalType ON
    env.put(JndiProvider.PROVIDER_URL, url);

    JndiConnection conn;
    try {
      conn = new JndiConnection(
        new InitialLdapContext(env, null),
        getProviderConfig());
    } catch (NamingException e) {
      throw new ConnectionException(
        e,
        NamingExceptionUtils.getResultCode(e.getClass()));
    }
    return conn;
  }


  /** {@inheritDoc} */
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
