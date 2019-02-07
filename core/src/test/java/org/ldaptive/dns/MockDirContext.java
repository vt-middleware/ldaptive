/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dns;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.InitialDirContext;


/**
 * Mock JNDI dir context that accepts DNS records of a particular type.
 *
 * @author  Middleware Services
 */
public class MockDirContext extends InitialDirContext
{

  /** Internal map of attributes. */
  private final Map<String, BasicAttributes> map = new HashMap<>();

  /**
   * Default constructor.
   *
   * @throws  NamingException  never
   */
  public MockDirContext() throws NamingException {}


  /**
   * Add attributes to the mock context.
   *
   * @param  name  of the context
   * @param  attribute  name of the attribute
   * @param  values  of the attribute
   */
  public void addAttribute(final String name, final String attribute, final String... values)
  {
    BasicAttributes entry = map.get(name);
    if (entry == null) {
      entry = new BasicAttributes();
      map.put(name, entry);
    }
    final BasicAttribute attr = new BasicAttribute(attribute);
    Arrays.stream(values).forEach(attr::add);
    entry.put(attr);
  }


  @Override
  public Attributes getAttributes(final String name, final String[] ids)
    throws NamingException
  {
    final BasicAttributes entry = map.get(name);
    if (entry == null) {
      return new BasicAttributes();
    }
    final Set<String> idSet = new HashSet<>(Arrays.asList(ids));
    final BasicAttributes subset = new BasicAttributes();
    final NamingEnumeration<Attribute> attributes = entry.getAll();
    while (attributes.hasMore()) {
      final Attribute attribute = attributes.next();
      if (idSet.contains(attribute.getID())) {
        subset.put(attribute);
      }
    }
    return subset;
  }
}
