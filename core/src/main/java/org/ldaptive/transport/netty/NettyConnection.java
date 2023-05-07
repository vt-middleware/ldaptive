/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
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
import org.ldaptive.sasl.DefaultSaslClientRequest;
import org.ldaptive.sasl.QualityOfProtection;
import org.ldaptive.sasl.SaslClient;
import org.ldaptive.sasl.SaslClientRequest;
import org.ldaptive.ssl.HostnameResolver;
import org.ldaptive.ssl.HostnameVerifierAdapter;
import org.ldaptive.ssl.SSLContextInitializer;
import org.ldaptive.ssl.SslConfig;
import org.ldaptive.transport.DefaultCompareOperationHandle;
import org.ldaptive.transport.DefaultExtendedOperationHandle;
import org.ldaptive.transport.DefaultOperationHandle;
import org.ldaptive.transport.DefaultSaslClient;
import org.ldaptive.transport.DefaultSearchOperationHandle;
import org.ldaptive.transport.ResponseParser;
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
  private static final AutoReadEventHandler READ_NEXT_MESSAGE = new AutoReadEventHandler();

  /** Type of channel. */
  private final Class<? extends Channel> channelType;

  /** Event worker group used to process I/O. */
  private final EventLoopGroup ioWorkerGroup;

  /** Event worker group used to process inbound messages. */
  private final EventLoopGroup messageWorkerGroup;

  /** Whether to shutdown the event loop groups on {@link #close()}. */
  private boolean shutdownOnClose;

  /** Netty channel configuration options. */
  private final Map<ChannelOption, Object> channelOptions;

  /** Queue holding requests that haven't received a response. */
  private final HandleMap pendingResponses;

  /** Listener notified when the connection is closed. */
  private final CloseFutureListener closeListener = new CloseFutureListener();

  /** Message ID counter, incremented as requests are sent. */
  private final AtomicInteger messageID = new AtomicInteger(1);

  /** Block operations while a reconnect is occurring. */
  private final ReentrantReadWriteLock reconnectLock = new ReentrantReadWriteLock();

  /** Operation lock when a bind occurs. */
  private final ReentrantReadWriteLock bindLock = new ReentrantReadWriteLock();

  /**
   * Executor for scheduling various connection related tasks that cannot or should not be handled by the netty
   * event loop groups. Reconnects in particular require a dedicated thread as the event loop group may be shared or may
   * not be configured with enough threads to handle the task.
   */
  private ExecutorService connectionExecutor;

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
   * class type and event loop group are tightly coupled in this regard.
   *
   * @param  config  connection configuration
   * @param  type  type of channel
   * @param  ioGroup  event loop group that handles I/O and supports the channel type, cannot be null
   * @param  messageGroup  event loop group that handles inbound messages, can be null
   * @param  shutdownGroups  whether to shutdown the event loop groups when the connection is closed
   */
  public NettyConnection(
    final ConnectionConfig config,
    final Class<? extends Channel> type,
    final EventLoopGroup ioGroup,
    final EventLoopGroup messageGroup,
    final boolean shutdownGroups)
  {
    super(config);
    if (ioGroup == null) {
      throw new NullPointerException("I/O worker group cannot be null");
    }
    channelType = type;
    ioWorkerGroup = ioGroup;
    messageWorkerGroup = messageGroup;
    channelOptions = new HashMap<>();
    channelOptions.put(ChannelOption.SO_KEEPALIVE, true);
    channelOptions.put(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) config.getConnectTimeout().toMillis());
    if (config.getTransportOptions() != null && !config.getTransportOptions().isEmpty()) {
      for (Map.Entry<String, ?> e : config.getTransportOptions().entrySet()) {
        final ChannelOption<?> option = ChannelOption.valueOf(e.getKey());
        final Object value = e.getValue();
        if (value instanceof String) {
          channelOptions.put(option, convertChannelOption((String) value));
        } else {
          channelOptions.put(option, value);
        }
      }
    }
    shutdownOnClose = shutdownGroups;
    pendingResponses = new HandleMap();
  }


  /**
   * Performs a best effort at converting a channel option value to the correct type. Handles Boolean and Integer types.
   *
   * @param  value  to convert
   *
   * @return  converted value or the supplied value if no conversion occurred
   */
  private Object convertChannelOption(final String value)
  {
    if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
      return Boolean.valueOf(value);
    } else {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException ignored) {}
    }
    return value;
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
    if (ioWorkerGroup.isShutdown()) {
      throw new IllegalStateException("Attempt to open connection with shutdown event loop on " + this);
    }
    final Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(ioWorkerGroup);
    bootstrap.channel(channelType);
    channelOptions.forEach(bootstrap::option);
    bootstrap.handler(initializer);
    LOGGER.trace("created netty bootstrap {} with worker group {} for {}", bootstrap, ioWorkerGroup, this);
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
      false);
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
    LOGGER.trace("opening connection {}", this);
    if (openLock.tryLock()) {
      try {
        inboundException = null;
        ldapURL = url;
        if (connectionExecutor == null) {
          connectionExecutor = Executors.newSingleThreadExecutor(
            r -> {
              final Thread t = new Thread(r, "ldaptive-" + getClass().getSimpleName() + "@" + hashCode());
              t.setDaemon(true);
              return t;
            });
        }
        channel = connectInternal();
        channel.closeFuture().addListener(closeListener);
        pendingResponses.open();
        // startTLS request must occur after the connection is ready
        if (connectionConfig.getUseStartTLS()) {
          final Result result = operation(new StartTLSRequest());
          if (!result.isSuccess()) {
            throw new ConnectException(
              ResultCode.CONNECT_ERROR,
              "StartTLS returned response: " + result + " for URL " + url);
          }
        }
        // initialize the connection
        if (connectionConfig.getConnectionInitializers() != null) {
          for (ConnectionInitializer initializer : connectionConfig.getConnectionInitializers()) {
            final Result result = initializer.initialize(this);
            if (!result.isSuccess()) {
              throw new ConnectException(
                ResultCode.CONNECT_ERROR,
                "Connection initializer " + initializer + " returned response: " + result + " for URL " + url);
            }
          }
        }
        connectTime = Instant.now();
        LOGGER.debug("Netty opened connection {}", this);
      } catch (Exception e) {
        LOGGER.error("Connection open failed for {}", this, e);
        try {
          notifyOperationHandlesOfClose();
          pendingResponses.close();
          if (isOpen()) {
            channel.closeFuture().removeListener(closeListener);
            channel.close().addListener(new LogFutureListener());
          }
        } finally {
          pendingResponses.clear();
          channel = null;
        }
        throw e;
      } finally {
        openLock.unlock();
      }
    } else {
      LOGGER.warn("Open lock {} could not be acquired by {}", openLock, Thread.currentThread());
      throw new ConnectException(ResultCode.CONNECT_ERROR, "Open in progress");
    }
  }


  @Override
  public LdapURL getLdapURL()
  {
    return ldapURL;
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
        throw new ConnectException(ResultCode.CONNECT_ERROR, e);
      }
    }
    final ClientInitializer initializer = new ClientInitializer(handler);
    final Bootstrap bootstrap = createBootstrap(initializer);

    final CountDownLatch channelLatch = new CountDownLatch(1);
    LOGGER.trace("connecting to bootstrap {} with URL {} for {}", bootstrap, ldapURL, this);
    final ChannelFuture future;
    if (ldapURL.getInetAddress() != null) {
      future = bootstrap.connect(ldapURL.getInetAddress(), ldapURL.getPort());
    } else {
      future = bootstrap.connect(new InetSocketAddress(ldapURL.getHostname(), ldapURL.getPort()));
    }
    future.addListener((ChannelFutureListener) f -> channelLatch.countDown());
    try {
      // wait until the connection future is complete
      // note that the wait time is controlled by the connectTimeout property in ConnectionConfig
      // if a deadlock occurs here, there may not be enough threads available in the worker group
      if (!channelLatch.await(connectionConfig.getConnectTimeout().multipliedBy(2).toMillis(), TimeUnit.MILLISECONDS)) {
        LOGGER.warn(
          "Error connecting to {} for {}. connectTimeout was not honored, check number of available threads",
          ldapURL,
          this);
        future.cancel(true);
      }
    } catch (InterruptedException e) {
      future.cancel(true);
    }
    LOGGER.trace("bootstrap connect returned {} for {}", future, this);
    if (future.isCancelled()) {
      throw new ConnectException(ResultCode.CONNECT_ERROR, "Connection cancelled");
    }
    if (!future.isSuccess()) {
      if (future.cause() != null) {
        throw new ConnectException(ResultCode.SERVER_DOWN, future.cause());
      } else {
        throw new ConnectException(ResultCode.SERVER_DOWN, "Connection could not be opened");
      }
    }

    if (initializer.isSsl()) {
      // socket is connected, wait for SSL handshake to complete
      try {
        waitForSSLHandshake(future.channel());
      } catch (SSLException e) {
        future.channel().close();
        throw new ConnectException(ResultCode.CONNECT_ERROR, e);
      }
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
      final SSLParameters sslParams = engine.getSSLParameters();
      sslParams.setEndpointIdentificationAlgorithm("LDAPS");
      engine.setSSLParameters(sslParams);
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
      if (!sslLatch.await(handler.getHandshakeTimeoutMillis() * 2, TimeUnit.MILLISECONDS)) {
        LOGGER.warn(
          "Error starting SSL with {} for {}. handShakeTimeout was not honored, check number of available threads",
          ldapURL,
          this);
        sslFuture.cancel(true);
      }
    } catch (InterruptedException e) {
      sslFuture.cancel(true);
    }
    if (sslFuture.isCancelled()) {
      throw new SSLException("SSL handshake cancelled");
    }
    if (!sslFuture.isSuccess()) {
      final SSLException sslEx;
      if (sslFuture.cause() != null) {
        sslEx = new SSLException(sslFuture.cause());
      } else {
        sslEx = new SSLException("SSL handshake failure");
      }
      if (inboundException != null) {
        sslEx.addSuppressed(inboundException);
      }
      throw sslEx;
    }
    if (connectionConfig.getSslConfig() != null && connectionConfig.getSslConfig().getHostnameVerifier() != null) {
      final HostnameVerifier verifier = new HostnameVerifierAdapter(
        connectionConfig.getSslConfig().getHostnameVerifier());
      final SSLSession session = handler.engine().getSession();
      final HostnameResolver resolver = new HostnameResolver(session);
      final String hostname  = resolver.resolve();
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
      throw new ConnectException(ResultCode.LOCAL_ERROR, "SslHandler is already in use");
    }
    final DefaultExtendedOperationHandle handle = new DefaultExtendedOperationHandle(
      request,
      this,
      connectionConfig.getStartTLSTimeout());
    final Result result;
    try {
      result = handle.execute();
    } catch (LdapException e) {
      throw new ConnectException(ResultCode.CONNECT_ERROR, "StartTLS operation failed", e);
    }
    if (result.isSuccess()) {
      try {
        channel.pipeline().addFirst("ssl", createSslHandler(connectionConfig));
        waitForSSLHandshake(channel);
      } catch (SSLException e) {
        throw new ConnectException(ResultCode.CONNECT_ERROR, e);
      }
    } else {
      throw new ConnectException(ResultCode.CONNECT_ERROR, "StartTLS operation failed with result " + result);
    }
    return result;
  }


  @Override
  protected void operation(final UnbindRequest request)
  {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("unbind request {} with pending responses {} for {}", request, pendingResponses, this);
    } else if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Unbind request {} with {} pending responses for {}", request, pendingResponses.size(), this);
    }
    if (reconnectLock.readLock().tryLock()) {
      try {
        if (!isOpen()) {
          LOGGER.warn("Attempt to unbind ignored, connection {} is not open", this);
        } else {
          if (bindLock.readLock().tryLock()) {
            try {
              final EncodedRequest encodedRequest = new EncodedRequest(getAndIncrementMessageID(), request);
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
   * Performs a SASL bind operation that uses a custom client.
   *
   * @param  request  to send
   *
   * @return  result of the GSS-API bind operation
   *
   * @throws  LdapException  if the operation fails or another bind is in progress
   */
  @Override
  @SuppressWarnings("unchecked")
  public BindResponse operation(final SaslClientRequest request)
    throws LdapException
  {
    throwIfClosed();
    if (!bindLock.writeLock().tryLock()) {
      throw new LdapException(ResultCode.LOCAL_ERROR, "Operation in progress, cannot send bind request");
    }
    try {
      final SaslClient client = request.getSaslClient();
      final BindResponse result;
      try {
        result = client.bind(this, request);
      } catch (Exception e) {
        if (e instanceof LdapException) {
          throw (LdapException) e;
        } else {
          throw new LdapException(ResultCode.LOCAL_ERROR, e);
        }
      }
      if (result == null) {
        throw new LdapException(ResultCode.LOCAL_ERROR, "SASL operation failed");
      }
      return result;
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
  @SuppressWarnings("unchecked")
  public BindResponse operation(final DefaultSaslClientRequest request)
    throws LdapException
  {
    throwIfClosed();
    if (!bindLock.writeLock().tryLock()) {
      throw new LdapException(ResultCode.LOCAL_ERROR, "Operation in progress, cannot send bind request");
    }
    try {
      final SaslClient client = request.getSaslClient();
      if (client instanceof DefaultSaslClient) {
        final DefaultSaslClient defaultClient = (DefaultSaslClient) client;
        final BindResponse response;
        boolean saslSecurity = false;
        try {
          response = defaultClient.bind(this, request);
          if (response.getResultCode() == ResultCode.SUCCESS) {
            final QualityOfProtection qop = defaultClient.getQualityOfProtection();
            if (QualityOfProtection.AUTH_INT == qop || QualityOfProtection.AUTH_CONF == qop) {
              if (channel.pipeline().get(SaslHandler.class) != null) {
                channel.pipeline().remove(SaslHandler.class);
              }
              if (channel.pipeline().get(SslHandler.class) != null) {
                channel.pipeline().addAfter("ssl", "sasl", new SaslHandler(defaultClient.getClient()));
              } else {
                channel.pipeline().addFirst("sasl", new SaslHandler(defaultClient.getClient()));
              }
              saslSecurity = true;
            }
          }
          return response;
        } catch (Exception e) {
          throw new LdapException(ResultCode.LOCAL_ERROR, "SASL bind operation failed", e);
        } finally {
          if (!saslSecurity) {
            defaultClient.dispose();
          }
        }
      } else {
        final BindResponse result;
        try {
          result = client.bind(this, request);
        } catch (Exception e) {
          if (e instanceof LdapException) {
            throw (LdapException) e;
          } else {
            throw new LdapException(ResultCode.LOCAL_ERROR, e);
          }
        }
        if (result == null) {
          throw new LdapException(ResultCode.LOCAL_ERROR, "SASL GSSAPI operation failed");
        }
        return result;
      }
    } finally {
      bindLock.writeLock().unlock();
    }
  }


  @Override
  public void operation(final AbandonRequest request)
  {
    final DefaultOperationHandle handle = pendingResponses.remove(request.getMessageID());
    if (handle == null && pendingResponses.isOpen()) {
      LOGGER.warn(
        "Attempt to abandon message {} that no longer exists for {}",
        request.getMessageID(),
        NettyConnection.this);
    }
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace(
        "abandon {} {} with pending responses {}",
        handle != null ? "handle" : "messageID",
        handle != null ? handle : request.getMessageID(),
        pendingResponses);
    } else if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
        "Abandon {} {} with {} pending responses",
        handle != null ? "handle" : "messageID",
        handle != null ? handle : request.getMessageID(),
        pendingResponses.size());
    }
    if (reconnectLock.readLock().tryLock()) {
      try {
        if (!isOpen()) {
          if (handle != null) {
            handle.exception(new LdapException(ResultCode.SERVER_DOWN, "Connection is not open"));
          }
        } else {
          if (bindLock.readLock().tryLock()) {
            try {
              final EncodedRequest encodedRequest = new EncodedRequest(getAndIncrementMessageID(), request);
              channel.writeAndFlush(encodedRequest).addListener(
                ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            } finally {
              bindLock.readLock().unlock();
            }
          } else {
            if (handle != null) {
              handle.exception(new LdapException(ResultCode.LOCAL_ERROR, "Bind in progress"));
            }
          }
        }
      } finally {
        reconnectLock.readLock().unlock();
      }
    } else {
      if (handle != null) {
        handle.exception(new LdapException(ResultCode.SERVER_DOWN, "Reconnect in progress"));
      }
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
  @SuppressWarnings("unchecked")
  protected void write(final DefaultOperationHandle handle)
  {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("write handle {} with pending responses {}", handle, pendingResponses);
    } else if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Write handle {} with {} pending responses", handle, pendingResponses.size());
    }
    try {
      final boolean gotReconnectLock;
      if (Duration.ZERO.equals(connectionConfig.getReconnectTimeout())) {
        reconnectLock.readLock().lock();
        gotReconnectLock = true;
      } else {
        gotReconnectLock = reconnectLock.readLock().tryLock(
          connectionConfig.getReconnectTimeout().toMillis(), TimeUnit.MILLISECONDS);
      }
      if (gotReconnectLock) {
        try {
          if (!isOpen()) {
            handle.exception(new LdapException(ResultCode.SERVER_DOWN, "Connection is closed, write aborted"));
          } else {
            if (bindLock.readLock().tryLock()) {
              try {
                final EncodedRequest encodedRequest = new EncodedRequest(
                  getAndIncrementMessageID(),
                  handle.getRequest());
                handle.messageID(encodedRequest.getMessageID());
                try {
                  if (pendingResponses.put(encodedRequest.getMessageID(), handle) != null) {
                    throw new LdapException(
                      ResultCode.ENCODING_ERROR,
                      "Request already exists for ID " + encodedRequest.getMessageID());
                  }
                } catch (LdapException e) {
                  if (inboundException != null) {
                    throw new LdapException(ResultCode.SERVER_DOWN, e.getMessage(), inboundException);
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
                    "event loop group {} has {} pending tasks for {}",
                    channel.eventLoop().parent(),
                    ((SingleThreadEventLoop) channel.eventLoop()).pendingTasks(),
                    this);
                }
              } finally {
                bindLock.readLock().unlock();
              }
            } else {
              handle.exception(new LdapException(ResultCode.LOCAL_ERROR, "Bind in progress"));
            }
          }
        } finally {
          reconnectLock.readLock().unlock();
        }
      } else {
        handle.exception(new LdapException(ResultCode.SERVER_DOWN, "Reconnect in progress"));
      }
    } catch (Exception e) {
      if (e instanceof LdapException) {
        handle.exception((LdapException) e);
      } else {
        handle.exception(new LdapException(ResultCode.LOCAL_ERROR, e));
      }
    }
  }


  @Override
  protected void complete(final DefaultOperationHandle handle)
  {
    if (handle != null && handle.getMessageID() != null) {
      pendingResponses.remove(handle.getMessageID());
    }
  }


  /**
   * Returns the value of the next message ID and increments the counter.
   *
   * @return  message ID
   */
  int getAndIncrementMessageID()
  {
    return messageID.getAndUpdate(i -> i < Integer.MAX_VALUE ? i + 1 : 1);
  }


  /**
   * Returns the value of the next message ID.
   *
   * @return  message ID
   */
  int getMessageID()
  {
    return messageID.get();
  }


  /**
   * Sets the value of the next message ID.
   *
   * @param  i  message ID
   */
  void setMessageID(final int i)
  {
    if (i < 1) {
      throw new IllegalArgumentException("messageID must be greater than zero");
    }
    messageID.set(i);
  }


  /**
   * Returns the channel options.
   *
   * @return  channel options
   */
  Map<ChannelOption, Object> getChannelOptions()
  {
    return channelOptions;
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
    LOGGER.trace("closing connection {}", this);
    if (closeLock.tryLock()) {
      try {
        pendingResponses.close();
        if (connectionExecutor != null) {
          connectionExecutor.shutdown();
        }
        if (isOpen()) {
          LOGGER.trace("connection {} is open, initiate orderly shutdown", this);
          channel.closeFuture().removeListener(closeListener);
          // abandon outstanding requests
          if (pendingResponses.size() > 0) {
            if (LOGGER.isTraceEnabled()) {
              LOGGER.trace("abandoning requests {} for {} to close connection", pendingResponses, this);
            } else if (LOGGER.isInfoEnabled()) {
              LOGGER.info("Abandoning {} requests for {} to close connection", pendingResponses.size(), this);
            }
            pendingResponses.abandonRequests();
          }
          // unbind
          final UnbindRequest req = new UnbindRequest();
          req.setControls(controls);
          operation(req);
          channel.close().addListener(new LogFutureListener());
        } else {
          LOGGER.trace("connection {} already closed", this);
          notifyOperationHandlesOfClose();
        }
        LOGGER.info("Closed connection {}", this);
      } finally {
        pendingResponses.clear();
        connectionExecutor = null;
        channel = null;
        connectTime = null;
        if (shutdownOnClose) {
          NettyUtils.shutdownGracefully(ioWorkerGroup);
          LOGGER.trace("shutdown worker group {} for {}", ioWorkerGroup, this);
          if (messageWorkerGroup != null) {
            NettyUtils.shutdownGracefully(messageWorkerGroup);
            LOGGER.trace("shutdown worker group {} for {}", messageWorkerGroup, this);
          }
        }
        closeLock.unlock();
      }
    } else {
      LOGGER.debug("Close lock {} could not be acquired by {}", closeLock, Thread.currentThread());
    }
  }


  /**
   * Sends an exception notification to all pending responses that the connection has been closed. Since this invokes
   * any configured exception handlers, notifications will use the {@link #messageWorkerGroup} if it is configured.
   */
  private void notifyOperationHandlesOfClose()
  {
    if (pendingResponses.size() > 0) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("notifying operation handles {} of connection close for {}", pendingResponses, this);
      } else if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Notifying {} operation handles of connection close for {}", pendingResponses.size(), this);
      }
      final LdapException ex;
      if (inboundException == null) {
        ex = new LdapException(ResultCode.SERVER_DOWN, "Connection closed");
      } else if (inboundException instanceof LdapException) {
        ex = (LdapException) inboundException;
      } else {
        ex = new LdapException(ResultCode.SERVER_DOWN, inboundException);
      }
      if (messageWorkerGroup != null) {
        messageWorkerGroup.execute(() -> pendingResponses.notifyOperationHandles(ex));
      } else {
        pendingResponses.notifyOperationHandles(ex);
      }
    }
  }


  /**
   * Attempts to reestablish the channel for this connection.
   *
   * @throws  IllegalStateException  if the connection is open
   */
  private void reconnect()
  {
    if (isOpen()) {
      throw new IllegalStateException("Reconnect cannot be invoked when the connection is open");
    }
    if (isOpening()) {
      LOGGER.debug("Open in progress, ignoring reconnect for connection {}", this);
      notifyOperationHandlesOfClose();
      return;
    }
    if (isClosing()) {
      LOGGER.debug("Close in progress, ignoring reconnect for connection {}", this);
      notifyOperationHandlesOfClose();
      return;
    }
    LOGGER.trace("reconnecting connection {}", this);
    if (!reconnectLock.isWriteLocked()) {
      boolean gotReconnectLock;
      try {
        if (Duration.ZERO.equals(connectionConfig.getReconnectTimeout())) {
          reconnectLock.writeLock().lock();
          gotReconnectLock = true;
        } else {
          gotReconnectLock = reconnectLock.writeLock().tryLock(
            connectionConfig.getReconnectTimeout().toMillis(), TimeUnit.MILLISECONDS);
        }
      } catch (InterruptedException e) {
        LOGGER.warn("Interrupted waiting on reconnect lock", e);
        gotReconnectLock = false;
      }
      if (gotReconnectLock) {
        List<DefaultOperationHandle> replayOperations = null;
        try {
          try {
            reopen(new ClosedRetryMetadata(lastSuccessfulOpen, inboundException));
            LOGGER.info("Auto reconnect finished for connection {}", this);
          } catch (Exception e) {
            LOGGER.debug("Auto reconnect failed for connection {}", this, e);
          }
          // replay operations that have been sent, but have not received a response
          // notify all other operations
          if (isOpen() && connectionConfig.getAutoReplay()) {
            replayOperations = pendingResponses.handles().stream()
              .filter(h -> h.getSentTime() != null && !h.hasConsumedMessage())
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
          replayOperations.forEach(this::write);
        }
        LOGGER.debug("Reconnect for connection {} finished", this);
      } else {
        LOGGER.warn("Reconnect failed, could not acquire reconnect lock");
      }
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
      throw new LdapException(ResultCode.SERVER_DOWN, "Connection is closed");
    }
  }


  @Override
  public String toString()
  {
    return new StringBuilder(getClass().getName()).append("@").append(hashCode()).append("::")
      .append("ldapUrl=").append(ldapURL).append(", ")
      .append("isOpen=").append(isOpen()).append(", ")
      .append("connectTime=").append(connectTime).append(", ")
      .append("connectionConfig=").append(connectionConfig).append(", ")
      .append("channel=").append(channel).toString();
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
        throw new IllegalStateException("Operation in progress, cannot send bind request");
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
        LOGGER.trace("operation channel success for {}", NettyConnection.this);
      } else {
        LOGGER.warn("operation channel error for {}", NettyConnection.this, future.cause());
      }
    }
  }


  /**
   * Listener for channel close events. If {@link ConnectionConfig#getAutoReconnect()} is true, a connection reconnect
   * is attempted on a separate thread.
   */
  private class CloseFutureListener implements ChannelFutureListener
  {

    /** Whether this listener is in the process of reconnecting. */
    private final AtomicBoolean reconnecting = new AtomicBoolean();


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
      if (connectionConfig.getAutoReconnect() && !isOpening() && !isClosing()) {
        LOGGER.trace("scheduling reconnect thread for {}", NettyConnection.this);
        if (connectionExecutor != null && !connectionExecutor.isShutdown()) {
          connectionExecutor.execute(
            () -> {
              if (reconnecting.compareAndSet(false, true)) {
                try {
                  reconnect();
                } catch (Exception e) {
                  LOGGER.warn("Reconnect attempt failed for {}", NettyConnection.this, e);
                } finally {
                  reconnecting.set(false);
                }
              } else {
                LOGGER.debug("Ignoring reconnect attempt, reconnect already in progress for {}", NettyConnection.this);
              }
            });
        } else {
          LOGGER.warn(
            "Reconnect could not be scheduled on executor {} for {}",
            connectionExecutor,
            NettyConnection.this);
        }
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
      } else if (LOGGER.isTraceEnabled()) {
        ch.pipeline().addLast("logger", new LoggingHandler(LogLevel.TRACE));
      }
      // inbound handlers are processed top to bottom
      // outbound handlers are processed bottom to top
      ch.pipeline().addLast("frame_decoder", new MessageFrameDecoder());
      ch.pipeline().addLast("response_decoder", new MessageDecoder());
      if (!ch.config().isAutoRead()) {
        ch.pipeline().addLast("flow_control_handler", new AutoReadFlowControlHandler());
      }
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
    COMPLETE,
  }


  /**
   * Encodes an LDAP request into its DER bytes. See {@link EncodedRequest#getEncoded()}. This class prefers direct
   * byte buffers.
   */
  @ChannelHandler.Sharable
  protected static class RequestEncoder extends MessageToByteEncoder<EncodedRequest>
  {

    /** Logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    protected void encode(final ChannelHandlerContext ctx, final EncodedRequest msg, final ByteBuf out)
    {
      logger.trace("encoding message {} on {}", msg, ctx);
      out.writeBytes(msg.getEncoded());
    }


    @Override
    protected ByteBuf allocateBuffer(
      final ChannelHandlerContext ctx,
      final EncodedRequest msg,
      final boolean preferDirect)
    {
      final int msgSize = msg.getEncoded().length;
      if (preferDirect) {
        return ctx.alloc().ioBuffer(msgSize);
      } else {
        return ctx.alloc().heapBuffer(msgSize);
      }
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
      throws LdapException
    {
      LOGGER.trace("received {} bytes on {}", in.readableBytes(), ctx);
      final ResponseParser parser = new ResponseParser();
      final Message message =  parser.parse(new NettyDERBuffer(in))
        .orElseThrow(() -> new LdapException(ResultCode.DECODING_ERROR, "No response found"));
      out.add(message);
      LOGGER.trace("decoded response message {} on {}", message, ctx);
      if (ctx != null) {
        ctx.fireUserEventTriggered(MessageStatus.DECODED);
      }
    }
  }


  /**
   * Matches an inbound LDAP response message to its operation handle and removes that handle from the response queue.
   * Notifies all operation handles when an unsolicited notification arrives.
   */
  private class InboundMessageHandler extends SimpleChannelInboundHandler<Message>
  {


    @Override
    @SuppressWarnings("unchecked")
    protected void channelRead0(final ChannelHandlerContext ctx, final Message msg)
    {
      LOGGER.trace("channel read message {} on {}", msg, ctx);
      try {
        final DefaultOperationHandle handle = pendingResponses.get(msg.getMessageID());
        LOGGER.debug("Received response message {} for handle {}", msg, handle);
        if (handle != null) {
          if (msg instanceof LdapEntry) {
            ((SearchRequest) handle.getRequest()).configureBinaryAttributes((LdapEntry) msg);
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
          ctx.fireUserEventTriggered(MessageStatus.COMPLETE);
        }
      }
    }
  }


  /**
   * Initiates a channel read when an LDAP message has been processed and auto read is false. This handler also
   * initiates a channel read when it becomes active to bootstrap the initial read.
   */
  @ChannelHandler.Sharable
  protected static class AutoReadEventHandler extends SimpleUserEventChannelHandler<MessageStatus>
  {

    /** Logger for this class. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public void channelActive(final ChannelHandlerContext ctx)
      throws Exception
    {
      logger.trace("channel active on {}", ctx);
      // invoking ctx.channel().read() starts at the tail of the pipeline
      ctx.channel().read();
      ctx.fireChannelActive();
    }


    @Override
    protected void eventReceived(final ChannelHandlerContext ctx, final MessageStatus evt)
    {
      logger.trace("received event {} on {}", evt, ctx);
      if (MessageStatus.COMPLETE == evt) {
        logger.trace("invoking read on {}", ctx);
        // invoking ctx.read() starts at this handler
        ctx.read();
      }
    }
  }


  /**
   * Schedules a connection validator to run based on its strategy. If the validator fails an exception caught is fired
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
      // this implementation could also be done with user events
      // while that may be cleaner it does introduce a dependency on the message thread pool which should be avoided
      LOGGER.trace("channel active on {}", ctx);
      sf = ctx.executor().scheduleAtFixedRate(
        () -> {
          if (ctx != null && !ctx.executor().isShuttingDown()) {
            final AtomicReference<Boolean> result = new AtomicReference<>();
            ctx.executor().submit(() -> connectionValidator.applyAsync(NettyConnection.this, result::set));
            ctx.executor().schedule(
              () -> {
                LOGGER.trace("connection validation returned {} for {}", result.get(), NettyConnection.this);
                final boolean success = result.updateAndGet(b -> b != null && b);
                if (!success) {
                  ctx.fireExceptionCaught(
                    new LdapException(
                      ResultCode.SERVER_DOWN,
                      "Connection validation failed for " + NettyConnection.this));
                }
              },
              Duration.ZERO.equals(connectionValidator.getValidateTimeout()) ?
                connectionConfig.getResponseTimeout().toMillis() : connectionValidator.getValidateTimeout().toMillis(),
              TimeUnit.MILLISECONDS);
          }
        },
        connectionValidator.getValidatePeriod().toMillis(),
        connectionValidator.getValidatePeriod().toMillis(),
        TimeUnit.MILLISECONDS);
      ctx.fireChannelActive();
    }


    @Override
    public void channelInactive(final ChannelHandlerContext ctx)
    {
      LOGGER.trace("channel inactive on {}", ctx);
      if (sf != null) {
        sf.cancel(true);
      }
      ctx.fireChannelInactive();
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
