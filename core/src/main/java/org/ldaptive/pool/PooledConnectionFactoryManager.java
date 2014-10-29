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
package org.ldaptive.pool;

/**
 * Interface for objects that manage an instance of pooled connection factory.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface PooledConnectionFactoryManager
{


  /**
   * Returns the connection factory.
   *
   * @return  connection factory
   */
  PooledConnectionFactory getConnectionFactory();


  /**
   * Sets the connection factory.
   *
   * @param  cf  connection factory
   */
  void setConnectionFactory(PooledConnectionFactory cf);
}
