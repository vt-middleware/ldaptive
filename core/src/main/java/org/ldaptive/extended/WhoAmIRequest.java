/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import java.util.Arrays;
import org.ldaptive.AbstractRequest;

/**
 * Contains the data required to perform an ldap who am i operation. See RFC
 * 4532.
 *
 * @author  Middleware Services
 */
public class WhoAmIRequest extends AbstractRequest implements ExtendedRequest
{

  /** OID of this extended request. */
  public static final String OID = "1.3.6.1.4.1.4203.1.11.3";


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
