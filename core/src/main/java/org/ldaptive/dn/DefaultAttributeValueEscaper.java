/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

import org.ldaptive.LdapUtils;

/**
 * Escapes an attribute value per RFC 4514 section 2.4.
 *
 * @author  Middleware Services
 */
public class DefaultAttributeValueEscaper implements AttributeValueEscaper
{


  @Override
  public String escape(final String value)
  {
    if (value == null || value.isEmpty()) {
      return value;
    }

    final int len = value.length();
    final StringBuilder sb = new StringBuilder(len);
    char ch;
    for (int i = 0; i < len; i++) {
      ch = value.charAt(i);
      switch (ch) {

      case '"':
      case '#':
      case '+':
      case ',':
      case ';':
      case '<':
      case '=':
      case '>':
      case '\\':
        sb.append('\\').append(ch);
        break;

      case ' ':
        // escape first space and last space
        if (i == 0 || i + 1 == len) {
          sb.append('\\').append(ch);
        } else {
          sb.append(ch);
        }
        break;

      case 0:
        // escape null
        sb.append("\\00");
        break;

      default:
        // CheckStyle:MagicNumber OFF
        if (ch > 31 && ch < 127) {
          sb.append(ch);
        } else if (i + 1 < len && Character.isHighSurrogate(ch)) {
          // escape UTF-8 characters
          final char nextChar = value.charAt(++i);
          final char[] hex;
          if (Character.isLowSurrogate(nextChar)) {
            final int codePoint = Character.toCodePoint(ch, nextChar);
            hex = LdapUtils.hexEncode(LdapUtils.utf8Encode(new String(new int[] {codePoint}, 0, 1)));
          } else {
            hex = LdapUtils.hexEncode(ch);
          }
          escapeHex(sb, hex);
        } else {
          // escape non-printable ASCII characters
          escapeHex(sb, LdapUtils.hexEncode(ch));
        }
        // CheckStyle:MagicNumber ON
        break;
      }
    }
    return sb.toString();
  }


  /**
   * Appends a backslash for every two hex characters.
   *
   * @param  sb  to apppend to
   * @param  hex  to read
   */
  private void escapeHex(final StringBuilder sb, final char... hex)
  {
    for (int i = 0; i < hex.length; i += 2) {
      sb.append('\\').append(hex[i]).append(hex[i + 1]);
    }
  }
}
