/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

/**
 * LDAP external bind request.
 *
 * @author  Middleware Services
 */
public class ExternalBindRequest extends SaslBindRequest
{

  /** External SASL mechanism. */
  public static final Mechanism MECHANISM = Mechanism.EXTERNAL;


  /**
   * Creates a new external bind request.
   */
  public ExternalBindRequest()
  {
    this(null);
  }


  /**
   * Creates a new external bind request.
   *
   * @param  authzID  to bind as
   */
  public ExternalBindRequest(final String authzID)
  {
    super(MECHANISM.mechanism(), authzID != null ? authzID : "");
  }
}
