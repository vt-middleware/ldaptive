/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.util.Map;
import org.ldaptive.auth.AuthenticationHandler;
import org.ldaptive.auth.AuthenticationRequest;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.pool.PooledConnectionFactoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Provides an authentication factory implementation that uses a spring context.
 *
 * @author  Middleware Services
 */
public class SpringAuthenticatorFactory implements AuthenticatorFactory
{

  /** Application context. */
  private static ClassPathXmlApplicationContext context;

  /** Initialize the context. */
  static {
    try {
      context = new ClassPathXmlApplicationContext(new String[] {
        "/spring-jaas-context.xml",
      });
    } catch (Exception e) {
      final Logger logger = LoggerFactory.getLogger(
        SpringAuthenticatorFactory.class);
      logger.warn("Could not create spring context", e.getMessage());
    }
  }


  @Override
  public Authenticator createAuthenticator(final Map<String, ?> jaasOptions)
  {
    if (context == null) {
      throw new UnsupportedOperationException(
        "Could not initialize spring context");
    }
    return context.getBean("authenticator", Authenticator.class);
  }


  @Override
  public AuthenticationRequest createAuthenticationRequest(
    final Map<String, ?> jaasOptions)
  {
    if (context == null) {
      throw new UnsupportedOperationException(
        "Could not initialize spring context");
    }
    return context.getBean(
      "authenticationRequest", AuthenticationRequest.class);
  }


  /**
   * Closes the authenticator dn resolver if it is a managed dn resolver.
   */
  public static void close()
  {
    if (context == null) {
      throw new UnsupportedOperationException(
        "Could not initialize spring context");
    }
    final Authenticator a = context.getBean(
      "authenticator", Authenticator.class);
    if (a.getDnResolver() instanceof PooledConnectionFactoryManager) {
      final PooledConnectionFactoryManager cfm =
        (PooledConnectionFactoryManager) a.getDnResolver();
      cfm.getConnectionFactory().getConnectionPool().close();
    }
    final AuthenticationHandler ah = a.getAuthenticationHandler();
    if (ah instanceof PooledConnectionFactoryManager) {
      final PooledConnectionFactoryManager cfm =
        (PooledConnectionFactoryManager) ah;
      cfm.getConnectionFactory().getConnectionPool().close();
    }
  }
}
