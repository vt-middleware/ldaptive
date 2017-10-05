/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for ldap controls.
 *
 * @author  Middleware Services
 */
public abstract class AbstractControl implements Control
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** control oid. */
  private final String oid;

  /** is control critical. */
  private final boolean criticality;


  /**
   * Creates a new abstract control.
   *
   * @param  id  OID of this control
   */
  public AbstractControl(final String id)
  {
    oid = id;
    criticality = false;
  }


  /**
   * Creates a new abstract control.
   *
   * @param  id  OID of this control
   * @param  b  whether this control is critical
   */
  public AbstractControl(final String id, final boolean b)
  {
    oid = id;
    criticality = b;
  }


  @Override
  public String getOID()
  {
    return oid;
  }


  @Override
  public boolean getCriticality()
  {
    return criticality;
  }


  // CheckStyle:EqualsHashCode OFF
  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof AbstractControl) {
      final AbstractControl v = (AbstractControl) o;
      return LdapUtils.areEqual(getOID(), v.getOID()) &&
             LdapUtils.areEqual(getCriticality(), v.getCriticality());
    }
    return false;
  }
  // CheckStyle:EqualsHashCode ON


  /**
   * Returns the hash code for this object.
   *
   * @return  hash code
   */
  @Override
  public abstract int hashCode();
}
