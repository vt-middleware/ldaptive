/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.function.Function;
import org.ldaptive.SearchResponse;

/**
 * Marker interface for a search result handler.
 *
 * @author  Middleware Services
 */
public interface SearchResultHandler extends Function<SearchResponse, SearchResponse> {}
