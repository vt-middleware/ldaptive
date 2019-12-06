/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.function.Consumer;
import org.ldaptive.control.ResponseControl;

/**
 * Marker interface for a response control handler.
 *
 * @author  Middleware Services
 */
public interface ResponseControlHandler extends Consumer<ResponseControl> {}
