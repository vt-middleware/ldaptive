/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import java.nio.ByteBuffer;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.asn1.UniversalDERTag;

/**
 * Request/response control for PagedResults. See RFC 2696. Control is defined as:
 *
 * <pre>
   realSearchControlValue ::= SEQUENCE {
     size            INTEGER (0..maxInt),
                             -- requested page size from client
                             -- result set size estimate from server
     cookie          OCTET STRING
   }
 * </pre>
 *
 * @author  Middleware Services
 */
public class PagedResultsControl extends AbstractControl implements RequestControl, ResponseControl
{

  /** OID of this control. */
  public static final String OID = "1.2.840.113556.1.4.319";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 709;

  /** Empty byte array used for null cookies. */
  private static final byte[] EMPTY_COOKIE = new byte[0];

  /** paged results size. */
  private int resultSize;

  /** server generated cookie. */
  private byte[] cookie;


  /** Default constructor. */
  public PagedResultsControl()
  {
    super(OID);
  }


  /**
   * Creates a new paged results control.
   *
   * @param  critical  whether this control is critical
   */
  public PagedResultsControl(final boolean critical)
  {
    super(OID, critical);
  }


  /**
   * Creates a new paged results control.
   *
   * @param  size  paged results size
   */
  public PagedResultsControl(final int size)
  {
    super(OID);
    setSize(size);
  }


  /**
   * Creates a new paged results control.
   *
   * @param  size  paged results size
   * @param  critical  whether this control is critical
   */
  public PagedResultsControl(final int size, final boolean critical)
  {
    super(OID, critical);
    setSize(size);
  }


  /**
   * Creates a new paged results control.
   *
   * @param  size  paged results size
   * @param  value  paged results cookie
   * @param  critical  whether this control is critical
   */
  public PagedResultsControl(final int size, final byte[] value, final boolean critical)
  {
    super(OID, critical);
    setSize(size);
    setCookie(value);
  }


  /**
   * Returns the paged results size. For requests this is the requested page size. For responses this is the result size
   * estimate from the server.
   *
   * @return  paged results size
   */
  public int getSize()
  {
    return resultSize;
  }


  /**
   * Sets the paged results size. For requests this is the requested page size. For responses this is the result size
   * estimate from the server.
   *
   * @param  size  paged results size
   */
  public void setSize(final int size)
  {
    resultSize = size;
  }


  /**
   * Returns the paged results cookie.
   *
   * @return  paged results cookie
   */
  public byte[] getCookie()
  {
    return cookie;
  }


  /**
   * Sets the paged results cookie.
   *
   * @param  value  paged results cookie
   */
  public void setCookie(final byte[] value)
  {
    cookie = value;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, getOID(), getCriticality(), resultSize, cookie);
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::criticality=%s, size=%s, cookie=%s]",
        getClass().getName(),
        hashCode(),
        getCriticality(),
        resultSize,
        LdapUtils.base64Encode(cookie));
  }


  @Override
  public byte[] encode()
  {
    final ConstructedDEREncoder se = new ConstructedDEREncoder(
      UniversalDERTag.SEQ,
      new IntegerType(getSize()),
      new OctetStringType(getCookie() != null ? getCookie() : EMPTY_COOKIE));
    return se.encode();
  }


  @Override
  public void decode(final byte[] berValue)
  {
    logger.trace("decoding control: {}", LdapUtils.base64Encode(berValue));

    final DERParser parser = new DERParser();
    parser.registerHandler(SizeHandler.PATH, new SizeHandler(this));
    parser.registerHandler(CookieHandler.PATH, new CookieHandler(this));
    parser.parse(ByteBuffer.wrap(berValue));
  }


  /** Parse handler implementation for the size. */
  private static class SizeHandler extends AbstractParseHandler<PagedResultsControl>
  {

    /** DER path to result size. */
    public static final DERPath PATH = new DERPath("/SEQ/INT");


    /**
     * Creates a new size handler.
     *
     * @param  control  to configure
     */
    public SizeHandler(final PagedResultsControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      getObject().setSize(IntegerType.decode(encoded).intValue());
    }
  }


  /** Parse handler implementation for the cookie. */
  private static class CookieHandler extends AbstractParseHandler<PagedResultsControl>
  {

    /** DER path to cookie value. */
    public static final DERPath PATH = new DERPath("/SEQ/OCTSTR");


    /**
     * Creates a new cookie handler.
     *
     * @param  control  to configure
     */
    public CookieHandler(final PagedResultsControl control)
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
