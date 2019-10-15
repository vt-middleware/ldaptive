/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.function.Predicate;
import org.ldaptive.LdapException;
import org.ldaptive.Result;

/**
 * Marker interface for a throw predicate.
 *
 * @author  Middleware Services
 */
@FunctionalInterface
public interface ResultPredicate extends Predicate<Result>
{


  /**
   * Test a result and throw if the test succeeds.
   *
   * @param  result  input argument
   *
   * @throws  LdapException  if {@link #test(Object)} returns true
   */
  default void testAndThrow(final Result result)
    throws LdapException
  {
    if (test(result)) {
      throw new LdapException("Predicate failed for result: " + result);
    }
  }
}
