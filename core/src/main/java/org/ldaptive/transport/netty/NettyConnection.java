/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.sasl.SaslException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.SimpleUserEventChannelHandler;
import io.netty.channel.SingleThreadEventLoop;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ScheduledFuture;
import org.ldaptive.AbandonRequest;
import org.ldaptive.AddRequest;
import org.ldaptive.AddResponse;
import org.ldaptive.BindRequest;
import org.ldaptive.BindResponse;
import org.ldaptive.ClosedRetryMetadata;
import org.ldaptive.CompareRequest;
import org.ldaptive.CompareResponse;
import org.ldaptive.ConnectException;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionInitializer;
import org.ldaptive.ConnectionValidator;
import org.ldaptive.DeleteRequest;
import org.ldaptive.DeleteResponse;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.LdapURL;
import org.ldaptive.Message;
import org.ldaptive.ModifyDnRequest;
import org.ldaptive.ModifyDnResponse;
import org.ldaptive.ModifyRequest;
import org.ldaptive.ModifyResponse;
import org.ldaptive.Result;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResultReference;
import org.ldaptive.UnbindRequest;
import org.ldaptive.control.RequestControl;
import org.ldaptive.extended.ExtendedRequest;
import org.ldaptive.extended.ExtendedResponse;
import org.ldaptive.extended.IntermediateResponse;
import org.ldaptive.extended.StartTLSRequest;
import org.ldaptive.extended.UnsolicitedNotification;
import org.ldaptive.sasl.GssApiBindRequest;
import org.ldaptive.sasl.QualityOfProtection;
import org.ldaptive.sasl.SaslClientRequest;
import org.ldaptive.ssl.HostnameVerifierAdapter;
import org.ldaptive.ssl.SSLContextInitializer;
import org.ldaptive.ssl.SslConfig;
import org.ldaptive.transport.DefaultCompareOperationHandle;
import org.ldaptive.transport.DefaultExtendedOperationHandle;
import org.ldaptive.transport.DefaultOperationHandle;
import org.ldaptive.transport.DefaultSearchOperationHandle;
import org.ldaptive.transport.ResponseParser;
import org.ldaptive.transport.SaslClient;
import org.ldaptive.transport.TransportConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty based connection implementation.
 *
 * @author  Middleware Services
 */
public final class NettyConnection extends TransportConnection
{

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(NettyConnection.class);

  /** Request encoder pipeline handler. */
  private static final RequestEncoder REQUEST_ENCODER = new RequestEncoder();

  /** Inbound handler to read the next message if autoRead is false. */
  private static final InboundAutoReadEventHandler READ_NEXT_MESSAGE = new InboundAutoReadEventHandler();

  /** Type of channel. */
  private final Class<? extends Channel> channelType;

  /** Event worker group used to process I/O. */
  private final EventLoopGroup ioWorkerGroup;

  /** Event worker group used to process inbound messages. */
  private final EventLoopGroup messageWorkerGroup;

  /** Netty channel configuration options. */
  private final Map<ChannelOption, Object> channelOptions;

  /** Queue holding requests that haven't received a response. */
  private final HandleMap pendingResponses;

  /** Listener notified when the connection is closed. */
  private final CloseFutureListener closeListener = new CloseFutureListener();

  /** Message ID counter, incremented as requests are sent. */
  private final AtomicInteger messageID = new AtomicInteger(1);

  /** Block operations while a reconnect is occurring. */
  private final ReadWriteLock reconnectLock = new ReentrantReadWriteLock();

  /** Operation lock when a bind occurs. */
  private final ReadWriteLock bindLock = new ReentrantReadWriteLock();

  /** Executor for scheduling various connection related tasks. */
  private ExecutorService connectionExecutor = Executors.newSingleThreadExecutor(
    r -> {
      final Thread t = new Thread(r);
      t.setDaemon(true);
      return t;
    });

  /** URL derived from the connection strategy. */
  private LdapURL ldapURL;

  /** Connection to the LDAP server. */
  private Channel channel;

  /** Time this connection was successfully established, null if the connection is not open. */
  private Instant connectTime;

  /** Last exception received on the inbound pipeline. */
  private Throwable inboundException;


