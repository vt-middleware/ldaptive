/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import java.util.Arrays;
import org.ldaptive.AbstractRequest;

/**
 * Contains the data required to perform an ldap who am i operation. See RFC
 * 4532.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class WhoAmIRequest extends AbstractRequest implements ExtendedRequest
{

  /** OID of this extended request. */
  public static final String OID = "1.3.6.1.4.1.4203.1.11.3";


  /** {@inheritDoc} */
  @Override
  public byte[] encode()
  {
    return null;
  }


  /** {@inheritDoc} */
  @Override
  public String getOID()
  {
    return OID;
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::controls=%s]",
        getClass().getName(),
        hashCode(),
        Arrays.toString(getControls()));
  }
}
