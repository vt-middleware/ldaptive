/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

/**
 * Base schema bean for named schema elements.
 *
 * @author  Middleware Services
 */
public abstract class AbstractNamedSchemaElement extends AbstractSchemaElement
{

  /** Names. */
  private String[] names;

  /** Obsolete. */
  private boolean obsolete;


  /**
   * Returns the first name defined or null if no names are defined.
   *
   * @return  first name in the list
   */
  public String getName()
  {
    if (names != null && names.length > 0) {
      return names[0];
    }
    return null;
  }


  /**
   * Returns the names.
   *
   * @return  names
   */
  public String[] getNames()
  {
    return names;
  }


  /**
   * Sets the names.
   *
   * @param  s  names
   */
  public void setNames(final String[] s)
  {
    names = s;
  }


  /**
   * Returns whether the supplied string matches, ignoring case, any of the
   * names for this schema element.
   *
   * @param  s  to match
   *
   * @return  whether the supplied string matches a name
   */
  public boolean hasName(final String s)
  {
    if (names != null) {
      for (String name : names) {
        if (name.equalsIgnoreCase(s)) {
          return true;
        }
      }
    }
    return false;
  }


  /**
   * Returns whether this attribute type definition is obsolete.
   *
   * @return  whether this attribute type definition is obsolete
   */
  public boolean isObsolete()
  {
    return obsolete;
  }


  /**
   * Sets whether this attribute type definition is obsolete.
   *
   * @param  b  whether this attribute type definition is obsolete
   */
  public void setObsolete(final boolean b)
  {
    obsolete = b;
  }
}
