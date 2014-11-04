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
package org.ldaptive.control;

import org.ldaptive.ad.control.DirSyncControl;
import org.ldaptive.ad.control.GetStatsControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for creating controls.
 *
 * @author  Middleware Services
 * @version  $Revision: 3064 $ $Date: 2014-09-16 10:54:06 -0400 (Tue, 16 Sep 2014) $
 */
public final class ControlFactory
{

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(
    ControlFactory.class);


  /** Default constructor. */
  private ControlFactory() {}


  /**
   * Creates a response control from the supplied control data.
   *
   * @param  oid  of the control
   * @param  critical  whether the control is critical
   * @param  encoded  BER encoding of the control
   *
   * @return  response control
   */
  public static ResponseControl createResponseControl(
    final String oid,
    final boolean critical,
    final byte[] encoded)
  {
    ResponseControl ctl = null;
    switch (oid) {
    case SortResponseControl.OID:
      ctl = new SortResponseControl(critical);
      ctl.decode(encoded);
      break;
    case PagedResultsControl.OID:
      ctl = new PagedResultsControl(critical);
      ctl.decode(encoded);
      break;
    case VirtualListViewResponseControl.OID:
      ctl = new VirtualListViewResponseControl(critical);
      ctl.decode(encoded);
      break;
    case PasswordPolicyControl.OID:
      ctl = new PasswordPolicyControl(critical);
      ctl.decode(encoded);
      break;
    case SyncStateControl.OID:
      ctl = new SyncStateControl(critical);
      ctl.decode(encoded);
      break;
    case SyncDoneControl.OID:
      ctl = new SyncDoneControl(critical);
      ctl.decode(encoded);
      break;
    case DirSyncControl.OID:
      ctl = new DirSyncControl(critical);
      ctl.decode(encoded);
      break;
    case EntryChangeNotificationControl.OID:
      ctl = new EntryChangeNotificationControl(critical);
      ctl.decode(encoded);
      break;
    case GetStatsControl.OID:
      ctl = new GetStatsControl(critical);
      ctl.decode(encoded);
      break;
    case PasswordExpiredControl.OID:
      ctl = new PasswordExpiredControl(critical);
      ctl.decode(encoded);
      break;
    case PasswordExpiringControl.OID:
      ctl = new PasswordExpiringControl(critical);
      ctl.decode(encoded);
      break;
    case AuthorizationIdentityResponseControl.OID:
      ctl = new AuthorizationIdentityResponseControl(critical);
      ctl.decode(encoded);
      break;
    default:
      LOGGER.debug("Unsupported response control OID {}", oid);
      break;
    }
    return ctl;
  }
}
