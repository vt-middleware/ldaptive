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
package org.ldaptive;

import java.util.Arrays;

/**
 * Contains the data required to perform an ldap modify operation.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class ModifyRequest extends AbstractRequest
{

  /** DN to modify. */
  private String modifyDn;

  /** Attribute modifications. */
  private AttributeModification[] attrMods;


  /** Default constructor. */
  public ModifyRequest() {}


  /**
   * Creates a new modify request.
   *
   * @param  dn  to modify
   * @param  mods  attribute modifications
   */
  public ModifyRequest(final String dn, final AttributeModification... mods)
  {
    setDn(dn);
    setAttributeModifications(mods);
  }


  /**
   * Returns the DN to modify.
   *
   * @return  DN
   */
  public String getDn()
  {
    return modifyDn;
  }


  /**
   * Sets the DN to modify.
   *
   * @param  dn  to modify
   */
  public void setDn(final String dn)
  {
    modifyDn = dn;
  }


  /**
   * Returns the attribute modifications.
   *
   * @return  attribute modifications
   */
  public AttributeModification[] getAttributeModifications()
  {
    return attrMods;
  }


  /**
   * Sets the attribute modifications.
   *
   * @param  mods  attribute modifications
   */
  public void setAttributeModifications(final AttributeModification... mods)
  {
    attrMods = mods;
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::modifyDn=%s, attrMods=%s, controls=%s]",
        getClass().getName(),
        hashCode(),
        modifyDn,
        Arrays.toString(attrMods),
        Arrays.toString(getControls()));
  }
}
