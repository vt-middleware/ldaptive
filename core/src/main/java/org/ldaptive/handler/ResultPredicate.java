/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.function.Predicate;
import org.ldaptive.LdapException;
import org.ldaptive.Result;
import org.ldaptive.ResultCode;

/**
 * Marker interface for a throw predicate.
 *
 * @author  Middleware Services
 */
@FunctionalInterface
public interface ResultPredicate extends Predicate<Result>
{

  /** Predicates that throws if the result code is not {@link ResultCode#SUCCESS}. */
  ResultPredicate NOT_SUCCESS = result -> !ResultCode.SUCCESS.equals(result.getResultCode());


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
      throw new LdapException(formatResult(result));
    }
  }


  /**
   * Formats the supplied result for use as an exception message.
   *
   * @param  result  to format
   *
   * @return  formatted result
   */
  default String formatResult(final Result result)
  {
    if (result == null) {
      return "Predicate failed for null result";
    }
    return new StringBuilder("resultCode=").append(result.getResultCode()).append(", ")
      .append("diagnosticMessage=").append(result.getEncodedDiagnosticMessage()).toString();
  }
}
