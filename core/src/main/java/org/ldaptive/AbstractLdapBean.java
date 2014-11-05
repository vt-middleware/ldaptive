/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.io.Serializable;

/**
 * Provides common implementations for ldap beans.
 *
 * @author  Middleware Services
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
