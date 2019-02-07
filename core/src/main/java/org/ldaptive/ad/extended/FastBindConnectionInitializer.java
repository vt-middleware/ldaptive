/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.extended;

import org.ldaptive.Connection;
import org.ldaptive.ConnectionInitializer;
import org.ldaptive.LdapException;
import org.ldaptive.Result;

/**
 * Initializes a connection by performing a fast bind operation.
 *
 * @author  Middleware Services
 */
public class FastBindConnectionInitializer implements ConnectionInitializer
{


  @Override
  public Result initialize(final Connection c)
    throws LdapException
  {
    return c.operation(new FastBindRequest()).execute();
  }
}
