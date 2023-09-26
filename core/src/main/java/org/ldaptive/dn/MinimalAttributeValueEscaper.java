/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

import org.ldaptive.LdapUtils;

/**
 * Escapes an attribute value per RFC 4514 section 2.4. ASCII control characters and all non-ASCII data is not encoded.
 *
 * @author  Middleware Services
 */
public class MinimalAttributeValueEscaper extends AbstractAttributeValueEscaper
{


  @Override
  protected void processAscii(final StringBuilder sb, final char ch)
  {
    sb.append(ch);
  }


  @Override
  protected void processNonAscii(final StringBuilder sb, final byte... bytes)
  {
    sb.append(LdapUtils.utf8Encode(bytes));
  }
}
