/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;
import org.ldaptive.asn1.BooleanType;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.ContextDERTag;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.asn1.UniversalDERTag;
import org.ldaptive.control.RequestControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LDAP message envelope defined as:
 *
 * <pre>
   LDAPMessage ::= SEQUENCE {
     messageID       MessageID,
     protocolOp      CHOICE {
       ...,
     controls       [0] Controls OPTIONAL }

   Control ::= SEQUENCE {
     controlType             LDAPOID,
     criticality             BOOLEAN DEFAULT FALSE,
     controlValue            OCTET STRING OPTIONAL }
 * </pre>
 *
 * @author  Middleware Services
 */
public abstract class AbstractRequestMessage implements Request
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** LDAP controls. */
  private RequestControl[] controls;


  public RequestControl[] getControls()
  {
    return controls;
  }


  public void setControls(final RequestControl... cntrls)
  {
    controls = cntrls;
  }


  @Override
  public byte[] encode(final int id)
  {
    final DEREncoder[] requestEncoders = getRequestEncoders(id);
    final DEREncoder controlEncoder = getControlEncoder();
    final DEREncoder[] encoders;
    if (controlEncoder != null) {
      encoders = LdapUtils.concatArrays(requestEncoders, new DEREncoder[]{controlEncoder});
    } else {
      encoders = requestEncoders;
    }
    final ConstructedDEREncoder se = new ConstructedDEREncoder(UniversalDERTag.SEQ, encoders);
    return se.encode();
  }


  /**
   * Returns the request encoders for this message.
   *
   * @param  id  message ID
   *
   * @return  request encoders
   */
  protected abstract DEREncoder[] getRequestEncoders(int id);


  /**
   * Returns the encoder to any controls that may be set on this message.
   *
   * @return  control encoder
   */
  private DEREncoder getControlEncoder()
  {
    if (controls == null || controls.length == 0) {
      return null;
    }
    final DEREncoder[] controlEncoders = new DEREncoder[controls.length];
    for (int i = 0; i < controls.length; i++) {
      if (controls[i].hasValue()) {
        controlEncoders[i] = new ConstructedDEREncoder(
          UniversalDERTag.SEQ,
          new OctetStringType(controls[i].getOID()),
          new BooleanType(controls[i].getCriticality()),
          new OctetStringType(controls[i].encode()));
      } else {
        controlEncoders[i] = new ConstructedDEREncoder(
          UniversalDERTag.SEQ,
          new OctetStringType(controls[i].getOID()),
          new BooleanType(controls[i].getCriticality()));
      }
    }
    return new ConstructedDEREncoder(new ContextDERTag(0, true), controlEncoders);
  }


  @Override
  public String toString()
  {
    return getClass().getName() + "@" + hashCode() + "::" + "controls=" + Arrays.toString(controls);
  }


  /**
   * Base class for request builders.
   *
   * @param  <B>  type of builder
   * @param  <T>  type of message
   */
  protected abstract static class AbstractBuilder<B, T extends AbstractRequestMessage>
  {

    /** Message to build. */
    protected final T object;


    /**
     * Creates a new abstract builder.
     *
     * @param  t  message to build
     */
    protected AbstractBuilder(final T t)
    {
      object = t;
    }


    /**
     * Returns this builder.
     *
     * @return  builder
     */
    protected abstract B self();


    /**
     * Sets controls on the message.
     *
     * @param  cntrls  controls
     *
     * @return  this builder
     */
    public B controls(final RequestControl... cntrls)
    {
      object.setControls(cntrls);
      return self();
    }


    /**
     * Returns the message.
     *
     * @return  message
     */
    public T build()
    {
      return object;
    }
  }
}
