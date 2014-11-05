/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.util.Set;
import org.ldaptive.Connection;

/**
 * Provides an interface for connection pooling.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface ConnectionPool
{


  /**
   * Returns the activator for this pool.
   *
   * @return  activator
   */
  Activator<Connection> getActivator();


  /**
   * Sets the activator for this pool.
   *
   * @param  a  activator
   */
  void setActivator(Activator<Connection> a);


  /**
   * Returns the passivator for this pool.
   *
   * @return  passivator
   */
  Passivator<Connection> getPassivator();


  /**
   * Sets the passivator for this pool.
   *
   * @param  p  passivator
   */
  void setPassivator(Passivator<Connection> p);


  /**
   * Returns the validator for this pool.
   *
   * @return  validator
   */
  Validator<Connection> getValidator();


  /**
   * Sets the validator for this pool.
   *
   * @param  v  validator
   */
  void setValidator(Validator<Connection> v);


  /** Initialize this pool for use. */
  void initialize();


  /**
   * Returns an object from the pool.
   *
   * @return  pooled object
   *
   * @throws  PoolException  if this operation fails
   * @throws  BlockingTimeoutException  if this pool is configured with a block
   * time and it occurs
   * @throws  PoolInterruptedException  if this pool is configured with a block
   * time and the current thread is interrupted
   */
  Connection getConnection()
    throws PoolException;


  /**
   * Returns the number of connections available for use.
   *
   * @return  count
   */
  int availableCount();


  /**
   * Returns the number of connections in use.
   *
   * @return  count
   */
  int activeCount();


  /**
   * Returns the statistics for each connection in the pool.
   *
   * @return  connection statistics
   */
  Set<PooledConnectionStatistics> getPooledConnectionStatistics();


  /** Empty this pool, freeing any resources. */
  void close();
}
