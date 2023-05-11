/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.security.Principal;
import java.security.cert.Certificate;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.security.cert.X509Certificate;

/**
 * Class to support testing where an SSLSession is required.
 *
 * @author  Middleware Services
 */
class MockSSLSession implements SSLSession
{


  @Override
  public byte[] getId()
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public SSLSessionContext getSessionContext()
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public long getCreationTime()
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public long getLastAccessedTime()
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public void invalidate()
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public boolean isValid()
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public void putValue(final String name, final Object value)
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public Object getValue(final String name)
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public void removeValue(final String name)
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public String[] getValueNames()
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public Certificate[] getPeerCertificates()
    throws SSLPeerUnverifiedException
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public Certificate[] getLocalCertificates()
  {
    throw new UnsupportedOperationException();
  }


  @Override
  @Deprecated
  public X509Certificate[] getPeerCertificateChain()
    throws SSLPeerUnverifiedException
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public Principal getPeerPrincipal()
    throws SSLPeerUnverifiedException
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public Principal getLocalPrincipal()
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public String getCipherSuite()
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public String getProtocol()
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public String getPeerHost()
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public int getPeerPort()
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public int getPacketBufferSize()
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public int getApplicationBufferSize()
  {
    throw new UnsupportedOperationException();
  }
}
