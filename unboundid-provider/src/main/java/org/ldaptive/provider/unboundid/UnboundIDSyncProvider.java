/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.unboundid;

import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import org.ldaptive.ConnectionConfig;

/**
 * UnboundID provider implementation that uses synchronous options. Attempting
 * to use connections by this provider in an asynchronous manner with throw
 * exceptions.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class UnboundIDSyncProvider extends UnboundIDProvider
{


  /** {@inheritDoc} */
  @Override
  protected LDAPConnectionOptions getDefaultLDAPConnectionOptions(
    final ConnectionConfig cc)
  {
    final LDAPConnectionOptions options = super.getDefaultLDAPConnectionOptions(
      cc);
    options.setUseSynchronousMode(true);
    return options;
  }
}
