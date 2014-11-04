/*
  $Id: NoOpEntryHandler.java 2468 2012-08-07 18:54:52Z dfisher $

  Copyright (C) 2003-2010 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2468 $
  Updated: $Date: 2012-08-07 14:54:52 -0400 (Tue, 07 Aug 2012) $
*/
package org.ldaptive.handler;

import org.ldaptive.LdapUtils;

/**
 * Entry handler that does nothing.
 *
 * @author  Middleware Services
 * @version  $Revision: 2468 $ $Date: 2012-08-07 14:54:52 -0400 (Tue, 07 Aug 2012) $
 */
public class NoOpEntryHandler extends AbstractSearchEntryHandler
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 887;


  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, (Object) null);
  }
}
