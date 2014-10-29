/*
  $Id: ApacheLdapControlHandler.java 3029 2014-07-02 21:00:42Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3029 $
  Updated: $Date: 2014-07-02 17:00:42 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive.provider.apache;

import java.nio.ByteBuffer;
import org.apache.directory.api.ldap.extras.controls.SynchronizationModeEnum;
import org.apache.directory.api.ldap.extras.controls.ad.AdDirSync;
import org.apache.directory.api.ldap.extras.controls.ad.AdDirSyncFlag;
import org.apache.directory.api.ldap.extras.controls.ad.AdDirSyncImpl;
import org.apache.directory.api.ldap.extras.controls.ppolicy.PasswordPolicy;
import org.apache.directory.api.ldap.extras.controls.ppolicy.PasswordPolicyErrorEnum;
import org.apache.directory.api.ldap.extras.controls.ppolicy.PasswordPolicyImpl;
import org.apache.directory.api.ldap.extras.controls.ppolicy.PasswordPolicyResponse;
import org.apache.directory.api.ldap.extras.controls.syncrepl.syncDone.SyncDoneValue;
import org.apache.directory.api.ldap.extras.controls.syncrepl.syncInfoValue.SyncRequestValueImpl;
import org.apache.directory.api.ldap.extras.controls.syncrepl.syncState.SyncStateValue;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.controls.ChangeType;
import org.apache.directory.api.ldap.model.message.controls.EntryChange;
import org.apache.directory.api.ldap.model.message.controls.ManageDsaITImpl;
import org.apache.directory.api.ldap.model.message.controls.PagedResults;
import org.apache.directory.api.ldap.model.message.controls.PagedResultsImpl;
import org.apache.directory.api.ldap.model.message.controls.PersistentSearchImpl;
import org.ldaptive.ad.control.DirSyncControl;
import org.ldaptive.asn1.UuidType;
import org.ldaptive.control.EntryChangeNotificationControl;
import org.ldaptive.control.ManageDsaITControl;
import org.ldaptive.control.PagedResultsControl;
import org.ldaptive.control.PasswordPolicyControl;
import org.ldaptive.control.PersistentSearchChangeType;
import org.ldaptive.control.PersistentSearchRequestControl;
import org.ldaptive.control.SyncDoneControl;
import org.ldaptive.control.SyncRequestControl;
import org.ldaptive.control.SyncStateControl;
import org.ldaptive.provider.ControlHandler;

/**
 * Apache Ldap control handler.
 *
 * @author  Middleware Services
 * @version  $Revision: 3029 $ $Date: 2014-07-02 17:00:42 -0400 (Wed, 02 Jul 2014) $
 */
public class ApacheLdapControlHandler implements ControlHandler<Control>
{


  /** {@inheritDoc} */
  @Override
  public String getOID(final Control control)
  {
    return control.getOid();
  }


