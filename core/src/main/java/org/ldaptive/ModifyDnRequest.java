/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;

/**
 * Contains the data required to perform an ldap modify dn operation.
 *
 * @author  Middleware Services
 */
public class ModifyDnRequest extends AbstractRequest
{

  /** DN to modify. */
  private String oldModifyDn;

  /** New DN. */
  private String newModifyDn;

  /** Whether to delete the old RDN attribute. */
  private boolean deleteOldRDn = true;


  /** Default constructor. */
  public ModifyDnRequest() {}


  /**
   * Creates a new modify dn request.
   *
   * @param  oldDn  to modify
   * @param  newDn  to rename to
   */
  public ModifyDnRequest(final String oldDn, final String newDn)
  {
    setDn(oldDn);
    setNewDn(newDn);
  }


  /**
   * Returns the DN to modify.
   *
   * @return  DN
   */
  public String getDn()
  {
    return oldModifyDn;
  }


  /**
   * Sets the DN to modify.
   *
   * @param  dn  to modify
   */
  public void setDn(final String dn)
  {
    oldModifyDn = dn;
  }


  /**
   * Returns the new DN.
   *
   * @return  DN
   */
  public String getNewDn()
  {
    return newModifyDn;
  }


  /**
   * Sets the new DN.
   *
   * @param  dn  to rename to
   */
  public void setNewDn(final String dn)
  {
    newModifyDn = dn;
  }


  /**
   * Returns whether to delete the old RDN attribute.
   *
   * @return  whether to delete the old RDN attribute
   */
  public boolean getDeleteOldRDn()
  {
    return deleteOldRDn;
  }


  /**
   * Sets whether to delete the old RDN attribute.
   *
   * @param  b  whether to delete the old RDN attribute
   */
  public void setDeleteOldRDn(final boolean b)
  {
    deleteOldRDn = b;
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::oldModifyDn=%s, newModifyDn=%s, deleteOldRDn=%s, controls=%s]",
        getClass().getName(),
        hashCode(),
        oldModifyDn,
        newModifyDn,
        deleteOldRDn,
        Arrays.toString(getControls()));
  }
}
