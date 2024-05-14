/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.asn1.ApplicationDERTag;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.ContextType;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;

/**
 * LDAP simple bind request.
 *
 * @author  Middleware Services
 */
public final class SimpleBindRequest extends AbstractRequestMessage implements BindRequest
{

  /** LDAP DN to bind as. */
  private String ldapDN;

  /** Password for the LDAP DN. */
  private byte[] password;


  /**
   * Default constructor.
   */
  private SimpleBindRequest() {}


  /**
   * Creates a new simple bind request.
   *
   * @param  name  to bind as
   * @param  pass  to bind with
   */
  public SimpleBindRequest(final String name, final String pass)
  {
    setLdapDN(name);
    setPassword(pass);
  }


  /**
   * Creates a new simple bind request.
   *
   * @param  name  to bind as
   * @param  cred  to bind with
   */
  public SimpleBindRequest(final String name, final Credential cred)
  {
    setLdapDN(name);
    setPassword(cred.getBytes());
  }


  /**
   * Sets the LDAP DN.
   *
   * @param  name  LDAP DN to set
   *
   * @throws  IllegalArgumentException  if name is null or empty
   */
  private void setLdapDN(final String name)
  {
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("bind request name cannot be null or empty");
    }
    ldapDN = name;
  }


  /**
   * Sets the password.
   *
   * @param  pass  password to set
   *
   * @throws  IllegalArgumentException  if pass is null or empty
   */
  private void setPassword(final String pass)
  {
    if (pass == null || pass.isEmpty()) {
      throw new IllegalArgumentException("bind request password cannot be null or empty");
    }
    setPassword(LdapUtils.utf8Encode(pass, false));
  }


  /**
   * Sets the password.
   *
   * @param  pass  password to set
   *
   * @throws  IllegalArgumentException  if pass is null or empty
   */
  private void setPassword(final byte[] pass)
  {
    if (pass == null || pass.length == 0) {
      throw new IllegalArgumentException("bind request password cannot be null or empty");
    }
    password = pass;
  }


  @Override
  protected DEREncoder[] getRequestEncoders(final int id)
  {
    if (ldapDN == null || ldapDN.isEmpty()) {
      throw new IllegalStateException("bind request DN cannot be null or empty");
    }
    if (password == null || password.length == 0) {
      throw new IllegalStateException("bind request password cannot be null or empty");
    }
    return new DEREncoder[] {
      new IntegerType(id),
      new ConstructedDEREncoder(
        new ApplicationDERTag(PROTOCOL_OP, true),
        new IntegerType(VERSION),
        new OctetStringType(ldapDN),
        new ContextType(0, password)),
    };
  }


  @Override
  public String toString()
  {
    return super.toString() + ", " + "dn=" + ldapDN;
  }


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static Builder builder()
  {
    return new Builder();
  }


  /** Simple bind request builder. */
  public static final class Builder extends
    AbstractRequestMessage.AbstractBuilder<SimpleBindRequest.Builder, SimpleBindRequest>
  {


    /**
     * Default constructor.
     */
    private Builder()
    {
      super(new SimpleBindRequest());
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    /**
     * Sets the bind DN.
     *
     * @param  dn  ldap DN
     *
     * @return  this builder
     */
    public Builder dn(final String dn)
    {
      object.setLdapDN(dn);
      return self();
    }


    /**
     * Sets the bind password.
     *
     * @param  password  associated with the DN
     *
     * @return  this builder
     */
    public Builder password(final String password)
    {
      object.setPassword(password);
      return self();
    }


    /**
     * Sets the bind password.
     *
     * @param  credential  associated with the DN
     *
     * @return  this builder
     */
    public Builder password(final Credential credential)
    {
      object.setPassword(credential.getBytes());
      return self();
    }
  }
}
