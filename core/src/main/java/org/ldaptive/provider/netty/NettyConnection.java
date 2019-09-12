/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.netty;

import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.ldaptive.AbandonRequest;
import org.ldaptive.AddRequest;
import org.ldaptive.AddResponse;
import org.ldaptive.BindRequest;
import org.ldaptive.BindResponse;
import org.ldaptive.CompareRequest;
import org.ldaptive.CompareResponse;
import org.ldaptive.ConnectException;
import org.ldaptive.ConnectionConfig;
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
import org.ldaptive.RetryMetadata;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResultReference;
import org.ldaptive.UnbindRequest;
import org.ldaptive.control.RequestControl;
import org.ldaptive.extended.ExtendedRequest;
import org.ldaptive.extended.ExtendedResponse;
import org.ldaptive.extended.IntermediateResponse;
import org.ldaptive.extended.StartTLSRequest;
import org.ldaptive.extended.UnsolicitedNotification;
import org.ldaptive.provider.DefaultCompareOperationHandle;
import org.ldaptive.provider.DefaultExtendedOperationHandle;
import org.ldaptive.provider.DefaultOperationHandle;
import org.ldaptive.provider.DefaultSearchOperationHandle;
import org.ldaptive.provider.ProviderConnection;
import org.ldaptive.provider.ResponseParser;
import org.ldaptive.provider.SaslClient;
import org.ldaptive.sasl.GssApiBindRequest;
import org.ldaptive.sasl.QualityOfProtection;
import org.ldaptive.sasl.SaslClientRequest;
import org.ldaptive.ssl.HostnameVerifierAdapter;
import org.ldaptive.ssl.SSLContextInitializer;
import org.ldaptive.ssl.SslConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty based connection implementation.
 *
 * @author  Middleware Services
 */
public final class NettyConnection extends ProviderConnection
{

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(NettyConnection.class);

  /** Maximum number of concurrent operations to allow before blocking. */
  private static final int MAX_CONCURRENT_OPS = Integer.parseInt(
    System.getProperty("org.ldaptive.provider.netty.maxConcurrentOps", "50"));

  /** Event worker group used by the bootstrap. */
  private final EventLoopGroup workerGroup;

  /** Netty channel configuration options. */
  private final Map<ChannelOption, Object> channelOptions;

  /** Queue holding requests that haven't received a response. */
  private final HandleMap pendingResponses;

  /** Listener notified when the connection is closed. */
  private final CloseFutureListener closeListener = new CloseFutureListener();

  /** Message ID counter, incremented as requests are sent. */
  private final AtomicInteger messageID = new AtomicInteger(1);

  /** Maximum number of concurrent operations. */
  private final Semaphore throttle = new Semaphore(MAX_CONCURRENT_OPS);

  /** Operation lock when a bind occurs. */
  private final ReadWriteLock bindLock = new ReentrantReadWriteLock();

  /** Whether connections should automatically reconnect. */
  private final boolean autoReconnect;

  /** URL derived from the connection strategy. */
  private LdapURL ldapURL;

  /** Connection to the LDAP server. */
  private Channel channel;

  /** Last exception received on the inbound pipeline. */
  private Throwable inboundException;


