/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control;

import java.math.BigInteger;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.asn1.UniversalDERTag;
import org.ldaptive.control.AbstractResponseControl;
import org.ldaptive.control.RequestControl;

/**
 * Request/response control for active directory synchronization. Control is defined as:
 *
 * <pre>
    dirSyncValue ::= SEQUENCE {
        flags              INTEGER,
        maxAttributeCount  INTEGER,
        cookie             OCTET STRING
    }
 * </pre>
 *
 * <p>See http://msdn.microsoft.com/en-us/library/cc223347.aspx</p>
 *
 * @author  Middleware Services
 */
public class DirSyncControl extends AbstractResponseControl implements RequestControl
{

  /** OID of this control. */
  public static final String OID = "1.2.840.113556.1.4.841";

  /** hash value seed. */
  private static final int HASH_CODE_SEED = 907;

  /** Empty byte array used for null cookies. */
  private static final byte[] EMPTY_COOKIE = new byte[0];

  /** Types of flags. */
  public enum Flag {

    /** object security. */
    OBJECT_SECURITY(1L),

    /** ancestors first order. */
    ANCESTORS_FIRST_ORDER(2048L),

    /** public data only. */
    PUBLIC_DATA_ONLY(8192L),

    /** incremental values. */
    INCREMENTAL_VALUES(2147483648L);

    /** underlying value. */
    private final long value;


    /**
     * Creates a new flag.
     *
     * @param  l  value
     */
    Flag(final long l)
    {
      value = l;
    }


    /**
     * Returns the value.
     *
     * @return  enum value
     */
    public long value()
    {
      return value;
    }


    /**
     * Returns the flag for the supplied integer constant.
     *
     * @param  l  to find flag for
     *
     * @return  flag
     */
    public static Flag valueOf(final long l)
    {
      for (Flag f : values()) {
        if (f.value() == l) {
          return f;
        }
      }
      return null;
    }
  }

  /** flags. */
  private long flags;

  /** maximum attribute count. */
  private int maxAttributeCount;

  /** server generated cookie. */
  private byte[] cookie;


  /** Default constructor. */
  public DirSyncControl()
  {
    super(OID);
  }


  /**
   * Creates a new dir sync control.
   *
   * @param  critical  whether this control is critical
   */
  public DirSyncControl(final boolean critical)
  {
    super(OID, critical);
  }


  /**
   * Creates a new dir sync control.
   *
   * @param  f  request flags
   */
  public DirSyncControl(final Flag[] f)
  {
    this(f, false);
  }


  /**
   * Creates a new dir sync control.
   *
   * @param  f  request flags
   * @param  critical  whether this control is critical
   */
  public DirSyncControl(final Flag[] f, final boolean critical)
  {
    this(f, null, critical);
  }


  /**
   * Creates a new dir sync control.
   *
   * @param  f  request flags
   * @param  count  maximum attribute count
   */
  public DirSyncControl(final Flag[] f, final int count)
  {
    this(f, null, count, false);
  }


  /**
   * Creates a new dir sync control.
   *
   * @param  f  request flags
   * @param  count  maximum attribute count
   * @param  critical  whether this control is critical
   */
  public DirSyncControl(final Flag[] f, final int count, final boolean critical)
  {
    this(f, null, count, critical);
  }


  /**
   * Creates a new dir sync control.
   *
   * @param  f  request flags
   * @param  value  dir sync cookie
   * @param  critical  whether this control is critical
   */
  public DirSyncControl(final Flag[] f, final byte[] value, final boolean critical)
  {
    this(f, value, 0, critical);
  }


  /**
   * Creates a new dir sync control.
   *
   * @param  f  request flags
   * @param  value  dir sync cookie
   * @param  count  maximum attribute count
   * @param  critical  whether this control is critical
   */
  public DirSyncControl(final Flag[] f, final byte[] value, final int count, final boolean critical)
  {
    super(OID, critical);
    if (f != null) {
      long l = 0;
      for (Flag flag : f) {
        if (flag != null) {
          l += flag.value();
        }
      }
      flags = l;
    }
    cookie = value;
    maxAttributeCount = count;
  }


