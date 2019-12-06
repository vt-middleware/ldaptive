/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

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
   * @param  strategy  connection strategy
   */
  public LdapURLRetryMetadata(final ConnectionStrategy strategy)
  {
    connectionStrategy = strategy;
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
    return new StringBuilder(super.toString()).append(", ")
      .append("connectionStrategy=").append(connectionStrategy).toString();
  }
}
