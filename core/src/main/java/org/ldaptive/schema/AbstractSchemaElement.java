/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import org.ldaptive.AbstractFreezable;

/**
 * Base class for schema elements.
 *
 * @param  <T>  type of element identifier key
 *
 * @author  Middleware Services
 */
public abstract class AbstractSchemaElement<T> extends AbstractFreezable implements SchemaElement<T>
{

  /** Description. */
  private String description;

  /** Extensions. */
  private Extensions extensions;


  @Override
  public void freeze()
  {
    super.freeze();
    freeze(extensions);
  }


  /**
   * Returns the description.
   *
   * @return  description
   */
  public String getDescription()
  {
    return description;
  }


  /**
   * Sets the description.
   *
   * @param  s  description
   */
  public void setDescription(final String s)
  {
    assertMutable();
    description = s;
  }


  /**
   * Returns the extensions.
   *
   * @return  extensions
   */
  public Extensions getExtensions()
  {
    return extensions;
  }


  /**
   * Sets the extensions.
   *
   * @param  e  extensions
   */
  public void setExtensions(final Extensions e)
  {
    assertMutable();
    extensions = e;
  }


  /**
   * Returns whether the supplied schema element has an extension name with a value of 'true'.
   *
   * @param  <T>  type of schema element
   * @param  schemaElement  to inspect
   * @param  extensionName  to read boolean from
   *
   * @return  whether syntax has this boolean extension
   */
  public static <T extends AbstractSchemaElement<?>> boolean containsBooleanExtension(
    final T schemaElement,
    final String extensionName)
  {
    if (schemaElement != null) {
      final Extensions exts = schemaElement.getExtensions();
      return exts != null && Boolean.parseBoolean(exts.getValue(extensionName));
    }
    return false;
  }


  // CheckStyle:EqualsHashCode OFF
  @Override
  public boolean equals(final Object o)
  {
    return super.equals(o);
  }
  // CheckStyle:EqualsHashCode ON


  @Override
  public abstract int hashCode();
}
