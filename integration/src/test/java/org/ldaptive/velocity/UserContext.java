/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.velocity;

/**
 * Sample context for velocity testing.
 *
 * @author  Middleware Services
 */
public class UserContext
{

  /** principal name. */
  private final String principal;


  /**
   * Creates a new user context.
   *
   * @param  username  that is the principal
   */
  public UserContext(final String username)
  {
    principal = username;
  }


  /**
   * Returns the principal.
   *
   * @return  principal
   */
  public String getPrincipal()
  {
    return principal;
  }
}
