/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

/**
 * Contains the response from an ldap cancel operation. See RFC 3909.
 *
 * @author  Middleware Services
 */
public class CancelResponse extends AbstractExtendedResponse<Void>
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
