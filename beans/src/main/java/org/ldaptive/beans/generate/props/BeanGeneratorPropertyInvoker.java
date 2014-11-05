/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.generate.props;

import java.util.HashMap;
import java.util.Map;
import org.ldaptive.props.AbstractPropertyInvoker;

/**
 * Handles properties for {@link org.ldaptive.beans.generate.BeanGenerator}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3013 $ $Date: 2014-07-02 11:26:52 -0400 (Wed, 02 Jul 2014) $
 */
public class BeanGeneratorPropertyInvoker extends AbstractPropertyInvoker
{


  /**
   * Creates a new bean generator property invoker for the supplied class.
   *
   * @param  c  class that has setter methods
   */
  public BeanGeneratorPropertyInvoker(final Class<?> c)
  {
    initialize(c);
  }


  /** {@inheritDoc} */
  @Override
  @SuppressWarnings("unchecked")
  protected Object convertValue(final Class<?> type, final String value)
  {
    Object newValue = value;
    if (type != String.class) {
      if (Map.class.isAssignableFrom(type)) {
        newValue = new HashMap<String, Object>();

        final String[] keyValues = value.split(",");
        for (String keyValue : keyValues) {
          final String[] s = keyValue.split("=");
          if (s[1].endsWith(".class")) {
            ((Map) newValue).put(
              s[0],
              createTypeFromPropertyValue(
                Class.class,
                s[1].substring(0, s[1].indexOf(".class"))));
          } else if (s[1].startsWith("[")) {
            ((Map) newValue).put(
              s[0],
              createTypeFromPropertyValue(Class.class, s[1]));
          } else {
            ((Map) newValue).put(s[0], s[1]);
          }
        }
      } else {
        newValue = convertSimpleType(type, value);
      }
    }
    return newValue;
  }
}
