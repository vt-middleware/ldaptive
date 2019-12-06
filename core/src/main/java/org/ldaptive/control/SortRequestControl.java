/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.ContextType;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.asn1.UniversalDERTag;

/**
 * Request control for server side sorting. See RFC 2891. Control is defined as:
 *
 * <pre>
   SortKeyList ::= SEQUENCE OF SEQUENCE {
      attributeType   AttributeDescription,
      orderingRule    [0] MatchingRuleId OPTIONAL,
      reverseOrder    [1] BOOLEAN DEFAULT FALSE }
 * </pre>
 *
 * @author  Middleware Services
 */
public class SortRequestControl extends AbstractControl implements RequestControl
{

  /** OID of this control. */
  public static final String OID = "1.2.840.113556.1.4.473";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 727;

  /** sort keys. */
  private SortKey[] sortKeys;


  /** Default constructor. */
  public SortRequestControl()
  {
    super(OID);
  }


  /**
   * Creates a new sort request control.
   *
   * @param  keys  sort keys
   */
  public SortRequestControl(final SortKey[] keys)
  {
    super(OID);
    setSortKeys(keys);
  }


  /**
   * Creates a new sort request control.
   *
   * @param  keys  sort keys
   * @param  critical  whether this control is critical
   */
  public SortRequestControl(final SortKey[] keys, final boolean critical)
  {
    super(OID, critical);
    setSortKeys(keys);
  }


  @Override
  public boolean hasValue()
  {
    return true;
  }


  /**
   * Returns the sort keys.
   *
   * @return  sort keys
   */
  public SortKey[] getSortKeys()
  {
    return sortKeys;
  }


  /**
   * Sets the sort keys.
   *
   * @param  keys  sort keys
   */
  public void setSortKeys(final SortKey[] keys)
  {
    sortKeys = keys;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof SortRequestControl && super.equals(o)) {
      final SortRequestControl v = (SortRequestControl) o;
      return LdapUtils.areEqual(sortKeys, v.sortKeys);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, getOID(), getCriticality(), sortKeys);
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("criticality=").append(getCriticality()).append(", ")
      .append("sortKeys=").append(Arrays.toString(sortKeys)).append("]").toString();
  }


  @Override
  public byte[] encode()
  {
    final DEREncoder[] keyEncoders = new DEREncoder[sortKeys.length];
    for (int i = 0; i < sortKeys.length; i++) {
      final List<DEREncoder> l = new ArrayList<>();
      l.add(new OctetStringType(sortKeys[i].getAttributeDescription()));
      if (sortKeys[i].getMatchingRuleId() != null) {
        l.add(new ContextType(0, sortKeys[i].getMatchingRuleId()));
      }
      if (sortKeys[i].getReverseOrder()) {
        l.add(new ContextType(1, sortKeys[i].getReverseOrder()));
      }
      keyEncoders[i] = new ConstructedDEREncoder(UniversalDERTag.SEQ, l.toArray(new DEREncoder[0]));
    }

    final ConstructedDEREncoder se = new ConstructedDEREncoder(UniversalDERTag.SEQ, keyEncoders);
    return se.encode();
  }
}
