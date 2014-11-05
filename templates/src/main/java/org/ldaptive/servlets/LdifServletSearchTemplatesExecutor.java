/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.servlets;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.servlet.http.HttpServletResponse;
import org.ldaptive.SearchResult;
import org.ldaptive.io.LdifWriter;

/**
 * Writes search results in LDIF format. See {@link
 * AbstractServletSearchTemplatesExecutor}.
 *
 * @author  Middleware Services
 */
public class LdifServletSearchTemplatesExecutor
  extends AbstractServletSearchTemplatesExecutor
{


  /** {@inheritDoc} */
  @Override
  protected void writeResponse(
    final SearchResult result,
    final HttpServletResponse response)
    throws IOException
  {
    response.setContentType("text/plain");

    final LdifWriter writer = new LdifWriter(
      new BufferedWriter(new OutputStreamWriter(response.getOutputStream())));
    writer.write(result);
  }
}
