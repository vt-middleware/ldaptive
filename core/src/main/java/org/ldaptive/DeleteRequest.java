/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;

/**
 * Contains the data required to perform an ldap delete operation.
 *
 * @author  Middleware Services
 */
public class DeleteRequest extends AbstractRequest
{

  /** DN to delete. */
  private String deleteDn;


  /** Default constructor. */
  public DeleteRequest() {}


  /**
   * Creates a new delete request.
   *
   * @param  dn  to delete
   */
  public DeleteRequest(final String dn)
  {
    setDn(dn);
  }


  /**
   * Returns the DN to delete.
   *
   * @return  DN
   */
  public String getDn()
  {
    return deleteDn;
  }


  /**
   * Sets the DN to delete.
   *
   * @param  dn  to delete
   */
  public void setDn(final String dn)
  {
    deleteDn = dn;
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::deleteDn=%s, controls=%s]",
        getClass().getName(),
        hashCode(),
        deleteDn,
        Arrays.toString(getControls()));
  }
}
