/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
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
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class SortRequestControl extends AbstractControl
  implements RequestControl
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


  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        getOID(),
        getCriticality(),
        sortKeys);
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::criticality=%s, sortKeys=%s]",
        getClass().getName(),
        hashCode(),
        getCriticality(),
        Arrays.toString(sortKeys));
  }


  /** {@inheritDoc} */
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
      keyEncoders[i] = new ConstructedDEREncoder(
        UniversalDERTag.SEQ,
        l.toArray(new DEREncoder[l.size()]));
    }

    final ConstructedDEREncoder se = new ConstructedDEREncoder(
      UniversalDERTag.SEQ,
      keyEncoders);
    return se.encode();
  }
}
