/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.generate.props;

import java.io.Reader;
import java.util.Properties;
import java.util.Set;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.beans.generate.BeanGenerator;
import org.ldaptive.props.AbstractPropertySource;
import org.ldaptive.props.DefaultConnectionFactoryPropertySource;
import org.ldaptive.schema.Schema;
import org.ldaptive.schema.SchemaFactory;

/**
 * Reads properties specific to {@link BeanGenerator} and returns an initialized
 * object of that type.
 *
 * @author  Middleware Services
 */
public final class BeanGeneratorPropertySource
  extends AbstractPropertySource<BeanGenerator>
{

  /** Invoker for bean generator. */
  private static final BeanGeneratorPropertyInvoker INVOKER =
    new BeanGeneratorPropertyInvoker(BeanGenerator.class);


  /**
   * Creates a new bean generator property source using the default properties
   * file.
   *
   * @param  bg  bean generator to invoke properties on
   */
  public BeanGeneratorPropertySource(final BeanGenerator bg)
  {
    this(bg, PROPERTIES_FILE);
  }


  /**
   * Creates a new bean generator property source.
   *
   * @param  pc  bean generator to invoke properties on
   * @param  paths  to read properties from
   */
  public BeanGeneratorPropertySource(
    final BeanGenerator pc,
    final String... paths)
  {
    this(pc, loadProperties(paths));
  }


  /**
   * Creates a new bean generator property source.
   *
   * @param  pc  bean generator to invoke properties on
   * @param  readers  to read properties from
   */
  public BeanGeneratorPropertySource(
    final BeanGenerator pc,
    final Reader... readers)
  {
    this(pc, loadProperties(readers));
  }


  /**
   * Creates a new bean generator property source.
   *
   * @param  pc  bean generator to invoke properties on
   * @param  props  to read properties from
   */
  public BeanGeneratorPropertySource(
    final BeanGenerator pc,
    final Properties props)
  {
    this(pc, PropertyDomain.LDAP, props);
  }


  /**
   * Creates a new bean generator property source.
   *
   * @param  pc  bean generator to invoke properties on
   * @param  domain  that properties are in
   * @param  props  to read properties from
   */
  public BeanGeneratorPropertySource(
    final BeanGenerator pc,
    final PropertyDomain domain,
    final Properties props)
  {
    super(pc, domain, props);
  }


  /** {@inheritDoc} */
  @Override
  public void initialize()
  {
    initializeObject(INVOKER);

    Schema schema = object.getSchema();
    if (schema == null) {
      final DefaultConnectionFactory cf = new DefaultConnectionFactory();
      final DefaultConnectionFactoryPropertySource cfPropSource =
        new DefaultConnectionFactoryPropertySource(
          cf,
          propertiesDomain,
          properties);
      cfPropSource.initialize();
      try {
        schema = SchemaFactory.createSchema(cf);
      } catch (LdapException e) {
        throw new IllegalArgumentException("Error reading schema from LDAP", e);
      }
      object.setSchema(schema);
    }
  }


  /**
   * Returns the property names for this property source.
   *
   * @return  all property names
   */
  public static Set<String> getProperties()
  {
    return INVOKER.getProperties();
  }
}
