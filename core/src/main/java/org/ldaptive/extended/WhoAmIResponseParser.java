/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import java.nio.charset.StandardCharsets;

/**
 * Utility class for parsing the responseValue from a whoami extended operation.
 *
 * @author  Middleware Services
 */
public final class WhoAmIResponseParser
{


  /**
   * Default constructor.
   */
  private WhoAmIResponseParser() {}


  /**
   * Parse the supplied extended operation response.
   *
   * @param  response  from a password modify extended operation
   *
   * @return  generated password
   */
  public static String parse(final ExtendedResponse response)
  {
    return new String(response.getResponseValue(), StandardCharsets.UTF_8);
  }
}
