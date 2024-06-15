/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.asn1.DefaultDERBuffer;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.asn1.ParseHandler;

/**
 * Parses DNs following the rules in <a href="http://www.ietf.org/rfc/rfc4514.txt">RFC 4514</a>. Attempts to be as
 * generous as possible in the format of allowed DNs.
 *
 * @author  Middleware Services
 */
public final class DefaultDnParser implements DnParser
{

  /** Hexadecimal radix. */
  private static final int HEX_RADIX = 16;

  /** DER path for hex values. */
  private static final DERPath HEX_PATH = new DERPath("/OCTSTR[0]");


  /**
   * Parses the supplied DN into a list of RDNs.
   *
   * @param  dn  to parse
   *
   * @return  unmodifiable list of RDNs
   */
  @Override
  public List<RDn> parse(final String dn)
  {
    if (LdapUtils.trimSpace(dn).isEmpty()) {
      return Collections.emptyList();
    }

    final List<RDn> rdns = new ArrayList<>();
    final List<NameValue> nameValues = new ArrayList<>();
    int pos = 0;
    while (pos < dn.length()) {
      final int[] endAttrNamePos = readToChar(dn, new char[] {'='}, pos);
      final String attrName = LdapUtils.trimSpace(dn.substring(pos, endAttrNamePos[0]));
      if (attrName.isEmpty()) {
        throw new IllegalArgumentException("Invalid RDN: no attribute name found for " + dn);
      } else if (attrName.contains("+") || attrName.contains(",")) {
        throw new IllegalArgumentException("Invalid RDN: unexpected '" + attrName.charAt(0) + "' for " + dn);
      }
      pos = endAttrNamePos[0];
      // error if char isn't an '='
      if (pos >= dn.length() || dn.charAt(pos++) != '=') {
        throw new IllegalArgumentException("Invalid RDN: no equals found for " + dn);
      }

      final int[] endAttrValuePos = readToChar(dn, new char[] {'+', ','}, pos);
      final String attrValue = LdapUtils.trimSpace(dn.substring(pos, endAttrValuePos[0]));
      if (attrValue.isEmpty()) {
        nameValues.add(new NameValue(attrName, ""));
      } else if (attrValue.startsWith("#")) {
        final DERParser parser = new DERParser();
        final OctetStringHandler handler = new OctetStringHandler();
        parser.registerHandler(HEX_PATH, handler);

        final String hexData = attrValue.substring(1);
        parser.parse(new DefaultDERBuffer(decodeHexValue(hexData.toCharArray())));
        nameValues.add(new NameValue(attrName, handler.getDecodedValue()));
      } else {
        nameValues.add(new NameValue(attrName, decodeStringValue(attrValue)));
      }
      if (endAttrValuePos[1] == -1 || endAttrValuePos[1] == ',') {
        rdns.add(new RDn(nameValues));
        nameValues.clear();
      }
      pos = endAttrValuePos[0] + 1;
      if (pos == dn.length() && endAttrValuePos[1] != -1) {
        // dangling match character
        throw new IllegalArgumentException(
          "Invalid RDN: attribute value ends with '" + endAttrValuePos[1] + "' for " + dn);
      }
    }
    return Collections.unmodifiableList(rdns);
  }


  /**
   * Decodes the supplied hexadecimal value.
   *
   * @param  value  hex to decode
   *
   * @return  decoded bytes
   */
  private static byte[] decodeHexValue(final char[] value)
  {
    if (value == null || value.length == 0) {
      throw new IllegalArgumentException("Invalid HEX value: value cannot be null or empty");
    }
    return LdapUtils.hexDecode(value);
  }


  /**
   * Decodes the supplied string attribute value. Unescapes escaped characters. If escaped character is a hex value, it
   * is decoded.
   *
   * @param  value  to decode
   *
   * @return  decoded string
   */
  private static String decodeStringValue(final String value)
  {
    if (!value.contains("\\")) {
      return value;
    }

    final StringBuilder sb = new StringBuilder();
    int pos = 0;
    final StringBuilder hexValue = new StringBuilder();
    while (pos < value.length()) {
      char c = value.charAt(pos);
      boolean appendHex = false;
      boolean appendValue = false;
      if (c == '\\') {
        if (pos + 1 < value.length()) {
          c = value.charAt(++pos);
          // if hexadecimal character add to buffer to decode later
          if (Character.digit(c, HEX_RADIX) != -1) {
            if (pos + 1 < value.length()) {
              hexValue.append(c).append(value.charAt(++pos));
              if (pos + 1 == value.length()) {
                appendHex = true;
              }
            } else {
              throw new IllegalArgumentException("Invalid HEX value: " + c);
            }
          } else {
            appendHex = hexValue.length() > 0;
            appendValue = true;
          }
        }
      } else {
        appendHex = hexValue.length() > 0;
        appendValue = true;
      }
      if (appendHex) {
        sb.append(LdapUtils.utf8Encode(decodeHexValue(hexValue.toString().toCharArray())));
        hexValue.setLength(0);
      }
      if (appendValue) {
        sb.append(c);
      }

      pos++;
    }
    return sb.toString();
  }


  /**
   * Reads the supplied string starting at the supplied position until one of the supplied characters is found.
   * Characters escaped with '\' are ignored. Characters inside of quotes are ignored.
   *
   * @param  s  to read
   * @param  chars  to match
   * @param  pos  to start reading at
   *
   * @return  string index that matched a character or the last index in the string
   */
  private static int[] readToChar(final String s, final char[] chars, final int pos)
  {
    int i = pos;
    int matchChar = -1;
    // 0 = no quotes, 1 = in quotes, 2 = after quotes
    int quotes = 0;
    while (i < s.length()) {
      boolean match = false;
      final char sChar = s.charAt(i);
      // ignore escaped characters
      if (sChar == '\\') {
        i++;
        if (i == s.length()) {
          // attribute value ends with a backslash, be lenient and ignore it
          break;
        }
      } else if (sChar == '"') {
        quotes++;
      } else if (quotes != 1) {
        // do not check for match characters inside of quotes
        for (char c : chars) {
          if (c == s.charAt(i)) {
            matchChar = c;
            match = true;
            break;
          }
        }
        if (match) {
          break;
        }
      }
      i++;
    }
    return new int[] {i, matchChar};
  }


  /** Parse handler for decoding octet strings. */
  private static final class OctetStringHandler implements ParseHandler
  {

    /** Decoded octet string. */
    private String decoded;


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      decoded = OctetStringType.decode(encoded);
    }


    /**
     * Returns the decoded octet string value.
     *
     * @return  decoded octet string
     */
    public String getDecodedValue()
    {
      return decoded;
    }
  }
}