  /** {@inheritDoc} */
  @Override
  public Control handleRequest(
    final org.ldaptive.control.RequestControl requestControl)
  {
    Control ctl = null;
    if (ManageDsaITControl.OID.equals(requestControl.getOID())) {
      ctl = new ManageDsaITImpl(requestControl.getCriticality());
    } else if (PagedResultsControl.OID.equals(requestControl.getOID())) {
      final PagedResultsControl c = (PagedResultsControl) requestControl;
      ctl = new PagedResultsImpl();
      ((PagedResultsImpl) ctl).setSize(c.getSize());
      ((PagedResultsImpl) ctl).setCookie(c.getCookie());
      ctl.setCritical(c.getCriticality());
    } else if (PasswordPolicyControl.OID.equals(requestControl.getOID())) {
      final PasswordPolicyControl c = (PasswordPolicyControl) requestControl;
      ctl = new PasswordPolicyImpl();
      ctl.setCritical(c.getCriticality());
    } else if (SyncRequestControl.OID.equals(requestControl.getOID())) {
      final SyncRequestControl c = (SyncRequestControl) requestControl;
      ctl = new SyncRequestValueImpl();
      ((SyncRequestValueImpl) ctl).setCookie(c.getCookie());
      ((SyncRequestValueImpl) ctl).setReloadHint(c.getReloadHint());
      ((SyncRequestValueImpl) ctl).setMode(
        SynchronizationModeEnum.getSyncMode(c.getRequestMode().value()));
      ctl.setCritical(c.getCriticality());
    } else if (PersistentSearchRequestControl.OID.equals(
               requestControl.getOID())) {
      final PersistentSearchRequestControl c =
        (PersistentSearchRequestControl) requestControl;
      ctl = new PersistentSearchImpl();
      for (PersistentSearchChangeType type : c.getChangeTypes()) {
        ((PersistentSearchImpl) ctl).enableNotification(
          ChangeType.getChangeType(type.value()));
      }
      ((PersistentSearchImpl) ctl).setChangesOnly(c.getChangesOnly());
      ((PersistentSearchImpl) ctl).setReturnECs(c.getReturnEcs());
      ctl.setCritical(c.getCriticality());
    } else if (DirSyncControl.OID.equals(requestControl.getOID())) {
      final DirSyncControl c = (DirSyncControl) requestControl;
      ctl = new AdDirSyncImpl();
      ((AdDirSyncImpl) ctl).setCookie(c.getCookie());
      ((AdDirSyncImpl) ctl).setFlag(AdDirSyncFlag.getFlag((int) c.getFlags()));
      ((AdDirSyncImpl) ctl).setMaxReturnLength(c.getMaxAttributeCount());
      ctl.setCritical(c.getCriticality());
    }
    return ctl;
  }


  /** {@inheritDoc} */
  @Override
  public org.ldaptive.control.ResponseControl handleResponse(
    final Control responseControl)
  {
    org.ldaptive.control.ResponseControl ctl = null;
    if (PagedResultsControl.OID.equals(responseControl.getOid())) {
      final PagedResults c = (PagedResults) responseControl;
      ctl = new PagedResultsControl(c.getSize(), c.getCookie(), c.isCritical());
    } else if (PasswordPolicyControl.OID.equals(responseControl.getOid())) {
      final PasswordPolicy c = (PasswordPolicy) responseControl;
      if (c.hasResponse()) {
        ctl = new PasswordPolicyControl(c.isCritical());

        final PasswordPolicyResponse ppr = c.getResponse();
        ((PasswordPolicyControl) ctl).setTimeBeforeExpiration(
          ppr.getTimeBeforeExpiration());
        ((PasswordPolicyControl) ctl).setGraceAuthNsRemaining(
          ppr.getGraceAuthNRemaining());

        final PasswordPolicyErrorEnum error = ppr.getPasswordPolicyError();
        if (error != null) {
          ((PasswordPolicyControl) ctl).setError(
            PasswordPolicyControl.Error.valueOf(error.getValue()));
        }
      }
    } else if (SyncStateControl.OID.equals(responseControl.getOid())) {
      final SyncStateValue c = (SyncStateValue) responseControl;
      ctl = new SyncStateControl(
        SyncStateControl.State.valueOf(c.getSyncStateType().getValue()),
        UuidType.decode(ByteBuffer.wrap(c.getEntryUUID())),
        c.getCookie(),
        c.isCritical());
    } else if (SyncDoneControl.OID.equals(responseControl.getOid())) {
      final SyncDoneValue c = (SyncDoneValue) responseControl;
      ctl = new SyncDoneControl(
        c.getCookie(),
        c.isRefreshDeletes(),
        c.isCritical());
    } else if (EntryChangeNotificationControl.OID.equals(
               responseControl.getOid())) {
      final EntryChange c = (EntryChange) responseControl;
      ctl = new EntryChangeNotificationControl(
        PersistentSearchChangeType.valueOf(c.getChangeType().getValue()),
        c.getPreviousDn().toString(),
        c.getChangeNumber(),
        c.isCritical());
    } else if (DirSyncControl.OID.equals(responseControl.getOid())) {
      final AdDirSync c = (AdDirSync) responseControl;
      ctl = new DirSyncControl(
        new DirSyncControl.Flag[] {
          DirSyncControl.Flag.valueOf(c.getFlag().getValue()),
        },
        c.getCookie(),
        c.getMaxReturnLength(),
        c.isCritical());
    }
    return ctl;
  }
}
