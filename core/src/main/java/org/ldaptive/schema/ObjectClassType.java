/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

/**
 * Enum for an attribute usage schema element.
 *
 * <pre>
   ObjectClassType = "ABSTRACT" / "STRUCTURAL" / "AUXILIARY"
 * </pre>
 *
 * @author  Middleware Services
 * @version  $Revision: 2940 $ $Date: 2014-03-31 11:10:46 -0400 (Mon, 31 Mar 2014) $
 */
public enum ObjectClassType {

  /** abstract. */
  ABSTRACT,

  /** structural. */
  STRUCTURAL,

  /** auxiliary. */
  AUXILIARY
}
