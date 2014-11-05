/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans;

/**
 * Interface for mutating the DN value on an arbitrary object.
 *
 * @author  Middleware Services
 * @version  $Revision: 2887 $ $Date: 2014-02-26 12:23:53 -0500 (Wed, 26 Feb 2014) $
 */
public interface DnValueMutator
{

  /**
   * Returns the DN value for the supplied object.
   *
   * @param  object  to return the DN of
   *
   * @return  DN value
   */
  String getValue(final Object object);


  /**
   * Set the DN value for the supplied object.
   *
   * @param  object  to set the DN on
   * @param  value  of the DN
   */
  void setValue(final Object object, final String value);
}
