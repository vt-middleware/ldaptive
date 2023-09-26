/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

import org.ldaptive.LdapUtils;

/**
 * Escapes an attribute value per RFC 4514 section 2.4. Implementations must decide how to handle unspecified
 * characters.
 *
 * @author  Middleware Services
 */
public abstract class AbstractAttributeValueEscaper implements AttributeValueEscaper
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
        if (ch <= 127) {
          processAscii(sb, ch);
        } else if (i + 1 < len && Character.isHighSurrogate(ch)) {
          final char nextChar = value.charAt(++i);
          if (Character.isLowSurrogate(nextChar)) {
            final int codePoint = Character.toCodePoint(ch, nextChar);
            processNonAscii(sb, LdapUtils.utf8Encode(new String(new int[] {codePoint}, 0, 1)));
          } else {
            processNonAscii(sb, LdapUtils.utf8Encode(String.valueOf(ch)));
          }
        } else {
          processNonAscii(sb, LdapUtils.utf8Encode(String.valueOf(ch)));
        }
        // CheckStyle:MagicNumber ON
        break;
      }
    }
    return sb.toString();
  }


  /**
   * Process ASCII character.
   *
   * @param  sb to append characters to
   * @param  ch  to process
   */
  protected abstract void processAscii(StringBuilder sb, char ch);


  /**
   * Process non-ASCII character(s).
   *
   * @param  sb to append characters to
   * @param  bytes  to process
   */
  protected abstract void processNonAscii(StringBuilder sb, byte... bytes);


  /**
   * Appends a backslash for every two hex characters.
   *
   * @param  sb  to append to
   * @param  hex  to read
   */
  protected void escapeHex(final StringBuilder sb, final char... hex)
  {
    for (int i = 0; i < hex.length; i += 2) {
      sb.append('\\').append(hex[i]).append(hex[i + 1]);
    }
  }
}
