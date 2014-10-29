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

import java.io.Serializable;

/**
 * Provides common implementations for ldap beans.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public abstract class AbstractLdapBean implements Serializable
{

  /** serial version uid. */
  private static final long serialVersionUID = 4715681585273172940L;

  /** Sort behavior. */
  private final SortBehavior sortBehavior;


  /**
   * Creates a new abstract ldap bean.
   *
   * @param  sb  sort behavior
   */
  public AbstractLdapBean(final SortBehavior sb)
  {
    if (sb == null) {
      throw new IllegalArgumentException("Sort behavior cannot be null");
    }
    sortBehavior = sb;
  }


  /**
   * Returns the sort behavior for this ldap bean.
   *
   * @return  sort behavior
   */
  public SortBehavior getSortBehavior()
  {
    return sortBehavior;
  }


  /** {@inheritDoc} */
  @Override
  public boolean equals(final Object o)
  {
    return LdapUtils.areEqual(this, o);
  }


  /** {@inheritDoc} */
  @Override
  public abstract int hashCode();
}
