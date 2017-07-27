/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.ldaptive.CompareOperation;
import org.ldaptive.CompareRequest;
import org.ldaptive.Connection;
import org.ldaptive.Credential;
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
  protected static final String DEFAULT_SCHEME = "SHA:SHA";

  /** Default password attribute. Value is {@value}. */
  protected static final String DEFAULT_ATTRIBUTE = "userPassword";

  /** Password scheme. */
  private Scheme passwordScheme = new Scheme(DEFAULT_SCHEME);

  /** Password attribute. */
  private String passwordAttribute = DEFAULT_ATTRIBUTE;


  /**
   * Returns the password scheme.
   *
   * @return  password scheme
   */
  public String getPasswordScheme()
  {
    return passwordScheme.toString();
  }


  /**
   * Sets the password scheme.
   *
   * @param  s  password scheme
   */
  public void setPasswordScheme(final String s)
  {
    passwordScheme = new Scheme(s);
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
    final byte[] hash = digestCredential(criteria.getCredential(), passwordScheme.getAlgorithm());
    final LdapAttribute la = new LdapAttribute(
      passwordAttribute,
      String.format("{%s}%s", passwordScheme.getLabel(), LdapUtils.base64Encode(hash)).getBytes());
    final CompareOperation compare = new CompareOperation(c);
    final CompareRequest request = new CompareRequest(criteria.getDn(), la);
    request.setControls(processRequestControls(criteria));

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
   * Digests the supplied credential using the supplied algorithm.
   *
   * @param  credential  to digest
   * @param  algorithm  type of digest to use
   *
   * @return  digested credential
   *
   * @throws  LdapException  if the supplied algorithm cannot be found
   */
  protected byte[] digestCredential(final Credential credential, final String algorithm)
    throws LdapException
  {
    try {
      final MessageDigest md = MessageDigest.getInstance(algorithm);
      md.update(credential.getBytes());
      return md.digest();
    } catch (NoSuchAlgorithmException e) {
      throw new LdapException(e);
    }
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


  /**
   * Represents a password scheme used for attribute comparison.
   */
  public static class Scheme
  {

    /** Label of the scheme. */
    private final String label;

    /** Algorithm used by this scheme. */
    private final String algorithm;


    /**
     * Creates a new scheme.
     *
     * @param  labelAndAlgorithm  colon delimited label:algorithm
     */
    public Scheme(final String labelAndAlgorithm)
    {
      final String[] s = labelAndAlgorithm.split(":", 2);
      label = s[0];
      algorithm = s.length == 2 ? s[1] : s[0];
    }


    /**
     * Creates a new scheme.
     *
     * @param  l  label
     * @param  a  algorithm
     */
    public Scheme(final String l, final String a)
    {
      label = l;
      algorithm = a;
    }


    /**
     * Returns the scheme label.
     *
     * @return  label
     */
    public String getLabel()
    {
      return label;
    }


    /**
     * Returns the scheme algorithm.
     *
     * @return  algorithm
     */
    public String getAlgorithm()
    {
      return algorithm;
    }


    @Override
    public String toString()
    {
      return String.format("%s:%s", label, algorithm);
    }
  }
}
