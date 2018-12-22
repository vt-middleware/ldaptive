/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.BooleanType;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.control.ControlFactory;
import org.ldaptive.control.ResponseControl;
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
public abstract class AbstractMessage implements Message
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Protocol message ID. */
  private int messageID;

  /** LDAP controls. */
  private List<ResponseControl> controls = new ArrayList<>();


  @Override
  public int getMessageID()
  {
    return messageID;
  }


  public void setMessageID(final int id)
  {
    messageID = id;
  }


  @Override
  public ResponseControl[] getControls()
  {
    return controls.stream().toArray(ResponseControl[]::new);
  }


  /**
   * Adds the supplied controls to this message.
   *
   * @param  cntrl  to add
   */
  public void addControls(final ResponseControl... cntrl)
  {
    for (ResponseControl c : cntrl) {
      controls.add(c);
    }
  }


  // CheckStyle:EqualsHashCode OFF
  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof AbstractMessage) {
      final AbstractMessage v = (AbstractMessage) o;
      return LdapUtils.areEqual(getMessageID(), v.getMessageID()) &&
        LdapUtils.areEqual(getControls(), v.getControls());
    }
    return false;
  }
  // CheckStyle:EqualsHashCode ON


  /**
   * Returns the hash code for this object.
   *
   * @return  hash code
   */
  @Override
  public abstract int hashCode();


  @Override
  public String toString()
  {
    return new StringBuilder(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("messageID=").append(messageID).append(", ")
      .append("controls=").append(controls).toString();
  }


  /** Parse handler implementation for the message ID. */
  protected static class MessageIDHandler extends AbstractParseHandler<AbstractMessage>
  {

    /** DER path to message id. */
    public static final DERPath PATH = new DERPath("/SEQ/INT[0]");


    /**
     * Creates a new message ID handler.
     *
     * @param  response  to configure
     */
    MessageIDHandler(final AbstractMessage response)
    {
      super(response);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      getObject().setMessageID(IntegerType.decode(encoded).intValue());
    }
  }


  /** Parse handler implementation for the message controls. */
  protected static class ControlsHandler extends AbstractParseHandler<AbstractMessage>
  {

    /** DER path to controls. */
    public static final DERPath PATH = new DERPath("/SEQ/CTX(0)");


    /**
     * Creates a new controls handler.
     *
     * @param  response  to configure
     */
    ControlsHandler(final AbstractMessage response)
    {
      super(response);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      final boolean[] critical = new boolean[1];
      final String[] oid = new String[1];
      final byte[][] value = {null};
      final DERParser p = new DERParser();
      p.registerHandler("/SEQ/BOOL", (p1, e1) -> critical[0] = BooleanType.decode(e1));
      p.registerHandler("/SEQ/OCTSTR[0]", (p1, e1) -> oid[0] = OctetStringType.decode(e1));
      p.registerHandler("/SEQ/OCTSTR[1]", (p1, e1) -> value[0] = OctetStringType.readBuffer(e1));
      p.parse(encoded);
      getObject().addControls(ControlFactory.createResponseControl(oid[0], critical[0], value[0]));
    }
  }


  // CheckStyle:OFF
  protected abstract static class AbstractBuilder<B, T extends AbstractMessage>
  {

    protected final T object;


    AbstractBuilder(final T t)
    {
      object = t;
    }


    protected abstract B self();


    public B messageID(final int id)
    {
      object.setMessageID(id);
      return self();
    }


    public B controls(final ResponseControl... c)
    {
      object.addControls(c);
      return self();
    }


    public T build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
