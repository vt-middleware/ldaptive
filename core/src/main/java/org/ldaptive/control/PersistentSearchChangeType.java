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
package org.ldaptive.control;

/**
 * The set of change types available for use with the {@link
 * PersistentSearchRequestControl} and returned by the {@link
 * EntryChangeNotificationControl}. See
 * http://tools.ietf.org/id/draft-ietf-ldapext-psearch-03.txt.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public enum PersistentSearchChangeType {

  /** add. */
  ADD(1),

  /** delete. */
  DELETE(2),

  /** modify. */
  MODIFY(4),

  /** modify dn. */
  MODDN(8);

  /** underlying value. */
  private final int value;


  /**
   * Creates a new persistent search change type.
   *
   * @param  i  value
   */
  PersistentSearchChangeType(final int i)
  {
    value = i;
  }


  /**
   * Returns the value.
   *
   * @return  enum value
   */
  public int value()
  {
    return value;
  }


  /**
   * Returns the persistent search change type for the supplied integer
   * constant.
   *
   * @param  i  to find change type for
   *
   * @return  persistent search change type
   */
  public static PersistentSearchChangeType valueOf(final int i)
  {
    for (PersistentSearchChangeType ct : PersistentSearchChangeType.values()) {
      if (ct.value() == i) {
        return ct;
      }
    }
    return null;
  }
}
