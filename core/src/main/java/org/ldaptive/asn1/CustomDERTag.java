/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

/**
 * Describes the tag of an application-specific, context-specific, or private DER type where the tag name may be
 * specified for clarity in application code.
 *
 * @author  Middleware Services
 */
public class CustomDERTag extends AbstractDERTag
{

  /** Tag name. */
  private final String tagName;


  /**
   * Creates a new custom DER tag.
   *
   * @param  number  of the tag
   * @param  name  of the tag
   * @param  isConstructed  whether this tag is primitive or constructed
   */
  public CustomDERTag(final int number, final String name, final boolean isConstructed)
  {
    super(number, isConstructed);
    tagName = name;
  }


  @Override
  public String name()
  {
    return tagName;
  }


  @Override
  public String toString()
  {
    return new StringBuilder(name()).append("(").append(getTagNo()).append(")").toString();
  }
}
