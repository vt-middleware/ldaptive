/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.BooleanType;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;

/**
 * Response control for ldap content synchronization. See RFC 4533. Control is defined as:
 *
 * <pre>
    syncDoneValue ::= SEQUENCE {
        cookie          syncCookie OPTIONAL,
        refreshDeletes  BOOLEAN DEFAULT FALSE
    }
 * </pre>
 *
 * @author  Middleware Services
 */
public class SyncDoneControl extends AbstractResponseControl
{

  /** OID of this control. */
  public static final String OID = "1.3.6.1.4.1.4203.1.9.1.3";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 757;

  /** server generated cookie. */
  private byte[] cookie;

  /** refresh deletes. */
  private boolean refreshDeletes;


  /** Default constructor. */
  public SyncDoneControl()
  {
    super(OID);
  }


  /**
   * Creates a new sync done control.
   *
   * @param  critical  whether this control is critical
   */
  public SyncDoneControl(final boolean critical)
  {
    super(OID, critical);
  }


  /**
   * Creates a new sync done control.
   *
   * @param  value  sync done cookie
   */
  public SyncDoneControl(final byte[] value)
  {
    super(OID);
    cookie = value;
    freeze();
  }


  /**
   * Creates a new sync done control.
   *
   * @param  value  sync done cookie
   * @param  critical  whether this control is critical
   */
  public SyncDoneControl(final byte[] value, final boolean critical)
  {
    super(OID, critical);
    cookie = value;
    freeze();
  }


  /**
   * Creates a new sync done control.
   *
   * @param  value  sync done cookie
   * @param  refresh  whether to refresh deletes
   * @param  critical  whether this control is critical
   */
  public SyncDoneControl(final byte[] value, final boolean refresh, final boolean critical)
  {
    super(OID, critical);
    cookie = value;
    refreshDeletes = refresh;
    freeze();
  }


  /**
   * Returns the sync done cookie.
   *
   * @return  sync done cookie
   */
  public byte[] getCookie()
  {
    return cookie;
  }


  /**
   * Returns whether to refresh deletes.
   *
   * @return  refresh deletes
   */
  public boolean getRefreshDeletes()
  {
    return refreshDeletes;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof SyncDoneControl && super.equals(o)) {
      final SyncDoneControl v = (SyncDoneControl) o;
      return LdapUtils.areEqual(cookie, v.cookie) &&
             LdapUtils.areEqual(refreshDeletes, v.refreshDeletes);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, getOID(), getCriticality(), cookie, refreshDeletes);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "criticality=" + getCriticality() + ", " +
      "cookie=" + LdapUtils.base64Encode(cookie) + ", " +
      "refreshDeletes=" + refreshDeletes + "]";
  }


  @Override
  public void decode(final DERBuffer encoded)
  {
    freezeAndAssertMutable();
    final DERParser parser = new DERParser();
    parser.registerHandler(CookieHandler.PATH, new CookieHandler(this));
    parser.registerHandler(RefreshDeletesHandler.PATH, new RefreshDeletesHandler(this));
    try {
      parser.parse(encoded);
    } catch (Exception e) {
      throw new IllegalArgumentException("Error parsing response", e);
    }
  }


  /** Parse handler implementation for the cookie. */
  private static class CookieHandler extends AbstractParseHandler<SyncDoneControl>
  {

    /** DER path to cookie value. */
    public static final DERPath PATH = new DERPath("/SEQ/OCTSTR[0]");


    /**
     * Creates a new cookie handler.
     *
     * @param  control  to configure
     */
    CookieHandler(final SyncDoneControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      final byte[] cookie = encoded.getRemainingBytes();
      if (cookie != null && cookie.length > 0) {
        getObject().cookie = cookie;
      }
    }
  }


  /** Parse handler implementation for the refresh deletes flag. */
  private static class RefreshDeletesHandler extends AbstractParseHandler<SyncDoneControl>
  {

    /** DER path to the boolean. */
    public static final DERPath PATH = new DERPath("/SEQ/BOOL[1]");


    /**
     * Creates a new refresh deletes handler.
     *
     * @param  control  to configure
     */
    RefreshDeletesHandler(final SyncDoneControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().refreshDeletes = BooleanType.decode(encoded);
    }
  }
}
