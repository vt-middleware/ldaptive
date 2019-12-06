/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

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


  /**
   * Returns the encoder for this filter.
   *
   * @return  DER encoder
   */
  DEREncoder getEncoder();
}
