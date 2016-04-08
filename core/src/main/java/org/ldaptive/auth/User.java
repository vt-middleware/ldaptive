/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

/**
 * Encapsulates the data needed to perform authentication for a user.
 *
 * @author  Middleware Services
 */
public class User
{

  /** User identifier. */
  private final String identifier;

  /** User context. */
  private final Object context;


  /**
   * Creates a new user.
   *
   * @param  id  user identifier
   */
  public User(final String id)
  {
    this(id, null);
  }


  /**
   * Creates a new user.
   *
   * @param  id  user identifier
   * @param  ctx  user context
   */
  public User(final String id, final Object ctx)
  {
    identifier = id;
    context = ctx;
  }


  /**
   * Returns the user identifier.
   *
   * @return  user identifier
   */
  public String getIdentifier()
  {
    return identifier;
  }


  /**
   * Returns the user context.
   *
   * @return  user context
   */
  public Object getContext()
  {
    return context;
  }


  @Override
  public String toString()
  {
    return String.format("[%s@%d::identifier=%s, context=%s]", getClass().getName(), hashCode(), identifier, context);
  }
}
