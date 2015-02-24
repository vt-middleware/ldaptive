/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;

/**
 * Contains the data required to perform an ldap compare operation.
 *
 * @author  Middleware Services
 */
public class CompareRequest extends AbstractRequest
{

  /** DN to compare. */
  private String compareDn = "";

  /** Attribute to compare. */
  private LdapAttribute attribute;


  /** Default constructor. */
  public CompareRequest() {}


  /**
   * Creates a new compare request.
   *
   * @param  dn  containing the attribute to compare
   * @param  attr  attribute to compare
   */
  public CompareRequest(final String dn, final LdapAttribute attr)
  {
    setDn(dn);
    setAttribute(attr);
  }


  /**
   * Returns the DN to compare.
   *
   * @return  DN
   */
  public String getDn()
  {
    return compareDn;
  }


  /**
   * Sets the DN to compare.
   *
   * @param  dn  to compare
   */
  public void setDn(final String dn)
  {
    compareDn = dn;
  }


  /**
   * Returns the attribute containing the value to compare. If this attribute
   * contains multiple values, only the first value as return by the underlying
   * collection is used.
   *
   * @return  attribute to compare
   */
  public LdapAttribute getAttribute()
  {
    return attribute;
  }


  /**
   * Sets the attribute to compare.
   *
   * @param  attr  attribute to compare
   */
  public void setAttribute(final LdapAttribute attr)
  {
    attribute = attr;
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::compareDn=%s, attribute=%s, controls=%s, " +
        "referralHandler=%s, intermediateResponseHandlers=%s]",
        getClass().getName(),
        hashCode(),
        compareDn,
        attribute,
        Arrays.toString(getControls()),
        getReferralHandler(),
        Arrays.toString(getIntermediateResponseHandlers()));
  }
}
