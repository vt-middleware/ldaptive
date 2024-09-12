/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

/**
 * Base class for search result handlers.
 *
 * @author  Middleware Services
 */
public abstract class AbstractSearchResultHandler implements SearchResultHandler
{

  /** Handler usage. */
  private final Usage usage;


  /**
   * Creates a new abstract search result handler.
   *
   * @param  u  handler usage
   */
  public AbstractSearchResultHandler(final Usage u)
  {
    usage = u;
  }


  @Override
  public Usage getUsage()
  {
    return usage;
  }


  @Override
  public String toString()
  {
    return getClass().getName() + "@" + hashCode() + "::" + "usage=" + usage;
  }
}
