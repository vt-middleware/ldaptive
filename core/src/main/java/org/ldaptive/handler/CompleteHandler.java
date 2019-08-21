/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

/**
 * Marker interface for a complete handler.
 *
 * @author  Middleware Services
 */
@FunctionalInterface
public interface CompleteHandler
{


  /** Method to execute on completion. */
  void execute();
}
