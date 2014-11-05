/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import java.nio.ByteBuffer;
import org.ldaptive.asn1.OctetStringType;

/**
 * Contains the response from an ldap who am i operation. See RFC 4532.
 *
 * @author  Middleware Services
 */
public class WhoAmIResponse extends AbstractExtendedResponse<String>
{


  /** {@inheritDoc} */
  @Override
  public String getOID()
  {
    // RFC defines the response name as absent
    return null;
  }


  /** {@inheritDoc} */
  @Override
  public void decode(final byte[] encoded)
  {
    setValue(OctetStringType.decode(ByteBuffer.wrap(encoded)));
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return String.format("[%s@%d]", getClass().getName(), hashCode());
  }
}
