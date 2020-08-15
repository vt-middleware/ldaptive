/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.ContextDERTag;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.OctetStringType;

/**
 * Presence search filter component defined as:
 *
 * <pre>
 * (attributeDescription=*)
 * </pre>
 *
 * @author  Middleware Services
 */
public class PresenceFilter implements Filter
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10093;

  /** Attribute description. */
  private final String attributeDesc;


  /**
   * Creates a new presence filter.
   *
   * @param  name  attribute description
   */
  public PresenceFilter(final String name)
  {
    attributeDesc = name;
  }


  /**
   * Returns the attribute description.
   *
   * @return  attribute description
   */
  public String getAttributeDesc()
  {
    return attributeDesc;
  }


  @Override
  public DEREncoder getEncoder()
  {
    return new OctetStringType(new ContextDERTag(Type.PRESENCE.ordinal(), false), attributeDesc);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof PresenceFilter) {
      final PresenceFilter v = (PresenceFilter) o;
      return LdapUtils.areEqual(attributeDesc, v.attributeDesc);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, attributeDesc);
  }
}
