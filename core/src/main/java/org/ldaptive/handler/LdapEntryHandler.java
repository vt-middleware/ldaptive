/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.function.Function;
import org.ldaptive.LdapEntry;

/**
 * Marker interface for an ldap entry handler.
 *
 * @author  Middleware Services
 */
public interface LdapEntryHandler extends Function<LdapEntry, LdapEntry> {}
