/*
  $Id: SpringAuthenticatorFactory.java 2238 2012-02-06 20:21:41Z dfisher $

  Copyright (C) 2003-2010 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2238 $
  Updated: $Date: 2012-02-06 15:21:41 -0500 (Mon, 06 Feb 2012) $
*/
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
 * @version  $Revision: 2238 $ $Date: 2012-02-06 15:21:41 -0500 (Mon, 06 Feb 2012) $
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


  /** {@inheritDoc} */
  @Override
  public Authenticator createAuthenticator(final Map<String, ?> jaasOptions)
  {
    if (context == null) {
      throw new UnsupportedOperationException(
        "Could not initialize spring context");
    }
    return context.getBean("authenticator", Authenticator.class);
  }


  /** {@inheritDoc} */
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
