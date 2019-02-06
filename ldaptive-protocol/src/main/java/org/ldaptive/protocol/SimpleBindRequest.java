/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.nio.charset.StandardCharsets;
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
public class SimpleBindRequest extends AbstractRequestMessage implements BindRequest
{

  /** LDAP DN to bind as. */
  private String ldapDN;

  /** Password for the LDAP DN. */
  private String password;


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
   * Sets the LDAP DN.
   *
   * @param  name  LDAP DN to set
   *
   * @throws  IllegalArgumentException  if name is null or empty
   */
  protected void setLdapDN(final String name)
  {
    if (name == null || name.length() == 0) {
      throw new IllegalArgumentException("name cannot be null or empty");
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
  protected void setPassword(final String pass)
  {
    if (pass == null || pass.length() == 0) {
      throw new IllegalArgumentException("password cannot be null or empty");
    }
    password = pass;
  }


  @Override
  protected DEREncoder[] getRequestEncoders(final int id)
  {
    return new DEREncoder[] {
      new IntegerType(id),
      new ConstructedDEREncoder(
        new ApplicationDERTag(PROTOCOL_OP, true),
        new IntegerType(VERSION),
        new OctetStringType(ldapDN),
        new ContextType(0, password.getBytes(StandardCharsets.UTF_8))),
    };
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ")
      .append("dn=").append(ldapDN).toString();
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
  public static class Builder extends
    AbstractRequestMessage.AbstractBuilder<SimpleBindRequest.Builder, SimpleBindRequest>
  {


    /**
     * Default constructor.
     */
    protected Builder()
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
  }
}
