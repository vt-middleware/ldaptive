/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.asn1.ApplicationDERTag;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;

/**
 * LDAP delete request defined as:
 *
 * <pre>
   DelRequest ::= [APPLICATION 10] LDAPDN
 * </pre>
 *
 * @author  Middleware Services
 */
public class DeleteRequest extends AbstractRequestMessage
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 10;

  /** LDAP DN to delete. */
  private String ldapDn;


  /**
   * Default constructor.
   */
  private DeleteRequest() {}


  /**
   * Creates a new delete request.
   *
   * @param  dn  DN to delete
   */
  public DeleteRequest(final String dn)
  {
    ldapDn = LdapUtils.assertNotNullArg(dn, "DN cannot be null");
  }


  /**
   * Returns the DN.
   *
   * @return  DN
   */
  public String getDn()
  {
    return ldapDn;
  }


  @Override
  protected DEREncoder[] getRequestEncoders(final int id)
  {
    return new DEREncoder[] {
      new IntegerType(id),
      new OctetStringType(new ApplicationDERTag(PROTOCOL_OP, false), ldapDn),
    };
  }


  @Override
  public String toString()
  {
    return super.toString() + ", " + "dn=" + ldapDn;
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


  /** Delete request builder. */
  public static class Builder extends AbstractRequestMessage.AbstractBuilder<DeleteRequest.Builder, DeleteRequest>
  {


    /**
     * Default constructor.
     */
    protected Builder()
    {
      super(new DeleteRequest());
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    /**
     * Sets the ldap DN.
     *
     * @param  dn  ldap DN
     *
     * @return  this builder
     */
    public Builder dn(final String dn)
    {
      object.ldapDn = dn;
      return self();
    }
  }
}
