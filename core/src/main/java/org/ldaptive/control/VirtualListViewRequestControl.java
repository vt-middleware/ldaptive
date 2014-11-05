/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import java.util.ArrayList;
import java.util.List;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.ContextDERTag;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.asn1.UniversalDERTag;

/**
 * Request control for virtual list view. See
 * http://tools.ietf.org/html/draft-ietf-ldapext-ldapv3-vlv-09. Control is
 * defined as:
 *
 * <pre>
   VirtualListViewRequest ::= SEQUENCE {
      beforeCount    INTEGER (0..maxInt),
      afterCount     INTEGER (0..maxInt),
      target         CHOICE {
         byOffset           [0] SEQUENCE {
            offset             INTEGER (1 .. maxInt),
            contentCount       INTEGER (0 .. maxInt) },
         greaterThanOrEqual [1] AssertionValue },
      contextID      OCTET STRING OPTIONAL }
 * </pre>
 *
 * @author  Middleware Services
 */
public class VirtualListViewRequestControl extends AbstractControl
  implements RequestControl
{

  /** OID of this control. */
  public static final String OID = "2.16.840.1.113730.3.4.9";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 761;

  /** number of entries before the target entry the server should send. */
  private int beforeCount;

  /** number of entries after the target entry the server should send. */
  private int afterCount;

  /** target entry's offset within the ordered search result set. */
  private int targetOffset;

  /**
   * server's estimate of the current number of entries in the ordered search
   * result set.
   */
  private int contentCount;

  /**
   * value to match against the ordering matching rule for the
   * attributeDescription in the sort control.
   */
  private String assertionValue;

  /**
   * value that clients should send back to the server to indicate that the
   * server is willing to return contiguous data from a subsequent search
   * request which uses the same search criteria.
   */
  private byte[] contextID;


  /** Default constructor. */
  public VirtualListViewRequestControl()
  {
    super(OID);
  }


  /**
   * Creates a new virtual list view request control.
   *
   * @param  offset  target entry offset
   * @param  before  number of entries before the target
   * @param  after  number of entries after the target
   */
  public VirtualListViewRequestControl(
    final int offset,
    final int before,
    final int after)
  {
    this(offset, before, after, 0, null, false);
  }


  /**
   * Creates a new virtual list view request control.
   *
   * @param  offset  target entry offset
   * @param  before  number of entries before the target
   * @param  after  number of entries after the target
   * @param  critical  whether this control is critical
   */
  public VirtualListViewRequestControl(
    final int offset,
    final int before,
    final int after,
    final boolean critical)
  {
    this(offset, before, after, 0, null, critical);
  }


  /**
   * Creates a new virtual list view request control.
   *
   * @param  offset  target entry offset
   * @param  before  number of entries before the target
   * @param  after  number of entries after the target
   * @param  count  server estimate of the number of entries
   * @param  context  server context id
   */
  public VirtualListViewRequestControl(
    final int offset,
    final int before,
    final int after,
    final int count,
    final byte[] context)
  {
    this(offset, before, after, count, context, false);
  }


  /**
   * Creates a new virtual list view request control.
   *
   * @param  offset  target entry offset
   * @param  before  number of entries before the target
   * @param  after  number of entries after the target
   * @param  count  server estimate of the number of entries
   * @param  context  server context id
   * @param  critical  whether this control is critical
   */
  public VirtualListViewRequestControl(
    final int offset,
    final int before,
    final int after,
    final int count,
    final byte[] context,
    final boolean critical)
  {
    super(OID, critical);
    setTargetOffset(offset);
    setBeforeCount(before);
    setAfterCount(after);
    setContentCount(count);
    setContextID(context);
  }


  /**
   * Creates a new virtual list view request control.
   *
   * @param  assertion  value to match in the sort control
   * @param  before  number of entries before the target
   * @param  after  number of entries after the target
   */
  public VirtualListViewRequestControl(
    final String assertion,
    final int before,
    final int after)
  {
    this(assertion, before, after, null, false);
  }


  /**
   * Creates a new virtual list view request control.
   *
   * @param  assertion  value to match in the sort control
   * @param  before  number of entries before the target
   * @param  after  number of entries after the target
   * @param  critical  whether this control is critical
   */
  public VirtualListViewRequestControl(
    final String assertion,
    final int before,
    final int after,
    final boolean critical)
  {
    this(assertion, before, after, null, critical);
  }


  /**
   * Creates a new virtual list view request control.
   *
   * @param  assertion  value to match in the sort control
   * @param  before  number of entries before the target
   * @param  after  number of entries after the target
   * @param  context  server context id
   */
  public VirtualListViewRequestControl(
    final String assertion,
    final int before,
    final int after,
    final byte[] context)
  {
    this(assertion, before, after, context, false);
  }


  /**
   * Creates a new virtual list view request control.
   *
   * @param  assertion  value to match in the sort control
   * @param  before  number of entries before the target
   * @param  after  number of entries after the target
   * @param  context  server context id
   * @param  critical  whether this control is critical
   */
  public VirtualListViewRequestControl(
    final String assertion,
    final int before,
    final int after,
    final byte[] context,
    final boolean critical)
  {
    super(OID, critical);
    setAssertionValue(assertion);
    setBeforeCount(before);
    setAfterCount(after);
    setContextID(context);
  }


  /**
   * Returns the before count. This indicates how many entries before the target
   * entry the client wants the server to send.
   *
   * @return  before count
   */
  public int getBeforeCount()
  {
    return beforeCount;
  }


  /**
   * Sets the before count.
   *
   * @param  count  before count
   */
  public void setBeforeCount(final int count)
  {
    beforeCount = count;
  }


  /**
   * Returns the after count. This indicates how many entries after the target
   * entry the client wants the server to send.
   *
   * @return  after count
   */
  public int getAfterCount()
  {
    return afterCount;
  }


  /**
   * Sets the after count.
   *
   * @param  count  after count
   */
  public void setAfterCount(final int count)
  {
    afterCount = count;
  }


  /**
   * Returns the target offset. This indicates the return entry's offset within
   * the ordered search result set.
   *
   * @return  target offset
   */
  public int getTargetOffset()
  {
    return targetOffset;
  }


  /**
   * Sets the target offset.
   *
   * @param  offset  target offset
   */
  public void setTargetOffset(final int offset)
  {
    targetOffset = offset;
  }


  /**
   * Returns the content count. From the RFC:
   *
   * <p>contentCount gives the server's estimate of the current number of
   * entries in the list. Together these give sufficient information for the
   * client to update a list box slider position to match the newly retrieved
   * entries and identify the target entry. The contentCount value returned
   * SHOULD be used in a subsequent VirtualListViewRequest control.</p>
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
   * Returns the assertion value. From the RFC:
   *
   * <p>The assertion value is encoded according to the ORDERING matching rule
   * for the attributeDescription in the sort control [SSS]. If present, the
   * value supplied in greaterThanOrEqual is used to determine the target entry
   * by comparison with the values of the attribute specified as the primary
   * sort key. The first list entry who's value is no less than (less than or
   * equal to when the sort order is reversed) the supplied value is the target
   * entry.</p>
   *
   * @return  assertion value
   */
  public String getAssertionValue()
  {
    return assertionValue;
  }


  /**
   * Sets the assertion value.
   *
   * @param  value  assertion value
   */
  public void setAssertionValue(final String value)
  {
    assertionValue = value;
  }


  /**
   * Returns the context id. From the RFC:
   *
   * <p>The contextID is a server-defined octet string. If present, the contents
   * of the contextID field SHOULD be returned to the server by a client in a
   * subsequent virtual list request. The presence of a contextID here indicates
   * that the server is willing to return contiguous data from a subsequent
   * search request which uses the same search criteria, accompanied by a
   * VirtualListViewRequest which indicates that the client wishes to receive an
   * adjoining page of data.</p>
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


  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        getOID(),
        getCriticality(),
        beforeCount,
        afterCount,
        targetOffset,
        contentCount,
        assertionValue,
        contextID);
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::criticality=%s, beforeCount=%s, afterCount=%s, " +
        "targetOffset=%s, contentCount=%s, assertionValue=%s, contextID=%s]",
        getClass().getName(),
        hashCode(),
        getCriticality(),
        beforeCount,
        afterCount,
        targetOffset,
        contentCount,
        assertionValue,
        LdapUtils.base64Encode(contextID));
  }


  /** {@inheritDoc} */
  @Override
  public byte[] encode()
  {
    final List<DEREncoder> l = new ArrayList<>();
    if (getAssertionValue() != null) {
      l.add(new IntegerType(getBeforeCount()));
      l.add(new IntegerType(getAfterCount()));
      l.add(
        new OctetStringType(new ContextDERTag(1, false), getAssertionValue()));
      if (getContextID() != null) {
        l.add(new OctetStringType(getContextID()));
      }
    } else {
      l.add(new IntegerType(getBeforeCount()));
      l.add(new IntegerType(getAfterCount()));
      l.add(
        new ConstructedDEREncoder(
          new ContextDERTag(0, true),
          new IntegerType(getTargetOffset()),
          new IntegerType(getContentCount())));
      if (getContextID() != null) {
        l.add(new OctetStringType(getContextID()));
      }
    }

    final ConstructedDEREncoder se = new ConstructedDEREncoder(
      UniversalDERTag.SEQ,
      l.toArray(new DEREncoder[l.size()]));
    return se.encode();
  }
}
