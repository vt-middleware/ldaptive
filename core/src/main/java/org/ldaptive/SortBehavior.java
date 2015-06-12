/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enum to define how ldap result, entries, and attribute data should be sorted. Default sort behavior can be controlled
 * with the org.ldaptive.sortBehavior system property. This property must be the fully qualified name of a sort
 * behavior.
 *
 * @author  Middleware Services
 */
public enum SortBehavior {

  /** unordered results. */
  UNORDERED,

  /** ordered results. */
  ORDERED,

  /** sorted results. */
  SORTED;

  /** Sort behavior name. */
  public static final String SORT_BEHAVIOR = "org.ldaptive.sortBehavior";

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(SortBehavior.class);

  /** Default sort behavior. */
  private static SortBehavior defaultSortBehavior;

  /**
   * statically initialize the default sort behavior.
   */
  static {
    final String sb = System.getProperty(SORT_BEHAVIOR);
    if (sb != null) {
      try {
        final SortBehavior sortBehavior;
        if (sb.startsWith(SortBehavior.class.getName())) {
          sortBehavior = SortBehavior.valueOf(sb.substring(SortBehavior.class.getName().length() + 1, sb.length()));
        } else {
          sortBehavior = SortBehavior.valueOf(sb);
        }
        LOGGER.info("Setting default sort behavior to {}", sortBehavior);
        defaultSortBehavior = sortBehavior;
      } catch (IllegalArgumentException e) {
        LOGGER.error("Error instantiating {}", sb, e);
      }
    }
    if (defaultSortBehavior == null) {
      defaultSortBehavior = UNORDERED;
    }
  }


  /**
   * Returns the default sort behavior.
   *
   * @return  default sort behavior
   */
  public static SortBehavior getDefaultSortBehavior()
  {
    return defaultSortBehavior;
  }
}
