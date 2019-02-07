/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * LDAP modification defined as:
 *
 * <pre>
   modification    PartialAttribute

   PartialAttribute ::= SEQUENCE {
     type       AttributeDescription,
     vals       SET OF value AttributeValue }
 * </pre>
 *
 * @author  Middleware Services
 */
public class AttributeModification
{
  /** Modification type. */
  public enum Type {

    /** Add a new attribute. */
    ADD,

    /** Delete an attribute. */
    DELETE,

    /** Replace an attribute. */
    REPLACE,

    /** Increment the value of an attribute. */
    INCREMENT,
  }

  /** Modification type. */
  private final Type operation;

  /** Attribute to modify. */
  private final LdapAttribute attribute;


  /**
   * Creates a new modification.
   *
   * @param  type  of modification
   * @param  attr  attribute to modify
   */
  public AttributeModification(final Type type, final LdapAttribute attr)
  {
    operation = type;
    attribute = attr;
  }


  public Type getOperation()
  {
    return operation;
  }


  public LdapAttribute getAttribute()
  {
    return attribute;
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("operation=").append(operation).append(", ")
      .append("attribute=").append(attribute).toString();
  }
}
