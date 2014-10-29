/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive;

import org.ldaptive.control.RequestControl;
import org.ldaptive.provider.ProviderConnection;

/**
 * Interface for ldap connection implementations.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface Connection
{


  /**
   * Returns the connection config for this connection. The config may be
   * read-only.
   *
   * @return  connection config
   */
  ConnectionConfig getConnectionConfig();


  /**
   * This will establish a connection to the LDAP. This connection should be
   * closed using {@link #close()}.
   *
   * @return  response associated with the {@link ConnectionInitializer} or an
   * empty response if no connection initializer was configured
   *
   * @throws  IllegalStateException  if the connection is already open
   * @throws  LdapException  if the LDAP cannot be reached
   */
  Response<Void> open()
    throws LdapException;


  /**
   * This will establish a connection to the LDAP using the supplied bind
   * request. This connection should be closed using {@link #close()}.
   *
   * @param  request  containing bind information
   *
   * @return  response associated with the bind operation
   *
   * @throws  IllegalStateException  if the connection is already open
   * @throws  LdapException  if the LDAP cannot be reached
   */
  Response<Void> open(BindRequest request)
    throws LdapException;


  /**
   * Returns whether {@link #open(BindRequest)} was successfully invoked on this
   * connection and {@link #close()} and not been invoked. This method does not
   * indicate the viability of this connection for use.
   *
   * @return  whether this connection is open
   */
  boolean isOpen();


  /**
   * Returns the provider connection to invoke the provider specific
   * implementation. Must be called after a successful call to {@link #open()}.
   *
   * @return  provider connection
   */
  ProviderConnection getProviderConnection();


  /** This will close the connection to the LDAP. */
  void close();


  /**
   * This will close the connection to the LDAP using the supplied controls.
   *
   * @param  controls  request controls
   */
  void close(RequestControl[] controls);


  /**
   * This will close an existing connection to the LDAP and establish a new
   * connection to the LDAP.
   *
   * @return  response associated with the {@link ConnectionInitializer} or an
   * empty response if no connection initializer was configured
   *
   * @throws  LdapException  if the LDAP cannot be reached
   */
  Response<Void> reopen()
    throws LdapException;


  /**
   * This will close an existing connection to the LDAP and establish a new
   * connection to the LDAP using the supplied bind request.
   *
   * @param  request  containing bind information
   *
   * @return  response associated with the bind operation
   *
   * @throws  LdapException  if the LDAP cannot be reached
   */
  Response<Void> reopen(BindRequest request)
    throws LdapException;
}
