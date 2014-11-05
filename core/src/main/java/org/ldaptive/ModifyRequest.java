/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;

/**
 * Contains the data required to perform an ldap modify operation.
 *
 * @author  Middleware Services
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
