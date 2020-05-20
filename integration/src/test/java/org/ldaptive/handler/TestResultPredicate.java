/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.Result;

/**
 * Predicate for testing.
 *
 * @author  Middleware Services
 */
public class TestResultPredicate implements ResultPredicate
{


  @Override
  public boolean test(final Result result)
  {
    return !result.isSuccess();
  }
}
