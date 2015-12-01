/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import java.nio.ByteBuffer;
import org.ldaptive.LdapUtils;
import org.ldaptive.ResultCode;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;

/**
 * Response control for virtual list view. See http://tools.ietf.org/html/draft-ietf-ldapext-ldapv3-vlv-09. Control is
 * defined as:
 *
 * <pre>
   VirtualListViewResponse ::= SEQUENCE {
      targetPosition    INTEGER (0 .. maxInt),
      contentCount      INTEGER (0 .. maxInt),
      virtualListViewResult ENUMERATED {
         success (0),
         operationsError (1),
         protocolError (2),
         unwillingToPerform (53),
         insufficientAccessRights (50),
         timeLimitExceeded (3),
         adminLimitExceeded (11),
         innapropriateMatching (18),
         sortControlMissing (60),
         offsetRangeError (61),
         other(80),
         ... },
      contextID         OCTET STRING OPTIONAL }
 * </pre>
 *
 * @author  Middleware Services
 */
public class VirtualListViewResponseControl extends AbstractControl implements ResponseControl
{

  /** OID of this control. */
  public static final String OID = "2.16.840.1.113730.3.4.10";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 773;

  /** list offset for the target entry. */
  private int targetPosition;

  /** server's estimate of the current number of entries in the ordered search result set. */
  private int contentCount;

  /** Result of the vlv operation. */
  private ResultCode viewResult;

  /**
   * value that clients should send back to the server to indicate that the server is willing to return contiguous data
   * from a subsequent search request which uses the same search criteria.
   */
  private byte[] contextID;


  /** Default constructor. */
  public VirtualListViewResponseControl()
  {
    super(OID);
  }


  /**
   * Creates a new virtual list view response control.
   *
   * @param  critical  whether this control is critical
   */
  public VirtualListViewResponseControl(final boolean critical)
  {
    super(OID, critical);
  }


  /**
   * Creates a new virtual list view response control.
   *
   * @param  position  offset for the target entry
   * @param  count  server estimate of the number of entries
   * @param  code  operation result code
   * @param  context  server context id
   */
  public VirtualListViewResponseControl(
    final int position,
    final int count,
    final ResultCode code,
    final byte[] context)
  {
    this(position, count, code, context, false);
  }


  /**
   * Creates a new virtual list view response control.
   *
   * @param  position  offset for the target entry
   * @param  count  server estimate of the number of entries
   * @param  code  operation result code
   * @param  context  server context id
   * @param  critical  whether this control is critical
   */
  public VirtualListViewResponseControl(
    final int position,
    final int count,
    final ResultCode code,
    final byte[] context,
    final boolean critical)
  {
    super(OID, critical);
    setTargetPosition(position);
    setContentCount(count);
    setViewResult(code);
    setContextID(context);
  }


  /**
   * Returns the target position. This indicates the list offset for the target entry.
   *
   * @return  target position
   */
  public int getTargetPosition()
  {
    return targetPosition;
  }


  /**
   * Sets the target position.
   *
   * @param  position  target position
   */
  public void setTargetPosition(final int position)
  {
    targetPosition = position;
  }


  /**
   * Returns the content count. From the RFC:
   *
   * <p>contentCount gives the server's estimate of the current number of entries in the list. Together these give
   * sufficient information for the client to update a list box slider position to match the newly retrieved entries and
   * identify the target entry. The contentCount value returned SHOULD be used in a subsequent VirtualListViewRequest
   * control.</p>
   *
   * @return  content count
   */
  public int getContentCount()
  {
    return contentCount;
  }


  /**
   * Sets the content count.
   *
   * @param  count  content count
   */
  public void setContentCount(final int count)
  {
    contentCount = count;
  }


  /**
   * Returns the result code of the virtual list view.
   *
   * @return  result code
   */
  public ResultCode getViewResult()
  {
    return viewResult;
  }


  /**
   * Sets the result code of the virtual list view.
   *
   * @param  code  result code
   */
  public void setViewResult(final ResultCode code)
  {
    viewResult = code;
  }


