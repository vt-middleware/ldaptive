/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.servlets;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.servlet.http.HttpServletResponse;
import org.ldaptive.SearchResult;
import org.ldaptive.io.Dsmlv1Writer;

/**
 * Writes search results as DSML version 1. See {@link
 * AbstractServletSearchTemplatesExecutor}.
 *
 * @author  Middleware Services
 */
public class Dsmlv1ServletSearchTemplatesExecutor
  extends AbstractServletSearchTemplatesExecutor
{


  @Override
  protected void writeResponse(
    final SearchResult result,
    final HttpServletResponse response)
    throws IOException
  {
    response.setContentType("text/xml");

    final Dsmlv1Writer writer = new Dsmlv1Writer(
      new BufferedWriter(new OutputStreamWriter(response.getOutputStream())));
    writer.write(result);
  }
}
