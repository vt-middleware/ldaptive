/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

/**
 * Enum for an attribute usage schema element.
 *
 * <pre>
   AttributeUsage =
     "userApplications"     /
     "directoryOperation"   /
     "distributedOperation" / ; DSA-shared
     "dSAOperation"           ; DSA-specific, value depends on server
 * </pre>
 *
 * @author  Middleware Services
 */
public enum AttributeUsage {

  /** user applications. */
  USER_APPLICATIONS("userApplications", false),

  /** directory operation. */
  DIRECTORY_OPERATION("directoryOperation", true),

  /** distributed operation. */
  DISTRIBUTED_OPERATION("distributedOperation", true),

  /** dSA operation. */
  DSA_OPERATION("dSAOperation", true);

  /** Name of this attribute usage. */
  private final String name;

  /** Whether this attribute usage is operational. */
  private final boolean operational;


  /**
   * Creates a new attribute usage.
   *
   * @param  s  name of this usage
   * @param  b  whether this usage is operational
   */
  AttributeUsage(final String s, final boolean b)
  {
    name = s;
    operational = b;
  }


  /**
   * Returns the name.
   *
   * @return  attribute usage name
   */
  public String getName()
  {
    return name;
  }


  /**
   * Whether this attribute usage is operational.
   *
   * @return  whether this attribute usage is operational
   */
  public boolean isOperational()
  {
    return operational;
  }


  /**
   * Returns the attribute usage for the supplied string name.
   *
   * @param  s  case insensitive name to find attribute usage for
   *
   * @return  attribute usage or null if name cannot be found
   */
  public static AttributeUsage parse(final String s)
  {
    for (AttributeUsage usage : AttributeUsage.values()) {
      if (usage.getName().equalsIgnoreCase(s)) {
        return usage;
      }
    }
    return null;
  }
}
