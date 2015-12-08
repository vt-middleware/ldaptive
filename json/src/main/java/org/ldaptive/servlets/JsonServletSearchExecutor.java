/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.servlets;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.servlet.http.HttpServletResponse;
import org.ldaptive.SearchResult;
import org.ldaptive.io.JsonWriter;

/**
 * Writes search results as JSON. See {@link AbstractServletSearchExecutor}.
 *
 * @author  Middleware Services
 */
public class JsonServletSearchExecutor extends AbstractServletSearchExecutor
{


  @Override
  protected void writeResponse(final SearchResult result, final HttpServletResponse response)
    throws IOException
  {
    response.setContentType("application/json");

    final JsonWriter writer = new JsonWriter(new BufferedWriter(new OutputStreamWriter(response.getOutputStream())));
    writer.write(result);
  }
}
