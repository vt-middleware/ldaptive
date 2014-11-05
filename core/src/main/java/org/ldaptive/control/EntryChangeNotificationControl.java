/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import java.nio.ByteBuffer;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;

/**
 * Response control for persistent search. See
 * http://tools.ietf.org/id/draft-ietf-ldapext-psearch-03.txt. Control is
 * defined as:
 *
 * <pre>
   EntryChangeNotification ::= SEQUENCE {
      changeType ENUMERATED {
         add             (1),
         delete          (2),
         modify          (4),
         modDN           (8)
      },
      previousDN   LDAPDN OPTIONAL,     -- modifyDN ops. only
      changeNumber INTEGER OPTIONAL     -- if supported
   }
 * </pre>
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class EntryChangeNotificationControl extends AbstractControl
  implements ResponseControl
{

  /** OID of this control. */
  public static final String OID = "2.16.840.1.113730.3.4.7";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 773;

  /** change type. */
  private PersistentSearchChangeType changeType;

  /** previous dn. */
  private String previousDn;

  /** change number. */
  private long changeNumber = -1;


  /** Default constructor. */
  public EntryChangeNotificationControl()
  {
    super(OID);
  }


  /**
   * Creates a new entry change notification control.
   *
   * @param  critical  whether this control is critical
   */
  public EntryChangeNotificationControl(final boolean critical)
  {
    super(OID, critical);
  }


  /**
   * Creates a new entry change notification control.
   *
   * @param  type  persistent search change type
   */
  public EntryChangeNotificationControl(final PersistentSearchChangeType type)
  {
    this(type, false);
  }


  /**
   * Creates a new entry change notification control.
   *
   * @param  type  persistent search change type
   * @param  critical  whether this control is critical
   */
  public EntryChangeNotificationControl(
    final PersistentSearchChangeType type,
    final boolean critical)
  {
    super(OID, critical);
    setChangeType(type);
  }


  /**
   * Creates a new entry change notification control.
   *
   * @param  type  persistent search change type
   * @param  dn  previous dn
   * @param  number  change number
   */
  public EntryChangeNotificationControl(
    final PersistentSearchChangeType type,
    final String dn,
    final long number)
  {
    this(type, dn, number, false);
  }


  /**
   * Creates a new entry change notification control.
   *
   * @param  type  persistent search change type
   * @param  dn  previous dn
   * @param  number  change number
   * @param  critical  whether this control is critical
   */
  public EntryChangeNotificationControl(
    final PersistentSearchChangeType type,
    final String dn,
    final long number,
    final boolean critical)
  {
    super(OID, critical);
    setChangeType(type);
    setPreviousDn(dn);
    setChangeNumber(number);
  }


  /**
   * Returns the change type.
   *
   * @return  change type
   */
  public PersistentSearchChangeType getChangeType()
  {
    return changeType;
  }


  /**
   * Sets the change type.
   *
   * @param  type  change type
   */
  public void setChangeType(final PersistentSearchChangeType type)
  {
    changeType = type;
  }


  /**
   * Returns the previous dn.
   *
   * @return  previous dn
   */
  public String getPreviousDn()
  {
    return previousDn;
  }


  /**
   * Sets the previous dn.
   *
   * @param  dn  previous dn
   */
  public void setPreviousDn(final String dn)
  {
    previousDn = dn;
  }


  /**
   * Returns the change number.
   *
   * @return  change number
   */
  public long getChangeNumber()
  {
    return changeNumber;
  }


  /**
   * Sets the change number.
   *
   * @param  number  change number
   */
  public void setChangeNumber(final long number)
  {
    changeNumber = number;
  }


  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        getOID(),
        getCriticality(),
        changeType,
        previousDn,
        changeNumber);
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::criticality=%s, changeType=%s, previousDn=%s, " +
        "changeNumber=%s]",
        getClass().getName(),
        hashCode(),
        getCriticality(),
        changeType,
        previousDn,
        changeNumber);
  }


  /** {@inheritDoc} */
  @Override
  public void decode(final byte[] berValue)
  {
    logger.trace("decoding control: {}", LdapUtils.base64Encode(berValue));

    final DERParser parser = new DERParser();
    parser.registerHandler(ChangeTypeHandler.PATH, new ChangeTypeHandler(this));
    parser.registerHandler(PreviousDnHandler.PATH, new PreviousDnHandler(this));
    parser.registerHandler(
      ChangeNumberHandler.PATH,
      new ChangeNumberHandler(this));
    parser.parse(ByteBuffer.wrap(berValue));
  }


  /** Parse handler implementation for the change type. */
  private static class ChangeTypeHandler
    extends AbstractParseHandler<EntryChangeNotificationControl>
  {

    /** DER path to change type. */
    public static final DERPath PATH = new DERPath("/SEQ/ENUM");


    /**
     * Creates a new change type handler.
     *
     * @param  control  to configure
     */
    public ChangeTypeHandler(final EntryChangeNotificationControl control)
    {
      super(control);
    }


    /** {@inheritDoc} */
    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      final int typeValue = IntegerType.decode(encoded).intValue();
      final PersistentSearchChangeType ct = PersistentSearchChangeType.valueOf(
        typeValue);
      if (ct == null) {
        throw new IllegalArgumentException(
          "Unknown change type code " + typeValue);
      }
      getObject().setChangeType(ct);
    }
  }


  /** Parse handler implementation for the previous dn. */
  private static class PreviousDnHandler
    extends AbstractParseHandler<EntryChangeNotificationControl>
  {

    /** DER path to previous dn. */
    public static final DERPath PATH = new DERPath("/SEQ/OCTSTR[1]");


    /**
     * Creates a new previous dn handler.
     *
     * @param  control  to configure
     */
    public PreviousDnHandler(final EntryChangeNotificationControl control)
    {
      super(control);
    }


    /** {@inheritDoc} */
    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      getObject().setPreviousDn(OctetStringType.decode(encoded));
    }
  }


  /** Parse handler implementation for the change number. */
  private static class ChangeNumberHandler
    extends AbstractParseHandler<EntryChangeNotificationControl>
  {

    /** DER path to change number. */
    public static final DERPath PATH = new DERPath("/SEQ/INT[2]");


    /**
     * Creates a new change number handler.
     *
     * @param  control  to configure
     */
    public ChangeNumberHandler(final EntryChangeNotificationControl control)
    {
      super(control);
    }


    /** {@inheritDoc} */
    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      getObject().setChangeNumber(IntegerType.decode(encoded).intValue());
    }
  }
}
