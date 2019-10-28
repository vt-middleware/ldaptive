/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DefaultDERBuffer;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.asn1.ParseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses DNs following the rules in <a href="http://www.ietf.org/rfc/rfc4514.txt">RFC 4514</a>. Attempts to be as
 * generous as possible in the format of allowed DNs.
 *
 * @author  Middleware Services
 */
public final class DnParser
{

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(DnParser.class);

  /** Hexadecimal radix. */
  private static final int HEX_RADIX = 16;


  /** Default constructor. */
  private DnParser() {}


  /**
   * Returns the RDN values for the attribute type with the supplied name.
   *
   * @param  dn  to parse
   * @param  name  of the attribute type to return values for
   *
   * @return  DN attribute values
   */
  public static Collection<String> getValues(final String dn, final String name)
  {
    final Collection<String> values = new ArrayList<>();
    convertDnToAttributes(dn).stream().filter(
      la -> la.getName().equalsIgnoreCase(name)).forEach(la -> values.addAll(la.getStringValues()));
    return values;
  }


  /**
   * Returns the RDN value for the attribute type with the supplied name. If the component has multiple values, the
   * first one is returned.
   *
   * @param  dn  to parse
   * @param  name  of the attribute to return value for
   *
   * @return  DN attribute value
   */
  public static String getValue(final String dn, final String name)
  {
    final Collection<String> values = getValues(dn, name);
    if (values.isEmpty()) {
      return "";
    }
    return values.iterator().next();
  }


  /**
   * Returns a string representation of the supplied DN beginning at the supplied index. The leftmost RDN component
   * begins at index 0.
   *
   * @param  dn  to parse
   * @param  beginIndex  index of first RDN to include in the result in the range [0, N-1] where N is the number of
   *                     elements in the DN
   *
   * @return  DN from the supplied beginIndex
   *
   * @throws  IndexOutOfBoundsException  if beginIndex is less than 0 or greater than the number of RDNs
   */
  public static String substring(final String dn, final int beginIndex)
  {
    if (beginIndex < 0) {
      throw new IndexOutOfBoundsException("beginIndex cannot be negative");
    }

    final List<LdapAttribute> attrs = convertDnToAttributes(dn);
    if (beginIndex >= attrs.size()) {
      throw new IndexOutOfBoundsException("beginIndex cannot be larger than the number of RDNs");
    }

    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < attrs.size(); i++) {
      if (i >= beginIndex) {
        final LdapAttribute la = attrs.get(i);
        sb.append(la.getName()).append("=").append(la.getStringValue()).append(",");
      }
    }
    if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',') {
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }


  /**
   * Returns a string representation of the supplied DN beginning at beginIndex (inclusive) and ending at endIndex
   * (exclusive). The leftmost RDN component begins at index 0. Where n is the number of RDNs, both beginIndex and
   * endIndex are on the range [0, N-1].
   *
   * @param  dn  to parse
   * @param  beginIndex  index of first RDN to include in the result in the range [0, N-2] where N is the number of
   *                     elements in the DN
   * @param  endIndex  index of last RDN to include in the result in the range [1, N-1] where N is the number of
   *                   elements in the RDN
   *
   * @return  DN from beginIndex (inclusive) to endIndex (exclusive)
   *
   * @throws  IndexOutOfBoundsException  if beginIndex is less than 0, if beginIndex is greater than endIndex, or
   * endIndex is greater than the number of RDNs
   */
  public static String substring(final String dn, final int beginIndex, final int endIndex)
  {
    if (beginIndex < 0) {
      throw new IndexOutOfBoundsException("beginIndex cannot be negative");
    }
    if (beginIndex > endIndex) {
      throw new IndexOutOfBoundsException("beginIndex cannot be larger than endIndex");
    }

    final List<LdapAttribute> attrs = convertDnToAttributes(dn);
    if (endIndex > attrs.size()) {
      throw new IndexOutOfBoundsException("endIndex cannot be larger than the number of RDNs");
    }

    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < attrs.size(); i++) {
      if (i >= beginIndex && i < endIndex) {
        final LdapAttribute la = attrs.get(i);
        sb.append(la.getName()).append("=").append(la.getStringValue()).append(",");
      }
    }
    if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',') {
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }


  /**
   * Parses the supplied DN and converts each RDN into a {@link LdapAttribute}.
   *
   * @param  dn  to parse
   *
   * @return  list of ldap attributes for each RDN
   */
  public static List<LdapAttribute> convertDnToAttributes(final String dn)
  {
    LOGGER.debug("parsing DN: {}", dn);

    final List<LdapAttribute> attributes = new ArrayList<>();
    if (dn.isEmpty()) {
      return attributes;
    }

    int pos = 0;
    while (pos < dn.length()) {
      final int endAttrNamePos = readToChar(dn, new char[] {'='}, pos);
      final String attrName = dn.substring(pos, endAttrNamePos);
      LOGGER.trace("read attribute name: [{}]", attrName);
      pos = endAttrNamePos;
      // error if char isn't an '='
      if (pos >= dn.length() || dn.charAt(pos++) != '=') {
        throw new IllegalArgumentException("Invalid DN: " + dn);
      }

      final int endAttrValuePos = readToChar(dn, new char[] {'+', ','}, pos);
      String attrValue = dn.substring(pos, endAttrValuePos);
      LOGGER.trace("read attribute value: [{}]", attrValue);
      attrValue = attrValue.trim();
      // error if attribute value is empty
      if (attrValue.isEmpty()) {
        throw new IllegalArgumentException("Invalid DN: " + dn);
      }
      if (attrValue.startsWith("#")) {
        final DERParser parser = new DERParser();
        final OctetStringHandler handler = new OctetStringHandler();
        parser.registerHandler("/OCTSTR[0]", handler);

        final String hexData = attrValue.substring(1);
        parser.parse(new DefaultDERBuffer(decodeHexValue(hexData.toCharArray())));
        attributes.add(new LdapAttribute(attrName.trim(), handler.getDecodedValue()));
      } else {
        attributes.add(new LdapAttribute(attrName.trim(), decodeStringValue(attrValue)));
      }
      pos = endAttrValuePos + 1;
    }
    LOGGER.debug("parsed DN into: {}", attributes);
    return attributes;
  }


  /**
   * Decodes the supplied hexadecimal value.
   *
   * @param  value  hex to decode
   *
   * @return  decoded bytes
   */
  protected static byte[] decodeHexValue(final char[] value)
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
  protected static String decodeStringValue(final String value)
  {
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
   * Characters escaped with '\' are ignored.
   *
   * @param  s  to read
   * @param  chars  to match
   * @param  pos  to start reading at
   *
   * @return  string index that matched a character or the last index in the string
   */
  private static int readToChar(final String s, final char[] chars, final int pos)
  {
    int i = pos;
    while (i < s.length()) {
      boolean match = false;
      final char sChar = s.charAt(i);
      // ignore escaped characters
      if (sChar == '\\' && i + 1 < s.length()) {
        i++;
      } else {
        for (char c : chars) {
          if (c == s.charAt(i)) {
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
    return i;
  }


  /** Parse handler for decoding octet strings. */
  private static class OctetStringHandler implements ParseHandler
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
