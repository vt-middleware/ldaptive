/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.Arrays;
import java.util.List;
import org.ldaptive.Result;
import org.ldaptive.ResultCode;

/**
 * Predicate that tests whether a result code is in a defined list of codes.
 *
 * @author  Middleware Services
 */
public class ContainsResultPredicate implements ResultPredicate
{

  /** Result codes to matches. */
  private final List<ResultCode> resultCodes;


  /**
   * Creates a new contains result predicate.
   *
   * @param  codes  to match
   */
  public ContainsResultPredicate(final ResultCode... codes)
  {
    resultCodes = Arrays.asList(codes);
  }


  @Override
  public boolean test(final Result result)
  {
    return resultCodes.contains(result);
  }
}
