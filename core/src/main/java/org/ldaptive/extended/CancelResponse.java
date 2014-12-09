/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

/**
 * Contains the response from an ldap cancel operation. See RFC 3909.
 *
 * @author  Middleware Services
 */
public class CancelResponse extends AbstractExtendedResponse<Void>
{


  @Override
  public String getOID()
  {
    return null;
  }


  @Override
  public void decode(final byte[] encoded) {}


  @Override
  public String toString()
  {
    return String.format("[%s@%d]", getClass().getName(), hashCode());
  }
}
