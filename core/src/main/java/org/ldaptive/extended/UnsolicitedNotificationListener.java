/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive.extended;

import org.ldaptive.Response;

/**
 * Processes an unsolicited notification.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface UnsolicitedNotificationListener
{

  /** OID for the notice of disconnection notification. */
  String NOTICE_OF_DISCONNECTION_OID = "1.3.6.1.4.1.1466.20036";


  /**
   * Processes an unsolicited notification from the server.
   *
   * @param  oid  of the unsolicited notification
   * @param  response  server response
   */
  void notificationReceived(String oid, Response<Void> response);
}
