/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.unboundid;

import java.util.Arrays;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import org.ldaptive.ResultCode;
import org.ldaptive.provider.ControlProcessor;
import org.ldaptive.provider.ProviderConfig;

/**
 * Contains configuration data for the UnboundID provider.
 *
 * @author  Middleware Services
 */
public class UnboundIDProviderConfig extends ProviderConfig<Control>
{

  /** Connection options. */
  private LDAPConnectionOptions connectionOptions;

  /** socket factory for ldap connections. */
  private SocketFactory socketFactory;

  /** socket factory for ldaps and startTLS connections. */
  private SSLSocketFactory sslSocketFactory;

  /** Search result codes to ignore. */
  private ResultCode[] searchIgnoreResultCodes;


  /** Default constructor. */
  public UnboundIDProviderConfig()
  {
    setOperationExceptionResultCodes(ResultCode.SERVER_DOWN);
    setControlProcessor(new ControlProcessor<>(new UnboundIDControlHandler()));
    searchIgnoreResultCodes = new ResultCode[] {
      ResultCode.TIME_LIMIT_EXCEEDED,
      ResultCode.SIZE_LIMIT_EXCEEDED,
      ResultCode.REFERRAL,
    };
  }


  /**
   * Returns the connection options.
   *
   * @return  ldap connection options
   */
  public LDAPConnectionOptions getConnectionOptions()
  {
    return connectionOptions;
  }


  /**
   * Sets the connection options.
   *
   * @param  options  ldap connection options
   */
  public void setConnectionOptions(final LDAPConnectionOptions options)
  {
    connectionOptions = options;
  }


  /**
   * Returns the socket factory to use for LDAP connections.
   *
   * @return  socket factory
   */
  public SocketFactory getSocketFactory()
  {
    return socketFactory;
  }


  /**
   * Sets the socket factory to use for LDAP connections.
   *
   * @param  sf  socket factory
   */
  public void setSocketFactory(final SocketFactory sf)
  {
    checkImmutable();
    logger.trace("setting socketFactory: {}", sf);
    socketFactory = sf;
  }


  /**
   * Returns the SSL socket factory to use for LDAPS and startTLS connections.
   *
   * @return  SSL socket factory
   */
  public SSLSocketFactory getSSLSocketFactory()
  {
    return sslSocketFactory;
  }


  /**
   * Sets the SSL socket factory to use for LDAPS and startTLS connections.
   *
   * @param  sf  socket factory
   */
  public void setSSLSocketFactory(final SSLSocketFactory sf)
  {
    checkImmutable();
    logger.trace("setting sslSocketFactory: {}", sf);
    sslSocketFactory = sf;
  }


  /**
   * Returns the search ignore result codes.
   *
   * @return  result codes to ignore
   */
  public ResultCode[] getSearchIgnoreResultCodes()
  {
    return searchIgnoreResultCodes;
  }


  /**
   * Sets the search ignore result codes.
   *
   * @param  codes  to ignore
   */
  public void setSearchIgnoreResultCodes(final ResultCode[] codes)
  {
    checkImmutable();
    logger.trace("setting searchIgnoreResultCodes: {}", Arrays.toString(codes));
    searchIgnoreResultCodes = codes;
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::operationExceptionResultCodes=%s, properties=%s, controlProcessor=%s, connectionOptions=%s, " +
        "socketFactory=%s, sslSocketFactory=%s, searchIgnoreResultCodes=%s]",
        getClass().getName(),
        hashCode(),
        Arrays.toString(getOperationExceptionResultCodes()),
        getProperties(),
        getControlProcessor(),
        connectionOptions,
        socketFactory,
        sslSocketFactory,
        Arrays.toString(searchIgnoreResultCodes));
  }
}
