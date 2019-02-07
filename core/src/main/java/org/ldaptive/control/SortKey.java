/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;

/**
 * Used by {@link SortRequestControl} to declare how sorting should occur. See RFC 3698 for the definition of
 * matchingRuleId.
 *
 * @author  Middleware Services
 */
public class SortKey
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 739;

  /** attribute description. */
  private String attributeDescription;

  /** matching rule id. */
  private String matchingRuleId;

  /** reverse order. */
  private boolean reverseOrder;


  /** Default constructor. */
  public SortKey() {}


  /**
   * Creates a new sort key.
   *
   * @param  attrDescription  attribute description
   */
  public SortKey(final String attrDescription)
  {
    setAttributeDescription(attrDescription);
  }


  /**
   * Creates a new sort key.
   *
   * @param  attrDescription  attribute description
   * @param  ruleId  matching rule id
   */
  public SortKey(final String attrDescription, final String ruleId)
  {
    setAttributeDescription(attrDescription);
    setMatchingRuleId(ruleId);
  }


  /**
   * Creates a new sort key.
   *
   * @param  attrDescription  attribute description
   * @param  ruleId  matching rule id
   * @param  reverse  reverse order
   */
  public SortKey(final String attrDescription, final String ruleId, final boolean reverse)
  {
    setAttributeDescription(attrDescription);
    setMatchingRuleId(ruleId);
    setReverseOrder(reverse);
  }


  /**
   * Returns the attribute description.
   *
   * @return  attribute description
   */
  public String getAttributeDescription()
  {
    return attributeDescription;
  }


  /**
   * Sets the attribute description.
   *
   * @param  s  attribute description
   */
  public void setAttributeDescription(final String s)
  {
    attributeDescription = s;
  }


  /**
   * Returns the matching rule id.
   *
   * @return  matching rule id
   */
  public String getMatchingRuleId()
  {
    return matchingRuleId;
  }


  /**
   * Sets the matching rule id.
   *
   * @param  s  matching rule id
   */
  public void setMatchingRuleId(final String s)
  {
    matchingRuleId = s;
  }


  /**
   * Returns whether results should be in reverse sorted order.
   *
   * @return  whether results should be in reverse sorted order
   */
  public boolean getReverseOrder()
  {
    return reverseOrder;
  }


  /**
   * Sets whether results should be in reverse sorted order.
   *
   * @param  b  whether results should be in reverse sorted order
   */
  public void setReverseOrder(final boolean b)
  {
    reverseOrder = b;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof SortKey) {
      final SortKey v = (SortKey) o;
      return LdapUtils.areEqual(attributeDescription, v.attributeDescription) &&
             LdapUtils.areEqual(matchingRuleId, v.matchingRuleId) &&
             LdapUtils.areEqual(reverseOrder, v.reverseOrder);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, attributeDescription, matchingRuleId, reverseOrder);
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("attributeDescription=").append(attributeDescription).append(", ")
      .append("matchingRuleId=").append(matchingRuleId).append(", ")
      .append("reverseOrder=").append(reverseOrder).append("]").toString();
  }
}
