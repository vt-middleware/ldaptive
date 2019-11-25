/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

import java.nio.charset.StandardCharsets;
import org.ldaptive.AbstractRequestMessage;
import org.ldaptive.BindRequest;
import org.ldaptive.asn1.ApplicationDERTag;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.ContextDERTag;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;

/**
 * LDAP SASL bind request defined as:
 *
 * <pre>
   SaslCredentials ::= SEQUENCE {
     mechanism               LDAPString,
     credentials             OCTET STRING OPTIONAL }
 * </pre>
 *
 * @author  Middleware Services
 */
public class SaslBindRequest extends AbstractRequestMessage implements BindRequest
{

  /** SASL mechanism. */
  private String saslMechanism;

  /** SASL credentials. */
  private byte[] saslCredentials;


  /**
   * Default constructor.
   */
  private SaslBindRequest() {}


  /**
   * Creates a new SASL bind request.
   *
   * @param  mechanism  type of SASL request
   */
  public SaslBindRequest(final String mechanism)
  {
    this(mechanism, (byte[]) null);
  }


  /**
   * Creates a new SASL bind request.
   *
   * @param  mechanism  type of SASL request
   * @param  credentials  to bind as
   */
  public SaslBindRequest(final String mechanism, final String credentials)
  {
    this(mechanism, credentials != null ? credentials.getBytes(StandardCharsets.UTF_8) : null);
  }


  /**
   * Creates a new SASL bind request.
   *
   * @param  mechanism  type of SASL request
   * @param  credentials  to bind as
   */
  public SaslBindRequest(final String mechanism, final byte[] credentials)
  {
    saslMechanism = mechanism;
    saslCredentials = credentials;
  }


  @Override
  protected DEREncoder[] getRequestEncoders(final int id)
  {
    final ConstructedDEREncoder saslMechanismEncoder;
    // CheckStyle:MagicNumber OFF
    if (saslCredentials == null) {
      saslMechanismEncoder = new ConstructedDEREncoder(
        new ContextDERTag(3, true),
        new OctetStringType(saslMechanism));
    } else {
      saslMechanismEncoder = new ConstructedDEREncoder(
        new ContextDERTag(3, true),
        new OctetStringType(saslMechanism),
        new OctetStringType(saslCredentials));
    }
    // CheckStyle:MagicNumber ON
    return new DEREncoder[] {
      new IntegerType(id),
      new ConstructedDEREncoder(
        new ApplicationDERTag(PROTOCOL_OP, true),
        new IntegerType(VERSION),
        new OctetStringType(""),
        saslMechanismEncoder),
    };
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ")
      .append("saslMechanism=").append(saslMechanism).toString();
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


  /** SASL bind request builder. */
  public static class Builder extends
    AbstractRequestMessage.AbstractBuilder<SaslBindRequest.Builder, SaslBindRequest>
  {


    /**
     * Default constructor.
     */
    protected Builder()
    {
      super(new SaslBindRequest());
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    /**
     * Sets the SASL mechanism.
     *
     * @param  mechanism  SASL mechanism
     *
     * @return  this builder
     */
    public Builder mechanism(final String mechanism)
    {
      object.saslMechanism = mechanism;
      return self();
    }


    /**
     * Sets the SASL credentials.
     *
     * @param  credentials  SASL credentials
     *
     * @return  this builder
     */
    public Builder credentials(final byte[] credentials)
    {
      object.saslCredentials = credentials;
      return self();
    }


    /**
     * Sets the SASL credentials.
     *
     * @param  credentials  SASL credentials
     *
     * @return  this builder
     */
    public Builder credentials(final String credentials)
    {
      object.saslCredentials = credentials != null ? credentials.getBytes(StandardCharsets.UTF_8) : null;
      return self();
    }
  }
}
