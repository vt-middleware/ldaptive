/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema.normalize;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Interface for LDAP attribute value normalizers.
 *
 * @author  Middleware Services
 */
public interface AttributeValueNormalizer
{


  /**
   * Returns a map of normalized value to un-normalized values for the supplied attribute values.
   *
   * @param  values  to normalize
   *
   * @return  normalized values as keys to map of un-normalized values
   */
  Map<String, List<String>> normalize(Collection<String> values);
}
