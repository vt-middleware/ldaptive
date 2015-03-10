/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.servlets;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchResult;

/**
 * Writes a single attribute value to the HTTP response. Useful for providing a mechanism to download large LDAP
 * attributes, such as certificates and photos. See {@link AbstractServletSearchExecutor}.
 *
 * @author  Middleware Services
 */
public class AttributeValueServletSearchExecutor extends AbstractServletSearchExecutor
{


  @Override
  protected void writeResponse(final SearchResult result, final HttpServletResponse response)
    throws IOException
  {
    final LdapEntry e = result.getEntry();
    if (e != null && e.size() > 0) {
      final LdapAttribute a = e.getAttribute();
      if (a != null && a.size() > 0) {
        if (a.isBinary()) {
          response.setContentType("application/octet-stream");
          response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s.bin\"", a.getName()));
        } else {
          response.setContentType("text/plain");
        }

        final OutputStream out = response.getOutputStream();
        out.write(a.getBinaryValue());
        out.flush();
      }
    }
  }
}
