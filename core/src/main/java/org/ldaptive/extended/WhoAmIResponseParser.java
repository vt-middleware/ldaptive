/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import org.ldaptive.LdapUtils;

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
    return LdapUtils.utf8Encode(response.getResponseValue(), false);
  }
}
