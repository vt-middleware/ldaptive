/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ldaptive.AbstractFreezable;
import org.ldaptive.LdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class containing a registry that associates element IDs and names with schema elements.
 *
 * @param  <K>  type of element key
 * @param  <V>  type of schema element
 *
 * @author  Middleware Services
 */
final class SchemaElementRegistry<K, V extends SchemaElement<K>> extends AbstractFreezable
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1187;

  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(SchemaElementRegistry.class);

  /** Map of keys to elements. */
  private Map<K, V> keyRegistry = Collections.emptyMap();

  /** Map of names to elements. */
  private Map<String, V> nameRegistry = Collections.emptyMap();

  /** Schema elements. */
  private List<V> schemaElements = Collections.emptyList();


  @Override
  public void freeze()
  {
    super.freeze();
    schemaElements.forEach(AbstractFreezable::freeze);
  }


  /**
   * Returns the schema elements.
   *
   * @return  schema elements
   */
  public Collection<V> getElements()
  {
    return Collections.unmodifiableList(schemaElements);
  }


  /**
   * Returns the element with the supplied identifier.
   *
   * @param  key  element identifier
   *
   * @return  element or null if key does not exist
   */
  public V getElementByKey(final K key)
  {
    if (key != null && keyRegistry.containsKey(key)) {
      return keyRegistry.get(key);
    }
    return null;
  }


  /**
   * Returns the element with the supplied name.
   *
   * @param  name  element name
   *
   * @return  element or null if name does not exist
   */
  public V getElementByName(final String name)
  {
    if (name != null) {
      final String lowerCaseName = LdapUtils.toLowerCase(name);
      if (nameRegistry.containsKey(lowerCaseName)) {
        return nameRegistry.get(lowerCaseName);
      }
    }
    return null;
  }


  /**
   * Sets the schema elements.
   *
   * @param  c  collections of elements
   */
  public void setElements(final Collection<V> c)
  {
    assertMutable();
    parseElements(c);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof SchemaElementRegistry) {
      final SchemaElementRegistry<?, ?> v = (SchemaElementRegistry<?, ?>) o;
      return LdapUtils.areEqual(schemaElements, v.schemaElements) &&
        LdapUtils.areEqual(keyRegistry, v.keyRegistry) &&
        LdapUtils.areEqual(nameRegistry, v.nameRegistry);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, schemaElements, keyRegistry, nameRegistry);
  }


  @Override
  public String toString()
  {
    return "[" + getClass().getName() + "@" + hashCode() + "::schemaElements=" + schemaElements + "]";
  }


  /**
   * Iterates over the supplied collection and updates the registries with each element.
   *
   * @param  c  to parse
   */
  private void parseElements(final Collection<V> c)
  {
    schemaElements = new ArrayList<>(c.size());
    keyRegistry = new HashMap<>();
    nameRegistry = new HashMap<>();
    for (V v : c) {
      final K key = v.getElementKey();
      if (keyRegistry.containsKey(key)) {
        logger.warn("Duplicate key detected for {} and {}", v, keyRegistry.get(key));
      } else {
        keyRegistry.put(key, v);
      }
      if (v instanceof NamedElement) {
        for (String n : ((NamedElement) v).getNames()) {
          final String lowerCaseName = LdapUtils.toLowerCase(n);
          if (nameRegistry.containsKey(lowerCaseName)) {
            logger.warn("Duplicate name detected for {} and {}", v, nameRegistry.get(lowerCaseName));
          } else {
            nameRegistry.put(lowerCaseName, v);
          }
        }
      }
      schemaElements.add(v);
    }
  }
}
