/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

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
  private String ldapDN;


  /**
   * Default constructor.
   */
  private DeleteRequest() {}


  /**
   * Creates a new delete request.
   *
   * @param  entry  DN to delete
   */
  public DeleteRequest(final String entry)
  {
    ldapDN = entry;
  }


  @Override
  protected DEREncoder[] getRequestEncoders(final int id)
  {
    return new DEREncoder[] {
      new IntegerType(id),
      new OctetStringType(new ApplicationDERTag(PROTOCOL_OP, false), ldapDN),
    };
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ")
      .append("dn=").append(ldapDN).toString();
  }


  /** Delete request builder. */
  public static class Builder extends AbstractRequestMessage.AbstractBuilder<DeleteRequest.Builder, DeleteRequest>
  {


    /**
     * Default constructor.
     */
    public Builder()
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
      object.ldapDN = dn;
      return self();
    }
  }
}
