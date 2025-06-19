/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.io.Hex;

/**
 * Provides utility methods for this package.
 *
 * @author  Middleware Services
 */
public final class SchemaUtils
{


  /** Default constructor. */
  private SchemaUtils() {}


  /**
   * Parses the supplied descriptors string and returns its contents as a string array. If the string contains a single
   * quote it is assumed to be a multivalue descriptor of the form "'value1' 'value2' 'value3'". Otherwise, it is
   * treated as a single value descriptor.
   *
   * @param  descrs  string to parse
   *
   * @return  array of descriptors
   */
  public static String[] parseDescriptors(final String descrs)
  {
    if (descrs.contains("'")) {
      final String[] quotedDescr = descrs.split(" ");
      final String[] s = new String[quotedDescr.length];
      for (int i = 0; i < s.length; i++) {
        s[i] = parseQDString(quotedDescr[i].substring(1, quotedDescr[i].length() - 1).trim());
      }
      return s;
    } else {
      return new String[] {parseQDString(descrs)};
    }
  }


  /**
   * Returns a string with any HEX escaped characters encoded.
   *
   * @param  value  to parse
   *
   * @return  parsed value
   */
  public static String parseQDString(final String value)
  {
    if (value == null || !value.contains("\\")) {
      return value;
    }

    final int len = value.length();
    final StringBuilder sb = new StringBuilder(len);
    final StringBuilder hexValue = new StringBuilder();
    for (int i = 0; i < len; i++) {
      final char c = value.charAt(i);
      boolean appendHex = false;
      boolean appendValue = false;
      if (c == '\\' && i + 2 < len && Hex.isValidChar(value.charAt(i + 1)) && Hex.isValidChar(value.charAt(i + 2))) {
        hexValue.append(value.charAt(++i)).append(value.charAt(++i));
        if (i + 1 == len) {
          appendHex = true;
        }
      } else {
        appendHex = hexValue.length() > 0;
        appendValue = true;
      }
      if (appendHex) {
        sb.append(LdapUtils.utf8Encode(LdapUtils.hexDecode(hexValue.toString().toCharArray())));
        hexValue.setLength(0);
      }
      if (appendValue) {
        sb.append(c);
      }
    }
    return sb.toString();
  }


  /**
   * Parses the supplied OID string and returns its contents as a string array. If the string contains a dollar sign it
   * is assumed to be a multivalue OID of the form "value1 $ value2 $ value3". Otherwise, it is treated as a single
   * value OID.
   *
   * @param  oids  string to parse
   *
   * @return  array of oids
   */
  public static String[] parseOIDs(final String oids)
  {
    if (oids.contains("$")) {
      final String[] s = oids.split("\\$");
      for (int i = 0; i < s.length; i++) {
        s[i] = s[i].trim();
      }
      return s;
    } else {
      return new String[] {oids};
    }
  }


  /**
   * Parses the supplied number string and returns its contents as a string array.
   *
   * @param  numbers  string to parse
   *
   * @return  array of numbers
   */
  public static int[] parseNumbers(final String numbers)
  {
    final String[] s = numbers.split(" ");
    final int[] i = new int[s.length];
    for (int j = 0; j < i.length; j++) {
      i[j] = Integer.parseInt(s[j].trim());
    }
    return i;
  }


  /**
   * Returns a formatted string to describe the supplied descriptors.
   *
   * @param  descrs  to format
   *
   * @return  formatted string
   */
  public static String formatDescriptors(final String... descrs)
  {
    final StringBuilder sb = new StringBuilder();
    if (descrs.length == 1) {
      sb.append("'");
      encodeDescriptor(sb, descrs[0]);
      sb.append("' ");
    } else {
      sb.append("( ");
      for (String descr : descrs) {
        sb.append("'");
        encodeDescriptor(sb, descr);
        sb.append("' ");
      }
      sb.append(") ");
    }
    return sb.toString();
  }


  /**
   * Encodes the supplied descriptor. Hex encodes non-printable ASCII, including backslash and single quote.
   *
   * @param  sb  to append encoded descriptor to
   * @param  descr  to encode
   */
  private static void encodeDescriptor(final StringBuilder sb, final String descr)
  {
    final int len = descr.length();
    for (int i = 0; i < len; i++) {
      final char ch = descr.charAt(i);
      // CheckStyle:MagicNumber OFF
      if (ch <= 0x1F || ch >= 0x7F || ch == 0x5C || ch == 0x27) {
        final char[] hex = LdapUtils.hexEncode(ch);
        for (int j = 0; j < hex.length; j += 2) {
          sb.append('\\').append(hex[j]).append(hex[j + 1]);
        }
      } else {
        sb.append(ch);
      }
      // CheckStyle:MagicNumber ON
    }
  }


  /**
   * Returns a formatted string to describe the supplied OIDs.
   *
   * @param  oids  to format
   *
   * @return  formatted string
   */
  public static String formatOids(final String... oids)
  {
    final StringBuilder sb = new StringBuilder();
    if (oids.length == 1) {
      sb.append(oids[0]).append(" ");
    } else {
      sb.append("( ");
      for (int i = 0; i < oids.length; i++) {
        sb.append(oids[i]);
        if (i < oids.length - 1) {
          sb.append(" $ ");
        } else {
          sb.append(" ");
        }
      }
      sb.append(") ");
    }
    return sb.toString();
  }


  /**
   * Returns a formatted string to describe the supplied numbers.
   *
   * @param  numbers  to format
   *
   * @return  formatted string
   */
  public static String formatNumbers(final int... numbers)
  {
    final StringBuilder sb = new StringBuilder();
    if (numbers.length == 1) {
      sb.append(numbers[0]).append(" ");
    } else {
      sb.append("( ");
      for (int number : numbers) {
        sb.append(number).append(" ");
      }
      sb.append(") ");
    }
    return sb.toString();
  }


  /**
   * Searches for the supplied dn and returns its ldap entry.
   *
   * @param  factory  to obtain an LDAP connection from
   * @param  dn  to search for
   * @param  filter  search filter
   * @param  retAttrs  attributes to return
   *
   * @return  ldap entry
   *
   * @throws LdapException  if the search fails
   */
  public static LdapEntry getLdapEntry(
    final ConnectionFactory factory,
    final String dn,
    final String filter,
    final String... retAttrs)
    throws LdapException
  {
    final SearchOperation search = new SearchOperation(factory);
    final SearchResponse result = search.execute(SearchRequest.objectScopeSearchRequest(dn, retAttrs, filter));
    if (!result.isSuccess()) {
      throw new LdapException("Unsuccessful search for schema: " + result);
    }
    return result.getEntry();
  }
}
