/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

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
  private String ldapDn;

  /** Attribute description */
  private String attributeDesc;

  /** Assertion value. */
  private String assertionValue;


  /**
   * Default constructor.
   */
  public CompareRequest() {}


  /**
   * Creates a new compare request.
   *
   * @param  dn  to compare
   * @param  name  attribute description
   * @param  value  assertion value
   */
  public CompareRequest(final String dn, final String name, final String value)
  {
    ldapDn = dn;
    attributeDesc = name;
    assertionValue = value;
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
   * Sets the DN.
   *
   * @param  dn  ldapDn to set
   */
  public void setDn(final String dn)
  {
    ldapDn = dn;
  }


  /**
   * Returns the name.
   *
   * @return  name
   */
  public String getName()
  {
    return attributeDesc;
  }


  /**
   * Sets the name.
   *
   * @param  name  attributeDesc to set
   */
  public void setName(final String name)
  {
    attributeDesc = name;
  }


  /**
   * Returns the value.
   *
   * @return  value
   */
  public String getValue()
  {
    return assertionValue;
  }


  /**
   * Sets the value.
   *
   * @param  value  assertionValue to set
   */
  public void setValue(final String value)
  {
    assertionValue = value;
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
          new OctetStringType(attributeDesc),
          new OctetStringType(assertionValue))),
    };
  }


  @Override
  public String toString()
  {
    return super.toString() + ", " +
      "dn=" + ldapDn + ", " +
      "attributeDesc=" + attributeDesc + ", " +
      "assertionValue=" + ("userPassword".equalsIgnoreCase(attributeDesc) ? "<suppressed>" : assertionValue);
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
      object.ldapDn = dn;
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
