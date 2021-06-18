/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;
import org.ldaptive.asn1.ApplicationDERTag;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.asn1.UniversalDERTag;

/**
 * LDAP modify request defined as:
 *
 * <pre>
   ModifyRequest ::= [APPLICATION 6] SEQUENCE {
     object          LDAPDN,
     changes         SEQUENCE OF change SEQUENCE {
     operation       ENUMERATED {
       add     (0),
       delete  (1),
       replace (2),
       ...  },
     modification    PartialAttribute } }

   PartialAttribute ::= SEQUENCE {
     type       AttributeDescription,
     vals       SET OF value AttributeValue }

   Attribute ::= PartialAttribute(WITH COMPONENTS {
     ...,
     vals (SIZE(1..MAX))})
 * </pre>
 *
 * @author  Middleware Services
 */
public class ModifyRequest extends AbstractRequestMessage
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 6;

  /** Empty byte. */
  private static final byte[] EMPTY_BYTE = new byte[0];

  /** LDAP DN to modify. */
  private String ldapDn;

  /** Modifications to perform. */
  private AttributeModification[] modifications;


  /**
   * Default constructor.
   */
  private ModifyRequest() {}


  /**
   * Creates a new modify request.
   *
   * @param  entry  DN to modify
   * @param  mod  to make on the object
   */
  public ModifyRequest(final String entry, final AttributeModification... mod)
  {
    ldapDn = entry;
    modifications = mod;
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


  /**
   * Returns the attribute modifications.
   *
   * @return  attributes modifications
   */
  public AttributeModification[] getModifications()
  {
    return modifications;
  }


  @Override
  protected DEREncoder[] getRequestEncoders(final int id)
  {
    return new DEREncoder[] {
      new IntegerType(id),
      new ConstructedDEREncoder(
        new ApplicationDERTag(PROTOCOL_OP, true),
        new OctetStringType(ldapDn),
        new ConstructedDEREncoder(
          UniversalDERTag.SEQ,
          Stream.of(modifications).map(m ->
            new ConstructedDEREncoder(
              UniversalDERTag.SEQ,
              new IntegerType(UniversalDERTag.ENUM, m.getOperation().ordinal()),
              new ConstructedDEREncoder(
                UniversalDERTag.SEQ,
                new OctetStringType(m.getAttribute().getName()),
                new ConstructedDEREncoder(
                  UniversalDERTag.SET,
                  getAttributeValueEncoders(m.getAttribute().getBinaryValues())))))
            .toArray(DEREncoder[]::new))),
    };
  }


  /**
   * Returns attribute value encoders for the supplied values.
   *
   * @param  values  to create encoders for
   *
   * @return  attribute value encoders
   */
  private DEREncoder[] getAttributeValueEncoders(final Collection<byte[]> values)
  {
    if (values == null || values.size() == 0) {
      return new DEREncoder[] {() -> EMPTY_BYTE};
    }
    return values.stream().map(OctetStringType::new).toArray(DEREncoder[]::new);
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ")
      .append("dn=").append(ldapDn).append(", ")
      .append("modifications=").append(Arrays.toString(modifications)).toString();
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


  /** Modify request builder. */
  public static class Builder extends AbstractRequestMessage.AbstractBuilder<ModifyRequest.Builder, ModifyRequest>
  {


    /**
     * Default constructor.
     */
    protected Builder()
    {
      super(new ModifyRequest());
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


    /**
     * Sets the modifications.
     *
     * @param  mod  modifications
     *
     * @return  this builder
     *
     * @deprecated  use {@link #modifications(AttributeModification...)}
     */
    @Deprecated
    public Builder modificiations(final AttributeModification... mod)
    {
      object.modifications = mod;
      return self();
    }


    /**
     * Sets the modifications.
     *
     * @param  mod  modifications
     *
     * @return  this builder
     */
    public Builder modifications(final AttributeModification... mod)
    {
      object.modifications = mod;
      return self();
    }


    /**
     * Sets the modifications.
     *
     * @param  mod  modifications
     *
     * @return  this builder
     *
     * @deprecated  use {@link #modifications(Collection)}
     */
    @Deprecated
    public Builder modificiations(final Collection<AttributeModification> mod)
    {
      object.modifications = mod.toArray(AttributeModification[]::new);
      return self();
    }


    /**
     * Sets the modifications.
     *
     * @param  mod  modifications
     *
     * @return  this builder
     */
    public Builder modifications(final Collection<AttributeModification> mod)
    {
      object.modifications = mod.toArray(AttributeModification[]::new);
      return self();
    }
  }
}
