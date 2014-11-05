/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import java.util.EnumSet;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.BooleanType;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.UniversalDERTag;

/**
 * Request control for persistent search. See
 * http://tools.ietf.org/id/draft-ietf-ldapext-psearch-03.txt. Control is
 * defined as:
 *
 * <pre>
   PersistentSearch ::= SEQUENCE {
      changeTypes INTEGER,
      changesOnly BOOLEAN,
      returnECs BOOLEAN }
 * </pre>
 *
 * @author  Middleware Services
 */
public class PersistentSearchRequestControl extends AbstractControl
  implements RequestControl
{

  /** OID of this control. */
  public static final String OID = "2.16.840.1.113730.3.4.3";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 761;

  /** persistent search change types. */
  private EnumSet<PersistentSearchChangeType> changeTypes;

  /** whether to return only changed entries. */
  private boolean changesOnly;

  /** whether to return an Entry Change Notification control. */
  private boolean returnEcs;


  /** Default constructor. */
  public PersistentSearchRequestControl()
  {
    super(OID);
  }


  /**
   * Creates a new persistent search request control.
   *
   * @param  types  persistent search change types
   */
  public PersistentSearchRequestControl(
    final EnumSet<PersistentSearchChangeType> types)
  {
    super(OID);
    setChangeTypes(types);
  }


  /**
   * Creates a new persistent search request control.
   *
   * @param  types  persistent search change types
   * @param  critical  whether this control is critical
   */
  public PersistentSearchRequestControl(
    final EnumSet<PersistentSearchChangeType> types,
    final boolean critical)
  {
    super(OID, critical);
    setChangeTypes(types);
  }


  /**
   * Creates a new persistent search request control.
   *
   * @param  types  persistent search change types
   * @param  co  whether only changed entries are returned
   * @param  re  return an Entry Change Notification control
   */
  public PersistentSearchRequestControl(
    final EnumSet<PersistentSearchChangeType> types,
    final boolean co,
    final boolean re)
  {
    super(OID);
    setChangeTypes(types);
    setChangesOnly(co);
    setReturnEcs(re);
  }


  /**
   * Creates a new persistent search request control.
   *
   * @param  types  persistent search change types
   * @param  co  whether only changed entries are returned
   * @param  re  return an Entry Change Notification control
   * @param  critical  whether this control is critical
   */
  public PersistentSearchRequestControl(
    final EnumSet<PersistentSearchChangeType> types,
    final boolean co,
    final boolean re,
    final boolean critical)
  {
    super(OID, critical);
    setChangeTypes(types);
    setChangesOnly(co);
    setReturnEcs(re);
  }


  /**
   * Returns the persistent search change types.
   *
   * @return  persistent search change types
   */
  public EnumSet<PersistentSearchChangeType> getChangeTypes()
  {
    return changeTypes;
  }


  /**
   * Sets the persistent search change types.
   *
   * @param  types  persistent search change types
   */
  public void setChangeTypes(final EnumSet<PersistentSearchChangeType> types)
  {
    changeTypes = types;
  }


  /**
   * Returns whether only changed entries are returned.
   *
   * @return  whether only changed entries are returned
   */
  public boolean getChangesOnly()
  {
    return changesOnly;
  }


  /**
   * Sets whether only changed entries are returned.
   *
   * @param  b  whether only changed entries are returned
   */
  public void setChangesOnly(final boolean b)
  {
    changesOnly = b;
  }


  /**
   * Returns whether to return an Entry Change Notification control.
   *
   * @return  whether to return an Entry Change Notification control
   */
  public boolean getReturnEcs()
  {
    return returnEcs;
  }


  /**
   * Sets whether to return an Entry Change Notification control.
   *
   * @param  b  return an Entry Change Notification control
   */
  public void setReturnEcs(final boolean b)
  {
    returnEcs = b;
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
        changeTypes,
        changesOnly,
        returnEcs);
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::criticality=%s, changeTypes=%s, changesOnly=%s, returnEcs=%s]",
        getClass().getName(),
        hashCode(),
        getCriticality(),
        changeTypes,
        changesOnly,
        returnEcs);
  }


  /** {@inheritDoc} */
  @Override
  public byte[] encode()
  {
    int types = 0;
    for (PersistentSearchChangeType type : getChangeTypes()) {
      types |= type.value();
    }

    final ConstructedDEREncoder se = new ConstructedDEREncoder(
      UniversalDERTag.SEQ,
      new IntegerType(types),
      new BooleanType(getChangesOnly()),
      new BooleanType(getReturnEcs()));
    return se.encode();
  }
}
