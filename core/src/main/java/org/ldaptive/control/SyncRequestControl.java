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

import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.BooleanType;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.asn1.UniversalDERTag;

/**
 * Request control for ldap content synchronization. See RFC 4533. Control is
 * defined as:
 *
 * <pre>
    syncRequestValue ::= SEQUENCE {
        mode ENUMERATED {
            -- 0 unused
            refreshOnly       (1),
            -- 2 reserved
            refreshAndPersist (3)
        },
        cookie     syncCookie OPTIONAL,
        reloadHint BOOLEAN DEFAULT FALSE
    }
 * </pre>
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class SyncRequestControl extends AbstractControl
  implements RequestControl
{

  /** OID of this control. */
  public static final String OID = "1.3.6.1.4.1.4203.1.9.1.1";

  /** hash value seed. */
  private static final int HASH_CODE_SEED = 743;

  /** Types of request modes. */
  public enum Mode {

    /** refresh only. */
    REFRESH_ONLY(1),

    /** refresh and persist. */
    REFRESH_AND_PERSIST(3);

    /** underlying value. */
    private final int value;


    /**
     * Creates a new mode.
     *
     * @param  i  value
     */
    Mode(final int i)
    {
      value = i;
    }


    /**
     * Returns the value.
     *
     * @return  enum value
     */
    public int value()
    {
      return value;
    }


    /**
     * Returns the mode for the supplied integer constant.
     *
     * @param  i  to find mode for
     *
     * @return  mode
     */
    public static Mode valueOf(final int i)
    {
      for (Mode m : Mode.values()) {
        if (m.value() == i) {
          return m;
        }
      }
      return null;
    }
  }

  /** request mode. */
  private Mode requestMode = Mode.REFRESH_ONLY;

  /** server generated cookie. */
  private byte[] cookie;

  /** reload hint. */
  private boolean reloadHint;


  /** Default constructor. */
  public SyncRequestControl()
  {
    super(OID);
  }


  /**
   * Creates a new sync request control.
   *
   * @param  mode  request mode
   */
  public SyncRequestControl(final Mode mode)
  {
    super(OID);
    setRequestMode(mode);
  }


  /**
   * Creates a new sync request control.
   *
   * @param  mode  request mode
   * @param  critical  whether this control is critical
   */
  public SyncRequestControl(final Mode mode, final boolean critical)
  {
    super(OID, critical);
    setRequestMode(mode);
  }


  /**
   * Creates a new sync request control.
   *
   * @param  mode  request mode
   * @param  value  sync request cookie
   * @param  critical  whether this control is critical
   */
  public SyncRequestControl(
    final Mode mode,
    final byte[] value,
    final boolean critical)
  {
    super(OID, critical);
    setRequestMode(mode);
    setCookie(value);
  }


  /**
   * Creates a new sync request control.
   *
   * @param  mode  request mode
   * @param  value  sync request cookie
   * @param  hint  reload hint
   * @param  critical  whether this control is critical
   */
  public SyncRequestControl(
    final Mode mode,
    final byte[] value,
    final boolean hint,
    final boolean critical)
  {
    super(OID, critical);
    setRequestMode(mode);
    setCookie(value);
    setReloadHint(hint);
  }


  /**
   * Returns the request mode.
   *
   * @return  request mode
   */
  public Mode getRequestMode()
  {
    return requestMode;
  }


  /**
   * Sets the request mode.
   *
   * @param  mode  request mode
   */
  public void setRequestMode(final Mode mode)
  {
    requestMode = mode;
  }


  /**
   * Returns the sync request cookie.
   *
   * @return  sync request cookie
   */
  public byte[] getCookie()
  {
    return cookie;
  }


  /**
   * Sets the sync request cookie.
   *
   * @param  value  sync request cookie
   */
  public void setCookie(final byte[] value)
  {
    cookie = value;
  }


  /**
   * Returns the reload hint.
   *
   * @return  reload hint
   */
  public boolean getReloadHint()
  {
    return reloadHint;
  }


  /**
   * Sets the reload hint.
   *
   * @param  b  reload hint
   */
  public void setReloadHint(final boolean b)
  {
    reloadHint = b;
  }


  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        getOID(),
        getCriticality(),
        requestMode,
        cookie,
        reloadHint);
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::criticality=%s, requestMode=%s, cookie=%s, reloadHint=%s]",
        getClass().getName(),
        hashCode(),
        getCriticality(),
        requestMode,
        LdapUtils.base64Encode(cookie),
        reloadHint);
  }


  /** {@inheritDoc} */
  @Override
  public byte[] encode()
  {
    ConstructedDEREncoder se;
    if (getCookie() != null) {
      se = new ConstructedDEREncoder(
        UniversalDERTag.SEQ,
        new IntegerType(getRequestMode().value()),
        new OctetStringType(getCookie()),
        new BooleanType(getReloadHint()));
    } else {
      se = new ConstructedDEREncoder(
        UniversalDERTag.SEQ,
        new IntegerType(getRequestMode().value()),
        new BooleanType(getReloadHint()));
    }
    return se.encode();
  }
}
