/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

/**
 * Enum for an object class schema element.
 *
 * <pre>
   ObjectClassType = "ABSTRACT" / "STRUCTURAL" / "AUXILIARY"
 * </pre>
 *
 * @author  Middleware Services
 */
public enum ObjectClassType {

  /** abstract. */
  ABSTRACT,

  /** structural. */
  STRUCTURAL,

  /** auxiliary. */
  AUXILIARY
}