  /**
   * Creates a new connection. Netty supports various transport implementations including NIO, EPOLL, KQueue, etc. The
   * class type and event loop group are tightly coupled in this regard. See {@link SharedNioTransport} for an example
   * of what NIO parameters look like.
   *
   * @param  config  connection configuration
   * @param  type  type of channel
   * @param  ioGroup  event loop group that handles I/O and supports the channel type, cannot be null
   * @param  messageGroup  event loop group that handles inbound messages, can be null
   * @param  options additional channel options
   */
  public NettyConnection(
    final ConnectionConfig config,
    final Class<? extends Channel> type,
    final EventLoopGroup ioGroup,
    final EventLoopGroup messageGroup,
    final Map<ChannelOption, Object> options)
  {
    super(config);
    channelType = type;
    ioWorkerGroup = ioGroup;
    messageWorkerGroup = messageGroup;
    channelOptions = new HashMap<>();
    channelOptions.put(ChannelOption.SO_KEEPALIVE, true);
    if (config.getConnectTimeout() != null) {
      channelOptions.put(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) config.getConnectTimeout().toMillis());
    } else {
      channelOptions.put(ChannelOption.CONNECT_TIMEOUT_MILLIS, 0);
    }
    if (options != null && !options.isEmpty()) {
      channelOptions.putAll(options);
    }
    pendingResponses = new HandleMap();
  }


  /**
   * Creates a Netty {@link Bootstrap} with the supplied client initializer.
   *
   * @param  initializer  to provide to the bootstrap
   *
   * @return  Netty bootstrap
   */
  @SuppressWarnings("unchecked")
  private Bootstrap createBootstrap(final ClientInitializer initializer)
  {
    final Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(ioWorkerGroup);
    bootstrap.channel(channelType);
    channelOptions.forEach(bootstrap::option);
    bootstrap.handler(initializer);
    return bootstrap;
  }


  @Override
  protected boolean test(final LdapURL url)
  {
    final NettyConnection conn = new NettyConnection(
      connectionConfig,
      channelType,
      ioWorkerGroup,
      messageWorkerGroup,
      null);
    try {
      conn.open(url);
      LOGGER.debug("Test of {} successful", conn);
      return true;
    } catch (LdapException e) {
      LOGGER.debug("Test of {} failed", conn, e);
      return false;
    } finally {
      conn.close();
    }
  }


  @Override
  protected void open(final LdapURL url)
    throws LdapException
  {
    if (isOpen()) {
      throw new IllegalStateException("Connection is already open");
    }
    LOGGER.trace("Netty opening connection {}", this);
    openLock.lock();
    try {
      inboundException = null;
      ldapURL = url;
      channel = connectInternal();
      pendingResponses.open();
      // startTLS request must occur after the connection is ready
      if (connectionConfig.getUseStartTLS()) {
        try {
          final Result result = operation(new StartTLSRequest());
          if (!ResultCode.SUCCESS.equals(result.getResultCode())) {
            throw new ConnectException("StartTLS returned response: " + result);
          }
        } catch (Exception e) {
          LOGGER.error("StartTLS failed on connection open for {}", this, e);
          close();
          pendingResponses.clear();
          throw e;
        }
      }
      // initialize the connection
      if (connectionConfig.getConnectionInitializers() != null) {
        for (ConnectionInitializer initializer : connectionConfig.getConnectionInitializers()) {
          try {
            final Result result = initializer.initialize(this);
            if (!ResultCode.SUCCESS.equals(result.getResultCode())) {
              throw new ConnectException("Connection initializer returned response: " + result);
            }
          } catch (Exception e) {
            LOGGER.error("Connection initializer {} failed for {}", initializer, this, e);
            close();
            pendingResponses.clear();
            throw e;
          }
        }
      }
      channel.closeFuture().addListener(closeListener);
      connectTime = Instant.now();
      LOGGER.debug("Netty opened connection {}", this);
    } finally {
      openLock.unlock();
    }
  }


  /**
   * Creates a Netty bootstrap and connects to the LDAP server. Handles the details of adding an SSL handler to the
   * pipeline. This method waits until the connection is established.
   *
   * @return  channel for the established connection.
   *
   * @throws  ConnectException  if the connection fails
   */
  private Channel connectInternal()
    throws ConnectException
  {
    SslHandler handler = null;
    if (ldapURL.getScheme().equals("ldaps")) {
      try {
        handler = createSslHandler(connectionConfig);
      } catch (SSLException e) {
        throw new ConnectException(e);
      }
    }
    final ClientInitializer initializer = new ClientInitializer(handler);
    final Bootstrap bootstrap = createBootstrap(initializer);

    final CountDownLatch channelLatch = new CountDownLatch(1);
    final ChannelFuture future = bootstrap.connect(new InetSocketAddress(ldapURL.getHostname(), ldapURL.getPort()));
    future.addListener((ChannelFutureListener) f -> channelLatch.countDown());
    try {
      // wait until the connection future is complete
      // note that the wait time is controlled by the connectTimeout property in ConnectionConfig
      channelLatch.await();
    } catch (InterruptedException e) {
      future.cancel(true);
    }
    LOGGER.trace("bootstrap connect returned {} for {}", future, this);
    if (future.isCancelled()) {
      throw new ConnectException("Connection cancelled");
    }
    if (!future.isSuccess()) {
      if (future.cause() != null) {
        throw new ConnectException(future.cause());
      } else {
        throw new ConnectException("Connection could not be opened");
      }
    }

    if (initializer.isSsl()) {
      // socket is connected, wait for SSL handshake to complete
      try {
        waitForSSLHandshake(future.channel());
      } catch (SSLException e) {
        future.channel().close();
        throw new ConnectException(e);
      }
    }

    if (!future.channel().config().isAutoRead()) {
      future.channel().read();
    }
    return future.channel();
  }


  /**
   * Creates a Netty SSL handler using the supplied connection config.
   *
   * @param  config  containing SSL config
   *
   * @return  SSL handler
   *
   * @throws  SSLException  if the SSL engine cannot be initialized
   */
  private SslHandler createSslHandler(final ConnectionConfig config)
    throws SSLException
  {
    final SslConfig sc = config.getSslConfig() != null ?
      SslConfig.copy(config.getSslConfig()) : new SslConfig();
    final SSLContext ctx;
    try {
      final SSLContextInitializer initializer = sc.createSSLContextInitializer();
      ctx = initializer.initSSLContext("TLS");
    } catch (GeneralSecurityException e) {
      throw new SSLException("Could not initialize SSL context", e);
    }
    final SSLEngine engine = ctx.createSSLEngine(ldapURL.getHostname(), ldapURL.getPort());
    engine.setUseClientMode(true);
    if (sc.getEnabledProtocols() != null) {
      engine.setEnabledProtocols(sc.getEnabledProtocols());
    }
    if (sc.getEnabledCipherSuites() != null) {
      engine.setEnabledCipherSuites(sc.getEnabledCipherSuites());
    }
    if (sc.getHostnameVerifier() == null) {
      engine.getSSLParameters().setEndpointIdentificationAlgorithm("LDAPS");
    }
    final SslHandler handler = new SslHandler(engine);
    handler.setHandshakeTimeout(sc.getHandshakeTimeout().toMillis(), TimeUnit.MILLISECONDS);
    return handler;
  }


  /**
   * Waits until the SSL handshake has completed.
   *
   * @param  ch  that the handshake is occurring on
   *
   * @throws  SSLException  if the handshake fails
   */
  private void waitForSSLHandshake(final Channel ch)
    throws SSLException
  {
    // socket is connected, wait for SSL handshake to complete
    final CountDownLatch sslLatch = new CountDownLatch(1);
    final SslHandler handler = ch.pipeline().get(SslHandler.class);
    final Future<Channel> sslFuture = handler.handshakeFuture();
    sslFuture.addListener(f -> sslLatch.countDown());
    try {
      // wait until the connection future is complete
      // note that the wait time is controlled by the handshakeTimeout property in SslConfig
      sslLatch.await();
    } catch (InterruptedException e) {
      sslFuture.cancel(true);
    }
    if (sslFuture.isCancelled()) {
      throw new SSLException("SSL handshake cancelled");
    }
    if (!sslFuture.isSuccess()) {
      if (inboundException != null) {
        throw new SSLException(inboundException);
      } else if (sslFuture.cause() != null) {
        throw new SSLException(sslFuture.cause());
      } else {
        throw new SSLException("SSL handshake failure");
      }
    }
    if (connectionConfig.getSslConfig() != null && connectionConfig.getSslConfig().getHostnameVerifier() != null) {
      final HostnameVerifier verifier = new HostnameVerifierAdapter(
        connectionConfig.getSslConfig().getHostnameVerifier());
      final SSLSession session = handler.engine().getSession();
      final String hostname = session.getPeerHost();
      if (!verifier.verify(hostname, session)) {
        throw new SSLPeerUnverifiedException("Hostname verification failed for " + hostname + " using " + verifier);
      }
    }
  }


  /**
   * Performs a startTLS operation.  This method can only be invoked when a connection is opened.
   *
   * @param  request  to send
   *
   * @return  result of the startTLS operation
   *
   * @throws  LdapException  if the operation fails
   */
  Result operation(final StartTLSRequest request)
    throws LdapException
  {
    throwIfClosed();
    if (channel.pipeline().get(SslHandler.class) != null) {
      throw new ConnectException("SslHandler is already in use");
    }
    final DefaultExtendedOperationHandle handle = new DefaultExtendedOperationHandle(
      request,
      this,
      connectionConfig.getResponseTimeout());
    final Result result;
    try {
      result = handle.execute();
    } catch (LdapException e) {
      throw new ConnectException("StartTLS operation failed", e);
    }
    if (ResultCode.SUCCESS.equals(result.getResultCode())) {
      try {
        channel.pipeline().addFirst("ssl", createSslHandler(connectionConfig));
        waitForSSLHandshake(channel);
      } catch (SSLException e) {
        throw new ConnectException(e);
      }
    } else {
      throw new ConnectException("StartTLS operation failed with result " + result);
    }
    return result;
  }


  @Override
  protected void operation(final UnbindRequest request)
  {
    LOGGER.debug("Unbind request {} with pending responses {}", request, pendingResponses);
    if (reconnectLock.readLock().tryLock()) {
      try {
        if (!isOpen()) {
          LOGGER.warn("Attempt to unbind ignored, connection {} is not open", this);
        } else {
          if (bindLock.readLock().tryLock()) {
            try {
              final EncodedRequest encodedRequest = new EncodedRequest(messageID.getAndIncrement(), request);
              channel.writeAndFlush(encodedRequest).addListener(
                ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            } finally {
              bindLock.readLock().unlock();
            }
          } else {
            throw new IllegalStateException("Bind in progress, cannot send unbind request");
          }
        }
      } finally {
        reconnectLock.readLock().unlock();
      }
    } else {
      LOGGER.warn("Attempt to unbind ignored, connection {} is reconnecting", this);
    }
  }


  /**
   * Performs a GSS-API SASL bind operation.
   *
   * @param  request  to send
   *
   * @return  result of the GSS-API bind operation
   *
   * @throws  LdapException  if the operation fails or another bind is in progress
   */
  public Result operation(final GssApiBindRequest request)
    throws LdapException
  {
    throwIfClosed();
    if (!bindLock.writeLock().tryLock()) {
      throw new LdapException("Bind in progress");
    }
    try {
      final LoginContext context = new LoginContext(request.getClass().getSimpleName(), request);
      context.login();
      final Result result = Subject.doAs(
        context.getSubject(), (PrivilegedAction<Result>) () -> {
          try {
            return operation((SaslClientRequest) request);
          } catch (LdapException e) {
            LOGGER.warn("SASL GSSAPI operation failed for {}", this, e);
          }
          return null;
        });
      if (result == null) {
        throw new LdapException("SASL GSSAPI operation failed");
      }
      return result;
    } catch (LoginException e) {
      throw new LdapException("SASL GSSAPI operation failed", e);
    } finally {
      bindLock.writeLock().unlock();
    }
  }


  /**
   * Performs a SASL client bind operation.
   *
   * @param  request  to send
   *
   * @return  result of the SASL client bind operation
   *
   * @throws  LdapException  if the operation fails or another bind is in progress
   */
  @Override
  public Result operation(final SaslClientRequest request)
    throws LdapException
  {
    throwIfClosed();
    if (!bindLock.writeLock().tryLock()) {
      throw new LdapException("Bind in progress");
    }
    try {
      final SaslClient client = new SaslClient(ldapURL.getHostname());
      final BindResponse response;
      boolean saslSecurity = false;
      try {
        response = client.bind(this, request);
        if (response.getResultCode() == ResultCode.SUCCESS) {
          final QualityOfProtection qop = client.getQualityOfProtection();
          if (QualityOfProtection.AUTH_INT == qop || QualityOfProtection.AUTH_CONF == qop) {
            if (channel.pipeline().get(SaslHandler.class) != null) {
              channel.pipeline().remove(SaslHandler.class);
            }
            if (channel.pipeline().get(SslHandler.class) != null) {
              channel.pipeline().addAfter("ssl", "sasl", new SaslHandler(client.getClient()));
            } else {
              channel.pipeline().addFirst("sasl", new SaslHandler(client.getClient()));
            }
            saslSecurity = true;
          }
        }
        return response;
      } catch (SaslException e) {
        throw new LdapException("SASL bind operation failed", e);
      } finally {
        if (!saslSecurity) {
          client.dispose();
        }
      }
    } finally {
      bindLock.writeLock().unlock();
    }
  }


  @Override
  public void operation(final AbandonRequest request)
  {
    final DefaultOperationHandle handle = pendingResponses.remove(request.getMessageID());
    LOGGER.debug("Abandon handle {} with pending responses {}", handle, pendingResponses);
    if (reconnectLock.readLock().tryLock()) {
      try {
        if (!isOpen()) {
          handle.exception(new LdapException("Connection is not open"));
        } else {
          if (bindLock.readLock().tryLock()) {
            try {
              final EncodedRequest encodedRequest = new EncodedRequest(messageID.getAndIncrement(), request);
              channel.writeAndFlush(encodedRequest).addListener(
                ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            } finally {
              bindLock.readLock().unlock();
            }
          } else {
            handle.exception(new LdapException("Bind in progress"));
          }
        }
      } finally {
        reconnectLock.readLock().unlock();
      }
    } else {
      handle.exception(new LdapException("Reconnect in progress"));
    }
  }


  @Override
  public DefaultOperationHandle<AddRequest, AddResponse> operation(final AddRequest request)
  {
    return new DefaultOperationHandle<>(request, this, connectionConfig.getResponseTimeout());
  }


  @Override
  public BindOperationHandle operation(final BindRequest request)
  {
    return new BindOperationHandle(request, this, connectionConfig.getResponseTimeout());
  }


  @Override
  public DefaultCompareOperationHandle operation(final CompareRequest request)
  {
    return new DefaultCompareOperationHandle(request, this, connectionConfig.getResponseTimeout());
  }


  @Override
  public DefaultOperationHandle<DeleteRequest, DeleteResponse> operation(final DeleteRequest request)
  {
    return new DefaultOperationHandle<>(request, this, connectionConfig.getResponseTimeout());
  }


  @Override
  public DefaultExtendedOperationHandle operation(final ExtendedRequest request)
  {
    if (request instanceof StartTLSRequest) {
      throw new IllegalArgumentException("StartTLS can only be invoked when the connection is opened");
    }
    return new DefaultExtendedOperationHandle(request, this, connectionConfig.getResponseTimeout());
  }


  @Override
  public DefaultOperationHandle<ModifyRequest, ModifyResponse> operation(final ModifyRequest request)
  {
    return new DefaultOperationHandle<>(request, this, connectionConfig.getResponseTimeout());
  }


  @Override
  public DefaultOperationHandle<ModifyDnRequest, ModifyDnResponse> operation(final ModifyDnRequest request)
  {
    return new DefaultOperationHandle<>(request, this, connectionConfig.getResponseTimeout());
  }


  @Override
  public DefaultSearchOperationHandle operation(final SearchRequest request)
  {
    return new DefaultSearchOperationHandle(request, this, connectionConfig.getResponseTimeout());
  }


  @Override
  protected void write(final DefaultOperationHandle handle)
  {
    LOGGER.debug("Write handle {} with pending responses {}", handle, pendingResponses);
    try {
      final boolean gotReconnectLock;
      if (connectionConfig.getReconnectTimeout() == null) {
        reconnectLock.readLock().lock();
        gotReconnectLock = true;
      } else {
        gotReconnectLock = reconnectLock.readLock().tryLock(
          connectionConfig.getReconnectTimeout().toMillis(), TimeUnit.MILLISECONDS);
      }
      if (gotReconnectLock) {
        try {
          if (!isOpen()) {
            handle.exception(new LdapException("Connection is closed, write aborted"));
          } else {
            if (bindLock.readLock().tryLock()) {
              try {
                final EncodedRequest encodedRequest = new EncodedRequest(
                  messageID.getAndIncrement(),
                  handle.getRequest());
                handle.messageID(encodedRequest.getMessageID());
                try {
                  if (pendingResponses.put(encodedRequest.getMessageID(), handle) != null) {
                    throw new IllegalStateException("Request already exists for ID " + encodedRequest.getMessageID());
                  }
                } catch (LdapException e) {
                  if (inboundException != null) {
                    throw new LdapException(e.getMessage(), inboundException);
                  }
                  throw e;
                }
                channel.writeAndFlush(encodedRequest).addListeners(
                  ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE,
                  f -> {
                    if (f.isSuccess()) {
                      handle.sent();
                    }
                  });
                if (LOGGER.isTraceEnabled() && channel.eventLoop() instanceof SingleThreadEventLoop) {
                  LOGGER.trace(
                    "Event loop group {} has {} pending tasks",
                    channel.eventLoop().parent(),
                    ((SingleThreadEventLoop) channel.eventLoop()).pendingTasks());
                }
              } finally {
                bindLock.readLock().unlock();
              }
            } else {
              handle.exception(new LdapException("Bind in progress"));
            }
          }
        } finally {
          reconnectLock.readLock().unlock();
        }
      } else {
        handle.exception(new LdapException("Reconnect in progress"));
      }
    } catch (Exception e) {
      handle.exception(e);
    }
  }


  /**
   * Closes this connection. Abandons all pending responses and sends an unbind to the LDAP server if the connection is
   * open when this method is invoked.
   *
   * @param  controls  to send with the unbind request when closing the connection
   */
  @Override
  public void close(final RequestControl... controls)
  {
    LOGGER.trace("Closing connection {}", this);
    closeLock.lock();
    try {
      pendingResponses.close();
      if (isOpen()) {
        LOGGER.trace("connection {} is open, initiate orderly shutdown", this);
        channel.closeFuture().removeListener(closeListener);
        // abandon outstanding requests
        if (pendingResponses.size() > 0) {
          LOGGER.info("Abandoning requests {} for {} to close connection", pendingResponses, this);
          pendingResponses.abandonRequests();
        }
        // unbind
        final UnbindRequest req = new UnbindRequest();
        req.setControls(controls);
        operation(req);
        channel.close().addListener(new LogFutureListener());
      } else {
        LOGGER.trace("connection {} already closed", this);
        if (!(connectionConfig.getAutoReconnect() && connectionConfig.getAutoReplay())) {
          notifyOperationHandlesOfClose();
        }
      }
      LOGGER.debug("Closed connection {}", this);
    } finally {
      channel = null;
      connectTime = null;
      closeLock.unlock();
    }
  }


  /**
   * Sends an exception notification to all pending responses that the connection has been closed.
   */
  protected void notifyOperationHandlesOfClose()
  {
    if (pendingResponses.size() > 0) {
      LOGGER.debug("Notifying operation handles {} for {} of connection close", pendingResponses, this);
      if (inboundException != null) {
        pendingResponses.notifyOperationHandles(inboundException);
      } else {
        pendingResponses.notifyOperationHandles(new LdapException("Connection closed"));
      }
    }
  }


  /**
   * Attempts to reestablish the channel for this connection.
   *
   * @throws  IllegalStateException  if the connection is open
   */
  protected void reconnect()
  {
    if (isOpen()) {
      throw new IllegalStateException("Reconnect cannot be invoked when the connection is open");
    }
    if (isOpening()) {
      LOGGER.debug("Open in progress, ignoring reconnect for connection {}", this);
      notifyOperationHandlesOfClose();
      return;
    }
    LOGGER.trace("Reconnecting connection {}", this);
    if (reconnectLock.writeLock().tryLock()) {
      List<DefaultOperationHandle> replayOperations = null;
      try {
        try {
          reopen(new ClosedRetryMetadata(lastSuccessfulOpen, inboundException));
          LOGGER.info("auto reconnect finished for connection {}", this);
        } catch (Exception e) {
          LOGGER.debug("auto reconnect failed for connection {}", this, e);
        }
        // replay operations that have been sent, but have not received a response
        // notify all other operations
        if (isOpen() && connectionConfig.getAutoReplay()) {
          replayOperations = pendingResponses.handles().stream()
            .filter(h -> h.getSentTime() != null && h.getReceivedTime() == null)
            .collect(Collectors.toList());
          replayOperations.forEach(h -> pendingResponses.remove(h.getMessageID()));
          // notify outstanding requests that have received a response
          notifyOperationHandlesOfClose();
        } else {
          notifyOperationHandlesOfClose();
        }
      } finally {
        reconnectLock.writeLock().unlock();
      }
      if (replayOperations != null && replayOperations.size() > 0) {
        replayOperations.forEach(h -> write(h));
      }
      LOGGER.debug("Reconnect for connection {} finished", this);
    } else {
      throw new IllegalStateException("Reconnect is already in progress");
    }
  }


  /**
   * Returns whether the underlying Netty channel is open. See {@link Channel#isOpen()}.
   *
   * @return  whether the Netty channel is open
   */
  public boolean isOpen()
  {
    return channel != null && channel.isOpen();
  }


  /**
   * Returns whether this connection is currently attempting to open.
   *
   * @return  whether the Netty channel is in the process of opening
   */
  private boolean isOpening()
  {
    if (openLock.tryLock()) {
      try {
        return false;
      } finally {
        openLock.unlock();
      }
    } else {
      return true;
    }
  }


  /**
   * Returns whether this connection is currently attempting to close.
   *
   * @return  whether the Netty channel is in the process of closing
   */
  private boolean isClosing()
  {
    if (closeLock.tryLock()) {
      try {
        return false;
      } finally {
        closeLock.unlock();
      }
    } else {
      return true;
    }
  }


  /**
   * Throws an exception if the Netty channel is closed. See {@link #isOpen()}.
   *
   * @throws  LdapException  if the connection is closed
   */
  private void throwIfClosed()
    throws LdapException
  {
    if (!isOpen()) {
      throw new LdapException("Connection is closed");
    }
  }


  @Override
  public String toString()
  {
    return new StringBuilder(getClass().getName()).append("@").append(hashCode()).append("::")
      .append("ldapUrl=").append(ldapURL).append(", ")
      .append("isOpen=").append(isOpen()).append(", ")
      .append("connectTime=").append(connectTime).append(", ")
      .append("connectionConfig=").append(connectionConfig).toString();
  }


  /** Bind specific operation handle that locks other operations until the bind completes. */
  public class BindOperationHandle extends DefaultOperationHandle<BindRequest, BindResponse>
  {


    /**
     * Creates a new bind operation handle.
     *
     * @param  req  bind request to expect a response for
     * @param  conn  the request will be executed on
     * @param  timeout  duration to wait for a response
     */
    BindOperationHandle(final BindRequest req, final TransportConnection conn, final Duration timeout)
    {
      super(req, conn, timeout);
    }


    @Override
    public BindOperationHandle send()
    {
      throw new UnsupportedOperationException("Bind requests are synchronous, invoke execute");
    }


    @Override
    public BindResponse await()
    {
      throw new UnsupportedOperationException("Bind requests are synchronous, invoke execute");
    }


    @Override
    public BindResponse execute()
      throws LdapException
    {
      if (bindLock.writeLock().tryLock()) {
        try {
          super.send();
          return super.await();
        } finally {
          bindLock.writeLock().unlock();
        }
      } else {
        throw new IllegalStateException("Bind in progress, cannot send bind request");
      }
    }
  }


  /**
   * Listener that logs the future success state when it occurs.
   */
  private class LogFutureListener implements ChannelFutureListener
  {


    @Override
    public void operationComplete(final ChannelFuture future)
    {
      if (future.isSuccess()) {
        LOGGER.trace("Operation channel success on {}", NettyConnection.this);
      } else {
        LOGGER.warn("Operation channel error on {}", NettyConnection.this, future.cause());
      }
    }
  }


  /**
   * Listener for channel close events. Invokes {@link #close()} to cleanup resources from a non-requested close event.
   * If {@link ConnectionConfig#getAutoReconnect()} is true, a connection reconnect is attempted.
   */
  private class CloseFutureListener implements ChannelFutureListener
  {

    /** Whether this listener is in the process of reconnecting. */
    private AtomicBoolean reconnecting = new AtomicBoolean();


    @Override
    public void operationComplete(final ChannelFuture future)
    {
      inboundException = future.cause();
      LOGGER.debug(
        "Close listener invoked for {} with future {} and cause {}",
        NettyConnection.this,
        future,
        inboundException != null ? inboundException.getClass() : null,
        inboundException);
      final boolean isOpening = isOpening();
      close();
      if (connectionConfig.getAutoReconnect() && !isOpening && !reconnecting.get()) {
        LOGGER.trace("scheduling reconnect thread for connection {}", NettyConnection.this);
        connectionExecutor.execute(
          () -> {
            reconnecting.set(true);
            try {
              reconnect();
            } catch (Exception e) {
              LOGGER.warn("Reconnect attempt failed for {}", NettyConnection.this, e);
            } finally {
              reconnecting.set(false);
            }
          });
      } else {
        notifyOperationHandlesOfClose();
      }
    }
  }


  /**
   * Sets up the Netty pipeline for this connection. Handler configuration looks like:
   *
   *  +-------------------------------------------------------------------+
   *  |                           ChannelPipeline                         |
   *  |                                                                   |
   *  |    +-----------------------+            +-----------+----------+  |
   *  |    | InboundMessageHandler |            | RequestEncoder       |  |
   *  |    +----------+------------+            +-----------+----------+  |
   *  |              /|\                                    |             |
   *  |               |                                     |             |
   *  |    +----------+------------+                        |             |
   *  |    | MessageDecoder        |                        |             |
   *  |    +----------+------------+                        |             |
   *  |              /|\                                    |             |
   *  |               |                                     |             |
   *  |    +----------+------------+                        |             |
   *  |    | MessageFrameDecoder   |                        |             |
   *  |    +----------+------------+                        |             |
   *  |              /|\                                    |             |
   *  |               |                                    \|/            |
   *  |    +----------+------------+            +-----------+----------+  |
   *  |    | I/O READ              |            | I/O WRITE            |  |
   *  |    +----------+------------+            +-----------+----------+  |
   *  |              /|\                                   \|/            |
   *  +---------------+-------------------------------------+-------------+
   *
   */
  private class ClientInitializer extends ChannelInitializer<SocketChannel>
  {

    /** SSL handler. */
    private final SslHandler sslHandler;


    /**
     * Creates a new client initializer.
     *
     * @param  handler  SSL handler or null
     */
    ClientInitializer(final SslHandler handler)
    {
      sslHandler = handler;
    }


    @Override
    public void initChannel(final SocketChannel ch)
    {
      if (sslHandler != null) {
        ch.pipeline().addFirst("ssl", sslHandler);
      }
      if (LOGGER.isDebugEnabled()) {
        ch.pipeline().addLast("logger", new LoggingHandler(LogLevel.DEBUG));
      }
      // inbound handlers are processed top to bottom
      // outbound handlers are processed bottom to top
      ch.pipeline().addLast("frame_decoder", new MessageFrameDecoder());
      ch.pipeline().addLast("response_decoder", new MessageDecoder());
      if (messageWorkerGroup != null) {
        ch.pipeline().addLast(messageWorkerGroup, "message_handler", new InboundMessageHandler());
      } else {
        ch.pipeline().addLast("message_handler", new InboundMessageHandler());
      }
      if (!ch.config().isAutoRead()) {
        ch.pipeline().addLast("next_message_handler", READ_NEXT_MESSAGE);
      }
      ch.pipeline().addLast("request_encoder", REQUEST_ENCODER);
      if (connectionConfig.getConnectionValidator() != null) {
        ch.pipeline().addLast("validate_conn", new ValidatorHandler(connectionConfig.getConnectionValidator()));
      }
      ch.pipeline().addLast("inbound_exception_handler", new InboundExceptionHandler());
    }


    /**
     * Returns whether the SSL pipeline is in use.
     *
     * @return  whether the SSL pipeline is in use
     */
    public boolean isSsl()
    {
      return sslHandler != null;
    }
  }


  /** Enum that describes the state of an LDAP message in the pipeline. */
  protected enum MessageStatus
  {
    /** All bytes for a message have been read. */
    READ,

    /** Bytes have been decoded into a concrete message. */
    DECODED,

    /** Message has passed through the entire pipeline. */
    HANDLED,
  }


  /**
   * Encodes an LDAP request into it's DER bytes. See {@link EncodedRequest#getEncoded()}. This class prefers direct
   * byte buffers.
   */
  @ChannelHandler.Sharable
  protected static class RequestEncoder extends MessageToByteEncoder<EncodedRequest>
  {


    @Override
    protected void encode(final ChannelHandlerContext ctx, final EncodedRequest msg, final ByteBuf out)
    {
      out.writeBytes(msg.getEncoded());
    }
  }


  /**
   * Decodes byte buffer into a concrete LDAP response message. See {@link ResponseParser}. Note that {@link
   * ByteToMessageDecoder} is stateful so this class cannot be marked sharable.
   */
  protected static class MessageDecoder extends ByteToMessageDecoder
  {


    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out)
    {
      LOGGER.trace("received {} bytes", in.readableBytes());
      final ResponseParser parser = new ResponseParser();
      final Message message =  parser.parse(new NettyDERBuffer(in))
        .orElseThrow(() -> new IllegalArgumentException("No response found"));
      out.add(message);
      if (ctx != null) {
        ctx.fireUserEventTriggered(MessageStatus.DECODED);
      }
    }
  }


  /**
   * Matches an inbound LDAP response message to it's operation handle and removes that handle from the response queue.
   * Notifies all operation handles when an unsolicited notification arrives.
   */
  private class InboundMessageHandler extends SimpleChannelInboundHandler<Message>
  {


    @Override
    @SuppressWarnings("unchecked")
    protected void channelRead0(final ChannelHandlerContext ctx, final Message msg)
    {
      try {
        final DefaultOperationHandle handle = pendingResponses.get(msg.getMessageID());
        LOGGER.debug("Received response message {} for handle {}", msg, handle);
        if (handle != null) {
          if (msg instanceof LdapEntry) {
            for (LdapAttribute a : ((LdapEntry) msg).getAttributes()) {
              a.configureBinary(((SearchRequest) handle.getRequest()).getBinaryAttributes());
            }
            ((DefaultSearchOperationHandle) handle).entry((LdapEntry) msg);
          } else if (msg instanceof SearchResultReference) {
            ((DefaultSearchOperationHandle) handle).reference((SearchResultReference) msg);
          } else if (msg instanceof Result) {
            if (pendingResponses.remove(msg.getMessageID()) == null) {
              LOGGER.warn(
                "Processed message {} that no longer exists for {}",
                msg.getMessageID(),
                NettyConnection.this);
            }
            if (msg instanceof ExtendedResponse) {
              ((DefaultExtendedOperationHandle) handle).extended((ExtendedResponse) msg);
            } else if (msg instanceof CompareResponse) {
              ((DefaultCompareOperationHandle) handle).compare((CompareResponse) msg);
            }
            if (msg.getControls() != null && msg.getControls().length > 0) {
              Stream.of(msg.getControls()).forEach(handle::control);
            }
            if (((Result) msg).getReferralURLs() != null && ((Result) msg).getReferralURLs().length > 0) {
              handle.referral(((Result) msg).getReferralURLs());
            }
            handle.result((Result) msg);
          } else if (msg instanceof IntermediateResponse) {
            handle.intermediate((IntermediateResponse) msg);
          } else {
            throw new IllegalStateException("Unknown message type: " + msg);
          }
        } else if (msg instanceof UnsolicitedNotification) {
          LOGGER.info("Received UnsolicitedNotification {} for {}", msg, NettyConnection.this);
          pendingResponses.notifyOperationHandles((UnsolicitedNotification) msg);
        } else {
          LOGGER.warn(
            "Received response message {} without matching request in {} for {}",
            msg,
            pendingResponses,
            this);
        }
      } finally {
        if (ctx != null) {
          ctx.fireUserEventTriggered(MessageStatus.HANDLED);
        }
      }
    }
  }


  /**
   * Initiates a channel read when an LDAP message has been processed.
   */
  @ChannelHandler.Sharable
  protected static class InboundAutoReadEventHandler extends SimpleUserEventChannelHandler<MessageStatus>
  {

    /** Logger for this class. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    protected void eventReceived(final ChannelHandlerContext ctx, final MessageStatus evt)
    {
      logger.trace("Received event {}", evt);
      if (MessageStatus.HANDLED == evt) {
        if (!ctx.channel().config().isAutoRead()) {
          ctx.read();
        }
      }
    }
  }


  /**
   * Schedules a connection validator to run based on it's strategy. If the validator fails an exception caught is fired
   * in the pipeline.
   */
  private class ValidatorHandler extends ChannelInboundHandlerAdapter
  {

    /** Connection validator. */
    private final ConnectionValidator connectionValidator;

    /** Future to track execution status. */
    private ScheduledFuture sf;


    /**
     * Creates a new validator handler.
     *
     * @param  validator  to execute
     */
    ValidatorHandler(final ConnectionValidator validator)
    {
      connectionValidator = validator;
    }


    @Override
    public void channelActive(final ChannelHandlerContext ctx)
    {
      sf = ctx.executor().scheduleAtFixedRate(
        () -> {
          final java.util.concurrent.Future<Boolean> f = connectionExecutor.submit(
            () -> connectionValidator.apply(NettyConnection.this));
          boolean success = false;
          try {
            success = f.get(connectionValidator.getValidateTimeout().toMillis(), TimeUnit.MILLISECONDS);
          } catch (Exception e) {
            LOGGER.debug("validating {} threw unexpected exception", NettyConnection.this, e);
          }
          if (!success) {
            ctx.fireExceptionCaught(new LdapException("Connection validation failed for " + NettyConnection.this));
          }
        },
        connectionValidator.getValidatePeriod().toMillis(),
        connectionValidator.getValidatePeriod().toMillis(),
        TimeUnit.MILLISECONDS);
    }


    @Override
    public void channelInactive(final ChannelHandlerContext ctx)
    {
      if (sf != null) {
        sf.cancel(true);
      }
    }
  }


  /**
   * Sets {@link #inboundException} and closes the channel when an exception occurs.
   */
  private class InboundExceptionHandler extends ChannelInboundHandlerAdapter
  {


    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause)
    {
      LOGGER.warn("Inbound handler caught exception for {}", NettyConnection.this, cause);
      inboundException = cause;
      if (channel != null && !isClosing()) {
        channel.close().addListener(new LogFutureListener());
      }
    }
  }
}
