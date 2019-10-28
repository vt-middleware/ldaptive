/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.ldaptive.LdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods common to property source implementations.
 *
 * @param  <T>  type of object to invoke properties on
 *
 * @author  Middleware Services
 */
public abstract class AbstractPropertySource<T> implements PropertySource<T>
{

  /** Default file to read properties from, value is {@value}. */
  public static final String PROPERTIES_FILE = "classpath:/org/ldaptive/ldap.properties";

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Object to initialize with properties. */
  protected final T object;

  /** Domain that properties are in. */
  protected final PropertyDomain propertiesDomain;

  /** Properties to set. */
  protected final Properties properties;

  /** Properties that are not in the ldaptive domain. */
  protected final Map<String, Object> extraProps = new HashMap<>();


  /**
   * Creates a new abstract property source.
   *
   * @param  t  to set properties on
   * @param  pd  domain that properties reside in
   * @param  p  properties to set
   */
  public AbstractPropertySource(final T t, final PropertyDomain pd, final Properties p)
  {
    object = t;
    propertiesDomain = pd;
    properties = p;
  }


  /**
   * Creates properties from the supplied file paths. See {@link #loadProperties(Reader...)}.
   *
   * @param  paths  to read properties from
   *
   * @return  initialized properties object.
   */
  protected static Properties loadProperties(final String... paths)
  {
    try {
      final Reader[] readers = new Reader[paths.length];
      for (int i = 0; i < paths.length; i++) {
        readers[i] = new InputStreamReader(LdapUtils.getResource(paths[i]));
      }
      return loadProperties(readers);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }


  /**
   * Creates properties from the supplied reader. See {@link Properties#load(Reader)}. Readers supplied to this method
   * will be closed.
   *
   * @param  readers  to read properties from
   *
   * @return  initialized properties object.
   */
  protected static Properties loadProperties(final Reader... readers)
  {
    try {
      final Properties properties = new Properties();
      for (Reader r : readers) {
        try (r) {
          properties.load(r);
        }
      }
      return properties;
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }


  /**
   * Iterates over the properties and uses the invoker to set those properties on the object. Any properties that do not
   * belong to the object are set in the extraProps map.
   *
   * @param  invoker  to set properties on the object
   */
  protected void initializeObject(final PropertyInvoker invoker)
  {
    final Map<String, String> props = new HashMap<>();
    final Enumeration<?> en = properties.keys();
    if (en != null) {
      while (en.hasMoreElements()) {
        final String name = (String) en.nextElement();
        final String value = (String) properties.get(name);
        // add to provider specific properties if it isn't an ldaptive property
        if (!name.startsWith(PropertyDomain.LDAP.value())) {
          extraProps.put(name, value);
        } else {
          // strip out the method name
          final int split = name.lastIndexOf('.') + 1;
          final String propName = name.substring(split);
          final String propDomain = name.substring(0, split);
          // if we have this property, set it last
          if (propertiesDomain.value().equals(propDomain)) {
            if (invoker.hasProperty(propName)) {
              props.put(propName, value);
            }
            // check if this is a super class property
            // if it is, set it now, it may be overridden with the props map
          } else if (propertiesDomain.value().startsWith(propDomain)) {
            if (invoker.hasProperty(propName)) {
              invoker.setProperty(object, propName, value);
            }
          }
        }
      }
      for (Map.Entry<String, String> entry : props.entrySet()) {
        invoker.setProperty(object, entry.getKey(), entry.getValue());
      }
    }
  }
}