  /**
   * Returns the context id. From the RFC:
   *
   * <p>The contextID is a server-defined octet string. If present, the contents of the contextID field SHOULD be
   * returned to the server by a client in a subsequent virtual list request. The presence of a contextID here indicates
   * that the server is willing to return contiguous data from a subsequent search request which uses the same search
   * criteria, accompanied by a VirtualListViewRequest which indicates that the client wishes to receive an adjoining
   * page of data.</p>
   *
   * @return  context id
   */
  public byte[] getContextID()
  {
    return contextID;
  }


  /**
   * Sets the context id.
   *
   * @param  id  context id
   */
  public void setContextID(final byte[] id)
  {
    contextID = id;
  }


  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        getOID(),
        getCriticality(),
        targetPosition,
        contentCount,
        viewResult,
        contextID);
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::criticality=%s, targetPosition=%s, contentCount=%s, " +
        "viewResult=%s, contextID=%s]",
        getClass().getName(),
        hashCode(),
        getCriticality(),
        targetPosition,
        contentCount,
        viewResult,
        LdapUtils.base64Encode(contextID));
  }


  @Override
  public void decode(final byte[] berValue)
  {
    logger.trace("decoding control: {}", LdapUtils.base64Encode(berValue));

    final DERParser parser = new DERParser();
    parser.registerHandler(TargetPositionHandler.PATH, new TargetPositionHandler(this));
    parser.registerHandler(ContentCountHandler.PATH, new ContentCountHandler(this));
    parser.registerHandler(ViewResultHandler.PATH, new ViewResultHandler(this));
    parser.registerHandler(ContextIDHandler.PATH, new ContextIDHandler(this));
    parser.parse(ByteBuffer.wrap(berValue));
  }


  /** Parse handler implementation for the target position. */
  private static class TargetPositionHandler extends AbstractParseHandler<VirtualListViewResponseControl>
  {

    /** DER path to target position. */
    public static final DERPath PATH = new DERPath("/SEQ/INT[0]");


    /**
     * Creates a new target position handler.
     *
     * @param  control  to configure
     */
    TargetPositionHandler(final VirtualListViewResponseControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      getObject().setTargetPosition(IntegerType.decode(encoded).intValue());
    }
  }


  /** Parse handler implementation for the content count. */
  private static class ContentCountHandler extends AbstractParseHandler<VirtualListViewResponseControl>
  {

    /** DER path to content count. */
    public static final DERPath PATH = new DERPath("/SEQ/INT[1]");


    /**
     * Creates a new content count handler.
     *
     * @param  control  to configure
     */
    ContentCountHandler(final VirtualListViewResponseControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      getObject().setContentCount(IntegerType.decode(encoded).intValue());
    }
  }


  /** Parse handler implementation for the view result. */
  private static class ViewResultHandler extends AbstractParseHandler<VirtualListViewResponseControl>
  {

    /** DER path to result code. */
    public static final DERPath PATH = new DERPath("/SEQ/INT[2]");


    /**
     * Creates a new view result handler.
     *
     * @param  control  to configure
     */
    ViewResultHandler(final VirtualListViewResponseControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      final int resultValue = IntegerType.decode(encoded).intValue();
      final ResultCode rc = ResultCode.valueOf(resultValue);
      if (rc == null) {
        throw new IllegalArgumentException("Unknown result code " + resultValue);
      }
      getObject().setViewResult(rc);
    }
  }


  /** Parse handler implementation for the context ID. */
  private static class ContextIDHandler extends AbstractParseHandler<VirtualListViewResponseControl>
  {

    /** DER path to context value. */
    public static final DERPath PATH = new DERPath("/SEQ/OCTSTR");


    /**
     * Creates a new context ID handler.
     *
     * @param  control  to configure
     */
    ContextIDHandler(final VirtualListViewResponseControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      final byte[] cookie = OctetStringType.readBuffer(encoded);
      if (cookie != null && cookie.length > 0) {
        getObject().setContextID(cookie);
      }
    }
  }
}
