/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.function.Consumer;
import org.ldaptive.LdapException;

/**
 * Marker interface for an LDAP exception handler.
 *
 * @author  Middleware Services
 */
public interface ExceptionHandler extends Consumer<LdapException> {}
