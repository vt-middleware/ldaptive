/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans;

import org.ldaptive.LdapEntry;

/**
 * Interface for ldap entry mappers.
 *
 * @param  <T>  type of object to map
 *
 * @author  Middleware Services
 * @version  $Revision: 2887 $ $Date: 2014-02-26 12:23:53 -0500 (Wed, 26 Feb 2014) $
 */
public interface LdapEntryMapper<T>
{


  /**
   * Returns the LDAP DN for the supplied object.
   *
   * @param  object  to retrieve the DN from
   *
   * @return  LDAP DN
   */
  String mapDn(T object);


  /**
   * Injects data from the supplied source object into the supplied ldap entry.
   *
   * @param  source  to read from
   * @param  dest  to write to
   */
  void map(T source, LdapEntry dest);


  /**
   * Injects data from the supplied ldap entry into the supplied destination
   * object.
   *
   * @param  source  to read from
   * @param  dest  to write to
   */
  void map(LdapEntry source, T dest);
}
