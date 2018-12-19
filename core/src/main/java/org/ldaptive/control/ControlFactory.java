/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.ad.control.DirSyncControl;
import org.ldaptive.ad.control.GetStatsControl;

/**
 * Utility class for creating controls.
 *
 * @author  Middleware Services
 */
public final class ControlFactory
{


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
  public static ResponseControl createResponseControl(final String oid, final boolean critical, final byte[] encoded)
  {
    final ResponseControl ctl;
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
      ctl = new GenericControl(oid, critical, encoded);
      break;
    }
    return ctl;
  }
}
