/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

/**
 * Class to support testing where an SSLSocket is required.
 *
 * @author  Middleware Services
 */
class MockSSLSocket extends SSLSocket
{

  /** SSL engine to wrap. */
  private final SSLEngine engine;


  /**
   * Creates a new mock SSL socket.
   *
   * @param  initializer  to create SSLEngine
   *
   * @throws GeneralSecurityException  if the SSLEngine cannot be created
   */
  MockSSLSocket(final SSLContextInitializer initializer)
    throws GeneralSecurityException
  {
    this(initializer.initSSLContext("TLS").createSSLEngine());
  }


  /**
   * Creates a new mock SSL socket.
   *
   * @param  e  engine to wrap
   */
  MockSSLSocket(final SSLEngine e)
  {
    engine = new MockSSLEngine(e);
  }


  @Override
  public String[] getSupportedCipherSuites()
  {
    return engine.getSupportedCipherSuites();
  }


  @Override
  public String[] getEnabledCipherSuites()
  {
    return engine.getEnabledCipherSuites();
  }


  @Override
  public void setEnabledCipherSuites(final String[] suites)
  {
    engine.setEnabledCipherSuites(suites);
  }


  @Override
  public String[] getSupportedProtocols()
  {
    return engine.getSupportedProtocols();
  }


  @Override
  public String[] getEnabledProtocols()
  {
    return engine.getEnabledProtocols();
  }


  @Override
  public void setEnabledProtocols(final String[] protocols)
  {
    engine.setEnabledProtocols(protocols);
  }


  @Override
  public SSLSession getSession()
  {
    return engine.getSession();
  }


  @Override
  public void addHandshakeCompletedListener(final HandshakeCompletedListener listener)
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public void removeHandshakeCompletedListener(final HandshakeCompletedListener listener)
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public void startHandshake()
    throws IOException
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public void setUseClientMode(final boolean mode)
  {
    engine.setUseClientMode(mode);
  }


  @Override
  public boolean getUseClientMode()
  {
    return engine.getUseClientMode();
  }


  @Override
  public void setNeedClientAuth(final boolean need)
  {
    engine.setNeedClientAuth(need);
  }


  @Override
  public boolean getNeedClientAuth()
  {
    return engine.getNeedClientAuth();
  }


  @Override
  public void setWantClientAuth(final boolean want)
  {
    engine.setWantClientAuth(want);
  }


  @Override
  public boolean getWantClientAuth()
  {
    return engine.getWantClientAuth();
  }


  @Override
  public void setEnableSessionCreation(final boolean flag)
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public boolean getEnableSessionCreation()
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public boolean isConnected()
  {
    return true;
  }


  @Override
  public SSLSession getHandshakeSession()
  {
    return engine.getHandshakeSession();
  }
}
