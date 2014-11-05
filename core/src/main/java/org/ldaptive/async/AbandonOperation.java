/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.async;

import java.util.Arrays;
import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.control.RequestControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes an ldap abandon operation.
 *
 * @author  Middleware Services
 */
public class AbandonOperation
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Connection to perform operation. */
  private final Connection connection;


  /**
   * Creates a new abandon operation.
   *
   * @param  conn  connection
   */
  public AbandonOperation(final Connection conn)
  {
    connection = conn;
  }


  /**
   * Execute this ldap operation.
   *
   * @param  messageId  of the operation to abandon
   *
   * @throws  LdapException  if the operation fails
   */
  public void execute(final int messageId)
    throws LdapException
  {
    execute(messageId, null);
  }


  /**
   * Execute this ldap operation.
   *
   * @param  messageId  of the operation to abandon
   * @param  controls  request controls
   *
   * @throws  LdapException  if the operation fails
   */
  public void execute(final int messageId, final RequestControl[] controls)
    throws LdapException
  {
    logger.debug(
      "execute abandon for messageId={}, controls={} with connection={}",
      messageId,
      Arrays.toString(controls),
      connection);
    connection.getProviderConnection().abandon(messageId, controls);
  }
}
