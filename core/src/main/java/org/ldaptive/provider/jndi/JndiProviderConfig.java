/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.jndi;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import javax.naming.ldap.Control;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import org.ldaptive.ResultCode;
import org.ldaptive.provider.ControlProcessor;
import org.ldaptive.provider.ProviderConfig;
import org.ldaptive.ssl.AllowAnyHostnameVerifier;

/**
 * Contains configuration data for the JNDI provider.
 *
 * @author  Middleware Services
 */
public class JndiProviderConfig extends ProviderConfig<Control>
{

  /**
   * Boolean property that indicates whether hostname verification should be
   * disabled for StartTLS connections. The value of this constant is {@value}.
   */
  protected static final String ALLOW_ANY_HOSTNAME =
    "jndi.starttls.allowAnyHostname";

  /** Context environment. */
  private Map<String, Object> environment;

  /** Stream to print LDAP ASN.1 BER packets. */
  private OutputStream tracePackets;

  /** Whether to remove the URL from any DNs which are not relative. */
  private boolean removeDnUrls = true;

  /** Search result codes to ignore. */
  private ResultCode[] searchIgnoreResultCodes;

  /** ldap socket factory used for SSL and startTLS. */
  private SSLSocketFactory sslSocketFactory;

  /** hostname verifier for startTLS connections. */
  private HostnameVerifier hostnameVerifier;


  /** Default constructor. */
  public JndiProviderConfig()
  {
    setOperationExceptionResultCodes(
      ResultCode.PROTOCOL_ERROR,
      ResultCode.SERVER_DOWN);
    searchIgnoreResultCodes = new ResultCode[] {
      ResultCode.TIME_LIMIT_EXCEEDED,
      ResultCode.SIZE_LIMIT_EXCEEDED,
      ResultCode.PARTIAL_RESULTS,
    };
    setControlProcessor(
      new ControlProcessor<>(new JndiControlHandler()));
  }


  /**
   * Returns the context environment.
   *
   * @return  ldap context environment
   */
  public Map<String, Object> getEnvironment()
  {
    return environment;
  }


  /**
   * Sets the context environment.
   *
   * @param  env  ldap context environment
   */
  public void setEnvironment(final Map<String, Object> env)
  {
    checkImmutable();
    logger.trace("setting environment: {}", env);
    environment = env;
  }


  /**
   * Returns the output stream used to print ASN.1 BER packets.
   *
   * @return  output stream
   */
  public OutputStream getTracePackets()
  {
    return tracePackets;
  }


  /**
   * Sets the output stream to print ASN.1 BER packets to.
   *
   * @param  stream  to output to
   */
  public void setTracePackets(final OutputStream stream)
  {
    checkImmutable();
    logger.trace("setting tracePackets: {}", stream);
    tracePackets = stream;
  }


  /**
   * Returns whether the URL will be removed from any DNs which are not
   * relative. The default value is true.
   *
   * @return  whether the URL will be removed from DNs
   */
  public boolean getRemoveDnUrls()
  {
    return removeDnUrls;
  }


  /**
   * Sets whether the URL will be removed from any DNs which are not relative
   * The default value is true.
   *
   * @param  b  whether the URL will be removed from DNs
   */
  public void setRemoveDnUrls(final boolean b)
  {
    checkImmutable();
    logger.trace("setting removeDnUrls: {}", b);
    removeDnUrls = b;
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
  public void setSearchIgnoreResultCodes(final ResultCode... codes)
  {
    checkImmutable();
    logger.trace("setting searchIgnoreResultCodes: {}", Arrays.toString(codes));
    searchIgnoreResultCodes = codes;
  }


  /**
   * Returns the SSL socket factory to use for SSL and startTLS connections.
   *
   * @return  SSL socket factory
   */
  public SSLSocketFactory getSslSocketFactory()
  {
    return sslSocketFactory;
  }


  /**
   * Sets the SSL socket factory to use for SSL and startTLS connections.
   *
   * @param  sf  SSL socket factory
   */
  public void setSslSocketFactory(final SSLSocketFactory sf)
  {
    checkImmutable();
    logger.trace("setting sslSocketFactory: {}", sf);
    sslSocketFactory = sf;
  }


  /**
   * Returns the hostname verifier to use for startTLS connections.
   *
   * @return  hostname verifier
   */
  public HostnameVerifier getHostnameVerifier()
  {
    return hostnameVerifier;
  }


  /**
   * Sets the hostname verifier to use for startTLS connections.
   *
   * @param  verifier  for hostnames
   */
  public void setHostnameVerifier(final HostnameVerifier verifier)
  {
    checkImmutable();
    logger.trace("setting hostnameVerifier: {}", verifier);
    hostnameVerifier = verifier;
  }


  /** {@inheritDoc} */
  @Override
  public void setProperties(final Map<String, Object> props)
  {
    checkImmutable();

    final boolean allowAnyHostname = Boolean.valueOf(
      (String) props.get(ALLOW_ANY_HOSTNAME));
    if (allowAnyHostname) {
      setHostnameVerifier(new AllowAnyHostnameVerifier());
    }
    super.setProperties(props);
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::operationExceptionResultCodes=%s, properties=%s, " +
        "connectionStrategy=%s, controlProcessor=%s, environment=%s, " +
        "tracePackets=%s, removeDnUrls=%s, searchIgnoreResultCodes=%s, " +
        "sslSocketFactory=%s, hostnameVerifier=%s]",
        getClass().getName(),
        hashCode(),
        Arrays.toString(getOperationExceptionResultCodes()),
        getProperties(),
        getConnectionStrategy(),
        getControlProcessor(),
        environment,
        tracePackets,
        removeDnUrls,
        Arrays.toString(searchIgnoreResultCodes),
        sslSocketFactory,
        hostnameVerifier);
  }
}
