/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.extended;

import java.util.Arrays;
import org.ldaptive.AbstractRequest;
import org.ldaptive.extended.ExtendedRequest;

/**
 * Contains the data required to perform a fast bind operation.
 *
 * @author  Middleware Services
 */
public class FastBindRequest extends AbstractRequest implements ExtendedRequest
{

  /** OID of this extended request. */
  public static final String OID = "1.2.840.113556.1.4.1781";


  @Override
  public byte[] encode()
  {
    return null;
  }


  @Override
  public String getOID()
  {
    return OID;
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::controls=%s, referralHandler=%s, " +
        "intermediateResponseHandlers=%s]",
        getClass().getName(),
        hashCode(),
        Arrays.toString(getControls()),
        getReferralHandler(),
        Arrays.toString(getIntermediateResponseHandlers()));
  }
}
