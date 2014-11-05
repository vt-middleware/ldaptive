/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.extended;

import org.ldaptive.extended.AbstractExtendedResponse;

/**
 * Contains the response from a fast bind operation.
 *
 * @author  Middleware Services
 */
public class FastBindResponse extends AbstractExtendedResponse<Void>
{


  /** {@inheritDoc} */
  @Override
  public String getOID()
  {
    return null;
  }


  /** {@inheritDoc} */
  @Override
  public void decode(final byte[] encoded) {}


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return String.format("[%s@%d]", getClass().getName(), hashCode());
  }
}
