/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.unboundid;

import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import org.ldaptive.ConnectionConfig;

/**
 * UnboundID provider implementation that uses synchronous options. Attempting to use connections by this provider in an
 * asynchronous manner with throw exceptions.
 *
 * @author  Middleware Services
 */
public class UnboundIDSyncProvider extends UnboundIDProvider
{


  @Override
  protected LDAPConnectionOptions getDefaultLDAPConnectionOptions(final ConnectionConfig cc)
  {
    final LDAPConnectionOptions options = super.getDefaultLDAPConnectionOptions(cc);
    options.setUseSynchronousMode(true);
    return options;
  }
}
