/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.asn1.UniversalDERTag;
import org.ldaptive.control.AbstractControl;
import org.ldaptive.control.RequestControl;
import org.ldaptive.control.ResponseControl;

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
public class DirSyncControl extends AbstractControl implements RequestControl, ResponseControl
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
      for (Flag f : Flag.values()) {
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
    this(null, null, critical);
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
      setFlags(l);
    }
    setCookie(value);
    setMaxAttributeCount(count);
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
    cookie = value;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, getOID(), getCriticality(), flags, maxAttributeCount, cookie);
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::criticality=%s, flags=%s, maxAttributeCount=%s, cookie=%s]",
        getClass().getName(),
        hashCode(),
        getCriticality(),
        flags,
        maxAttributeCount,
        LdapUtils.base64Encode(cookie));
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
  public void decode(final byte[] berValue)
  {
    logger.trace("decoding control: {}", LdapUtils.base64Encode(berValue));

    final DERParser parser = new DERParser();
    parser.registerHandler(FlagHandler.PATH, new FlagHandler(this));
    parser.registerHandler(MaxAttrCountHandler.PATH, new MaxAttrCountHandler(this));
    parser.registerHandler(CookieHandler.PATH, new CookieHandler(this));
    parser.parse(ByteBuffer.wrap(berValue));
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
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      getObject().setFlags(IntegerType.decode(encoded).longValue());
    }
  }


  /** Parse handler implementation for the maxAttributeCount. */
  private static class MaxAttrCountHandler extends AbstractParseHandler<DirSyncControl>
  {

    /** DER path to cookie value. */
    public static final DERPath PATH = new DERPath("/SEQ/INT[1]");


    /**
     * Creates a new max attr handler handler.
     *
     * @param  control  to configure
     */
    MaxAttrCountHandler(final DirSyncControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      getObject().setMaxAttributeCount(IntegerType.decode(encoded).intValue());
    }
  }


  /** Parse handler implementation for the cookie. */
  private static class CookieHandler extends AbstractParseHandler<DirSyncControl>
  {

    /** DER path to cookie value. */
    public static final DERPath PATH = new DERPath("/SEQ/OCTSTR");


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
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      final byte[] cookie = OctetStringType.readBuffer(encoded);
      if (cookie != null && cookie.length > 0) {
        getObject().setCookie(cookie);
      }
    }
  }
}
