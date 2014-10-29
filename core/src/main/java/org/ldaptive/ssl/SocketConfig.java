/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive.ssl;

import java.net.Socket;
import java.net.SocketException;
import org.ldaptive.AbstractConfig;

/**
 * Contains the configuration data for sockets.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class SocketConfig extends AbstractConfig
{

  /** Keep alive. */
  private Boolean keepAlive;

  /** OOB Inline. */
  private Boolean oobInline;

  /** Receive buffer size. */
  private Integer receiveBufferSize;

  /** Send buffer size. */
  private Integer sendBufferSize;

  /** Reuse address. */
  private Boolean reuseAddress;

  /** SO Linger. */
  private Integer soLinger;

  /** SO Timeout. */
  private Integer soTimeout;

  /** TCP No Delay. */
  private Boolean tcpNoDelay;

  /** Traffic Class. */
  private Integer trafficClass;


  /**
   * Returns whether this socket config contains any configuration data.
   *
   * @return  whether all properties are null
   */
  public boolean isEmpty()
  {
    return
      keepAlive == null && oobInline == null && receiveBufferSize == null &&
      sendBufferSize == null && reuseAddress == null && soLinger == null &&
      soTimeout == null && tcpNoDelay == null && trafficClass == null;
  }


  /**
   * See {@link Socket#getKeepAlive()}.
   *
   * @return  keep alive
   */
  public Boolean getKeepAlive()
  {
    return keepAlive;
  }


  /**
   * See {@link Socket#setKeepAlive(boolean)}.
   *
   * @param  b  keep alive
   */
  public void setKeepAlive(final boolean b)
  {
    checkImmutable();
    logger.trace("setting keepAlive: {}", b);
    keepAlive = b;
  }


  /**
   * See {@link Socket#getOOBInline()}.
   *
   * @return  OOB in line
   */
  public Boolean getOOBInline()
  {
    return oobInline;
  }


  /**
   * See {@link Socket#setOOBInline(boolean)}.
   *
   * @param  b  OOB in line
   */
  public void setOOBInline(final boolean b)
  {
    checkImmutable();
    logger.trace("setting oobInline: {}", b);
    oobInline = b;
  }


  /**
   * See {@link Socket#getReceiveBufferSize()}.
   *
   * @return  receive buffer size
   */
  public Integer getReceiveBufferSize()
  {
    return receiveBufferSize;
  }


  /**
   * See {@link Socket#setReceiveBufferSize(int)}.
   *
   * @param  i  receive buffer size
   */
  public void setReceiveBufferSize(final int i)
  {
    checkImmutable();
    logger.trace("setting receiveBufferSize: {}", i);
    receiveBufferSize = i;
  }


  /**
   * See {@link Socket#getSendBufferSize()}.
   *
   * @return  send buffer size
   */
  public Integer getSendBufferSize()
  {
    return sendBufferSize;
  }


  /**
   * See {@link Socket#setSendBufferSize(int)}.
   *
   * @param  i  send buffer size
   */
  public void setSendBufferSize(final int i)
  {
    checkImmutable();
    logger.trace("setting sendBufferSize: {}", i);
    sendBufferSize = i;
  }


  /**
   * See {@link Socket#getReuseAddress()}.
   *
   * @return  reuse address
   */
  public Boolean getReuseAddress()
  {
    return reuseAddress;
  }


  /**
   * See {@link Socket#setReuseAddress(boolean)}.
   *
   * @param  b  reuse address
   */
  public void setReuseAddress(final boolean b)
  {
    checkImmutable();
    logger.trace("setting reuseAddress: {}", b);
    reuseAddress = b;
  }


  /**
   * See {@link Socket#getSoLinger()}.
   *
   * @return  SO linger
   */
  public Integer getSoLinger()
  {
    return soLinger;
  }


  /**
   * See {@link Socket#setSoLinger(boolean, int)}.
   *
   * @param  i  SO linger
   */
  public void setSoLinger(final int i)
  {
    checkImmutable();
    logger.trace("setting soLinger: {}", i);
    soLinger = i;
  }


  /**
   * See {@link Socket#getSoTimeout()}.
   *
   * @return  SO timeout
   */
  public Integer getSoTimeout()
  {
    return soTimeout;
  }


  /**
   * See {@link Socket#setSoTimeout(int)}.
   *
   * @param  i  SO timeout
   */
  public void setSoTimeout(final int i)
  {
    checkImmutable();
    logger.trace("setting soTimeout: {}", i);
    soTimeout = i;
  }


  /**
   * See {@link Socket#getTcpNoDelay()}.
   *
   * @return  tcp no delay
   */
  public Boolean getTcpNoDelay()
  {
    return tcpNoDelay;
  }


  /**
   * See {@link Socket#setTcpNoDelay(boolean)}.
   *
   * @param  b  tcp no delay
   */
  public void setTcpNoDelay(final boolean b)
  {
    checkImmutable();
    logger.trace("setting tcpNoDelay: {}", b);
    tcpNoDelay = b;
  }


  /**
   * See {@link Socket#getTrafficClass()}.
   *
   * @return  traffic class
   */
  public Integer getTrafficClass()
  {
    return trafficClass;
  }


  /**
   * See {@link Socket#setTrafficClass(int)}.
   *
   * @param  i  traffic class
   */
  public void setTrafficClass(final int i)
  {
    checkImmutable();
    logger.trace("setting trafficClass: {}", i);
    trafficClass = i;
  }


  /**
   * Applies this configuration to the supplied socket.
   *
   * @param  socket  to set properties on
   *
   * @throws  SocketException  if an error occurs invoking a socket setter
   */
  public void configureSocket(final Socket socket)
    throws SocketException
  {
    if (keepAlive != null) {
      socket.setKeepAlive(keepAlive);
    }
    if (oobInline != null) {
      socket.setOOBInline(oobInline);
    }
    if (reuseAddress != null) {
      socket.setReuseAddress(reuseAddress);
    }
    if (tcpNoDelay != null) {
      socket.setTcpNoDelay(tcpNoDelay);
    }
    if (receiveBufferSize != null) {
      socket.setReceiveBufferSize(receiveBufferSize);
    }
    if (sendBufferSize != null) {
      socket.setSendBufferSize(sendBufferSize);
    }
    if (soLinger != null) {
      socket.setSoLinger(true, soLinger);
    }
    if (soTimeout != null) {
      socket.setSoTimeout(soTimeout);
    }
    if (trafficClass != null) {
      socket.setTrafficClass(trafficClass);
    }
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::keepAlive=%s, oobInline=%s, receiveBufferSize=%s, " +
        "sendBufferSize=%s, reuseAddress=%s, soLinger=%s, soTimeout=%s, " +
        "tcpNoDelay=%s, trafficClass=%s]",
        getClass().getName(),
        hashCode(),
        keepAlive,
        oobInline,
        receiveBufferSize,
        sendBufferSize,
        reuseAddress,
        soLinger,
        soTimeout,
        tcpNoDelay,
        trafficClass);
  }
}
