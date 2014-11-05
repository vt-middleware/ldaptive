/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import org.ldaptive.Response;
import org.ldaptive.SearchResult;
import org.ldaptive.control.VirtualListViewRequestControl;
import org.ldaptive.control.VirtualListViewResponseControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains data required by the virtual list view operation.
 *
 * @author  Middleware Services
 */
public class VirtualListViewParams
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** VLV before count. */
  private final int beforeCount;

  /** VLV after count. */
  private final int afterCount;

  /** VLV target offset; mutually exclusive with the assertion value. */
  private final int targetOffset;

  /** VLV assertion value; mutually exclusive with the target offset. */
  private final String assertionValue;


  /**
   * Creates a new virtual list view params.
   *
   * @param  offset  target offset
   * @param  before  before count
   * @param  after  after count
   */
  public VirtualListViewParams(
    final int offset,
    final int before,
    final int after)
  {
    targetOffset = offset;
    beforeCount = before;
    afterCount = after;
    assertionValue = null;
  }


  /**
   * Creates a new virtual list view params.
   *
   * @param  assertion  assertion value
   * @param  before  before count
   * @param  after  after count
   */
  public VirtualListViewParams(
    final String assertion,
    final int before,
    final int after)
  {
    assertionValue = assertion;
    beforeCount = before;
    afterCount = after;
    targetOffset = 0;
  }


  /**
   * Returns the before count.
   *
   * @return  before count
   */
  public int getBeforeCount()
  {
    return beforeCount;
  }


  /**
   * Returns the after count.
   *
   * @return  after count
   */
  public int getAfterCount()
  {
    return afterCount;
  }


  /**
   * Returns the target offset.
   *
   * @return  target offset
   */
  public int getTargetOffset()
  {
    return targetOffset;
  }


  /**
   * Returns the assertion value.
   *
   * @return  assertion value
   */
  public String getAssertionValue()
  {
    return assertionValue;
  }


  /**
   * Creates a new virtual list view request control using the properties in
   * this VLV params.
   *
   * @param  critical  whether the returned control is critical
   *
   * @return  virtual list view request control
   */
  public VirtualListViewRequestControl createRequestControl(
    final boolean critical)
  {
    if (assertionValue != null) {
      return
        new VirtualListViewRequestControl(
          assertionValue,
          beforeCount,
          afterCount,
          critical);
    } else {
      return
        new VirtualListViewRequestControl(
          targetOffset,
          beforeCount,
          afterCount,
          critical);
    }
  }


  /**
   * Creates a new virtual list view request control using the properties in
   * this VLV params. The supplied response is inspected and if it contains a
   * VLV response control, it's contextID and/or content count will be passed
   * into the created request control.
   *
   * @param  response  response of a previous VLV operation
   * @param  critical  whether the returned control is critical
   *
   * @return  virtual list view request control
   */
  public VirtualListViewRequestControl createRequestControl(
    final Response<SearchResult> response,
    final boolean critical)
  {
    final VirtualListViewRequestControl control = createRequestControl(
      critical);
    final VirtualListViewResponseControl responseControl =
      (VirtualListViewResponseControl) response.getControl(
        VirtualListViewResponseControl.OID);
    if (responseControl != null) {
      if (assertionValue == null) {
        control.setContentCount(responseControl.getContentCount());
      }
      control.setContextID(responseControl.getContextID());
    }
    return control;
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    String s;
    if (assertionValue != null) {
      s = String.format(
        "[%s@%d::assertionValue=%s, beforeCount=%s, afterCount=%s]",
        getClass().getName(),
        hashCode(),
        assertionValue,
        beforeCount,
        afterCount);
    } else {
      s = String.format(
        "[%s@%d::targetOffset=%s, beforeCount=%s, afterCount=%s]",
        getClass().getName(),
        hashCode(),
        targetOffset,
        beforeCount,
        afterCount);
    }
    return s;
  }
}
