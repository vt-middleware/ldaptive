/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

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
public class Modification
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
  private final Attribute attribute;


  /**
   * Creates a new modification.
   *
   * @param  type  of modification
   * @param  attr  attribute to modify
   */
  public Modification(final Type type, final Attribute attr)
  {
    operation = type;
    attribute = attr;
  }


  public Type getOperation()
  {
    return operation;
  }


  public Attribute getAttribute()
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
