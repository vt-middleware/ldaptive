/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

/**
 * Interface for property driven object initialization.
 *
 * @param  <T>  type of object to invoke properties on
 *
 * @author  Middleware Services
 */
public interface PropertySource<T>
{

  /** Enum to define the domain for properties. */
  public enum PropertyDomain {

    /** ldap property domain. */
    LDAP("org.ldaptive."),

    /** auth property domain. */
    AUTH("org.ldaptive.auth."),

    /** pool property domain. */
    POOL("org.ldaptive.pool.");

    /** properties domain. */
    private final String domain;


    /**
     * Creates a new property domain.
     *
     * @param  s  properties domain
     */
    PropertyDomain(final String s)
    {
      domain = s;
    }


    /**
     * Returns the properties domain value.
     *
     * @return  properties domain
     */
    public String value()
    {
      return domain;
    }
  }


  /** Initializes the object for this property source. */
  void initialize();
}
