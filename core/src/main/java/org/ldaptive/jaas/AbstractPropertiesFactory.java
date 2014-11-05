/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.util.Map;
import java.util.Properties;
import org.ldaptive.props.PropertySource.PropertyDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides implementation common to properties based factories.
 *
 * @author  Middleware Services
 */
public abstract class AbstractPropertiesFactory
{

  /** Cache ID option used on the JAAS config. */
  public static final String CACHE_ID = "cacheId";

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());


  /**
   * Returns context specific properties based on the supplied JAAS options.
   *
   * @param  options  to read properties from
   *
   * @return  properties
   */
  protected static Properties createProperties(final Map<String, ?> options)
  {
    final Properties p = new Properties();
    for (Map.Entry<String, ?> entry : options.entrySet()) {
      // if property name contains a dot, it isn't an ldaptive property
      // else add the domain to the ldaptive properties
      if (entry.getKey().contains(".")) {
        p.setProperty(entry.getKey(), entry.getValue().toString());
      } else {
        p.setProperty(
          PropertyDomain.AUTH.value() + entry.getKey(),
          entry.getValue().toString());
      }
    }
    return p;
  }
}
