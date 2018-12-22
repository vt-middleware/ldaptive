/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.util.Arrays;
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
  private String ldapDN;

  /** Modifications to perform. */
  private Modification[] modifications;


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
  public ModifyRequest(final String entry, final Modification... mod)
  {
    ldapDN = entry;
    modifications = mod;
  }


  @Override
  protected DEREncoder[] getRequestEncoders(final int id)
  {
    return new DEREncoder[] {
      new IntegerType(id),
      new ConstructedDEREncoder(
        new ApplicationDERTag(PROTOCOL_OP, true),
        new OctetStringType(ldapDN),
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
                  getAttributeValueEncoders(m.getAttribute().getValues())))))
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
  private DEREncoder[] getAttributeValueEncoders(final byte[][] values)
  {
    if (values == null || values.length == 0) {
      return new DEREncoder[] {() -> EMPTY_BYTE};
    }
    return Stream.of(values).map(v -> new OctetStringType(v)).toArray(DEREncoder[]::new);
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ")
      .append("dn=").append(ldapDN).append(", ")
      .append("modifications=").append(Arrays.toString(modifications)).toString();
  }


  /** Modify request builder. */
  public static class Builder extends AbstractRequestMessage.AbstractBuilder<ModifyRequest.Builder, ModifyRequest>
  {


    /**
     * Default constructor.
     */
    public Builder()
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
      object.ldapDN = dn;
      return self();
    }


    /**
     * Sets the modifications.
     *
     * @param  mod  modifications
     *
     * @return  this builder
     */
    public Builder modificiations(final Modification... mod)
    {
      object.modifications = mod;
      return self();
    }
  }
}