  @Override
  public boolean hasValue()
  {
    return true;
  }


  /**
   * Returns the flags value.
   *
   * @return  flags value
   */
  public long getFlags()
  {
    return flags;
  }


  /**
   * Sets the flags.
   *
   * @param  l  flags value
   */
  public void setFlags(final long l)
  {
    assertMutable();
    flags = l;
  }


  /**
   * Returns the maximum attribute count.
   *
   * @return  maximum attribute count
   */
  public int getMaxAttributeCount()
  {
    return maxAttributeCount;
  }


  /**
   * Sets the maximum attribute count.
   *
   * @param  count  maximum attribute count
   */
  public void setMaxAttributeCount(final int count)
  {
    assertMutable();
    maxAttributeCount = count;
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
    assertMutable();
    cookie = value;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof DirSyncControl && super.equals(o)) {
      final DirSyncControl v = (DirSyncControl) o;
      return LdapUtils.areEqual(flags, v.flags) &&
             LdapUtils.areEqual(maxAttributeCount, v.maxAttributeCount) &&
             LdapUtils.areEqual(cookie, v.cookie);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, getOID(), getCriticality(), flags, maxAttributeCount, cookie);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "criticality=" + getCriticality() + ", " +
      "flags=" + flags + ", " +
      "maxAttributeCount=" + maxAttributeCount + ", " +
      "cookie=" + LdapUtils.base64Encode(cookie) + "]";
  }


  @Override
  public byte[] encode()
  {
    final ConstructedDEREncoder se = new ConstructedDEREncoder(
      UniversalDERTag.SEQ,
      new IntegerType(BigInteger.valueOf(getFlags())),
      new IntegerType(getMaxAttributeCount()),
      new OctetStringType(getCookie() != null ? getCookie() : EMPTY_COOKIE));
    return se.encode();
  }


  @Override
  public void decode(final DERBuffer encoded)
  {
    freezeAndAssertMutable();
    final DERParser parser = new DERParser();
    parser.registerHandler(FlagHandler.PATH, new FlagHandler(this));
    parser.registerHandler(MaxAttrCountHandler.PATH, new MaxAttrCountHandler(this));
    parser.registerHandler(CookieHandler.PATH, new CookieHandler(this));
    try {
      parser.parse(encoded);
    } catch (Exception e) {
      throw new IllegalArgumentException("Error parsing response", e);
    }
  }


  /** Parse handler implementation for the flag. */
  private static class FlagHandler extends AbstractParseHandler<DirSyncControl>
  {

    /** DER path to flag. */
    public static final DERPath PATH = new DERPath("/SEQ/INT[0]");


    /**
     * Creates a new flag handler.
     *
     * @param  control  to configure
     */
    FlagHandler(final DirSyncControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().flags = IntegerType.decode(encoded).longValue();
    }
  }


  /** Parse handler implementation for the maxAttributeCount. */
  private static class MaxAttrCountHandler extends AbstractParseHandler<DirSyncControl>
  {

    /** DER path to cookie value. */
    public static final DERPath PATH = new DERPath("/SEQ/INT[1]");


    /**
     * Creates a new max attr count handler.
     *
     * @param  control  to configure
     */
    MaxAttrCountHandler(final DirSyncControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().maxAttributeCount = IntegerType.decode(encoded).intValue();
    }
  }


  /** Parse handler implementation for the cookie. */
  private static class CookieHandler extends AbstractParseHandler<DirSyncControl>
  {

    /** DER path to cookie value. */
    public static final DERPath PATH = new DERPath("/SEQ/OCTSTR[2]");


    /**
     * Creates a new cookie handler.
     *
     * @param  control  to configure
     */
    CookieHandler(final DirSyncControl control)
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
}
