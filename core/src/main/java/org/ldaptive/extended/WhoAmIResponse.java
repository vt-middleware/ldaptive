/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.OctetStringType;

/**
 * Contains the response from an ldap who am i operation. See RFC 4532.
 *
 * @author  Middleware Services
 */
public class WhoAmIResponse extends AbstractExtendedResponse<String>
{


  @Override
  public String getOID()
  {
    // RFC defines the response name as absent
    return null;
  }


  @Override
  public void decode(final DERBuffer encoded)
  {
    setValue(OctetStringType.decode(encoded));
  }


  @Override
  public String toString()
  {
    return String.format("[%s@%d]", getClass().getName(), hashCode());
  }
}
