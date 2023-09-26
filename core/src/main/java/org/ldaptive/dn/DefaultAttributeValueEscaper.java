/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

import org.ldaptive.LdapUtils;

/**
 * Escapes an attribute value per RFC 4514 section 2.4. ASCII control characters and all non-ASCII data is HEX encoded.
 *
 * @author  Middleware Services
 */
public class DefaultAttributeValueEscaper extends AbstractAttributeValueEscaper
{


  @Override
  protected void processAscii(final StringBuilder sb, final char ch)
  {
    // CheckStyle:MagicNumber OFF
    if (ch > 31 && ch < 127) {
      sb.append(ch);
    } else {
      escapeHex(sb, LdapUtils.hexEncode(ch));
    }
    // CheckStyle:MagicNumber ON
  }


  @Override
  protected void processNonAscii(final StringBuilder sb, final byte... bytes)
  {
    escapeHex(sb, LdapUtils.hexEncode(bytes));
  }
}
