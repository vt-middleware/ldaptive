/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

/**
 * LDAP external bind request.
 *
 * @author  Middleware Services
 */
public class ExternalBindRequest extends SASLBindRequest
{

  /** External SASL mechanism name. */
  private static final String MECHANISM = "EXTERNAL";


  /**
   * Creates a new external bind request.
   *
   * @param  authzID  to bind as
   */
  public ExternalBindRequest(final String authzID)
  {
    super(MECHANISM, authzID);
  }
}
