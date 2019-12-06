/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

/**
 * Provides a hook in the DER parser for handling specific paths as they are encountered.
 *
 * @author  Middleware Services
 */
public interface ParseHandler
{


  /**
   * Invoked when a DER path is encountered that belongs to this parse handler.
   *
   * @param  parser  that invoked this handler
   * @param  encoded  to handle
   */
  void handle(DERParser parser, DERBuffer encoded);
}
