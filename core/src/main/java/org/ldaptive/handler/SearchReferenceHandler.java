/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.function.Consumer;
import org.ldaptive.SearchResultReference;

/**
 * Marker interface for a search reference handler.
 *
 * @author  Middleware Services
 */
public interface SearchReferenceHandler extends Consumer<SearchResultReference> {}
