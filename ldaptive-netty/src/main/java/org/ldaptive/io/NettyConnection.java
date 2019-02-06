/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ThreadPerTaskExecutor;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.LdapURL;
import org.ldaptive.ResultCode;
import org.ldaptive.control.RequestControl;
import org.ldaptive.protocol.AbandonRequest;
import org.ldaptive.protocol.AddRequest;
import org.ldaptive.protocol.BindRequest;
import org.ldaptive.protocol.BindResponse;
import org.ldaptive.protocol.CompareRequest;
import org.ldaptive.protocol.CompareResponse;
import org.ldaptive.protocol.DeleteRequest;
import org.ldaptive.protocol.ExtendedRequest;
import org.ldaptive.protocol.ExtendedResponse;
import org.ldaptive.protocol.GssApiBindRequest;
import org.ldaptive.protocol.IntermediateResponse;
import org.ldaptive.protocol.Message;
import org.ldaptive.protocol.ModifyDnRequest;
import org.ldaptive.protocol.ModifyRequest;
import org.ldaptive.protocol.Request;
import org.ldaptive.protocol.ResponseParser;
import org.ldaptive.protocol.Result;
import org.ldaptive.protocol.SASLClientRequest;
import org.ldaptive.protocol.SearchRequest;
import org.ldaptive.protocol.SearchResultEntry;
import org.ldaptive.protocol.SearchResultReference;
import org.ldaptive.protocol.StartTLSRequest;
import org.ldaptive.protocol.UnbindRequest;
import org.ldaptive.protocol.UnsolicitedNotification;
import org.ldaptive.sasl.QualityOfProtection;
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
public final class NettyConnection extends Connection
{

  /**
   * Worker group with the default number of threads. Creates a daemon threads with a normal priority. See {@link
   * DefaultThreadFactory}.
   *
   * TODO something should invoke {@link EventLoopGroup#shutdownGracefully()}
   * TODO since this is a shared object I'm not sure what
   * TODO note that I am using daemon threads so the executor won't hold the jvm hostage
   */
  private static final EventLoopGroup WORKER_GROUP = new NioEventLoopGroup(
    0,
    new ThreadPerTaskExecutor(new DefaultThreadFactory(NettyConnection.class, true, Thread.NORM_PRIORITY)));


  /** TODO remove this when ConnectionConfig guarantees a response timeout value. */
  private static final Duration DEFAULT_RESPONSE_TIMEOUT = Duration.ofMinutes(1);

  /** TODO move this into ConnectionConfig. */
  private static final Duration DEFAULT_REQUEST_TTL = Duration.ofMinutes(5);

  /** Default Netty channel configuration options. */
  private static final Map<ChannelOption, Object> DEFAULT_CHANNEL_OPTIONS = new HashMap<>();

  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(NettyConnection.class);

  /** Listener notified when the connection is closed. */
  private CloseFutureListener closeListener = new CloseFutureListener();

  /** Queue holding requests that haven't received a response. */
  private ResponseQueue pendingResponses = new ResponseQueue(DEFAULT_REQUEST_TTL);

  /** Message ID counter, incremented as requests are sent. */
  private final AtomicInteger messageID = new AtomicInteger(1);

  /** Operation lock when a bind occurs. */
  private final ReadWriteLock bindLock = new ReentrantReadWriteLock();

  /** Netty channel configuration options. */
  private final Map<ChannelOption, Object> channelOptions = new HashMap<>();

  /** Client connection configuration. */
  private final ConnectionConfig connectionConfig;

  /** Whether connections should automatically reconnect. */
  private final boolean autoReconnect;

  /** Connection to the LDAP server. */
  private Channel channel;

  /** Last exception received on the inbound pipeline. */
  private Throwable inboundException;


