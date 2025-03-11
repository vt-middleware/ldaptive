/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Clock;

/**
 * Retry metadata used by {@link LdapURL}.
 *
 * @author  Middleware Services
 */
public class LdapURLRetryMetadata extends AbstractRetryMetadata
{

  /** Connection strategy associated with this retry. */
  private final ConnectionStrategy connectionStrategy;


  /**
   * Creates a new LDAP URL retry metadata.
   *
   * @param  clock  to set the create time
   * @param  strategy  connection strategy
   */
  LdapURLRetryMetadata(final Clock clock, final ConnectionStrategy strategy)
  {
    super(clock);
    connectionStrategy = LdapUtils.assertNotNullArg(strategy, "Connection strategy cannot be null");
  }


  /**
   * Creates a new LDAP URL retry metadata.
   *
   * @param  strategy  connection strategy
   */
  public LdapURLRetryMetadata(final ConnectionStrategy strategy)
  {
    this(Clock.systemDefaultZone(), strategy);
  }


  /**
   * Return the connection strategy.
   *
   * @return  connection strategy
   */
  public ConnectionStrategy getConnectionStrategy()
  {
    return connectionStrategy;
  }


  @Override
  public String toString()
  {
    return super.toString() + ", " + "connectionStrategy=" + connectionStrategy;
  }
}
