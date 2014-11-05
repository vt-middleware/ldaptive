/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.jldap;

import java.util.Arrays;
import javax.net.ssl.SSLSocketFactory;
import com.novell.ldap.LDAPConstraints;
import com.novell.ldap.LDAPControl;
import org.ldaptive.ResultCode;
import org.ldaptive.provider.ControlProcessor;
import org.ldaptive.provider.ProviderConfig;

/**
 * Contains configuration data for the JLdap provider.
 *
 * @author  Middleware Services
 */
public class JLdapProviderConfig extends ProviderConfig<LDAPControl>
{

  /** Connection constraints. */
  private LDAPConstraints ldapConstraints;

  /** Search result codes to ignore. */
  private ResultCode[] searchIgnoreResultCodes;

  /** ldap socket factory used for SSL and TLS. */
  private SSLSocketFactory sslSocketFactory;


  /** Default constructor. */
  public JLdapProviderConfig()
  {
    setOperationExceptionResultCodes(ResultCode.CONNECT_ERROR);
    setControlProcessor(new ControlProcessor<>(new JLdapControlHandler()));
    searchIgnoreResultCodes = new ResultCode[] {
      ResultCode.TIME_LIMIT_EXCEEDED,
      ResultCode.SIZE_LIMIT_EXCEEDED,
    };
  }


  /**
   * Returns the connection constraints.
   *
   * @return  ldap connection constraints
   */
  public LDAPConstraints getLDAPConstraints()
  {
    return ldapConstraints;
  }


  /**
   * Sets the connection constraints.
   *
   * @param  constraints  ldap connection constraints
   */
  public void setLDAPConstraints(final LDAPConstraints constraints)
  {
    checkImmutable();
    logger.trace("setting ldapConstraints: {}", constraints);
    ldapConstraints = constraints;
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


  /**
   * Returns the SSL socket factory to use for TLS/SSL connections.
   *
   * @return  SSL socket factory
   */
  public SSLSocketFactory getSslSocketFactory()
  {
    return sslSocketFactory;
  }


  /**
   * Sets the SSL socket factory to use for TLS/SSL connections.
   *
   * @param  sf  SSL socket factory
   */
  public void setSslSocketFactory(final SSLSocketFactory sf)
  {
    checkImmutable();
    logger.trace("setting sslSocketFactory: {}", sf);
    sslSocketFactory = sf;
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::operationExceptionResultCodes=%s, properties=%s, " +
        "connectionStrategy=%s, controlProcessor=%s, ldapConstraints=%s, " +
        "searchIgnoreResultCodes=%s, sslSocketFactory=%s]",
        getClass().getName(),
        hashCode(),
        Arrays.toString(getOperationExceptionResultCodes()),
        getProperties(),
        getConnectionStrategy(),
        getControlProcessor(),
        ldapConstraints,
        Arrays.toString(searchIgnoreResultCodes),
        sslSocketFactory);
  }
}