  /** Initialize the default channel options. */
  static {
    DEFAULT_CHANNEL_OPTIONS.put(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) Duration.ofMinutes(1).toMillis());
    DEFAULT_CHANNEL_OPTIONS.put(ChannelOption.SO_KEEPALIVE, true);
  }


  /**
   * Creates a new connection.
   *
   * @param  cc  connection config
   */
  public NettyConnection(final ConnectionConfig cc)
  {
    connectionConfig = cc;
    autoReconnect = false;
  }


  /**
   * Creates a Netty {@link Bootstrap} with the supplied client initializer.
   *
   * TODO map ConnectionConfig properties to Netty channel options
   *
   * @param  initializer  to provide to the bootstrap
   *
   * @return  Netty bootstrap
   */
  @SuppressWarnings("unchecked")
  private Bootstrap createBootstrap(final ClientInitializer initializer)
  {
    final Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(WORKER_GROUP);
    bootstrap.channel(NioSocketChannel.class);

    if (channelOptions == null || channelOptions.isEmpty()) {
      DEFAULT_CHANNEL_OPTIONS.entrySet().stream().forEach(e -> bootstrap.option(e.getKey(), e.getValue()));
    } else {
      channelOptions.entrySet().stream().forEach(e -> bootstrap.option(e.getKey(), e.getValue()));
    }
    bootstrap.handler(initializer);
    return bootstrap;
  }


  /**
   * Connect to the LDAP server.
   *
   * @throws  LdapException  if startTLS is requested and the operation fails
   */
  public synchronized void open()
    throws LdapException
  {
    logger.debug("Opening connection {}", this);
    if (isOpen()) {
      throw new ConnectException("Connection is already open");
    }
    inboundException = null;
    final ChannelFuture future = connectInternal();
    channel = future.channel();
    channel.closeFuture().addListener(closeListener);
    pendingResponses.open();
    // startTLS request must occur after the connection is ready
    if (connectionConfig.getUseStartTLS()) {
      operation(new StartTLSRequest());
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
    if (connectionConfig.getUseSSL() || connectionConfig.getLdapUrl().toLowerCase().contains("ldaps://")) {
      try {
        handler = createSslHandler(connectionConfig);
      } catch (SSLException e) {
        throw new ConnectException(e);
      }
    }
    final ClientInitializer initializer = new ClientInitializer(handler);
    final Bootstrap bootstrap = createBootstrap(initializer);

    final LdapURL ldapUrl = new LdapURL(connectionConfig.getLdapUrl());
    final ChannelFuture future = bootstrap.connect(
      new InetSocketAddress(ldapUrl.getLastEntry().getHostname(), ldapUrl.getLastEntry().getPort()));
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
      SslConfig.newSslConfig(config.getSslConfig()) : new SslConfig();
    final SSLContext ctx;
    try {
      final SSLContextInitializer initializer = sc.createSSLContextInitializer();
      ctx = initializer.initSSLContext("TLS");
    } catch (GeneralSecurityException e) {
      throw new SSLException("Could not initialize SSL context", e);
    }
    final LdapURL ldapUrl = new LdapURL(connectionConfig.getLdapUrl());
    final SSLEngine engine = ctx.createSSLEngine(
      ldapUrl.getLastEntry().getHostname(),
      ldapUrl.getLastEntry().getPort());
    engine.setUseClientMode(true);
    if (sc.getEnabledProtocols() != null) {
      engine.setEnabledProtocols(sc.getEnabledProtocols());
    }
    if (sc.getEnabledCipherSuites() != null) {
      engine.setEnabledCipherSuites(sc.getEnabledCipherSuites());
    }
    // TODO review use of Hostname verifiers
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
    if (connectionConfig.getSslConfig().getHostnameVerifier() != null) {
      final HostnameVerifier verifier = new HostnameVerifierAdapter(
        connectionConfig.getSslConfig().getHostnameVerifier());
      final SSLSession session = handler.engine().getSession();
      final String hostname = session.getPeerHost();
      // TODO review use of Hostname verifiers
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
    final ExtendedOperationHandle handle = new ExtendedOperationHandle(
      request,
      this,
      connectionConfig.getResponseTimeout() != null ? connectionConfig.getResponseTimeout() : DEFAULT_RESPONSE_TIMEOUT);
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
  void operation(final UnbindRequest request)
  {
    if (!isOpen()) {
      logger.warn("Attempt to unbind ignored, connection is not open");
    } else {
      // TODO should this wait?
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
            return operation((SASLClientRequest) request);
          } catch (LdapException e) {
            logger.warn("SASL GSSAPI operation failed", e);
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
  public Result operation(final SASLClientRequest request)
    throws LdapException
  {
    throwIfClosed();
    if (!bindLock.writeLock().tryLock()) {
      throw new LdapException("Bind in progress");
    }
    try {
      final LdapURL ldapUrl = new LdapURL(connectionConfig.getLdapUrl());
      final SASLClient client = new SASLClient(ldapUrl.getLastEntry().getHostname());
      final BindResponse response;
      boolean saslSecurity = false;
      try {
        response = client.bind(this, request);
        if (response.getResultCode() == ResultCode.SUCCESS) {
          final QualityOfProtection qop = client.getQualityOfProtection();
          if (qop != null && (QualityOfProtection.AUTH_INT == qop || QualityOfProtection.AUTH_CONF == qop)) {
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
    final OperationHandle handle = pendingResponses.remove(request.getMessageID());
    if (!isOpen()) {
      logger.warn("Attempt to abandon request {} ignored, connection is not open", request.getMessageID());
    } else {
      // TODO should this wait?
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
  public OperationHandle operation(final AddRequest request)
  {
    return operationInternal(request);
  }


  @Override
  public BindOperationHandle operation(final BindRequest request)
  {
    return new BindOperationHandle(
      request,
      this,
      connectionConfig.getResponseTimeout() != null ? connectionConfig.getResponseTimeout() : DEFAULT_RESPONSE_TIMEOUT);
  }


  @Override
  public CompareOperationHandle operation(final CompareRequest request)
  {
    return new CompareOperationHandle(
      request,
      this,
      connectionConfig.getResponseTimeout() != null ? connectionConfig.getResponseTimeout() : DEFAULT_RESPONSE_TIMEOUT);
  }


  @Override
  public OperationHandle operation(final DeleteRequest request)
  {
    return operationInternal(request);
  }


  @Override
  public ExtendedOperationHandle operation(final ExtendedRequest request)
  {
    if (request instanceof StartTLSRequest) {
      throw new IllegalArgumentException("StartTLS can only be invoked when the connection is opened");
    }
    return new ExtendedOperationHandle(
      request,
      this,
      connectionConfig.getResponseTimeout() != null ? connectionConfig.getResponseTimeout() : DEFAULT_RESPONSE_TIMEOUT);
  }


  @Override
  public OperationHandle operation(final ModifyRequest request)
  {
    return operationInternal(request);
  }


  @Override
  public OperationHandle operation(final ModifyDnRequest request)
  {
    return operationInternal(request);
  }


  @Override
  public SearchOperationHandle operation(final SearchRequest request)
  {
    return new SearchOperationHandle(
      request,
      this,
      connectionConfig.getResponseTimeout() != null ? connectionConfig.getResponseTimeout() : DEFAULT_RESPONSE_TIMEOUT);
  }


  /**
   * Creates an operation handle for the supplied request.
   *
   * @param  request  to create a handle for
   *
   * @return  operation handle
   */
  private OperationHandle operationInternal(final Request request)
  {
    return new OperationHandle(
      request,
      this,
      connectionConfig.getResponseTimeout() != null ? connectionConfig.getResponseTimeout() : DEFAULT_RESPONSE_TIMEOUT);
  }


  @Override
  void write(final OperationHandle handle)
  {
    logger.debug("Write handle {} {}", handle, pendingResponses);
    if (!isOpen()) {
      handle.exception(new LdapException("Connection is closed"));
    } else {
      try {
        // TODO should this wait?
        if (bindLock.readLock().tryLock()) {
          try {
            final EncodedRequest encodedRequest = new EncodedRequest(messageID.getAndIncrement(), handle.getRequest());
            handle.messageID(encodedRequest.getMessageID());
            if (pendingResponses.put(encodedRequest.getMessageID(), handle) != null) {
              throw new IllegalStateException("Request already exists for ID " + encodedRequest.getMessageID());
            }
            channel.writeAndFlush(encodedRequest);
            handle.sent();
          } finally {
            bindLock.readLock().unlock();
          }
        } else {
          handle.exception(new LdapException("Bind in progress"));
        }
      } catch (Exception e) {
        handle.exception(e);
      }
    }
  }


  /**
   * Closes this connection. Abandons all pending responses and sends an unbind to the LDAP server.
   *
   * @param  controls  to send with the unbind request when closing the connection
   */
  @Override
  public synchronized void close(final RequestControl... controls)
  {
    logger.debug("Closing connection {}", this);
    try {
      pendingResponses.close();
      if (isOpen()) {
        channel.closeFuture().removeListener(closeListener);
        // abandon outstanding requests
        if (pendingResponses.size() > 0) {
          logger.info("Abandoning requests {} to close connection", pendingResponses);
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
      channel = null;
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
      throw new LdapException("Connection is not open");
    }
  }


  /** Bind specific operation handle that locks other operations until the bind completes. */
  public class BindOperationHandle extends OperationHandle<BindRequest>
  {


    /**
     * Creates a new bind operation handle.
     *
     * @param  req  bind request to expect a response for
     * @param  conn  the request will be executed on
     * @param  timeout  duration to wait for a response
     */
    BindOperationHandle(final BindRequest req, final Connection conn, final Duration timeout)
    {
      super(req, conn, timeout);
    }


    @Override
    public OperationHandle send()
    {
      throw new UnsupportedOperationException("Bind requests are synchronous, invoke execute");
    }


    @Override
    public Result await()
    {
      throw new UnsupportedOperationException("Bind requests are synchronous, invoke execute");
    }


    @Override
    public Result execute()
      throws LdapException
    {
      // TODO add a timeout property or simply throw if a another bind is in progress
      bindLock.writeLock().lock();
      try {
        super.send();
        return super.await();
      } finally {
        bindLock.writeLock().unlock();
      }
    }
  }


  /**
   * Listener for channel close events. Invokes {@link #close()} to cleanup resources from a non-requested close event.
   * If {@link #autoReconnect} is set, the connection is opened and any outstanding requests are replayed.
   */
  private class CloseFutureListener implements GenericFutureListener<ChannelFuture>
  {


    @Override
    public void operationComplete(final ChannelFuture future)
    {
      logger.debug("Close listener complete operation future={}, inboundException={}", future, inboundException);
      if (autoReconnect) {
        try {
          final Collection<OperationHandle> handles = new HashSet<>(pendingResponses.handles());
          pendingResponses.clear();
          open();
          logger.info("Replaying {} requests", handles.size());
          handles.stream().forEach(h -> {
            if (!h.hasConsumedMessage() && h.getReceivedTime() == null) {
              write(h);
            } else {
              h.exception(
                inboundException != null ? inboundException :
                  future.cause() != null ? future.cause() :
                    new LdapException("Cannot replay request with partial response"));
            }
          });
        } catch (Exception e) {
          inboundException = e;
          close();
        }
      } else {
        inboundException = future.cause();
        close();
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
      // TODO provide property to control whether the logging handler is added to the pipeline
      ch.pipeline().addLast("logger", new LoggingHandler(LogLevel.DEBUG));
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
      final OperationHandle handle = pendingResponses.get(msg.getMessageID());
      logger.debug("Received response message {} for handle {}", msg, handle);
      if (handle != null) {
        if (msg instanceof SearchResultEntry) {
          ((SearchOperationHandle) handle).entry((SearchResultEntry) msg);
        } else if (msg instanceof SearchResultReference) {
          ((SearchOperationHandle) handle).reference((SearchResultReference) msg);
        } else if (msg instanceof Result) {
          if (pendingResponses.remove(msg.getMessageID()) == null) {
            logger.warn("Processed message {} that no longer exists", msg.getMessageID());
          }
          if (msg instanceof ExtendedResponse) {
            ((ExtendedOperationHandle) handle).extended((ExtendedResponse) msg);
          } else if (msg instanceof CompareResponse) {
            ((CompareOperationHandle) handle).compare((CompareResponse) msg);
          }
          if (msg.getControls() != null && msg.getControls().length > 0) {
            Stream.of(msg.getControls()).forEach(c -> handle.control(c));
          }
          handle.result((Result) msg);
        } else if (msg instanceof IntermediateResponse) {
          handle.intermediate((IntermediateResponse) msg);
        } else {
          throw new IllegalStateException("Unknown message type: " + msg);
        }
      } else if (msg instanceof UnsolicitedNotification) {
        logger.info("Received UnsolicitedNotification: {}", msg);
        pendingResponses.notifyOperationHandles((UnsolicitedNotification) msg);
      } else {
        logger.warn("Received response message {} without matching request in {}", msg, pendingResponses);
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
      logger.debug("Inbound handler caught exception", cause);
      inboundException = cause;
      channel.close();
    }
  }
}
