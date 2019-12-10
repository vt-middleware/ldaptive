/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Enum to define the type of search scope.
 *
 * <pre>
   scope           ENUMERATED {
     baseObject              (0),
     singleLevel             (1),
     wholeSubtree            (2),
     subordinateSubtree      (3),
     ...  }
 * </pre>
 *
 * @author  Middleware Services
 */
public enum SearchScope {

  /** base object search. */
  OBJECT,

  /** single level search. */
  ONELEVEL,

  /** whole subtree search. */
  SUBTREE,

  /** subordinate subtree search. See draft-sermersheim-ldap-subordinate-scope. */
  SUBORDINATE
}
