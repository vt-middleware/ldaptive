/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import java.nio.charset.StandardCharsets;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.DEREncoder;

/**
 * LDAP search filter defined as:
 *
 * <pre>
   Filter ::= CHOICE {
     and             [0] SET SIZE (1..MAX) OF filter Filter,
     or              [1] SET SIZE (1..MAX) OF filter Filter,
     not             [2] Filter,
     equalityMatch   [3] AttributeValueAssertion,
     substrings      [4] SubstringFilter,
     greaterOrEqual  [5] AttributeValueAssertion,
     lessOrEqual     [6] AttributeValueAssertion,
     present         [7] AttributeDescription,
     approxMatch     [8] AttributeValueAssertion,
     extensibleMatch [9] MatchingRuleAssertion,
     ...  }

   SubstringFilter ::= SEQUENCE {
     type           AttributeDescription,
     substrings     SEQUENCE SIZE (1..MAX) OF substring CHOICE {
     initial [0] AssertionValue,  -- can occur at most once
     any     [1] AssertionValue,
     final   [2] AssertionValue } -- can occur at most once
   }

   MatchingRuleAssertion ::= SEQUENCE {
     matchingRule    [1] MatchingRuleId OPTIONAL,
     type            [2] AttributeDescription OPTIONAL,
     matchValue      [3] AssertionValue,
     dnAttributes    [4] BOOLEAN DEFAULT FALSE }
 * </pre>
 *
 * @author  Middleware Services
 */
public interface Filter
{


  /** Filter type. */
  enum Type {

    /** And filter. */
    AND,

    /** Or filter. */
    OR,

    /** Not filter. */
    NOT,

    /** Equality filter */
    EQUALITY,

    /** Substring filter. */
    SUBSTRING,

    /** Greater or equal filter. */
    GREATER_OR_EQUAL,

    /** Less or equal filter. */
    LESS_OR_EQUAL,

    /** Presence filter. */
    PRESENCE,

    /** Approximate match filter. */
    APPROXIMATE_MATCH,

    /** Extensible match filter. */
    EXTENSIBLE_MATCH,
  }


  /** Regular expression that matches an attribute description. */
  String ATTRIBUTE_DESC = "[\\p{Alnum};\\-\\.]+";

  /** Regular expression that matches an assertion value. */
  String ASSERTION_VALUE = "([^\\)]*+)";


  /**
   * Returns the encoder for this filter.
   *
   * @return  DER encoder
   */
  DEREncoder getEncoder();


  /**
   * Escapes the supplied string per RFC 4515.
   *
   * @param  s  to escape
   *
   * @return  escaped string
   */
  static String escape(final String s)
  {
    final StringBuilder sb = new StringBuilder(s.length());
    final byte[] utf8 = s.getBytes(StandardCharsets.UTF_8);
    // CheckStyle:MagicNumber OFF
    // optimize if ASCII
    if (s.length() == utf8.length) {
      for (byte b : utf8) {
        if (b <= 0x1F || b == 0x28 || b == 0x29 || b == 0x2A || b == 0x5C || b == 0x7F) {
          sb.append('\\').append(LdapUtils.hexEncode(b));
        } else {
          sb.append((char) b);
        }
      }
    } else {
      int multiByte = 0;
      for (byte b : utf8) {
        if (multiByte > 0) {
          sb.append('\\').append(LdapUtils.hexEncode(b));
          multiByte--;
        } else if ((b & 0x7F) == b) {
          if (b <= 0x1F || b == 0x28 || b == 0x29 || b == 0x2A || b == 0x5C || b == 0x7F) {
            sb.append('\\').append(LdapUtils.hexEncode(b));
          } else {
            sb.append((char) b);
          }
        } else {
          // 2 byte character
          if ((b & 0xE0) == 0xC0) {
            multiByte = 1;
            // 3 byte character
          } else if ((b & 0xF0) == 0xE0) {
            multiByte = 2;
            // 4 byte character
          } else if ((b & 0xF8) == 0xF0) {
            multiByte = 3;
          } else {
            throw new IllegalStateException("Could not read UTF-8 string encoding");
          }
          sb.append('\\').append(LdapUtils.hexEncode(b));
        }
      }
    }
    // CheckStyle:MagicNumber ON
    return sb.toString();
  }
}
