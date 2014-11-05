/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.nio.ByteBuffer;

/**
 * Provides a hook in the DER parser for handling specific paths as they are
 * encountered.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface ParseHandler
{


  /**
   * Invoked when a DER path is encountered that belongs to this parse handler.
   *
   * @param  parser  that invoked this handler
   * @param  encoded  to handle
   */
  void handle(DERParser parser, ByteBuffer encoded);
}