  /**
   * Creates a new connection.
   *
   * @param  group  event loop group
   * @param  config  connection configuration
   */
  public NettyConnection(final EventLoopGroup group, final ConnectionConfig config)
  {
    super(config);
    workerGroup = group;
    autoReconnect = config.getAutoReconnect();
    channelOptions = new HashMap<>();
    channelOptions.put(ChannelOption.SO_KEEPALIVE, true);
    channelOptions.put(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) config.getConnectTimeout().toMillis());
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
    bootstrap.group(workerGroup);
    bootstrap.channel(NioSocketChannel.class);
    channelOptions.forEach(bootstrap::option);
    bootstrap.handler(initializer);
    return bootstrap;
  }


  @Override
  protected boolean test(final LdapURL url)
  {
    final NettyConnection conn = new NettyConnection(workerGroup, connectionConfig);
    try {
      conn.open(url);
      return true;
    } catch (LdapException e) {
      return false;
    } finally {
      conn.close();
    }
  }


  @Override
  protected synchronized void open(final LdapURL url)
    throws LdapException
  {
    inboundException = null;
    ldapURL = url;
    LOGGER.debug("Opening connection {}", this);
    final ChannelFuture future = connectInternal();
    channel = future.channel();
    channel.closeFuture().addListener(closeListener);
    pendingResponses.open();
    // startTLS request must occur after the connection is ready
    if (connectionConfig.getUseStartTLS()) {
      operation(new StartTLSRequest());
    }
    // initialize the connection
    if (connectionConfig.getConnectionInitializer() != null) {
      connectionConfig.getConnectionInitializer().initialize(this);
    }
  }


  /**
   * Creates a Netty bootstrap and connects to the LDAP server. Handles the details of adding an SSL handler to the
   * pipeline. This method waits until the connection is established.
   *
   * @return  channel future for the connection.
   *
   * @throws  ConnectException  if the connection fails
   */
  private ChannelFuture connectInternal()
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

    final ChannelFuture future = bootstrap.connect(new InetSocketAddress(ldapURL.getHostname(), ldapURL.getPort()));
    future.awaitUninterruptibly();
    if (!future.isDone()) {
      throw new ConnectException("Connection could not be completed");
    }
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
    return future;
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
    return new SslHandler(engine);
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
    final SslHandler handler = ch.pipeline().get(SslHandler.class);
    final Future<Channel> sslFuture = handler.handshakeFuture();
    sslFuture.awaitUninterruptibly();
    if (!sslFuture.isDone()) {
      throw new SSLException("SSL handshake could not be completed");
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
    if (!isOpen()) {
      LOGGER.warn("Attempt to unbind ignored, connection {} is not open", this);
    } else {
      if (bindLock.readLock().tryLock()) {
        try {
          final EncodedRequest encodedRequest = new EncodedRequest(messageID.getAndIncrement(), request);
          channel.writeAndFlush(encodedRequest);
        } finally {
          bindLock.readLock().unlock();
        }
      } else {
        throw new IllegalStateException("Bind in progress, cannot send unbind request");
      }
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
            LOGGER.warn("SASL GSSAPI operation failed", e);
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
    if (!isOpen()) {
      LOGGER.warn("Attempt to abandon request {} ignored, connection {} is not open", request.getMessageID(), this);
    } else {
      if (bindLock.readLock().tryLock()) {
        try {
          final EncodedRequest encodedRequest = new EncodedRequest(messageID.getAndIncrement(), request);
          channel.writeAndFlush(encodedRequest);
        } finally {
          bindLock.readLock().unlock();
        }
      } else {
        handle.exception(new LdapException("Bind in progress"));
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
  protected void write(final DefaultOperationHandle handle)
  {
    LOGGER.debug("Write handle {} {}", handle, pendingResponses);
    if (!isOpen()) {
      handle.exception(new LdapException("Connection is closed, write aborted"));
    } else {
      try {
        if (throttle.tryAcquire(1, TimeUnit.MINUTES)) {
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
              channel.writeAndFlush(encodedRequest);
              handle.sent();
            } finally {
              bindLock.readLock().unlock();
            }
          } else {
            handle.exception(new LdapException("Bind in progress"));
          }
        } else {
          handle.exception(new LdapException("Too many operations in progress"));
        }
      } catch (Exception e) {
        handle.exception(e);
      }
    }
  }


  @Override
  protected void done(final DefaultOperationHandle handle)
  {
    throttle.release();
  }


  /**
   * Closes this connection. Abandons all pending responses and sends an unbind to the LDAP server.
   *
   * @param  controls  to send with the unbind request when closing the connection
   */
  @Override
  public synchronized void close(final RequestControl... controls)
  {
    LOGGER.debug("Closing connection {}", this);
    try {
      pendingResponses.close();
      if (isOpen()) {
        channel.closeFuture().removeListener(closeListener);
        // abandon outstanding requests
        if (pendingResponses.size() > 0) {
          LOGGER.info("Abandoning requests {} to close connection", pendingResponses);
          pendingResponses.abandonRequests();
        }
        // unbind
        final UnbindRequest req = new UnbindRequest();
        req.setControls(controls);
        operation(req);
        channel.close();
      } else {
        // notify outstanding requests
        if (inboundException != null) {
          pendingResponses.notifyOperationHandles(inboundException);
        } else {
          pendingResponses.notifyOperationHandles(new LdapException("Connection closed"));
        }
      }
    } finally {
      pendingResponses.clear();
      channel = null;
    }
  }


  /**
   * Attempts to reestablish the channel for this connection.
   *
   * @throws  IllegalStateException  if the connection is open
   */
  protected synchronized void reconnect()
  {
    LOGGER.debug("Reconnecting connection {}", this);
    if (isOpen()) {
      throw new IllegalStateException("Reconnect cannot be invoked when the connection is open");
    }
    final RetryMetadata metadata = new RetryMetadata();
    while (connectionConfig.getAutoReconnectCondition().test(metadata)) {
      try {
        open();
        LOGGER.info("auto reconnect succeeded");
        break;
      } catch (LdapException e) {
        LOGGER.debug("auto reconnect failed", e);
        metadata.recordFailure(Instant.now());
      }
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
    BindOperationHandle(final BindRequest req, final ProviderConnection conn, final Duration timeout)
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
   * Listener for channel close events. Invokes {@link #close()} to cleanup resources from a non-requested close event.
   * If {@link #autoReconnect} is set, a connection reconnect is attempted.
   */
  private class CloseFutureListener implements GenericFutureListener<ChannelFuture>
  {

    /** Whether this listener is in the process of reconnecting. */
    private boolean reconnecting;


    @Override
    public void operationComplete(final ChannelFuture future)
    {
      LOGGER.debug("Close listener complete operation future={}, inboundException={}", future, inboundException);
      inboundException = future.cause();
      close();
      if (autoReconnect && !reconnecting) {
        NettyConnection.this.workerGroup.execute(
          () -> {
            reconnecting = true;
            try {
              reconnect();
            } finally {
              reconnecting = false;
            }
          });
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
      ch.pipeline().addLast("request_encoder", new RequestEncoder());
      ch.pipeline().addLast("frame_decoder", new MessageFrameDecoder());
      ch.pipeline().addLast("response_decoder", new MessageDecoder());
      ch.pipeline().addLast("message_handler", new InboundMessageHandler());
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


  /**
   * Encodes an LDAP request into it's DER bytes. See {@link EncodedRequest#getEncoded()}.
   */
  private static class RequestEncoder extends MessageToByteEncoder<EncodedRequest>
  {


    @Override
    protected void encode(final ChannelHandlerContext ctx, final EncodedRequest msg, final ByteBuf out)
    {
      out.writeBytes(msg.getEncoded());
    }
  }


  /**
   * Decodes byte buffer into a concrete LDAP response message. See {@link ResponseParser}.
   */
  private static class MessageDecoder extends ByteToMessageDecoder
  {


    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out)
    {
      final ResponseParser parser = new ResponseParser();
      final Message message =  parser.parse(new NettyDERBuffer(in))
        .orElseThrow(() -> new IllegalArgumentException("No response found"));
      out.add(message);
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
            LOGGER.warn("Processed message {} that no longer exists", msg.getMessageID());
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
        LOGGER.info("Received UnsolicitedNotification: {}", msg);
        pendingResponses.notifyOperationHandles((UnsolicitedNotification) msg);
      } else {
        LOGGER.warn("Received response message {} without matching request in {}", msg, pendingResponses);
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
      LOGGER.warn("Inbound handler caught exception", cause);
      inboundException = cause;
      channel.close();
    }
  }
}
