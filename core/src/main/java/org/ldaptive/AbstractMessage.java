/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.BooleanType;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.control.ControlFactory;
import org.ldaptive.control.ResponseControl;

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
    return controls.toArray(new ResponseControl[0]);
  }


  /**
   * Adds the supplied controls to this message.
   *
   * @param  cntrls  to add
   */
  public void addControls(final ResponseControl... cntrls)
  {
    for (ResponseControl c : cntrls) {
      controls.add(c);
    }
  }


  /**
   * Copies the property values from the supplied message to this message.
   *
   * @param  <T>  type of message
   * @param  message  to copy from
   */
  protected <T extends Message> void copyValues(final T message)
  {
    setMessageID(message.getMessageID());
    addControls(message.getControls());
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
    public MessageIDHandler(final AbstractMessage response)
    {
      super(response);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setMessageID(IntegerType.decodeUnsignedPrimitive(encoded));
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
    public ControlsHandler(final AbstractMessage response)
    {
      super(response);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      final ControlParser p = new ControlParser();
      p.parse(encoded);
      getObject().addControls(
        ControlFactory.createResponseControl(
          p.getOid().isPresent() ? p.getOid().get() : null,
          p.getCritical().isPresent() ? p.getCritical().get() : false,
          p.getValue().isPresent() ? p.getValue().get() : null));
    }
  }


  /**
   * Parses a buffer containing an LDAP control.
   */
  protected static class ControlParser
  {

    /** Parser for decoding LDAP controls. */
    private final DERParser parser = new DERParser();

    /** Control criticality. */
    private boolean critical;

    /** Control oid. */
    private String oid;

    /** Control value. */
    private DERBuffer value;


    /**
     * Creates a new control parser.
     */
    public ControlParser()
    {
      parser.registerHandler("/SEQ/BOOL", (p, e) -> critical = BooleanType.decode(e));
      parser.registerHandler("/SEQ/OCTSTR[0]", (p, e) -> oid = OctetStringType.decode(e));
      parser.registerHandler("/SEQ/OCTSTR[1]", (p, e) -> value = e.slice());
    }


    /**
     * Examines the supplied buffer and parses an LDAP control if one is found.
     *
     * @param  buffer  to parse
     */
    public void parse(final DERBuffer buffer)
    {
      parser.parse(buffer);
    }


    /**
     * Returns the control criticality.
     *
     * @return  criticality or empty
     */
    public Optional<Boolean> getCritical()
    {
      return Optional.ofNullable(critical);
    }


    /**
     * Returns the control oid.
     *
     * @return  control oid or empty
     */
    public Optional<String> getOid()
    {
      return Optional.ofNullable(oid);
    }


    /**
     * Returns the control value.
     *
     * @return  control value or empty
     */
    public Optional<DERBuffer> getValue()
    {
      return Optional.ofNullable(value);
    }
  }


  // CheckStyle:OFF
  protected abstract static class AbstractBuilder<B, T extends AbstractMessage>
  {

    protected final T object;


    protected AbstractBuilder(final T t)
    {
      object = t;
    }


    protected abstract B self();


    public B messageID(final int id)
    {
      object.setMessageID(id);
      return self();
    }


    public B controls(final ResponseControl... controls)
    {
      object.addControls(controls);
      return self();
    }


    public T build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
