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
package org.ldaptive.jaas;

import java.util.Set;
import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.auth.AbstractSearchOperationFactory;

/**
 * Base class for search role resolver implementations.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public abstract class AbstractSearchRoleResolver
  extends AbstractSearchOperationFactory implements RoleResolver
{


  /** {@inheritDoc} */
  @Override
  public Set<LdapRole> search(final SearchRequest request)
    throws LdapException
  {
    Connection conn = null;
    try {
      conn = getConnection();

      final SearchOperation op = createSearchOperation(conn);
      return LdapRole.toRoles(op.execute(request).getResult());
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }


  /**
   * Retrieve a connection that is ready for use.
   *
   * @return  connection
   *
   * @throws  LdapException  if an error occurs opening the connection
   */
  protected abstract Connection getConnection()
    throws LdapException;
}
