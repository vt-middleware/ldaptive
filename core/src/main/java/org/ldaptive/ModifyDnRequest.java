/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.asn1.ApplicationDERTag;
import org.ldaptive.asn1.BooleanType;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.ContextDERTag;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;

/**
 * LDAP modify DN request defined as:
 *
 * <pre>
   ModifyDNRequest ::= [APPLICATION 12] SEQUENCE {
     entry           LDAPDN,
     newrdn          RelativeLDAPDN,
     deleteoldrdn    BOOLEAN,
     newSuperior     [0] LDAPDN OPTIONAL }
 * </pre>
 *
 * @author  Middleware Services
 */
public class ModifyDnRequest extends AbstractRequestMessage
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 12;

  /** DN to modify. */
  private String oldModifyDn;

  /** New DN. */
  private String newModifyRDn;

  /** Whether to delete the old RDN attribute. */
  private boolean deleteOldRDn;

  /** New superior DN. */
  private String newSuperiorDn;


  /**
   * Default constructor.
   */
  private ModifyDnRequest() {}


  /**
   * Creates a new modify DN request.
   *
   * @param  oldDN  old modify DN
   * @param  newRDN  new modify DN
   * @param  delete  whether to delete the old RDN attribute
   */
  public ModifyDnRequest(final String oldDN, final String newRDN, final boolean delete)
  {
    this(oldDN, newRDN, delete, null);
  }


  /**
   * Creates a new modify DN request.
   *
   * @param  oldDN  old modify DN
   * @param  newRDN  new modify DN
   * @param  delete  whether to delete the old RDN attribute
   * @param newSuperior  new superior DN
   */
  public ModifyDnRequest(final String oldDN, final String newRDN, final boolean delete, final String newSuperior)
  {
    oldModifyDn = LdapUtils.assertNotNullArg(oldDN, "old DN cannot be null");
    newModifyRDn = LdapUtils.assertNotNullArg(newRDN, "new RDN cannot be null");
    deleteOldRDn = delete;
    newSuperiorDn = newSuperior;
  }


  /**
   * Returns the old DN.
   *
   * @return  old DN
   */
  public String getOldDn()
  {
    return oldModifyDn;
  }


  /**
   * Returns the new RDN.
   *
   * @return  new RDN
   */
  public String getNewRDn()
  {
    return newModifyRDn;
  }


  /**
   * Whether to delete the old RDN.
   *
   * @return  whether to delete the old RDN
   */
  public boolean isDeleteOldRDn()
  {
    return deleteOldRDn;
  }


  /**
   * Returns the new superior DN.
   *
   * @return  new superior DN
   */
  public String getNewSuperiorDn()
  {
    return newSuperiorDn;
  }


  @Override
  protected DEREncoder[] getRequestEncoders(final int id)
  {
    if (newSuperiorDn == null) {
      return new DEREncoder[] {
        new IntegerType(id),
        new ConstructedDEREncoder(
          new ApplicationDERTag(PROTOCOL_OP, true),
          new OctetStringType(oldModifyDn),
          new OctetStringType(newModifyRDn),
          new BooleanType(deleteOldRDn)),
      };
    } else {
      return new DEREncoder[] {
        new IntegerType(id),
        new ConstructedDEREncoder(
          new ApplicationDERTag(PROTOCOL_OP, true),
          new OctetStringType(oldModifyDn),
          new OctetStringType(newModifyRDn),
          new BooleanType(deleteOldRDn),
          new OctetStringType(new ContextDERTag(0, false), newSuperiorDn)),
      };
    }
  }


  @Override
  public String toString()
  {
    return super.toString() + ", " +
      "oldModifyDn=" + oldModifyDn + ", " +
      "newModifyRDn=" + newModifyRDn + ", " +
      "delete=" + deleteOldRDn + ", " +
      "superior=" + newSuperiorDn;
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


  /** Modify DN request builder. */
  public static class Builder extends AbstractRequestMessage.AbstractBuilder<ModifyDnRequest.Builder, ModifyDnRequest>
  {


    /**
     * Default constructor.
     */
    protected Builder()
    {
      super(new ModifyDnRequest());
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    /**
     * Sets the old modify ldap DN.
     *
     * @param  dn  ldap DN
     *
     * @return  this builder
     */
    public Builder oldDN(final String dn)
    {
      object.oldModifyDn = dn;
      return self();
    }


    /**
     * Sets the new modify ldap DN.
     *
     * @param  dn  ldap DN
     *
     * @return  this builder
     */
    public Builder newRDN(final String dn)
    {
      object.newModifyRDn = dn;
      return self();
    }


    /**
     * Sets whether to delete the old RDN.
     *
     * @param  delete  whether to delete the old RDN
     *
     * @return  this builder
     */
    public Builder delete(final boolean delete)
    {
      object.deleteOldRDn = delete;
      return self();
    }


    /**
     * Sets the new superior ldap DN.
     *
     * @param  dn  ldap DN
     *
     * @return  this builder
     */
    public Builder superior(final String dn)
    {
      object.newSuperiorDn = dn;
      return self();
    }
  }
}
