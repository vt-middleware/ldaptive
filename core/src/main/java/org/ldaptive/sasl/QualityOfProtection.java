/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

/**
 * Enum to define SASL quality of protection.
 *
 * @author  Middleware Services
 */
public enum QualityOfProtection {

  /** Authentication only. */
  AUTH("auth"),

  /** Authentication with integrity protection. */
  AUTH_INT("auth-int"),

  /** Authentication with integrity and privacy protection. */
  AUTH_CONF("auth-conf");

  /** Quality of protection strings. */
  private final String qop;


  /**
   * Creates a new quality of protection.
   *
   * @param  s  quality of protection strings
   */
  QualityOfProtection(final String s)
  {
    qop = s;
  }


  /**
   * Returns the protection string.
   *
   * @return  protection string
   */
  public String string()
  {
    return qop;
  }


  /**
   * Returns the quality of protection for the supplied protection string.
   *
   * @param  s  to find quality of protection for
   *
   * @return  quality of protection
   */
  public static QualityOfProtection fromString(final String s)
  {
    for (QualityOfProtection p : values()) {
      if (p.string().equalsIgnoreCase(s)) {
        return p;
      }
    }
    return null;
  }
}
