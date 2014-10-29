/*
  $Id: FastBindOperation.java 2885 2014-02-05 21:28:49Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2885 $
  Updated: $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
*/
package org.ldaptive.ad.extended;

import org.ldaptive.AbstractOperation;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionInitializer;
import org.ldaptive.LdapException;
import org.ldaptive.Response;

/**
 * Executes an active directory fast bind operation. See
 * http://msdn.microsoft.com/en-us/library/cc223503(v=prot.20).aspx.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class FastBindOperation extends AbstractOperation<FastBindRequest, Void>
{


  /**
   * Creates a new fast bind operation.
   *
   * @param  conn  connection
   */
  public FastBindOperation(final Connection conn)
  {
    super(conn);
  }


  /** {@inheritDoc} */
  @Override
  protected Response<Void> invoke(final FastBindRequest request)
    throws LdapException
  {
    @SuppressWarnings("unchecked") final Response<Void> response =
      (Response<Void>)
        getConnection().getProviderConnection().extendedOperation(request);
    return response;
  }


  /** Connection initializer that executes the {@link FastBindOperation}. */
  public static class FastBindConnectionInitializer
    implements ConnectionInitializer
  {


    /** {@inheritDoc} */
    @Override
    public Response<Void> initialize(final Connection c)
      throws LdapException
    {
      final FastBindOperation op = new FastBindOperation(c);
      op.setOperationExceptionHandler(null);
      return op.execute(new FastBindRequest());
    }
  }
}
