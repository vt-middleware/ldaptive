/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

/**
 * Class to support testing where an SSLSession is required.
 *
 * @author  Middleware Services
 */
class MockSSLEngine extends SSLEngine
{

  /** SSL engine to wrap. */
  private final SSLEngine engine;


  /**
   * Creates a new mock SSL engine.
   *
   * @param  initializer  to create SSLEngine
   *
   * @throws  GeneralSecurityException  if the SSLEngine cannot be created
   */
  MockSSLEngine(final SSLContextInitializer initializer)
    throws GeneralSecurityException
  {
    this(initializer.initSSLContext("TLS").createSSLEngine());
  }


  /**
   * Creates a new mock SSL engine.
   *
   * @param  e  engine to mock
   */
  MockSSLEngine(final SSLEngine e)
  {
    engine = e;
  }


  @Override
  public SSLEngineResult wrap(final ByteBuffer[] srcs, final int offset, final int length, final ByteBuffer dst)
    throws SSLException
  {
    return engine.wrap(srcs, offset, length, dst);
  }


  @Override
  public SSLEngineResult unwrap(final ByteBuffer src, final ByteBuffer[] dsts, final int offset, final int length)
    throws SSLException
  {
    return engine.unwrap(src, dsts, offset, length);
  }


  @Override
  public Runnable getDelegatedTask()
  {
    return engine.getDelegatedTask();
  }


  @Override
  public void closeInbound()
    throws SSLException
  {
    engine.closeInbound();
  }


  @Override
  public boolean isInboundDone()
  {
    return engine.isInboundDone();
  }


  @Override
  public void closeOutbound()
  {
    engine.closeOutbound();
  }


  @Override
  public boolean isOutboundDone()
  {
    return engine.isOutboundDone();
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
  public void beginHandshake()
    throws SSLException
  {
    engine.beginHandshake();
  }


  @Override
  public SSLEngineResult.HandshakeStatus getHandshakeStatus()
  {
    return engine.getHandshakeStatus();
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
    engine.setEnableSessionCreation(flag);
  }


  @Override
  public boolean getEnableSessionCreation()
  {
    return engine.getEnableSessionCreation();
  }


  @Override
  public SSLSession getHandshakeSession()
  {
    final SSLSession session = engine.getHandshakeSession();
    if (session == null) {
      return new MockSSLSession();
    }
    return session;
  }
}
