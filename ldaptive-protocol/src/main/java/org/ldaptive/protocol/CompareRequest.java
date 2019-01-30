/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import org.ldaptive.asn1.ApplicationDERTag;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.asn1.UniversalDERTag;

/**
 * LDAP compare request defined as:
 *
 * <pre>
   CompareRequest ::= [APPLICATION 14] SEQUENCE {
     entry           LDAPDN,
     ava             AttributeValueAssertion }
 * </pre>
 *
 * @author  Middleware Services
 */
public class CompareRequest extends AbstractRequestMessage
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 14;

  /** LDAP DN to compare. */
  private String ldapDN;

  /** Attribute description */
  private String attributeDesc;

  /** Assertion value. */
  private String assertionValue;


  /**
   * Default constructor.
   */
  private CompareRequest() {}


  /**
   * Creates a new compare request.
   *
   * @param  entry  to compare
   * @param  name  attribute description
   * @param  value  assertion value
   */
  public CompareRequest(final String entry, final String name, final String value)
  {
    ldapDN = entry;
    attributeDesc = name;
    assertionValue = value;
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
          new OctetStringType(attributeDesc),
          new OctetStringType(assertionValue))),
    };
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ")
      .append("dn=").append(ldapDN).append(", ")
      .append("attributeDesc=").append(attributeDesc).append(", ")
      .append("assertionValue=").append(assertionValue).toString();
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


  /** Compare request builder. */
  public static class Builder extends AbstractRequestMessage.AbstractBuilder<CompareRequest.Builder, CompareRequest>
  {


    /**
     * Default constructor.
     */
    protected Builder()
    {
      super(new CompareRequest());
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
     * Sets the attribute description.
     *
     * @param  name  attribute description
     *
     * @return  this builder
     */
    public Builder name(final String name)
    {
      object.attributeDesc = name;
      return self();
    }


    /**
     * Sets the assertion value.
     *
     * @param  value  assertion value
     *
     * @return  this builder
     */
    public Builder value(final String value)
    {
      object.assertionValue = value;
      return self();
    }
  }
}
