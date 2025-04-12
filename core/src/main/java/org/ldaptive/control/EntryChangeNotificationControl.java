/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;

/**
 * Response control for persistent search. See http://tools.ietf.org/id/draft-ietf-ldapext-psearch-03.txt. Control is
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
 */
public class EntryChangeNotificationControl extends AbstractResponseControl
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
  public EntryChangeNotificationControl(final PersistentSearchChangeType type, final boolean critical)
  {
    super(OID, critical);
    changeType = type;
    freeze();
  }


  /**
   * Creates a new entry change notification control.
   *
   * @param  type  persistent search change type
   * @param  dn  previous dn
   * @param  number  change number
   */
  public EntryChangeNotificationControl(final PersistentSearchChangeType type, final String dn, final long number)
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
    changeType = type;
    previousDn = dn;
    changeNumber = number;
    freeze();
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
   * Returns the previous dn.
   *
   * @return  previous dn
   */
  public String getPreviousDn()
  {
    return previousDn;
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


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof EntryChangeNotificationControl && super.equals(o)) {
      final EntryChangeNotificationControl v = (EntryChangeNotificationControl) o;
      return LdapUtils.areEqual(changeType, v.changeType) &&
             LdapUtils.areEqual(previousDn, v.previousDn) &&
             LdapUtils.areEqual(changeNumber, v.changeNumber);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, getOID(), getCriticality(), changeType, previousDn, changeNumber);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "criticality=" + getCriticality() + ", " +
      "changeType=" + changeType + ", " +
      "previousDn=" + previousDn + ", " +
      "changeNumber=" + changeNumber + "]";
  }


  @Override
  public void decode(final DERBuffer encoded)
  {
    freezeAndAssertMutable();
    final DERParser parser = new DERParser();
    parser.registerHandler(ChangeTypeHandler.PATH, new ChangeTypeHandler(this));
    parser.registerHandler(PreviousDnHandler.PATH, new PreviousDnHandler(this));
    parser.registerHandler(ChangeNumberHandler.PATH, new ChangeNumberHandler(this));
    try {
      parser.parse(encoded);
    } catch (Exception e) {
      throw new IllegalArgumentException("Error parsing response", e);
    }
  }


  /** Parse handler implementation for the change type. */
  private static class ChangeTypeHandler extends AbstractParseHandler<EntryChangeNotificationControl>
  {

    /** DER path to change type. */
    public static final DERPath PATH = new DERPath("/SEQ/ENUM[0]");


    /**
     * Creates a new change type handler.
     *
     * @param  control  to configure
     */
    ChangeTypeHandler(final EntryChangeNotificationControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      final int typeValue = IntegerType.decode(encoded).intValue();
      final PersistentSearchChangeType ct = PersistentSearchChangeType.valueOf(typeValue);
      if (ct == null) {
        throw new IllegalArgumentException("Unknown change type code " + typeValue);
      }
      getObject().changeType = ct;
    }
  }


  /** Parse handler implementation for the previous dn. */
  private static class PreviousDnHandler extends AbstractParseHandler<EntryChangeNotificationControl>
  {

    /** DER path to previous dn. */
    public static final DERPath PATH = new DERPath("/SEQ/OCTSTR[1]");


    /**
     * Creates a new previous dn handler.
     *
     * @param  control  to configure
     */
    PreviousDnHandler(final EntryChangeNotificationControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().previousDn = OctetStringType.decode(encoded);
    }
  }


  /** Parse handler implementation for the change number. */
  private static class ChangeNumberHandler extends AbstractParseHandler<EntryChangeNotificationControl>
  {

    /** DER path to change number. */
    public static final DERPath PATH = new DERPath("/SEQ/INT");


    /**
     * Creates a new change number handler.
     *
     * @param  control  to configure
     */
    ChangeNumberHandler(final EntryChangeNotificationControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().changeNumber = IntegerType.decode(encoded).intValue();
    }
  }
}
