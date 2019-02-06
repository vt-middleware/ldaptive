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
 * LDAP add request defined as:
 *
 * <pre>
   AddRequest ::= [APPLICATION 8] SEQUENCE {
     entry           LDAPDN,
     attributes      AttributeList }

   AttributeList ::= SEQUENCE OF attribute Attribute
 * </pre>
 *
 * @author  Middleware Services
 */
public class AddRequest extends AbstractRequestMessage
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 8;

  /** LDAP DN to add. */
  private String ldapDN;

  /** Attributes to add to the entry. */
  private Attribute[] attributes;


  /**
   * Default constructor.
   */
  private AddRequest() {}


  /**
   * Creates a new add request.
   *
   * @param  entry  DN to add
   * @param  attrs  to add to the entry
   */
  public AddRequest(final String entry, final Attribute... attrs)
  {
    ldapDN = entry;
    attributes = attrs;
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
          Stream.of(attributes).map(a ->
            new ConstructedDEREncoder(
              UniversalDERTag.SEQ,
              new OctetStringType(a.getName()),
              new ConstructedDEREncoder(
                UniversalDERTag.SET,
                  Stream.of(a.getValues()).map(v ->
                    new OctetStringType(v)).toArray(DEREncoder[]::new)))).toArray(DEREncoder[]::new))),
    };
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ")
      .append("dn=").append(ldapDN).append(", ")
      .append("attributes=").append(Arrays.toString(attributes)).toString();
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


  /** Add request builder. */
  public static class Builder extends AbstractRequestMessage.AbstractBuilder<AddRequest.Builder, AddRequest>
  {


    /**
     * Default constructor.
     */
    protected Builder()
    {
      super(new AddRequest());
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
     * Sets the attributes.
     *
     * @param  attrs  attributes
     *
     * @return  this builder
     */
    public Builder attributes(final Attribute... attrs)
    {
      object.attributes = attrs;
      return self();
    }
  }
}
