/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.ldaptive.CompareOperation;
import org.ldaptive.CompareRequest;
import org.ldaptive.Connection;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.Response;

/**
 * Provides implementation common to compare authentication handlers.
 *
 * @author  Middleware Services
 */
public abstract class AbstractCompareAuthenticationHandler extends AbstractAuthenticationHandler
{

  /** Default password scheme. Value is {@value}. */
  protected static final String DEFAULT_SCHEME = "SHA";

  /** Default password attribute. Value is {@value}. */
  protected static final String DEFAULT_ATTRIBUTE = "userPassword";

  /** Password scheme. */
  private String passwordScheme = DEFAULT_SCHEME;

  /** Password attribute. */
  private String passwordAttribute = DEFAULT_ATTRIBUTE;


  /**
   * Returns the password scheme.
   *
   * @return  password scheme
   */
  public String getPasswordScheme()
  {
    return passwordScheme;
  }


  /**
   * Sets the password scheme. Must equal a known message digest algorithm.
   *
   * @param  s  password scheme
   */
  public void setPasswordScheme(final String s)
  {
    passwordScheme = s;
  }


  /**
   * Returns the password attribute.
   *
   * @return  password attribute
   */
  public String getPasswordAttribute()
  {
    return passwordAttribute;
  }


  /**
   * Sets the password attribute. Must equal a readable attribute in LDAP scheme.
   *
   * @param  s  password attribute
   */
  public void setPasswordAttribute(final String s)
  {
    passwordAttribute = s;
  }


  @Override
  protected AuthenticationHandlerResponse authenticateInternal(
    final Connection c,
    final AuthenticationCriteria criteria)
    throws LdapException
  {
    byte[] hash;
    try {
      final MessageDigest md = MessageDigest.getInstance(passwordScheme);
      md.update(criteria.getCredential().getBytes());
      hash = md.digest();
    } catch (NoSuchAlgorithmException e) {
      throw new LdapException(e);
    }

    final LdapAttribute la = new LdapAttribute(
      passwordAttribute,
      String.format("{%s}%s", passwordScheme, LdapUtils.base64Encode(hash)).getBytes());
    final CompareOperation compare = new CompareOperation(c);
    final CompareRequest request = new CompareRequest(criteria.getDn(), la);
    request.setControls(getAuthenticationControls());

    final Response<Boolean> compareResponse = compare.execute(request);
    return
      new AuthenticationHandlerResponse(
        compareResponse.getResult(),
        compareResponse.getResultCode(),
        c,
        compareResponse.getMessage(),
        compareResponse.getControls(),
        compareResponse.getMessageId());
  }


  /**
   * Returns a connection that the compare operation should be performed on.
   *
   * @return  connection
   *
   * @throws  LdapException  if an error occurs provisioning the connection
   */
  @Override
  protected abstract Connection getConnection()
    throws LdapException;
}
