/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
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
@SuperBuilder
public abstract class AbstractMessage implements Message
{

  /** LDAP controls. */
  private final List<ResponseControl> controls = new ArrayList<>();

  /** Protocol message ID. */
  private int messageID;


  public AbstractMessage(final int messageID, final List<ResponseControl> controls) {
    this.messageID = messageID;
    this.controls.addAll(controls);
  }

  @Override
  public final int getMessageID()
  {
    return messageID;
  }


  @Override
  public final ResponseControl[] getControls()
  {
    return controls.toArray(ResponseControl[]::new);
  }


  /**
   * Returns whether the base properties of this message are equal. Those include message ID and controls.
   *
   * @param  message  to compare
   *
   * @return  whether message properties are equal
   */
  public final boolean equalsMessage(final Message message)
  {
    if (message == this) {
      return true;
    }
    return LdapUtils.areEqual(getMessageID(), message.getMessageID()) &&
      LdapUtils.areEqual(getControls(), message.getControls());
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
    return getClass().getName() + "@" + hashCode() + "::" + "messageID=" + messageID + ", " + "controls=" + controls;
  }


  /** Parse handler implementation for the message ID. */
  protected static class MessageIDHandler<T extends Message> extends AbstractParseHandler<T, Message.Builder<T>>
  {
    /** DER path to message id. */
    public static final DERPath PATH = new DERPath("/SEQ/INT[0]");


    /**
     * Creates a new message ID handler.
     *
     * @param  builder  That will produces the message.
     */
    public MessageIDHandler(final Message.Builder<T> builder)
    {
      super(builder);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getBuilder().messageID(IntegerType.decodeUnsignedPrimitive(encoded));
    }
  }


  /** Parse handler implementation for the message controls. */
  protected static class ControlsHandler<T extends Message> extends AbstractParseHandler<T, Message.Builder<T>>
  {
    /** DER path to controls. */
    public static final DERPath PATH = new DERPath("/SEQ/CTX(0)/SEQ");


    /**
     * Creates a new controls handler.
     *
     * @param  builder  Message builder.
     */
    public ControlsHandler(final Message.Builder<T> builder)
    {
      super(builder);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      final ControlParser p = new ControlParser();
      p.parse(encoded);
      if (p.getOid().isEmpty()) {
        throw new IllegalArgumentException("Cannot parse response control without OID");
      }
      getBuilder().controls(
        ControlFactory.createResponseControl(
          p.getOid().get(),
          p.getCritical().isPresent() ? p.getCritical().get() : false,
          p.getValue().isPresent() ? p.getValue().get() : null));
    }
  }


  /**
   * Parses a buffer containing an LDAP control.
   */
  protected static class ControlParser
  {
    /** DER path to criticality. */
    private static final DERPath CRITICAL_PATH = new DERPath("/BOOL[1]");

    /** DER path to OID. */
    private static final DERPath OID_PATH = new DERPath("/OCTSTR[0]");

    /** DER path to value. */
    private static final DERPath VALUE_PATH = new DERPath("/OCTSTR[1]");

    /** DER path to alternate value. */
    private static final DERPath ALT_VALUE_PATH = new DERPath("/OCTSTR[2]");

    /** Parser for decoding LDAP controls. */
    private final DERParser parser = new DERParser();

    /** Control criticality. */
    private Boolean critical;

    /** Control oid. */
    private String oid;

    /** Control value. */
    private DERBuffer value;


    /**
     * Creates a new control parser.
     */
    public ControlParser()
    {
      parser.registerHandler(CRITICAL_PATH, (p, e) -> critical = BooleanType.decode(e));
      parser.registerHandler(OID_PATH, (p, e) -> oid = OctetStringType.decode(e));
      parser.registerHandler(VALUE_PATH, (p, e) -> value = e.slice());
      parser.registerHandler(ALT_VALUE_PATH, (p, e) -> {
        if (value == null) {
          value = e.slice();
        }
      });
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
}
