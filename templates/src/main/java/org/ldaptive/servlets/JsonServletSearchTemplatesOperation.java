/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.servlets;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.servlet.http.HttpServletResponse;
import org.ldaptive.SearchResponse;
import org.ldaptive.io.JsonWriter;

/**
 * Writes execute results as JSON. See {@link AbstractServletSearchTemplatesOperation}.
 *
 * @author  Middleware Services
 */
public class JsonServletSearchTemplatesOperation extends AbstractServletSearchTemplatesOperation
{


  @Override
  protected void writeResponse(final SearchResponse result, final HttpServletResponse response)
    throws IOException
  {
    response.setContentType("application/json");

    final JsonWriter writer = new JsonWriter(new BufferedWriter(new OutputStreamWriter(response.getOutputStream())));
    writer.write(result);
  }
}
